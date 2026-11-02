package com.example;
import com.example.service.PublicPromptService;
import com.example.entity.Prompt;
import com.example.mapper.PromptMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.assertj.core.api.Assertions.*;

class PublicCacheFallbackTest {
    @Test void databaseTemplatesStayAvailableWithoutRepeatedRedisTimeouts() {
        var mapper = mock(PromptMapper.class); var redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked") ValueOperations<String,String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(values.get(anyString())).thenThrow(new IllegalStateException("offline"));
        var prompt = new Prompt(); prompt.setId(1L); prompt.setShareVersion(0L); prompt.setContent("from database");
        when(mapper.selectOne(any())).thenReturn(prompt);
        var cache = new PublicPromptService(mapper, redis, new ObjectMapper());
        assertThat(cache.get(1L).content()).isEqualTo("from database");
        assertThat(cache.get(1L).content()).isEqualTo("from database");
        verify(values, times(1)).get(anyString());
    }
}
