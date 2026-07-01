package com.rvms.backend.cache;

import com.rvms.backend.config.LruCacheProperties;
import com.rvms.backend.entity.Customer;
import com.rvms.backend.mapper.CustomerMapper;
import com.rvms.backend.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerSearchCacheServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerSearchCacheService cacheService;

    @BeforeEach
    void setUp() {
        // useRedis=false and no RedisTemplate bean available -> exercises the in-memory LRU fallback.
        LruCacheProperties properties = new LruCacheProperties(true, 2, 600, false);
        ObjectProvider<org.springframework.data.redis.core.RedisTemplate<String, Object>> emptyProvider = new ObjectProvider<>() {
            @Override
            public org.springframework.data.redis.core.RedisTemplate<String, Object> getObject() {
                throw new IllegalStateException("no redis in tests");
            }

            @Override
            public org.springframework.data.redis.core.RedisTemplate<String, Object> getObject(Object... args) {
                throw new IllegalStateException("no redis in tests");
            }

            @Override
            public org.springframework.data.redis.core.RedisTemplate<String, Object> getIfAvailable() {
                return null;
            }

            @Override
            public org.springframework.data.redis.core.RedisTemplate<String, Object> getIfUnique() {
                return null;
            }
        };
        cacheService = new CustomerSearchCacheService(customerRepository, new CustomerMapper(), properties, emptyProvider);
    }

    private Customer customer(String name, String email) {
        return Customer.builder()
                .id(1L)
                .fullName(name)
                .email(email)
                .phone("+1-555-0000")
                .driverLicenseNumber("DL-" + name.hashCode())
                .blacklisted(false)
                .build();
    }

    @Test
    void cachesRepeatedSearchesAndAvoidsSecondRepositoryCall() {
        when(customerRepository.searchByTerm("alice")).thenReturn(List.of(customer("Alice", "alice@example.com")));

        List<?> first = cacheService.search("Alice");
        List<?> second = cacheService.search("alice"); // same term, different case -> should still hit cache

        assertThat(first).hasSize(1);
        assertThat(second).hasSize(1);
        verify(customerRepository, times(1)).searchByTerm("alice");
    }

    @Test
    void evictsLeastRecentlyUsedSearchTermWhenCacheExceedsConfiguredSize() {
        when(customerRepository.searchByTerm(anyString())).thenAnswer(inv ->
                List.of(customer("Match-" + inv.getArgument(0), inv.getArgument(0) + "@example.com")));

        cacheService.search("alice");
        cacheService.search("bob");
        assertThat(cacheService.localCacheSize()).isEqualTo(2);

        // max size is 2 -> adding a third distinct term evicts "alice" (least recently used)
        cacheService.search("carol");
        assertThat(cacheService.localCacheSize()).isEqualTo(2);
        assertThat(cacheService.localCache().containsKey("alice")).isFalse();
        assertThat(cacheService.localCache().containsKey("bob")).isTrue();
        assertThat(cacheService.localCache().containsKey("carol")).isTrue();

        // re-searching "alice" must hit the repository again since it was evicted
        cacheService.search("alice");
        verify(customerRepository, times(2)).searchByTerm("alice");
    }
}
