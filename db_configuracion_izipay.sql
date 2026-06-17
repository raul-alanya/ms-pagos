-- Ejecutar este script en MySQL Workbench sobre la base de datos db_pagos
USE db_pagos;

CREATE TABLE IF NOT EXISTS configuracion_izipay (
    id BIGINT NOT NULL AUTO_INCREMENT,
    merchant_code VARCHAR(100) NOT NULL,
    password_izipay VARCHAR(255) NOT NULL,
    public_key VARCHAR(500),
    hmac_sha256 VARCHAR(500),
    url_pago VARCHAR(500),
    url_token VARCHAR(500),
    moneda VARCHAR(10) DEFAULT 'PEN',
    activo TINYINT(1) DEFAULT 1,
    fecha_registro VARCHAR(255),
    PRIMARY KEY (id)
);
