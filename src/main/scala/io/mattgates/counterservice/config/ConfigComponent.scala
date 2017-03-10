package io.mattgates.counterservice.config

import com.typesafe.config.{Config, ConfigFactory}

/**
  * Created by mgates on 3/4/17.
  */
trait ConfigComponent {
  val config: Config = ConfigFactory.load()
  val appConfig: AppConfig = AppConfig(config.getConfig("application"))
  val ckiteConfig: CKiteConfig = CKiteConfig(config.getConfig("ckite"))
}
