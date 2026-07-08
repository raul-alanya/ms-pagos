package ms_pagos.service;

import ms_pagos.dto.EmpresaDTO;
import ms_pagos.entity.Empresa;
import ms_pagos.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class EmpresaService {

    @Autowired
    private EmpresaRepository empresaRepository;

    // Registrar nueva empresa
    public Empresa registrarEmpresa(EmpresaDTO dto) {
        // Verificar que no exista ya el RUC
        if (empresaRepository.existsByRuc(dto.getRuc())) {
            throw new RuntimeException("Ya existe una empresa registrada con el RUC: " + dto.getRuc());
        }
        // Verificar que no exista ya el email
        if (empresaRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Ya existe una empresa registrada con el correo: " + dto.getEmail());
        }

        Empresa empresa = new Empresa();
        empresa.setNombre(dto.getNombre());
        empresa.setRuc(dto.getRuc());
        empresa.setDireccion(dto.getDireccion());
        empresa.setTelefono(dto.getTelefono());
        empresa.setEmail(dto.getEmail());
        empresa.setRepresentanteLegal(dto.getRepresentanteLegal());
        empresa.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        empresa.setFechaRegistro(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        return empresaRepository.save(empresa);
    }

    // Listar todas las empresas
    public List<Empresa> listarEmpresas() {
        return empresaRepository.findAll();
    }

    // Listar empresas activas
    public List<Empresa> listarEmpresasActivas() {
        return empresaRepository.findByActivoTrue();
    }

    // Obtener empresa por ID
    public Optional<Empresa> obtenerPorId(Long id) {
        return empresaRepository.findById(id);
    }

    // Obtener empresa por RUC
    public Optional<Empresa> obtenerPorRuc(String ruc) {
        return empresaRepository.findByRuc(ruc);
    }

    // Actualizar empresa
    public Empresa actualizarEmpresa(Long id, EmpresaDTO dto) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + id));

        // Verificar RUC duplicado en otra empresa
        empresaRepository.findByRuc(dto.getRuc())
                .ifPresent(e -> {
                    if (!e.getId().equals(id)) {
                        throw new RuntimeException("Ya existe otra empresa con el RUC: " + dto.getRuc());
                    }
                });

        empresa.setNombre(dto.getNombre());
        empresa.setRuc(dto.getRuc());
        empresa.setDireccion(dto.getDireccion());
        empresa.setTelefono(dto.getTelefono());
        empresa.setEmail(dto.getEmail());
        empresa.setRepresentanteLegal(dto.getRepresentanteLegal());
        if (dto.getActivo() != null) empresa.setActivo(dto.getActivo());

        return empresaRepository.save(empresa);
    }

    // Desactivar empresa
    public Empresa desactivarEmpresa(Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + id));
        empresa.setActivo(false);
        return empresaRepository.save(empresa);
    }
}
