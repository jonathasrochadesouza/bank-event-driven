# Fase 2 — Relatório e guia de validação manual

Serviços de aplicação: `order-service` (produtor) e `logistic-service` (consumidor),
ponta a ponta sobre o tópico `order.created.v1`.

## O que foi entregue

Dois projetos Spring Boot (Java 21, Spring Boot 3.3.5), construídos via Docker multi-stage
(`maven:3.9-eclipse-temurin-21` no build, `eclipse-temurin:21-jre` no runtime).

### order-service (produtor) — porta host `18080`
- `POST /orders` com validação (Bean Validation); retorna `202 Accepted` + `{ "orderId": ... }`.
- Monta o envelope `order.created.v1` (eventId UUID, eventType, eventVersion, occurredAt UTC, source, data).
- Publica no Kafka com **key = orderId** (ordem por pedido na partição), serialização **JSON**.
- Métrica Micrometer `orders_produced_total`.

### logistic-service (consumidor) — porta host `18081`
- `@KafkaListener` no grupo `logistic-service`, desserialização JSON (tipo default fixo, sem type headers).
- **Rate limiter** Guava a **50 msg/s por instância** (`app.rate-limit.permits-per-second`, env `RATE_LIMIT_PERMITS`).
- **Idempotência** por `eventId` (dedupe em memória, bounded); duplicatas são ignoradas.
- Métricas `orders_consumed_total`, `orders_duplicates_total`, timer `orders_processing_seconds`.

### Decisões de contrato
- Produtor **não** envia type headers do Spring (`spring.json.add.type.headers=false`).
- Consumidor fixa o tipo (`spring.json.value.default.type`) e ignora campos desconhecidos.
- Resultado: os serviços **não dependem** do nome de pacote/classe um do outro.

## Resultado da validação automática (já executada)

- ✅ Ambas as imagens compilaram (Maven dentro do Docker) e subiram **healthy**.
- ✅ logistic-service assumiu as 3 partições de `order.created.v1`.
- ✅ `POST /orders` retornou `{"orderId":"ORD-2026-000001"}`.
- ✅ logistic-service logou `Processed order ORD-2026-000001 (eventId=46151187-...)`.
- ✅ Consumer group `logistic-service` com offsets avançando e **lag 0**.
- ✅ Métricas expostas: `orders_produced_total=1`, `orders_consumed_total`, `orders_processing_seconds_count`.
- ✅ Idempotência comprovada: reentregas (durante restarts) contabilizadas em `orders_duplicates_total` sem reprocessar efeito.

> Bug encontrado e corrigido durante a validação: `WORKDIR /app` + `COPY ... app.jar`
> colocava o jar em `/app/app.jar`, mas o `ENTRYPOINT` chamava `/app.jar` (raiz),
> causando crash-loop "Unable to access jarfile". Corrigido para caminho relativo.

## Passo a passo para VOCÊ validar manualmente

Mantenha um terminal WSL aberto:

```bash
cd /mnt/c/Dev/Personal/bank-event-driven/shop-lab

# 1) subir tudo (constroi as imagens na primeira vez)
docker compose up -d --build

# 2) aguardar apps saudaveis (~40s) e conferir
docker compose ps
#   order-service e logistic-service devem estar "healthy"

# 3) criar um pedido
curl -s -X POST http://localhost:18080/orders \
  -H 'Content-Type: application/json' \
  -d @scripts/sample-order.json
#   -> {"orderId":"ORD-2026-00000N"}

# 4) validacao: rejeicao de request invalido (sem items) -> HTTP 400
curl -s -o /dev/null -w '%{http_code}\n' -X POST http://localhost:18080/orders \
  -H 'Content-Type: application/json' -d '{"customerId":"x","currency":"BRL","items":[]}'

# 5) ver o consumo
docker logs shop-lab-logistic-service | grep 'Processed order' | tail

# 6) offsets/lag
docker exec shop-lab-kafka kafka-consumer-groups \
  --bootstrap-server localhost:29092 --describe --group logistic-service

# 7) mensagem em JSON legivel no kafka-ui
#    http://localhost:8085 -> Topics -> order.created.v1 -> Messages
```

## Checklist de aceite da Fase 2

- [ ] `docker compose ps` mostra order-service e logistic-service `healthy`
- [ ] `POST /orders` válido retorna 202 + `orderId`
- [ ] `POST /orders` inválido (items vazio) retorna 400 e **não** publica
- [ ] logistic-service loga `Processed order ...` com `eventId` preenchido
- [ ] consumer group `logistic-service` com lag 0 após consumir
- [ ] mensagem visível como JSON no kafka-ui
- [ ] `orders_produced_total` e `orders_consumed_total` presentes em `/actuator/prometheus`

## Observações que preparam as próximas fases

- O rate limiter de 50 msg/s só "aparece" sob carga; na Fase 5 (JMeter) você verá o lag crescer.
- As métricas já existem, mas o scrape e os dashboards chegam na Fase 4 (Prometheus + Grafana).
- O `traceId` no envelope está `null` de propósito; será preenchido pela propagação via header na Fase 3 (OTel).
