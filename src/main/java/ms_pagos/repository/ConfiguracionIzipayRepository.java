package ms_pagos.repository;

import ms_pagos.entity.ConfiguracionIzipay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfiguracionIzipayRepository extends JpaRepository<ConfiguracionIzipay, Long> {
    Optional<ConfiguracionIzipay> findFirstByActivoTrue();
}
