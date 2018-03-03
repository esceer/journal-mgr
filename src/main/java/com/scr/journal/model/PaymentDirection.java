package com.scr.journal.model;

public enum PaymentDirection {
    INCOMING("incoming"),
    OUTGOING("outgoing");

    private final String associatedValue;

    PaymentDirection(String associatedValue) {
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
