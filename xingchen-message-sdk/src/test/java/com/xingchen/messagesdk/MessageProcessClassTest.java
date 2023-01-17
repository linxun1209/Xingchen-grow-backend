package com.xingchen.messagesdk;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/21 21:51
 */
@SpringBootTest
public class MessageProcessClassTest {

    @Resource
    MessageProcessClass messageProcessClass;

    @Test
    public void test() throws InterruptedException {

        System.out.println("开始执行-----》" + LocalDateTime.now());
        messageProcessClass.process(0, 1, "course_publish", 2, 1000);

        System.out.println("结束执行-----》" + LocalDateTime.now());

        Thread.sleep(90000000);

    }
}
