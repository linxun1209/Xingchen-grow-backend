package com.xingchen.content.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.xingchen.content.model.PageParams;
import com.xingchen.content.model.PageResult;
import com.xingchen.content.model.dto.AddCourseDto;
import com.xingchen.content.model.dto.CourseBaseInfoDto;
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
     * @param params 分页参数
     * @param queryCourseParamsDto 查询条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.content.model.po.CourseBase>
     * @author Mr.M
     * @date 2022/10/8 9:46
     */
    public PageResult<CourseBase>  queryCourseBaseList(PageParams params, QueryCourseParamsDto queryCourseParamsDto);

    /**
     * 新增课程
     * @param companyId 培训机构id
     * @param addCourseDto 新增课程的信息
     * @return 课程信息包括基本信息、营销信息
     */
    public CourseBaseInfoDto createCourseBase(Long companyId,AddCourseDto addCourseDto);
}

