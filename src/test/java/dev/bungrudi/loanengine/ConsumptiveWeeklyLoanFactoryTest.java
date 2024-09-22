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
    private static final BigDecimal LOAN_AMOUNT = BigDecimal.valueOf(1_000_000.0);
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    @BeforeEach
    void setUp() {
        factory = new ConsumptiveWeeklyLoanFactory();
        startDate = LocalDate.of(2023, 1, 1);
    }

    @Test
    void given_validInputs_when_createLoan_then_returnCorrectLoan() {
        Loan loan = factory.createLoan("L001", LOAN_AMOUNT, BigDecimal.valueOf(0.1), 10, startDate);
        
        assertNotNull(loan);
        assertEquals("L001", loan.getLoanId());
        assertEquals(LOAN_AMOUNT, loan.getLoanAmount());
        assertEquals(10, loan.getNumberOfWeeks());
        assertEquals(startDate, loan.getStartDate());
    }

    @Test
    void given_loanInputs_when_createLoan_then_createCorrectSchedule() {
        Loan loan = factory.createLoan("L003", LOAN_AMOUNT, BigDecimal.valueOf(0.1), 10, startDate);
        
        assertEquals(10, loan.getSchedule().size());
        assertEquals(startDate.plusDays(7), loan.getSchedule().get(0).getDueDate());
        assertEquals(startDate.plusDays(14), loan.getSchedule().get(1).getDueDate());
    }

    @Test
    void given_zeroInterestRate_when_createLoan_then_totalAmountEqualsPrincipal() {
        Loan loan = factory.createLoan("L004", LOAN_AMOUNT, BigDecimal.ZERO, 10, startDate);
        
        assertEquals(LOAN_AMOUNT, loan.getLoanAmount());
    }

    @Test
    void given_oneWeekLoan_when_createLoan_then_createSinglePaymentSchedule() {
        Loan loan = factory.createLoan("L005", LOAN_AMOUNT, BigDecimal.valueOf(0.1), 1, startDate);
        
        assertEquals(1, loan.getSchedule().size());
    }

    @Test
    void given_loanInputs_when_createLoan_then_setCorrectFirstPaymentDate() {
        Loan loan = factory.createLoan("L006", LOAN_AMOUNT, BigDecimal.valueOf(0.1), 10, startDate);
        
        assertEquals(startDate.plusDays(7), loan.getFirstPaymentDate());
    }

    @Test
    void given_20WeekLoan_when_createLoan_then_calculateCorrectOutstanding() {
        /**
         * annual rate: 0.1
         * loan amount: 1_000_000
         * duration in weeks: 20
         * duration in months: 5
         * interest rate: 0.0417
         * interest: 41_700
         * total loan: 1_041_700
         * weekly installment: 52_085
         */
        Loan loan = factory.createLoan("L008", LOAN_AMOUNT, BigDecimal.valueOf(0.1), 20, startDate);

        BigDecimal expectedTotal = BigDecimal.valueOf(1_041_700);
        BigDecimal expectedWeeklyPayment = BigDecimal.valueOf(52085);
        
        assertEquals(0, expectedTotal.compareTo(loan.getOutstanding()));
        assertEquals(0, expectedWeeklyPayment.compareTo(loan.getWeeklyPayment()));
    }

    @Test
    void given_50WeekLoan_when_createLoan_then_calculateCorrectOutstanding() {
        /**
         * annual rate: 0.1
         * loan amount: 1_000_000
         * duration in weeks: 50
         * duration in months: 13
         * interest rate: 0.1084
         * interest: 108_400
         * total loan: 1_108_400
         * weekly installment: 22_168
         */
        Loan loan = factory.createLoan("L009", LOAN_AMOUNT, BigDecimal.valueOf(0.1), 50, startDate);

        BigDecimal expectedTotal = BigDecimal.valueOf(1_108_400);
        BigDecimal expectedWeeklyPayment = BigDecimal.valueOf(22_168);
        
        assertEquals(0, expectedTotal.compareTo(loan.getOutstanding()));
        assertEquals(0, expectedWeeklyPayment.compareTo(loan.getWeeklyPayment()));
    }

    @Test
    void given_72WeekLoan_when_createLoan_then_calculateCorrectOutstanding() {
        /**
         * annual rate: 0.1
         * loan amount: 1_000_000
         * duration in weeks: 72
         * duration in months: 18
         * interest rate: 0.15
         * interest: 150_000
         * total loan: 1_150_000
         * weekly installment: 15_972.23
         */
        Loan loan = factory.createLoan("L010", LOAN_AMOUNT, BigDecimal.valueOf(0.1), 72, startDate);

        // adjusted total value, because monthly installment there's .23
        BigDecimal expectedTotal = new BigDecimal("1150000.56");
        BigDecimal expectedWeeklyPayment = new BigDecimal("15972.23");
        
        assertEquals(0, expectedTotal.compareTo(loan.getOutstanding()));
        assertEquals(0, expectedWeeklyPayment.compareTo(loan.getWeeklyPayment()));
    }

    @Test
    void given_50WeekLoanOf5mio_when_createLoan_then_calculateCorrectOutstanding() {
        /**
         * annual rate: 0.1
         * loan amount: 5_000_000
         * duration in weeks: 50
         * duration in months: 13
         * interest rate: 0.1084
         * interest: 542_000
         * total loan: 5_542_000
         * weekly installment: 110_840
         */
        Loan loan = factory.createLoan("L010", BigDecimal.valueOf(5_000_000), BigDecimal.valueOf(0.1), 50, startDate);

        // adjusted total value, because monthly installment there's .23
        BigDecimal expectedTotal = new BigDecimal(5_542_000);
        BigDecimal expectedWeeklyPayment = new BigDecimal(110_840);

        assertEquals(0, expectedTotal.compareTo(loan.getOutstanding()));
        assertEquals(0, expectedWeeklyPayment.compareTo(loan.getWeeklyPayment()));
    }
}
