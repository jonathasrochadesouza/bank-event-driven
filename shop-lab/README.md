# shop-lab

Laboratório local de mensageria orientada a eventos com Apache Kafka, observabilidade
(OpenTelemetry + Tempo + Prometheus + Grafana) e teste de carga com JMeter.

Domínio: `shop`. Um `order-service` publica eventos `order.created.v1`; um
`logistic-service` consome com limite de 50 msg/s por instância.

> Este README é incremental: cada fase do laboratório adiciona serviços ao
> `docker-compose.yml` e novas seções aqui.

## Pré-requisitos

- Docker Engine + Docker Compose v2 (neste ambiente: via WSL Ubuntu).
- Portas livres no host: `19092` (Kafka), `8085` (kafka-ui). As portas de
  observabilidade (`9091`, `3001`, `3200`) são usadas a partir da Fase 3/4.

## Estrutura

```
shop-lab/
├── docker-compose.yml     # orquestração (cresce a cada fase)
├── .env                   # portas, tópico, partições, endpoints
├── order-service/         # produtor (Fase 2)
├── logistic-service/      # consumidor (Fase 2)
├── observability/         # otel-collector, prometheus, tempo, grafana (Fase 3/4)
└── loadtest/              # plano JMeter (Fase 5)
```

## Fases

| Fase | Conteúdo | Status |
|------|----------|--------|
| 1 | Kafka (KRaft, 1 broker) + tópico + kafka-ui | ✅ concluída |
| 2 | order-service + logistic-service ponta a ponta | ✅ concluída |
| 3 | OTel Java Agent + Collector + Tempo (trace ponta a ponta) | pendente |
| 4 | Micrometer + Prometheus (scrape 1s) + kafka-exporter + Grafana | pendente |
| 5 | JMeter via REST | pendente |
| 6 | Exercício de backpressure e tuning | pendente |

## Fase 1 — Infraestrutura de mensageria

### Subir

Pelo Windows/PowerShell delegando ao WSL:

```powershell
wsl -d Ubuntu -e bash -lc "cd /mnt/c/Dev/Personal/bank-event-driven/shop-lab && docker compose up -d"
```

Ou, já dentro do WSL:

```bash
cd /mnt/c/Dev/Personal/bank-event-driven/shop-lab
docker compose up -d
```

### O que sobe

| Serviço | Container | Porta host | Descrição |
|---------|-----------|------------|-----------|
| kafka | `shop-lab-kafka` | `19092` | Broker único em modo KRaft (sem Zookeeper) |
| kafka-init | `shop-lab-kafka-init` | — | Cria o tópico `order.created.v1` e encerra |
| kafka-ui | `shop-lab-kafka-ui` | `8085` | Inspeção de tópicos/mensagens/consumer groups |

### Acessos

- kafka-ui: http://localhost:8085
- Bootstrap para clientes no host: `localhost:19092`
- Bootstrap dentro da rede compose: `kafka:29092`

### Validação rápida

```bash
# não deve existir Zookeeper
docker compose ps

# o tópico deve existir
docker exec shop-lab-kafka kafka-topics --bootstrap-server localhost:29092 --list

# detalhe do tópico (partições e replicação)
docker exec shop-lab-kafka kafka-topics --bootstrap-server localhost:29092 \
  --describe --topic order.created.v1
```

### Derrubar

```bash
# para os containers, mantém o volume de dados
docker compose down

# reset completo (remove o volume kafka-data e o estado do broker)
docker compose down -v
```

## Fase 2 — Serviços de aplicação (order-service + logistic-service)

Produtor e consumidor Spring Boot (Java 21, Spring Boot 3.3.5), construídos via
Docker multi-stage. Detalhes e validação manual em `docs/PHASE-2-VALIDATION.md`.

| Serviço | Container | Porta host | Papel |
|---------|-----------|------------|-------|
| order-service | `shop-lab-order-service` | `18080` | `POST /orders` publica `order.created.v1` (key=orderId) |
| logistic-service | `shop-lab-logistic-service` | `18081` | consome, limita a 50 msg/s, deduplica por `eventId` |

### Testar ponta a ponta

```bash
cd /mnt/c/Dev/Personal/bank-event-driven/shop-lab
docker compose up -d --build

# criar um pedido (retorna o orderId)
curl -s -X POST http://localhost:18080/orders \
  -H 'Content-Type: application/json' \
  -d @scripts/sample-order.json

# ver o logistic-service processar
docker logs shop-lab-logistic-service | grep 'Processed order' | tail

# offsets/lag do consumer group
docker exec shop-lab-kafka kafka-consumer-groups \
  --bootstrap-server localhost:29092 --describe --group logistic-service
```

### Métricas (já disponíveis, exploradas na Fase 4)

- order-service: `curl http://localhost:18080/actuator/prometheus | grep orders_produced_total`
- logistic-service: `curl http://localhost:18081/actuator/prometheus | grep -E 'orders_consumed_total|orders_processing_seconds'`

## Notas de design

- Modo **KRaft** (sem Zookeeper): menos containers e alinhado ao presente/futuro do Kafka.
- Porta host **19092** para o Kafka porque a `9092` já está ocupada por outro stack neste ambiente.
- `replication-factor = 1` e ISR mínimos: laboratório de aprendizado, sem durabilidade/failover.
