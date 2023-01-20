package com.xingchen.content.api;



import com.xingchen.base.exception.ValidationGroups;
import com.xingchen.content.model.PageParams;
import com.xingchen.content.model.PageResult;
import com.xingchen.content.model.dto.AddCourseDto;
import com.xingchen.content.model.dto.CourseBaseInfoDto;
import com.xingchen.content.model.dto.EditCourseDto;
import com.xingchen.content.model.dto.QueryCourseParamsDto;
import com.xingchen.content.model.po.CourseBase;
import com.xingchen.content.service.CourseBaseInfoService;
import com.xingchen.content.utils.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author xing'chen
 * @description TODO
 * @date 2022/10/7 16:22
 * @version 1.0
 */
@Api(value = "课程管理接口",tags = "课程管理接口")
@RestController
public class CourseBaseInfoController {

    @Resource
    CourseBaseInfoService courseBaseInfoService;

    @ApiOperation("课程查询接口")
    @PreAuthorize("hasAuthority('xc_teachmanager_course_list')")//拥有课程列表查询的权限方可访问
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody QueryCourseParamsDto queryCourseParams){
        //取出用户身份
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        //机构id
        String companyId = user.getCompanyId();
        return courseBaseInfoService.queryCourseBaseList(Long.parseLong(companyId),pageParams,queryCourseParams);
    }

    @ApiOperation("新增课程")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated(ValidationGroups.Inster.class) AddCourseDto addCourseDto){

        //获取当前用户所属培训机构的id
        Long companyId = 22L;

        //调用service
        CourseBaseInfoDto courseBase = courseBaseInfoService.createCourseBase(companyId, addCourseDto);
        return courseBase;
    }

    @ApiOperation("根据课程id查询课程基础信息")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable Long courseId){
        //取出当前用户身份
        //Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        System.out.println(user);
        return courseBaseInfoService.getCourseBaseInfo(courseId);
    }

    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody EditCourseDto dto){

        Long companyId =1232141425L;
        return courseBaseInfoService.updateCourseBase(companyId,dto);
    }

//    测试分级校验用
    @PostMapping("/course2")
    public CourseBaseInfoDto createCourseBase2(@RequestBody @Validated(ValidationGroups.Update.class) AddCourseDto addCourseDto) {

        Long companyId = 22L;
        return courseBaseInfoService.createCourseBase(companyId, addCourseDto);
//    }
    }
}
