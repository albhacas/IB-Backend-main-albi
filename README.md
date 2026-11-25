# Configuración de autenticación

La API requiere un JWT firmado para acceder a sus endpoints. Si no se define
`JWT_SECRET`, el backend genera un secreto aleatorio y lo guarda en
`%USERPROFILE%\.iterbus\jwt-secret` (Windows) o `~/.iterbus/jwt-secret` (Linux/macOS).
Así se conserva entre reinicios sin configuración manual. En Docker Compose se
persiste en el volumen `jwt_data`.

## Ejecución local

Arrancá el backend normalmente con `mvn spring-boot:run` o
`docker compose up --build`; el secreto se crea automáticamente. Para producción,
configurá un `JWT_SECRET` aleatorio de al menos 32 bytes mediante el gestor de
secretos del entorno y no lo guardes en el repositorio.

## Primer acceso

Cuando la base de datos no tiene usuarios, el frontend ofrece crear la primera
cuenta. Esa cuenta recibe el rol `GERENTE_ADMINISTRATIVO`; una vez creada, el
registro inicial queda cerrado y se ingresa con correo y contraseña.

`POST /api/auth/login` devuelve un token JWT, su hora de expiración y el perfil
del usuario. El token vence a los 5 minutos y debe enviarse como `Authorization: Bearer <token>` en los
demás endpoints `/api/**`. El frontend guarda el token en `sessionStorage` y lo
elimina al cerrar sesión o al expirar.
