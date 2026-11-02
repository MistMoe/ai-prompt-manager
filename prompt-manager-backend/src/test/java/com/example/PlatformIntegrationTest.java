package com.example;

import com.example.common.UserContext;
import com.example.sandbox.ModelRateLimiter;
import com.example.utils.JwtUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.MockMvc;
import redis.embedded.RedisServer;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlatformIntegrationTest {
    static RedisServer server;
    static int port;
    static {
        try {
            try (var socket = new ServerSocket(0)) { port = socket.getLocalPort(); }
            server = RedisServer.newRedisServer().port(port).setting("bind 127.0.0.1")
                    .setting("save \"\"").setting("appendonly no").build();
            server.start();
        } catch (Exception failure) { throw new ExceptionInInitializerError(failure); }
    }
    @DynamicPropertySource static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.port", () -> port);
    }
    @AfterAll static void stop() throws Exception { server.stop(); }
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate db;
    @Autowired JwtUtil jwt;
    @Autowired StringRedisTemplate redis;
    private String auth(long user) { return "Bearer " + jwt.createToken(user, "user" + user); }
    @BeforeEach void seed() {
        UserContext.clear();
        db.update("DELETE FROM prompt");
        db.update("INSERT INTO prompt(id,user_id,title,content,is_public,share_version,delete_status) VALUES(1,1,'私人模板','请回答 {{名字}}',0,0,0)");
        db.update("INSERT INTO prompt(id,user_id,title,content,is_public,share_version,delete_status) VALUES(2,2,'他人模板','private',0,0,0)");
        redis.keys("prompt:*").forEach(redis::delete);
    }
    @Test void ownershipChecksProtectReadUpdateDeleteAndPublishing() throws Exception {
        mvc.perform(get("/api/prompts/1")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/prompts/1").header("Authorization", auth(2))).andExpect(jsonPath("$.code").value(500));
        mvc.perform(put("/api/prompts/1").header("Authorization", auth(2)).contentType("application/json")
                .content("{\"title\":\"stolen\",\"content\":\"overwrite\"}")).andExpect(jsonPath("$.code").value(500));
        mvc.perform(delete("/api/prompts/1").header("Authorization", auth(2))).andExpect(jsonPath("$.code").value(500));
        mvc.perform(put("/api/public-prompts/1/visibility").param("published","true").header("Authorization",auth(2)))
                .andExpect(status().isNotFound());
        assertThat(db.queryForObject("SELECT content FROM prompt WHERE id=1",String.class)).isEqualTo("请回答 {{名字}}");
        assertThat(UserContext.getUserId()).isNull();
        mvc.perform(get("/api/prompts/page").header("Authorization", auth(2)))
                .andExpect(jsonPath("$.data.records.length()").value(1)).andExpect(jsonPath("$.data.records[0].id").value(2));
        mvc.perform(get("/api/prompts/1").header("Authorization","Bearer forged" )).andExpect(status().isUnauthorized());
        assertThat(UserContext.getUserId()).isNull();
    }
    @Test void publicVersionsInvalidateCacheAndCopiesRemainPrivate() throws Exception {
        mvc.perform(get("/api/public-prompts").header("Authorization",auth(2)))
                .andExpect(jsonPath("$.data.records.length()").value(0));
        mvc.perform(put("/api/public-prompts/1/visibility").param("published","true").header("Authorization",auth(1)))
                .andExpect(status().isOk());
        mvc.perform(get("/api/public-prompts").header("Authorization",auth(2)))
                .andExpect(jsonPath("$.data.records[0].content").value("请回答 {{名字}}"))
                .andExpect(jsonPath("$.data.records[0].userId").doesNotExist());
        assertThat(redis.hasKey("prompt:public:1:v1")).isTrue();
        mvc.perform(put("/api/prompts/1").header("Authorization",auth(1)).contentType("application/json")
                .content("{\"title\":\"已更新\",\"content\":\"新内容\"}")).andExpect(jsonPath("$.code").value(200));
        assertThat(redis.hasKey("prompt:public:1:v1")).isFalse();
        mvc.perform(post("/api/public-prompts/1/copy").header("Authorization",auth(2))).andExpect(jsonPath("$.code").value(200));
        assertThat(db.queryForObject("SELECT count(*) FROM prompt WHERE user_id=2 AND content='新内容' AND is_public=0",Integer.class)).isEqualTo(1);
        mvc.perform(put("/api/public-prompts/1/visibility").param("published","false").header("Authorization",auth(1)));
        // Even an old entry repopulated by an in-flight reader cannot bypass the database visibility check.
        redis.opsForValue().set("prompt:public:1:v2", "{\"content\":\"stale private data\"}");
        mvc.perform(get("/api/public-prompts").header("Authorization",auth(2)))
                .andExpect(jsonPath("$.data.records.length()").value(0));
        mvc.perform(post("/api/public-prompts/1/copy").header("Authorization",auth(2))).andExpect(status().isNotFound());
    }
    @Test void nativeRedisLuaAdmitsExactlyTheLimitUnderConcurrency() throws Exception {
        var limiter = new ModelRateLimiter(redis, 10, 60000);
        var pool = Executors.newFixedThreadPool(16);
        try {
            AtomicInteger allowed = new AtomicInteger();
            var tasks = new ArrayList<Future<?>>();
            for (int i=0; i<50; i++) tasks.add(pool.submit(() -> {
                try { limiter.acquire(99,1); allowed.incrementAndGet(); }
                catch (org.springframework.web.server.ResponseStatusException failure) { assertThat(failure.getStatusCode().value()).isEqualTo(429); }
            }));
            for (var task : tasks) task.get(10,TimeUnit.SECONDS);
            assertThat(allowed.get()).isEqualTo(10);
            assertThat(redis.opsForZSet().zCard("prompt:sandbox:rate:99")).isEqualTo(10);
            assertThat(redis.getExpire("prompt:sandbox:rate:99")).isPositive();
            limiter.acquire(100,3); limiter.acquire(100,3); limiter.acquire(100,3);
            assertThatThrownBy(() -> limiter.acquire(100,2)).isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
            assertThat(redis.opsForZSet().zCard("prompt:sandbox:rate:100")).isEqualTo(9);
            // Expired entries are removed inside the script, freeing a complete sliding window.
            redis.opsForZSet().add("prompt:sandbox:rate:101", "expired", 0);
            new ModelRateLimiter(redis,1,100).acquire(101,1);
            assertThat(redis.opsForZSet().zCard("prompt:sandbox:rate:101")).isEqualTo(1);
        } finally { pool.shutdownNow(); }
    }
    @Test void modelApiRejectsForeignAssetsAndUnknownModelsBeforeSpendingQuota() throws Exception {
        mvc.perform(post("/api/sandbox/run").header("Authorization",auth(2)).contentType("application/json")
                .content("{\"promptId\":1,\"variables\":{\"名字\":\"test\"},\"modelIds\":[\"unknown\"]}"))
                .andExpect(jsonPath("$.code").value(500));
        mvc.perform(post("/api/sandbox/run").header("Authorization",auth(1)).contentType("application/json")
                .content("{\"promptId\":1,\"variables\":{\"名字\":\"test\"},\"modelIds\":[\"unknown\"]}"))
                .andExpect(status().isBadRequest());
        assertThat(redis.keys("prompt:sandbox:rate:*")).isEmpty();
        mvc.perform(get("/api/sandbox/models").header("Authorization",auth(1)))
                .andExpect(jsonPath("$.data[0].apiKey").doesNotExist()).andExpect(jsonPath("$.data[0].apiUrl").doesNotExist());
    }
    @Test void favoritesAndCountersCannotRestoreOldPublicationState() throws Exception {
        db.update("UPDATE prompt SET is_public=0, share_version=10 WHERE id=1");
        mvc.perform(put("/api/prompts/1/favorite").param("isFavorite","1").header("Authorization",auth(1)))
                .andExpect(jsonPath("$.code").value(200));
        mvc.perform(put("/api/prompts/1/use").header("Authorization",auth(1))).andExpect(jsonPath("$.code").value(200));
        assertThat(db.queryForObject("SELECT share_version FROM prompt WHERE id=1",Long.class)).isEqualTo(10);
        assertThat(db.queryForObject("SELECT is_public FROM prompt WHERE id=1",Integer.class)).isZero();
        assertThat(db.queryForObject("SELECT use_count FROM prompt WHERE id=1",Integer.class)).isEqualTo(1);
    }
}
