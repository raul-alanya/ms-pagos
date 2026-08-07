package ms_pagos.repository;

import ms_pagos.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    Optional<Empresa> findByRuc(String ruc);
    Optional<Empresa> findByEmail(String email);
    List<Empresa> findByActivoTrue();
    // PERF-IZI-001: versión paginada
    org.springframework.data.domain.Page<Empresa> findByActivoTrue(org.springframework.data.domain.Pageable pageable);
    boolean existsByRuc(String ruc);
    boolean existsByEmail(String email);
}
