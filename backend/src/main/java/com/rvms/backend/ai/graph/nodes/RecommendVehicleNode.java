package com.rvms.backend.ai.graph.nodes;

import com.rvms.backend.ai.graph.RentalAssistantState;
import com.rvms.backend.dto.vehicle.VehicleResponse;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Node 6: recommendVehicle — picks the cheapest matching vehicle from the real search
 * results and formats a recommendation strictly from those fields. Never invents a
 * vehicle, price, or booking confirmation.
 */
@Component
public class RecommendVehicleNode implements NodeAction<RentalAssistantState> {

    @Override
    public Map<String, Object> apply(RentalAssistantState state) {
        List<VehicleResponse> vehicles = state.vehicleResults();
        if (vehicles.isEmpty()) {
            return Map.of(RentalAssistantState.RECOMMENDATION,
                    "No vehicles matched those criteria — try widening the date range, price, or branch.");
        }

        VehicleResponse best = vehicles.stream()
                .min(Comparator.comparing(VehicleResponse::dailyRate))
                .orElse(vehicles.get(0));

        String recommendation = String.format(
                "Recommended: %s %s (%s, %s) at %s - plate %s, $%s/day.",
                best.brand(), best.model(), best.type(), best.transmission(), best.branchName(),
                best.plateNumber(), best.dailyRate());

        return Map.of(RentalAssistantState.RECOMMENDATION, recommendation);
    }
}
