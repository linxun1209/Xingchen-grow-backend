package com.xingchen.learning;

import com.xingchen.content.model.po.CoursePublish;
import com.xingchen.learning.feignclient.ContentServiceClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @description 内容管理远程接口测试
 * @author Mr.M
 * @date 2022/10/25 9:16
 * @version 1.0
 */
@SpringBootTest(classes = ContentServiceClientTest.class)
public class ContentServiceClientTest {

  @Resource
 ContentServiceClient contentServiceClient;




  @Test
 public void testGetCoursepublish(){
   CoursePublish coursepublish = contentServiceClient.getCoursepublish(18L);
   //断言coursepublish不为空
   Assertions.assertNotNull(coursepublish);
  }

}
