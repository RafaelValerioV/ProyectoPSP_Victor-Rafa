# DrivePSP - Instrucciones de Instalación para uso del proyecto en red local
## Requisitos previos

- Java 17 instalado
- Node.js y npm instalados
- Docker y Docker Compose funcionando
- Maven configurado en el sistema

---

## Paso 1: Base de Datos (Docker)
Desde la raíz del proyecto donde se encuentra el archivo `docker-compose.yml`, ejecutar:

```bash
docker-compose up -d
```
Nota: Se levantará PostgreSQL 16 en el puerto 5437.

## Paso 2: Backend (Spring Boot)
El certificado SSL no se sube a Git por seguridad, por lo que hay que generarlo antes de arrancar.

Primero hay que obtener la IP local:
```bash
ipconfig
```
Busca la dirección IPv4 del adaptador de red (por ejemplo 192.168.1.X).

Luego, navega a la carpeta del backend (`cd drivepsp-backend`) y ejecuta:
```bash
mkdir certs
keytool -genkeypair -alias servidor -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore certs/servidor.p12 -validity 365 -storepass 123456 -dname "CN=PONER_IPv4, OU=DrivePSP, O=DrivePSP, L=Madrid, ST=Madrid, C=ES" -ext "SAN=ip:PONER_IPv4,ip:127.0.0.1,dns:localhost"
.\mvnw clean install
.\mvnw spring-boot:run
```
Sustituye `PONER_IPv4` por tu dirección IPv4 local en ambos sitios del comando keytool.

Nota: Puedes cambiar la contraseña del certificado cambiando el argumento de -storepass, pero deberás actualizar también el valor de keystore-password en `drivepsp-backend\src\main\resources\application.yml`
El servidor arranca en HTTPS en el puerto 8443.

## Paso 3: Frontend (React)
Navegar a la carpeta del frontend (`cd drivepsp-frontend`) y ejecutar:
```bash
npm install
npm run dev
```
El frontend arranca en HTTPS en el puerto 5173.

## Acceso desde otros ordenadores de la red local
Una vez levantado el proyecto, desde cualquier PC de la misma red se puede acceder a:
```
https://PONER_IPv4:5173
```
La primera vez el navegador mostrará un aviso de certificado autofirmado. Pulsa Avanzado -> Continuar para aceptarlo. No es necesario instalar ningún certificado en los equipos clientes.
