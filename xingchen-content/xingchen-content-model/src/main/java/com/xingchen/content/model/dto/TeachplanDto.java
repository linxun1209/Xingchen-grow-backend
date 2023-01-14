package com.xingchen.content.model.dto;

import com.xingchen.content.model.po.Teachplan;
import com.xingchen.content.model.po.TeachplanMedia;
import lombok.Data;

import java.util.List;

/**
 * @author xingchen
 * @version V1.0
 * @Package com.xingchen.content.model.dto
 * @date 2023/1/14 19:05
 */
@Data
public class TeachplanDto extends Teachplan {

    //课程计划关联的媒资信息

    TeachplanMedia teachplanMedia;

    //子目录

    List<TeachplanDto> teachPlanTreeNodes;
}
