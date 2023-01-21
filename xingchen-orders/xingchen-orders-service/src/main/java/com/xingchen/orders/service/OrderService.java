package com.xingchen.orders.service;

import com.xingchen.orders.model.dto.AddOrderDto;
import com.xingchen.orders.model.dto.PayRecordDto;

/**
 * @author Mr.M
 * @version 1.0
 * @description 订单服务接口
 * @date 2022/10/25 11:41
 */
public interface OrderService {

    /**
     * @description 创建商品订单
     * @param addOrderDto 订单信息
     * @return PayRecordDto 支付交易记录(包括二维码)
     * @author Mr.M
     * @date 2022/10/4 11:02
     */
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto);
}
