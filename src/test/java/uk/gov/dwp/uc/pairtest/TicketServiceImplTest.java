package uk.gov.dwp.uc.pairtest;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.concurrent.ThreadLocalRandom;

import static org.mockito.Mockito.*;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.*;

@RunWith(MockitoJUnitRunner.class)
public class TicketServiceImplTest {

    private long accountId;

    @Mock
    private TicketTypeRequest firstRequest, secondRequest, thirdRequest;

    @Mock
    private TicketPaymentService paymentService;

    @Mock
    private SeatReservationService reservationService;

    @Before
    public void setUp() {
        accountId = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
    }

    @After
    public void tearDown() {
        reset(firstRequest, secondRequest, thirdRequest, paymentService, reservationService);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void purchaseTicketsThrowsAnInvalidPurchaseExceptionWhenAccountIdLessThanZero() {
        when(firstRequest.getNoOfTickets()).thenReturn(5);
        when(secondRequest.getNoOfTickets()).thenReturn(5);

        TicketServiceImpl underTest = new TicketServiceImpl(paymentService, reservationService);
        underTest.purchaseTickets(-100L, firstRequest, secondRequest);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void purchaseTicketsThrowsAnInvalidPurchaseExceptionWhenAccountIdZero() {
        when(firstRequest.getNoOfTickets()).thenReturn(5);
        when(secondRequest.getNoOfTickets()).thenReturn(5);

        TicketServiceImpl underTest = new TicketServiceImpl(paymentService, reservationService);
        underTest.purchaseTickets(0L, firstRequest, secondRequest);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void purchaseTicketsThrowsAnInvalidPurchaseExceptionWhenMoreThan20Tickets() {
        when(firstRequest.getNoOfTickets()).thenReturn(10);
        when(secondRequest.getNoOfTickets()).thenReturn(11);

        TicketServiceImpl underTest = new TicketServiceImpl(paymentService, reservationService);
        underTest.purchaseTickets(100L, firstRequest, secondRequest);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void purchaseTicketsThrowsAnInvalidPurchaseExceptionWhenNoAdult() {
        when(firstRequest.getTicketType()).thenReturn(CHILD);
        when(secondRequest.getTicketType()).thenReturn(INFANT);

        TicketServiceImpl underTest = new TicketServiceImpl(paymentService, reservationService);
        underTest.purchaseTickets(accountId, firstRequest, secondRequest);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void purchaseTicketsThrowsAnInvalidPurchaseWhenInfantsOutnumberAdults() {
        when(firstRequest.getTicketType()).thenReturn(ADULT);
        when(firstRequest.getNoOfTickets()).thenReturn(5);
        when(secondRequest.getTicketType()).thenReturn(INFANT);
        when(secondRequest.getNoOfTickets()).thenReturn(6);

        TicketServiceImpl underTest = new TicketServiceImpl(paymentService, reservationService);
        underTest.purchaseTickets(accountId, firstRequest, secondRequest);
    }

    @Test
    public void purchaseTicketsCallsTicketPaymentServiceImplWithSumOfPayments() {
        when(firstRequest.getTicketType()).thenReturn(ADULT);
        when(firstRequest.totalToPay()).thenReturn(20);
        when(secondRequest.getTicketType()).thenReturn(CHILD);
        when(secondRequest.totalToPay()).thenReturn(10);

        TicketServiceImpl underTest = new TicketServiceImpl(paymentService, reservationService);
        underTest.purchaseTickets(accountId, firstRequest, secondRequest);

        verify(paymentService, times(1)).makePayment(accountId, 30);
    }

    @Test
    public void purchaseTicketsCallsSeatReservationServiceImplWithSumOfSeatsIgnoringInfants() {
        when(firstRequest.getTicketType()).thenReturn(ADULT);
        when(firstRequest.getNoOfTickets()).thenReturn(6);
        when(secondRequest.getTicketType()).thenReturn(CHILD);
        when(secondRequest.getNoOfTickets()).thenReturn(8);
        when(thirdRequest.getTicketType()).thenReturn(INFANT);
        when(thirdRequest.getNoOfTickets()).thenReturn(2);

        TicketServiceImpl underTest = new TicketServiceImpl(paymentService, reservationService);
        underTest.purchaseTickets(accountId, firstRequest, secondRequest);

        verify(reservationService, times(1)).reserveSeat(accountId, 14);
    }

}