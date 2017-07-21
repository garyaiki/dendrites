### HTTP, JSON streaming components

{% include nav.html %}

Non-blocking pre-built HTTP client stages and JSON mapping to case classes. Streams can be both clients and servers.

[<img src="png/TypedQueryFlow.png?raw=true" alt="TypedQueryFlow" width="20%" height="20%" title="input case class, output Future of HTTPResponse to Get query">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/http/stream/TypedQueryFlow.scala){:target="_blank"}
[<img src="png/TypedResponseFlow.png?raw=true" alt="TypedResponseFlow" width="20%" height="20%" title="input HTTPResponse, output Either Right is mapped case class Either Left is error message">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/http/stream/TypedResponseFlow.scala){:target="_blank"}

[<img src="png/TypedQueryResponseFlow.png?raw=true" alt="TypedQueryResponseFlow" width="50%" height="50%" title="input case class, output Either Right is mapped case class Either Left is error message">](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/http/stream/TypedQueryResponseFlow.scala){:target="_blank"}
###### Click image to open source code in a new tab. Hover over image for stage inputs and outputs

#### GET request/response with JSON/case class mapping

<img src="png/TypedQuery%26TypedResponseFlow.png?raw=true" width="50%" />

[TypedQueryFlow](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/http/stream/TypedQueryFlow.scala){:target="_blank"} takes advantage of the natural (but unappreciated!) fit of [currying](http://docs.scala-lang.org/tutorials/tour/currying.html){:target="_blank"} and streaming. It's initialized with a base URL, base path, and mapping function. These don't change over the stream's lifetime. They are the first parameter list of [typedQuery](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/http/package.scala){:target="_blank"}, it becomes a curried function. Mapping  turns a case class into a fully constructed GET request. `caseClassToGetQuery` maps case classes with [basic argument types](http://www.artima.com/pins1ed/basic-types-and-operations.html){:target="_blank"}, a custom mapping function is needed for complex types.

When TypedQueryFlow pulls a case class, it’s passed to `typeQuery`'s second argument list. Case class fields are mapped to a complete request string. An [HTTPRequest](http://doc.akka.io/api/akka-http/current/akka/http/scaladsl/model/HttpRequest.html){:target="_blank"} is sent to the server, returning an [HttpResponse](http://doc.akka.io/api/akka-http/current/akka/http/scaladsl/model/HttpResponse.html){:target="_blank"} Future.

[mapAsync](http://doc.akka.io/docs/akka/current/scala/stream/stages-overview.html#mapasync){:target="_blank"} is one of Akka Streams' built in stages, it  calls the server, handles the Future's completion and pushes the HTTPResponse.

[TypedResponseFlow](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/http/stream/TypedResponseFlow.scala){:target="_blank"} also marries currying to streaming. HTTP can throw exceptions or return an error messages. Scala's [Either](http://www.scala-lang.org/api/current/scala/util/Either.html){:target="_blank"} fits here: Left for errors, Right for JSON values.

[typedResponse](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/http/package.scala){:target="_blank"}, first argument list takes [custom functions mapping the response](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/account/http/BalancesService.scala){:target="_blank"}, if the HttpResponse's content type is ‘json', `mapRight` unmarshalls it to a case class, if its content type is ‘plain’, or there was an error, `mapLeft` extracts the error message.

[BalancesProtocols](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/account/http/BalancesService.scala){:target="_blank"} shows [JSON marshalling and unmarshalling](http://doc.akka.io/docs/akka-http/current/scala/http/common/json-support.html#spray-json-support){:target="_blank"} with case classes. A UDF maps errors to strings and good results to case classes.

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

<p >
<img src="png/TypedQueryResponseFlow.png?raw=true" width="50%" />
</p>

Request case class in, result case class out, non-blocking

<img src="png/ParallelHttpFlow.png?raw=true" width="80%" />

#### POST, PUT, and DELETE methods
[Akka HTTP](http://doc.akka.io/docs/akka-http/current/scala/http/index.html){:target="_blank"} built-in components and [examples](http://doc.akka.io/docs/akka-http/current/scala/http/client-side/index.html){:target="_blank"} support these methods on the client side.


#### Non-blocking calls to parallel services and combined responses.
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

#### Akka HTTP servers
Akka HTTP's high level [routing DSL](http://doc.akka.io/docs/akka-http/current/scala/http/routing-dsl/index.html){:target="_blank"} is elegant, easy to code, and readable for non-programmers. [BalancesService](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/com/github/garyaiki/dendrites/examples/account/http/BalancesService.scala) shows an example server with a GET request handler.

#### Stand-alone Functions
```scala
val hostConfig: (Config, String, Int) = getHostConfig("dendrites.checking-balances.http.interface","my.http.port")
```
###### Get Config, ip address, port as tuple3
```scala
val baseURL: StringBuilder = configBaseUrl("my.http.path", hostConfig)
```
###### Append path to host URL
```scala
val url: StringBuilder = createUrl(scheme, ipDomain, port, path) 
```
###### Create URL including path
```scala
val balanceQuery = GetAccountBalances(goodId)
val checkingPath = "/account/balances/checking/"
val balancesQuery: StringBuilder = caseClassToGetQuery(balanceQuery)()
val q = checkingPath ++ balancesQuery 
```
###### Create GET request with request path i.e. "?", "key=value" for case classes with only basic field types
```scala
val callFuture: Future[HttpResponse] = typedQuery(GetAccountBalances(id), clientConfig.baseURL)
```
###### Create GET request from URL with path and GET arguments mapped to JSON from case class. Call it.
```scala
partial: HttpResponse => Future[Either[String, AnyRef]] = typedResponse(mapLeft, mapRight) _ // curried
``` 
###### Handle response, mapPlain for exception & server error messages, mapRight for JSON result, 2nd arg list `callFuture` can be curried.
```scala
val id = 1L
val cc = GetAccountBalances(id)
val callFuture: Future[HttpResponse] = typedQuery(baseURL, cc.productPrefix, caseClassToGetQuery)(cc)
val future: Future[Either[String, AnyRef]] = typedFutureResponse(mapPlain, mapChecking)(callFuture)
```
###### Map Future[HttpResponse} to a Future[Either] Left for error, Right for good result.
```scala
val partial: Product => Future[Either[String, AnyRef]] = typedQueryResponse(baseURL, mapPlain, mapChecking) _ // curried
val responseFuture: Future[Either[String, AnyRef]] = partial(GetAccountBalances(id))
```
###### Combine query & response, call it with first argument list once. Call curried function with second argument list for each case class to query on.

#### Example Configurations

[Typesafe Config](https://github.com/typesafehub/config){:target="_blank"} example, and optional, config settings for HTTP are in `src/main/resources/reference.conf`. You can choose to use Typesafe Config and override these in your application's `src/main/resources/application.conf`.

