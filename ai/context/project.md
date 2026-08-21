# Project Context

`shortner` is a full-stack URL shortener MVP.

## Implemented Stack

- Frontend: Angular in `frontend/`
- Backend: Java 21 Spring Boot in `backend/`
- Mapping storage: DynamoDB table named `url-shortner`
- Sequence allocation and per-IP creation quotas: Redis locally and ElastiCache Serverless in production Terraform
- Local services: Docker Compose with Redis and DynamoDB Local
- Infrastructure: Terraform in `infra/terraform/`
- API contract: OpenAPI in `openapi/openapi.yaml`

## Current Behavior

- `POST /v1/shortner` creates a short link for an `http://` or `https://` URL using `{ "url": "..." }`.
- Creation is limited by source IP to 5 requests per minute and 100 per 24-hour window; excess returns `429` with `Retry-After`.
- Redis initializes the `url-shortner:sequence` counter to `238327`; its first allocation is `238328`.
- Sqids obfuscates the normalized allocated ID into an exactly four-character ASCII-alphanumeric code; it is not encryption and must not encode sensitive data.
- Creation fails closed with `503` when Redis allocation is unavailable or Sqids would produce a code outside the fixed length.
- DynamoDB stores `short_link` and `original_link`.
- `GET /{shortCode}` validates the code and returns an HTTP 302 redirect, or a 404 when absent or malformed.
- API Gateway routes creation and redirects to separate Lambdas with independent throttling, IAM, networking, and reserved concurrency.

## Not Yet Implemented

- TTL expiration
- Analytics
- Authentication or authorization
- CI/CD
- Complete Lambda artifact packaging

Treat the code as the source of truth when documentation disagrees.
