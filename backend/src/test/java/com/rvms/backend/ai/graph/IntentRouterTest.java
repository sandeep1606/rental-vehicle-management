package com.rvms.backend.ai.graph;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IntentRouterTest {

    @Test
    void routesVehicleSearchAndBookingIntentsToSearchBranch() {
        assertThat(IntentRouter.route(IntentType.VEHICLE_SEARCH)).isEqualTo("search");
        assertThat(IntentRouter.route(IntentType.BOOKING_HELP)).isEqualTo("search");
        assertThat(IntentRouter.route(IntentType.BRANCH_AVAILABILITY)).isEqualTo("search");
    }

    @Test
    void routesCustomerLookupToCustomerBranch() {
        assertThat(IntentRouter.route(IntentType.CUSTOMER_LOOKUP)).isEqualTo("customer");
    }

    @Test
    void routesPolicyQuestionToPolicyBranch() {
        assertThat(IntentRouter.route(IntentType.POLICY_QUESTION)).isEqualTo("policy");
    }

    @Test
    void routesGeneralAndNullToGeneralBranch() {
        assertThat(IntentRouter.route(IntentType.GENERAL)).isEqualTo("general");
        assertThat(IntentRouter.route(null)).isEqualTo("general");
    }
}
