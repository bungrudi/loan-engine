package dev.bungrudi.loanengine;

import lombok.Getter;
import lombok.Setter;
import lombok.AccessLevel;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Getter
@Setter
public class Loan {
    private String loanId;
    private double totalAmount;
    private double weeklyPayment;
    private int numberOfWeeks;
    @Setter(AccessLevel.NONE)
    private List<PaymentDue> schedule;
    @Setter(AccessLevel.NONE)
    private double outstanding;
    private LocalDate startDate;
    private LocalDate firstPaymentDate;
    private LoanStanding standing;

    public Loan(String loanId, double loanAmount, double interestRate, int numberOfWeeks, LocalDate startDate, List<PaymentDue> schedule) {
        this.loanId = loanId;
        this.totalAmount = loanAmount * (1 + interestRate);
        this.weeklyPayment = this.totalAmount / numberOfWeeks;
        this.numberOfWeeks = numberOfWeeks;
        this.startDate = startDate;
        this.firstPaymentDate = startDate.plusDays(7);
        this.schedule = schedule;
        this.outstanding = this.totalAmount;
        this.standing = LoanStanding.GOOD_STANDING;
    }

    public int getCurrentWeek(LocalDate date) {
        if (date.isBefore(startDate.plusDays(1))) {
            return -1;
        }
        long daysSinceStart = ChronoUnit.DAYS.between(startDate, date);
        return Math.min(numberOfWeeks - 1, (int) ((daysSinceStart-1) / 7));
    }

    public double getOutstanding() {
        return outstanding;
    }

    public List<PaymentDue> getSchedule() {
        return schedule; // ideally we should return immutable wrapper
    }

    public void makePayment(double amount, LocalDate paymentDate) {
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
        
        if (amount != nextPayment.getAmountDue()) {
            throw new IllegalArgumentException("Payment must be exact amount due for week " + nextPayment.getWeekNumber());
        }
        
        nextPayment.markAsPaid(paymentDate);
        updateOutstanding();
    }

    private void updateOutstanding() {
        outstanding = totalAmount;
        for (PaymentDue payment : schedule) {
            if (payment.isPaid()) {
                outstanding -= payment.getAmountDue();
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
                ", totalAmount=" + totalAmount +
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
