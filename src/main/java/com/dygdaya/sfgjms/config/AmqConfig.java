package com.dygdaya.sfgjms.config;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

@Configuration
public class AmqConfig {

    public static final String MY_QUEUE = "my-hello-world";
    public static final String MY_SEND_RCV_QUEUE = "send-n-receive-queue";

    @Value("#{${spring.artemis.broker-url}}")
    private String amqBrokerUrl;
    @Value("#{${amq.connection-cache}}")
    private Integer connectionCache;
    @Value("#{${amq.concurrency}}")
    private String concurrency;
    @Value("#{${amq.connection-transacted}}")
    private String connectionTransacted;
    @Value("#{${amq.message-per-task}}")
    private Integer messagePerTask;
    @Value("#{${spring.artemis.user}}")
    String amqUser;
    @Value("#{${spring.artemis.password}}")
    String amqPassword;

    /**
     * Convert JSON message to JMS Message for sending and receiving message.
     * Typically, you'll use this to pass traceable ID across multiple microservices
     * @return MessageConverter
     */
    @Bean
    public MessageConverter messageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");

        return converter;
    }

    @Bean
    public ConnectionFactory connectionFactory() throws JMSException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL(amqBrokerUrl);
        connectionFactory.setUser(amqUser);
        connectionFactory.setPassword(amqPassword);

        return  connectionFactory;
    }

    @Bean
    public ConnectionFactory cachingConnectionFactory() throws JMSException {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setTargetConnectionFactory(connectionFactory());
        connectionFactory.setSessionCacheSize(connectionCache);
        connectionFactory.setCacheProducers(true);
        connectionFactory.setReconnectOnException(true);

        return connectionFactory;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(
            DefaultJmsListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory) {

        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setConcurrency(concurrency);
        factory.setSessionTransacted(Boolean.valueOf(connectionTransacted));
        factory.setMaxMessagesPerTask(messagePerTask);
        return factory;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerTopicConnectionFactory(DefaultJmsListenerContainerFactoryConfigurer configurer,ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setPubSubDomain(true);
        return factory;
    }

    @Bean
    public JmsTemplate jmsTemplate() throws JMSException {
        JmsTemplate template = new JmsTemplate();
        template.setConnectionFactory(cachingConnectionFactory());
        template.setExplicitQosEnabled(true);
        return template;
    }
}
