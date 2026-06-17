package ms_pagos.service;

import ms_pagos.dto.PagoRechazoDTO;
import ms_pagos.dto.PagoRequestDTO;
import ms_pagos.dto.PagoResponseDTO;
import ms_pagos.entity.PagoRechazo;
import ms_pagos.entity.PagoRequest;
import ms_pagos.entity.PagoResponse;
import ms_pagos.repository.PagoRechazoRepository;
import ms_pagos.repository.PagoRequestRepository;
import ms_pagos.repository.PagoResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PagoService {

    @Autowired
    private PagoRequestRepository pagoRequestRepository;

    @Autowired
    private PagoResponseRepository pagoResponseRepository;

    @Autowired
    private PagoRechazoRepository pagoRechazoRepository;

    // ─── PAGO REQUEST ────────────────────────────────────────────────────────

    // Registrar datos de envío de pago online
    public PagoRequest registrarPago(PagoRequestDTO dto) {
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
        return pagoRequestRepository.save(pago);
    }

    // Listar todos los pagos
    public List<PagoRequest> listarPagos() {
        return pagoRequestRepository.findAll();
    }

    // Obtener pago por ID
    public Optional<PagoRequest> obtenerPagoPorId(Long id) {
        return pagoRequestRepository.findById(id);
    }

    // Buscar por referencia
    public List<PagoRequest> buscarPorReferencia(String reference) {
        return pagoRequestRepository.findByReference(reference);
    }

    // ─── PAGO RESPONSE ───────────────────────────────────────────────────────

    // Registrar datos de respuesta de pago (código de operación)
    public PagoResponse registrarRespuesta(PagoResponseDTO dto) {
        // Verifica que exista el pago original
        pagoRequestRepository.findById(dto.getPagoRequestId())
                .orElseThrow(() -> new RuntimeException(
                        "Pago no encontrado con ID: " + dto.getPagoRequestId()));

        PagoResponse response = new PagoResponse();
        response.setCodigoRespuesta(dto.getCodigoRespuesta());
        response.setCodigoTransaccion(dto.getCodigoTransaccion());
        response.setEstado(dto.getEstado() != null ? dto.getEstado() : "APROBADO");
        response.setFechaRespuesta(dto.getFechaRespuesta() != null
                ? dto.getFechaRespuesta()
                : LocalDate.now().toString());
        response.setMensaje(dto.getMensaje());
        response.setPagoRequestId(dto.getPagoRequestId());

        return pagoResponseRepository.save(response);
    }

    // Listar todas las respuestas
    public List<PagoResponse> listarRespuestas() {
        return pagoResponseRepository.findAll();
    }

    // Obtener respuesta por ID del pago original
    public Optional<PagoResponse> obtenerRespuestaPorPagoId(Long pagoRequestId) {
        return pagoResponseRepository.findByPagoRequestId(pagoRequestId);
    }

    // ─── PAGO RECHAZO ────────────────────────────────────────────────────────

    // Registrar rechazo de pago online
    public PagoRechazo registrarRechazo(PagoRechazoDTO dto) {
        // Verifica que exista el pago original
        pagoRequestRepository.findById(dto.getPagoRequestId())
                .orElseThrow(() -> new RuntimeException(
                        "Pago no encontrado con ID: " + dto.getPagoRequestId()));

        PagoRechazo rechazo = new PagoRechazo();
        rechazo.setCodigoRechazo(dto.getCodigoRechazo());
        rechazo.setMotivoRechazo(dto.getMotivoRechazo());
        rechazo.setFechaRechazo(dto.getFechaRechazo() != null
                ? dto.getFechaRechazo()
                : LocalDate.now().toString());
        rechazo.setEstado(dto.getEstado() != null ? dto.getEstado() : "RECHAZADO");
        rechazo.setPagoRequestId(dto.getPagoRequestId());

        return pagoRechazoRepository.save(rechazo);
    }

    // Listar todos los rechazos
    public List<PagoRechazo> listarRechazos() {
        return pagoRechazoRepository.findAll();
    }

    // Obtener rechazo por ID del pago original
    public Optional<PagoRechazo> obtenerRechazoPorPagoId(Long pagoRequestId) {
        return pagoRechazoRepository.findByPagoRequestId(pagoRequestId);
    }
}
