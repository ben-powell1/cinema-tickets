package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TicketServiceImpl implements TicketService {

    private static final int MAX_TICKETS = 25;

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
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequestsIn) throws InvalidPurchaseException {
        if (Objects.requireNonNull(accountId, "accountId must not be null") < 1) {
            throw new InvalidPurchaseException("accountId must be > 0");
        }
        Objects.requireNonNull(ticketTypeRequestsIn, "ticket requests must not be null");
        List<TicketTypeRequest> ticketTypeRequests = new ArrayList<>(Arrays.asList(ticketTypeRequestsIn));
        if (ticketTypeRequests.isEmpty()) {
            throw new InvalidPurchaseException("ticket requests must be specified");
        }
        if (ticketTypeRequests.stream().anyMatch(Objects::isNull)) {
            throw new InvalidPurchaseException("ticket requests must all be non-null");
        }
        if (ticketTypeRequests.stream().allMatch(this::ticketsMustBeAccompanied)) {
            throw new InvalidPurchaseException("child and infant tickets cannot be purchased without adult tickets");
        }

        int totalSeatsToAllocate = ticketTypeRequests.stream()
                .filter(this::ticketsRequireSeatsToBeAllocated)
                .map(TicketTypeRequest::getNoOfTickets)
                .reduce(0, Integer::sum);

        if (totalSeatsToAllocate > MAX_TICKETS) {
            throw new InvalidPurchaseException(String.format("a maximum of %d tickets can be purchased at a time", MAX_TICKETS));
        }

        int totalAmountToPay = ticketTypeRequests.stream()
                .map(this::getCostForTickets)
                .reduce(0, Integer::sum);

        ticketPaymentService.makePayment(accountId, totalAmountToPay);

        seatReservationService.reserveSeat(accountId, totalSeatsToAllocate);
    }

    private boolean ticketsMustBeAccompanied(TicketTypeRequest ticketRequest) {
        return switch(ticketRequest.getTicketType()) {
            case INFANT, CHILD -> true;
            case ADULT -> false;
        };
    }

    private boolean ticketsRequireSeatsToBeAllocated(TicketTypeRequest ticketRequest) {
        return switch(ticketRequest.getTicketType()) {
            case INFANT -> false;
            case CHILD, ADULT -> true;
        };
    }

    private int getCostForTickets(TicketTypeRequest ticketRequest) {
        int pricePerTicket = switch(ticketRequest.getTicketType()) {
            case INFANT -> 0;
            case CHILD -> 15;
            case ADULT -> 25;
        };
        return ticketRequest.getNoOfTickets() * pricePerTicket;
    }
}
