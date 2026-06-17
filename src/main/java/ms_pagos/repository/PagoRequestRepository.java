package ms_pagos.repository;

import ms_pagos.entity.PagoRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoRequestRepository extends JpaRepository<PagoRequest, Long> {
    List<PagoRequest> findByReference(String reference);
}
