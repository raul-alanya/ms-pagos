package ms_pagos.service;

import ms_pagos.config.CajaClient;
import ms_pagos.dto.PagoRechazoDTO;
import ms_pagos.dto.PagoRequestDTO;
import ms_pagos.dto.PagoResponseDTO;
import ms_pagos.dto.PagoResponseResultDTO;
import ms_pagos.dto.PagoSafeDTO;
import ms_pagos.entity.PagoRechazo;
import ms_pagos.entity.PagoRequest;
import ms_pagos.entity.PagoResponse;
import ms_pagos.repository.PagoRechazoRepository;
import ms_pagos.repository.PagoRequestRepository;
import ms_pagos.repository.PagoResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
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
        validarLuhn(dto.getCardnumber());
        validarFechaVigencia(dto.getCardexpiry());

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

        PagoRequest pago = new PagoRequest();
        pago.setCardnumber(dto.getCardnumber());
        pago.setCardholdername(dto.getCardholdername());
        pago.setCvv(dto.getCvv());
        pago.setCardexpiry(dto.getCardexpiry());
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

        return PagoSafeDTO.from(pagoRequestRepository.save(pago));
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

        if ("APROBADO".equals(saved.getEstado()) && pagoOriginal.getCajaId() != null) {
            cajaClient.agregarMontoCaja(pagoOriginal.getCajaId(),
                    pagoOriginal.getTotalamount(), pagoOriginal.getId());
            pagoOriginal.setEstado("APROBADO");
            pagoRequestRepository.save(pagoOriginal);
        } else if ("RECHAZADO".equals(saved.getEstado())) {
            pagoOriginal.setEstado("RECHAZADO");
            pagoRequestRepository.save(pagoOriginal);
        }

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
