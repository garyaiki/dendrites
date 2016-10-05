[CallStream](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/org/gs/stream/actor/CallStream.scala) is a generic Actor that runs streams.
![image](https://github.com/garyaiki/dendrites/blob/master/docs/png/RunnableGraph4Actor.png?raw=true)

[SourceQueue](http://doc.akka.io/docs/akka/2.4/scala/stream/stages-overview.html#queue) is offered messages received by the actor one at a time, the queue immediately returns a Future OfferResult that is piped back to the actor. `offerResultHandler` does nothing if the message was accepted and pushed downstream, logs a warning if the message was dropped, throws an exception if the queue is closed, failures are logged and there exceptions thrown.

CallStream is constructed with a runnable graph with any flows and any sink.

CallStreamSupervisor is a generic supervisor that creates CallStream with a RunnableGraph and handles its errors. An error in the stream invokes a Supervision Decider when an error causes the stream to stop, the actor’s offerResultHandler throws an exception to the supervisor.

CallStreamSupervisor receives messages intended for CallStream and forwards them. This saves actors sending to CallStream from putting a DeathWatch on it.