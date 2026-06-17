package ms_pagos.repository;

<<<<<<< HEAD
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
=======
import ms_pagos.model.PagoRechazo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PagoRechazoRepository extends JpaRepository<PagoRechazo, Long> {

}
>>>>>>> c47c6f3c7fd747f5cb3d9080e09f9db0fdad8b66
