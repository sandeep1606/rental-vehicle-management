package com.rvms.backend.ai.graph.nodes;

import com.rvms.backend.ai.graph.IntentType;
import com.rvms.backend.ai.graph.RentalAssistantState;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Node 1: classifyIntent — deterministic keyword-based routing (kept rule-based rather
 * than LLM-based so workflow routing is exactly reproducible in unit tests).
 */
@Component
public class ClassifyIntentNode implements NodeAction<RentalAssistantState> {

    @Override
    public Map<String, Object> apply(RentalAssistantState state) {
        String msg = state.userMessage().toLowerCase();
        IntentType intent = classify(msg);
        return Map.of(RentalAssistantState.INTENT, intent);
    }

    private IntentType classify(String msg) {
        if (containsAny(msg, "policy", "cancellation", "cancel fee", "late fee", "late return", "refund policy",
                "insurance", "deductible", "mileage policy", "age requirement", "license requirement", "damage policy")) {
            return IntentType.POLICY_QUESTION;
        }
        if (containsAny(msg, "customer", "renter", "license number", "driver license", "driver's license")
                && containsAny(msg, "find", "look up", "lookup", "search")) {
            return IntentType.CUSTOMER_LOOKUP;
        }
        if (containsAny(msg, "availability summary", "how many vehicles", "branch summary", "location summary",
                "branch availability")) {
            return IntentType.BRANCH_AVAILABILITY;
        }
        if (containsAny(msg, "book", "reserve", "reservation", "how do i rent", "rental help")) {
            return IntentType.BOOKING_HELP;
        }
        if (containsAny(msg, "find", "search", "suv", "sedan", "truck", "van", "hatchback", "luxury",
                "convertible", "car", "vehicle")) {
            return IntentType.VEHICLE_SEARCH;
        }
        return IntentType.GENERAL;
    }

    private boolean containsAny(String haystack, String... needles) {
        for (String needle : needles) {
            if (haystack.contains(needle)) {
                return true;
            }
        }
        return false;
    }
}
