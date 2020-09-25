//package flock.community.office.monitoring.sensoringestion.websocket
//
//import com.fasterxml.jackson.annotation.JsonProperty
//import com.fasterxml.jackson.databind.ObjectMapper
//import com.fasterxml.jackson.module.kotlin.readValue
//import org.springframework.stereotype.Component
//import org.springframework.web.socket.*
//import org.springframework.web.socket.client.WebSocketConnectionManager
//import org.springframework.web.socket.client.standard.StandardWebSocketClient
//
//
//@Component
//class SensorWebsocket(
//        val flockWebsocketHandler: FlockWebsocketHandler
//) {
//
//    init {
//        WebSocketConnectionManager(
//                StandardWebSocketClient(),
//                flockWebsocketHandler,  //Must be defined to handle messages
//                "ws://192.168.1.84:443").run { this.start() }
//    }
//
//
//}
//
//@Component
//class FlockWebsocketHandler(
//        val objectMapper: ObjectMapper
//) : WebSocketHandler {
//
//    override fun afterConnectionEstablished(p0: WebSocketSession) {
//        println("afterConnectionEstablished")
//    }
//
//    override fun handleMessage(p0: WebSocketSession, message: WebSocketMessage<*>) {
//        val sensorEvent: SensorEvent = objectMapper.readValue((message.payload as TextMessage).payload)
//        println("handleMessage" + sensorEvent)
//    }
//
//    override fun handleTransportError(p0: WebSocketSession, p1: Throwable) {
//        println("handleTransportError")
//    }
//
//    override fun afterConnectionClosed(p0: WebSocketSession, p1: CloseStatus) {
//        println("afterConnectionClosed")
//    }
//
//    override fun supportsPartialMessages(): Boolean {
//        return false
//    }
//}
//
//data class SensorEvent(
//        @JsonProperty("e")
//        val event: String
//)
//
