package de.staatsbibliothek.berlin.hsp.fo.discovery.config;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.util.Timeout;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
  import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebConfig {
  @LoadBalanced
  @Bean
  WebClient.Builder webClientBuilder() {
    return WebClient.builder();
  }

  @Bean
  CloseableHttpAsyncClient closeableHttpAsyncClient() {
    return HttpAsyncClients.custom()
        .setDefaultRequestConfig(RequestConfig.custom()
            .setConnectTimeout(Timeout.ofMinutes(1))
            .setResponseTimeout(Timeout.ofMinutes(1))
            .build())
        .build();
  }
}