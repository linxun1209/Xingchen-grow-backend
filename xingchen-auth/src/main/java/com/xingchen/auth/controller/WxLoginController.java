//package com.xingchen.auth.controller;
//
//
//import com.xingchen.ucenter.model.po.XcUser;
//import com.xingchen.ucenter.service.impl.WxAuthServiceImpl;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//
//import javax.annotation.Resource;
//import java.io.IOException;
//
///**
// * @description TODO
// * @author Mr.M
// * @date 2022/10/20 16:47
// * @version 1.0
// */
// @Controller
//public class WxLoginController {
//
//  @Resource
//  WxAuthServiceImpl wxAuthService;
//
//
//  @RequestMapping("/wxLogin")
//  public String wxLogin(String code, String state) throws IOException {
//
//     //拿授权码申请令牌，查询用户
//   XcUser xcUser = wxAuthService.wxAuth(code);
//   if(xcUser == null){
//     //重定向到一个错误页面
//     return "redirect:http://www.xuecheng-plus.com/error.html";
//   }else{
//    String username = xcUser.getUsername();
//    //重定向到登录页面，自动登录
//     return "redirect:http://www.xuecheng-plus.com/sign.html?username="+username+"&authType=wx";
//   }
//
//  }
//
//
//
//}
