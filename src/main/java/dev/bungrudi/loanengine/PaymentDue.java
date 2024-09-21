package dev.bungrudi.loanengine;

import java.time.LocalDate;
import lombok.ToString;

@ToString
public class PaymentDue {
    private int weekNumber;
    private double amountDue;
    private boolean isPaid;
    private LocalDate paymentDate;
    private LocalDate dueDate;

    public PaymentDue(int weekNumber, double amountDue, LocalDate dueDate) {
        this.weekNumber = weekNumber;
        this.amountDue = amountDue;
        this.isPaid = false;
        this.paymentDate = null;
        this.dueDate = dueDate;
    }

    public int getWeekNumber() { return weekNumber; }
    public double getAmountDue() { return amountDue; }
    public boolean isPaid() { return isPaid; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public LocalDate getDueDate() { return dueDate; }

    public void markAsPaid(LocalDate paymentDate) {
        this.isPaid = true;
        this.paymentDate = paymentDate;
    }
}
