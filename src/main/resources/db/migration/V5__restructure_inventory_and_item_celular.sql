-- Script de migración V5: Estructura Padre-Hijo (Producto - ItemCelular)
ALTER TABLE producto 
    DROP COLUMN IF EXISTS imei_1,
    DROP COLUMN IF EXISTS imei_2,
    DROP COLUMN IF EXISTS numero_serial,
    DROP COLUMN IF EXISTS bateria;

ALTER TABLE producto 
    MODIFY COLUMN stock INT NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS item_celular (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    producto_id BIGINT NOT NULL,
    imei_1 VARCHAR(20) NOT NULL,
    imei_2 VARCHAR(20) NULL,
    numero_serial VARCHAR(50) NULL,
    bateria VARCHAR(10) NULL COMMENT 'Ej: 85%, 100%',
    estado_dispositivo ENUM('Disponible', 'Reservado', 'Vendido') NOT NULL DEFAULT 'Disponible',
    creado_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    actualizado_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_item_celular_producto FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE CASCADE,
    CONSTRAINT uq_item_celular_imei_1 UNIQUE (imei_1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_item_celular_producto ON item_celular(producto_id);
CREATE INDEX idx_item_celular_estado ON item_celular(estado_dispositivo);
CREATE INDEX idx_item_celular_busqueda ON item_celular(producto_id, estado_dispositivo);
