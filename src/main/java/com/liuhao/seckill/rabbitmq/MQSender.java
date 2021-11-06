package com.liuhao.seckill.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 消息发送者
 */
@Service
@Slf4j
public class MQSender {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    // public void send(Object msg) {
    //     log.info("发送消息" + msg);
    //     rabbitTemplate.convertAndSend("queue", msg);
    // }

    /**
     * 发送消息信息
     * @param msg
     */
    public void sendMessage(String msg) {
        log.info(msg);
        rabbitTemplate.convertAndSend("secKillExchange","secKill.message",msg);
    }
}
