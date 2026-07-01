package com.rvms.backend.ai.graph.nodes;

import com.rvms.backend.ai.graph.IntentType;
import com.rvms.backend.ai.graph.RentalAssistantState;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ClassifyIntentNodeTest {

    private final ClassifyIntentNode node = new ClassifyIntentNode();

    private RentalAssistantState stateFor(String message) {
        return new RentalAssistantState(Map.of(RentalAssistantState.USER_MESSAGE, message));
    }

    @Test
    void classifiesVehicleSearchMessage() throws Exception {
        var updates = node.apply(stateFor("Find me an SUV in location 2 from July 5 to July 10 under $80/day"));
        assertThat(updates.get(RentalAssistantState.INTENT)).isEqualTo(IntentType.VEHICLE_SEARCH);
    }

    @Test
    void classifiesCustomerLookupMessage() throws Exception {
        var updates = node.apply(stateFor("Look up customer with driver license DL-100002"));
        assertThat(updates.get(RentalAssistantState.INTENT)).isEqualTo(IntentType.CUSTOMER_LOOKUP);
    }

    @Test
    void classifiesPolicyQuestionMessage() throws Exception {
        var updates = node.apply(stateFor("What is the late return fee policy?"));
        assertThat(updates.get(RentalAssistantState.INTENT)).isEqualTo(IntentType.POLICY_QUESTION);
    }

    @Test
    void classifiesBookingHelpMessage() throws Exception {
        var updates = node.apply(stateFor("How do I book a reservation for next week?"));
        assertThat(updates.get(RentalAssistantState.INTENT)).isEqualTo(IntentType.BOOKING_HELP);
    }

    @Test
    void classifiesGeneralMessageWhenNoKeywordsMatch() throws Exception {
        var updates = node.apply(stateFor("Hello there!"));
        assertThat(updates.get(RentalAssistantState.INTENT)).isEqualTo(IntentType.GENERAL);
    }
}
