package com.rvms.backend.ai.graph.nodes;

import com.rvms.backend.ai.graph.RentalAssistantState;
import com.rvms.backend.ai.tools.CustomerLookupTool;
import com.rvms.backend.dto.customer.CustomerResponse;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Node 3: searchCustomerWithLRU — extracts a search term from free text and looks the
 * customer up via CustomerLookupTool, which is backed by the Redis/in-memory LRU cache.
 */
@Component
@RequiredArgsConstructor
public class SearchCustomerWithLruNode implements NodeAction<RentalAssistantState> {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[\\w.+-]+@[\\w-]+\\.[\\w.-]+");
    private static final Pattern ID_LIKE_PATTERN = Pattern.compile("\\b[A-Za-z]{1,4}-?\\d{4,}\\b");
    private static final List<String> STOP_WORDS = List.of(
            "find", "look", "up", "lookup", "search", "for", "customer", "renter", "the", "a", "an", "please", "me",
            "with", "having", "whose", "license", "licence", "number", "driver", "drivers", "id", "phone", "email",
            "named", "called", "named:", "is");

    private final CustomerLookupTool customerLookupTool;

    @Override
    public Map<String, Object> apply(RentalAssistantState state) {
        String term = extractSearchTerm(state.userMessage());
        List<CustomerResponse> results = customerLookupTool.lookupCustomers(term);

        List<String> tools = new ArrayList<>(state.toolsUsed());
        tools.add("CustomerLookupTool.lookupCustomers");

        return Map.of(
                RentalAssistantState.CUSTOMER_SEARCH_TERM, term,
                RentalAssistantState.CUSTOMER_RESULTS, results,
                RentalAssistantState.TOOLS_USED, tools
        );
    }

    private String extractSearchTerm(String message) {
        Matcher emailMatcher = EMAIL_PATTERN.matcher(message);
        if (emailMatcher.find()) {
            return emailMatcher.group();
        }
        Matcher idMatcher = ID_LIKE_PATTERN.matcher(message);
        if (idMatcher.find()) {
            return idMatcher.group();
        }
        String[] words = message.replaceAll("[^a-zA-Z0-9@.\\s-]", "").split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isBlank() && !STOP_WORDS.contains(w.toLowerCase())) {
                sb.append(w).append(' ');
            }
        }
        return sb.toString().trim();
    }
}
