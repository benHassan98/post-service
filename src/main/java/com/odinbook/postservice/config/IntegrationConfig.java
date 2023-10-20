package com.odinbook.postservice.config;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.amqp.outbound.AmqpOutboundEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.json.ObjectToJsonTransformer;
import org.springframework.messaging.MessageChannel;


@Configuration
public class IntegrationConfig {

    @Bean
    public MessageChannel notificationRequest() {
        return new DirectChannel();
    }
    @Bean
    @Transformer(inputChannel = "notificationRequest", outputChannel = "toRabbit")
    public ObjectToJsonTransformer objectToJsonTransformer() {
        return new ObjectToJsonTransformer();
    }

    @Bean
    public MessageChannel toRabbit() {
        return new DirectChannel();
    }
    @ServiceActivator(inputChannel = "toRabbit")
    @Bean
    public AmqpOutboundEndpoint amqpOutboundEndpoint(AmqpTemplate amqpTemplate) {
        AmqpOutboundEndpoint adapter = new AmqpOutboundEndpoint(amqpTemplate);
        adapter.setRoutingKey("odinBook.notificationChannel");
        return adapter;
    }
}
