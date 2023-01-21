package com.xingchen.orders.api;

import com.xingchen.orders.model.dto.AddOrderDto;
import com.xingchen.orders.model.dto.PayRecordDto;
import com.xingchen.orders.service.OrderService;
import com.xingchen.orders.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/10/25 11:39
 */
@Api(value = "订单支付接口", tags = "订单支付接口")
@Slf4j
@Controller
public class OrderController {

    @Autowired
    OrderService orderService;

    @ApiOperation("生成支付二维码")
    @PostMapping("/generatepaycode")
    @ResponseBody
    public PayRecordDto generatePayCode(@RequestBody AddOrderDto addOrderDto) {
        //登录用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        //用户id
        String userId = user.getId();

        return orderService.createOrder(userId,addOrderDto);
    }



    @ApiOperation("扫码下单接口")
    @GetMapping("/requestpay")
    public void requestpay(String payNo, HttpServletResponse httpResponse) throws IOException {

    }
}

