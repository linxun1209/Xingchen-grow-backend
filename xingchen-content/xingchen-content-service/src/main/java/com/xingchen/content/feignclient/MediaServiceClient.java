package com.xingchen.content.feignclient;


import com.xingchen.config.MultipartSupportConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * @description TODO
 * @author Mr.M
 * @date 2022/10/19 9:24
 * @version 1.0
 */
//@FeignClient(value = "media-api",configuration = MultipartSupportConfig.class,fallbackFactory = MediaServiceClientFallbackFactory.class)
//@RequestMapping("/media")
@FeignClient(value = "media-api",configuration = MultipartSupportConfig.class)
public interface MediaServiceClient {
 @RequestMapping(value = "/media/upload/coursefile",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
 String uploadFile(@RequestPart("filedata") MultipartFile upload,
                   @RequestParam(value = "folder",required=false) String folder,
                   @RequestParam(value = "objectName",required=false) String objectName);


 }
