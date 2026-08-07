package ms_pagos.service;

import ms_pagos.dto.ConfiguracionIzipayDTO;
import ms_pagos.dto.TokenResponseDTO;
import ms_pagos.entity.ConfiguracionIzipay;
import ms_pagos.entity.Empresa;
import ms_pagos.repository.ConfiguracionIzipayRepository;
import ms_pagos.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class ConfiguracionIzipayService {

    @Autowired
    private ConfiguracionIzipayRepository configuracionRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    /** BUS-IZI-004: garantiza una sola config activa por empresa. */
    private void desactivarConfiguracionesActivas(Long empresaId) {
        configuracionRepository.findByEmpresaId(empresaId).stream()
                .filter(c -> Boolean.TRUE.equals(c.getActivo()))
                .forEach(c -> {
                    c.setActivo(false);
                    configuracionRepository.save(c);
                });
    }

    @Transactional
    public ConfiguracionIzipay registrarConfiguracion(ConfiguracionIzipayDTO dto) {
        ConfiguracionIzipay config = new ConfiguracionIzipay();
        config.setMerchantCode(dto.getMerchantCode());
        config.setPasswordIzipay(dto.getPasswordIzipay());
        config.setPublicKey(dto.getPublicKey());
        config.setHmacSha256(dto.getHmacSha256());
        config.setUrlPago(dto.getUrlPago() != null ? dto.getUrlPago()
                : "https://api.micuentaweb.pe/api-payment/V4/Charge/CreatePayment");
        config.setUrlToken(dto.getUrlToken() != null ? dto.getUrlToken()
                : "https://api.micuentaweb.pe/api-payment/V4/Charge/CreateToken");
        config.setMoneda(dto.getMoneda() != null ? dto.getMoneda() : "PEN");

        boolean seraActiva = dto.getActivo() == null || dto.getActivo();
        config.setActivo(seraActiva);
        config.setFechaRegistro(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        if (dto.getEmpresaId() != null) {
            Empresa empresa = empresaRepository.findById(dto.getEmpresaId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Empresa no encontrada con ID: " + dto.getEmpresaId()));
            config.setEmpresa(empresa);
            if (seraActiva) {
                desactivarConfiguracionesActivas(dto.getEmpresaId());
            }
        }

        return configuracionRepository.save(config);
    }

    /** SEC-IZI-004/005 + BUS-IZI-001: verifica activa, NO expone credenciales. */
    public TokenResponseDTO generarToken(Long id) {
        ConfiguracionIzipay config = configuracionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Configuración IZIPAY no encontrada con ID: " + id));

        if (!Boolean.TRUE.equals(config.getActivo())) {
            throw new IllegalStateException(
                    "La configuración ID " + id + " está inactiva y no puede usarse para pagos.");
        }

        TokenResponseDTO response = new TokenResponseDTO();
        response.setMerchantCode(config.getMerchantCode());
        response.setUrlPago(config.getUrlPago());
        response.setMensaje("Configuración activa verificada correctamente");
        response.setEstado("SUCCESS");
        return response;
    }

    public TokenResponseDTO generarTokenActivo() {
        ConfiguracionIzipay config = configuracionRepository.findFirstByActivoTrue()
                .orElseThrow(() -> new RuntimeException("No existe configuración IZIPAY activa"));

        TokenResponseDTO response = new TokenResponseDTO();
        response.setMerchantCode(config.getMerchantCode());
        response.setUrlPago(config.getUrlPago());
        response.setMensaje("Configuración activa verificada correctamente");
        response.setEstado("SUCCESS");
        return response;
    }

    // PERF-IZI-001: listado paginado
    public Page<ConfiguracionIzipay> listarConfiguraciones(Pageable pageable) {
        return configuracionRepository.findAll(pageable);
    }

    public Optional<ConfiguracionIzipay> obtenerPorId(Long id) {
        return configuracionRepository.findById(id);
    }

    public List<ConfiguracionIzipay> obtenerPorEmpresa(Long empresaId) {
        return configuracionRepository.findByEmpresaId(empresaId);
    }

    @Transactional
    public ConfiguracionIzipay actualizarConfiguracion(Long id, ConfiguracionIzipayDTO dto) {
        ConfiguracionIzipay config = configuracionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Configuración no encontrada con ID: " + id));

        config.setMerchantCode(dto.getMerchantCode());
        config.setPasswordIzipay(dto.getPasswordIzipay());
        config.setPublicKey(dto.getPublicKey());
        config.setHmacSha256(dto.getHmacSha256());
        if (dto.getUrlPago() != null) config.setUrlPago(dto.getUrlPago());
        if (dto.getUrlToken() != null) config.setUrlToken(dto.getUrlToken());
        if (dto.getMoneda() != null) config.setMoneda(dto.getMoneda());

        if (dto.getActivo() != null) {
            config.setActivo(dto.getActivo());
            if (dto.getActivo() && config.getEmpresa() != null) {
                desactivarConfiguracionesActivas(config.getEmpresa().getId());
                config.setActivo(true);
            }
        }

        if (dto.getEmpresaId() != null) {
            Empresa empresa = empresaRepository.findById(dto.getEmpresaId())
                    .orElseThrow(() -> new RuntimeException(
                            "Empresa no encontrada con ID: " + dto.getEmpresaId()));
            config.setEmpresa(empresa);
        }

        return configuracionRepository.save(config);
    }
}
