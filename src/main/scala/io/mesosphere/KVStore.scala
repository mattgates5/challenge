package io.mesosphere

import java.nio.ByteBuffer
import scala.collection.mutable.Map
import ckite.rpc.ReadCommand
import ckite.rpc.WriteCommand
import ckite.statemachine.StateMachine
import ckite.util.Serializer

/**
  * Adapted by mgates on 10/9/16.
  * CKite Key-Value Store
  * An in-memory distributed Map allowing Puts and Gets operations
  * https://github.com/pablosmedina/ckite
  */
class KVStore extends StateMachine{
  private var map = Map[String, Int]()
  private var lastIndex: Long = 0

  // Called when consensus reached for a write
  def applyWrite = {
    case (index, Put(key: String, value: Int)) => {
      map.put(key, value)
      lastIndex = index
      value
    }
  }

  // Called when a read has been received
  def applyRead = {
    case Get(key) => map.get(key)
  }

  // Know the last applied write on log replay to provide 'exactly-once' semantics
  def getLastAppliedIndex: Long = lastIndex

  // Called during Log replay on startup and upon installSnapshot requests
  def restoreSnapshot(byteBuffer: ByteBuffer) = {
    map = Serializer.deserialize[Map[String, Int]](byteBuffer.array())
  }

  // Called when Log compaction is required
  def takeSnapshot(): ByteBuffer = ByteBuffer.wrap(Serializer.serialize(map))
}

// Case classes for handling gets and puts
case class Put(key: String, value: Int) extends WriteCommand[String]
case class Get(key: String) extends ReadCommand[Option[String]]
