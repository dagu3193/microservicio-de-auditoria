# 🛡️ Microservicio de Auditoría (Audit Service)

Este microservicio se encarga del registro, persistencia y consulta de bitácoras de auditoría ante cambios en **precios** y **cupos/capacidad** de productos o servicios del e-commerce. Está diseñado siguiendo principios de **Arquitectura Hexagonal (Ports & Adapters)** y **Clean Code**, garantizando un bajo acoplamiento y alta mantenibilidad.

---

## 🚀 Tecnologías y Características Principales

*   **Java 21**: Uso nativo de **Virtual Threads** (Hilos Virtuales) para un manejo eficiente de operaciones I/O concurrentes y de alto rendimiento.
*   **Spring Boot 3.2.5**: Framework base (Spring Web, Spring Data JPA, Spring AMQP, Actuator).
*   **Arquitectura Hexagonal**: Separación limpia entre el dominio de negocio, los casos de uso (puertos) y los detalles técnicos (adaptadores REST, base de datos y mensajería).
*   **PostgreSQL**: Base de datos relacional para almacenar las bitácoras.
*   **Flyway**: Control de versiones y migraciones automáticas del esquema de base de datos.
*   **RabbitMQ**: Consumo asíncrono de eventos de cambios de precio y cupo publicados por otros microservicios.
*   **Lombok y MapStruct**: Reducción de código repetitivo y mapeo automático entre capas.
*   **Springdoc OpenAPI (Swagger)**: Documentación interactiva de las APIs del microservicio.
*   **Testcontainers**: Pruebas de integración automatizadas levantando contenedores reales de PostgreSQL y RabbitMQ mediante Docker.

---

## 📂 Arquitectura del Proyecto

El proyecto se estructura bajo el patrón de **Arquitectura Hexagonal (Puertos y Adaptadores)**:

```text
com.ecommerce.audit
│
├── domain/                         # Núcleo del Negocio (Independiente del Framework)
│   ├── model/                      # Entidades del Dominio (AuditLog, AuditType)
│   └── port/                       # Puertos de Entrada y Salida (Interfaces)
│       ├── in/                     # Casos de uso invocados por adaptadores de entrada
│       └── out/                    # Operaciones requeridas por el dominio (ej. Persistencia)
│
├── application/                    # Lógica de Aplicación
│   ├── dto/                        # Data Transfer Objects (Requests, Responses)
│   └── service/                    # Implementación de los casos de uso (Servicios de dominio)
│
└── infrastructure/                 # Detalles Técnicos y Adaptadores
    ├── adapter/                    # Implementaciones concretas de los puertos
    │   ├── in/                     # Adaptadores de entrada (REST Controllers, RabbitMQ Consumers)
    │   └── out/                    # Adaptadores de salida (JPA Repositories, Database Adapters)
    ├── config/                     # Configuraciones (OpenAPI, RabbitMQ, Virtual Threads)
    └── exception/                  # Manejo global de excepciones
```

---

## 🛠️ Requisitos Previos

Antes de ejecutar el microservicio, asegúrate de tener instalado:
*   [Java JDK 21](https://www.oracle.com/java/technologies/downloads/)
*   [Apache Maven 3.9+](https://maven.apache.org/download.cgi)
*   [Docker](https://www.docker.com/) (necesario para ejecutar los tests de integración con *Testcontainers*)
*   Instancia de PostgreSQL y RabbitMQ corriendo (opcional si se definen variables de entorno externas, de lo contrario se asume `localhost`).

---

## ⚙️ Configuración del Entorno

El microservicio puede configurarse mediante variables de entorno en el archivo `src/main/resources/application.yml`. A continuación se detallan las variables soportadas:

| Variable de Entorno | Descripción | Valor por Defecto |
| :--- | :--- | :--- |
| `SERVER_PORT` | Puerto en el que corre el servidor web | `8084` |
| `DB_HOST` | Host del servidor de base de datos PostgreSQL | `localhost` |
| `DB_PORT` | Puerto de PostgreSQL | `5432` |
| `DB_NAME` | Nombre de la base de datos | `audit_db` |
| `DB_USER` | Usuario de conexión a la base de datos | `audit_user` |
| `DB_PASSWORD` | Contraseña del usuario de base de datos | `audit_pass` |
| `RABBITMQ_HOST` | Host del servidor de RabbitMQ | `localhost` |
| `RABBITMQ_PORT` | Puerto de RabbitMQ | `5672` |
| `RABBITMQ_USER` | Usuario de RabbitMQ | `guest` |
| `RABBITMQ_PASSWORD` | Contraseña del usuario de RabbitMQ | `guest` |
| `RABBITMQ_VHOST` | Virtual Host en RabbitMQ | `/` |

---

## ⚡ API REST Endpoints

La base URL de las APIs es `/api/audit/logs`.

### 1. Registrar Auditoría Manual
Permite registrar un log de auditoría enviando los datos del cambio directamente vía HTTP.

*   **Endpoint**: `POST /api/audit/logs`
*   **Content-Type**: `application/json`
*   **Request Body**:
    ```json
    {
      "changeType": "PRICE", 
      "itemId": "PROD-9876",
      "itemName": "Laptop ASUS ROG Zephyrus",
      "oldValue": "1200.00",
      "newValue": "1150.00",
      "reason": "Promoción del CyberMonday",
      "changedBy": "admin_user"
    }
    ```
    *(Nota: `changeType` acepta únicamente `PRICE` o `QUOTA`)*.
*   **Response (201 Created)**:
    ```json
    {
      "id": "e8fb47de-d98c-4a3b-9ab5-1db65f7c32bf",
      "changeType": "PRICE",
      "itemId": "PROD-9876",
      "itemName": "Laptop ASUS ROG Zephyrus",
      "oldValue": "1200.00",
      "newValue": "1150.00",
      "reason": "Promoción del CyberMonday",
      "changedBy": "admin_user",
      "changedAt": "2026-06-06T17:51:30"
    }
    ```

### 2. Consultar Historial de Auditorías (Paginado y Filtrado)
Permite buscar los registros aplicando diferentes filtros. Retorna una respuesta estructurada con paginación de Spring Data.

*   **Endpoint**: `GET /api/audit/logs`
*   **Parámetros de Consulta (Query Params - Opcionales)**:
    *   `changeType`: Filtra por tipo de cambio (`PRICE` o `QUOTA`).
    *   `itemId`: ID específico del item auditado.
    *   `changedBy`: Nombre/ID del usuario o sistema que realizó el cambio.
    *   `from`: Fecha y hora de inicio en formato ISO (`YYYY-MM-DDTHH:MM:SS`).
    *   `to`: Fecha y hora de fin en formato ISO (`YYYY-MM-DDTHH:MM:SS`).
    *   `page`: Número de página a retornar (base `0`, por defecto `0`).
    *   `size`: Cantidad de registros por página (por defecto `10`).

*   **Ejemplo de Petición**:
    ```bash
    GET /api/audit/logs?changeType=PRICE&page=0&size=5
    ```

### 3. Obtener Auditoría por ID
Obtiene el detalle completo de un registro de auditoría individual empleando su ID (UUID).

*   **Endpoint**: `GET /api/audit/logs/{id}`
*   **Ejemplo de Petición**:
    ```bash
    GET /api/audit/logs/e8fb47de-d98c-4a3b-9ab5-1db65f7c32bf
    ```

---

## 🖲️ Consumo Asíncrono de Eventos (RabbitMQ)

El microservicio cuenta con un consumidor automático (`PriceQuotaEventConsumer`) que escucha los eventos publicados en:
*   **Exchange**: `price-quota.exchange` (Topic Exchange)
*   **Cola**: `audit.price-quota.queue`

El consumidor utiliza la **Routing Key** para clasificar el tipo de auditoría:

### 1. Cambio de Precio (`price.changed`)
Procesa eventos de modificación de precios.
*   **Routing Key**: `price.changed`
*   **Payload Esperado**:
    ```json
    {
      "itemId": "PROD-9876",
      "itemName": "Laptop ASUS ROG Zephyrus",
      "oldPrice": 1200.00,
      "newPrice": 1150.00,
      "changedBy": "sistema_precios",
      "reason": "Actualización automática de tarifas"
    }
    ```

### 2. Cambio de Cupo (`quota.changed`)
Procesa eventos de modificación de inventario, cupos o capacidad.
*   **Routing Key**: `quota.changed`
*   **Payload Esperado**:
    ```json
    {
      "itemId": "PROD-9876",
      "itemName": "Laptop ASUS ROG Zephyrus",
      "oldQuota": 50,
      "newQuota": 45,
      "changedBy": "sistema_inventario",
      "reason": "Venta de unidad"
    }
    ```

---

## 🗄️ Esquema de Base de Datos

La estructura de la tabla de persistencia `audit_logs` es administrada por Flyway mediante archivos SQL ubicados en `src/main/resources/db/migration/`:

```sql
CREATE TABLE IF NOT EXISTS audit_logs (
    id            VARCHAR(36)     PRIMARY KEY,
    change_type   VARCHAR(30)     NOT NULL,
    item_id       VARCHAR(100)    NOT NULL,
    item_name     VARCHAR(200)    NOT NULL,
    old_value     VARCHAR(200),
    new_value     VARCHAR(200)    NOT NULL,
    reason        VARCHAR(500),
    changed_by    VARCHAR(100)    NOT NULL,
    changed_at    TIMESTAMP       NOT NULL
);
```

### Índices de Rendimiento
Para acelerar las búsquedas y generación de reportes, se cuenta con índices sobre los campos de filtro principales:
*   `idx_audit_logs_type` en la columna `change_type`
*   `idx_audit_logs_item` en la columna `item_id`
*   `idx_audit_logs_changed_by` en la columna `changed_by`
*   `idx_audit_logs_changed_at` en la columna `changed_at`

---

## 📕 Documentación de Swagger (OpenAPI)

Cuando el servicio esté en ejecución, puedes explorar, probar e interactuar con las APIs de manera visual a través de la UI de Swagger:

🔗 **URL local**: [http://localhost:8084/swagger-ui.html](http://localhost:8084/swagger-ui.html)  
🔗 **API Docs (JSON)**: [http://localhost:8084/api-docs](http://localhost:8084/api-docs)

---

## 🔨 Instrucciones de Compilación y Ejecución

### 1. Compilar y Ejecutar Pruebas
Para compilar el proyecto y ejecutar las pruebas unitarias y de integración (asegúrate de tener Docker corriendo para Testcontainers):
```bash
mvn clean test
```

### 2. Empaquetar el Proyecto
Para generar el archivo `.jar` ejecutable:
```bash
mvn clean package -DskipTests
```
El archivo generado se ubicará en la ruta `target/audit-service-1.0.0.jar`.

### 3. Ejecutar Localmente
Puedes iniciar la aplicación usando Maven:
```bash
mvn spring-boot:run
```
O directamente con el JAR compilado:
```bash
java -jar target/audit-service-1.0.0.jar
```
