package com.example.sandbox;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix="sandbox")
public class SandboxProperties {
    private List<Model> models = new ArrayList<>();
    @Data public static class Model {
        private String id;
        private String label;
        private String apiUrl;
        private String apiKey = "";
        private String model;
    }
}
