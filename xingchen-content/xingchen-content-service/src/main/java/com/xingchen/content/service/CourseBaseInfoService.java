package com.xingchen.content.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.xingchen.content.model.PageParams;
import com.xingchen.content.model.PageResult;
import com.xingchen.content.model.dto.AddCourseDto;
import com.xingchen.content.model.dto.CourseBaseInfoDto;
import com.xingchen.content.model.dto.EditCourseDto;
import com.xingchen.content.model.dto.QueryCourseParamsDto;
import com.xingchen.content.model.po.CourseBase;

/**
 * <p>
 * 课程基本信息 服务类
 * </p>
 *
 * @author xingchen
 * @since 2023-01-13
 */
public interface CourseBaseInfoService extends IService<CourseBase> {

    /**
     * @description 课程查询
     * @param queryCourseParamsDto 查询条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.content.model.po.CourseBase>
     * @author Mr.M
     * @date 2022/10/8 9:46
     */
    public PageResult<CourseBase>  queryCourseBaseList(Long companyId,PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    /**
     * 新增课程
     * @param companyId 培训机构id
     * @param addCourseDto 新增课程的信息
     * @return 课程信息包括基本信息、营销信息
     */
    public CourseBaseInfoDto createCourseBase(Long companyId,AddCourseDto addCourseDto);

    /**
     * @description 修改课程信息
     * @param companyId  机构id，要校验本机构只能修改本机构的课程
     * @param dto  课程信息
     * @return com.xuecheng.content.model.dto.CourseBaseInfoDto
     * @author Mr.M
     * @date 2022/9/8 21:04
     */
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto dto);

    /**
     * @description 根据id查询课程信息
     * @param courseId 课程id
     * @return com.xuecheng.content.model.dto.CourseBaseInfoDto
     * @author Mr.M
     * @date 2022/10/10 10:58
     */


    /**
     * @description 根据id查询课程信息
     * @param courseId 课程id
     * @return com.xuecheng.content.model.dto.CourseBaseInfoDto
     * @author Mr.M
     * @date 2022/10/10 10:58
     */
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId);
}

