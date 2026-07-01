package com.rvms.backend.ai.graph.nodes;

import com.rvms.backend.ai.graph.IntentType;
import com.rvms.backend.ai.graph.RentalAssistantState;
import com.rvms.backend.dto.customer.CustomerResponse;
import com.rvms.backend.dto.vehicle.VehicleResponse;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Node 7: finalResponse — assembles the natural-language reply from whichever upstream
 * node(s) ran, using only real data already present in state (search results, RAG
 * answer, recommendation). This node never adds new facts of its own.
 */
@Component
public class FinalResponseNode implements NodeAction<RentalAssistantState> {

    @Override
    public Map<String, Object> apply(RentalAssistantState state) {
        IntentType intent = state.intent().orElse(IntentType.GENERAL);
        String response = switch (intent) {
            case POLICY_QUESTION -> state.policyAnswer().orElse("I don't have policy information on that yet.");
            case CUSTOMER_LOOKUP -> formatCustomers(state.customerResults());
            case VEHICLE_SEARCH, BOOKING_HELP, BRANCH_AVAILABILITY -> formatVehicleSearch(state);
            case GENERAL -> "I can help you search vehicles, look up customers, check branch availability, "
                    + "or answer rental policy questions. What would you like to do?";
        };

        return Map.of(RentalAssistantState.FINAL_RESPONSE, response);
    }

    private String formatVehicleSearch(RentalAssistantState state) {
        List<VehicleResponse> vehicles = state.vehicleResults();
        if (vehicles.isEmpty()) {
            return "I couldn't find any vehicles matching that request. " + state.recommendation().orElse("");
        }
        String list = vehicles.stream()
                .map(v -> String.format("- %s %s (%s) at %s: $%s/day [%s]",
                        v.brand(), v.model(), v.type(), v.branchName(), v.dailyRate(), v.plateNumber()))
                .collect(Collectors.joining("\n"));
        String recommendation = state.recommendation().map(r -> "\n\n" + r).orElse("");
        return "Here's what's available:\n" + list + recommendation;
    }

    private String formatCustomers(List<CustomerResponse> customers) {
        if (customers.isEmpty()) {
            return "No customers matched that search.";
        }
        return "Found " + customers.size() + " matching customer(s):\n" + customers.stream()
                .map(c -> String.format("- %s (%s, %s) license %s%s",
                        c.fullName(), c.email(), c.phone(), c.driverLicenseNumber(),
                        c.blacklisted() ? " [BLACKLISTED]" : ""))
                .collect(Collectors.joining("\n"));
    }
}
