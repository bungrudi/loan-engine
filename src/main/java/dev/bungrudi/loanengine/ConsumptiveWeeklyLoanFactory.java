package dev.bungrudi.loanengine;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ConsumptiveWeeklyLoanFactory implements LoanFactory {
    private static final int WEEKS_PER_MONTH = 4;
    private static final int SCALE = 2;

    @Override
    public Loan createLoan(String loanId, BigDecimal loanAmount, BigDecimal annualInterestRate, int numberOfWeeks, LocalDate startDate) {
//        MathContext mathContext = new MathContext(3, RoundingMode.CEILING);

        // Round up number of weeks to nearest month
        int numberOfMonths = (numberOfWeeks + WEEKS_PER_MONTH - 1) / WEEKS_PER_MONTH;

        // interest rate annualized
        BigDecimal interestRate = annualInterestRate.multiply(
            BigDecimal.valueOf(numberOfMonths).divide(BigDecimal.valueOf(12), MathContext.DECIMAL32))
            .setScale(4, RoundingMode.CEILING);

        // Calculate total interest (flat rate)
        BigDecimal totalInterest = loanAmount.multiply(interestRate);

        // Calculate total amount to be repaid
        BigDecimal totalAmount = loanAmount.add(totalInterest);

        // Calculate weekly payment
        BigDecimal weeklyPayment = totalAmount.divide(BigDecimal.valueOf(numberOfWeeks), SCALE, RoundingMode.CEILING);

        List<PaymentDue> schedule = createSchedule(numberOfWeeks, weeklyPayment, startDate.plusDays(7));

        // adjust total amount to take into account round-up
        totalAmount = weeklyPayment.multiply(BigDecimal.valueOf(numberOfWeeks));
        
        return new Loan(loanId, loanAmount, totalAmount, annualInterestRate.doubleValue(), numberOfWeeks, startDate, schedule);
    }

    private List<PaymentDue> createSchedule(int numberOfWeeks, BigDecimal weeklyPayment, LocalDate firstPaymentDate) {
        List<PaymentDue> schedule = new ArrayList<>();
        for (int i = 0; i < numberOfWeeks; i++) {
            LocalDate dueDate = firstPaymentDate.plusWeeks(i);
            schedule.add(new PaymentDue(i, weeklyPayment.doubleValue(), dueDate));
        }
        return schedule;
    }
}
