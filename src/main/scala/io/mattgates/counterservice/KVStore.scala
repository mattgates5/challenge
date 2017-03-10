package io.mattgates.counterservice

import java.nio.ByteBuffer

import ckite.rpc.{ReadCommand, WriteCommand}
import ckite.statemachine.StateMachine
import ckite.util.Serializer

import scala.collection.mutable

/**
  * Adapted by mgates on 10/9/16.
  * CKite Key-Value Store
  * An in-memory distributed Map allowing Puts and Gets operations
  * https://github.com/pablosmedina/ckite
  */

// Case classes for handling gets and puts
final case class Put(key: String, value: Int) extends WriteCommand[Int]
final case class Get(key: String) extends ReadCommand[Option[Int]]

class KVStore extends StateMachine {
  private var map = mutable.Map[String, Int]()
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

  // Direct local read from the store
  def directRead(key: String): Option[Int] = {
    map.get(key)
  }

  // Know the last applied write on log replay to provide 'exactly-once' semantics
  def getLastAppliedIndex: Long = lastIndex

  // Called during Log replay on startup and upon installSnapshot requests
  def restoreSnapshot(byteBuffer: ByteBuffer) = {
    map = Serializer.deserialize[mutable.Map[String, Int]](byteBuffer.array())
  }

  // Called when Log compaction is required
  def takeSnapshot(): ByteBuffer = ByteBuffer.wrap(Serializer.serialize(map))
}
