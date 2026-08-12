package ms_pagos.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ms_pagos.config.CajaClient;
import ms_pagos.config.IzipayClient;
import ms_pagos.dto.PagoAdminDTO;
import ms_pagos.dto.PagoRechazoDTO;
import ms_pagos.dto.PagoRequestDTO;
import ms_pagos.dto.PagoResponseDTO;
import ms_pagos.dto.PagoResponseResultDTO;
import ms_pagos.dto.PagoSafeDTO;
import ms_pagos.entity.ConfiguracionIzipay;
import ms_pagos.entity.PagoRechazo;
import ms_pagos.entity.PagoRequest;
import ms_pagos.entity.PagoResponse;
import ms_pagos.repository.ConfiguracionIzipayRepository;
import ms_pagos.repository.PagoRechazoRepository;
import ms_pagos.repository.PagoRequestRepository;
import ms_pagos.repository.PagoResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PagoService {

    @Autowired
    private PagoRequestRepository pagoRequestRepository;

    @Autowired
    private PagoResponseRepository pagoResponseRepository;

    @Autowired
    private PagoRechazoRepository pagoRechazoRepository;

    @Autowired
    private CajaClient cajaClient;

    @Autowired
    private IzipayClient izipayClient;

    @Autowired
    private ConfiguracionIzipayRepository configuracionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // URL del JS del formulario de pago hospedado de IZIPAY
    @Value("${izipay.js-url:https://static.micuentaweb.pe/static/js/krypton-client/V4.0/stable/kr-payment-form.min.js}")
    private String izipayJsUrl;

    // URL de notificación IPN a la que IZIPAY reportará el resultado del pago
    @Value("${izipay.ipn-url:https://pagos.sistemas9noa.com/api/pagos/ipn}")
    private String izipayIpnUrl;

    // URL de retorno del cliente tras finalizar el pago en IZIPAY
    @Value("${izipay.return-url:https://pagos.sistemas9noa.com}")
    private String izipayReturnUrl;

    // ─── VALIDACIONES PRIVADAS ───────────────────────────────────────────────

    /** VAL-IZI-001: Algoritmo de Luhn. */
    private void validarLuhn(String pan) {
        int suma = 0;
        boolean duplicar = false;
        for (int i = pan.length() - 1; i >= 0; i--) {
            int digito = pan.charAt(i) - '0';
            if (duplicar) {
                digito *= 2;
                if (digito > 9) digito -= 9;
            }
            suma += digito;
            duplicar = !duplicar;
        }
        if (suma % 10 != 0) {
            throw new IllegalArgumentException(
                    "El número de tarjeta no es válido (falla verificación Luhn)");
        }
    }

    /** VAL-IZI-002: Rechaza tarjetas vencidas. */
    private void validarFechaVigencia(String cardexpiry) {
        try {
            YearMonth expiracion = YearMonth.parse(cardexpiry,
                    DateTimeFormatter.ofPattern("MM/yy"));
            if (expiracion.isBefore(YearMonth.now())) {
                throw new IllegalArgumentException(
                        "La tarjeta está vencida. Fecha de expiración: " + cardexpiry);
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Formato de fecha de expiración inválido. Use MM/YY");
        }
    }

    // ─── PAGO REQUEST ────────────────────────────────────────────────────────

    public PagoSafeDTO registrarPago(PagoRequestDTO dto) {
        // INT-IZI-003: con el formulario hospedado de IZIPAY la tarjeta no llega.
        // Si algún cliente antiguo aún la envía, se valida como antes.
        boolean tarjetaPresente = dto.getCardnumber() != null && !dto.getCardnumber().isBlank();
        if (tarjetaPresente) {
            validarLuhn(dto.getCardnumber());
            if (dto.getCardexpiry() != null && !dto.getCardexpiry().isBlank()) {
                validarFechaVigencia(dto.getCardexpiry());
            }
        }

        if (!pagoRequestRepository.findByReference(dto.getReference()).isEmpty()) {
            throw new IllegalStateException(
                    "Ya existe un pago con la referencia '" + dto.getReference()
                    + "'. Use una referencia única por transacción.");
        }

        if (!cajaClient.verificarCajaAbierta(dto.getCajaId())) {
            throw new IllegalArgumentException(
                    "La caja con ID " + dto.getCajaId()
                    + " no existe, está cerrada o no está disponible.");
        }

        ConfiguracionIzipay config = configuracionRepository.findFirstByActivoTrue()
                .orElseThrow(() -> new IllegalStateException(
                        "No existe una configuración IZIPAY activa. Contacte al administrador."));

        PagoRequest pago = new PagoRequest();
        // INT-IZI-003: solo se persiste el PAN/CVV si el cliente antiguo lo envió.
        // Con el formulario hospedado de IZIPAY no se almacena información de tarjeta.
        if (tarjetaPresente) {
            pago.setCardnumber(dto.getCardnumber());
            pago.setCvv(dto.getCvv());
            pago.setCardexpiry(dto.getCardexpiry());
        }
        pago.setCardholdername(dto.getCardholdername());
        pago.setTotalamount(dto.getTotalamount());
        pago.setReference(dto.getReference());
        pago.setEmail(dto.getEmail());
        pago.setPhone(dto.getPhone());
        pago.setClientip(dto.getClientip());
        pago.setClientcountry(dto.getClientcountry());
        pago.setClienturl(dto.getClienturl());
        pago.setTransactiontype(dto.getTransactiontype() != null ? dto.getTransactiontype() : "Sale");
        pago.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "PEN");
        pago.setCajaId(dto.getCajaId());
        pago.setEstado("PENDIENTE");

        // INT-IZI-001: llama a IZIPAY CreatePayment para obtener el formToken
        // Se obtiene el token ANTES de persistir para no dejar pagos PENDIENTE
        // huérfanos cuando IZIPAY rechaza o falla la conexión.
        String formToken = izipayClient.crearPago(config, pago.getReference(),
                pago.getTotalamount(), pago.getCurrency(), pago.getEmail(),
                pago.getCardholdername(), izipayReturnUrl, izipayReturnUrl, izipayIpnUrl);

        PagoRequest saved = pagoRequestRepository.save(pago);

        PagoSafeDTO safe = PagoSafeDTO.from(saved);
        safe.setFormToken(formToken);
        safe.setPublicKey(config.getPublicKey());
        safe.setUrlJs(izipayJsUrl);
        return safe;
    }

    // PERF-IZI-001: listado paginado — devuelve Page<PagoSafeDTO>
    public Page<PagoSafeDTO> listarPagos(Pageable pageable) {
        Page<PagoRequest> page = pagoRequestRepository.findAll(pageable);
        List<PagoSafeDTO> contenido = page.getContent().stream()
                .map(PagoSafeDTO::from)
                .collect(Collectors.toList());
        return new PageImpl<>(contenido, pageable, page.getTotalElements());
    }

    public Optional<PagoRequest> obtenerPagoPorId(Long id) {
        return pagoRequestRepository.findById(id);
    }

    public List<PagoRequest> buscarPorReferencia(String reference) {
        return pagoRequestRepository.findByReference(reference);
    }

    // ─── CONSULTA ADMIN: listado, filtros y sumatorias ───────────────────────

    /**
     * Lista todos los pagos (aprobados, rechazados y pendientes) para el panel
     * administrador, aplicando los filtros opcionales recibidos.
     */
    public List<PagoAdminDTO> listarPagosAdmin(String estado, String referencia, Long cajaId,
                                               LocalDateTime desde, LocalDateTime hasta) {
        return pagoRequestRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .filter(p -> matchesAdmin(p, estado, referencia, cajaId, desde, hasta))
                .map(PagoAdminDTO::from)
                .collect(Collectors.toList());
    }

    /**
     * Calcula la sumatoria de montos por estado y los totales del listado filtrado.
     */
    public Map<String, Object> resumenPagos(List<PagoAdminDTO> pagos) {
        double total = 0;
        double totalAprobado = 0;
        double totalRechazado = 0;
        double totalPendiente = 0;
        int cantidadAprobado = 0;
        int cantidadRechazado = 0;
        int cantidadPendiente = 0;

        for (PagoAdminDTO pago : pagos) {
            double monto = pago.getTotalamount() != null ? pago.getTotalamount() : 0;
            total += monto;
            String estado = pago.getEstado() == null ? "PENDIENTE" : pago.getEstado().toUpperCase(Locale.ROOT);
            switch (estado) {
                case "APROBADO" -> {
                    totalAprobado += monto;
                    cantidadAprobado++;
                }
                case "RECHAZADO" -> {
                    totalRechazado += monto;
                    cantidadRechazado++;
                }
                default -> {
                    totalPendiente += monto;
                    cantidadPendiente++;
                }
            }
        }

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("total", round(total));
        resumen.put("totalAprobado", round(totalAprobado));
        resumen.put("totalRechazado", round(totalRechazado));
        resumen.put("totalPendiente", round(totalPendiente));
        resumen.put("cantidad", pagos.size());
        resumen.put("cantidadAprobado", cantidadAprobado);
        resumen.put("cantidadRechazado", cantidadRechazado);
        resumen.put("cantidadPendiente", cantidadPendiente);
        return resumen;
    }

    public Optional<PagoAdminDTO> obtenerPagoAdminPorId(Long id) {
        return pagoRequestRepository.findById(id).map(PagoAdminDTO::from);
    }

    public List<PagoAdminDTO> buscarPorReferenciaAdmin(String reference) {
        return pagoRequestRepository.findByReference(reference).stream()
                .map(PagoAdminDTO::from)
                .collect(Collectors.toList());
    }

    private boolean matchesAdmin(PagoRequest pago, String estado, String referencia,
                                 Long cajaId, LocalDateTime desde, LocalDateTime hasta) {
        if (estado != null && !estado.isBlank()
                && !estado.trim().equalsIgnoreCase(pago.getEstado())) {
            return false;
        }
        if (referencia != null && !referencia.isBlank()
                && (pago.getReference() == null || !pago.getReference().toLowerCase(Locale.ROOT)
                        .contains(referencia.trim().toLowerCase(Locale.ROOT)))) {
            return false;
        }
        if (cajaId != null && !cajaId.equals(pago.getCajaId())) {
            return false;
        }
        if (desde != null || hasta != null) {
            LocalDateTime creado = pago.getCreatedAt();
            if (creado == null) {
                return false;
            }
            if (desde != null && creado.isBefore(desde)) {
                return false;
            }
            if (hasta != null && creado.isAfter(hasta)) {
                return false;
            }
        }
        return true;
    }

    private double round(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    // ─── PAGO RESPONSE ───────────────────────────────────────────────────────

    private String generarCodigoTransaccion() {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String secuencia = String.format("%04d", pagoResponseRepository.count() + 1);
        return "TXN-" + fecha + "-" + secuencia;
    }

    public PagoResponseResultDTO registrarRespuesta(PagoResponseDTO dto) {
        PagoRequest pagoOriginal = pagoRequestRepository.findById(dto.getPagoRequestId())
                .orElseThrow(() -> new RuntimeException(
                        "Pago no encontrado con ID: " + dto.getPagoRequestId()));

        PagoResponse response = new PagoResponse();
        response.setCodigoRespuesta(dto.getCodigoRespuesta());
        response.setCodigoTransaccion(
                (dto.getCodigoTransaccion() != null && !dto.getCodigoTransaccion().isEmpty())
                        ? dto.getCodigoTransaccion()
                        : generarCodigoTransaccion());
        response.setEstado(dto.getEstado() != null ? dto.getEstado() : "APROBADO");
        response.setFechaRespuesta(dto.getFechaRespuesta() != null
                ? dto.getFechaRespuesta() : LocalDate.now().toString());
        response.setMensaje(dto.getMensaje());
        response.setPagoRequestId(dto.getPagoRequestId());

        PagoResponse saved = pagoResponseRepository.save(response);

        // SEC-IZI-007: el endpoint /respuesta NO modifica el estado del pago ni
        // registra movimientos de caja. La única vía autorizada para aprobar y
        // sumar dinero a caja es el IPN (firma HMAC verificada por IZIPAY).

        PagoResponseResultDTO result = new PagoResponseResultDTO();
        result.setId(saved.getId());
        result.setCodigoTransaccion(saved.getCodigoTransaccion());
        result.setReferencia(pagoOriginal.getReference());
        result.setCodigoRespuesta(saved.getCodigoRespuesta());
        result.setEstado(saved.getEstado());
        result.setFechaRespuesta(saved.getFechaRespuesta());
        result.setMensaje(saved.getMensaje());
        return result;
    }

    /**
     * INT-IZI-002: Procesa la notificación IPN (webhook) enviada por IZIPAY
     * al final del pago. Verifica la firma HMAC-SHA256 y actualiza el estado
     * del pago original (busca por la referencia de la orden).
     *
     * @return true si el webhook fue válido y procesado
     */
    public boolean procesarIpn(String krAnswer, String krHash, String krHashKey) {
        ConfiguracionIzipay config = configuracionRepository.findFirstByActivoTrue().orElse(null);
        if (config == null) {
            System.err.println("IPN: no existe configuración IZIPAY activa");
            return false;
        }

        if (!izipayClient.validarFirmaIPN(krAnswer, krHash, krHashKey, config)) {
            System.err.println("IPN: firma HMAC inválida");
            return false;
        }

        try {
            JsonNode answer = objectMapper.readTree(krAnswer);
            String orderStatus = answer.path("orderStatus").asText("UNPAID");
            String orderId = answer.path("orderDetails").path("orderId").asText("");
            String transaccion = answer.path("orderDetails").path("transactionUuid").asText("");
            String codigoRespuesta = answer.path("errorCode").asText("00");

            List<PagoRequest> pagos = pagoRequestRepository.findByReference(orderId);
            if (pagos.isEmpty()) {
                System.err.println("IPN: no existe pago con referencia " + orderId);
                return false;
            }
            PagoRequest pagoOriginal = pagos.get(0);

            // IDEMP-IZI-001: si IZIPAY reenvía el IPN (retry) y la respuesta ya
            // se procesó, se omite para no duplicar el movimiento de caja ni el registro.
            if (pagoResponseRepository.findByPagoRequestId(pagoOriginal.getId()).isPresent()) {
                System.out.println("IPN: respuesta ya procesada para el pago "
                        + pagoOriginal.getId() + ". Se omite para evitar duplicados.");
                return true;
            }

            boolean aprobado = "PAID".equalsIgnoreCase(orderStatus)
                    || "AUTHORISED".equalsIgnoreCase(orderStatus);

            PagoResponse response = new PagoResponse();
            response.setCodigoRespuesta(aprobado ? "00" : codigoRespuesta);
            response.setCodigoTransaccion(
                    !transaccion.isEmpty() ? transaccion : generarCodigoTransaccion());
            response.setEstado(aprobado ? "APROBADO" : "RECHAZADO");
            response.setFechaRespuesta(LocalDate.now().toString());
            response.setMensaje(aprobado
                    ? "Pago aprobado por IZIPAY (IPN). Estado: " + orderStatus
                    : "Pago rechazado por IZIPAY (IPN). Estado: " + orderStatus);
            response.setPagoRequestId(pagoOriginal.getId());

            PagoResponse saved = pagoResponseRepository.save(response);

            if (aprobado && pagoOriginal.getCajaId() != null) {
                cajaClient.agregarMontoCaja(pagoOriginal.getCajaId(),
                        pagoOriginal.getTotalamount(), pagoOriginal.getId());
            }
            pagoOriginal.setEstado(saved.getEstado());
            pagoRequestRepository.save(pagoOriginal);

            System.out.println("IPN procesado: referencia=" + orderId
                    + " estado=" + saved.getEstado() + " txn=" + saved.getCodigoTransaccion());
            return true;

        } catch (Exception e) {
            System.err.println("IPN: error al procesar kr-answer: " + e.getMessage());
            return false;
        }
    }

    // PERF-IZI-001: listado paginado de respuestas
    public Page<PagoResponse> listarRespuestas(Pageable pageable) {
        return pagoResponseRepository.findAll(pageable);
    }

    public Optional<PagoResponseResultDTO> obtenerRespuestaPorPagoId(Long pagoRequestId) {
        return pagoResponseRepository.findByPagoRequestId(pagoRequestId).map(resp -> {
            PagoRequest pagoOriginal = pagoRequestRepository.findById(pagoRequestId).orElse(null);
            PagoResponseResultDTO result = new PagoResponseResultDTO();
            result.setId(resp.getId());
            result.setCodigoTransaccion(resp.getCodigoTransaccion());
            result.setReferencia(pagoOriginal != null ? pagoOriginal.getReference() : null);
            result.setCodigoRespuesta(resp.getCodigoRespuesta());
            result.setEstado(resp.getEstado());
            result.setFechaRespuesta(resp.getFechaRespuesta());
            result.setMensaje(resp.getMensaje());
            return result;
        });
    }

    // ─── PAGO RECHAZO ────────────────────────────────────────────────────────

    public PagoRechazo registrarRechazo(PagoRechazoDTO dto) {
        pagoRequestRepository.findById(dto.getPagoRequestId())
                .orElseThrow(() -> new RuntimeException(
                        "Pago no encontrado con ID: " + dto.getPagoRequestId()));

        PagoRechazo rechazo = new PagoRechazo();
        rechazo.setCodigoRechazo(dto.getCodigoRechazo());
        rechazo.setMotivoRechazo(dto.getMotivoRechazo());
        rechazo.setFechaRechazo(dto.getFechaRechazo() != null
                ? dto.getFechaRechazo() : LocalDate.now().toString());
        rechazo.setEstado(dto.getEstado() != null ? dto.getEstado() : "RECHAZADO");
        rechazo.setPagoRequestId(dto.getPagoRequestId());

        return pagoRechazoRepository.save(rechazo);
    }

    // PERF-IZI-001: listado paginado de rechazos
    public Page<PagoRechazo> listarRechazos(Pageable pageable) {
        return pagoRechazoRepository.findAll(pageable);
    }

    public Optional<PagoRechazo> obtenerRechazoPorPagoId(Long pagoRequestId) {
        return pagoRechazoRepository.findByPagoRequestId(pagoRequestId);
    }
}
