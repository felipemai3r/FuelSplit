# Trips & Charges Service

Microsserviço responsável pelo registro de viagens e divisão automática de custos de combustível entre participantes do grupo.

Desenvolvido por **Diogo Ruan** como parte do trabalho prático das disciplinas de **DevOps** e **Dispositivos Móveis** — UDESC Alto Vale, 2026/1.

---

## Sobre o Serviço

O `trips-charges-service` é um dos microsserviços da aplicação **FuelSplit / Carona Tracker**, que permite que grupos de pessoas registrem viagens compartilhadas e dividam automaticamente os custos de combustível.

### Responsabilidades

- Registro de viagens com cálculo automático de custo baseado nos parâmetros do grupo
- Divisão do custo entre os participantes presentes na viagem
- Histórico de viagens por grupo
- Gerenciamento de pendências de pagamento (aberto/pago)
- Publicação de eventos via RabbitMQ para atualização de saldos no serviço de Usuários/Grupos

### Regras de Negócio

1. **Cálculo automático de custo** — o custo é calculado com base nos parâmetros do grupo: `(distância / km_por_litro) × preço_combustível`
2. **Validação de data** — uma viagem não pode ser registrada com data futura
3. **Cobrança por presença** — um passageiro só é cobrado se estava presente naquela viagem

---

## Endpoints

| Método | Rota | Descrição |
|--------|------|-----------|
| `POST` | `/api/trips` | Registrar uma nova viagem |
| `GET` | `/api/trips` | Listar viagens (filtro por grupo opcional) |
| `GET` | `/api/trips/{id}` | Buscar viagem por ID |
| `GET` | `/api/trips/debts/{userId}` | Listar dívidas em aberto de um usuário |
| `PATCH` | `/api/trips/participants/{participantId}/pay` | Marcar participante como pago |

### Documentação Swagger

- **DEV:** https://trips-charges-service-dev.onrender.com/swagger-ui.html
- **HOMOL:** Swagger desabilitado (ambiente de homologação)

---

## Arquitetura

```
trips-charges-service/
├── config/          # Configurações RabbitMQ e OpenAPI/Swagger
├── controller/      # Endpoints REST (TripController)
├── domain/
│   ├── enums/       # PaymentStatus (PENDING, PAID)
│   └── model/       # Entidades JPA (Trip, TripParticipant)
├── dto/             # Objetos de transferência (Request/Response)
├── exception/       # BusinessException e GlobalExceptionHandler
├── messaging/       # TripEventPublisher (RabbitMQ)
├── repository/      # TripRepository, TripParticipantRepository
└── service/         # TripService (regras de negócio)
```

### Padrões utilizados

- **Arquitetura em camadas** — Controller → Service → Repository
- **Event-Driven** — publicação de eventos no RabbitMQ após registro de viagem
- **API Gateway** — todas as requisições passam pelo Nginx (`/api/trips/`)

---

## Stack Tecnológica

| Tecnologia | Versão | Uso |
|-----------|--------|-----|
| Java | 17 | Linguagem principal |
| Spring Boot | 3.3.2 | Framework web |
| Spring Data JPA | - | Persistência |
| Spring AMQP | - | Mensageria RabbitMQ |
| PostgreSQL | 16 | Banco de dados |
| Springdoc OpenAPI | 2.3.0 | Documentação Swagger |
| Lombok | 1.18.32 | Redução de boilerplate |
| JaCoCo | 0.8.11 | Cobertura de testes |

---

## Como Executar Localmente

### Pré-requisitos

- Docker Desktop instalado e rodando
- Git

### Subindo o ambiente completo

```bash
# Clone o repositório
git clone https://github.com/felipemai3r/FuelSplit.git
cd FuelSplit

# Sobe todos os serviços em background
docker-compose up -d

# Acompanha os logs do serviço
docker-compose logs -f trips-charges-service
```

### URLs locais após subir

| Serviço | URL |
|---------|-----|
| Trips & Charges Service | http://localhost:8083 |
| Swagger UI | http://localhost:8083/swagger-ui.html |
| Métricas Prometheus | http://localhost:8083/actuator/prometheus |
| API Gateway (Nginx) | http://localhost:80 |
| RabbitMQ Dashboard | http://localhost:15672 (guest/guest) |
| SonarQube | http://localhost:9000 (admin/admin) |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 (admin/admin) |

### Parando o ambiente

```bash
# Para os containers
docker-compose down

# Para os containers e remove os volumes (dados)
docker-compose down -v
```

---

## Ambientes Cloud

| Ambiente | URL | Branch | Swagger |
|----------|-----|--------|---------|
| DEV | https://trips-charges-service-dev.onrender.com | `develop` | ✅ Habilitado |
| HOMOL | https://trips-charges-service-latest.onrender.com | `main` | ❌ Desabilitado |

---

## CI/CD Pipeline

O pipeline é executado automaticamente via **GitHub Actions** em cada push nas branches `develop` e `main`, apenas quando arquivos do `trips-charges-service/` são alterados.

### Fluxo do Pipeline

```
Push na branch
      ↓
Job 1: Testes e Análise de Qualidade
  - mvn verify (compila + testa + JaCoCo)
  - Envio para SonarCloud
      ↓
Job 2: Build e Publicação Docker
  - Build da imagem Docker
  - Push para Docker Hub (diogoruan/trips-charges-service)
  - Tag: 'dev' (develop) ou 'homol' (main)
      ↓
Job 3: Deploy no Render
  - Webhook aciona deploy automático
  - DEV ou HOMOL conforme a branch
```

### Imagens Docker Hub

```
diogoruan/trips-charges-service:latest
diogoruan/trips-charges-service:dev
```

---

## Qualidade de Código

Análise realizada via **SonarCloud**: https://sonarcloud.io/project/overview?id=trips-charges-service

| Métrica | Resultado |
|---------|-----------|
| Quality Gate | ✅ Passed |
| Cobertura de testes | 69.8% (mínimo exigido: 50%) |
| Duplicações | 0.0% |
| Security Rating | A |

### Cobertura por camada

| Pacote | Cobertura |
|--------|-----------|
| service | 88% |
| dto | 69% |
| domain.model | 68% |
| controller | 67% |
| exception | 41% |

---

## Segurança

- **Dependabot** ativo — monitora vulnerabilidades nas dependências semanalmente
- **GitHub Secrets** — credenciais nunca expostas no código
- **Swagger desabilitado no HOMOL** — documentação da API não exposta em produção

---

## Observabilidade

O serviço expõe métricas no padrão Prometheus via Spring Boot Actuator:

```
http://localhost:8083/actuator/prometheus
```

Métricas disponíveis:
- Requisições HTTP (taxa, tempo de resposta, erros)
- Uso de memória JVM (heap e non-heap)
- Pool de conexões do banco (HikariCP)
- Threads ativas
- Uso de CPU

Para visualizar os dashboards, acesse o **Grafana** em `http://localhost:3000` após subir o `docker-compose`.

---

## Testes

```bash
# Roda os testes e gera relatório de cobertura
cd trips-charges-service
./mvnw verify

# Relatório de cobertura
# Abrir no navegador: target/site/jacoco/index.html
```

12 testes implementados:
- `TripServiceTest` — 7 testes unitários das regras de negócio
- `TripControllerTest` — 5 testes de integração dos endpoints REST
