package com.rvms.backend.ai.tools;

import com.rvms.backend.dto.vehicle.VehicleResponse;
import com.rvms.backend.dto.vehicle.VehicleSearchRequest;
import com.rvms.backend.service.VehicleService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Backend tool the AI assistant calls for real-time vehicle availability.
 * Data always comes straight from VehicleService/the database — the model never
 * fabricates vehicles, prices, or availability.
 */
@Component
@RequiredArgsConstructor
public class VehicleSearchTool {

    private final VehicleService vehicleService;

    @Tool("Search for available rental vehicles filtered by branch id, vehicle type, date range, and maximum daily rate")
    public List<VehicleResponse> searchAvailableVehicles(
            Long branchId, String vehicleType, LocalDate startDate, LocalDate endDate, BigDecimal maxDailyRate) {
        return vehicleService.search(new VehicleSearchRequest(branchId, vehicleType, startDate, endDate, maxDailyRate));
    }
}
