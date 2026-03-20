package com.example.revly.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openai")
public class OpenAiProperties {

    private Api api = new Api();
    private Embeddings embeddings = new Embeddings();
    private Http http = new Http();

    public static class Api {
        private String baseUrl;
        private String key;

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
    }

    public static class Embeddings {
        private String model;
        private int dimensions;

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }

        public int getDimensions() { return dimensions; }
        public void setDimensions(int dimensions) { this.dimensions = dimensions; }
    }

    public static class Http {
        private int timeoutSeconds = 20;

        public int getTimeoutSeconds() { return timeoutSeconds; }
        public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }

        public Duration timeout() { return Duration.ofSeconds(timeoutSeconds); }
    }

    public Api getApi() { return api; }
    public Embeddings getEmbeddings() { return embeddings; }
    public Http getHttp() { return http; }
}