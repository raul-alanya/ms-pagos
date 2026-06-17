package ms_pagos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.RequiredArgsConstructor;
import java.util.Base64;
import java.util.Map;
import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class IzipayClient {

    private final WebClient webClient;

    @Value("${izipay.username}")
    private String username;

    @Value("${izipay.password}")
    private String password;

    @Value("${izipay.merchant-code}")
    private String merchantCode;

    private String getBasicAuth() {
        String credenciales = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credenciales.getBytes());
    }

    public Map<String, Object> procesarPago(Map<String, Object> request) {
        return webClient.post()
                .uri("/api-payment/V4/Charge/CreatePayment")
                .header("Authorization", getBasicAuth())
                .header("Content-Type", "application/json")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}