## Micronaut 4.10.17 Documentation

- [User Guide](https://docs.micronaut.io/4.10.17/guide/index.html)
- [API Reference](https://docs.micronaut.io/4.10.17/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/4.10.17/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)
---
- [Micronaut Maven Plugin documentation](https://micronaut-projects.github.io/micronaut-maven-plugin/latest/)
## Feature micronaut-aot documentation
- [Micronaut AOT documentation](https://micronaut-projects.github.io/micronaut-aot/latest/guide/)
## Feature maven-enforcer-plugin documentation
- [https://maven.apache.org/enforcer/maven-enforcer-plugin/](https://maven.apache.org/enforcer/maven-enforcer-plugin/)
## Feature serialization-jackson documentation
- [Micronaut Serialization Jackson Core documentation](https://micronaut-projects.github.io/micronaut-serialization/latest/guide/)

🌟 The Microservices architecture is:

	1. Trade-service:
		* Outbox pattern
		* Kafka producer
		* TradeEvent DTO 
		* Reliable event publishing

	2. Ledger-service
		* Kafka consumer
		* Idempotency
		* Ledger entries
		* Account balances
		* Transactional correctness
		* Full audit trail
		
------------------------------------------
	1. trade-service (write side)
		* Accepts trades
		* Stores OutboxEvents
		* Publishes events to Kafka
		* Tested with unit + integration tests
		* Guarantees exactly once event publishing

	2. ledger‑service (read side + state machine)
		* Consumes trade events
		* Writes immutable ledger entries
		* Updates balances
		* Idempotent processing
		* Fully tested with trade‑service

	3. wallet‑service (query side)
		* Aggregates balances
		* Computes positions
		* Computes market value
		* Computes unrealized PnL
		* Computes total portfolio value
		* Exposes REST API for frontend
		* Tested with ledger-service + trade-service



