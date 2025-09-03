package com.yakrooms.be.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configuration class for UploadThing API integration.
 * Provides WebClient configuration with proper timeout, retry logic, and error handling.
 */
@Configuration
public class UploadThingConfig {

    @Value("${uploadthing.api.base-url}")
    private String baseUrl;

    @Value("${uploadthing.api.secret}")
    private String apiSecret;

    @Value("${uploadthing.api.app-id}")
    private String appId;

    @Value("${uploadthing.api.timeout:30s}")
    private Duration timeout;

    @Value("${uploadthing.api.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${uploadthing.api.retry.backoff-delay:1s}")
    private Duration backoffDelay;

    /**
     * Creates a configured WebClient for UploadThing API calls.
     * Includes timeout configuration, retry logic, and error handling.
     */
    @Bean
    public WebClient uploadThingWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) timeout.toMillis())
                .responseTimeout(timeout)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(timeout.toMillis(), TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(timeout.toMillis(), TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Authorization", "Bearer " + apiSecret)
                .defaultHeader("Content-Type", "application/json")
                .filter(retryFilter())
                .filter(errorHandler())
                .build();
    }

    /**
     * Retry filter for handling transient failures.
     * Implements exponential backoff strategy.
     */
    private ExchangeFilterFunction retryFilter() {
        return (request, next) -> next.exchange(request)
                .retryWhen(Retry.backoff(maxRetryAttempts, backoffDelay)
                        .filter(throwable -> throwable instanceof RuntimeException)
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            throw new RuntimeException("UploadThing API call failed after " + maxRetryAttempts + " attempts", 
                                    retrySignal.failure());
                        }));
    }

    /**
     * Error handling filter for logging and transforming errors.
     */
    private ExchangeFilterFunction errorHandler() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            return Mono.just(clientRequest);
        }).andThen(ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().isError()) {
                return clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            String errorMessage = String.format("UploadThing API error: %s - %s", 
                                    clientResponse.statusCode(), errorBody);
                            return Mono.error(new RuntimeException(errorMessage));
                        });
            }
            return Mono.just(clientResponse);
        }));
    }
}
