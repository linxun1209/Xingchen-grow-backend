package com.xingchen.content.feignclient;


import com.xingchen.content.feignclient.model.CourseIndex;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author xing'chen
 * @description 搜索服务远程调用接口
 * @date 2022/10/19 9:24
 * @version 1.0
 */
@FeignClient(value = "search",fallbackFactory = SearchServiceClientFallbackFactory.class)
@RequestMapping("/search")
public interface SearchServiceClient {

 @PostMapping("/index/course")
 public Boolean add(@RequestBody CourseIndex courseIndex);

 }
