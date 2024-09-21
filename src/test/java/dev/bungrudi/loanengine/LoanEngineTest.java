//package dev.bungrudi.loanengine;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import static org.junit.jupiter.api.Assertions.*;
//
//import java.time.LocalDate;
//import java.time.DayOfWeek;
//import java.time.temporal.TemporalAdjusters;
//
//public class LoanEngineTest {
//
//    private LoanEngine engine;
//    private LoanFactory loanFactory;
//
//    @BeforeEach
//    void setUp() {
//        loanFactory = new ConsumptiveWeeklyLoanFactory();
//        engine = new LoanEngine(LocalDate.of(2023, 3, 1), loanFactory); // Start date (Friday, March 1, 2023)
//    }
//
//    @Test
//    void given_newLoan_when_creatingLoan_then_loanDetailsAreCorrect() {
//        Loan loan = engine.createLoan("L001", 5_000_000, 0.1, 50);
//
//        assertEquals(5_500_000, loan.totalAmount, 0.01, "Total amount to repay should be 5,500,000");
//        assertEquals(110_000, loan.weeklyPayment, 0.01, "Weekly payment should be 110,000");
//        assertEquals(50, loan.numberOfWeeks, "Number of weeks should be 50");
//        assertEquals(LocalDate.of(2023, 3, 8), loan.firstPaymentDate, "First payment date should be 7 days after the start date (March 8, 2023)");
//    }
//
//    @Test
//    void given_loanWithOnePayment_when_checkingOutstanding_then_outstandingIsCorrect() {
//        engine.createLoan("L001", 5_000_000, 0.1, 50);
//        engine.setCurrentDate(LocalDate.of(2023, 3, 6)); // First payment date
//        engine.makePayment("L001", 110_000);
//
//        assertEquals(5_390_000, engine.getOutstanding("L001"), 0.01, "Outstanding after first payment should be 5,390,000");
//    }
//
//    @Test
//    void given_loanWithThreePayments_when_checkingOutstanding_then_outstandingIsCorrect() {
//        engine.createLoan("L001", 5_000_000, 0.1, 50);
//        engine.setCurrentDate(LocalDate.of(2023, 3, 8)); // First payment date
//        engine.makePayment("L001", 110_000);
//        engine.setCurrentDate(LocalDate.of(2023, 3, 15)); // Second week
//        engine.makePayment("L001", 110_000);
//        engine.setCurrentDate(LocalDate.of(2023, 3, 22)); // Third week
//        engine.makePayment("L001", 110_000);
//
//        assertEquals(5_170_000, engine.getOutstanding("L001"), 0.01, "Outstanding after three payments should be 5,170,000");
//    }
//
//    @Test
//    void given_loanWithUpToDatePayments_when_checkingDelinquency_then_loanIsNotDelinquent() {
//        engine.createLoan("L001", 5_000_000, 0.1, 50);
//        engine.setCurrentDate(LocalDate.of(2023, 3, 8)); // First payment date
//        engine.makePayment("L001", 110_000);
//        engine.setCurrentDate(LocalDate.of(2023, 3, 15)); // Second week
//        engine.makePayment("L001", 110_000);
//
//        assertFalse(engine.isDelinquent("L001"), "Loan should not be delinquent");
//    }
//
//    @Test
//    void given_loanWithMissed2Payments_when_checkingDelinquency_then_loanIsDelinquent() {
//        engine.createLoan("L001", 5_000_000, 0.1, 50);
//        engine.setCurrentDate(LocalDate.of(2023, 3, 22)); // second week
//
//        assertTrue(engine.isDelinquent("L001"), "Loan should be delinquent after missing 2 consecutive payment");
//    }
//
//    @Test
//    void given_incorrectPaymentAmount_when_makingPayment_then_exceptionIsThrown() {
//        engine.createLoan("L001", 5_000_000, 0.1, 50);
//        engine.setCurrentDate(LocalDate.of(2023, 3, 8)); // First payment date
//
//        assertThrows(IllegalArgumentException.class, () -> {
//            engine.makePayment("L001", 55_000); // Incorrect payment amount
//        }, "Payment with incorrect amount should throw an IllegalArgumentException");
//    }
//
//    @Test
//    void given_overpayment_when_makingPayment_then_exceptionIsThrown() {
//        engine.createLoan("L001", 5_000_000, 0.1, 50);
//        engine.setCurrentDate(LocalDate.of(2023, 3, 8)); // First payment date
//
//        assertThrows(IllegalArgumentException.class, () -> {
//            engine.makePayment("L001", 220_000); // Overpayment
//        }, "Overpayment should throw an IllegalArgumentException");
//    }
//
//    @Test
//    void given_paidTwice_when_makingPayment_then_exceptionIsThrown() {
//        engine.createLoan("L001", 5_000_000, 0.1, 50);
//        engine.setCurrentDate(LocalDate.of(2023, 3, 8)); // First payment date
//
//        engine.makePayment("L001", 110_000);
//        assertThrows(IllegalStateException.class, () -> {
//            engine.makePayment("L001", 110_000); // Attempt to pay for the next week
//        }, "Forward payment should throw an IllegalStateException");
//    }
//
//    @Test
//    void given_paymentForPreviousWeek_when_makingPayment_then_paymentIsAppliedToEarliestUnpaidWeek() {
//        engine.createLoan("L001", 5_000_000, 0.1, 50);
//        engine.setCurrentDate(LocalDate.of(2023, 3, 22)); // Third week
//
//        engine.makePayment("L001", 110_000); // Payment for the first week
//        assertEquals(5_390_000, engine.getOutstanding("L001"), 0.01, "Outstanding after first payment should be 5,390,000");
//        assertFalse(engine.isDelinquent("L001"), "Loan should not be delinquent with only one missed payment");
//
//        engine.makePayment("L001", 110_000); // Payment for the second week
//        assertEquals(5_280_000, engine.getOutstanding("L001"), 0.01, "Outstanding after second payment should be 5,280,000");
//        assertFalse(engine.isDelinquent("L001"), "Loan should not be delinquent after paying for the second week");
//
//        engine.makePayment("L001", 110_000); // Payment for the third week
//        assertEquals(5_170_000, engine.getOutstanding("L001"), 0.01, "Outstanding after third payment should be 5,170,000");
//
//        engine.setCurrentDate(LocalDate.of(2023, 4, 12)); // Fifth week (at the due date)
//        assertTrue(engine.isDelinquent("L001"), "Loan should be delinquent with two missed payments");
//
//        engine.makePayment("L001", 110_000); // Payment for the fourth week
//        assertFalse(engine.isDelinquent("L001"), "Loan should not be delinquent after paying for the fourth week");
//
//        engine.makePayment("L001", 110_000); // Payment for the fifth week
//        assertFalse(engine.isDelinquent("L001"), "Loan should not be delinquent after paying for the fifth week");
//    }
//}
