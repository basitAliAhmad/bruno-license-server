package com.bebasit.brunolicenseserver.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.lang.reflect.Field;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimiterFilterTest {

    private static final int CAPACITY = 10;

    private RateLimiterFilter filter;

    @BeforeEach
    void setup() throws Exception {
        filter = new RateLimiterFilter(new RateLimiterProperties());
        clearBuckets();
    }

    @AfterEach
    void clearBuckets() throws Exception {
        Field field = RateLimiterFilter.class.getDeclaredField("BUCKETS");
        field.setAccessible(true);
        ((Map<?, ?>) field.get(null)).clear();
    }

    // --- within limit ---

    @Test
    void firstRequest_passesThroughFilterChain() throws Exception {
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(buildRequest("10.0.0.1"), new MockHttpServletResponse(), chain);
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void requestsWithinCapacity_allSucceed() throws Exception {
        for (int i = 0; i < CAPACITY; i++) {
            MockHttpServletResponse response = perform("10.0.0.2");
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        }
    }

    // --- over limit ---

    @Test
    void requestOverCapacity_returns429() throws Exception {
        exhaustBucket("10.0.0.3");
        MockHttpServletResponse response = perform("10.0.0.3");
        assertThat(response.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    @Test
    void requestOverCapacity_returnsTooManyRequestsBody() throws Exception {
        exhaustBucket("10.0.0.4");
        MockHttpServletResponse response = perform("10.0.0.4");
        assertThat(response.getContentAsString()).isEqualTo("Too many requests");
    }

    @Test
    void requestOverCapacity_doesNotInvokeFilterChain() throws Exception {
        exhaustBucket("10.0.0.5");
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(buildRequest("10.0.0.5"), new MockHttpServletResponse(), chain);
        assertThat(chain.getRequest()).isNull();
    }

    // --- per-IP isolation ---

    @Test
    void differentIps_haveIndependentBuckets() throws Exception {
        exhaustBucket("10.0.0.6");
        MockHttpServletResponse response = perform("10.0.0.7");
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void exhaustedIp_staysBlockedWhileOtherIpSucceeds() throws Exception {
        exhaustBucket("10.0.0.8");
        perform("10.0.0.9");
        MockHttpServletResponse response = perform("10.0.0.8");
        assertThat(response.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    // --- helpers ---

    private MockHttpServletRequest buildRequest(String ip) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/status");
        request.setRemoteAddr(ip);
        return request;
    }

    private MockHttpServletResponse perform(String ip) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(buildRequest(ip), response, new MockFilterChain());
        return response;
    }

    private void exhaustBucket(String ip) throws Exception {
        for (int i = 0; i < CAPACITY; i++) {
            perform(ip);
        }
    }
}
