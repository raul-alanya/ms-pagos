package ms_pagos.repository;

import ms_pagos.model.PagoRechazo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PagoRechazoRepository extends JpaRepository<PagoRechazo, Long> {

}