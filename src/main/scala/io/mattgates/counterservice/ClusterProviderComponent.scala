package io.mattgates.counterservice

import ckite.rpc.FinagleThriftRpc
import ckite.{CKite, CKiteBuilder}
import io.mattgates.counterservice.config.CKiteConfig

/**
  * Created by mgates on 3/4/17.
  */
trait ClusterProviderComponent {
  val cluster: CKite

  object ClusterProvider {
    def apply(config: CKiteConfig, store: KVStore): CKite = {
      CKiteBuilder().listenAddress(s"${config.address}:${config.port}")
        .rpc(FinagleThriftRpc)
        .stateMachine(store)
        .bootstrap(config.bootstrap)
        .build
    }
  }
}
