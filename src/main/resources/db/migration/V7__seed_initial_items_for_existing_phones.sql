-- V7: Sembrar items disponibles iniciales para los celulares existentes que quedaron sin items
INSERT INTO item_celular (producto_id, imei_1, imei_2, numero_serial, bateria, estado_dispositivo)
SELECT p.id, 
       CONCAT('35', LPAD(p.id, 10, '0'), '01') AS imei_1, 
       CONCAT('35', LPAD(p.id, 10, '0'), '02') AS imei_2, 
       CONCAT('SN-', LPAD(p.id, 6, '0')) AS numero_serial, 
       '100%' AS bateria, 
       'Disponible' AS estado_dispositivo
FROM producto p
JOIN categoria c ON p.categoria_id = c.id
WHERE c.nombre = 'Celulares'
  AND NOT EXISTS (SELECT 1 FROM item_celular ic WHERE ic.producto_id = p.id);
