# EcommerceAPI

API REST de e-commerce para práctica de QA Automation (nivel principiante).

**Stack:** Spring Boot 3.2.5 · Java 17 · PostgreSQL 15 · Lombok · MapStruct · Maven

---

## Inicio rápido

### 1. Levantar PostgreSQL con Docker

```bash
docker run -d \
  --name ecommerce-postgres \
  -e POSTGRES_DB=ecommerce_db \
  -e POSTGRES_USER=ecommerce_user \
  -e POSTGRES_PASSWORD=ecommerce_pass \
  -p 5432:5432 \
  postgres:15
```

### 2. Compilar y ejecutar

```bash
mvn spring-boot:run
```

La aplicación levanta en `http://localhost:8080`.  
Al iniciar por primera vez, Hibernate crea las tablas y se carga el **seed data** automáticamente.

---

## Modelo de datos

```
users          products        orders          order_items
─────────────  ──────────────  ──────────────  ──────────────────
id (PK)        id (PK)         id (PK)         id (PK)
name           name            user_id (FK)    order_id (FK)
email (UNIQUE) description     status          product_id (FK)
passwordHash   price           total           quantity
role           stock           created_at      unitPrice
active         category        updated_at      created_at
created_at     active                          updated_at
updated_at     created_at
               updated_at
```

**Enums**
- `UserRole`: `CUSTOMER`, `ADMIN`
- `OrderStatus`: `PENDING`, `CONFIRMED`, `SHIPPED`, `CANCELLED`

---

## Endpoints

Base URL: `http://localhost:8080/api/v1`

### Productos `/products`

| Método | Ruta                    | Status OK   | Descripción                              |
|--------|-------------------------|-------------|------------------------------------------|
| GET    | `/products`             | 200         | Listar todos los productos activos       |
| GET    | `/products?category=X`  | 200         | Filtrar por categoría                    |
| GET    | `/products/{id}`        | 200 / 404   | Obtener producto por ID                  |
| POST   | `/products`             | 201         | Crear nuevo producto                     |
| PUT    | `/products/{id}`        | 200 / 404   | Actualizar producto completo             |
| PATCH  | `/products/{id}/stock`  | 200 / 404   | Actualizar solo el stock                 |
| DELETE | `/products/{id}`        | 204 / 404   | Desactivar producto (soft delete)        |

### Usuarios `/users`

| Método | Ruta          | Status OK      | Descripción                        |
|--------|---------------|----------------|------------------------------------|
| GET    | `/users`      | 200            | Listar todos los usuarios          |
| GET    | `/users/{id}` | 200 / 404      | Obtener usuario por ID             |
| POST   | `/users`      | 201 / 409      | Registrar nuevo usuario            |
| PUT    | `/users/{id}` | 200 / 404      | Actualizar datos del usuario       |
| DELETE | `/users/{id}` | 204 / 404      | Desactivar usuario (soft delete)   |

### Órdenes `/orders`

| Método | Ruta                    | Status OK        | Descripción                              |
|--------|-------------------------|------------------|------------------------------------------|
| GET    | `/orders`               | 200              | Listar todas las órdenes                 |
| GET    | `/orders/{id}`          | 200 / 404        | Obtener orden con ítems                  |
| GET    | `/orders/user/{userId}` | 200 / 404        | Listar órdenes de un usuario             |
| POST   | `/orders`               | 201 / 400 / 404  | Crear nueva orden                        |
| PATCH  | `/orders/{id}/status`   | 200 / 400 / 404  | Cambiar estado de la orden               |
| DELETE | `/orders/{id}`          | 204 / 400 / 404  | Cancelar orden (solo si está PENDING)    |

---

## Contratos JSON

### POST /products — ProductRequest
```json
{
  "name": "Laptop Pro 15",
  "description": "Laptop para profesionales",
  "price": 1299.99,
  "stock": 50,
  "category": "ELECTRONICS"
}
```

### ProductResponse
```json
{
  "id": 1,
  "name": "Laptop Pro 15",
  "description": "Laptop para profesionales",
  "price": 1299.99,
  "stock": 50,
  "category": "ELECTRONICS",
  "active": true,
  "createdAt": "2025-01-15T10:00:00"
}
```

### POST /users — UserRequest
```json
{
  "name": "María González",
  "email": "maria@example.com",
  "password": "securePass123"
}
```

### UserResponse
```json
{
  "id": 1,
  "name": "María González",
  "email": "maria@example.com",
  "role": "CUSTOMER",
  "active": true,
  "createdAt": "2025-01-15T09:00:00"
}
```
> El campo `passwordHash` **nunca** aparece en el response.

### POST /orders — CreateOrderRequest
```json
{
  "userId": 1,
  "items": [
    { "productId": 2, "quantity": 3 },
    { "productId": 5, "quantity": 1 }
  ]
}
```

### OrderResponse
```json
{
  "id": 10,
  "userId": 1,
  "status": "PENDING",
  "total": 149.97,
  "items": [
    {
      "productId": 2,
      "productName": "Mouse Inalámbrico",
      "quantity": 3,
      "unitPrice": 49.99
    }
  ],
  "createdAt": "2025-01-15T11:00:00"
}
```

### PATCH /products/{id}/stock — UpdateStockRequest
```json
{ "stock": 75 }
```

### PATCH /orders/{id}/status — UpdateOrderStatusRequest
```json
{ "status": "CONFIRMED" }
```

---

## Reglas de negocio

### Productos
- `name` obligatorio, máx. 200 caracteres
- `price` debe ser mayor que 0
- `stock` no puede ser negativo
- `DELETE` hace soft delete (`active = false`)
- `GET /products` retorna **solo** productos con `active = true`

### Usuarios
- `email` único en el sistema (409 si ya existe)
- `password` mínimo 8 caracteres, se almacena hasheado con **BCrypt**
- Rol por defecto al crear: `CUSTOMER`
- `DELETE` hace soft delete (`active = false`)

### Órdenes
- Stock se **valida** al crear (422 si insuficiente)
- Stock se **descuenta** cuando la orden pasa a `CONFIRMED`
- `DELETE /orders/{id}` solo funciona si la orden está en `PENDING` (400 de lo contrario)
- El campo `total` lo calcula el servidor: `Σ (unitPrice × quantity)`
- El `unitPrice` es un snapshot del precio actual del producto al momento de la compra

### Transiciones de estado

```
PENDING ──→ CONFIRMED
        ──→ CANCELLED

CONFIRMED ──→ SHIPPED
          ──→ CANCELLED  (stock NO se restaura)

SHIPPED   → (estado final)
CANCELLED → (estado final)
```
Cualquier otra transición devuelve **400 Bad Request**.

---

## Formato de errores

Todos los errores retornan el mismo body:

```json
{
  "timestamp": "2025-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Product with id 99 not found",
  "path": "/api/v1/products/99"
}
```

| Código | Cuándo                                            |
|--------|---------------------------------------------------|
| 400    | Validación fallida o transición de estado inválida|
| 404    | Recurso no encontrado por ID                      |
| 409    | Email duplicado al registrar usuario              |
| 422    | Stock insuficiente al crear orden                 |
| 500    | Error inesperado del servidor                     |

---

## Seed data

Cargado automáticamente al primer inicio. Contraseña para todos: `password123`.

### Usuarios

| ID | Nombre          | Email                   | Rol      |
|----|-----------------|-------------------------|----------|
| 1  | Admin Sistema   | admin@ecommerce.com     | ADMIN    |
| 2  | María González  | maria@example.com       | CUSTOMER |
| 3  | Carlos Pérez    | carlos@example.com      | CUSTOMER |

### Productos

| ID | Nombre              | Precio   | Stock | Categoría   | Activo |
|----|---------------------|----------|-------|-------------|--------|
| 1  | Laptop Pro 15       | 1299.99  | 10    | ELECTRONICS | true   |
| 2  | Mouse Inalámbrico   | 29.99    | 100   | ELECTRONICS | true   |
| 3  | Camiseta Básica     | 19.99    | 200   | CLOTHING    | true   |
| 4  | Zapatillas Running  | 89.99    | 50    | CLOTHING    | true   |
| 5  | Libro Spring Boot   | 45.00    | 30    | BOOKS       | true   |
| 6  | Producto Inactivo   | 9.99     | 0     | OTHER       | false  |

---

## Estructura del proyecto

```
ecommerce-api/
├── pom.xml
└── src/main/java/com/vermann/ecommerce/
    ├── EcommerceApplication.java
    ├── config/
    │   ├── SecurityConfig.java      # Deshabilita Spring Security (permitAll)
    │   └── DataInitializer.java     # Carga seed data al iniciar
    ├── controller/
    │   ├── ProductController.java
    │   ├── UserController.java
    │   └── OrderController.java
    ├── dto/
    │   ├── request/                 # ProductRequest, UserRequest, CreateOrderRequest...
    │   └── response/                # ProductResponse, UserResponse, OrderResponse...
    ├── exception/
    │   ├── ResourceNotFoundException.java   → 404
    │   ├── DuplicateEmailException.java     → 409
    │   ├── BusinessException.java           → configurable (400/422)
    │   └── GlobalExceptionHandler.java      # @RestControllerAdvice
    ├── mapper/                      # Interfaces MapStruct (generadas en compilación)
    ├── model/                       # Entidades JPA + Enums
    ├── repository/                  # Interfaces Spring Data JPA
    └── service/
        ├── ProductService.java / UserService.java / OrderService.java
        └── impl/                    # Implementaciones con lógica de negocio
```

---

## Configuración (`application.yml`)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ecommerce_db
    username: ecommerce_user
    password: ecommerce_pass
  jpa:
    hibernate:
      ddl-auto: update    # Crea/actualiza tablas automáticamente
    show-sql: true
server:
  port: 8080
```

---

## Notas para QA

- **Sin autenticación**: todos los endpoints son públicos para facilitar el testing.
- **Soft delete**: los registros eliminados quedan en la BD con `active = false`.
- **Producto Inactivo (ID=6)**: útil para probar que `GET /products` no lo retorna.
- **Categorías del seed**: `ELECTRONICS`, `CLOTHING`, `BOOKS`, `OTHER` (son strings libres, no un enum).
