package com.alibaba.otter.node.etl.common.Utils;

import com.alibaba.otter.node.etl.OtterContextLocator;
import com.alibaba.otter.node.etl.common.config.GoodsConfig;
import com.alibaba.otter.node.etl.load.loader.db.DbLoadAction;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MqProceducer{


    static RabbitTemplate rabbitTemplate = OtterContextLocator.getRabbitTemplate();
    static Logger logger = LoggerFactory.getLogger(MqProceducer.class);

    public static void sendMessage(final Object message) {
        final String msg_id = UUID.randomUUID().toString().replace("-","");
        try {
            ObjectMapper objectMapper = new ObjectMapper();String msg_json = objectMapper.writeValueAsString(message);
            Message message_send = MessageBuilder.withBody(msg_json.getBytes()).setContentType(MessageProperties.CONTENT_TYPE_JSON).setCorrelationId(msg_id.getBytes()).build();
            //异步保存到redis中
            DbLoadAction.executorService.submit(new Runnable() {
                @Override
                public void run() {
                    //保存到redis中
                    Map<String,Object> msg_redis_new = new HashMap<String, Object>();
                    if(RedisCacheManager.hasKey(GoodsConfig.RABBIT_SEMD_MSG_CONFIRM)){
                        Map<Object,Object> redis_goods_msg = RedisCacheManager.hmget(GoodsConfig.RABBIT_SEMD_MSG_CONFIRM);
                        boolean has_item = null!=redis_goods_msg && redis_goods_msg.size() > 0?true:false;
                        if(has_item){
                            for(Map.Entry<Object,Object> entry:redis_goods_msg.entrySet()){
                                msg_redis_new.put(entry.getKey().toString(),entry.getValue());
                            }
                        }
                        RedisCacheManager.del(GoodsConfig.RABBIT_SEMD_MSG_CONFIRM);
                    }
                    msg_redis_new.put(msg_id,message);RedisCacheManager.hmset(GoodsConfig.RABBIT_SEMD_MSG_CONFIRM,msg_redis_new);
                }
            });
            //发送消息
            rabbitTemplate.send("exchange_goods_meaasge","routing.goods.first",message_send,new CorrelationData(msg_id));
        }catch (Exception e){
            logger.error("send goods rabbitmq msg error!",e);
        }
    }
}