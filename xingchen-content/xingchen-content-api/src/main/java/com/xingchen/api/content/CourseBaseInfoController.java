//package com.xingchen.api.content;
//
//
//
//import com.xingchen.grow.model.PageParams;
//import com.xingchen.grow.model.PageResult;
//import com.xingchen.model.dto.AddCourseDto;
//import com.xingchen.model.dto.CourseBaseInfoDto;
//import com.xingchen.model.dto.QueryCourseParamsDto;
//import com.xingchen.model.po.CourseBase;
//import com.xingchen.service.service.CourseBaseInfoService;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.annotation.Resource;
//
///**
// * @description TODO
// * @author Mr.M
// * @date 2022/10/7 16:22
// * @version 1.0
// */
//@Api(value = "课程管理接口",tags = "课程管理接口")
//@RestController
//public class CourseBaseInfoController {
//
//    @Resource
//    CourseBaseInfoService courseBaseInfoService;
//
//    @ApiOperation("课程查询接口")
//    @PostMapping("/course/list")
//  public PageResult<CourseBase> list(PageParams params, @RequestBody QueryCourseParamsDto queryCourseParamsDto){
//        //调用service获取数据
//        PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourseBaseList(params, queryCourseParamsDto);
//
//        return  courseBasePageResult;
//    }
//
//    @ApiOperation("新增课程")
//    @PostMapping("/course")
//    public CourseBaseInfoDto createCourseBase(@RequestBody AddCourseDto addCourseDto){
//
//        //获取当前用户所属培训机构的id
//        Long companyId = 22L;
//
//        //调用service
//        CourseBaseInfoDto courseBase = courseBaseInfoService.createCourseBase(companyId, addCourseDto);
//        return courseBase;
//    }
//
//}
