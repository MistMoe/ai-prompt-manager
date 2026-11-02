package com.example.sandbox;

import com.example.common.UserContext;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import static org.assertj.core.api.Assertions.*;

class ModelClientTest {
    @Test void reportsProviderUsageAndClearsPropagatedIdentity() throws Exception {
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/chat", exchange -> {
            exchange.getRequestBody().readAllBytes();
            byte[] bytes = """
                    {"choices":[{"message":{"content":"模型回答"}}],"usage":{"prompt_tokens":12,"completion_tokens":8,"total_tokens":20}}
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length); exchange.getResponseBody().write(bytes); exchange.close();
        });
        server.start();
        try {
            var model = new SandboxProperties.Model(); model.setId("test"); model.setLabel("测试模型");
            model.setApiKey("test-key"); model.setModel("test"); model.setApiUrl("http://127.0.0.1:"+server.getAddress().getPort()+"/chat");
            var result = new ModelClient().call(42L, "hello", model);
            assertThat(result.answer()).isEqualTo("模型回答");
            assertThat(result.usage().totalTokens()).isEqualTo(20);
            assertThat(result.error()).isNull();
            assertThat(UserContext.getUserId()).isNull();
            model.setApiUrl("http://127.0.0.1:"+server.getAddress().getPort()+"/missing");
            assertThat(new ModelClient().call(43L, "hello", model).error()).isNotNull();
            assertThat(UserContext.getUserId()).isNull();
        } finally { server.stop(0); }
    }
    @Test void missingUsageIsNotInvented() throws Exception {
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/chat", exchange -> {
            exchange.getRequestBody().readAllBytes();
            byte[] bytes = "{\"choices\":[{\"message\":{\"content\":\"ok\"}}]}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length); exchange.getResponseBody().write(bytes); exchange.close();
        }); server.start();
        try {
            var model = new SandboxProperties.Model(); model.setId("test"); model.setLabel("Test"); model.setModel("test"); model.setApiKey("key");
            model.setApiUrl("http://127.0.0.1:"+server.getAddress().getPort()+"/chat");
            assertThat(new ModelClient().call(1L, "test", model).usage().totalTokens()).isNull();
        } finally { server.stop(0); }
    }
}
