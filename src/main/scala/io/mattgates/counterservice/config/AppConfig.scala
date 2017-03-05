package io.mattgates.counterservice.config

import com.typesafe.config.Config

/**
  * Created by mgates on 3/4/17.
  */
case class AppConfig(name: String, address: String, port: Int)

object AppConfig {
  def apply(config: Config): AppConfig = {
    AppConfig(
      config.getString("name"),
      config.getString("bind-address"),
      config.getInt("port")
    )
  }
}
