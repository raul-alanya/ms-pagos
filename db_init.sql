-- =====================================================
-- SCRIPT SQL UNIFICADO - MICROSERVICIO MS-PAGOS
-- Base de datos: db_pagos
-- =====================================================

CREATE DATABASE IF NOT EXISTS db_pagos;
USE db_pagos;

-- Tabla de empresas
CREATE TABLE IF NOT EXISTS empresa (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(150) NOT NULL,
    ruc VARCHAR(11) NOT NULL UNIQUE,
    direccion VARCHAR(255),
    telefono VARCHAR(9),
    email VARCHAR(150) NOT NULL UNIQUE,
    representante_legal VARCHAR(150),
    activo TINYINT(1) DEFAULT 1,
    fecha_registro VARCHAR(255),
    PRIMARY KEY (id)
);

-- Tabla de configuracion IZIPAY (ligada a empresa)
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
    empresa_id BIGINT,
    PRIMARY KEY (id),
    FOREIGN KEY (empresa_id) REFERENCES empresa(id)
);

-- Tabla de datos de envio de pago
CREATE TABLE IF NOT EXISTS pago_request (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cardnumber VARCHAR(255),
    cardholdername VARCHAR(255),
    cvv VARCHAR(255),
    cardexpiry VARCHAR(255),
    totalamount DOUBLE,
    reference VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(255),
    clientip VARCHAR(255),
    clientcountry VARCHAR(255),
    clienturl VARCHAR(255),
    transactiontype VARCHAR(255),
    currency VARCHAR(255),
    caja_id BIGINT,
    estado VARCHAR(50) DEFAULT 'PENDIENTE',
    PRIMARY KEY (id)
);

-- Tabla de respuestas de pago
CREATE TABLE IF NOT EXISTS pago_response (
    id BIGINT NOT NULL AUTO_INCREMENT,
    codigo_respuesta VARCHAR(255),
    codigo_transaccion VARCHAR(255),
    estado VARCHAR(255),
    fecha_respuesta VARCHAR(255),
    mensaje VARCHAR(255),
    pago_request_id BIGINT,
    PRIMARY KEY (id),
    FOREIGN KEY (pago_request_id) REFERENCES pago_request(id)
);

-- Tabla de rechazos de pago
CREATE TABLE IF NOT EXISTS pago_rechazo (
    id BIGINT NOT NULL AUTO_INCREMENT,
    codigo_rechazo VARCHAR(255),
    motivo_rechazo VARCHAR(255),
    fecha_rechazo VARCHAR(255),
    estado VARCHAR(255),
    pago_request_id BIGINT,
    PRIMARY KEY (id),
    FOREIGN KEY (pago_request_id) REFERENCES pago_request(id)
);
