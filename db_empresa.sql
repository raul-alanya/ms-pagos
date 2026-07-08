-- Ejecutar este script en MySQL Workbench sobre la base de datos db_pagos
USE db_pagos;

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

-- Agregar columna empresa_id a configuracion_izipay si no existe
ALTER TABLE configuracion_izipay
ADD COLUMN IF NOT EXISTS empresa_id BIGINT,
ADD CONSTRAINT fk_config_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id);
