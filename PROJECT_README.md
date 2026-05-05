# TropelCare Signal Engine

Backend del hackathon DBP — Spring Boot 3 + Java 21 + PostgreSQL.

> El enunciado oficial vive en `Readme.md`. Este archivo es el README del entregable.

## Integrantes

- _Nombre completo 1_ — _Codigo UTEC_
- _Nombre completo 2_ — _Codigo UTEC_
- _Nombre completo 3_ — _Codigo UTEC_

## Levantar PostgreSQL

```bash
docker run --name tropelcare-db \
  -e POSTGRES_DB=tropelcare \
  -e POSTGRES_USER=tropeluser \
  -e POSTGRES_PASSWORD=tropelpass \
  -p 5432:5432 \
  -d postgres:16
```

## Variables de entorno requeridas

Crear `.env` en la raiz (ya en `.gitignore`):

```
DB_HOST=localhost
DB_PORT=5432
DB_NAME=tropelcare
DB_USERNAME=tropeluser
DB_PASSWORD=tropelpass

GITHUB_TOKEN=<token con permisos de Models>
GITHUB_MODELS_URL=https://models.inference.ai.azure.com
MODEL_ID=gpt-4o-mini

MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=<correo del equipo>
MAIL_PASSWORD=<app password de 16 caracteres>

ADMIN_NAME=Cameron Walker
ADMIN_EMAIL=cameron@tuckersoft.com
ADMIN_NOTIFICATION_EMAIL=<correo real al que llegan las alertas>
```

`spring.config.import=optional:file:.env[.properties]` carga el `.env` automaticamente.

## Ejecutar la aplicacion

```bash
./mvnw spring-boot:run
```

App escuchando en `http://localhost:8080`. Al arrancar el `DataInitializer` crea a Cameron Walker como guardian si no existe.

## Correr los tests

```bash
./mvnw test
```

7 tests unitarios (Mockito puro, sin PostgreSQL ni red ni SMTP).

## Flujo asincrono

`POST /api/v1/signals` ejecuta dentro de la transaccion del `SignalService`:

1. Valida request, verifica que `request.guardianId` coincida con `tropel.guardian.id`.
2. Llama a la IA (GitHub Models). Si responde JSON valido con tipos en las listas permitidas, actualiza stats del Tropel y `stabilityLevel` del Sector cuando aplica.
3. Guarda `TropelSignal` con `status = RECIBIDA` y crea `CareResponse`.
4. Publica `TropelSignalCreatedEvent` via `ApplicationEventPublisher`. **Solo se publica cuando la IA fue exitosa.**
5. Retorna **201 inmediatamente**.

Si la IA falla (timeout, JSON invalido, valores fuera de lista) el service guarda la senal con valores fallback y `status = ERROR`, **no publica el evento**, y aun asi retorna 201.

`TropelSignalNotificationListener` (clase `@Component` separada) recibe el evento con `@TransactionalEventListener(phase = AFTER_COMMIT)` + `@Async("tropelExecutor")` + su propio `@Transactional`. Solo se ejecuta despues de que PostgreSQL confirma la transaccion. Pasa la senal a `PROCESANDO`, intenta enviar el correo via `JavaMailSender`, y deja:

- Exito: `signal.status = ATENDIDA`, `NotificationLog` con `notifStatus = SENT`.
- Fallo SMTP: `signal.status = ERROR`, `NotificationLog` con `notifStatus = FAILED` y `errorMessage`.

En consola siempre imprime:

```
[TROPEL-LOG] Signal ID: 5 | Tropel: BipBop | Type: HAMBRE | Severity: MODERADO | Unit: Laboratorio de Nutricion | Thread: tropel-worker-1 | Status: ATENDIDA
```

El executor (`AsyncConfig`) es un `ThreadPoolTaskExecutor` con `corePoolSize=2`, `maxPoolSize=4`, `queueCapacity=50`, `threadNamePrefix="tropel-worker-"`. `SignalService` no inyecta `JavaMailSender` ni el listener — el desacople va solo por el evento.

## Endpoints

| Metodo | Ruta | Descripcion |
|:-------|:-----|:------------|
| GET | `/api/v1/guardians` | Lista guardianes |
| GET | `/api/v1/guardians/{id}` | Detalle de guardian |
| POST | `/api/v1/sectors` | Crea sector |
| GET | `/api/v1/sectors` | Lista sectores |
| GET | `/api/v1/sectors/{id}` | Detalle de sector |
| POST | `/api/v1/tropels` | Registra Tropel |
| GET | `/api/v1/tropels` | Lista Tropeles (filtros: `species`, `vitalState`, `sectorId`, `guardianId`, `page`, `size`) |
| GET | `/api/v1/tropels/{id}` | Detalle de Tropel |
| GET | `/api/v1/tropels/{id}/diary` | (Bonus) notas de personalidad |
| POST | `/api/v1/signals` | Registra senal — dispara la cadena completa |
| GET | `/api/v1/signals` | Lista senales (filtros: `signalType`, `severity`, `status`, `tropelId`, `guardianId`, `from`, `to`, `page`, `size`) |
| GET | `/api/v1/signals/{id}` | Detalle de senal |
| GET | `/api/v1/signals/{id}/care-response` | Respuesta de cuidado |
| GET | `/api/v1/signals/{id}/notifications` | Logs de notificacion |
