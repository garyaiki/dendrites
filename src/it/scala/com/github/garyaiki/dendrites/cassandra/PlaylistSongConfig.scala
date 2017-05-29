/**
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
package com.github.garyaiki.dendrites.cassandra

import com.typesafe.config.ConfigFactory

/** Example configuration for testing */
object PlaylistSongConfig extends CassandraConfig {
  val config = ConfigFactory.load()
  val ipAddress = config.getString("dendrites.cassandra.ipAddress")
  val ipAddresses = config.getStringList("dendrites.cassandra.ipAddresses")
  val keySpace = config.getString("dendrites.cassandra.keySpace")
  val replicationStrategy = config.getString("dendrites.cassandra.replicationStrategy")
  val localDataCenter = config.getString("dendrites.cassandra.localDataCenter")
}
