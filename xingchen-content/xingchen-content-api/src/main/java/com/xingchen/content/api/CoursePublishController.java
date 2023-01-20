package com.xingchen.content.api;


import com.xingchen.content.model.dto.CoursePreviewDto;
import com.xingchen.content.model.po.CoursePublish;
import com.xingchen.content.service.CoursePublishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;

/**
 * @author xing'chen
 * @version 1.0
 * @description 课程预览，发布
 * @date 2022/10/17 10:47
 */
@Slf4j
@Api(value = "课程发布相关接口",tags = "课程发布相关接口")
@RestController
public class CoursePublishController {

    @Resource
    CoursePublishService coursePublishService;


    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId) {

        //查询数据
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);


        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("model", coursePreviewInfo);
        modelAndView.setViewName("course_template");
        return modelAndView;
    }

    /**
     * 提交审核
     * @param courseId
     */
    @ApiOperation("提交审核")
    @ResponseBody
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable("courseId") Long courseId) {
        Long companyId = 1232141425L;
        coursePublishService.commitAudit(companyId, courseId);
    }

    @ApiOperation("课程发布")
    @ResponseBody
    @PostMapping("/coursepublish/{courseId}")
    public void coursepublish(@PathVariable("courseId") Long courseId) {

        Long companyId = 1232141425L;
        coursePublishService.publish(companyId,courseId);
    }


    @ApiOperation("查询课程发布信息")
    @ResponseBody
    @GetMapping ("/r/coursepublish/{courseId}")
    public CoursePublish getCoursepublish(@PathVariable("courseId") Long courseId){
        CoursePublish coursePublish = coursePublishService.getCoursePublish(courseId);
        if(coursePublish==null){
            return null;
        }
        //课程发布状态
        String status = coursePublish.getStatus();
        if(status.equals("203002")){
            return coursePublish;
        }
        //课程下线返回null
        return null;

    }

}