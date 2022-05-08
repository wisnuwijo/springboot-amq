package com.dygdaya.sfgjms.sender;

import com.dygdaya.sfgjms.config.AmqConfig;
import com.dygdaya.sfgjms.model.HelloWorldMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.jms.*;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class HelloSender {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedRate = 2000)
    public void sendMessage() throws JMSException, JsonProcessingException {
         System.out.println("[1] I'm sending a helloWorldMessage");

        HelloWorldMessage helloWorldMessage = new HelloWorldMessage()
                .builder()
                .id(UUID.randomUUID())
                .message("Hello world")
                .build();

        jmsTemplate.convertAndSend(AmqConfig.MY_QUEUE, helloWorldMessage);
        System.out.println("Message sent!");
    }

    @Scheduled(fixedRate = 2000)
    public void sendAndReceiveMessage() throws JMSException {
        HelloWorldMessage message = new HelloWorldMessage()
                .builder()
                .id(UUID.randomUUID())
                .message("Hello")
                .build();

        Message receivedMsg = jmsTemplate.sendAndReceive(AmqConfig.MY_SEND_RCV_QUEUE, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                Message helloMessage = null;
                try {
                    helloMessage = session.createTextMessage(objectMapper.writeValueAsString(message));
                    helloMessage.setStringProperty("_type","com.dygdaya.sfgjms.model.HelloWorldMessage");

                    System.out.println("[2] Sending Hello");

                    return helloMessage;
                } catch (JsonProcessingException e) {
                    e.printStackTrace();

                    throw new JMSException(e.getMessage());
                }
            }
        });

        System.out.println("Message sent!");
        System.out.println("Message received : " + receivedMsg.getBody(HelloWorldMessage.class));
    }
}
