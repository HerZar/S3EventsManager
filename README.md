# MongoDB Docker Setup

## Pasos para configurar MongoDB con Docker

### 1. Crear la imagen de MongoDB
```bash
cd docker/mongodb
docker build -t mongodb-8.2.4 .
```

### 2. Crear y ejecutar el contenedor en modo detach
```bash
docker run -d --name mongodb-8.2.4 -p 27028:27028 -v $(pwd)/data:/data/db mongodb-8.2.4
```

O usando docker-compose:
```bash
cd docker/mongodb
docker-compose up -d
```

### 3. Configurar proyecto Spring Boot para conectarse a MongoDB

#### application.properties
```properties
# MongoDB Configuration
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27028
spring.data.mongodb.database=events
spring.data.mongodb.username=events_user
spring.data.mongodb.password=events_password
```

#### application.yml (alternativa)
```yaml
spring:
  data:
    mongodb:
      host: localhost
      port: 27028
      database: events
      username: events_user
      password: events_password
```

#### Dependencia Maven (pom.xml)
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
```

#### Ejemplo de configuración en código Java
```java
@Configuration
public class MongoConfig {

    @Value("${spring.data.mongodb.host}")
    private String host;

    @Value("${spring.data.mongodb.port}")
    private int port;

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Value("${spring.data.mongodb.username}")
    private String username;

    @Value("${spring.data.mongodb.password}")
    private String password;

    @Bean
    public MongoClient mongoClient() {
        String connectionString = String.format("mongodb://%s:%s@%s:%d/%s",
                username, password, host, port, database);
        
        return MongoClients.create(connectionString);
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), database);
    }
}
```

## Verificación de conexión
Para verificar que MongoDB está funcionando correctamente:
```bash
# Conectar como administrador
docker exec -it mongodb-8.2.4 mongosh --port 27028 -u admin -p password --authenticationDatabase admin

# Conectar a la base de datos events
docker exec -it mongodb-8.2.4 mongosh --port 27028 -u events_user -p events_password --authenticationDatabase events
```