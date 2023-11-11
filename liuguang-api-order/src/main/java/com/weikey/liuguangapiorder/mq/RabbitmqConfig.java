package com.weikey.liuguangapiorder.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 声明订单支付超时需要用到的交换机和队列
 */
@Configuration
@Slf4j
public class RabbitmqConfig {

    public static final String ORDER_EXCHANGE_NAME = "order.exchange";

    public static final String ORDER_DELAY_QUEUE_NAME = "order.delay.queue";

    public static final String ORDER_DLX_QUEUE_NAME = "order.dlx.queue";

    public static final String ORDER_DELAY_ROUTINGKEY = "order.delay.routingkey";

    public static final String ORDER_DLX_ROUTINGKEY = "order.dlx.routingkey";

    // 声明交换机，既作正常交换机，又作死信交换机
    @Bean
    public DirectExchange orderExchange(){
        return ExchangeBuilder.directExchange(ORDER_EXCHANGE_NAME).build();
    }

    // 声明延时队列
    @Bean
    public Queue orderDelayQueue(){
        return QueueBuilder.durable(ORDER_DELAY_QUEUE_NAME)
                .deadLetterExchange(ORDER_EXCHANGE_NAME) // 死信交换机
                .deadLetterRoutingKey(ORDER_DLX_ROUTINGKEY) // 死信路由key，即死信交换机和死信队列绑定的key
                .ttl(600000) // 过期时间 10分钟，即订单的超时时间，单位：毫秒
                .build();
    }

    // 声明死信队列
    @Bean
    public Queue orderDlxQueue(){
        return QueueBuilder.durable(ORDER_DLX_QUEUE_NAME).build();
    }

    // 交换机绑定延时队列
    @Bean
    public Binding delayBinding(DirectExchange orderExchange, Queue orderDelayQueue){
        return BindingBuilder.bind(orderDelayQueue).to(orderExchange).with(ORDER_DELAY_ROUTINGKEY);
    }

    // 交换机绑定死信队列
    @Bean
    public Binding dlxBinding(DirectExchange orderExchange, Queue orderDlxQueue){
        return BindingBuilder.bind(orderDlxQueue).to(orderExchange).with(ORDER_DLX_ROUTINGKEY);
    }
}