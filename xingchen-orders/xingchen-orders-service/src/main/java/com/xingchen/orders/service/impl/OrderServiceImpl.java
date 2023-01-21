package com.xingchen.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingchen.base.exception.xingchenPlusException;
import com.xingchen.base.utils.IdWorkerUtils;
import com.xingchen.base.utils.QRCodeUtil;
import com.xingchen.orders.mapper.XcOrdersGoodsMapper;
import com.xingchen.orders.mapper.XcOrdersMapper;
import com.xingchen.orders.mapper.XcPayRecordMapper;
import com.xingchen.orders.model.dto.AddOrderDto;
import com.xingchen.orders.model.dto.PayRecordDto;
import com.xingchen.orders.model.po.XcOrders;
import com.xingchen.orders.model.po.XcOrdersGoods;
import com.xingchen.orders.model.po.XcPayRecord;
import com.xingchen.orders.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description 订单接口实现类
 * @date 2022/10/25 11:42
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    XcOrdersMapper ordersMapper;

    @Resource
    XcOrdersGoodsMapper ordersGoodsMapper;
    @Resource
    XcPayRecordMapper payRecordMapper;


    @Override
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto) {

        //创建商品订单
        XcOrders orders = saveXcOrders(userId, addOrderDto);
        //添加支付记录
        XcPayRecord payRecord = createPayRecord(orders);

        //生成支付二维码
        String qrCode = null;
        try {
            //url要可以被模拟器访问到，url为下单接口(稍后定义)
            qrCode = new QRCodeUtil().createQRCode("http://192.168.68.1/api/orders/requestpay?payNo="+payRecord.getPayNo(), 200, 200);
        } catch (IOException e) {
            xingchenPlusException.cast("生成二维码出错");
        }
        //封装要返回的数据
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord,payRecordDto);
        //支付二维码
        payRecordDto.setQrcode(qrCode);

        return payRecordDto;
    }

    /**
     * 添加支付记录
     * @param orders
     * @return
     */
    public XcPayRecord createPayRecord(XcOrders orders){
        XcPayRecord payRecord = new XcPayRecord();
        long payNo = IdWorkerUtils.getInstance().nextId();
        //支付记录交易号
        payRecord.setPayNo(payNo);

        //记录关键订单id
        payRecord.setOrderId(orders.getId());
        payRecord.setOrderName(orders.getOrderName());
        payRecord.setTotalPrice(orders.getTotalPrice());
        payRecord.setCurrency("CNY");
        payRecord.setCreateDate(LocalDateTime.now());
        //未支付
        payRecord.setStatus("601001");
        payRecord.setUserId(orders.getUserId());
        payRecordMapper.insert(payRecord);
        return payRecord;
    }

    //创建商品订单
    @Transactional
    public XcOrders saveXcOrders(String userId, AddOrderDto addOrderDto) {

        //选课记录id
        String outBusinessId = addOrderDto.getOutBusinessId();
        //对订单插入进行幂等性处理
        //根据选课记录id从数据库查询订单信息
        XcOrders orders = getOrderByBusinessId(outBusinessId);
        if(orders!=null){
            return orders;
        }

        //添加订单
        orders = new XcOrders();
        //雪花算法生成订单号
        long orderId = IdWorkerUtils.getInstance().nextId();
        orders.setId(orderId);
        orders.setTotalPrice(addOrderDto.getTotalPrice());
        orders.setCreateDate(LocalDateTime.now());
        orders.setStatus("600001");//未支付
        orders.setUserId(userId);
        orders.setOrderType(addOrderDto.getOrderType());
        orders.setOrderName(addOrderDto.getOrderName());
        orders.setOrderDetail(addOrderDto.getOrderDetail());
        orders.setOrderDescrip(addOrderDto.getOrderDescrip());
        orders.setOutBusinessId(addOrderDto.getOutBusinessId());//选课记录id
        ordersMapper.insert(orders);
        //插入订单明细表
        String orderDetailJson = addOrderDto.getOrderDetail();
        //将json转成List
        List<XcOrdersGoods> xcOrdersGoods = JSON.parseArray(orderDetailJson, XcOrdersGoods.class);
        //将明细List插入数据库
        xcOrdersGoods.forEach(ordersGodds->{
            //在明细中记录订单号
            ordersGodds.setOrderId(orderId);
            ordersGoodsMapper.insert(ordersGodds);
        });

        return orders;


    }

    /**
     * 根据业务id查询订单
     * @param businessId
     * @return
     */
    public XcOrders getOrderByBusinessId(String businessId) {
        XcOrders orders = ordersMapper.selectOne(new LambdaQueryWrapper<XcOrders>().eq(XcOrders::getOutBusinessId, businessId));
        return orders;
    }

}
