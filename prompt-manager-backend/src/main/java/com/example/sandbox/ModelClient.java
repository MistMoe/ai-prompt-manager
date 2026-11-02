package com.example.sandbox;

import com.example.common.UserContext;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ModelClient {
    public record Usage(Long promptTokens, Long completionTokens, Long totalTokens) {}
    public record RunResult(String modelId, String label, String answer, long elapsedMs, Usage usage, String error) {}
    private final RestClient client;
    public ModelClient() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000); factory.setReadTimeout(20000);
        client = RestClient.builder().requestFactory(factory).build();
    }
    public RunResult call(long userId, String prompt, SandboxProperties.Model model) {
        long start = System.nanoTime();
        // Explicitly propagate authenticated identity; ThreadLocal values do not follow executor tasks.
        UserContext.setUserId(userId);
        try {
            JsonNode response = client.post().uri(model.getApiUrl()).header("Authorization", "Bearer " + model.getApiKey())
                    .body(Map.of("model", model.getModel(), "stream", false, "max_tokens", 1500,
                            "messages", List.of(Map.of("role", "user", "content", prompt))))
                    .retrieve().body(JsonNode.class);
            String text = response == null ? "" : response.path("choices").path(0).path("message").path("content").asText("");
            if (text.isBlank()) throw new IllegalStateException("模型未返回有效文本");
            JsonNode usage = response.path("usage");
            return new RunResult(model.getId(), model.getLabel(), text, elapsed(start),
                    new Usage(tokens(usage, "prompt_tokens"), tokens(usage, "completion_tokens"), tokens(usage, "total_tokens")), null);
        } catch (Exception failure) {
            // Do not return provider error bodies: they may contain credentials or internal details.
            return new RunResult(model.getId(), model.getLabel(), "", elapsed(start), null, "模型调用失败或超时，请检查服务端配置后重试");
        } finally { UserContext.clear(); }
    }
    private Long tokens(JsonNode usage, String name) {
        JsonNode value = usage.path(name);
        return value.isIntegralNumber() && value.canConvertToLong() && value.asLong() >= 0 ? value.asLong() : null;
    }
    private long elapsed(long start) { return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start); }
}
