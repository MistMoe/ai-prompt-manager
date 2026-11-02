package com.example.sandbox;

import com.example.common.UserContext;
import com.example.service.PromptService;
import jakarta.annotation.PreDestroy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.util.concurrent.*;

@Service
public class SandboxService {
    public record ModelOption(String id, String label, boolean configured) {}
    public record RunRequest(Long promptId, Map<String,String> variables, List<String> modelIds) {}
    private final PromptService prompts;
    private final SandboxProperties properties;
    private final ModelRateLimiter limiter;
    private final ModelClient client;
    private final Semaphore requests = new Semaphore(6);
    private final ExecutorService workers = new ThreadPoolExecutor(6, 6, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(12),
            task -> { Thread thread = new Thread(task, "prompt-model"); thread.setDaemon(true); return thread; });
    public SandboxService(PromptService prompts, SandboxProperties properties, ModelRateLimiter limiter, ModelClient client) {
        this.prompts = prompts; this.properties = properties; this.limiter = limiter; this.client = client;
    }
    public List<ModelOption> models() {
        return properties.getModels().stream().map(m -> new ModelOption(m.getId(), m.getLabel(), configured(m))).toList();
    }
    private boolean configured(SandboxProperties.Model m) {
        return m.getApiKey() != null && !m.getApiKey().isBlank() && m.getApiUrl() != null && !m.getApiUrl().isBlank();
    }
    public List<ModelClient.RunResult> run(RunRequest request) {
        Long userId = UserContext.getUserId();
        if (userId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录");
        if (request == null || request.promptId() == null || request.modelIds() == null || request.modelIds().isEmpty()
                || request.modelIds().size() > 3 || new HashSet<>(request.modelIds()).size() != request.modelIds().size())
            throw new IllegalArgumentException("请选择 1～3 个不同模型");
        String rendered = TemplateRenderer.render(prompts.getPromptDetail(request.promptId()).getContent(), request.variables());
        List<SandboxProperties.Model> selected = request.modelIds().stream().map(id -> properties.getModels().stream()
                .filter(m -> Objects.equals(id, m.getId()) && configured(m)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("模型未配置或不可用：" + id))).toList();
        if (!requests.tryAcquire()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "调试任务繁忙，请稍后重试");
        List<Future<ModelClient.RunResult>> futures = new ArrayList<>();
        try {
            limiter.acquire(userId, selected.size());
            long start = System.nanoTime(), deadline = start + TimeUnit.SECONDS.toNanos(25);
            for (var model : selected) futures.add(workers.submit(() -> client.call(userId, rendered, model)));
            List<ModelClient.RunResult> results = new ArrayList<>();
            for (int i = 0; i < selected.size(); i++) {
                try { results.add(futures.get(i).get(Math.max(1, deadline - System.nanoTime()), TimeUnit.NANOSECONDS)); }
                catch (TimeoutException | ExecutionException failed) {
                    futures.get(i).cancel(true);
                    var model = selected.get(i);
                    results.add(new ModelClient.RunResult(model.getId(), model.getLabel(), "",
                            TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start), null, "模型调用失败或超时"));
                }
            }
            return results;
        } catch (InterruptedException cancelled) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "调试已中断");
        } catch (RejectedExecutionException busy) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "调试任务繁忙，请稍后重试");
        } finally {
            futures.stream().filter(f -> !f.isDone()).forEach(f -> f.cancel(true));
            requests.release();
        }
    }
    @PreDestroy public void shutdown() { workers.shutdownNow(); }
}
