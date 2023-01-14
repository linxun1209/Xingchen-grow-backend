package com.xingchen.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xingchen.content.model.dto.TeachplanDto;
import com.xingchen.content.model.po.Teachplan;

import java.util.List;


/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author xingchen
 */
public interface TeachplanMapper extends BaseMapper<Teachplan> {

    List<TeachplanDto> selectTreeNodes(Long courseId);
}
