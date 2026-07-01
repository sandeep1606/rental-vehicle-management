package com.rvms.backend.ai.tools;

import com.rvms.backend.cache.CustomerSearchCacheService;
import com.rvms.backend.dto.customer.CustomerResponse;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Backend tool the AI assistant calls to look up customers. Delegates to the same
 * LRU-cached search used by the REST API (CustomerSearchCacheService), so repeated
 * lookups by staff/AI benefit from the same cache.
 */
@Component
@RequiredArgsConstructor
public class CustomerLookupTool {

    private final CustomerSearchCacheService customerSearchCacheService;

    @Tool("Look up customers by name, email, phone number, or driver license number")
    public List<CustomerResponse> lookupCustomers(String searchTerm) {
        return customerSearchCacheService.search(searchTerm);
    }
}
