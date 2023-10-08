package com.odinbook.postservice.config;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.dsl.Amqp;
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
    @Bean
    public IntegrationFlow amqpOutbound(AmqpTemplate amqpTemplate,
                                      @Qualifier("toRabbit") MessageChannel amqpOutboundChannel) {
        return IntegrationFlow.from(amqpOutboundChannel)
                .handle(Amqp.outboundAdapter(amqpTemplate)
                        .routingKey("odinBook.notificationChannel"))
                .get();
    }
}
