package ms_pagos.repository;

<<<<<<< HEAD
import ms_pagos.entity.PagoRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoRequestRepository extends JpaRepository<PagoRequest, Long> {
    List<PagoRequest> findByReference(String reference);
}
=======
import ms_pagos.model.PagoRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PagoRequestRepository extends JpaRepository<PagoRequest, Long> {

}
>>>>>>> c47c6f3c7fd747f5cb3d9080e09f9db0fdad8b66
