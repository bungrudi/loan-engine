package dev.bungrudi.loanengine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class ConsumptiveWeeklyLoanFactoryTest {

    private ConsumptiveWeeklyLoanFactory factory;
    private LocalDate startDate;
    private static final BigDecimal MINIMUM_LOAN_AMOUNT = BigDecimal.valueOf(1_000_000.0);
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    @BeforeEach
    void setUp() {
        factory = new ConsumptiveWeeklyLoanFactory();
        startDate = LocalDate.of(2023, 1, 1);
    }

    @Test
    void given_validInputs_when_createLoan_then_returnCorrectLoan() {
        Loan loan = factory.createLoan("L001", MINIMUM_LOAN_AMOUNT.doubleValue(), 0.1, 10, startDate);
        
        assertNotNull(loan);
        assertEquals("L001", loan.getLoanId());
        assertEquals(MINIMUM_LOAN_AMOUNT, loan.getLoanAmount());
        assertEquals(10, loan.getNumberOfWeeks());
        assertEquals(startDate, loan.getStartDate());
    }

    @Test
    void given_loanInputs_when_createLoan_then_calculateCorrectWeeklyPayment() {
        Loan loan = factory.createLoan("L002", MINIMUM_LOAN_AMOUNT.doubleValue(), 0.1, 10, startDate);
        
        BigDecimal expectedTotalAmount = MINIMUM_LOAN_AMOUNT.multiply(BigDecimal.ONE.add(BigDecimal.valueOf(0.1).multiply(BigDecimal.valueOf(3)).divide(BigDecimal.valueOf(12), SCALE, ROUNDING_MODE))); // 3 months interest
        assertEquals(expectedTotalAmount.divide(BigDecimal.valueOf(10), SCALE, ROUNDING_MODE), loan.getWeeklyPayment());
    }

    @Test
    void given_loanInputs_when_createLoan_then_createCorrectSchedule() {
        Loan loan = factory.createLoan("L003", MINIMUM_LOAN_AMOUNT.doubleValue(), 0.1, 10, startDate);
        
        assertEquals(10, loan.getSchedule().size());
        assertEquals(startDate.plusDays(7), loan.getSchedule().get(0).getDueDate());
        assertEquals(startDate.plusDays(14), loan.getSchedule().get(1).getDueDate());
    }

    @Test
    void given_zeroInterestRate_when_createLoan_then_totalAmountEqualsPrincipal() {
        Loan loan = factory.createLoan("L004", MINIMUM_LOAN_AMOUNT.doubleValue(), 0, 10, startDate);
        
        assertEquals(MINIMUM_LOAN_AMOUNT, loan.getLoanAmount());
    }

    @Test
    void given_oneWeekLoan_when_createLoan_then_createSinglePaymentSchedule() {
        Loan loan = factory.createLoan("L005", MINIMUM_LOAN_AMOUNT.doubleValue(), 0.1, 1, startDate);
        
        assertEquals(1, loan.getSchedule().size());
        BigDecimal expectedTotalAmount = MINIMUM_LOAN_AMOUNT.multiply(BigDecimal.ONE.add(BigDecimal.valueOf(0.1).divide(BigDecimal.valueOf(12), SCALE, ROUNDING_MODE))); // 1 month interest
        assertEquals(expectedTotalAmount, loan.getWeeklyPayment());
    }

    @Test
    void given_loanInputs_when_createLoan_then_setCorrectFirstPaymentDate() {
        Loan loan = factory.createLoan("L006", MINIMUM_LOAN_AMOUNT.doubleValue(), 0.1, 10, startDate);
        
        assertEquals(startDate.plusDays(7), loan.getFirstPaymentDate());
    }

    @Test
    void given_20WeekLoan_when_createLoan_then_calculateCorrectOutstanding() {
        Loan loan = factory.createLoan("L008", MINIMUM_LOAN_AMOUNT.doubleValue(), 0.1, 20, startDate);
        
        BigDecimal expectedInterest = MINIMUM_LOAN_AMOUNT.multiply(BigDecimal.valueOf(0.1).multiply(BigDecimal.valueOf(5)).divide(BigDecimal.valueOf(12), SCALE, ROUNDING_MODE)); // 5 months interest
        BigDecimal expectedTotal = MINIMUM_LOAN_AMOUNT.add(expectedInterest);
        assertEquals(expectedTotal, loan.getOutstanding());
    }

    @Test
    void given_50WeekLoan_when_createLoan_then_calculateCorrectOutstanding() {
        Loan loan = factory.createLoan("L009", MINIMUM_LOAN_AMOUNT.doubleValue(), 0.1, 50, startDate);
        
        BigDecimal expectedInterest = MINIMUM_LOAN_AMOUNT.multiply(BigDecimal.valueOf(0.1)); // 12 months (1 year) interest
        BigDecimal expectedTotal = MINIMUM_LOAN_AMOUNT.add(expectedInterest);
        assertEquals(expectedTotal, loan.getOutstanding());
    }

    @Test
    void given_72WeekLoan_when_createLoan_then_calculateCorrectOutstanding() {
        Loan loan = factory.createLoan("L010", MINIMUM_LOAN_AMOUNT.doubleValue(), 0.1, 72, startDate);
        
        BigDecimal expectedInterest = MINIMUM_LOAN_AMOUNT.multiply(BigDecimal.valueOf(0.1).multiply(BigDecimal.valueOf(18)).divide(BigDecimal.valueOf(12), SCALE, ROUNDING_MODE)); // 18 months interest
        BigDecimal expectedTotal = MINIMUM_LOAN_AMOUNT.add(expectedInterest);
        assertEquals(expectedTotal, loan.getOutstanding());
    }
}
