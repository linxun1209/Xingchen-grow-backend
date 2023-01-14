package com.xingchen.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xingchen.content.model.dto.CourseCategoryTreeDto;
import com.xingchen.content.model.po.CourseCategory;

import java.util.List;


/**
 * <p>
 * 课程分类 Mapper 接口
 * </p>
 *
 * @author xingchen
 */
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {

    List<CourseCategoryTreeDto> selectTreeNodes(String id);
}
