package com.rvms.backend.ai.graph.nodes;

import com.rvms.backend.ai.graph.RentalAssistantState;
import com.rvms.backend.ai.tools.VehicleSearchTool;
import com.rvms.backend.dto.vehicle.VehicleResponse;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Node 4: searchAvailableVehicles — real-time availability from VehicleSearchTool
 * (backed by VehicleService/the database), never fabricated by the model.
 */
@Component
@RequiredArgsConstructor
public class SearchAvailableVehiclesNode implements NodeAction<RentalAssistantState> {

    private final VehicleSearchTool vehicleSearchTool;

    @Override
    public Map<String, Object> apply(RentalAssistantState state) {
        LocalDate start = state.startDate().orElse(LocalDate.now());
        LocalDate end = state.endDate().orElse(start.plusDays(1));

        List<VehicleResponse> results = vehicleSearchTool.searchAvailableVehicles(
                state.branchId().orElse(null),
                state.vehicleType().orElse(null),
                start,
                end,
                state.maxDailyRate().orElse(null)
        );

        List<String> tools = new ArrayList<>(state.toolsUsed());
        tools.add("VehicleSearchTool.searchAvailableVehicles");

        return Map.of(
                RentalAssistantState.VEHICLE_RESULTS, results,
                RentalAssistantState.START_DATE, start,
                RentalAssistantState.END_DATE, end,
                RentalAssistantState.TOOLS_USED, tools
        );
    }
}
