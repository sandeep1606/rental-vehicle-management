package com.rvms.backend.ai.graph.nodes;

import com.rvms.backend.ai.graph.RentalAssistantState;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ExtractRentalSearchCriteriaNodeTest {

    private final ExtractRentalSearchCriteriaNode node = new ExtractRentalSearchCriteriaNode();

    @Test
    void extractsTypeBranchDatesAndPriceFromExampleMessage() throws Exception {
        RentalAssistantState state = new RentalAssistantState(Map.of(
                RentalAssistantState.USER_MESSAGE,
                "Find me an SUV in location 2 from July 5 to July 10 under $80/day"));

        Map<String, Object> updates = node.apply(state);

        assertThat(updates.get(RentalAssistantState.VEHICLE_TYPE)).isEqualTo("SUV");
        assertThat(updates.get(RentalAssistantState.BRANCH_ID)).isEqualTo(2L);
        assertThat(updates.get(RentalAssistantState.MAX_DAILY_RATE)).isEqualTo(new BigDecimal("80"));

        LocalDate start = (LocalDate) updates.get(RentalAssistantState.START_DATE);
        LocalDate end = (LocalDate) updates.get(RentalAssistantState.END_DATE);
        assertThat(start.getMonth()).isEqualTo(Month.JULY);
        assertThat(start.getDayOfMonth()).isEqualTo(5);
        assertThat(end.getDayOfMonth()).isEqualTo(10);
    }

    @Test
    void leavesFieldsAbsentWhenNotMentioned() throws Exception {
        RentalAssistantState state = new RentalAssistantState(Map.of(
                RentalAssistantState.USER_MESSAGE, "I need a car"));

        Map<String, Object> updates = node.apply(state);

        assertThat(updates).doesNotContainKey(RentalAssistantState.BRANCH_ID);
        assertThat(updates).doesNotContainKey(RentalAssistantState.MAX_DAILY_RATE);
    }
}
