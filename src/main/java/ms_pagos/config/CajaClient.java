package ms_pagos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class CajaClient {

    // FIX 1: URL correcta — ms-back-caja corre en puerto 8007
    @Value("${ms.caja.url:http://192.171.100.23:8007}")
    private String cajaBaseUrl;

    // FIX 2: API Key requerida por ms-back-caja
    @Value("${ms.caja.api-key:}")
    private String cajaApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Construye los headers con x-api-key para todas las llamadas al ms-back-caja.
     */
    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (cajaApiKey != null && !cajaApiKey.isBlank()) {
            headers.set("x-api-key", cajaApiKey);
        }
        return headers;
    }

    /**
     * BUS-IZI-003: Verifica que la caja exista y esté abierta antes de registrar un pago.
     * FIX 3: el ms-back-caja devuelve estado como número (1 = abierta, 0 = cerrada).
     */
    public boolean verificarCajaAbierta(Long cajaId) {
        try {
            String url = cajaBaseUrl + "/api/cajas/" + cajaId;

            // FIX 2: enviar x-api-key en el header
            HttpEntity<Void> request = new HttpEntity<>(buildHeaders());
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return false;
            }

            Map<?, ?> body = response.getBody();

            // Intentar leer desde "data" si existe, si no leer directo del body
            Object target = body.get("data");
            Map<?, ?> dataMap = (target instanceof Map<?, ?> m) ? m : body;

            // FIX 3: el estado puede venir como número (1/0) o como texto (ABIERTA/ABIERTO)
            Object estado = dataMap.get("estado");
            if (estado != null) {
                String estadoStr = estado.toString().trim();
                // Número: 1 = abierta
                if (estadoStr.equals("1")) return true;
                if (estadoStr.equals("0")) return false;
                // Texto
                if ("ABIERTA".equalsIgnoreCase(estadoStr)
                        || "ABIERTO".equalsIgnoreCase(estadoStr)
                        || "OPEN".equalsIgnoreCase(estadoStr)
                        || "ACTIVA".equalsIgnoreCase(estadoStr)
                        || "ACTIVE".equalsIgnoreCase(estadoStr)) {
                    return true;
                }
                return false;
            }

            // Algunos micros usan campo booleano "abierto"
            Object abierto = dataMap.get("abierto");
            if (abierto != null) {
                if (abierto instanceof Boolean) return (Boolean) abierto;
                return "true".equalsIgnoreCase(abierto.toString())
                    || "1".equals(abierto.toString());
            }

            // Si responde 200 pero sin campo de estado, asumir que existe y está abierta
            return true;

        } catch (Exception e) {
            System.err.println("Error al verificar caja ID " + cajaId + ": " + e.getMessage());
            // Falla cerrada: si no podemos verificar, no permitimos el pago
            return false;
        }
    }

    /**
     * Notifica al ms-back-caja que se aprobó un pago y actualiza el monto.
     */
    public boolean agregarMontoCaja(Long cajaId, Double monto, Long pagoId) {
        try {
            String url = cajaBaseUrl + "/api/cajas/" + cajaId + "/agregar-monto";

            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("monto", monto);
            bodyMap.put("pagoId", pagoId);
            bodyMap.put("descripcion", "Pago aprobado - ID: " + pagoId);

            // FIX 2: enviar x-api-key
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(bodyMap, buildHeaders());
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.PUT, request, Map.class);

            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            System.err.println("Error al notificar monto a caja ID " + cajaId + ": " + e.getMessage());
            return false;
        }
    }
}
