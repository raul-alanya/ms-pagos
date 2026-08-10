package ms_pagos.config;

import ms_pagos.entity.ConfiguracionIzipay;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cliente HTTP hacia la API REST de IZIPAY (V4).
 * - crearPago(): llama a Charge/CreatePayment para obtener el formToken
 *   que necesita el formulario de pago hospedado de IZIPAY.
 * - validarFirmaIPN(): verifica la firma HMAC-SHA256 de la notificación
 *   IPN/webhook enviada por IZIPAY al final del pago.
 */
@Component
public class IzipayClient {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Crea el pago en IZIPAY y devuelve el formToken.
     * El monto se envía en unidades menores (céntimos).
     *
     * @throws RuntimeException si IZIPAY rechaza el pago o hay error de conexión
     */
    public String crearPago(ConfiguracionIzipay config, String reference, Double totalamount,
                            String currency, String email, String cardHolderName,
                            String urlReturn, String urlSuccess, String ipnTargetUrl) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            long amountMinor = Math.round(totalamount * 100);
            body.put("amount", amountMinor);
            body.put("currency", currency);
            body.put("orderId", reference);

            Map<String, Object> customer = new LinkedHashMap<>();
            customer.put("email", email);
            Map<String, Object> billing = new LinkedHashMap<>();
            String[] partes = cardHolderName == null ? new String[0]
                    : cardHolderName.trim().split("\\s+", 2);
            billing.put("firstName", partes.length > 0 ? partes[0] : "Cliente");
            billing.put("lastName", partes.length > 1 ? partes[1] : "");

            // API V4: billingDetails lleva campos planos; "address" es un string
            // y city/zipCode/country son hermanos (NO un objeto anidado).
            // En el formulario hospedado el cliente completa sus datos, así que
            // se envía una dirección genérica de respaldo.
            billing.put("streetNumber", "NA");
            billing.put("address", "Av. Principal 123");
            billing.put("city", "Lima");
            billing.put("zipCode", "15000");
            billing.put("country", "PE");

            customer.put("billingDetails", billing);
            body.put("customer", customer);

            if (urlReturn != null && !urlReturn.isBlank()) body.put("urlReturn", urlReturn);
            if (urlSuccess != null && !urlSuccess.isBlank()) body.put("urlSuccess", urlSuccess);
            if (ipnTargetUrl != null && !ipnTargetUrl.isBlank()) body.put("ipnTargetUrl", ipnTargetUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String auth = Base64.getEncoder().encodeToString(
                    (config.getMerchantCode() + ":" + config.getPasswordIzipay())
                            .getBytes(StandardCharsets.UTF_8));
            headers.set("Authorization", "Basic " + auth);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    config.getUrlPago(), HttpMethod.POST, request, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RuntimeException("IZIPAY respondió HTTP " + response.getStatusCodeValue()
                        + " al crear el pago.");
            }

            Map<?, ?> respuesta = response.getBody();
            Object status = respuesta.get("status");
            Object answer = respuesta.get("answer");
            if (answer instanceof Map<?, ?> answerMap) {
                Object formToken = answerMap.get("formToken");
                if ("SUCCESS".equalsIgnoreCase(String.valueOf(status)) && formToken != null) {
                    return formToken.toString();
                }
            }

            throw new RuntimeException("IZIPAY rechazó el pago: status=" + status
                    + ", respuesta=" + respuesta);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error de conexión con IZIPAY al crear el pago: " + e.getMessage(), e);
        }
    }

    /**
     * Crea una sesión de pago genérica en IZIPAY (Charge/CreateToken) y
     * devuelve el formToken de sesión.
     *
     * @throws RuntimeException si IZIPAY rechaza la operación
     */
    public String crearTokenSesion(ConfiguracionIzipay config) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("currency", config.getMoneda() != null ? config.getMoneda() : "PEN");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String auth = Base64.getEncoder().encodeToString(
                    (config.getMerchantCode() + ":" + config.getPasswordIzipay())
                            .getBytes(StandardCharsets.UTF_8));
            headers.set("Authorization", "Basic " + auth);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    config.getUrlToken(), HttpMethod.POST, request, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RuntimeException("IZIPAY respondió HTTP " + response.getStatusCodeValue()
                        + " al crear el token de sesión.");
            }

            Map<?, ?> respuesta = response.getBody();
            Object status = respuesta.get("status");
            Object answer = respuesta.get("answer");
            if (answer instanceof Map<?, ?> answerMap) {
                Object formToken = answerMap.get("formToken");
                if ("SUCCESS".equalsIgnoreCase(String.valueOf(status)) && formToken != null) {
                    return formToken.toString();
                }
            }

            throw new RuntimeException("IZIPAY rechazó la creación del token: status=" + status
                    + ", respuesta=" + respuesta);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error de conexión con IZIPAY al crear el token de sesión: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica la firma HMAC-SHA256 de la notificación IPN.
     * El kr-hash-key indica qué clave usar: "password" o "hmac-sha256".
     */
    public boolean validarFirmaIPN(String krAnswer, String krHash, String krHashKey,
                                   ConfiguracionIzipay config) {
        if (krAnswer == null || krHash == null) return false;
        String clave = "password".equalsIgnoreCase(krHashKey)
                ? config.getPasswordIzipay()
                : config.getHmacSha256();
        if (clave == null) return false;
        String esperado = hmacSha256(krAnswer, clave);
        return MessageDigest.isEqual(
                esperado.getBytes(StandardCharsets.UTF_8),
                krHash.toLowerCase().getBytes(StandardCharsets.UTF_8));
    }

    private String hmacSha256(String data, String clave) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(clave.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error al calcular HMAC-SHA256", e);
        }
    }
}
