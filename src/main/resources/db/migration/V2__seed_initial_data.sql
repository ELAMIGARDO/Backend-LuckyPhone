-- Script de migración V2: Datos semilla de categorías iniciales

INSERT INTO categoria (id, nombre) VALUES (1, 'Celulares') ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);
INSERT INTO categoria (id, nombre) VALUES (2, 'Tablets') ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);
INSERT INTO categoria (id, nombre) VALUES (3, 'Accesorios') ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);
