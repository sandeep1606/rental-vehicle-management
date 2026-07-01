package com.rvms.backend.ai.graph;

/**
 * Pure routing logic extracted from RentalAssistantGraphBuilder so the
 * classifyIntent -> next-node decision can be unit tested without booting the graph.
 */
public final class IntentRouter {

    private IntentRouter() {}

    public static String route(IntentType intent) {
        IntentType effective = intent == null ? IntentType.GENERAL : intent;
        return switch (effective) {
            case VEHICLE_SEARCH, BOOKING_HELP, BRANCH_AVAILABILITY -> "search";
            case CUSTOMER_LOOKUP -> "customer";
            case POLICY_QUESTION -> "policy";
            case GENERAL -> "general";
        };
    }
}
