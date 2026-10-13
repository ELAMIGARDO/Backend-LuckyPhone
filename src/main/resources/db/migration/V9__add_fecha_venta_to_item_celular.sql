-- Migración V9: Agregar columna fecha_venta a item_celular para trazabilidad y auditoría contable
ALTER TABLE item_celular 
    ADD COLUMN fecha_venta TIMESTAMP NULL AFTER creado_at;

-- Si existen celulares que ya fueron vendidos, inicializar fecha_venta con creado_at
UPDATE item_celular 
SET fecha_venta = creado_at 
WHERE estado_dispositivo = 'Vendido' AND fecha_venta IS NULL;

CREATE INDEX idx_item_celular_fecha_venta ON item_celular(fecha_venta);