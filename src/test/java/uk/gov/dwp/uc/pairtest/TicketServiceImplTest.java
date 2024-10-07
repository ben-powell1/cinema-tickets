package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    private TicketPaymentService ticketPaymentServiceMock;
    @Mock
    private SeatReservationService seatReservationServiceMock;

    @InjectMocks
    private TicketServiceImpl ticketService;

    @Test
    void GIVEN_no_ticket_request_WHEN_purchasing_tickets_THEN_exception() {
        assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(1L));
    }

    @Test
    void GIVEN_null_accountId_WHEN_purchasing_tickets_THEN_exception() {
        assertThrows(NullPointerException.class, () -> ticketService.purchaseTickets(null, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)));
    }

    @ParameterizedTest(name = "accountId: {0}")
    @ValueSource(longs = {0, -1})
    void GIVEN_accountId_less_than_1_WHEN_purchasing_tickets_THEN_exception(long accountId) {
        assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(accountId, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)));
    }

    @Test
    void GIVEN_null_ticket_requests_array_WHEN_purchasing_tickets_THEN_exception() {
        assertThrows(NullPointerException.class, () -> ticketService.purchaseTickets(1L, (TicketTypeRequest[]) null));
    }

    @Test
    void GIVEN_null_ticket_request_WHEN_purchasing_tickets_THEN_exception() {
        assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(1L, new TicketTypeRequest[]{null}));
    }

    @Test
    void GIVEN_number_of_tickets_greater_than_25_in_one_request_WHEN_purchasing_tickets_THEN_exception() {
        var ticketRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 26);

        assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(1L, ticketRequest));
    }

    @Test
    void GIVEN_number_of_tickets_greater_than_25_in_multiple_requests_WHEN_purchasing_tickets_THEN_exception() {
        var adultTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 25);
        var childTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);

        assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(1L, adultTicketRequest, childTicketRequest));
    }

    @Test
    void GIVEN_only_infant_and_child_tickets_WHEN_purchasing_tickets_THEN_exception() {
        var infantTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);
        var childTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);

        assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(1L, infantTicketRequest, childTicketRequest));
    }

    @Test
    void GIVEN_all_ticket_types_included_WHEN_purchasing_tickets_THEN_only_child_and_adult_seats_allocated() {
        var infantTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);
        var childTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);
        var adultTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 4);

        ticketService.purchaseTickets(1L, infantTicketRequest, childTicketRequest, adultTicketRequest);

        verify(seatReservationServiceMock).reserveSeat(1L, 6);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', useHeadersInDisplayName = true, textBlock = """
            Adult_tickets | Child_tickets | Infant_tickets | Expected_total_amount_to_pay
            1             | 0             | 0              | 25
            1             | 1             | 0              | 40
            1             | 0             | 1              | 25
            1             | 1             | 1              | 40
            2             | 0             | 0              | 50
            2             | 2             | 0              | 80
            2             | 0             | 2              | 50
            2             | 2             | 2              | 80
            """)
    void GIVEN_different_combinations_of_tickets_WHEN_purchasing_tickets_THEN_correct_payment_requested(
            int noAdultTickets, int noChildTickets, int noInfantTickets, int expectedTotalAmountToPay) {
        var adultTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, noAdultTickets);
        var childTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, noChildTickets);
        var infantTicketRequest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, noInfantTickets);

        ticketService.purchaseTickets(1L, adultTicketRequest, childTicketRequest, infantTicketRequest);

        verify(ticketPaymentServiceMock).makePayment(1L, expectedTotalAmountToPay);
    }

}