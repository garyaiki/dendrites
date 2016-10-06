Non blocking HTTP Get requests
![image](https://github.com/garyaiki/dendrites/blob/master/docs/png/TypedQuery%26TypedResponseFlow.png?raw=true)

[TypedQueryFlow](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/org/gs/http/stream/TypedQueryFlow.scala) curries [typedQuery](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/org/gs/http/package.scala), its first argument list is initialized in the flow’s constructor setting up a Get request URL with a case class mapping function, either `caseClassToGetQuery` or a custom function.

When the running flow receives a case class, it’s passed to the second argument list to encode fields in the Get URL query string then makes a non-blocking call to the server and returns a Future HttpResponse.

[mapAsync](http://doc.akka.io/docs/akka/2.4/scala/stream/stages-overview.html#mapAsync) makes the call and handles completion of the Future.

[TypedResponseFlow](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/org/gs/http/stream/TypedResponseFlow.scala) curries [typedResponse](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/org/gs/http/package.scala), the first argument list takes [custom functions mapping the response](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/org/gs/examples/account/http/BalancesService.scala) if its content type is ‘json', mapRight unmarshalls it to a case class, if content type is ‘plain’ or there was an error, mapLeft extracts the error message.

[BalancesProtocols](https://github.com/garyaiki/dendrites/blob/master/src/main/scala/org/gs/examples/account/http/BalancesService.scala) example shows [Spray Json marshalling and unmarshalling](http://doc.akka.io/docs/akka/2.4/scala/http/common/json-support.html#akka-http-spray-json) with case classes.

Define functions to map an error responses and good results to case classes.

```scala
def mapPlain(entity: HttpEntity): Future[Left[String, Nothing]] = {
  Unmarshal(entity).to[String].map(Left(_))
}

def mapChecking(entity: HttpEntity): Future[Right[String, AnyRef]] = {
  Unmarshal(entity).to[CheckingAccountBalances[BigDecimal]].map(Right(_))
}
```

Specify the server's base URL, its path for this request, create query and response flows, and construct the composite flow. Then add a Supervision decider, TypedQueryResponse defines one in its companion object or define your own.

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