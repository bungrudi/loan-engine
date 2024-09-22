package dev.bungrudi.loanengine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ConsumptiveWeeklyLoanFactory implements LoanFactory {
    private static final int WEEKS_PER_MONTH = 4;
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    @Override
    public Loan createLoan(String loanId, double loanAmount, double annualInterestRate, int numberOfWeeks, LocalDate startDate) {
        BigDecimal principal = BigDecimal.valueOf(loanAmount);
        BigDecimal rate = BigDecimal.valueOf(annualInterestRate);
        
        // Round up number of weeks to nearest month
        int numberOfMonths = (numberOfWeeks + WEEKS_PER_MONTH - 1) / WEEKS_PER_MONTH;

        // Calculate total interest (flat rate)
        BigDecimal totalInterest = principal.multiply(rate.divide(BigDecimal.valueOf(12), SCALE, ROUNDING_MODE))
                                            .multiply(BigDecimal.valueOf(numberOfMonths));

        // Calculate total amount to be repaid
        BigDecimal totalAmount = principal.add(totalInterest);

        // Calculate weekly payment
        BigDecimal weeklyPayment = totalAmount.divide(BigDecimal.valueOf(numberOfWeeks), SCALE, ROUNDING_MODE);

        List<PaymentDue> schedule = createSchedule(numberOfWeeks, weeklyPayment, startDate.plusDays(7));
        
        return new Loan(loanId, principal, totalAmount, annualInterestRate, numberOfWeeks, startDate, schedule);
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
