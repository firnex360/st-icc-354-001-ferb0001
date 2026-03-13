# Clon Mocky - Práctica 2

Clon de [Mocky.io](https://designer.mocky.io/) desarrollado con **Spring Boot 4**, **Thymeleaf** y **Bootstrap 5**. Permite crear endpoints HTTP mockeados con respuestas configurables, headers personalizados, delays, expiración y autenticación JWT.

## Tecnologías

| Componente        | Tecnología                          |
|-------------------|-------------------------------------|
| Backend           | Spring Boot 4.0.2, Java 25         |
| Frontend          | Thymeleaf + Bootstrap 5.3.3 (CDN)  |
| Base de datos     | H2 (en memoria)                    |
| Seguridad         | Spring Security 7 + JWT (jjwt)     |
| Build             | Gradle 9.3.0                       |
| Internacionalización | i18n (Español / English)        |

## Requisitos

- **Java 25** (o compatible con toolchain Java 25)
- **Gradle 9.3.0** (incluido vía wrapper)

## Ejecución

```bash
# Clonar el repositorio
git clone <url-del-repo>
cd practica-2/clon-mocky

# Ejecutar la aplicación
./gradlew bootRun        # Linux/Mac
gradlew.bat bootRun      # Windows
```

La aplicación estará disponible en: **http://localhost:8080**

## Credenciales por defecto

| Usuario | Password | Roles              |
|---------|----------|--------------------|
| admin   | admin    | ROLE_ADMIN, ROLE_USER |
| user    | user     | ROLE_USER          |

## Estructura del proyecto

```
src/main/java/com/practica/clon_mocky/
├── config/          # Configuración (Security, DataInitializer, WebConfig, PasswordEncoder)
├── controllers/     # Controladores MVC (Dashboard, Proyecto, Mock, Admin, Login, MockConsumer)
├── entities/        # Entidades JPA (Usuario, Proyecto, MockEndpoint)
├── enums/           # Enumeraciones (Rol, HttpMetodo, TipoExpiracion)
├── repositories/    # Repositorios JPA
├── security/        # Filtro JWT (JwtAuthorizationFilter)
├── services/        # Servicios de negocio (Usuario, Proyecto, MockEndpoint, JWT)
└── ClonMockyApplication.java

src/main/resources/
├── application.properties
├── messages.properties      # i18n Español (por defecto)
├── messages_en.properties   # i18n English
├── static/css/style.css     # Estilos personalizados
└── templates/               # Plantillas Thymeleaf
    ├── fragments/layout.html
    ├── login.html
    ├── dashboard.html
    ├── error.html
    ├── admin/               # Gestión de usuarios (solo ADMIN)
    ├── proyectos/           # CRUD de proyectos
    └── mocks/               # CRUD y detalle de mocks
```

## Funcionalidades

### Gestión de Proyectos
- Crear, editar y eliminar proyectos
- Cada proyecto agrupa múltiples mocks
- Filtrado por usuario (los admins ven todos)

### Mock Endpoints
- **Métodos HTTP**: GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS
- **Código de respuesta**: Configurable (100-599)
- **Content-Type**: JSON, XML, text/plain, HTML, CSV
- **Body**: Cuerpo de respuesta personalizado
- **Headers**: Headers HTTP personalizados (clave-valor)
- **Delay**: Demora configurable (0-30 segundos)
- **Expiración**: 1 Hora, 1 Día, 1 Semana, 1 Mes, 1 Año
- **JWT**: Opcionalmente requiere token Bearer para consumo

### Consumo de Mocks

Cada mock tiene una URL pública con UUID:

```
GET http://localhost:8080/mock/{uuid}
```

Si el mock requiere JWT:
```bash
curl -X GET http://localhost:8080/mock/{uuid} \
  -H "Authorization: Bearer <token>"
```

El token JWT se genera automáticamente al crear el mock y se muestra en la página de detalle.

### Seguridad

- **Cadena 1** (`/mock/**`): Stateless + JWT Bearer token
- **Cadena 2** (`/**`): Sesiones HTTP + formulario de login
- Rutas `/admin/**` requieren `ROLE_ADMIN`
- CSRF habilitado para formularios web
- Contraseñas encriptadas con BCrypt

### Internacionalización (i18n)

Cambiar idioma agregando `?lang=en` o `?lang=es` a cualquier URL:

```
http://localhost:8080/login?lang=en    # English
http://localhost:8080/login?lang=es    # Español
```

### Panel de Administración

- Gestión de usuarios (crear, editar, activar/desactivar)
- Asignación de roles
- Vista global de todos los proyectos y mocks
- Estadísticas en el dashboard

## Datos de ejemplo

Al iniciar la aplicación por primera vez, se crean automáticamente:
- 2 usuarios (admin y user)
- 1 proyecto de ejemplo ("API de Ejemplo")
- 3 mocks de demostración:
  - **Listar Usuarios** (GET 200, JSON)
  - **Crear Usuario** (POST 201, JSON con delay de 2s)
  - **Usuario No Encontrado** (GET 404, error JSON)

## API de Consumo de Mocks

| Endpoint             | Método    | Descripción                    |
|----------------------|-----------|--------------------------------|
| `/mock/{uuid}`       | Cualquiera | Consume el mock y retorna la respuesta configurada |

**Respuestas posibles:**
- Respuesta configurada del mock (código, headers, body)
- `404` - Mock no encontrado
- `410` - Mock expirado
- `403` - Mock inactivo o JWT inválido/faltante
