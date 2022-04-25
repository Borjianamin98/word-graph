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
 kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic topic --from-beginning
```