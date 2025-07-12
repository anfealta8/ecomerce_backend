# Servicio Backend de E-commerce con Spring Boot

## Descripción General del Proyecto

Este proyecto es el servicio backend de un sistema de e-commerce, construido con Spring Boot.  Está diseñado para gestionar los componentes esenciales de una tienda online, incluyendo la administración de productos, inventarios, órdenes  y usuarios. Cumple con los requisitos de un Producto Mínimo Viable (MVP) y considera funcionalidades avanzadas como la aplicación de descuentos dinámicos y un sistema de auditoría. 

## Características Implementadas (MVP)

El backend soporta las siguientes funcionalidades clave, según lo definido en el alcance del proyecto: 

* **Gestión Completa (CRUD) de Entidades:** 
    * **Usuarios:** Creación de usuarios, login, y operaciones CRUD completas para la gestión de usuarios.
    * **Productos:** Operaciones CRUD para gestionar el catálogo de productos.
    * **Inventarios:** Gestión de las cantidades disponibles y reservadas de productos, incluyendo la actualización automática al crear órdenes.
    * **Órdenes:** Creación, consulta, actualización de estado y eliminación de órdenes.

* **Funcionalidades Específicas de Órdenes (Casos Especiales de Funcionamiento):**
    * **Descuento del 10%:** Aplicación automática si la orden se registra dentro de un rango de tiempo predefinido.
    * **Descuento Aleatorio del 50%:** Se aplica si la función de pedido aleatorio es seleccionada y la probabilidad es favorable, siempre que la orden esté dentro del rango de tiempo definido.
    * **Descuento del 5% para Cliente Frecuente:** Descuento adicional si el usuario es identificado como cliente frecuente.
    * **Combinación de Descuentos:** Los descuentos pueden aplicarse de manera combinada según las reglas de negocio.

* **Búsqueda Avanzada de Productos:**
    * Permite buscar productos por diversos criterios, como nombre o categoría.

* **Reportes Estratégicos:**
    * Reporte de productos activos.
    * Reporte del Top 5 de productos más vendidos.
    * Reporte del Top 5 de clientes frecuentes.

* **Sistema de Auditoría:**
    * Implementación de un mecanismo de auditoría para registrar las operaciones importantes (creación, actualización, eliminación) en las entidades clave.

* **Pruebas Unitarias:**
    * Amplia cobertura de pruebas unitarias para asegurar la calidad y el correcto funcionamiento del sistema.

## Tecnologías Utilizadas

* **Lenguaje:** Java 17
* **Framework:** Spring Boot 3.x
* **Base de Datos:** MySQL
* **Gestor de Dependencias:** Maven
* **Seguridad:** Spring Security (con JWT)
* **Validación:** Jakarta Validation
* **Pruebas:** JUnit 5, Mockito
* **Utilidades:** Lombok (para simplificar DTOs y entidades)

## Requisitos Previos

Antes de ejecutar este proyecto, asegúrate de tener instalado:

* **Java Development Kit (JDK):** Versión 17 o superior.
* **Apache Maven:** Versión 3.x o superior.
* **MySQL Server:** Una instancia de MySQL en ejecución (versión 8.0+ recomendada).

## Configuración de la Base de Datos

1.  **Crear la Base de Datos:**
    Crea una base de datos MySQL para el proyecto. Puedes nombrarla `eecomerce_backend`:
    ```sql
    CREATE DATABASE eecomerce_backend;
    ```
2.  **Configuración de Conexión:**
    Abre el archivo `src/main/resources/application.properties` y actualiza la configuración de la base de datos con tus credenciales de MySQL:

    ```properties
    # application.properties
    spring.jackson.time-zone=America/Bogota
    server.port=9000

    spring.datasource.url=jdbc:mysql://localhost:3306/eecomerce_backend
    spring.datasource.username=dba
    spring.datasource.password=!E[6jt7S(8wTmnyW
    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.show-sql=true
    spring.jpa.properties.hibernate.format_sql=true 
    jwt.secret=qA87gJqGHFJc9aF1GVl6pG23wQ6Sl2T7dYKiGeyM+/I=
    jwt.expiration=86400000
    springdoc.api-docs.enabled=true
    app.descuentos.fecha-inicio=2025-07-01T00:00:00
    app.descuentos.fecha-fin=2025-07-31T23:59:59
    app.descuentos.probabilidad-aleatorio=0.05  
    app.descuentos.cliente-frecuente.min-ordenes=5
    app.descuentos.cliente-frecuente.periodo-dias=30```
    

## Configuración y Ejecución del Proyecto

1.  **Clonar el Repositorio:**
    ```bash
    git clone [https://github.com/anfealta8/ecomerce_backend.git](https://github.com/anfealta8/ecomerce_backend.git)
    cd ecomerce_backend
    ```

2.  **Construir el Proyecto:**
    ```bash
    mvn clean install
    ```

3.  **Ejecutar la Aplicación:**
    * **Desde la línea de comandos:**
        ```bash
        mvn spring-boot:run
        ```
    * **Desde tu IDE (ej. IntelliJ IDEA, Eclipse):**
        Ejecuta la clase principal `EcomerceBackendApplication.java`.

La aplicación se iniciará por defecto en `http://localhost:9000`.

## Ejecución de Pruebas

Para ejecutar las pruebas unitarias del proyecto:

```bash
mvn test
