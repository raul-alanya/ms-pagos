package ms_pagos.repository;

<<<<<<< HEAD
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
=======
import ms_pagos.model.PagoResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PagoResponseRepository extends JpaRepository<PagoResponse, Long> {

}
>>>>>>> c47c6f3c7fd747f5cb3d9080e09f9db0fdad8b66
