package com.xingchen.content.model.dto;

import lombok.Data;

import java.util.List;

/**
 * @author xingchen
 * @version V1.0
 * @Package com.xingchen.content.model.dto
 * @date 2023/1/16 18:02
 */
@Data
public class CoursePreviewDto {

    //课程基本信息,课程营销信息
    CourseBaseInfoDto courseBase;


    //课程计划信息
    List<TeachplanDto> teachplans;

    //师资信息暂时不加...

}
