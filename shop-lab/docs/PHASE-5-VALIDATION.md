# Fase 5 — carga por JMeter e navegador

O laboratório oferece duas formas de criar tráfego no mesmo endpoint:

- JMeter para uma taxa sustentada, configurável e reproduzível;
- a página same-origin `http://localhost:18080/load-generator/` para disparar manualmente um lote de pedidos.

## Página web

Informe de 1 a 10.000 pedidos e clique em **Enviar agora**. A página gera payloads válidos para `POST /orders`, envia no máximo 20 requests em paralelo e exibe concluídos, sucessos e falhas. Como os arquivos estáticos são servidos pelo próprio `order-service`, ela usa `/orders` no mesmo host e não exige CORS nem um container adicional.

Após um lote bem-sucedido, confira o contador do produtor:

```bash
curl -s http://localhost:18080/actuator/prometheus | grep orders_produced_total
```

## JMeter

O plano em `loadtest/order-load-test.jmx` usa um Thread Group com duração configurável e um throughput timer Groovy em escopo global. Ele coordena as threads para que `requestsPerMinute` seja a taxa total do cenário. Os parâmetros são properties do JMeter:

| Property | Padrão | Descrição |
|---|---:|---|
| `targetHost` | `localhost` | Host do order-service |
| `targetPort` | `18080` | Porta do order-service |
| `requestsPerMinute` | `3600` | Taxa global desejada (60 req/s) |
| `durationSeconds` | `60` | Duração do cenário |
| `threads` | `20` | Threads disponíveis para atingir a taxa |

No host, com JMeter instalado:

```bash
jmeter -n -t loadtest/order-load-test.jmx -l /tmp/order-load-results.jtl \
  -JtargetHost=localhost -JtargetPort=18080 \
  -JrequestsPerMinute=4800 -JdurationSeconds=45 -Jthreads=30
```

Dentro da rede Docker, use `targetHost=order-service` e `targetPort=8080`.

Caso não tenha o JMeter instalado, o mesmo comando pode ser executado em um
container temporário (na pasta `shop-lab`):

```bash
docker run --rm --network shop-lab-network \
  -v "$PWD/loadtest:/tests:ro" justb4/jmeter:5.5 \
  -n -t /tests/order-load-test.jmx -l /tmp/order-load-results.jtl \
  -JtargetHost=order-service -JtargetPort=8080 \
  -JrequestsPerMinute=4800 -JdurationSeconds=90 -Jthreads=30
```

Uma taxa de 4.800 req/min (80 req/s), superior ao limite de consumo de 50 msg/s, deve elevar temporariamente `orders_produced_total` acima de `orders_consumed_total` e fazer crescer o painel **Consumer lag**. Quando o teste termina, o consumidor drena o lag e os dois throughput voltam a zero.

## Validação executada

Em 31/08/2026, a página foi exercitada em navegador real com um lote de 12:
**12 sucesso e 0 falha**. O JMeter 5.5 foi executado a 4.800 req/min por 90 s,
sem erros. Nas janelas estabilizadas, o relatório registrou 79,1 e 80,0 req/s.
Durante o teste, o Prometheus mostrou `orders_produced_total` em **80/s**,
`orders_consumed_total` em **50/s** e consumer lag de **1.747** — o comportamento
de backpressure esperado.
