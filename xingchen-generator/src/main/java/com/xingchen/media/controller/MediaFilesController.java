package com.xingchen.media.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.xingchen.media.service.MediaFilesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;


/**
 * <p>
 * 媒资信息 前端控制器
 * </p>
 *
 * @author xingchen
 */
@Slf4j
@RestController
@RequestMapping("mediaFiles")
public class MediaFilesController {

    @Resource
    private MediaFilesService  mediaFilesService;
}
