package org.gs.cassandra

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