# Fase 1 — Relatório e guia de validação manual

Infraestrutura de mensageria: Kafka (KRaft, 1 broker) + criação de tópico + kafka-ui.

## O que foi entregue

| Arquivo | Papel |
|---------|-------|
| `shop-lab/docker-compose.yml` | Orquestra `kafka`, `kafka-init`, `kafka-ui` |
| `shop-lab/.env` | Portas, tópico, partições, endpoints |
| `shop-lab/scripts/smoke-test.sh` | Produz e consome 1 evento JSON |
| `shop-lab/README.md` | Visão geral e comandos |

Serviços e portas (host):

| Serviço | Container | Porta | Observação |
|---------|-----------|-------|------------|
| Kafka (KRaft) | `shop-lab-kafka` | `19092` | `9092` estava ocupada por outro stack |
| Topic init | `shop-lab-kafka-init` | — | one-shot, cria `order.created.v1` e sai (exit 0) |
| kafka-ui | `shop-lab-kafka-ui` | `8085` | http://localhost:8085 |

## Resultado da validação automática (já executada)

- ✅ Stack sobe **sem Zookeeper** (KRaft, este nó é broker + controller).
- ✅ Tópico `order.created.v1` criado: **3 partições, replication-factor 1**.
- ✅ `kafka-init` encerrou com **exit 0** (criação idempotente, `--if-not-exists`).
- ✅ kafka-ui respondeu **HTTP 200**; cluster `shop-lab` **online**, 1 broker, 1 tópico, 3 partições.
- ✅ **Round-trip**: mensagem `key=ORD-SMOKE-1` com payload JSON produzida e consumida intacta.

> Observação: uma mensagem de smoke test (`ORD-SMOKE-1`) fica no tópico. Para começar limpo, use `docker compose down -v` (reseta o volume do broker).

## Gotcha do ambiente (importante)

O Docker roda dentro da distro **WSL Ubuntu**. O WSL2 **desliga a VM quando fica ociosa** (idle timeout ~15s). Quando isso acontece, o `dockerd` para e os containers recebem SIGTERM (exit 143).

Mitigações aplicadas / recomendadas:
- Os serviços usam `restart: unless-stopped`, então **voltam sozinhos** quando o daemon reinicia (o `kafka-init` usa `restart: "no"` por ser one-shot).
- Para validar sem interrupção, **mantenha um terminal WSL aberto** durante a sessão.
- Opcional (config do usuário): aumentar o idle timeout em `%USERPROFILE%\.wslconfig`:
  ```ini
  [wsl2]
  vmIdleTimeout=3600000
  ```
  (reinicie com `wsl --shutdown` depois de alterar)

## Passo a passo para VOCÊ validar manualmente

Abra um terminal WSL e mantenha aberto:

```bash
cd /mnt/c/Dev/Personal/bank-event-driven/shop-lab

# 1) subir e aguardar tudo saudavel
docker compose up -d --wait --wait-timeout 180

# 2) estado dos containers (kafka healthy; kafka-init Exited 0; kafka-ui Up)
docker compose ps

# 3) confirmar que NAO ha Zookeeper
docker ps -a --filter name=shop-lab --format '{{.Image}}' | grep -i zookeeper || echo "sem zookeeper - OK"

# 4) tópico e partições
docker exec shop-lab-kafka kafka-topics --bootstrap-server localhost:29092 \
  --describe --topic order.created.v1

# 5) round-trip produtor/consumidor
bash scripts/smoke-test.sh
```

Depois abra a UI no navegador: **http://localhost:8085**
- Cluster `shop-lab` deve aparecer **online**.
- Em *Topics* -> `order.created.v1` você vê 3 partições e as mensagens.

Para derrubar:

```bash
docker compose down       # para (mantém dados)
docker compose down -v    # reset total (remove volume kafka-data)
```

## Checklist de aceite da Fase 1

- [ ] `docker compose ps` mostra kafka `healthy`, kafka-init `Exited (0)`, kafka-ui `Up`
- [ ] Nenhum container Zookeeper
- [ ] `order.created.v1` com 3 partições e RF 1
- [ ] kafka-ui acessível em http://localhost:8085 com o cluster online
- [ ] `smoke-test.sh` consome de volta a mensagem produzida
