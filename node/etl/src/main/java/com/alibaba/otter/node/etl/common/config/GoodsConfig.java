package com.alibaba.otter.node.etl.common.config;


public interface GoodsConfig {

    String GOODS_MQ_TABLE_NAME = "goods_table_name";
    String GOODS_MQ_TABLE_ID = "goods_table_id";
    String GOODS_MQ_TABLE_OPER_TYPE = "goods_table_oper_type";
    String GOODS_MQ_TABLE_CREATE_TIME = "goods_table_create_time";

    String RABBIT_SEMD_MSG_CONFIRM = "goods_confirm_msg_id";

    String GOODS_INFLUENCE_TABLE_NAMES = "brand,catalogue,goods,knowledge,optionset,productsaleorgext,property,propertyrelation,propertyvalues,relatedproduct,spu_relation";

}