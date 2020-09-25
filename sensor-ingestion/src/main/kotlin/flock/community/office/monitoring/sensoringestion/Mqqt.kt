//package flock.community.office.monitoring.sensoringestion
//
//import org.eclipse.paho.client.mqttv3.MqttAsyncClient
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.ComponentScan
//import org.springframework.context.annotation.Configuration
//import org.springframework.integration.annotation.IntegrationComponentScan
//import org.springframework.integration.annotation.MessagingGateway
//import org.springframework.integration.annotation.ServiceActivator
//import org.springframework.integration.channel.DirectChannel
//import org.springframework.integration.core.MessageProducer
//import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory
//import org.springframework.integration.mqtt.core.MqttPahoClientFactory
//import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter
//import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler
//import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter
//import org.springframework.messaging.Message
//import org.springframework.messaging.MessageChannel
//import org.springframework.messaging.MessageHandler
//import org.springframework.messaging.MessagingException
//
//
//@Configuration
//@ComponentScan
//@EnableAutoConfiguration
//@IntegrationComponentScan
//class MQtt() {
//
//    init {
//
//
//        MqttAsyncClient("")
//    }
//
//
//    @Bean
//    fun mqttInputChannel(): MessageChannel? {
//        return DirectChannel()
//    }
//
//    @Bean
//    fun mqttClientFactory(): MqttPahoClientFactory? {
//        val factory = DefaultMqttPahoClientFactory()
//
//        factory.connectionOptions.serverURIs = listOf("tcp://192.168.1.84:1883").toTypedArray()
//
//
//        return factory
//    }
//
//    @Bean
//    fun inbound(): MessageProducer? {
//        val adapter = MqttPahoMessageDrivenChannelAdapter(MqttAsyncClient.generateClientId(), mqttClientFactory(),
//                "zigbee2mqtt")
//        adapter.setCompletionTimeout(5000)
//        adapter.setConverter(DefaultPahoMessageConverter())
//        adapter.setQos(1)
//        adapter.setOutputChannel(mqttInputChannel())
//        return adapter
//    }
//
//    @Bean
//    @ServiceActivator(inputChannel = "mqttInputChannel")
//    fun handler(): MessageHandler? {
//
//        return MessageHandler {
//            System.out.println("!!!!!!!!!!!!!!!!!!!" + it.getPayload())
//
//        }
//
////        return object : MessageHandler() {
////            @kotlin.Throws(MessagingException::class)
////            fun handleMessage(message: Message<*>) {
////                System.out.println("!!!!!!!!!!!!!!!!!!!" + message.getPayload())
////            }
////        }
//    }
//
//    @Bean
//    @ServiceActivator(inputChannel = "mqttOutboundChannel")
//    fun mqttOutbound(): MessageHandler? {
//        val messageHandler = MqttPahoMessageHandler("testClient", mqttClientFactory())
//        messageHandler.setAsync(true)
//        messageHandler.setDefaultTopic("test")
//        return messageHandler
//    }
//
//    @Bean
//    fun mqttOutboundChannel(): MessageChannel? {
//        return DirectChannel()
//    }
//
//    @MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
//    interface MyGateway {
//        fun sendToMqtt(data: String?)
//    }
//
//}