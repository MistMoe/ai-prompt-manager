package com.example.sandbox;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.server.ResponseStatusException;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
class ModelRateLimiterTest {
    @Test void redisFailureClosesPaidModelAccess() {
        var redis = mock(StringRedisTemplate.class);
        when(redis.execute(any(org.springframework.data.redis.core.script.RedisScript.class), anyList(), any(Object[].class)))
                .thenThrow(new IllegalStateException("offline"));
        assertThatThrownBy(() -> new ModelRateLimiter(redis,10,60000).acquire(1,1))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> assertThat(e.getStatusCode().value()).isEqualTo(503));
    }
}
