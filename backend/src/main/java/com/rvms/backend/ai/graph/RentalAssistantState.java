package com.rvms.backend.ai.graph;

import com.rvms.backend.dto.customer.CustomerResponse;
import com.rvms.backend.dto.vehicle.VehicleResponse;
import org.bsc.langgraph4j.state.AgentState;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Shared state threaded through the LangGraph4j workflow nodes. Each node reads what it
 * needs and returns a partial update map that gets merged back into this state by the graph.
 */
public class RentalAssistantState extends AgentState {

    public static final String SESSION_ID = "sessionId";
    public static final String USER_MESSAGE = "userMessage";
    public static final String INTENT = "intent";
    public static final String BRANCH_ID = "branchId";
    public static final String VEHICLE_TYPE = "vehicleType";
    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
    public static final String MAX_DAILY_RATE = "maxDailyRate";
    public static final String CUSTOMER_SEARCH_TERM = "customerSearchTerm";
    public static final String CUSTOMER_RESULTS = "customerResults";
    public static final String VEHICLE_RESULTS = "vehicleResults";
    public static final String POLICY_ANSWER = "policyAnswer";
    public static final String RECOMMENDATION = "recommendation";
    public static final String FINAL_RESPONSE = "finalResponse";
    public static final String TOOLS_USED = "toolsUsed";

    public RentalAssistantState(Map<String, Object> initData) {
        super(initData);
    }

    public String sessionId() {
        return this.<String>value(SESSION_ID).orElse(null);
    }

    public String userMessage() {
        return this.<String>value(USER_MESSAGE).orElse("");
    }

    public Optional<IntentType> intent() {
        return this.<IntentType>value(INTENT);
    }

    public Optional<Long> branchId() {
        return this.<Long>value(BRANCH_ID);
    }

    public Optional<String> vehicleType() {
        return this.<String>value(VEHICLE_TYPE);
    }

    public Optional<LocalDate> startDate() {
        return this.<LocalDate>value(START_DATE);
    }

    public Optional<LocalDate> endDate() {
        return this.<LocalDate>value(END_DATE);
    }

    public Optional<BigDecimal> maxDailyRate() {
        return this.<BigDecimal>value(MAX_DAILY_RATE);
    }

    public Optional<String> customerSearchTerm() {
        return this.<String>value(CUSTOMER_SEARCH_TERM);
    }

    @SuppressWarnings("unchecked")
    public List<CustomerResponse> customerResults() {
        return this.<List<CustomerResponse>>value(CUSTOMER_RESULTS).orElseGet(ArrayList::new);
    }

    @SuppressWarnings("unchecked")
    public List<VehicleResponse> vehicleResults() {
        return this.<List<VehicleResponse>>value(VEHICLE_RESULTS).orElseGet(ArrayList::new);
    }

    public Optional<String> policyAnswer() {
        return this.<String>value(POLICY_ANSWER);
    }

    public Optional<String> recommendation() {
        return this.<String>value(RECOMMENDATION);
    }

    public Optional<String> finalResponse() {
        return this.<String>value(FINAL_RESPONSE);
    }

    @SuppressWarnings("unchecked")
    public List<String> toolsUsed() {
        return this.<List<String>>value(TOOLS_USED).orElseGet(ArrayList::new);
    }
}
