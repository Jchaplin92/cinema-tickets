package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.CostProvider;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static java.util.Arrays.*;


public class TicketServiceImpl implements TicketService {

    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    public TicketServiceImpl(TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    /**
     * Should only have private methods other than the one below.
     */
    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        if (invalid(accountId) || totalTicketsGreaterThan20(ticketTypeRequests) || noAdultPresent(ticketTypeRequests) || insufficientAdultsForNumberOfInfants(ticketTypeRequests)) {
            throw new InvalidPurchaseException();
        }

        ticketPaymentService.makePayment(accountId, totalToPay(ticketTypeRequests));
        seatReservationService.reserveSeat(accountId, totalSeats(ticketTypeRequests));
    }

    private boolean invalid(long accountId) {
        boolean result = accountId<=0;
        if (result)
            System.err.println("AccountId cannot be less than or equal to 0");
        return result;
    }

    private boolean totalTicketsGreaterThan20(TicketTypeRequest... ticketTypeRequests) {
        int totalTickets = stream(ticketTypeRequests) // alternatively consider: .mapToInt(TicketTypeRequest::getNoOfTickets).reduce(0, (a,b) -> a+b)
                .reduce(0, (subtotal, request) -> subtotal + request.getNoOfTickets(), Integer::sum);
        boolean result = totalTickets > 20;
        if (result)
                System.err.println("You cannot purchase more than 20 tickets at once");
        return result;
    }

    private boolean noAdultPresent(TicketTypeRequest... ticketTypeRequests) {
        boolean result = stream(ticketTypeRequests).noneMatch(request -> request.getTicketType() == TicketTypeRequest.Type.ADULT);
        if (result)
            System.err.println("You cannot purchase tickets without an adult present");
        return result;
    }

    private boolean insufficientAdultsForNumberOfInfants(TicketTypeRequest... ticketTypeRequests) {
        int numberOfAdults = stream(ticketTypeRequests)
                .filter(request -> request.getTicketType() == TicketTypeRequest.Type.ADULT)
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .reduce(0, Integer::sum);
        int numberOfInfants = stream(ticketTypeRequests)
                .filter(request -> request.getTicketType() == TicketTypeRequest.Type.INFANT)
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .reduce(0, Integer::sum);
        boolean result = numberOfAdults < numberOfInfants;
        if (result)
            System.err.println("Every infant needs an adult seat holder to look after them");
        return result;
    }

    private int totalToPay(CostProvider... ticketTypeRequests) {
        return stream(ticketTypeRequests).mapToInt(CostProvider::totalToPay).reduce(0, Integer::sum);
    }

    private int totalSeats(TicketTypeRequest... ticketTypeRequests) {
        return stream(ticketTypeRequests)
                .filter(request -> request.getTicketType() != TicketTypeRequest.Type.INFANT)
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .reduce(0, Integer::sum);
    }
}
