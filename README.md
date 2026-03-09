## OplogToSQLParser

Small Kotlin project that converts MongoDB oplog-style events into equivalent SQL statements and executes them on PostgreSQL.

The project runs fully inside Docker and demonstrates a simple streaming CDC pipeline:

```
MongoDB → Debezium → Kafka → Parser → PostgreSQL
```

Changes in MongoDB collections are captured via the oplog, streamed through Kafka using Debezium, transformed into SQL by the Kotlin parser, and applied to PostgreSQL.

The goal is to simulate how MongoDB operations can be replayed in a relational database.

---

## Architecture

```
MongoDB
   ↓
Mongo Oplog
   ↓
Debezium (Kafka Connect)
   ↓
Kafka Topic (mongo.<db>.<collection>)
   ↓
OplogToSQLParser (Kafka Consumer)
   ↓
Generated SQL
   ↓
PostgreSQL
```

Example Kafka topic produced by Debezium:

```
mongo.test.student
```

---

## How to Run

Build the jar:

```bash
./gradlew shadowJar
```

Build docker images:

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

## Initialize Mongo Replica Set

MongoDB oplog works only with replica sets.

Open a Mongo shell:

```bash
docker exec -it mongo mongosh
```

Run:

```javascript
rs.initiate({
  _id: "rs0",
  members: [
    { _id: 0, host: "mongo:27017" }
  ]
})
```

Verify:

```javascript
rs.status()
```

You should see:

```
stateStr: "PRIMARY"
```

---

## Register Debezium Connector

Create the MongoDB connector so Debezium can stream oplog events to Kafka.

Run:

```bash
curl -X POST http://localhost:8083/connectors \
-H "Content-Type: application/json" \
-d '{
"name": "mongo-oplog-connector",
"config": {
"connector.class": "io.debezium.connector.mongodb.MongoDbConnector",
"mongodb.connection.string": "mongodb://mongo:27017/?replicaSet=rs0",
"topic.prefix": "mongo",
"database.exclude.list": "admin,config,local"
}
}'
```

Verify the connector:

```bash
curl http://localhost:8083/connectors
```

Expected output:

```
["mongo-oplog-connector"]
```

---

## Insert Data in MongoDB

Open Mongo shell:

```bash
docker exec -it mongo mongosh
```

Use a database:

```javascript
use test
```

Insert a document:

```javascript
db.student.insertOne({
  _id: "100",
  name: "Alice",
  roll_no: 45
})
```

Debezium will produce a Kafka event on topic:

```
mongo.test.student
```

The parser will convert it to SQL and execute it in Postgres.

---

## Update Example

```javascript
db.student.updateOne(
  { _id: "100" },
  { $set: { roll_no: 99 } }
)
```

---

## Delete Example

```javascript
db.student.deleteOne({ _id: "100" })
```

---

## Verify Data in Postgres

Open Postgres shell:

```bash
docker exec -it postgres psql -U postgres -d oplogdb
```

Run:

```sql
SELECT * FROM test.student;
```

You should see the replicated rows.

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

List containers:

```bash
docker ps
```

---

## Current Setup

The system currently includes:

- MongoDB (replica set)
- Debezium (Kafka Connect)
- Apache Kafka
- Kotlin parser service
- PostgreSQL
- Docker Compose environment

Flow:

```
MongoDB
   ↓
Oplog
   ↓
Debezium
   ↓
Kafka topic (mongo.<db>.<collection>)
   ↓
OplogToSQLParser
   ↓
Generated SQL
   ↓
PostgreSQL
```

Supported operations:

- insert
- update
- delete
- multiple oplog entries
