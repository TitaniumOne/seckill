package com.liuhao.seckill.controller;


import com.liuhao.seckill.rabbitmq.MQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author liuhao
 * @since 2021-10-19
 */
@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private MQSender mqSender;

    // /**
    //  * 测试发送RabbitMq消息
    //  */
    // @RequestMapping("/mq")
    // @ResponseBody
    // public void mq(){
    //     mqSender.send("hello!!!");
    // }
}
