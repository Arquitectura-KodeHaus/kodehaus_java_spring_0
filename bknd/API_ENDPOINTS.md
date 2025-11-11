# API Endpoints - PlazApp Plaza Service

## Endpoint de Registro de Managers

### POST /api/managers/register

Endpoint para que el System Service registre nuevos managers en el Plaza Service.

**Permisos**: Público (no requiere autenticación)

**Request:**
```json
{
  "username": "manager2",
  "email": "manager2@plaza.com",
  "password": "password123",
  "firstName": "Jane",
  "lastName": "Smith",
  "phoneNumber": "+1-555-0005",
  "plazaId": 1,
  "roleIds": [1],
  "externalId": "ext-manager-123"
}
```

**Nota:** El campo `externalId` es opcional. Si se proporciona, debe ser único.

**Response (201 Created):**
```json
{
  "id": 5,
  "username": "manager2",
  "email": "manager2@plaza.com",
  "firstName": "Jane",
  "lastName": "Smith",
  "phoneNumber": "+1-555-0005",
  "isActive": true,
  "createdAt": "2025-10-27T11:22:12.8054297",
  "updatedAt": "2025-10-27T11:22:12.8054297",
  "plazaId": 1,
  "plazaName": "Centro Comercial Plaza Central",
  "externalId": "ext-manager-123",
  "roles": [
    {
      "id": 1,
      "name": "MANAGER",
      "description": "Plaza Manager with full access",
      "isActive": true
    }
  ],
  "fullName": "Jane Smith"
}
```

**Errores posibles:**
- `409 Conflict`: Username ya existe
- `409 Conflict`: Email ya existe
- `409 Conflict`: External ID ya existe
- `400 Bad Request`: Plaza no encontrada
- `400 Bad Request`: Plaza no está activa

**Nota:** Si no se proporcionan `roleIds`, se asigna automáticamente el rol MANAGER (ID: 1).

---

## Otros Endpoints Disponibles

### Endpoints Públicos

#### POST /api/auth/login
**Request:**
```json
{
  "username": "manager1",
  "password": "password123"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "id": 1,
  "username": "manager1",
  "email": "manager@plazacentral.com",
  "firstName": "John",
  "lastName": "Doe",
  "fullName": "John Doe",
  "plazaId": 1,
  "plazaName": "Centro Comercial Plaza Central",
  "roles": ["MANAGER"]
}
```

#### POST /api/auth/logout
Cierra la sesión del usuario autenticado.

#### GET /api/auth/me
Obtiene información del usuario autenticado actual.

---

### Endpoints Protegidos (Requieren JWT)

#### GET /api/users
Lista todos los usuarios de la plaza del usuario autenticado.
**Roles:** MANAGER, ADMIN

#### POST /api/users
Crea un nuevo usuario en la plaza.
**Roles:** MANAGER, ADMIN
**Request:**
```json
{
  "username": "employee2",
  "email": "employee2@plaza.com",
  "password": "password123",
  "firstName": "Bob",
  "lastName": "Johnson",
  "phoneNumber": "+1-555-0006",
  "plazaId": 1,
  "roleIds": [4]
}
```

#### GET /api/users/{id}
Obtiene un usuario por ID.
**Roles:** MANAGER, ADMIN

#### PUT /api/users/{id}
Actualiza un usuario existente.
**Roles:** MANAGER, ADMIN

#### DELETE /api/users/{id}
Desactiva un usuario (soft delete).
**Roles:** MANAGER, ADMIN

---

#### GET /api/roles
Lista todos los roles disponibles.
**Roles:** MANAGER, ADMIN

#### POST /api/roles
Crea un nuevo rol.
**Roles:** MANAGER, ADMIN
**Request:**
```json
{
  "name": "EMPLOYEE_CLEANING",
  "description": "Cleaning personnel",
  "permissionIds": [9, 10]
}
```

#### GET /api/roles/{id}
Obtiene un rol por ID.
**Roles:** MANAGER, ADMIN

#### PUT /api/roles/{id}
Actualiza un rol existente.
**Roles:** MANAGER, ADMIN

#### DELETE /api/roles/{id}
Desactiva un rol (soft delete).
**Roles:** MANAGER, ADMIN

---

#### GET /api/bulletins
Lista todos los boletines de la plaza.
**Roles:** MANAGER, EMPLOYEE_GENERAL, EMPLOYEE_SECURITY, EMPLOYEE_PARKING

#### POST /api/bulletins
Crea un nuevo boletín.
**Roles:** MANAGER, EMPLOYEE_GENERAL, EMPLOYEE_SECURITY, EMPLOYEE_PARKING
**Request:**
```json
{
  "title": "Daily Market Prices - 2025-10-27",
  "content": "Fresh produce prices:\n• Potatoes: $1000/kg\n• Tomatoes: $2000/kg",
  "publicationDate": "2025-10-27",
  "plazaId": 1
}
```

#### GET /api/bulletins/{id}
Obtiene un boletín por ID.
**Roles:** MANAGER, EMPLOYEE_GENERAL, EMPLOYEE_SECURITY, EMPLOYEE_PARKING

#### GET /api/bulletins/today
Obtiene los boletines del día de hoy.
**Roles:** MANAGER, EMPLOYEE_GENERAL, EMPLOYEE_SECURITY, EMPLOYEE_PARKING

#### GET /api/bulletins/date/{date}
Obtiene los boletines de una fecha específica (formato: YYYY-MM-DD).
**Roles:** MANAGER, EMPLOYEE_GENERAL, EMPLOYEE_SECURITY, EMPLOYEE_PARKING

#### PUT /api/bulletins/{id}
Actualiza un boletín existente.
**Roles:** MANAGER, EMPLOYEE_GENERAL, EMPLOYEE_SECURITY, EMPLOYEE_PARKING

#### DELETE /api/bulletins/{id}
Desactiva un boletín (soft delete).
**Roles:** MANAGER, EMPLOYEE_GENERAL, EMPLOYEE_SECURITY, EMPLOYEE_PARKING

---

#### GET /api/managers/{plazaId}
Obtiene el manager de una plaza específica.
**Permisos:** Público

#### GET /api/managers/{plazaId}/exists
Verifica si existe un manager para una plaza.
**Permisos:** Público
**Response:** `true` o `false`

---

## Uso de Autenticación JWT

Para acceder a endpoints protegidos, incluir el token JWT en el header:

```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

## Datos de Prueba

### Usuarios
- **Manager**: `manager1` / `password123`
- **Security**: `security1` / `password123`
- **Parking**: `parking1` / `password123`
- **Employee**: `employee1` / `password123`

### Plaza
- **ID**: 1
- **Nombre**: "Centro Comercial Plaza Central"

## Base de Datos H2 Console

Acceder a: http://localhost:8081/h2-console

- **JDBC URL**: `jdbc:h2:mem:plazappdb`
- **Username**: `sa`
- **Password**: (vacío)

