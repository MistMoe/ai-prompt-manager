package com.example.sandbox;

import com.example.common.UserContext;
import com.example.entity.Prompt;
import com.example.service.PromptService;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class SandboxServiceTest {
    @Test void submitsAllModelsConcurrentlyAndKeepsIndividualFailures() throws Exception {
        var prompts = mock(PromptService.class); var limiter = mock(ModelRateLimiter.class); var client = mock(ModelClient.class);
        var prompt = new Prompt(); prompt.setContent("hello {{name}}"); when(prompts.getPromptDetail(1L)).thenReturn(prompt);
        var properties = new SandboxProperties();
        properties.setModels(List.of(model("first"), model("second")));
        var entered = new CountDownLatch(2);
        when(client.call(eq(7L), eq("hello $&"), any())).thenAnswer(invocation -> {
            assertThat(UserContext.getUserId()).isNull(); // Worker identity is explicitly passed as an argument.
            entered.countDown(); assertThat(entered.await(2,TimeUnit.SECONDS)).isTrue();
            SandboxProperties.Model model = invocation.getArgument(2);
            if (model.getId().equals("second")) throw new IllegalStateException("provider failed");
            return new ModelClient.RunResult("first","first","ok",10,null,null);
        });
        var sandbox = new SandboxService(prompts, properties, limiter, client);
        UserContext.setUserId(7L);
        try {
            var results = sandbox.run(new SandboxService.RunRequest(1L, Map.of("name","$&"),List.of("first","second")));
            assertThat(results).hasSize(2);
            assertThat(results.get(0).answer()).isEqualTo("ok");
            assertThat(results.get(1).error()).isNotBlank();
            verify(limiter).acquire(7L,2);
            assertThat(UserContext.getUserId()).isEqualTo(7L);
        } finally { UserContext.clear(); sandbox.shutdown(); }
    }
    private SandboxProperties.Model model(String id) {
        var model = new SandboxProperties.Model(); model.setId(id); model.setLabel(id);
        model.setApiKey("test"); model.setApiUrl("http://localhost/mock"); return model;
    }
}
