-- =====================================================================
--  BASE DE DATOS: alquiler_vehiculos
--  Version 2 -- ajustada para que coincida con las entidades JPA
--
--  CAMBIOS RESPECTO A LA VERSION ANTERIOR (2 cosas):
--    1. SERIAL/INTEGER  ->  BIGSERIAL/BIGINT
--       Motivo: en Java los ids son Long, que en PostgreSQL es BIGINT.
--       Con SERIAL (INTEGER) la aplicacion no arranca en modo validate.
--    2. Se agrega la columna alquileres.estado
--       Motivo: HU-24 debe marcar el alquiler como 'CERRADO' y
--       HU-21 debe listar solo los que no fueron cancelados.
--
--  COMO CORRERLO:
--    Este script BORRA Y RECREA las tablas (DROP ... CASCADE).
--    Como todavia no hay datos reales, no se pierde nada.
--    Ejecutarlo conectado a la base alquiler_vehiculos.
-- =====================================================================

-- Solo la primera vez, conectado como superusuario (postgres):
-- CREATE USER alquiler_vehiculos WITH ENCRYPTED PASSWORD 'alquiler';
-- CREATE DATABASE alquiler_vehiculos WITH OWNER alquiler_vehiculos;

DROP TABLE IF EXISTS alquileres, detalles_autos, autos, clientes, tiendas, categorias CASCADE;


CREATE TABLE tiendas (
    id_tienda  BIGSERIAL PRIMARY KEY,
    nombre     VARCHAR(70) NOT NULL,
    ciudad     VARCHAR(50) NOT NULL,
    direccion  VARCHAR(70) NOT NULL
);

CREATE TABLE categorias (
    id_categoria BIGSERIAL PRIMARY KEY,
    nombre       VARCHAR(50) NOT NULL,
    descripcion  TEXT NOT NULL
);

-- La tabla autos guarda solo la unidad fisica y sus relaciones.
-- Marca, modelo, anio y placa viven en detalles_autos.
CREATE TABLE autos (
    id_auto        BIGSERIAL PRIMARY KEY,
    disponibilidad BOOLEAN NOT NULL DEFAULT TRUE,
    id_tienda      BIGINT NOT NULL,
    id_categoria   BIGINT NOT NULL,

    FOREIGN KEY (id_tienda)    REFERENCES tiendas(id_tienda),
    FOREIGN KEY (id_categoria) REFERENCES categorias(id_categoria)
);

CREATE TABLE detalles_autos (
    id_detalles_autos BIGSERIAL PRIMARY KEY,
    imagen            VARCHAR(2000),
    modelo            VARCHAR(50) NOT NULL,
    marca             VARCHAR(50) NOT NULL,
    anio              VARCHAR(4) NOT NULL,
    placa             VARCHAR(20) UNIQUE NOT NULL,
    precio_dia        DECIMAL(10,2) NOT NULL,
    oferta_porcentaje DECIMAL(10,2) NULL,
    id_auto           BIGINT NOT NULL,

    FOREIGN KEY (id_auto) REFERENCES autos(id_auto),

    CHECK (precio_dia > 0),
    CHECK (oferta_porcentaje BETWEEN 0 AND 100)
);

CREATE TABLE clientes (
    id_cliente      BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    email           VARCHAR(150) UNIQUE NOT NULL,
    telefono        VARCHAR(20) NOT NULL,
    tarjeta_credito VARCHAR(20)
);

CREATE TABLE alquileres (
    id_alquiler       BIGSERIAL PRIMARY KEY,
    id_cliente        BIGINT        NOT NULL,
    id_auto           BIGINT        NOT NULL,
    fecha_inicio      DATE          NOT NULL,
    fecha_fin         DATE          NOT NULL,
    precio_total      DECIMAL(10,2) NOT NULL,
    ciudad_retirada   VARCHAR(50)   NOT NULL,
    ciudad_devolucion VARCHAR(50)   NOT NULL,
    -- NUEVO: 'ACTIVO' mientras el auto esta afuera, 'CERRADO' tras la devolucion (HU-24)
    estado            VARCHAR(20)   NOT NULL DEFAULT 'ACTIVO',

    FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente),
    FOREIGN KEY (id_auto)    REFERENCES autos(id_auto),

    CHECK (fecha_fin >= fecha_inicio),
    CHECK (precio_total >= 0),
    CHECK (estado IN ('ACTIVO', 'CERRADO'))
);


-- =====================================================================
--  DATOS DE PRUEBA
--  Sirven para probar los GET desde el primer dia sin crear nada a mano.
-- =====================================================================

INSERT INTO tiendas (nombre, ciudad, direccion) VALUES
    ('AutoRent Norte',   'Bogotá',       'Av. 19 #120-45'),
    ('AutoRent Centro',  'Medellín',     'Calle 50 #40-12'),
    ('AutoRent Sur',     'Cali',         'Carrera 5 #15-30'),
    ('AutoRent Costa',   'Barranquilla', 'Calle 72 #46-20'),
    ('AutoRent Oriente', 'Bucaramanga',  'Carrera 27 #51-10'),
    ('AutoRent Eje',     'Manizales',    'Av. Santander #23-60'),
    ('AutoRent Caribe',  'Cartagena',    'Bocagrande Calle 4 #3-12'),
    ('AutoRent Llanos',  'Villavicencio','Calle 40 #32-15');

INSERT INTO categorias (nombre, descripcion) VALUES
    ('Económico',    'Vehículos de bajo consumo y costo accesible'),
    ('Sedán',        'Vehículos de cuatro puertas para uso familiar'),
    ('SUV',          'Vehículos utilitarios deportivos de mayor capacidad'),
    ('Camioneta',    'Vehículos de carga y doble tracción'),
    ('Lujo',         'Vehículos de alta gama con equipamiento premium'),
    ('Eléctrico',    'Vehículos de cero emisiones con motor eléctrico'),
    ('Deportivo',    'Vehículos de alto desempeño y diseño aerodinámico'),
    ('Minivan',      'Vehículos de gran capacidad para grupos o familias');

INSERT INTO autos (disponibilidad, id_tienda, id_categoria) VALUES
    (TRUE,  1, 1),
    (TRUE,  2, 2),
    (FALSE, 3, 3),
    (TRUE,  4, 4),
    (TRUE,  5, 5),
    (FALSE, 6, 6),
    (TRUE,  7, 7),
    (TRUE,  8, 8);

INSERT INTO detalles_autos (imagen, modelo, marca, anio, placa, precio_dia, oferta_porcentaje, id_auto) VALUES
    ('https://imgs.com/spark.jpg',    'Spark',    'Chevrolet', '2021', 'ABC-001',  80000.00, NULL,  1),
    ('https://imgs.com/corolla.jpg',  'Corolla',  'Toyota',    '2022', 'ABC-002', 120000.00, 10.00, 2),
    ('https://imgs.com/cx5.jpg',      'CX-5',     'Mazda',     '2023', 'ABC-003', 180000.00, NULL,  3),
    ('https://imgs.com/ranger.jpg',   'Ranger',   'Ford',      '2022', 'ABC-004', 200000.00, 15.00, 4),
    ('https://imgs.com/bmw5.jpg',     'Serie 5',  'BMW',       '2023', 'ABC-005', 350000.00, NULL,  5),
    ('https://imgs.com/ioniq.jpg',    'Ioniq 6',  'Hyundai',   '2024', 'ABC-006', 220000.00, 5.00,  6),
    ('https://imgs.com/mustang.jpg',  'Mustang',  'Ford',      '2023', 'ABC-007', 300000.00, NULL,  7),
    ('https://imgs.com/carnival.jpg', 'Carnival', 'Kia',       '2022', 'ABC-008', 250000.00, 20.00, 8);

INSERT INTO clientes (nombre, email, telefono, tarjeta_credito) VALUES
    ('Carlos Ramírez', 'carlos.ramirez@email.com', '3101234567', '4111111111111111'),
    ('Laura Gómez',    'laura.gomez@email.com',    '3209876543', '4222222222222222'),
    ('Andrés Torres',  'andres.torres@email.com',  '3154567890', '4333333333333333'),
    ('María Pérez',    'maria.perez@email.com',    '3001112233', '4444444444444444'),
    ('Juan Martínez',  'juan.martinez@email.com',  '3183334455', '4555555555555555'),
    ('Sofía Herrera',  'sofia.herrera@email.com',  '3126667788', '4666666666666666'),
    ('Diego Castillo', 'diego.castillo@email.com', '3049998877', '4777777777777777'),
    ('Valentina Ruiz', 'valentina.ruiz@email.com', '3172221133', '4888888888888888');

-- Los 4 primeros quedan CERRADOS (ya se devolvieron) y los 4 ultimos ACTIVOS,
-- para poder probar HU-21 (listar activos) contra datos reales.
INSERT INTO alquileres (id_cliente, id_auto, fecha_inicio, fecha_fin, precio_total, ciudad_retirada, ciudad_devolucion, estado) VALUES
    (1, 1, '2025-07-01', '2025-07-05', 320000.00, 'Bogotá',        'Villavicencio', 'CERRADO'),
    (2, 2, '2025-07-03', '2025-07-07', 432000.00, 'Medellín',      'Cali',          'CERRADO'),
    (3, 4, '2025-07-10', '2025-07-12', 400000.00, 'Cali',          'Bucaramanga',   'CERRADO'),
    (4, 5, '2025-07-15', '2025-07-20', 875000.00, 'Barranquilla',  'Bucaramanga',   'CERRADO'),
    (5, 7, '2025-07-08', '2025-07-09', 300000.00, 'Bucaramanga',   'Cartagena',     'ACTIVO'),
    (6, 8, '2025-07-20', '2025-07-23', 600000.00, 'Manizales',     'Bogotá',        'ACTIVO'),
    (7, 2, '2025-07-25', '2025-07-28', 388800.00, 'Cartagena',     'Medellín',      'ACTIVO'),
    (8, 6, '2025-07-18', '2025-07-20', 418000.00, 'Villavicencio', 'Barranquilla',  'ACTIVO');
