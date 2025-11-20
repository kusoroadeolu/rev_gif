package com.github.kusoroadeolu.revgif;

import com.github.kusoroadeolu.revgif.configprops.RateLimitConfigProperties;
import com.github.kusoroadeolu.revgif.exceptions.RateLimitException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, Long> redisTemplate;
    private final RateLimitConfigProperties rateLimitConfigProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String ip = request.getRemoteAddr();
        try{
            this.rateLimit(ip);
        }catch (RateLimitException e){
            response.sendError(429, "Too many requests. Slow down...");
        }

    }

    private void rateLimit(final String ip) throws RateLimitException{
        final ValueOperations<String, Long> valueOperations = this.redisTemplate.opsForValue();
        final LocalDateTime now = LocalDateTime.now();
        final int elapsedSec = now.getSecond();
        final int secFromLastMin = 60 - elapsedSec;
        final int currentMin = now.getMinute();
        final int prevMinute = currentMin - 1 == 0 ? 59 : currentMin - 1;
        final String currKey = this.buildKey(ip, currentMin);

        final Long requestsThisMin = valueOperations.increment(currKey , 1L);
        Long requestsLastMin = valueOperations.get(this.buildKey(ip, prevMinute));
        if(requestsLastMin == null) requestsLastMin = 0L;

        final double avgRequests = ((requestsThisMin * elapsedSec) + (requestsLastMin * secFromLastMin)) / 60.0;
        if(avgRequests > this.rateLimitConfigProperties.reqPerMinute())throw new RateLimitException();

        valueOperations.getAndExpire(currKey, this.rateLimitConfigProperties.keyTtl(), TimeUnit.MINUTES);
    }


    private String buildKey(String ip, int currentMinute){
        return"%s&%s".formatted(ip, currentMinute);
    }
}
