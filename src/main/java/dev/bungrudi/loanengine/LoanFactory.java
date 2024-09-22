package dev.bungrudi.loanengine;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface LoanFactory {
    Loan createLoan(String loanId, BigDecimal loanAmount, BigDecimal interestRate, int numberOfWeeks, LocalDate startDate);
}
