package com.xingchen.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xingchen.model.dto.CourseCategoryTreeDto;
import com.xingchen.model.po.CourseCategory;

import javax.annotation.Resource;
import java.util.List;


/**
 * <p>
 * 课程分类 Mapper 接口
 * </p>
 *
 * @author xingchen
 */
@Resource
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {

    List<CourseCategoryTreeDto> selectTreeNodes(String id);
}
