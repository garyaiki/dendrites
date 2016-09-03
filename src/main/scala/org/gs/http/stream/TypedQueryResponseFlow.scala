/** Copyright 2016 Gary Struthers

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package org.gs.http.stream

/** Send query to Http service. Build a GET request and call the server in queryFlow, handle
  * response in responseHandler flow.
  *
  *	@constructor Creates a composite flow query ++ responseHandler
  * @param query: TypedQueryFlow
  * @param responseHandler: TypedResponseFlow
  * @author Gary Struthers
  *
  */
class TypedQueryResponseFlow(query: TypedQueryFlow, responseHandler: TypedResponseFlow) {

  val flow = query.flow.via(responseHandler.flow)
}
