package org.gs.http.stream

/** Send query to Http service. Build a GET request and call the server in queryFlow, handle
  * response in responseHandler flow. Compose both flows into a single flow.
  *
  * @author Gary Struthers
  *
  */
class TypedQueryResponseFlow(query: TypedQueryFlow, responseHandler: TypedResponseFlow) {

  val flow = query.flow.via(responseHandler.flow)
}
