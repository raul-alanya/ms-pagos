package ms_pagos.repository;

import ms_pagos.entity.PagoResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagoResponseRepository extends JpaRepository<PagoResponse, Long> {
    Optional<PagoResponse> findByPagoRequestId(Long pagoRequestId);
    List<PagoResponse> findByEstado(String estado);
}
