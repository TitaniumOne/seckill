package com.liuhao.seckill.rabbitmq;

import com.liuhao.seckill.pojo.SecKillMessage;
import com.liuhao.seckill.pojo.SeckillOrders;
import com.liuhao.seckill.pojo.User;
import com.liuhao.seckill.service.IGoodsService;
import com.liuhao.seckill.service.IOrdersService;
import com.liuhao.seckill.utils.JsonUtil;
import com.liuhao.seckill.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 消息消费者
 */
@Service
@Slf4j
public class MQReceiver {
    // @RabbitListener(queues = "queue")
    // public void receive(Object msg) {
    //     log.info("接收消息" + msg);
    // }
    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IOrdersService ordersService;

    @RabbitListener(queues = "secKillQueue")
    public void receive(String msg) {
        log.info("接受的消息：" + msg);
        SecKillMessage secKillMessage = JsonUtil.jsonToPojo(msg, SecKillMessage.class);
        Long goodsId = secKillMessage.getGoodsId();
        User user = secKillMessage.getUser();
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        if(goodsVo.getStockCount() < 1) {
            return;
        }
        // 判断是否重复抢购
        SeckillOrders seckillOrders = (SeckillOrders) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if(seckillOrders != null) {
            return;
        }
        // 下单操作
        ordersService.secKill(user, goodsVo);
    }
}
