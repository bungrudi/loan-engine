package dev.bungrudi.loanengine;

import java.time.LocalDate;

public interface LoanFactory {
    Loan createLoan(String loanId, double loanAmount, double interestRate, int numberOfWeeks, LocalDate startDate);
}
