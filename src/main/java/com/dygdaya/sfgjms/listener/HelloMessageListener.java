package com.dygdaya.sfgjms.listener;

import com.dygdaya.sfgjms.config.AmqConfig;
import com.dygdaya.sfgjms.model.HelloWorldMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HelloMessageListener {

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = AmqConfig.MY_QUEUE)
    public void listen(
            @Payload HelloWorldMessage helloWorldMessage,
            @Headers MessageHeaders headers, Message message
            ) {
        System.out.println("New msg received");
        System.out.println(helloWorldMessage);

        /**
         * Whenever there's an exception,
         * the AMQ will notice the failure and requeue the message
         */
        // throw new RuntimeException("Hola");
    }

    @JmsListener(destination = AmqConfig.MY_SEND_RCV_QUEUE)
    public void listenForHello(
            @Payload HelloWorldMessage helloWorldMessage,
            @Headers MessageHeaders headers, Message message
    ) throws JMSException {
        HelloWorldMessage payloadMsg = new HelloWorldMessage()
                .builder()
                .id(UUID.randomUUID())
                .message("World")
                .build();

        System.out.println("message.getJMSReplyTo() : "+ message.getJMSReplyTo());
        jmsTemplate.convertAndSend(message.getJMSReplyTo(),payloadMsg);
    }
}
