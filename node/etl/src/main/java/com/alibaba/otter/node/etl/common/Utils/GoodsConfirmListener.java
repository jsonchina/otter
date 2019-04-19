package com.alibaba.otter.node.etl.common.Utils;

import com.alibaba.otter.node.etl.common.config.GoodsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author liang.ren@zkh360.com
 * @Description //goods msg reliable deliver
 * @Date 10:45 2019/4/18
 * @Param 
 * @return 
 **/
public class GoodsConfirmListener implements RabbitTemplate.ConfirmCallback {


    Logger logger = LoggerFactory.getLogger(GoodsConfirmListener.class);

    @Override
    public void confirm(CorrelationData correlationData, boolean ack) {
        if(ack){
            Map<String,Object> new_goods_msgs = new HashMap<String, Object>();
            Map<Object,Object> confirm_goods_msgs = RedisCacheManager.hmget(GoodsConfig.RABBIT_SEMD_MSG_CONFIRM);
            boolean has_item = null!=confirm_goods_msgs && confirm_goods_msgs.size() > 0?true:false;
            if(has_item){
                for(Map.Entry<Object,Object> entry:confirm_goods_msgs.entrySet()){
                    if(!entry.getKey().equals(correlationData.getId())){
                        new_goods_msgs.put(entry.getKey().toString(),entry.getValue());
                    }
                }
            }
            RedisCacheManager.del(GoodsConfig.RABBIT_SEMD_MSG_CONFIRM);
            if(null!=new_goods_msgs && new_goods_msgs.size()>0){
                RedisCacheManager.hmset(GoodsConfig.RABBIT_SEMD_MSG_CONFIRM,new_goods_msgs);
            }
        }else{
            this.logger.error("商品中心消息投递失败,消息id为:"+correlationData.getId());
        }
    }
}