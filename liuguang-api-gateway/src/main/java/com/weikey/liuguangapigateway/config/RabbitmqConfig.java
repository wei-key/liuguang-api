package com.weikey.liuguangapigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.weikey.liuguangapicommon.constant.RabbitMQConstant.*;

/**
 * 声明交换机和队列
 */
@Configuration
@Slf4j
public class RabbitmqConfig {


    // 声明交换机
    @Bean
    public DirectExchange interfaceExchange(){
        return ExchangeBuilder.directExchange(INTERFACE_EXCHANGE_NAME).build();
    }

    // 声明死信队列
    @Bean
    public Queue interfaceQueue(){
        return QueueBuilder.durable(INTERFACE_QUEUE_NAME).build();
    }

    // 交换机绑定队列
    @Bean
    public Binding binding(DirectExchange interfaceExchange, Queue interfaceQueue){
        return BindingBuilder.bind(interfaceQueue).to(interfaceExchange).with(INTERFACE_ROUTINGKEY);
    }
}