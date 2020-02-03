package org.scash.core.p2p

import org.scash.core.config.NetworkParameters
import org.scash.core.protocol.NetworkElement
import org.scash.core.serializers.p2p.RawNetworkMessageSerializer
import org.scash.core.util.Factory
import scodec.bits.ByteVector

/**
  * Represents a P2P network message
  */
sealed abstract class NetworkMessage extends NetworkElement {
  require(
    header.payloadSize.toInt == payload.bytes.length,
    s"Payload size is not what header says it is, " +
      s"header.payloadSize=${header.payloadSize.toInt} actual=${payload.bytes.length}"
  )
  def header: NetworkHeader
  def payload: NetworkPayload
  override def bytes: ByteVector = RawNetworkMessageSerializer.write(this)

  override def toString(): String = s"NetworkMessage($header, $payload)"
}

object NetworkMessage extends Factory[NetworkMessage] {
  private case class NetworkMessageImpl(
      header: NetworkHeader,
      payload: NetworkPayload)
      extends NetworkMessage

  def fromBytes(bytes: ByteVector): NetworkMessage =
    RawNetworkMessageSerializer.read(bytes)

  /**
    * Creates a network message from it's [[NetworkHeader]] and [[NetworkPayload]]
    * @param header the [[NetworkHeader]] which is being sent across the network
    * @param payload the [[NetworkPayload]] which contains the information being sent across the network
    * @return
    */
  def apply(header: NetworkHeader, payload: NetworkPayload): NetworkMessage = {
    NetworkMessageImpl(header, payload)
  }

  /**
    * Creates a [[NetworkMessage]] out of it's [[NetworkPayload]]
    * @param network the [[org.scash.core.config.NetworkParameters NetworkParameters]] indicating the network which the message is going to be sent on
    * @param payload the payload that needs to be sent across the network
    * @return
    */
  def apply(
      network: NetworkParameters,
      payload: NetworkPayload): NetworkMessage = {
    val header = NetworkHeader(network, payload)
    NetworkMessage(header, payload)
  }
}
