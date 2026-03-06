# OplogToSQLParser

Small Kotlin project that converts MongoDB oplog JSON entries into equivalent SQL statements and executes them on PostgreSQL.

The project runs fully inside Docker and demonstrates a simple streaming pipeline:

```
Kafka Producer → oplog-events topic → parser → SQL → PostgreSQL
```

Messages sent to the Kafka topic are parsed by the Kotlin service, converted to SQL, and applied to Postgres.

The goal is to simulate how MongoDB oplog events could be translated and replayed in a relational database.

---

## Input Format

The parser expects MongoDB oplog-style JSON messages.

Example insert event:

```json
{"op":"i","ns":"test.student","o":{"_id":"635b79e231d82a8ab1de863b","name":"Selena Miller","roll_no":51,"is_graduated":false,"date_of_birth":"2000-01-30"}}
```

Example update event:

```json
{"op":"u","ns":"test.student","o":{"$v":2,"diff":{"u":{"is_graduated":true}}},"o2":{"_id":"635b79e231d82a8ab1de863b"}}
```

Example delete event:

```json
{"op":"d","ns":"test.student","o":{"_id":"635b79e231d82a8ab1de863b"}}
```

Multiple events can also be sent as a JSON array.

---

## How to Run

Build the fat jar:

```bash
./gradlew shadowJar
```

Build Docker images:

```bash
docker-compose build
```

Start the system:

```bash
docker-compose up
```

You should see logs like:

```
Connecting to Postgres...
Connected to Postgres!
Connecting to Kafka...
Kafka consumer started. Waiting for messages...
```

---

## Sending Oplog Events

Open a shell in the Kafka container:

```bash
docker exec -it kafka bash
```

Start the producer:

```bash
/opt/kafka/bin/kafka-console-producer.sh \
  --topic oplog-events \
  --bootstrap-server localhost:9092
```

Now paste a JSON message like:

```json
{"op":"i","ns":"test.student","o":{"_id":"100","name":"Alice","roll_no":45}}
```

The parser will:

1. Read the message from Kafka
2. Convert it to SQL
3. Execute it in PostgreSQL

---

## Verify Data in Postgres

Open a shell inside the Postgres container:

```bash
docker exec -it postgres psql -U postgres -d oplogdb
```

Run:

```sql
SELECT * FROM test.student;
```

You should see the inserted/updated rows.

---

## Useful Commands

Start containers:

```bash
docker-compose up
```

Rebuild after code changes:

```bash
./gradlew shadowJar
docker-compose build
docker-compose up
```

Stop containers:

```bash
docker-compose down
```

View parser logs:

```bash
docker-compose logs -f oplog-parser
```

List running containers:

```bash
docker ps
```

---

## Current Setup

The system currently includes:

- Kotlin parser service
- Apache Kafka (single node)
- PostgreSQL
- Docker Compose environment

Flow:

```
Kafka producer
      ↓
Kafka topic (oplog-events)
      ↓
OplogToSQLParser (consumer)
      ↓
Generated SQL
      ↓
PostgreSQL
```

Supported operations so far:

- insert
- update
- delete
- multiple oplog entries in a single message
