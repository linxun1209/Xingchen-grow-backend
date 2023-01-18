package com.xingchen.content;


import com.xingchen.content.config.MultipartSupportConfig;
import com.xingchen.content.feignclient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/10/17 17:28
 */
@SpringBootTest
public class MeidaClientTest {

    @Autowired
    MediaServiceClient mediaServiceClient;


    @Test
    public void testUpload() {
        File file = new File("D:\\develop\\test.html");

        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String result = mediaServiceClient.upload(multipartFile, "test", "test01.html");
        System.out.println(result);
    }

}
