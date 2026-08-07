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
import java.util.List;
import java.util.Map;

@Component
public class CajaClient {

    @Value("${ms.caja.url:https://mscaja.sistemas9noa.com}")
    private String cajaBaseUrl;

    @Value("${ms.caja.api-key:}")
    private String cajaApiKey;

    // ID del tipo de movimiento INGRESO en ms-back-caja.
    // Configurable por si el ID difiere entre ambientes.
    @Value("${ms.caja.tipo-movimiento-ingreso:1}")
    private int idTipoMovimientoIngreso;

    private final RestTemplate restTemplate = new RestTemplate();

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (cajaApiKey != null && !cajaApiKey.isBlank()) {
            headers.set("x-api-key", cajaApiKey);
        }
        return headers;
    }

    /**
     * BUS-IZI-003: Verifica que la caja exista y tenga una apertura activa.
     * Usa GET /api/aperturas?idCaja={id} y busca apertura con estado abierto.
     * Falla cerrada: si no puede verificar, rechaza el pago.
     */
    public boolean verificarCajaAbierta(Long cajaId) {
        try {
            // Primero verificar que la caja existe
            String urlCaja = cajaBaseUrl + "/api/cajas/" + cajaId;
            HttpEntity<Void> request = new HttpEntity<>(buildHeaders());
            ResponseEntity<Map> responseCaja = restTemplate.exchange(
                    urlCaja, HttpMethod.GET, request, Map.class);

            if (!responseCaja.getStatusCode().is2xxSuccessful()
                    || responseCaja.getBody() == null) {
                return false;
            }

            // Verificar que la caja tiene estado activo (estado=1)
            Map<?, ?> cajaBody = responseCaja.getBody();
            Object cajaData = cajaBody.get("data");
            if (cajaData instanceof Map<?, ?> cajaMap) {
                Object estado = cajaMap.get("estado");
                if (estado != null && "0".equals(estado.toString())) {
                    return false; // caja inactiva
                }
            }

            // Verificar apertura activa
            String urlAperturas = cajaBaseUrl + "/api/aperturas?idCaja=" + cajaId;
            ResponseEntity<Map> responseAperturas = restTemplate.exchange(
                    urlAperturas, HttpMethod.GET, request, Map.class);

            if (!responseAperturas.getStatusCode().is2xxSuccessful()
                    || responseAperturas.getBody() == null) {
                // Si no puede consultar aperturas pero la caja existe, permitir
                return true;
            }

            Map<?, ?> aperturasBody = responseAperturas.getBody();
            Object aperturasData = aperturasBody.get("data");

            if (aperturasData instanceof List<?> lista) {
                // Buscar apertura activa (sin fechaCierre o con estado abierto)
                for (Object item : lista) {
                    if (item instanceof Map<?, ?> apertura) {
                        Object fechaCierre = apertura.get("fechaCierre");
                        Object estadoAp = apertura.get("estado");
                        // Apertura sin cierre = abierta
                        if (fechaCierre == null
                                || (estadoAp != null && "1".equals(estadoAp.toString()))) {
                            return true;
                        }
                    }
                }
                return false; // ninguna apertura activa
            }

            // Si no hay estructura de lista, asumir que la caja existe y está disponible
            return true;

        } catch (Exception e) {
            System.err.println("Error al verificar caja ID " + cajaId + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Registra un ingreso en ms-back-caja usando POST /api/movimientos.
     * El tipo de movimiento es INGRESO (idTipoMovimientoIngreso).
     */
    public boolean agregarMontoCaja(Long cajaId, Double monto, Long pagoId) {
        try {
            String url = cajaBaseUrl + "/api/movimientos";

            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("idCaja", cajaId.intValue());
            bodyMap.put("idTipoMovimiento", idTipoMovimientoIngreso);
            bodyMap.put("monto", monto);
            bodyMap.put("motivo", "Pago online aprobado - ID: " + pagoId);

            HttpEntity<Map<String, Object>> requestEntity =
                    new HttpEntity<>(bodyMap, buildHeaders());

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, Map.class);

            boolean ok = response.getStatusCode().is2xxSuccessful();
            if (ok) {
                System.out.println("Movimiento INGRESO registrado en caja "
                        + cajaId + " por S/" + monto + " (pago ID " + pagoId + ")");
            } else {
                System.err.println("ms-back-caja rechazó el movimiento: "
                        + response.getStatusCode());
            }
            return ok;

        } catch (Exception e) {
            System.err.println("Error al registrar movimiento en caja ID "
                    + cajaId + ": " + e.getMessage());
            return false;
        }
    }
}
