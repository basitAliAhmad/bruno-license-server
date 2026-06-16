package com.bebasit.brunolicenseserver.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(RateLimiterProperties.class)
public class RateLimiterFilter extends OncePerRequestFilter {

    private static final ConcurrentHashMap<String, Bucket> BUCKETS = new ConcurrentHashMap<>();

    private final RateLimiterProperties properties;

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.builder()
            .capacity(properties.getCapacity())
            .refillGreedy(properties.getRefillTokens(), Duration.ofMinutes(properties.getRefillDurationMinutes()))
            .build();

        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    private Bucket resolveBucket(String key) {
        return BUCKETS.computeIfAbsent(key, k -> createNewBucket());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String ip = request.getRemoteAddr();

        Bucket bucket = resolveBucket(ip);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("{} is blocked by RateLimiterFilter", ip);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too many requests");
        }
    }
}
