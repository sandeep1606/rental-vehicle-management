package com.rvms.backend.ai.graph.nodes;

import com.rvms.backend.ai.graph.RentalAssistantState;
import com.rvms.backend.entity.VehicleType;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Node 2: extractRentalSearchCriteria — parses free text like
 * "Find me an SUV in location 2 from July 5 to July 10 under $80/day" into structured
 * search fields (vehicle type, branch id, date range, max daily rate) with lightweight
 * regex/keyword parsing. "location N" / "branch N" is treated as branch id N, matching
 * this project's 4 seeded branches (Downtown=1, Airport=2, Uptown=3, Suburban=4).
 */
@Component
public class ExtractRentalSearchCriteriaNode implements NodeAction<RentalAssistantState> {

    private static final Pattern LOCATION_PATTERN = Pattern.compile("(?:location|branch)\\s*#?(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PRICE_PATTERN = Pattern.compile("(?:under|below|less than)\\s*\\$?(\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATE_PATTERN = Pattern.compile(
            "(January|February|March|April|May|June|July|August|September|October|November|December)\\s+(\\d{1,2})",
            Pattern.CASE_INSENSITIVE);

    @Override
    public Map<String, Object> apply(RentalAssistantState state) {
        String msg = state.userMessage();
        Map<String, Object> updates = new HashMap<>();

        findVehicleType(msg).ifPresent(t -> updates.put(RentalAssistantState.VEHICLE_TYPE, t));
        findLocationOrdinal(msg).ifPresent(id -> updates.put(RentalAssistantState.BRANCH_ID, id));
        findMaxPrice(msg).ifPresent(p -> updates.put(RentalAssistantState.MAX_DAILY_RATE, p));

        LocalDate[] dates = findDateRange(msg);
        if (dates[0] != null) {
            updates.put(RentalAssistantState.START_DATE, dates[0]);
        }
        if (dates[1] != null) {
            updates.put(RentalAssistantState.END_DATE, dates[1]);
        }

        return updates;
    }

    private Optional<String> findVehicleType(String msg) {
        String upper = msg.toUpperCase(Locale.ROOT);
        for (VehicleType type : VehicleType.values()) {
            if (upper.contains(type.name())) {
                return Optional.of(type.name());
            }
        }
        return Optional.empty();
    }

    private Optional<Long> findLocationOrdinal(String msg) {
        Matcher m = LOCATION_PATTERN.matcher(msg);
        if (m.find()) {
            return Optional.of(Long.parseLong(m.group(1)));
        }
        return Optional.empty();
    }

    private Optional<BigDecimal> findMaxPrice(String msg) {
        Matcher m = PRICE_PATTERN.matcher(msg);
        if (m.find()) {
            return Optional.of(new BigDecimal(m.group(1)));
        }
        return Optional.empty();
    }

    private LocalDate[] findDateRange(String msg) {
        Matcher m = DATE_PATTERN.matcher(msg);
        LocalDate[] result = new LocalDate[2];
        int index = 0;
        int currentYear = LocalDate.now().getYear();
        while (m.find() && index < 2) {
            Month month = parseMonth(m.group(1));
            int day = Integer.parseInt(m.group(2));
            LocalDate candidate = LocalDate.of(currentYear, month, day);
            if (candidate.isBefore(LocalDate.now())) {
                candidate = candidate.plusYears(1);
            }
            result[index++] = candidate;
        }
        return result;
    }

    private Month parseMonth(String name) {
        for (Month month : Month.values()) {
            if (month.getDisplayName(TextStyle.FULL, Locale.ENGLISH).equalsIgnoreCase(name)) {
                return month;
            }
        }
        throw new IllegalArgumentException("Unknown month: " + name);
    }
}
