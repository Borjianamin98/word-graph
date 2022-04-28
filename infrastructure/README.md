# Infrastructure Commands

## MongoDB

Some useful commands:

```bash
mongo --username root --password password
show dbs;
show collections;

use linksDatabase;
db.links.find();
db.links.remove({})
```


## Kafka

Consume from a Kafka topic named `topic`:

```bash
 kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic <topic> --from-beginning
```

Reset offset of Kafka consumer group:

```bash
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group <group_id> --describe
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group <group_id> --reset-offsets --to-earliest --all-topics --execute
```

Delete a Kafka topic:

```bash
kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic <topic>
```