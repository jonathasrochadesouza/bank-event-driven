# Fase 2 — Cenários de teste das métricas de pedidos

Este roteiro valida os contratos observáveis no endpoint Prometheus:

- `orders_produced_total`: número de eventos enviados com sucesso ao Kafka;
- `orders_consumed_total`: número de eventos processados pelo `logistic-service`.

Os contadores são mantidos em memória. Por isso, compare **deltas dentro da mesma
execução dos serviços**, e não valores absolutos: reiniciar um serviço zera a
respectiva métrica.

## Pré-requisitos

Execute em um terminal WSL, sem outra pessoa ou processo criando pedidos durante
o teste:

```bash
cd /mnt/c/Dev/Personal/bank-event-driven/shop-lab
docker compose up -d --build
docker compose ps
```

Espere até `order-service` e `logistic-service` ficarem `healthy`. Defina estes
atalhos e funções no mesmo terminal:

```bash
ORDER_METRICS=http://localhost:18080/actuator/prometheus
LOGISTIC_METRICS=http://localhost:18081/actuator/prometheus

metric_value() {
  curl -fsS "$1" | awk -v metric="$2" \
    '$1 ~ ("^" metric "(\\{[^}]*\\})?$") { print $2; exit }'
}

assert_increment() {
  awk -v before="$1" -v after="$2" -v increment="$3" \
    'BEGIN { exit !(after == before + increment) }'
}
```

> Os comandos abaixo usam `curl` e `awk`, normalmente já disponíveis no WSL.
> Cada novo pedido recebe um `orderId` novo, portanto deve gerar exatamente um
> evento novo e um consumo novo.

## CT-MET-01 — Exposição inicial das duas métricas

**Objetivo:** confirmar que ambos os endpoints expõem as séries Prometheus antes
de criar pedidos.

```bash
curl -fsS "$ORDER_METRICS" | grep -E '^# (HELP|TYPE) orders_produced_total|^orders_produced_total(\{[^}]*\})? '
curl -fsS "$LOGISTIC_METRICS" | grep -E '^# (HELP|TYPE) orders_consumed_total|^orders_consumed_total(\{[^}]*\})? '
```

**Resultado esperado:** cada comando retorna as linhas `# HELP`, `# TYPE` e a
série da métrica; o tipo é `counter`. A série inclui a tag `application`, por
exemplo `orders_produced_total{application="order-service"} 0.0`. O valor pode
ser `0.0` em uma execução recém-iniciada ou maior que zero se já houver pedidos
nesta mesma execução.

## CT-MET-02 — Um pedido válido incrementa produção e consumo

**Objetivo:** provar o caminho ponta a ponta e o incremento unitário dos dois
contadores.

```bash
produced_before=$(metric_value "$ORDER_METRICS" orders_produced_total)
consumed_before=$(metric_value "$LOGISTIC_METRICS" orders_consumed_total)

curl -fsS -D /tmp/order-headers.txt -o /tmp/order-response.json \
  -X POST http://localhost:18080/orders \
  -H 'Content-Type: application/json' \
  -d @scripts/sample-order.json

cat /tmp/order-headers.txt
cat /tmp/order-response.json

# Aguarda o callback assíncrono de produção e o listener Kafka concluírem.
for attempt in {1..30}; do
  produced_after=$(metric_value "$ORDER_METRICS" orders_produced_total)
  consumed_after=$(metric_value "$LOGISTIC_METRICS" orders_consumed_total)
  if assert_increment "$produced_before" "$produced_after" 1 && \
     assert_increment "$consumed_before" "$consumed_after" 1; then
    break
  fi
  sleep 1
done

printf 'produced: %s -> %s\n' "$produced_before" "$produced_after"
printf 'consumed: %s -> %s\n' "$consumed_before" "$consumed_after"
assert_increment "$produced_before" "$produced_after" 1 && \
  assert_increment "$consumed_before" "$consumed_after" 1
```

**Resultado esperado:**

- a resposta HTTP possui status `202 Accepted` e o corpo contém `orderId`;
- `orders_produced_total` aumenta em exatamente `1`;
- até 30 segundos depois, `orders_consumed_total` também aumenta em exatamente
  `1`;
- o último comando termina com código `0`.

Evidência complementar para investigação:

```bash
docker logs --tail 20 shop-lab-logistic-service | grep 'Processed order'
```

## CT-MET-03 — Request inválido não altera os contadores

**Objetivo:** assegurar que uma rejeição de validação não publica nem processa
um evento.

```bash
produced_before=$(metric_value "$ORDER_METRICS" orders_produced_total)
consumed_before=$(metric_value "$LOGISTIC_METRICS" orders_consumed_total)

http_code=$(curl -sS -o /tmp/invalid-order-response.json -w '%{http_code}' \
  -X POST http://localhost:18080/orders \
  -H 'Content-Type: application/json' \
  -d '{"customerId":"x","currency":"BRL","items":[]}')

sleep 2
produced_after=$(metric_value "$ORDER_METRICS" orders_produced_total)
consumed_after=$(metric_value "$LOGISTIC_METRICS" orders_consumed_total)

printf 'HTTP: %s; produced: %s -> %s; consumed: %s -> %s\n' \
  "$http_code" "$produced_before" "$produced_after" "$consumed_before" "$consumed_after"
test "$http_code" = 400 && \
  test "$produced_before" = "$produced_after" && \
  test "$consumed_before" = "$consumed_after"
```

**Resultado esperado:** HTTP `400`; ambos os valores ficam inalterados; o último
comando termina com código `0`.

## CT-MET-04 — Lote de pedidos converge nos dois contadores

**Objetivo:** validar que o total de eventos enviados e o total processado
acompanham um lote conhecido. Este cenário também tolera o pequeno atraso do
processamento assíncrono.

```bash
batch_size=10
produced_before=$(metric_value "$ORDER_METRICS" orders_produced_total)
consumed_before=$(metric_value "$LOGISTIC_METRICS" orders_consumed_total)

for n in $(seq 1 "$batch_size"); do
  curl -fsS -o /dev/null -X POST http://localhost:18080/orders \
    -H 'Content-Type: application/json' -d @scripts/sample-order.json
done

for attempt in {1..30}; do
  produced_after=$(metric_value "$ORDER_METRICS" orders_produced_total)
  consumed_after=$(metric_value "$LOGISTIC_METRICS" orders_consumed_total)
  if assert_increment "$produced_before" "$produced_after" "$batch_size" && \
     assert_increment "$consumed_before" "$consumed_after" "$batch_size"; then
    break
  fi
  sleep 1
done

printf 'produced: %s -> %s; consumed: %s -> %s\n' \
  "$produced_before" "$produced_after" "$consumed_before" "$consumed_after"
assert_increment "$produced_before" "$produced_after" "$batch_size" && \
  assert_increment "$consumed_before" "$consumed_after" "$batch_size"
```

**Resultado esperado:** cada contador aumenta exatamente `10`. Com a configuração
atual de 50 msg/s, o lote deve convergir muito antes do limite de 30 segundos.

## Interpretação de falhas

| Evidência | Interpretação inicial |
| --- | --- |
| Produção não aumenta | O envio ao Kafka não foi confirmado; verifique `order-service` e a saúde/conectividade do Kafka. |
| Produção aumenta, consumo não | A mensagem foi produzida, mas o consumidor não a processou no prazo; verifique logs, grupo `logistic-service` e lag. |
| Request inválido aumenta um contador | Falha de contrato: a validação deveria impedir a publicação. |
| Consumo fica abaixo do esperado no lote | Aguarde o limite de 30 s e inspecione o lag; depois disso, há falha a investigar. |
| Valores voltam a zero após restart | Comportamento esperado na Fase 2: são contadores em memória. |

Para conferir o grupo consumidor em qualquer falha:

```bash
docker exec shop-lab-kafka kafka-consumer-groups \
  --bootstrap-server localhost:29092 --describe --group logistic-service
```
