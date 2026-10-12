-- Migración V4: Aumentar la capacidad de imagen_url para soportar Data URLs e imágenes locales en Base64
ALTER TABLE producto MODIFY COLUMN imagen_url LONGTEXT;