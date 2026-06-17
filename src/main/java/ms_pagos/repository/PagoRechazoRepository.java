package ms_pagos.repository;

import ms_pagos.entity.PagoRechazo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRechazoRepository extends JpaRepository<PagoRechazo, Long> {
    Optional<PagoRechazo> findByPagoRequestId(Long pagoRequestId);
    List<PagoRechazo> findByCodigoRechazo(String codigoRechazo);
}
