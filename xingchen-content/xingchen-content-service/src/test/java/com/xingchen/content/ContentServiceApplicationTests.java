package com.xingchen.content;


import com.xingchen.content.mapper.CourseBaseMapper;
import com.xingchen.content.mapper.CourseCategoryMapper;
import com.xingchen.content.model.PageParams;
import com.xingchen.content.model.PageResult;
import com.xingchen.content.model.dto.CourseCategoryTreeDto;
import com.xingchen.content.model.dto.QueryCourseParamsDto;
import com.xingchen.content.model.po.CourseBase;
import com.xingchen.content.service.CourseBaseInfoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


@SpringBootTest
class ContentServiceApplicationTests {

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Test
    void testCourseBaseMapper() {
        CourseBase courseBase = courseBaseMapper.selectById(22);
        Assertions.assertNotNull(courseBase);
    }
    @Test
    void testCourseBaseInfoService() {
        PageParams pageParams = new PageParams();
        PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourseBaseList(pageParams, new QueryCourseParamsDto());
        System.out.println(courseBasePageResult);
    }
    @Test
    void testCourseCategoryMapper() {


    }

}
