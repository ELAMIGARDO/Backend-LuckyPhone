-- ==============================================================================
-- MIGRACIÓN FLYWAY V8: Limpieza de columnas redundantes en 'producto'
-- NOTA CRÍTICA: NO TOCA, NO MODIFICA NI VACÍA LA TABLA 'cliente'
-- ==============================================================================

-- 1. Eliminación de tabla huérfana obsoleta en singular
DROP TABLE IF EXISTS garantia;

-- 2. Procedimiento seguro para verificar y remover columnas de 'producto'
DELIMITER $$

CREATE PROCEDURE drop_legacy_columns_from_producto()
BEGIN
    -- Remover imei_1 si existe
    IF EXISTS (
        SELECT 1 FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'producto' AND COLUMN_NAME = 'imei_1'
    ) THEN
        ALTER TABLE producto DROP COLUMN imei_1;
    END IF;

    -- Remover imei_2 si existe
    IF EXISTS (
        SELECT 1 FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'producto' AND COLUMN_NAME = 'imei_2'
    ) THEN
        ALTER TABLE producto DROP COLUMN imei_2;
    END IF;

    -- Remover numero_serial si existe
    IF EXISTS (
        SELECT 1 FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'producto' AND COLUMN_NAME = 'numero_serial'
    ) THEN
        ALTER TABLE producto DROP COLUMN numero_serial;
    END IF;

    -- Remover bateria si existe
    IF EXISTS (
        SELECT 1 FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'producto' AND COLUMN_NAME = 'bateria'
    ) THEN
        ALTER TABLE producto DROP COLUMN bateria;
    END IF;
END$$

DELIMITER ;

CALL drop_legacy_columns_from_producto();
DROP PROCEDURE IF EXISTS drop_legacy_columns_from_producto;
