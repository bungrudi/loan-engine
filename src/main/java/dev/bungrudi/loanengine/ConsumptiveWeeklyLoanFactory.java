package dev.bungrudi.loanengine;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ConsumptiveWeeklyLoanFactory implements LoanFactory {
    @Override
    public Loan createLoan(String loanId, double loanAmount, double interestRate, int numberOfWeeks, LocalDate startDate) {
        double totalAmount = loanAmount * (1 + interestRate);
        double weeklyPayment = totalAmount / numberOfWeeks;
        List<PaymentDue> schedule = createSchedule(numberOfWeeks, weeklyPayment, startDate.plusDays(7));
        return new Loan(loanId, loanAmount, interestRate, numberOfWeeks, startDate, schedule);
    }

    private List<PaymentDue> createSchedule(int numberOfWeeks, double weeklyPayment, LocalDate firstPaymentDate) {
        List<PaymentDue> schedule = new ArrayList<>();
        for (int i = 0; i < numberOfWeeks; i++) {
            LocalDate dueDate = firstPaymentDate.plusWeeks(i);
            schedule.add(new PaymentDue(i, weeklyPayment, dueDate));
        }
        return schedule;
    }
}
