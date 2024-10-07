package uk.gov.dwp.uc.pairtest.domain;

import java.util.Objects;

/**
 * Immutable Object
 */

public final class TicketTypeRequest {

    private final int noOfTickets;
    private final Type type;

    public TicketTypeRequest(Type type, int noOfTickets) {
        this.type = Objects.requireNonNull(type, "Type of ticket must not be null");
        if (noOfTickets < 0) {
            throw new IllegalArgumentException("Number of tickets must not be a negative number");
        }
        this.noOfTickets = noOfTickets;
    }

    public int getNoOfTickets() {
        return noOfTickets;
    }

    public Type getTicketType() {
        return type;
    }

    public enum Type {
        ADULT, CHILD , INFANT
    }

}
