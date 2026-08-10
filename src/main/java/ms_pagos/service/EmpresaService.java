package ms_pagos.service;

import ms_pagos.dto.EmpresaDTO;
import ms_pagos.entity.Empresa;
import ms_pagos.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class EmpresaService {

    @Autowired
    private EmpresaRepository empresaRepository;

    public Empresa registrarEmpresa(EmpresaDTO dto) {
        if (empresaRepository.existsByRuc(dto.getRuc())) {
            throw new IllegalStateException("Ya existe una empresa registrada con el RUC: " + dto.getRuc());
        }
        if (empresaRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalStateException("Ya existe una empresa registrada con el correo: " + dto.getEmail());
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

    // PERF-IZI-001: listado paginado
    public Page<Empresa> listarEmpresas(Pageable pageable) {
        return empresaRepository.findAll(pageable);
    }

    // PERF-IZI-001: listado paginado de activas
    public Page<Empresa> listarEmpresasActivas(Pageable pageable) {
        return empresaRepository.findByActivoTrue(pageable);
    }

    public Optional<Empresa> obtenerPorId(Long id) {
        return empresaRepository.findById(id);
    }

    public Optional<Empresa> obtenerPorRuc(String ruc) {
        return empresaRepository.findByRuc(ruc);
    }

    public Empresa actualizarEmpresa(Long id, EmpresaDTO dto) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + id));

        empresaRepository.findByRuc(dto.getRuc()).ifPresent(e -> {
            if (!e.getId().equals(id)) {
                throw new IllegalStateException("Ya existe otra empresa con el RUC: " + dto.getRuc());
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

    public Empresa desactivarEmpresa(Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + id));
        empresa.setActivo(false);
        return empresaRepository.save(empresa);
    }

    /**
     * DELETE /api/empresas/{id}: elimina (borrado lógico) una empresa marcándola
     * como inactiva para no violar las llaves foráneas de configuraciones.
     */
    public Empresa eliminarEmpresa(Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + id));
        empresa.setActivo(false);
        return empresaRepository.save(empresa);
    }
}
