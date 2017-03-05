package io.mattgates.counterservice.config

import com.typesafe.config.Config

/**
  * Created by mgates on 3/4/17.
  */
case class CKiteConfig(address: String, port: Int, bootstrap: Boolean = false)

object CKiteConfig {
  def apply(config: Config): CKiteConfig = {
    CKiteConfig(
      config.getString("bind-address"),
      config.getInt("port"),
      System.getProperty("ckite.bootstrap", config.getString("bootstrap")).toBoolean
    )
  }
}
