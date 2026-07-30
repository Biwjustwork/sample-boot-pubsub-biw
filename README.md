# sample-boot-pubsub

Your first **event-driven** system. Starting point for the event-driven
architecture session of **Backend Programming** (MFU).

The library again - but this time, when a member borrows a book, nobody calls
anybody. The borrow service **announces what happened**, and two other services
**react** to the announcement. The three of them never talk to each other.

> **Start here → [TODO.md](TODO.md)** - five small steps, in order.

Continues from
[`sample-boot-microservice`](https://github.com/cnacha-mfu/sample-boot-microservice)
and leads into the `lab-web-pubsub` lab.

**Stuck?** The finished version is in
[`sample-boot-pubsub-solution`](https://github.com/cnacha-mfu/sample-boot-pubsub-solution).
Every step in [TODO.md](TODO.md) links to the exact file that answers it.

---

## What changed since the microservice sample

Last time, recording a borrow **called** book-service and **waited**:

```java
BookDTO book = bookClient.getBook(dto.getBookId());   // call, and wait
```

Remember what that cost: when book-service was down, the caller failed too -
that was the whole of step 5. A chain of calls is only as strong as its weakest
service.

Today, recording a borrow **announces** it and moves on:

```java
kafkaTemplate.send("borrows", borrowJson);            // announce, move on
```

The announcement is called an **event**: a small message that says *something
happened*. Events go to a **broker** (Kafka) - a program whose only job is to
receive events and hand them to everyone who subscribed. The producer does not
know who listens. Stop a listener and nothing else breaks; the broker keeps the
events until it comes back.

That is **publish/subscribe**, and it is the heart of event-driven
architecture.

## The pieces

| What | Role | Port |
| --- | --- | --- |
| ZooKeeper + **Kafka** | the broker: keeps the events, serves the subscribers | 9092 / 9094 |
| `library-borrow-service` | **producer**: announces each borrow on topic `borrows` | 8200 |
| `library-notification-service` | **subscriber**: writes a notification per borrow | 8201 |
| `library-popularity-service` | **subscriber**: counts borrows per book | 8202 |

```
                              +--------------------------+
 POST /borrows                |   Kafka broker  :9094    | --->  notification-service :8201
-----------> borrow-service ->|   topic "borrows"        |
                    :8200     |   (a log of events)      | --->  popularity-service :8202
                              +--------------------------+
```

The three services have **no dependency on each other** - open the `pom.xml`
files and check. They only agree on two things: the topic name, and what the
event looks like. Note also what is *gone* since last time: no Eureka. Nobody
calls anybody, so nobody needs to find anybody.

## Before you start

You need JDK 11+, Maven, and **Docker** (for the broker only - the three Java
services run on your own machine, so you can restart them quickly while
coding).

```bash
docker compose up          # starts ZooKeeper and Kafka. The first time is slow
mvn install -DskipTests    # from THIS folder
```

Then start the three services, each in its own terminal:

```bash
mvn -pl library-borrow-service        spring-boot:run    # 8200
mvn -pl library-notification-service  spring-boot:run    # 8201
mvn -pl library-popularity-service    spring-boot:run    # 8202
```

VS Code users: the three run configurations are in `.vscode/launch.json`.

Open the two pages side by side - they reload themselves every 2 seconds:

- <http://localhost:8201/> - notifications
- <http://localhost:8202/> - book popularity

> **One broker, two doors.** A program on your machine reaches Kafka at
> `localhost:9094`. A program inside Docker reaches the same broker at
> `kafka:9092`. This sample runs the services on your machine, so it uses 9094.
> The lab runs them in Docker, so it uses `kafka:9092`. Same broker, two doors -
> knock on the wrong one and you get connection errors.

## Trying it by hand

Import `postman/library-pubsub.postman_collection.json` into Postman, or:

```bash
curl -X POST http://localhost:8200/borrows \
  -H "Content-Type: application/json" \
  -d '{"bookTitle":"1984","memberName":"Alice Johnson"}'
```

## Checking your work

```bash
mvn test
```

12 tests. 6 pass from the start; the other 6 turn green as you do the steps. Do
not edit the tests - make them pass. (`mvn test -fae` shows every module's
result instead of stopping at the first failure.)

---

## Two notes for the curious

**ZooKeeper?** Kafka in this stack keeps its own bookkeeping (which brokers
exist, who leads what) in ZooKeeper, a small coordination service. You never
talk to it yourself. Newer Kafka (4.0, 2025) has replaced ZooKeeper with
**KRaft** and does its own coordination - this course uses the older, still
very common setup.

**The price of events.** Decoupling is not free. The producer cannot know
whether anybody handled its event, an event once published cannot be changed -
only followed by another event - and asking a question like "how popular is
each book?" needs a service that builds the answer out of events, the way
popularity-service does. Event-driven systems trade simplicity of *flow* for
simplicity of *coupling*. You will feel both sides today.
