# 🏢 Kodehaus App - Stock Platform

Plataforma de gestión de stocks construida con **Spring Boot** (backend) y **Angular** (frontend), pensada para desplegarse en **Google Cloud Platform (GCP)**.

## 🏗️ Arquitectura

- **Backend**: Spring Boot + Java 21 + CockroachDB (compatibilidad PostgreSQL)
- **Frontend**: Angular 17 + TypeScript
- **Cloud**: Google Cloud Platform
  - Cloud Run (Backend)
  - Cloud Storage (Frontend)
  - Artifact Registry (Imágenes Docker)
- **CI/CD**: GitHub Actions

## ✅ Requisitos previos

Antes de iniciar el desarrollo, cada persona del equipo debe contar con:

- **Java 21** y **Maven Wrapper** (`./mvnw` ya incluido).
- **Node.js 18+** y **npm**.
- **Angular CLI 17+** (`npm install -g @angular/cli`).
- Acceso a internet para consumir CockroachDB y descargar dependencias.
- (Opcional) Cliente `cockroach` o cualquier cliente PostgreSQL para inspeccionar la base.

## 🌱 Preparar el entorno local

1. **Clonar el repositorio base**
   ```bash
   git clone <url-del-repo>
   cd kodehaus_app
   ```
2. **Crear una rama a partir de la base** (usa un nombre descriptivo)
   ```bash
   git checkout -b feature/<mi-feature>
   ```
3. **Configurar las variables de entorno** que usará Spring Boot. Se recomienda crear un archivo `.env` o exportarlas en la terminal antes de arrancar el backend.

   | Variable | Descripción |
   |----------|-------------|
   | `APP_ENV` | Entorno lógico (ej. `prod`, `test`, `local`). |
   | `SERVER_PORT` | Puerto HTTP del backend. |
   | `SPRING_DATASOURCE_URL` | URL JDBC hacia CockroachDB/PostgreSQL. |
   | `SPRING_DATASOURCE_USERNAME` | Usuario de la base. |
   | `SPRING_DATASOURCE_PASSWORD` | Contraseña de la base. |

> 💡 Puedes guardar estas variables en un archivo `stocks-backend/.env` y cargarlas con herramientas como [direnv](https://direnv.net/) o `source .env`.

## 🛠️ Desarrollo local

### Backend

1. Instala las dependencias (solo la primera vez):
   ```bash
   cd bknd
   ./mvnw dependency:go-offline
   ```
2. Ejecuta la aplicación apuntando al entorno configurado:
   ```bash
   ./mvnw spring-boot:run
   ```
3. Verifica el endpoint de salud (útil para despliegues y CI/CD):
   ```bash
   curl http://localhost:${SERVER_PORT:-8080}/health
   ```

El backend expone la API REST para gestionar stocks y un endpoint `/health` que valida conectividad con la base de datos.

### Frontend

1. Instala dependencias:
   ```bash
   cd frnt
   npm install
   ```
2. Ajusta el archivo `src/environments/environment.ts` si necesitas apuntar a otro backend (por defecto usa `http://localhost:8080`).
3. Levanta el servidor de desarrollo:
   ```bash
   npm start
   ```
4. Abre `http://localhost:4200` en tu navegador.

## 🔄 Flujo de trabajo recomendado

1. Asegúrate de tener la rama actualizada con `main` (`git pull origin main`).
2. Desarrolla tus cambios en tu rama feature y agrega tests si corresponde.
3. Haz commit de tus cambios y sube tu rama:
   ```bash
   git add .
   git commit -m "feat: describe tu cambio"
   git push origin feature/<mi-feature>
   ```
4. Crea un Pull Request en GitHub. El pipeline de CI/CD validará el `/health` y los builds.

## 🚀 Deployment

Cuando los cambios se fusionan en `main`:

- `bknd/` se despliega automáticamente a Cloud Run.
- `frnt/` se publica en Cloud Storage.

Cada despliegue usa el endpoint `/health` para validar la aplicación antes de exponerla.

## 🤝 Soporte y buenas prácticas

- Mantén las credenciales seguras; no las expongas en issues ni commits.
- Usa el sandbox `test` para experimentar sin afectar producción.
- Documenta en el PR cualquier configuración adicional que requiera el equipo.
