package uk.gov.dwp.uc.pairtest.domain;

import org.junit.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class TicketTypeRequestTest {

    @Test
    public void totalToPayCorrectForAdult() {
        int numberOfTickets = ThreadLocalRandom.current().nextInt(1, 21);
        TicketTypeRequest underTest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, numberOfTickets);
        assertThat(underTest.totalToPay(), equalTo(numberOfTickets * 20));
    }
    @Test
    public void totalToPayCorrectForChild() {
        int numberOfTickets = ThreadLocalRandom.current().nextInt(1, 21);
        TicketTypeRequest underTest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, numberOfTickets);
        assertThat(underTest.totalToPay(), equalTo(numberOfTickets * 10));
    }
    @Test
    public void totalToPayCorrectForInfant() {
        int numberOfTickets = ThreadLocalRandom.current().nextInt(1, 21);
        TicketTypeRequest underTest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, numberOfTickets);
        assertThat(underTest.totalToPay(), equalTo(0));
    }
}