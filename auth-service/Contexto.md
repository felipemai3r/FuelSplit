Estou implementando o auth-service do projeto carona-tracker, um sistema de
divisão de custos de combustível entre caroneiros. É um trabalho acadêmico de
microsserviços — Java 21 + Spring Boot 3.5.14 + Maven.

O repositório já existe em https://github.com/diogoruan/carona-tracker.
Meu trabalho é substituir o placeholder da pasta auth-service com a
implementação real, mantendo a estrutura de pastas e o Dockerfile existentes.

## Ambiente (docker-compose do repositório)

Rede interna: carona-network
Todos os serviços se comunicam pelo nome do container como hostname.

auth-service:
  porta: 8081 (externa e interna)
  banco: postgres-auth (container), banco authdb, user auth_user, pass auth_pass
  variáveis de ambiente disponíveis: DB_HOST, DB_NAME, DB_USER, DB_PASS

RabbitMQ:
  host: rabbitmq (nome do container)
  porta: 5672
  user: guest / pass: guest

O application.yml deve usar as variáveis de ambiente do docker-compose:

spring:
  application:
    name: auth-service
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:5432/${DB_NAME:authdb}
    username: ${DB_USER:auth_user}
    password: ${DB_PASS:auth_pass}
    driver-class-name: org.postgresql.Driver
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: update
    show-sql: true

server:
  port: 8081

jwt:
  secret: fuelsplit-secret-key-256bits-minimum-required-here
  expiration: 86400000

springdoc:
  swagger-ui:
    path: /swagger-ui.html

## Dependências a adicionar no pom.xml

JJWT:
  io.jsonwebtoken:jjwt-api:0.12.6
  io.jsonwebtoken:jjwt-impl:0.12.6 (scope runtime)
  io.jsonwebtoken:jjwt-jackson:0.12.6 (scope runtime)

Springdoc OpenAPI:
  org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0

## Estrutura de pacotes

com.fuelsplit.auth
├── config/
│   ├── SecurityConfig.java
│   └── SwaggerConfig.java
├── controller/
│   └── AuthController.java
├── dto/
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   └── AuthResponse.java
├── entity/
│   └── AuthUser.java
├── exception/
│   ├── EmailAlreadyExistsException.java
│   └── GlobalExceptionHandler.java
├── repository/
│   └── AuthUserRepository.java
├── security/
│   ├── JwtService.java
│   └── JwtAuthFilter.java
└── service/
    └── AuthService.java

## Entidade AuthUser

Tabela: auth_users
Campos:
  id         → UUID, gerado automaticamente
  email      → String, único, não nulo
  passwordHash → String, não nulo
  role       → String, default "USER"
  createdAt  → Instant, gerado automaticamente na criação

## Endpoints

POST /auth/register
  Body: { "email": "...", "password": "..." }
  Valida: email único, senha mínimo 6 caracteres
  Salva senha com BCrypt
  Retorna: { "token": "...", "email": "..." }

POST /auth/login
  Body: { "email": "...", "password": "..." }
  Valida credenciais
  Retorna: { "token": "...", "email": "..." }

GET /auth/validate
  Header: Authorization: Bearer <token>
  Retorna 200 + { "email": "...", "role": "..." } se válido
  Retorna 401 se inválido ou expirado
  Esse endpoint será chamado internamente pelo Nginx (API Gateway)

## Regras de negócio

RN01 - Email deve ser único. Registro com email existente → 409 + { "error": "..." }
RN02 - Senha mínimo 6 caracteres. Senha inválida → 400 + { "error": "..." }
RN03 - Senha nunca armazenada em texto plano. Sempre BCryptPasswordEncoder.
RN04 - Token JWT expira em 24h (86400000ms).
RN05 - /auth/register e /auth/login são públicos (sem autenticação).
RN06 - /auth/validate é público (chamado pelo gateway, sem token próprio).
RN07 - Qualquer outro endpoint exige token válido no header Authorization.

## Claims do JWT

O token deve conter:
  sub: email do usuário
  role: papel do usuário (ex: "USER")
  exp: timestamp de expiração

## Tratamento de erros

GlobalExceptionHandler (@RestControllerAdvice) tratando:
  EmailAlreadyExistsException → 409
  IllegalArgumentException    → 400
  BadCredentialsException     → 401
  Exception (genérica)        → 500

Sempre retornar: { "error": "mensagem descritiva" }

## Swagger

Disponível em: /swagger-ui.html
SwaggerConfig com:
  título: "Carona Tracker - Auth Service"
  descrição: "Serviço de autenticação JWT"
  versão: "1.0"
Endpoints documentados com @Operation e @ApiResponse

## Testes unitários (AuthServiceTest)

Usar JUnit 5 + Mockito. Mockar AuthUserRepository e JwtService.
Cobrir:
  - registro com sucesso
  - registro com email duplicado (deve lançar EmailAlreadyExistsException)
  - registro com senha curta (deve lançar IllegalArgumentException)
  - login com sucesso
  - login com senha errada (deve lançar BadCredentialsException)
  - login com email inexistente (deve lançar BadCredentialsException)

## Observações técnicas

- Usar Records para DTOs (LoginRequest, RegisterRequest, AuthResponse)
- Usar SecurityFilterChain (API moderna — não usar WebSecurityConfigurerAdapter)
- JwtAuthFilter deve estender OncePerRequestFilter
- O Dockerfile já existe na pasta auth-service e não deve ser alterado
- Não criar docker-compose — já existe na raiz do projeto
- Não alterar estrutura de pastas além do código Java e application.yml