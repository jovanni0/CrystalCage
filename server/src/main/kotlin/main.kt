package dev.jovanni0

import dev.jovanni0.data_types.DiscoveryReply
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.netty.EngineMain
import kotlinx.serialization.json.Json
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.concurrent.thread


fun main(args: Array<String>)
{
    val config = ApplicationConfig("application.yaml")
    val api_port = config.propertyOrNull("ktor.deployment.port")?.getString()?.toInt() ?: 8080
    val discovery_port = config.propertyOrNull("ktor.deployment.discovery_port")?.getString()?.toInt() ?: 8081

    val server_name = config.propertyOrNull("server.name")?.getString() ?: "Unknown Sanctum Server"
    val use_https = config.propertyOrNull("server.https")?.getString()?.toBoolean() ?: false


    println("server trying to listen for API on port $api_port, with discovery on port $discovery_port")

    startDiscoveryListener(api_port, discovery_port, server_name, use_https)
    EngineMain.main(args)
}


/**
 * usable only by native clients. requires them sending UDP datagram, which is not possible for browser-based clients.
 */
fun startDiscoveryListener(api_port: Int, discovery_port: Int, server_name: String, use_https: Boolean)
{
    thread(isDaemon = true)
    {
        val socket = DatagramSocket(discovery_port, InetAddress.getByName("0.0.0.0"))
        socket.broadcast = true
        val buf = ByteArray(1024)

        while (true)
        {
            val packet = DatagramPacket(buf, buf.size)
            socket.receive(packet)
            val msg = String(packet.data, 0, packet.length)

            if (msg == "DISCOVER_SANCTUM")
            {
                val reply = Json
                    .encodeToString(DiscoveryReply(name = server_name, port = api_port, use_https))
                    .toByteArray()
                socket.send(DatagramPacket(reply, reply.size, packet.address, packet.port))
            }
        }
    }
}