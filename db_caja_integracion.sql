-- Ejecutar este script en MySQL Workbench sobre la base de datos db_pagos
USE db_pagos;

-- Agregar campo caja_id y estado a pago_request
ALTER TABLE pago_request
ADD COLUMN caja_id BIGINT,
ADD COLUMN estado VARCHAR(50) DEFAULT 'PENDIENTE';
