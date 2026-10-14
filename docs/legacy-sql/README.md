# SQL anterior (solo referencia)

Estos scripts crean el esquema **relacional de PostgreSQL** que usaba el proyecto antes de pasar a DynamoDB.
Ya **no se usan**: las tablas de DynamoDB las crea la propia aplicacion al arrancar (ver `GUIA_DYNAMODB.md`).

Se conservan para poder comparar el modelo SQL (6 tablas, llaves foraneas, `UNIQUE`, `CHECK`) con el modelo
NoSQL (documentos, indices secundarios, candados de unicidad).
