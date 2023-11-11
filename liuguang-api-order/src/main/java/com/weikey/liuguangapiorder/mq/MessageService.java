package com.weikey.liuguangapiorder.mq;

import com.weikey.liuguangapicommon.model.entity.OrderInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 发送消息
 */
@Service
@Slf4j
public class MessageService implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnsCallback {

    @Resource
    private RabbitTemplate rabbitTemplate;

    // bean在初始化的时候，会调用一次该方法，只调用一次，起到初始化的作用
    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnsCallback(this);
    }

    /**
     * 交换机收到消息后，会回调该方法
     *
     * @param correlationData  相关联的数据
     * @param ack  有两个取值，true和false，true表示成功：消息正确地到达交换机，反之false就是消息没有正确地到达交换机
     * @param cause 消息没有正确地到达交换机的原因是什么
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (!ack) {
            log.error("消息投递到交换机失败，orderNo: {}, cause: {}", correlationData.getId(), cause);
        }
    }

    /**
     * 当消息从交换机没有正确地到达队列，则会触发该方法
     */
    @Override
    public void returnedMessage(ReturnedMessage returned) {
        log.error("消息投递到队列失败，ReturnedMessage: {}", returned);
    }

    /**
     * 发送消息
     */
    public void sendMessage(OrderInfo orderInfo) {
        // Confirm关联数据对象
        CorrelationData correlationData = new CorrelationData();
        correlationData.setId(orderInfo.getOrderNo()); // 设置订单ID，在confirm回调里，就可以知道是哪个订单没有发送到交换机上去
        // void convertAndSend(String exchange, String routingKey, Object message, CorrelationData correlationData)
        rabbitTemplate.convertAndSend(RabbitmqConfig.ORDER_EXCHANGE_NAME, RabbitmqConfig.ORDER_DELAY_ROUTINGKEY, orderInfo, correlationData);
    }
}