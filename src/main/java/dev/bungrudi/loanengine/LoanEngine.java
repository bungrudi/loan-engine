package dev.bungrudi.loanengine;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class LoanEngine {
    private Map<String, Loan> loans;
    private LocalDate currentDate;
    private LoanFactory loanFactory;

    public LoanEngine(LocalDate currentDate, LoanFactory loanFactory) {
        this.loans = new HashMap<>();
        this.currentDate = currentDate;
        this.loanFactory = loanFactory;
    }

    public Loan createLoan(String loanId, double loanAmount, double interestRate, int numberOfWeeks) {
        Loan loan = loanFactory.createLoan(loanId, loanAmount, interestRate, numberOfWeeks, currentDate);
        loans.put(loanId, loan);
        return loan;
    }

    public void makePayment(String loanId, double amount) {
        Loan loan = getLoan(loanId);
        loan.makePayment(amount, currentDate);
        loan.updateStatus(currentDate);
    }

    public double getOutstanding(String loanId) {
        Loan loan = getLoan(loanId);
        return loan.getOutstanding();
    }

    public boolean isDelinquent(String loanId) {
        Loan loan = getLoan(loanId);
        return loan.isDelinquent();
    }

    private Loan getLoan(String loanId) {
        Loan loan = loans.get(loanId);
        if (loan == null) {
            throw new IllegalArgumentException("Loan not found");
        }
        return loan;
    }

    public void setCurrentDate(LocalDate newDate) {
        this.currentDate = newDate;
    }

    public void endOfDay() {
        currentDate = currentDate.plusDays(1);
        for (Loan loan : loans.values()) {
            loan.updateStatus(currentDate);
        }
        // Other end-of-day activities could be added here
    }

    public static void main(String[] args) {
        // Main method left empty for now
    }
}
