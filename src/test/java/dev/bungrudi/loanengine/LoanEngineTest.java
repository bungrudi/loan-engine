package dev.bungrudi.loanengine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LoanEngineTest {

    private LoanEngine engine;
    private LoanFactory loanFactory;
    private LocalDate startDate;
    private Random random;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2023, 3, 1);
        loanFactory = new ConsumptiveWeeklyLoanFactory();
        engine = new LoanEngine(startDate, loanFactory);
        random = new Random();
    }

    @Test
    void given_multipleLoans_when_creatingAndGettingOutstanding_then_allLoansAreCreatedAndHaveOutstanding() {
        List<String> loanIds = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            BigDecimal loanAmount = BigDecimal.valueOf(random.nextInt(9000000) + 1000000); // 1,000,000 to 10,000,000
            BigDecimal interestRate = BigDecimal.valueOf(random.nextDouble() * 0.2); // 0% to 20%
            int numberOfWeeks = random.nextInt(96) + 4; // 4 to 100 weeks
            
            String loanId = "L" + String.format("%03d", i + 1);
            Loan loan = engine.createLoan(loanId, loanAmount, interestRate, numberOfWeeks);
            loanIds.add(loanId);
            
            assertNotNull(loan);
            assertEquals(loanId, loan.getLoanId());
            assertTrue(loan.getTotalAmount().compareTo(loanAmount) > 0);
            assertEquals(numberOfWeeks, loan.getNumberOfWeeks());
        }

        assertEquals(100, loanIds.size());

        // Test getting outstanding amounts
        for (String loanId : loanIds) {
            BigDecimal outstanding = engine.getOutstanding(loanId);
            assertNotNull(outstanding);
            assertTrue(outstanding.compareTo(BigDecimal.ZERO) > 0);
        }
    }

    @Test
    void given_multipleLoans_when_endOfDayAndPayment_then_loanStatusesAreUpdatedCorrectly() {
        // Create a few loans
        engine.createLoan("L001", BigDecimal.valueOf(1000000), BigDecimal.valueOf(0.1), 20);
        engine.createLoan("L002", BigDecimal.valueOf(2000000), BigDecimal.valueOf(0.15), 30);
        engine.createLoan("L003", BigDecimal.valueOf(3000000), BigDecimal.valueOf(0.05), 40);

        // Simulate more than 2 weeks passing
        for (int i = 0; i < 15; i++) {
            engine.endOfDay();
        }

        LocalDate expectedDate = startDate.plusDays(15);
        assertEquals(expectedDate, engine.getCurrentDate());

        // Check loan statuses
        assertTrue(engine.isDelinquent("L001"));
        assertTrue(engine.isDelinquent("L002"));
        assertTrue(engine.isDelinquent("L003"));

        // Make a payment on one loan
        engine.makePayment("L002", engine.getLoan("L002").getWeeklyPayment());

        // End of day again
        engine.endOfDay();

        // Check statuses again
        assertTrue(engine.isDelinquent("L001"));
        assertFalse(engine.isDelinquent("L002")); // not delinquent after 1 payment made
        assertTrue(engine.isDelinquent("L003"));

        // Make another payment on L002
        engine.makePayment("L002", engine.getLoan("L002").getWeeklyPayment());

        // End of day again
        engine.endOfDay();

        // Check statuses one more time
        assertTrue(engine.isDelinquent("L001"));
        assertFalse(engine.isDelinquent("L002")); // Now it should not be delinquent
        assertTrue(engine.isDelinquent("L003"));
    }
}
