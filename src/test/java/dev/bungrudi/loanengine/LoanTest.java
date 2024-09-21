package dev.bungrudi.loanengine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

public class LoanTest {

    private Loan loan;
    private LocalDate startDate;
    private LoanFactory loanFactory;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2023, 3, 1);
        loanFactory = new ConsumptiveWeeklyLoanFactory();
        loan = loanFactory.createLoan("L001", 5_000_000, 0.1, 50, startDate);
    }

    @Test
    void given_newLoan_when_initialized_then_detailsAreCorrect() {
        assertEquals("L001", loan.getLoanId());
        assertEquals(5_500_000, loan.getTotalAmount(), 0.01);
        assertEquals(110_000, loan.getWeeklyPayment(), 0.01);
        assertEquals(50, loan.getNumberOfWeeks());
        assertEquals(startDate, loan.getStartDate());
        assertEquals(startDate.plusDays(7), loan.getFirstPaymentDate());
    }

    @Test
    void given_loan_when_checkingCurrentWeek_then_weeksAreCalculatedCorrectly() {
        LocalDate date0 = startDate;
        LocalDate date1 = startDate.plusDays(6);
        LocalDate date2 = startDate.plusDays(7);
        LocalDate date3 = startDate.plusDays(13);
        LocalDate date4 = startDate.plusDays(14);
        LocalDate date5 = startDate.plusDays(20);

        System.out.println("Start Date: " + startDate);
        System.out.println("Date 0: " + date0 + ", Week: " + loan.getCurrentWeek(date0));
        System.out.println("Date 1: " + date1 + ", Week: " + loan.getCurrentWeek(date1));
        System.out.println("Date 2: " + date2 + ", Week: " + loan.getCurrentWeek(date2));
        System.out.println("Date 3: " + date3 + ", Week: " + loan.getCurrentWeek(date3));
        System.out.println("Date 4: " + date4 + ", Week: " + loan.getCurrentWeek(date4));
        System.out.println("Date 5: " + date5 + ", Week: " + loan.getCurrentWeek(date5));

        assertEquals(-1, loan.getCurrentWeek(date0));
        assertEquals(0, loan.getCurrentWeek(date1));
        assertEquals(0, loan.getCurrentWeek(date2));
        assertEquals(1, loan.getCurrentWeek(date3));
        assertEquals(1, loan.getCurrentWeek(date4));
        assertEquals(2, loan.getCurrentWeek(date5));
    }

    @Test
    void given_loan_when_makePayment_then_outstandingIsUpdated() {
        loan.makePayment(110_000, startDate.plusDays(7));
        assertEquals(5_390_000, loan.getOutstanding(), 0.01);
    }

    @Test
    void given_loan_when_makePaymentEarly_then_exceptionIsThrown() {
        assertThrows(IllegalStateException.class, () -> {
            loan.makePayment(110_000, startDate);
        });

    }

    @Test
    void given_loan_when_makePaymentWithIncorrectAmount_then_exceptionIsThrown() {
        assertThrows(IllegalArgumentException.class, () -> {
            loan.makePayment(100_000, startDate.plusDays(7));
        });
    }

    @Test
    void given_loan_when_checkingStanding_then_statusIsCorrect() {
        LocalDate date1 = startDate.plusDays(7);
        LocalDate date2 = startDate.plusDays(14);
        LocalDate date3 = startDate.plusDays(21);

        System.out.println("Date 1: " + date1);
        System.out.println("Expected PaymentDue 1: " + loan.getSchedule().get(0));
        loan.updateStatus(date1);
        assertEquals(LoanStanding.GOOD_STANDING, loan.getStanding());

        System.out.println("Date 2: " + date2);
        System.out.println("Expected PaymentDue 2: " + loan.getSchedule().get(1));
        loan.updateStatus(date2);
        assertEquals(LoanStanding.LATE, loan.getStanding());

        System.out.println("Date 3: " + date3);
        System.out.println("Expected PaymentDue 3: " + loan.getSchedule().get(2));
        loan.updateStatus(date3);
        assertEquals(LoanStanding.DELINQUENT, loan.getStanding());
    }

    @Test
    void given_loan_when_payWithinWeek_then_success() {
        loan.makePayment(110_000, startDate.plusDays(2));
        assertTrue(loan.getSchedule().get(0).isPaid());
        assertFalse(loan.getSchedule().get(1).isPaid());
    }

    @Test
    void given_loan_when_alreadyPaidThisWeek_then_cannotPayAgain() {
        loan.makePayment(110_000, startDate.plusDays(2));
        assertTrue(loan.getSchedule().get(0).isPaid());
        assertFalse(loan.getSchedule().get(1).isPaid());
        // still in the first week, cannot pay again
        assertThrows(IllegalStateException.class, () -> {
            loan.makePayment(110_000, startDate.plusDays(3));
        }, "Payment already made for this week.");
    }

    @Test
    void given_latePayments_whenPay_then_payLateFirst() {
        loan.makePayment(110_000, startDate.plusDays(14));
        assertTrue(loan.getSchedule().get(0).isPaid());
        assertFalse(loan.getSchedule().get(1).isPaid());
    }

    @Test
    void given_delinquentLoan_when_makePayment_then_becomeLate() {
        // Make the loan delinquent
        loan.updateStatus(startDate.plusDays(21));
        assertEquals(LoanStanding.DELINQUENT, loan.getStanding());

        // Make a payment
        loan.makePayment(110_000, startDate.plusDays(21));

        // Update status manually
        loan.updateStatus(startDate.plusDays(21));

        // Check if it's now LATE
        assertEquals(LoanStanding.LATE, loan.getStanding());

        // Verify the payment was applied to the earliest unpaid week
        assertTrue(loan.getSchedule().get(0).isPaid());
        assertFalse(loan.getSchedule().get(1).isPaid());
        assertFalse(loan.getSchedule().get(2).isPaid());
    }

    @Test
    void given_loan_when_makePayments_then_outstandingIsCorrect() {
        assertEquals(5_500_000, loan.getOutstanding(), 0.01, "Initial outstanding should be 5,500,000");

        loan.makePayment(110_000, startDate.plusDays(7));
        assertEquals(5_390_000, loan.getOutstanding(), 0.01, "Outstanding after first payment should be 5,390,000");

        loan.makePayment(110_000, startDate.plusDays(14));
        assertEquals(5_280_000, loan.getOutstanding(), 0.01, "Outstanding after second payment should be 5,280,000");

        loan.makePayment(110_000, startDate.plusDays(21));
        assertEquals(5_170_000, loan.getOutstanding(), 0.01, "Outstanding after third payment should be 5,170,000");

        // Make all remaining payments
        for (int i = 3; i < 50; i++) {
            loan.makePayment(110_000, startDate.plusDays(7L * (i + 1)));
        }
        assertEquals(0, loan.getOutstanding(), 0.01, "Outstanding after all payments should be 0");
    }
}
