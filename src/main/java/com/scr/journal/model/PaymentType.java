package com.scr.journal.model;

public enum PaymentType {
    CASH("cash"),
    BANK_TRANSFER("bank transfer");

    private final String associatedValue;

    PaymentType(String associatedValue) {
        this.associatedValue = associatedValue;
    }

    public String getAssociatedValue() {
        return associatedValue;
    }

    @Override
    public String toString() {
        return getAssociatedValue();
    }

    public static PaymentDirection tryParse(String value) {
        for (PaymentDirection pd : PaymentDirection.values()) {
            if (pd.getAssociatedValue().equalsIgnoreCase(value)) {
                return pd;
            }
        }
        return null;
    }
}
