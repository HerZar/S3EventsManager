# S3 Events Manager API

## Descripción de la funcionalidad

API RESTful para gestionar eventos de S3, permitiendo almacenar y consultar eventos de objetos en buckets de S3. La API permite registrar eventos tipo OBJECT_CREATED, OBJECT_UPDATED y OBJECT_DELETED, y los publica en una cola SQS para procesamiento asíncrono.



## Prerequisitos

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

Esto creará un contenedor MongoDB 8.2.4 en el puerto 27028 con persistencia de datos.

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

Para configurar AWS SQS localmente:

1. **Crear cuenta personal en AWS**
   - Regístrate en [AWS Console](https://aws.amazon.com/console/)

2. **Crear Queue en Simple Queue Service**
   - Ve a SQS (Simple Queue Service)
   - Crea una nueva cola
   - Anota el URL de la cola (ej: `https://sqs.us-east-2.amazonaws.com/TU_ACCOUNT_ID/s3-events`)

3. **Configurar IAM (Identity and Access Management)**
   - Crea un grupo de usuarios en IAM
   - Crea un usuario y asócialo al grupo
   - Genera una clave de acceso con permisos para uso local
   - Obtendrás un `access-key` y `secret-key`

4. **Configurar en application.properties**
   ```properties
   # AWS SQS Configuration
   aws.sqs.queue-url=https://sqs.<region>.amazonaws.com/TU_ACCOUNT_ID/s3-events
   aws.access-key=TU_ACCESS_KEY
   aws.secret-key=TU_SECRET_KEY
   aws.region=<region>
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
```

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
  - Ejemplo: `GET /events/my-bucket?page=0&size=10`

- **POST /events** - Crear un nuevo evento
  - Body: JSON con el evento S3
  - Ejemplo: 
    ```json
    {
      "bucketName": "my-bucket",
      "objectKey": "path/to/file.txt",
      "type": "OBJECT_CREATED",
      "time": "2024-01-25T14:00:00Z",
      "objectSize": 1024
    }
    ```

## Facilidad de ejecución

Para facilitar la configuración y ejecución del proyecto, se ha incluido un archivo `docker-compose.yml` en la carpeta `docker/mongodb` que permite levantar rápidamente la base de datos MongoDB necesaria sin necesidad de configuración manual.

Este docker-compose:
- **Crea automáticamente** el contenedor MongoDB 8.2.4
- **Configura el puerto 27028** para evitar conflictos con instancias locales
- **Establece credenciales por defecto** (`events_user`/`events_password`)
- **Mapea volumen persistente** para mantener los datos entre reinicios
- **Inicializa la base de datos** `events` automáticamente

Con esto, cualquier desarrollador puede tener el entorno de base de datos listo en segundos con un solo comando, eliminando barreras de configuración y permitiendo enfocarse en el desarrollo de la API.

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