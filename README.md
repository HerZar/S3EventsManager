# S3 Events Manager API

## Descripción de la funcionalidad

API RESTful para gestionar eventos de S3, permitiendo almacenar y consultar eventos de objetos en buckets de S3. La API permite registrar eventos tipo OBJECT_CREATED, OBJECT_UPDATED y OBJECT_DELETED, y los publica en una cola SQS para procesamiento asíncrono.



## Prerrequisitos

Para ejecutar esta aplicación, necesitas tener instalados:

### Tecnologías requeridas
- **Java 11+**: JDK para ejecutar la aplicación Spring Boot
- **Maven**: Para compilar y gestionar dependencias del proyecto
- **Docker**: Para ejecutar MongoDB en contenedor
- **Docker Compose**: Para orquestar los servicios Docker

### Herramientas opcionales
- **Postman/Insomnia**: Para probar los endpoints de la API
- **MongoDB Compass**: Para explorar la base de datos (opcional)

### Cuentas y servicios
- **Cuenta AWS**: Para configurar SQS (ver sección AWS SQS)
- **Credenciales AWS**: Access Key y Secret Key con permisos SQS

## Base de datos

### MongoDB con Docker Compose

Para generar el contenedor Docker con MongoDB usando docker-compose:
Desde el directorio raíz del proyecto, ejecuta:

```bash
docker-compose -f docker/mongodb/docker-compose.yml up -d
```

Esto creará un contenedor mongodb a partir de la imagen challenge-db en el puerto 27028 con persistencia de datos.

### PARA BORRAR CONTENEDOR Y DATA
Para contenedor e imagen docker ejecuta las siguientes sentencias en la terminal
```
docker rm challenge-db
docker rmi challenge-db-image
```

### Configuración de MongoDB

La configuración se realiza automáticamente a través de `application.properties`:

```properties
# MongoDB Configuration
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27028
spring.data.mongodb.database=events
spring.data.mongodb.username=events_user
spring.data.mongodb.password=events_password
```

La aplicación crea automáticamente un índice compuesto único para optimizar las consultas por bucketName, objectKey, type y time.

## AWS SQS

### Configuración para uso local

Para configurar AWS SQS paso a paso:

1. **Crear cuenta personal en AWS**
 - Regístrate en [AWS Console](https://aws.amazon.com/console/)

2. **Crear Queue en Simple Queue Service**
 - Ve dentro de la consola de AWS a services -> SQS (Simple Queue Service)
 - Click en Crear una cola
 - Tipo standar -> set nombre -> configuracion por defecto ->  clic en Crear una cola.
 - Anota el URL de la cola (ej: `https://sqs.us-east-2.amazonaws.com/TU_ACCOUNT_ID/s3-events`)
 - También anota la region.

3. **Configurar IAM (Identity and Access Management) Grupo de personas** 
 - Ve dentro de la consola de AWS a services -> IAM (Identity and Access Management)
 - Clic en el menú izquierdo sobre Administración del acceso en Grupos de personas.
 - Clic en crear un grupo -> establecemos un nombre, y en Asociar políticas de permisos asociar seleccionar AmazonSQSFullAccess
 - Clic en crear grupo de personas.

4. **Configurar IAM (Identity and Access Management) personas**
  - Clic en el menú izquierdo sobre Administración del acceso en personas.
  - Clic en crear persona -> establecemos un nombre de usuario -> clic siguiente.
  - Dejamos marcado Agregar persona al grupo y seleccionamos el grupo anteriormente creado. -> clic en siguiente.
  - clic en crear persona.
  - Una vez creada la persona se puede ingresar a su perfil haciendo click sobre su nombre en el menu personas.
  - Dentro del perfil de la persona hacer clic en Crear clave de acceso.
  - Seleccionamos el caso de uso Código local para efectos de esta prueba, y hacemos clic en siguiente.
  - Clic en crear clave de acceso.
  - Luego se mostrará la clave de acceso (access key) y enmascarada la clave de acceso secreta.
  - es necesario guardar ambas claves cuidadosamente para configurar el servicio.

5. **Configurar en application.properties**
 - Configurar las siguientes variables del application.properties del proyecto con los valores obtenidos en los pasos anteriores.
   ```properties
   # AWS SQS Configuration
    aws.sqs.queue-url=https://sqs.<region>.amazonaws.com/TU_ACCOUNT_ID/s3-events
    aws.access-key=TU_ACCESS_KEY
    aws.secret-key=TU_SECRET_KEY
    aws.region=<region>

    # AWS Cloud Configuration (disable EC2 metadata detection)
    cloud.aws.region.static=<region>
    cloud.aws.credentials.instance-profile=false
   ```

## Ejecución de la API

Luego de tener disponible la base de datos, la cola SQS y las credenciales obtenidas, sigue estos pasos:

### 1. Configurar application.properties

Configura el archivo `src/main/resources/application.properties` como se describe en las secciones anteriores:

```properties
# MongoDB Configuration
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27028
spring.data.mongodb.database=events
spring.data.mongodb.username=events_user
spring.data.mongodb.password=events_password

# AWS SQS Configuration
aws.sqs.queue-url=https://sqs.<region>.amazonaws.com/TU_ACCOUNT_ID/s3-events
aws.access-key=TU_ACCESS_KEY
aws.secret-key=TU_SECRET_KEY
aws.region=<region>

# AWS Cloud Configuration (disable EC2 metadata detection)
cloud.aws.region.static=<region>
cloud.aws.credentials.instance-profile=false
```

La configuración de base de datos viene configurada por defecto.
Debe reemplazar los campos de aws con la url y región de la cola y el client y secret keys del usuario configurado. 

### 2. Compilar el proyecto

Desde el directorio raíz del proyecto, ejecuta:

```bash
./mvnw clean install
```

Esto generará el ejecutable JAR en la carpeta `target`.

### 3. Ejecutar la aplicación

Ejecuta el JAR generado:

```bash
java -jar target/challenge-0.0.1-SNAPSHOT.jar
```

### 4. Acceder a la API

El servicio estará disponible en:

- **URL base**: `http://localhost:8080`

#### Endpoints disponibles:

- **GET /events/{bucketName}** - Obtener eventos de un bucket
  - Parámetros: `page` (número de página), `size` (tamaño de página)
  - Ejemplo: 

``` 
curl --location 'http://localhost:8080/api/v1/s3-events/zonda-data-bucket?page=0&size=100'
```

- **POST /events** - Crear un nuevo evento
  - Body: JSON con el evento S3
    - Ejemplo: 
      ```json
      {
            "bucketName": "zonda-data-bucket",
            "objectKey": "reports/daily/report_2024-01-33.csv",
            "eventType": "OBJECT_UPDATED",  
            "eventTime": "2025-07-30T14:04:00Z",
            "objectSize": 2048
      }
      ```
      eventType solo permite los siguientes valores
    - OBJECT_CREATED
    - OBJECT_UPDATED
    - OBJECT_DELETED

```
curl --location 'http://localhost:8080/api/v1/s3-events' \
--header 'Content-Type: application/json' \
--data '{
"bucketName": "zonda-data-bucket",
"objectKey": "reports/daily/report_2024-01-33.csv",
"eventType": "OBJECT_UPDATED",
"eventTime": "2025-07-30T14:04:00Z",
"objectSize": 2048
}'
```

## Decisiones de diseño

### Arquitectura Hexagonal

Se diseñó el servicio utilizando una arquitectura hexagonal (ports and adapters), que brinda los siguientes beneficios:

- **Desacoplamiento**: La lógica de negocio está completamente separada de la infraestructura
- **Testabilidad**: Facilita la creación de mocks y pruebas unitarias
- **Flexibilidad**: Permite cambiar tecnologías sin modificar el dominio
- **Mantenibilidad**: Cada capa tiene responsabilidades claras y definidas

### Patrón Strategy

Se implementó el patrón Strategy en la verificación de existencia de eventos:

- **DefaultExistenceStrategy**: Para eventos OBJECT_CREATED y OBJECT_DELETED
- **UpdatedExistenceStrategy**: Para eventos OBJECT_UPDATED (requiere verificación por fecha)
- **ExistenceStrategyFactory**: Selecciona la estrategia adecuada según el tipo de evento

Esto permite extender fácilmente nuevos tipos de verificación sin modificar el código existente.

### Patrón Retry

Se utilizó retry como patrón de resiliencia para evitar fallos al enviar mensajes a SQS:

- **3 intentos**: Reintenta hasta 3 veces en caso de fallo
- **Delay de 1 segundo**: Espera 1 segundo entre cada intento
- **Backoff fijo**: Utiliza delay constante entre reintentos
- **Manejo de agotamiento**: Lanza excepción específica cuando se agotan los intentos

Esto proporciona resiliencia ante fallos temporales de red o de AWS SQS.


# Escalabilidad e idempotencia (respuesta teórica)
## Estrategias para Identificar Mensajes Duplicados 
### Message Deduplication ID

- Generar identificador único usando algoritmo hash (SHA-256) sobre campos clave del mensaje
- Composición: hash(event_type + resource_id + timestamp + payload_hash)
- Ventaja: Control total sobre identificación, permite duplicados fuera de ventana de 5 min
- Desventaja: Requiere lógica adicional para generar y validar Content-Based Deduplication

### SQS FIFO queue calcula hash SHA-256 del body del mensaje automáticamente
- Elimina duplicados con mismo contenido dentro de ventana de 5 minutos
- Ventaja: Sin código adicional, manejado por AWS
- Desventaja: Ventana fija de 5 min, solo funciona con contenido idéntico.

## Patrones para Procesamiento Exactamente Una Vez
### Idempotency Key
-  Campo único en cada mensaje que identifica la operación de forma única
- Ejemplos: request_id, correlation_id, transaction_id
- Implementación: tabla de tracking con clave primaria y estado
- Flujo: verificar existencia → procesar si no existe → marcar como completado

### Write-Ahead Log
- Registrar intención de procesar antes de ejecutar operación principal
- Estructura: idempotency_key + status + timestamp + payload
- Estados: PENDING, PROCESSING, COMPLETED, FAILED
- Permite recuperación después de caídas y evita duplicados 

### Saga Pattern
- Coordinación de transacciones distribuidas con compensación
- Cada paso: ejecutar acción local + publicar evento
- Si falla: ejecutar acciones de compensación en orden inverso
- Tipos: Choreography (eventos) vs Orchestration (coordinador central)

## Identificadores Únicos y Restricciones
### Índices Únicos Compuestos
- Restricción a nivel de base de datos en múltiples columnas
- Ejemplo: UNIQUE(user_id, order_id, event_type, created_at)
- Motor DB rechaza duplicados a nivel de índice
- Implementación: CREATE UNIQUE INDEX idx_event_unique ON events(user_id, order_id, event_type)

### Optimistic Locking
- Campo version que se incrementa en cada actualización
- Flujo: leer versión actual → procesar → actualizar si versión no cambió
- SQL: UPDATE events SET data=?, version=version+1 WHERE id=? AND version=?
- Si affected_rows=0: otro proceso modificó

### Distributed Locks
- Implementación con Redis: SET lock_key resource_id NX PX 30000
- NX: solo si no existe, PX: TTL de 30 segundos
- Liberación: script Lua para verificar dueño antes de eliminar
- Previene procesamiento concurrente del mismo recurso
