package uk.gov.dwp.uc.pairtest.domain;

/**
 * Immutable Object
 */
public class TicketTypeRequest implements CostProvider {

    private final int noOfTickets;
    private final Type type;

    public TicketTypeRequest(Type type, int noOfTickets) {
        this.type = type;
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

    public int totalToPay() {
        switch (type) {
            case ADULT:
                return 20 * noOfTickets;
            case CHILD:
                return 10 * noOfTickets;
            case INFANT:
                return 0;
            default:
                throw new IllegalArgumentException("A new Type of ticket holder is unaccounted for");
        }
    }

}
