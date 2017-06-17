### HTTP, JSON streaming components

{% include nav.html %}
Build HTTP streaming clients with pre-built HTTP stages and JSON mapping to case classes.

[<img src="png/TypedQueryFlow.png?raw=true" alt="TypedQueryFlow" width="20%" height="20%" title="input case class, output Future of HTTPResponse to Get query">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/http/stream/TypedQueryFlow.scala){:target="_blank"}
[<img src="png/TypedResponseFlow.png?raw=true" alt="TypedResponseFlow" width="20%" height="20%" title="input HTTPResponse, output Either Right is mapped case class Either Left is error message">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/http/stream/TypedResponseFlow.scala){:target="_blank"}
[<img src="png/TypedQueryResponseFlow.png?raw=true" alt="TypedQueryResponseFlow" width="50%" height="50%" title="input case class, output Either Right is mapped case class Either Left is error message">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/http/stream/TypedQueryResponseFlow.scala){:target="_blank"}
###### Click image to open source code in a new tab. Hover over image for stage inputs and outputs
##### Akka HTTP servers
Akka HTTP's high level [routing DSL](http://doc.akka.io/docs/akka-http/current/scala/http/routing-dsl/index.html){:target="_blank"} is elegant and easy to read. [BalancesService](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/account/http/BalancesService.scala) shows an example GET request handler.

##### POST, PUT, and DELETE methods
[Akka HTTP](http://doc.akka.io/docs/akka-http/current/scala/http/index.html){:target="_blank"} built-in components and examples fully support these methods on the client side.

##### Non blocking GET requests
![image](png/TypedQuery%26TypedResponseFlow.png?raw=true)

[TypedQueryFlow](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/http/stream/TypedQueryFlow.scala){:target="_blank"} takes advantage of the natural (but unappreciated!) fit of [currying](http://docs.scala-lang.org/tutorials/tour/currying.html){:target="_blank"} and streaming. It's initialized with a base URL, base path, and mapping function. These don't change over the streams lifetime. They are the first parameter list of [typedQuery](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/http/package.scala){:target="_blank"}, it becomes a curried function. Mapping  turns a case class into a fully constructed GET request. `caseClassToGetQuery` maps case classes with [basic argument types](http://www.artima.com/pins1ed/basic-types-and-operations.html){:target="_blank"}. A custom function is needed with complex types.

When TypedQueryFlow pulls a case class, it’s passed to `typeQuery`'s second argument list. Case class fields are mapped to a complete request string. An [HTTPRequest](http://doc.akka.io/api/akka-http/current/akka/http/scaladsl/model/HttpRequest.html){:target="_blank"} is sent to the server, returning an [HttpResponse](http://doc.akka.io/api/akka-http/current/akka/http/scaladsl/model/HttpResponse.html){:target="_blank"} Future.

[mapAsync](http://doc.akka.io/docs/akka/current/scala/stream/stages-overview.html#mapasync){:target="_blank"} is one of Akka Streams' built in stages, it makes the call, handles completion of the Future and pushes the HTTPResponse.

[TypedResponseFlow](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/http/stream/TypedResponseFlow.scala){:target="_blank"} also marries currying to streaming. HTTP can throw exceptions or return an error messages. Scala's [Either](http://www.scala-lang.org/api/current/scala/util/Either.html){:target="_blank"} fits here: Left for errors, Right for JSON values.

[typedResponse](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/http/package.scala){:target="_blank"}, first argument list takes [custom functions mapping the response](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/account/http/BalancesService.scala){:target="_blank"}, if the HttpResponse's content type is ‘json', `mapRight` unmarshalls it to a case class, if its content type is ‘plain’, or there was an error, `mapLeft` extracts the error message.

[BalancesProtocols](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/account/http/BalancesService.scala){:target="_blank"} shows [JSON marshalling and unmarshalling](http://doc.akka.io/docs/akka-http/10.0.7/scala/http/common/json-support.html#spray-json-support){:target="_blank"} with case classes. A user defines functions to map errors to strings and good results to case classes.

```scala
def mapPlain(entity: HttpEntity): Future[Left[String, Nothing]] = {
  Unmarshal(entity).to[String].map(Left(_))
}

def mapChecking(entity: HttpEntity): Future[Right[String, AnyRef]] = {
  Unmarshal(entity).to[CheckingAccountBalances[BigDecimal]].map(Right(_))
}
```

Then specify the server's base URL, request path, query and response flows, and construct the composite flow. You can optionally add a [Supervision](http://doc.akka.io/docs/akka/current/scala/stream/stream-error.html){:target="_blank"} decider for error handling. `TypedQueryResponse` defines a decider in its companion object or use your own.

```scala
val baseURL = clientConfig.baseURL
val requestPath = clientConfig.requestPath
val queryFlow = new TypedQueryFlow(baseURL, requestPath,caseClassToGetQuery)
val responseFlow = new TypedResponseFlow(mapPlain, mapChecking)

val tqr = new TypedQueryResponseFlow(queryFlow, responseFlow)

def flow: Flow[Product, Either[String, AnyRef], NotUsed] = {
  val flow = tqr.flow
  flow.withAttributes(ActorAttributes.supervisionStrategy(decider))
}
```

[TypedQueryResponseFlow](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/http/stream/TypedQueryResponseFlow.scala){:target="_blank"} composes `TypedQuery` and `TypedResponse` to a single flow.

![image](png/TypedQueryResponseFlow.png?raw=true)

Request case class in, result case class out, non-blocking

![image](png/ParallelHttpFlow.png?raw=true)
###### Non-blocking calls to parallel services and combined responses.

[ZipWith](http://doc.akka.io/docs/akka/current/scala/stream/stages-overview.html#zipWith){:target="_blank"} takes outputs of all services and pushes them as a tuple when they are all ready.

```scala
def zipper = ZipWith((in0: Either[String, AnyRef],
  in1: Either[String, AnyRef],
  in2: Either[String, AnyRef]) => (in0, in1, in2))

val ccf = new CheckingCallFlow
val mmcf = new MoneyMarketCallFlow
val scf = new SavingsCallFlow

import GraphDSL.Implicits._
// Create Graph in Shape of a Flow
val flowGraph = GraphDSL.create() { implicit builder =>
  val bcast: UniformFanOutShape[Product, Product] =     builder.add(Broadcast[Product](3))
  val check: FlowShape[Product,Either[String, AnyRef]] = builder.add(ccf.flow)
  val mm: FlowShape[Product,Either[String, AnyRef]] = builder.add(mmcf.flow)
  val savings: FlowShape[Product,Either[String, AnyRef]] = builder.add(scf.flow)
  val zip = builder.add(zipper)

  bcast ~> check ~> zip.in0
  bcast ~> mm ~> zip.in1
  bcast ~> savings ~> zip.in2
  FlowShape(bcast.in, zip.out)
}.named("calls")
// Cast Graph to Flow
val asFlow = Flow.fromGraph(flowGraph)
// Map tuple3 from flowGraph
val fgLR = GraphDSL.create() { implicit builder =>
  val fgCalls = builder.add(asFlow)
  val fgLR = builder.add(leftRightFlow) // results combiner

  fgCalls ~> fgLR
  FlowShape(fgCalls.in, fgLR.outlet)
}.named("callsLeftRight")
val wrappedCallsLRFlow = Flow.fromGraph(fgLR)
```