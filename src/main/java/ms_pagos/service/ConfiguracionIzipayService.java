package ms_pagos.service;

import ms_pagos.dto.ConfiguracionIzipayDTO;
import ms_pagos.dto.TokenResponseDTO;
import ms_pagos.entity.ConfiguracionIzipay;
import ms_pagos.repository.ConfiguracionIzipayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class ConfiguracionIzipayService {

    @Autowired
    private ConfiguracionIzipayRepository configuracionRepository;

    // Registrar nueva configuración IZIPAY
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
        config.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        config.setFechaRegistro(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return configuracionRepository.save(config);
    }

    // Generar token de comunicación con IZIPAY (Basic Auth base64)
    public TokenResponseDTO generarToken(Long id) {
        ConfiguracionIzipay config = configuracionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Configuración IZIPAY no encontrada con ID: " + id));

        // Token de comunicación: Base64(merchantCode:password) — estándar Basic Auth IZIPAY
        String credenciales = config.getMerchantCode() + ":" + config.getPasswordIzipay();
        String token = Base64.getEncoder().encodeToString(credenciales.getBytes());

        TokenResponseDTO response = new TokenResponseDTO();
        response.setToken(token);
        response.setMerchantCode(config.getMerchantCode());
        response.setUrlPago(config.getUrlPago());
        response.setMensaje("Token de comunicación generado exitosamente");
        response.setEstado("SUCCESS");

        return response;
    }

    // Generar token usando la configuración activa
    public TokenResponseDTO generarTokenActivo() {
        ConfiguracionIzipay config = configuracionRepository.findFirstByActivoTrue()
                .orElseThrow(() -> new RuntimeException("No existe configuración IZIPAY activa"));

        String credenciales = config.getMerchantCode() + ":" + config.getPasswordIzipay();
        String token = Base64.getEncoder().encodeToString(credenciales.getBytes());

        TokenResponseDTO response = new TokenResponseDTO();
        response.setToken(token);
        response.setMerchantCode(config.getMerchantCode());
        response.setUrlPago(config.getUrlPago());
        response.setMensaje("Token de comunicación generado exitosamente");
        response.setEstado("SUCCESS");

        return response;
    }

    // Obtener todas las configuraciones
    public List<ConfiguracionIzipay> listarConfiguraciones() {
        return configuracionRepository.findAll();
    }

    // Obtener configuración por ID
    public Optional<ConfiguracionIzipay> obtenerPorId(Long id) {
        return configuracionRepository.findById(id);
    }

    // Actualizar configuración
    public ConfiguracionIzipay actualizarConfiguracion(Long id, ConfiguracionIzipayDTO dto) {
        ConfiguracionIzipay config = configuracionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Configuración no encontrada con ID: " + id));

        config.setMerchantCode(dto.getMerchantCode());
        config.setPasswordIzipay(dto.getPasswordIzipay());
        config.setPublicKey(dto.getPublicKey());
        config.setHmacSha256(dto.getHmacSha256());
        if (dto.getUrlPago() != null) config.setUrlPago(dto.getUrlPago());
        if (dto.getUrlToken() != null) config.setUrlToken(dto.getUrlToken());
        if (dto.getMoneda() != null) config.setMoneda(dto.getMoneda());
        if (dto.getActivo() != null) config.setActivo(dto.getActivo());

        return configuracionRepository.save(config);
    }
}
