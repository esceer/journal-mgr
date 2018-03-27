package com.scr.journal.model;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;
import java.util.Objects;

public class Journal implements Comparable<Journal> {

    private SimpleObjectProperty<LocalDate> date;
    private SimpleObjectProperty<PaymentType> paymentType;
    private SimpleObjectProperty<PaymentDirection> paymentDirection;
    private SimpleStringProperty invoiceNumber;
    private SimpleLongProperty amount;
    private SimpleStringProperty comment;
    private SimpleStringProperty address;
    private SimpleStringProperty expenseType;

    public Journal() {
        this(null, null, null, "", 0, "", "", "");
    }

    public Journal(
            LocalDate date,
            PaymentType paymentType,
            PaymentDirection paymentDirection,
            String invoiceNumber,
            long amount,
            String comment,
            String address,
            String expenseType) {
        this.date = new SimpleObjectProperty(date);
        this.paymentType = new SimpleObjectProperty<>(paymentType);
        this.paymentDirection = new SimpleObjectProperty<>(paymentDirection);
        this.invoiceNumber = new SimpleStringProperty(invoiceNumber);
        this.amount = new SimpleLongProperty(amount);
        this.comment = new SimpleStringProperty(comment);
        this.address = new SimpleStringProperty(address);
        this.expenseType = new SimpleStringProperty(expenseType);
    }

    public LocalDate getDate() {
        return date.get();
    }

    public void setDate(LocalDate date) {
        this.date.set(date);
    }

    public PaymentType getPaymentType() {
        return paymentType.get();
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType.set(paymentType);
    }

    public PaymentDirection getPaymentDirection() {
        return paymentDirection.get();
    }

    public void setPaymentDirection(PaymentDirection paymentDirection) {
        this.paymentDirection.set(paymentDirection);
    }

    public String getInvoiceNumber() {
        return invoiceNumber.get();
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber.set(invoiceNumber);
    }

    public long getAmount() {
        return amount.get();
    }

    public void setAmount(long amount) {
        this.amount.set(amount);
    }

    public long getSignedAmount() {
        long amount = getAmount();
        if (PaymentDirection.OUTGOING == getPaymentDirection()) {
            amount = Math.negateExact(amount);

        }
        return amount;
    }

    public String getComment() {
        return comment.get();
    }

    public void setComment(String comment) {
        this.comment.set(comment);
    }

    public String getAddress() {
        return address.get();
    }

    public void setAddress(String address) {
        this.address.set(address);
    }

    public String getExpenseType() {
        return expenseType.get();
    }

    public void setExpenseType(String expenseType) {
        this.expenseType.set(expenseType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Journal journal = (Journal) o;
        return Objects.equals(date, journal.date) &&
                Objects.equals(paymentType, journal.paymentType) &&
                Objects.equals(paymentDirection, journal.paymentDirection) &&
                Objects.equals(invoiceNumber, journal.invoiceNumber) &&
                Objects.equals(comment, journal.comment) &&
                Objects.equals(address, journal.address) &&
                Objects.equals(amount, journal.amount) &&
                Objects.equals(expenseType, journal.expenseType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, paymentType, paymentDirection, invoiceNumber, amount, comment, address, expenseType);
    }

    @Override
    public String toString() {
        return "Journal{" +
                "date=" + date +
                ", paymentType=" + paymentType +
                ", paymentDirection=" + paymentDirection +
                ", invoiceNumber=" + invoiceNumber +
                ", amount=" + amount +
                ", comment=" + comment +
                ", address=" + address +
                ", expenseType=" + expenseType +
                '}';
    }

    @Override
    public int compareTo(Journal o) {
        return o.getDate().compareTo(getDate());
    }
}
