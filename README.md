# dendrites

Aggregate service results without blocking or futures. Uses [Akka Aggregator Pattern](http://doc.akka.io/docs/akka/snapshot/contrib/aggregator.html) to call many services through actors and collect their results. 
Results are reduced to a single value using [Twitter Algebird](https://github.com/twitter/algebird)