# OplogToSQLParser

Small Kotlin project that converts MongoDB oplog JSON entries into equivalent SQL statements and executes them on PostgreSQL.

Right now the project runs inside Docker and demonstrates a simple pipeline:

```id="pipe1"
sample-oplog.json → parser → SQL → PostgreSQL
```

The goal is to simulate how MongoDB operations could be translated and applied to a relational database.

---

## Sample Input

The input oplog JSON is stored in:

```
src/main/resources/sample-oplog.json
```

The application reads this file at runtime and generates SQL from it.

You can modify this file to test different operations like:

* insert
* update
* delete
* multiple oplog entries

After modifying the file, rebuild and run again.

---

## How to Run

Build the fat jar:

```id="run1"
./gradlew shadowJar
```

Start the containers:

```id="run2"
docker-compose up
```

You should see logs like:

```id="run3"
Connecting to Postgres...
Connected to Postgres!

Generated SQL:
CREATE SCHEMA test;
CREATE TABLE test.student ...
INSERT INTO test.student ...

SQL executed successfully
```

---

## Verify Data in Postgres

Open a shell inside the Postgres container:

```id="verify1"
docker exec -it postgres psql -U postgres -d oplogdb
```

Run:

```id="verify2"
SELECT * FROM test.student;
```

---

## Useful Commands

Start containers:

```id="cmd1"
docker-compose up
```

Rebuild after code or input change:

```id="cmd2"
./gradlew shadowJar
docker-compose build
docker-compose up
```

Stop containers:

```id="cmd3"
docker-compose down
```

List running containers:

```id="cmd4"
docker ps
```

---

## Current Setup

* Kotlin parser converts oplog JSON → SQL
* PostgreSQL runs in Docker
* Parser container connects to Postgres using JDBC
* Sample oplog JSON is read from `resources`
