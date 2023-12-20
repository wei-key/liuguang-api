package com.weikey.liuguangapiorder.mq;

import com.rabbitmq.client.Channel;
import com.weikey.liuguangapicommon.model.entity.OrderInfo;
import com.weikey.liuguangapicommon.model.enums.OrderStatus;
import com.weikey.liuguangapiorder.service.AliPayService;
import com.weikey.liuguangapiorder.service.OrderInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

import static com.weikey.liuguangapicommon.constant.RabbitMQConstant.ORDER_DLX_QUEUE_NAME;

@Component
@Slf4j
public class MessageListener {

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private AliPayService aliPayService;

    /**
     * 监听死信队列，拿到超时的订单
     * @param orderInfo 可以直接拿到订单对象
     * @param message 消息
     * @param channel
     */
    @RabbitListener(queues = {ORDER_DLX_QUEUE_NAME})
    public void receiveMsg(OrderInfo orderInfo, Message message, Channel channel) {
        log.info("监听队列拿到延时消息...");

        // 消息的唯一标识
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            // 查询订单
            OrderInfo orderByNo = orderInfoService.getOrderByOrderNo(orderInfo.getOrderNo());

            // 订单状态是未支付，进行相应处理
            // 消息幂等性：重复接收消息后，订单状态已更改，业务不会重复执行（根据业务进行幂等性的判断）
            if (orderByNo != null && OrderStatus.NOTPAY.getValue() == orderByNo.getOrderStatus()) {
                // 检查订单状态，并进行相应处理
                aliPayService.checkOrderStatus(orderByNo);
            }

            // 业务正常执行，进行消息确认
            // void basicAck(long deliveryTag, boolean multiple)
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            // 业务处理异常，拒绝消息
            try {
                // 第3个参数如果为true，表示消息被Nack后，重新发送到队列
                // void basicNack(long deliveryTag, boolean multiple, boolean requeue)
                // 没有让消息重新投递
                channel.basicNack(deliveryTag, false, false);
                log.error("订单消息处理发生异常, error ===> {}", e); // 记录异常
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
