package com.xingchen.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xingchen.model.dto.CourseCategoryTreeDto;
import com.xingchen.model.po.CourseCategory;

import java.util.List;


/**
 * <p>
 * 课程分类 服务类
 * </p>
 *
 * @author xingchen
 * @since 2023-01-13
 */
public interface CourseCategoryService extends IService<CourseCategory> {

    List<CourseCategoryTreeDto> queryTreeNodes(String s);
}
