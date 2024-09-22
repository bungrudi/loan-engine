package dev.bungrudi.loanengine;

import lombok.Getter;
import lombok.Setter;
import lombok.AccessLevel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Getter
@Setter
public class Loan {
    private String loanId;
    private BigDecimal loanAmount;
    private BigDecimal totalAmount;
    private double interestRate;
    private BigDecimal weeklyPayment;
    private int numberOfWeeks;
    @Setter(AccessLevel.NONE)
    private List<PaymentDue> schedule;
    @Setter(AccessLevel.NONE)
    private BigDecimal outstanding;
    private LocalDate startDate;
    private LocalDate firstPaymentDate;
    private LoanStanding standing;

    private static final int SCALE = 2;
    /**
     * banker's rounding
     */
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    public Loan(String loanId, BigDecimal loanAmount, BigDecimal totalAmount, double interestRate, int numberOfWeeks, LocalDate startDate, List<PaymentDue> schedule) {
        this.loanId = loanId;
        this.loanAmount = loanAmount;
        this.totalAmount = totalAmount;
        this.interestRate = interestRate;
        this.weeklyPayment = this.totalAmount.divide(BigDecimal.valueOf(numberOfWeeks), SCALE, ROUNDING_MODE);
        this.numberOfWeeks = numberOfWeeks;
        this.startDate = startDate;
        this.firstPaymentDate = startDate.plusDays(7);
        this.schedule = schedule;
        this.outstanding = this.totalAmount;
        this.standing = LoanStanding.GOOD_STANDING;
    }

    public BigDecimal getLoanAmount() {
        return loanAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getWeeklyPayment() {
        return weeklyPayment;
    }

    public BigDecimal getOutstanding() {
        return outstanding;
    }

    public int getCurrentWeek(LocalDate date) {
        if (date.isBefore(startDate.plusDays(1))) {
            return -1;
        }
        long daysSinceStart = ChronoUnit.DAYS.between(startDate, date);
        return Math.min(numberOfWeeks - 1, (int) ((daysSinceStart-1) / 7));
    }

    public List<PaymentDue> getSchedule() {
        return schedule; // ideally we should return immutable wrapper
    }

    public void makePayment(BigDecimal amount, LocalDate paymentDate) {
        int currentWeek = getCurrentWeek(paymentDate);
        PaymentDue nextPayment = null;
        for (PaymentDue payment : schedule) {
            if (!payment.isPaid()) {
                nextPayment = payment;
                break;
            }
        }
        
        if (nextPayment == null) {
            throw new IllegalStateException("No more payments due");
        }
        
        if (nextPayment.getWeekNumber() > currentWeek) {
            throw new IllegalStateException("Payment is not yet due");
        }
        
        if (amount.compareTo(BigDecimal.valueOf(nextPayment.getAmountDue())) != 0) {
            throw new IllegalArgumentException("Payment must be exact amount due for week " + nextPayment.getWeekNumber());
        }
        
        nextPayment.markAsPaid(paymentDate);
        updateOutstanding();
    }

    private void updateOutstanding() {
        outstanding = totalAmount;
        for (PaymentDue payment : schedule) {
            if (payment.isPaid()) {
                outstanding = outstanding.subtract(BigDecimal.valueOf(payment.getAmountDue()));
            }
        }
    }

    public boolean isDelinquent() {
        return standing == LoanStanding.DELINQUENT;
    }

    public void updateStatus(LocalDate currentDate) {
        int currentWeek = getCurrentWeek(currentDate);
        if (currentWeek < 0) {
            standing = LoanStanding.GOOD_STANDING;
            return;
        }

        int unpaidCount = 0;
        for (int i = 0; i < currentWeek; i++) {
            if (!schedule.get(i).isPaid()) {
                unpaidCount++;
            }
        }

        if (unpaidCount == 0) {
            standing = LoanStanding.GOOD_STANDING;
        } else if (unpaidCount == 1) {
            standing = LoanStanding.LATE;
        } else {
            standing = LoanStanding.DELINQUENT;
        }

        if (currentWeek == numberOfWeeks - 1 && unpaidCount == 0) {
            standing = LoanStanding.CLOSED;
        }
    }

    @Override
    public String toString() {
        return "Loan{" +
                "loanId='" + loanId + '\'' +
                ", totalAmount=" + loanAmount +
                ", weeklyPayment=" + weeklyPayment +
                ", numberOfWeeks=" + numberOfWeeks +
                ", startDate=" + startDate +
                ", firstPaymentDate=" + firstPaymentDate +
                ", schedule=" + schedule +
                ", outstanding=" + outstanding +
                ", standing=" + standing +
                '}';
    }
}
