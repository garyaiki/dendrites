### Running Akka Stream with Actors

{% include nav.html %}

 .

[Supervision](http://doc.akka.io/docs/akka/current/scala/fault-tolerance.html#default-supervisor-strategy){:target="_blank"} within Akka Streams can Resume a stage, Restart a stage, or Stop the stream. [Actor](http://doc.akka.io/docs/akka/current/scala/actors.html){:target="_blank"} Supervision gives you a second, more powerful way to handle failures and customize recovery.

Streams within an Actor can be initialized by Actors and can use Actors' Supervision.

<img src="png/RunnableGraph4Actor.png?raw=true" width="60%" />

[CallStream](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/stream/actor/CallStream.scala){:target="_blank"} is a generic Actor for running streams. It's created with a [RunnableGraph](http://doc.akka.io/api/akka/current/akka/stream/scaladsl/RunnableGraph.html){:target="_blank"}. [SourceQueue](http://doc.akka.io/docs/akka/current/scala/stream/stages-overview.html#queue){:target="_blank"} must be the Source of the RunnableGraph to take messages received by the Actor. An Actor message of the right type is offered to SourceQueue in CallStream's `receive` method. An OfferResult Future is piped back to CallStream's `offerResultHandler` it logs a warning if the message was dropped or throws an exception if the queue can't accept it. Accepted messages are processed by the stream. Thrown exceptions are handled by Actor Supervisors.

[CallStreamSupervisor](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/stream/actor/CallStreamSupervisor.scala){:target="_blank"} is a generic actor supervisor that creates CallStream with a RunnableGraph and handles its errors. If CallStream’s offerResultHandler throws an exception CallStreamSupervisor has a decider that will either Restart CallStream, Stop it, or Escalate the exception. Messages received by CallStreamSupervisor are forwarded to CallStream. Actors sending messages directly to CallStream should put a [DeathWatch](http://doc.akka.io/docs/akka/current/scala/actors.html#lifecycle-monitoring-aka-deathwatch){:target="_blank"} on it.

CallStream and CallStreamSupervisor are provided for convenience, users are free to combine actors and streams their way.

#### Actor Ref used as a stream's sink
An Actor Supervisor can create both an actor running a stream and another actor used as its Sink.

<img src="png/RunnableGraph2Actors.png?raw=true" width="80%" />

[Sink.actorRef](http://doc.akka.io/docs/akka/current/scala/stream/stages-overview.html#actorref){:target="_blank"} forwards the message pushed from the last flow to another actor. The supervisor creates the sink actor then the stream actor with the sink actorRef. DeathWatch is put on the sink actor, when it dies, the stream actor is restarted with a new actorRef. [example](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/account/http/stream/actor/ParallelCallSupervisor.scala){:target="_blank"}


