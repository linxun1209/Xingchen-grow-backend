package com.xingchen.media.api;


import com.xingchen.base.exception.xingchenPlusException;
import com.xingchen.base.model.RestResponse;
import com.xingchen.media.po.MediaFiles;
import com.xingchen.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @description TODO
 * @author Mr.M
 * @date 2022/10/17 11:20
 * @version 1.0
 */
 @Api(value = "媒资文件管理接口",tags = "媒资文件管理接口")
 @RestController
 @RequestMapping("/open")
 public class MediaOpenController {

  @Resource
  MediaFileService mediaFileService;

  @ApiOperation("预览文件")
  @GetMapping("/preview/{mediaId}")
  public RestResponse<String> getPlayUrlByMediaId(@PathVariable String mediaId){

   MediaFiles mediaFiles = mediaFileService.getFileById(mediaId);
   if(mediaFiles == null || StringUtils.isEmpty(mediaFiles.getUrl())){
    xingchenPlusException.cast("视频还没有转码处理");
   }
   return RestResponse.success(mediaFiles.getUrl());

  }


 }
