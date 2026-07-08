package ms_pagos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

@Component
public class CajaClient {

    // URL del microservicio de caja — cambiar cuando el compañero dé la URL real
    @Value("${ms.caja.url:http://localhost:8081}")
    private String cajaBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Notifica al microservicio de caja que se realizó un pago aprobado
     * y actualiza el monto de la caja aperturada
     *
     * @param cajaId ID de la caja aperturada
     * @param monto  Monto del pago aprobado a sumar
     * @param pagoId ID del pago procesado
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
            // Si el microservicio de caja no está disponible, se registra el error
            // pero no se interrumpe el flujo del pago
            System.err.println("Error al notificar a microservicio de caja: " + e.getMessage());
            return false;
        }
    }
}
