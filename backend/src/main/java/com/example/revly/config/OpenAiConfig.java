package com.example.revly.config;

import java.time.Duration;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
@EnableConfigurationProperties(OpenAiProperties.class)
public class OpenAiConfig {

    @Bean
    public WebClient openAiWebClient(OpenAiProperties props) {
        if (props.getApi().getKey() == null || props.getApi().getKey().isBlank()) {
            throw new IllegalStateException("Missing OpenAI API key. Set OPENAI_API_KEY env var.");
        }
        if (props.getApi().getBaseUrl() == null || props.getApi().getBaseUrl().isBlank()) {
            throw new IllegalStateException("Missing openai.api.base-url");
        }

        Duration timeout = props.getHttp().timeout();

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) timeout.toMillis())
                .responseTimeout(timeout)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler((int) timeout.getSeconds()))
                        .addHandlerLast(new WriteTimeoutHandler((int) timeout.getSeconds()))
                );

        return WebClient.builder()
                .baseUrl(props.getApi().getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.getApi().getKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}