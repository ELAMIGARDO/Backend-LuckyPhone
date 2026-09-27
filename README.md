# 📱 Backend LuckyPhone - API RESTful Comercial

Backend comercial desarrollado con **Spring Boot 3 / Java 21** para el catálogo público de celulares y panel de administración interna de **LuckyPhone**.

---

## 🚀 Tecnologías Utilizadas

* **Lenguaje:** Java 21
* **Framework:** Spring Boot 4.0.5 / 3.x
* **Seguridad:** Spring Security + JWT (JSON Web Tokens)
* **Base de Datos:** MySQL 8
* **Versionado de BD:** Flyway DB
* **Documentación:** Swagger UI / OpenAPI 3
* **Testing:** JUnit 5 & Mockito
* **Containerización:** Docker & Docker Compose
* **CI/CD:** GitHub Actions

---

## 🛠️ Arquitectura y Estructura del Proyecto

```text
com.ventas.luckyphonedemo
├── config/             # Configuración de OpenAPI / Swagger UI
├── controller/         # Controladores REST (Auth, Producto, Categoria, Cliente)
├── dto/                # Request y Response DTOs
├── exception/          # Manejo global de excepciones (@RestControllerAdvice)
├── mapper/             # Mapeadores de Objetos (ProductoMapper, ClienteMapper)
├── model/              # Entidades JPA (Producto, Categoria, Cliente)
├── repositorio/        # Repositorios Spring Data JPA
├── security/           # Filtros JWT, Provider y SecurityConfig
└── service/            # Lógica de Negocio
```

---

## ⚙️ Variables de Entorno

| Variable | Descripción | Valor por Defecto |
| :--- | :--- | :--- |
| `SPRING_DATASOURCE_URL` | URL de conexión JDBC a MySQL | `jdbc:mysql://localhost:3301/luckyphone` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de MySQL | `root` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de MySQL | `root` |
| `JWT_SECRET` | Clave secreta para firmar tokens JWT | `LuckyPhoneSecretKey...` |
| `LUCKYPHONE_WHATSAPP_NUMERO` | Número de WhatsApp corporativo | `51934368572` |

---

## 📦 Ejecución con Docker Compose

Para desplegar la aplicación y la base de datos MySQL 8 con un solo comando:

```bash
docker compose up --build
```

* **Swagger UI:** `http://localhost:8080/swagger-ui.html`
* **API Base URL:** `http://localhost:8080/api`

---

## 🧪 Pruebas Automatizadas

Para ejecutar la suite de pruebas unitarias:

```bash
./mvnw test
```

---

## 🔐 Endpoints Principales

### Autenticación
* `POST /api/auth/registro` - Registro de nuevos usuarios/admins.
* `POST /api/auth/login` - Autenticación y obtención del Token JWT.

### Catálogo Público (Sin necesidad de Token)
* `GET /api/productos/lista` - Lista de celulares activos paginada.
* `GET /api/productos/buscar?nombre=iPhone` - Buscador inteligente del catálogo.
* `GET /api/productos/{id}` - Ficha técnica de un equipo con enlace comercial a WhatsApp.

### Panel de Administración (Requiere `ROLE_ADMIN`)
* `POST /api/productos` - Crear un nuevo producto.
* `PUT /api/productos/{id}` - Actualizar producto.
* `DELETE /api/productos/{id}` - Eliminar producto.
* `PATCH /api/productos/{id}/estado` - Activar/Desactivar visibilidad.
