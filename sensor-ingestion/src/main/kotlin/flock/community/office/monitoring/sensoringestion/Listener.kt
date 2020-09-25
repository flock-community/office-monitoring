//package flock.community.office.monitoring.sensoringestion
//
//import org.springframework.context.annotation.Bean
//import org.springframework.integration.annotation.ServiceActivator
//import org.springframework.integration.channel.DirectChannel
//import org.springframework.integration.dsl.IntegrationFlows
//import org.springframework.integration.handler.LoggingHandler
//import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter
//import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter
//import org.springframework.integration.transformer.GenericTransformer
//import org.springframework.messaging.MessageChannel
//import org.springframework.messaging.MessageHandler
//import org.springframework.scheduling.TaskScheduler
//import org.springframework.stereotype.Component
//
//
//@Bean
//fun mqttInputChannel(): MessageChannel? {
//    return DirectChannel()
//}
//
//
//@Component
//class Receiver(
//        val taskScheduler: TaskScheduler
//) {
//
//    init {
//
//        val adapter = MqttPahoMessageDrivenChannelAdapter("tcp://192.168.1.84:1883", "testClient",
//                "zigbee2mqtt")
//        adapter.setCompletionTimeout(5000)
//        adapter.setConverter(DefaultPahoMessageConverter())
//        adapter.setQos(1)
//        adapter.outputChannel = mqttInputChannel()
//        adapter.setTaskScheduler(taskScheduler)
//
//        val messageChannel = MessageChannel { message, l ->
//
//            println("Hoi: $message")
//            true
//        }
//
//        adapter.setOutputChannel(messageChannel)
//
//
//        adapter.setErrorChannel(MessageChannel { message: org.springframework.messaging.Message<*>, l: kotlin.Long ->
//
//
//            true
//        })
//
//        //adapter.start()
//
//        IntegrationFlows.from(adapter)
//                .transform(GenericTransformer { p: Any -> "$p, received from MQTT" })
//                .handle(logger())
//                .get()
//
//
//
//    }
//
//    private fun logger(): LoggingHandler? {
//        val loggingHandler = LoggingHandler("INFO")
//        loggingHandler.setLoggerName("siSample")
//        return loggingHandler
//    }
//
//    @Bean
//    @ServiceActivator(inputChannel = "mqttInputChannel")
//    fun handler(): MessageHandler? {
//
//
//
//        return MessageHandler {
//
//            println(it.payload)
//        }
//    }
//}
//
//
