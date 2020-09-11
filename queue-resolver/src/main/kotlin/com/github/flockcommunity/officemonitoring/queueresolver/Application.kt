/*
 * Copyright (c) 2019 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.flockcommunity.officemonitoring.queueresolver

import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.Delivery
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.AmqpAdmin
import org.springframework.amqp.core.Queue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.amqp.RabbitProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.rabbitmq.*
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@SpringBootApplication
class SpringBootSample {
    @Autowired
    var connectionMono: Mono<Connection?>? = null

    @Autowired
    var amqpAdmin: AmqpAdmin? = null

    // the mono for connection, it is cached to re-use the connection across sender and receiver instances
    // this should work properly in most cases
    @Bean
    fun connectionMono(rabbitProperties: RabbitProperties?): Mono<Connection?>? {
        val connectionFactory = ConnectionFactory()
        connectionFactory.host = rabbitProperties!!.host
        connectionFactory.port = rabbitProperties.port
        connectionFactory.username = rabbitProperties.username
        connectionFactory.password = rabbitProperties.password
        return Mono.fromCallable { connectionFactory.newConnection("reactor-rabbit") }.cache()
    }

    @Bean
    fun sender(connectionMono: Mono<Connection?>?): Sender? {
        return RabbitFlux.createSender(SenderOptions().connectionMono(connectionMono))
    }

    @Bean
    fun receiver(connectionMono: Mono<Connection?>?): Receiver? {
        return RabbitFlux.createReceiver(ReceiverOptions().connectionMono(connectionMono))
    }

    @Bean
    fun deliveryFlux(receiver: Receiver?): Flux<Delivery?>? {
        return receiver?.consumeNoAck(QUEUE)
    }

    @PostConstruct
    fun init() {
        amqpAdmin?.declareQueue(Queue(QUEUE, false, false, true))
    }

    @PreDestroy
    @kotlin.jvm.Throws(Exception::class)
    fun close() {
        connectionMono?.block()?.close()
    }

    // a runner that publishes messages with the sender bean and consumes them with the receiver bean
    @Component
    internal class Runner(sender: Sender?, deliveryFlux: Flux<Delivery?>?) : CommandLineRunner {
        val sender: Sender? = sender
        val deliveryFlux: Flux<Delivery?>? = deliveryFlux
        val latchCompleted: AtomicBoolean? = AtomicBoolean(false)

        @kotlin.jvm.Throws(Exception::class)
        override fun run(vararg args: String?) {
            val messageCount = 10
            val latch = CountDownLatch(messageCount)
            deliveryFlux?.subscribe { m ->
                log.info("Received message {}", String(m?.body ?: ByteArray(0)))
                latch.countDown()
            }
            log.info("Sending messages...")
            sender?.send(Flux.range(1, messageCount+1).delayElements(Duration.ofMillis(500)).map{ i ->
                val message = "Message_$i"
                log.info("Sending message: '$message' ")
                OutboundMessage("", QUEUE, message.toByteArray()) })
                    ?.subscribe()
            latchCompleted!!.set(latch.await(5, TimeUnit.SECONDS))
        }

    }

    companion object {
        const val QUEUE: String = "reactor.rabbitmq.spring.boot"
        private val log: Logger = LoggerFactory.getLogger(SpringBootSample::class.java)
    }
}


fun main(args: Array<String>) {
    runApplication<SpringBootSample>(*args)
}