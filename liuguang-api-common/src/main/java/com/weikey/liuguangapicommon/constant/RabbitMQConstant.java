package com.weikey.liuguangapicommon.constant;

public interface RabbitMQConstant {

    String INTERFACE_EXCHANGE_NAME = "interface.exchange";

    String INTERFACE_QUEUE_NAME = "interface.queue";

    String INTERFACE_ROUTINGKEY = "interface.routingkey";

    String ORDER_EXCHANGE_NAME = "order.exchange";

    String ORDER_DELAY_QUEUE_NAME = "order.delay.queue";

    String ORDER_DLX_QUEUE_NAME = "order.dlx.queue";

    String ORDER_DELAY_ROUTINGKEY = "order.delay.routingkey";

    String ORDER_DLX_ROUTINGKEY = "order.dlx.routingkey";
}
