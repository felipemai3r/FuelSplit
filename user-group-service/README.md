# User Group Service

Microsserviço responsável pelo gerenciamento de usuários, grupos, veículos e despesas extras da plataforma **FuelSplit**.

---

## Visão Geral

| Item | Valor |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.3.2 |
| Porta | 8082 |
| Banco | PostgreSQL (schema `users_groups`) |
| Mensageria | RabbitMQ |
| Versão atual | 1.0.0 |

---

## Ambientes

| Ambiente | Branch | URL | Swagger |
|---|---|---|---|
| DEV | `develop` | *(configurar após deploy no Render)* | Habilitado |
| HOMOL | `main` | *(configurar após deploy no Render)* | Desabilitado |

---

## Pipeline CI/CD

O pipeline é executado automaticamente pelo **GitHub Actions** ao fazer push nas branches `develop` ou `main`, somente quando arquivos dentro de `user-group-service/` são alterados.

```
Push → Testes + JaCoCo + SonarCloud → Build Docker → Push Docker Hub → Deploy Render
```

### Fluxo de branches (Git Flow)

```
feature/* → develop (DEV) → main (HOMOL)
```

### Secrets necessários no GitHub

| Secret | Descrição |
|---|---|
| `SONAR_TOKEN_F` | Token de autenticação do SonarCloud |
| `DOCKER_USERNAME_F` | Usuário do Docker Hub (`felipemaier`) |
| `DOCKER_PASSWORD_F` | Senha / Access Token do Docker Hub |
| `RENDER_DEPLOY_HOOK_DEV_F` | Webhook do serviço DEV no Render |
| `RENDER_DEPLOY_HOOK_HOMOL_F` | Webhook do serviço HOMOL no Render |

---

## Imagens Docker

| Tag | Ambiente |
|---|---|
| `felipemaier/user-group-service:dev` | Ambiente DEV |
| `felipemaier/user-group-service:homol` | Ambiente HOMOL |
| `felipemaier/user-group-service:latest` | Última versão publicada |

---

## Como Rodar Localmente

### Pré-requisitos

- Docker e Docker Compose instalados

### Subir o serviço + banco + RabbitMQ + observabilidade

```bash
cd user-group-service
docker compose -f docker-compose.observability.yml up --build
```

| Serviço | URL |
|---|---|
| API | http://localhost:8082 |
| Swagger | http://localhost:8082/swagger-ui.html |
| Actuator / Health | http://localhost:8082/actuator/health |
| Métricas Prometheus | http://localhost:8082/actuator/prometheus |
| Prometheus UI | http://localhost:9090 |
| Grafana | http://localhost:3000 (admin/admin) |

---

## Observabilidade

### Prometheus

Raspa métricas a cada 15 segundos do endpoint `/actuator/prometheus`.

### Grafana

Dashboard pré-configurado em `observability/grafana/provisioning/dashboards/user-group-service.json` com:

- Requisições HTTP por segundo
- Latência p99 por endpoint
- Uso de memória JVM (Heap e Non-Heap)
- Threads ativas
- Taxa de erros 5xx
- Uptime e CPU usage

---

## Qualidade de Código

- **JaCoCo**: cobertura mínima de 60% (verificado em cada build)
- **SonarCloud**: análise estática a cada push — [ver projeto](https://sonarcloud.io/project/overview?id=user-group-service)

---

## Versionamento

Projeto segue **Semantic Versioning (SemVer)**:

| Situação | Ação |
|---|---|
| Correção de bug | Incrementa PATCH (ex: `1.0.1`) |
| Nova funcionalidade | Incrementa MINOR (ex: `1.1.0`) |
| Quebra de compatibilidade | Incrementa MAJOR (ex: `2.0.0`) |

---

## Contribuindo com Alterações

### Fluxo completo via Git

```bash
# 1. Crie uma branch a partir de develop
git checkout develop
git pull origin develop
git checkout -b feature/nome-da-feature

# 2. Faça as alterações e commit
git add .
git commit -m "feat: descrição da alteração"

# 3. Suba a branch para o remoto
git push origin feature/nome-da-feature

# 4. Merge em develop (aciona deploy no DEV)
git checkout develop
git merge feature/nome-da-feature
git push origin develop

# 5. Após validar no DEV, promova para main (aciona deploy no HOMOL)
git checkout main
git merge develop
git push origin main
```

O push em `develop` ou `main` aciona automaticamente o pipeline de CI/CD.

---

## Variáveis de Ambiente

| Variável | Padrão (local) | Descrição |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` | Perfil ativo (`dev` ou `homol`) |
| `DB_HOST` | `localhost` | Host do banco PostgreSQL |
| `DB_PORT` | `5434` | Porta do banco |
| `DB_NAME` | `authdb` | Nome do banco |
| `DB_USER` | `auth_user` | Usuário do banco |
| `DB_PASS` | `auth_pass` | Senha do banco |
| `RABBITMQ_HOST` | `localhost` | Host do RabbitMQ |
| `RABBITMQ_USER` | `guest` | Usuário do RabbitMQ |
| `RABBITMQ_PASS` | `guest` | Senha do RabbitMQ |
| `JWT_SECRET` | *(valor padrão)* | Chave secreta JWT |
