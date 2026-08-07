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

    @Value("${ms.caja.url:http://localhost:8081}")
    private String cajaBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * BUS-IZI-003: Verifica que la caja exista y esté abierta antes de registrar un pago.
     * Falla cerrada: si el microservicio no responde, se rechaza el pago.
     */
    public boolean verificarCajaAbierta(Long cajaId) {
        try {
            String url = cajaBaseUrl + "/api/cajas/" + cajaId;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return false;
            }

            Map<?, ?> body = response.getBody();
            Object data = body.get("data");
            if (data instanceof Map<?, ?> dataMap) {
                Object estado = dataMap.get("estado");
                if (estado != null) {
                    String estadoStr = estado.toString();
                    return "ABIERTA".equalsIgnoreCase(estadoStr)
                        || "ABIERTO".equalsIgnoreCase(estadoStr)
                        || "OPEN".equalsIgnoreCase(estadoStr);
                }
                Object abierto = dataMap.get("abierto");
                if (abierto instanceof Boolean) {
                    return (Boolean) abierto;
                }
            }
            // Si responde 200 sin campo de estado conocido, se asume que la caja existe
            return true;

        } catch (Exception e) {
            System.err.println("Error al verificar caja ID " + cajaId + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Notifica al microservicio de caja que se realizó un pago aprobado
     * y actualiza el monto de la caja aperturada.
     */
    public boolean agregarMontoCaja(Long cajaId, Double monto, Long pagoId) {
        try {
            String url = cajaBaseUrl + "/api/cajas/" + cajaId + "/agregar-monto";

            Map<String, Object> body = new HashMap<>();
            body.put("monto", monto);
            body.put("pagoId", pagoId);
            body.put("descripcion", "Pago aprobado - ID: " + pagoId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.PUT, request, Map.class);

            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            System.err.println("Error al notificar a microservicio de caja: " + e.getMessage());
            return false;
        }
    }
}
