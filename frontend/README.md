# Auto Policy Management — Frontend

SPA en **React + TypeScript + Vite** que consume la API de Spring Boot (clientes y pólizas).

## Stack

- **Vite + React 18 + TypeScript** — base del proyecto.
- **React Router** — navegación (listados, formularios, login).
- **TanStack Query (React Query)** — fetching, caché e invalidación.
- **Axios** — cliente HTTP con interceptor que añade el header de HTTP Basic.

## Requisitos

- **Node.js 18+** y npm.

## Puesta en marcha

```bash
cd frontend
npm install
npm run dev
```

Abre `http://localhost:5173`.

> El backend debe estar corriendo en `http://localhost:8080` (perfil dev: `./mvnw spring-boot:run`).
> El origen `http://localhost:5173` ya está permitido en el CORS del backend.

### Login

Usa las credenciales del backend:

- **Dev (H2):** `admin` / `admin123`
- **Docker (Postgres):** `admin` / `change-me-locally`

## Configuración

La URL de la API se lee de `VITE_API_BASE_URL` (ver `.env.development`). Para producción
crea un `.env.production` con la URL del backend desplegado.

## Estructura

```
src/
├── api/         # cliente axios, tipos (espejo de los DTOs) y funciones por recurso
├── auth/        # AuthContext: login/logout con HTTP Basic
├── components/  # Layout y ProtectedRoute
└── pages/       # Login, Clientes (lista/form), Pólizas (lista/form)
```

## Notas

- La autenticación es **HTTP Basic**: las credenciales se guardan en `localStorage` y se
  envían en cada request. Es lo que expone el backend hoy; migrar a **JWT** sería el
  siguiente paso natural en ambos lados.
- En un `401` el interceptor limpia las credenciales y redirige al login.
