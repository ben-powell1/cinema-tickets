package uk.gov.dwp.uc.pairtest.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TicketTypeRequestTest {

    @Test
    void GIVEN_null_type_WHEN_constructing_THEN_exception() {
        assertThrows(NullPointerException.class, () -> new TicketTypeRequest(null, 1));
    }

    @Test
    void GIVEN_number_of_tickets_less_than_zero_WHEN_constructing_THEN_exception() {
        assertThrows(IllegalArgumentException.class, () -> new TicketTypeRequest(TicketTypeRequest.Type.ADULT, -1));
    }

}