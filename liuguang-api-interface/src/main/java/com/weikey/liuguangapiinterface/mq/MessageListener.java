package com.weikey.liuguangapiinterface.mq;

import com.rabbitmq.client.Channel;
import com.weikey.liuguangapicommon.model.dto.userInterfaceInfo.UserInterfaceInfoInvokeCountRequest;
import com.weikey.liuguangapicommon.model.entity.OrderInfo;
import com.weikey.liuguangapicommon.model.enums.OrderStatus;
import com.weikey.liuguangapiinterface.service.InterfaceInfoService;
import com.weikey.liuguangapiinterface.service.UserInterfaceInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

import static com.weikey.liuguangapicommon.constant.RabbitMQConstant.INTERFACE_QUEUE_NAME;
import static com.weikey.liuguangapicommon.constant.RabbitMQConstant.ORDER_DLX_QUEUE_NAME;

@Component
@Slf4j
public class MessageListener {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    /**
     * 监听队列，拿到消息
     * @param message
     * @param channel
     */
    @RabbitListener(queuesToDeclare = { @Queue(INTERFACE_QUEUE_NAME)})
    public void receiveMsg(UserInterfaceInfoInvokeCountRequest userInterfaceInfoInvokeCountRequest, Message message, Channel channel) {
        log.info("监听队列拿到消息: {}", userInterfaceInfoInvokeCountRequest);

        userInterfaceInfoService.rollbackCount(userInterfaceInfoInvokeCountRequest.getUserId(), userInterfaceInfoInvokeCountRequest.getInterfaceInfoId());

    }
}
