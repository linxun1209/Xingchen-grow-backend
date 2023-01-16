package com.xingchen.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xingchen.content.model.dto.BindTeachplanMediaDto;
import com.xingchen.content.model.dto.SaveTeachplanDto;
import com.xingchen.content.model.dto.TeachplanDto;
import com.xingchen.content.model.po.Teachplan;
import com.xingchen.content.model.po.TeachplanMedia;

import java.util.List;


/**
 * <p>
 * 课程计划 服务类
 * </p>
 *
 * @author xingchen
 * @since 2023-01-13
 */
public interface TeachplanService extends IService<Teachplan> {

    public List<TeachplanDto> findTeachplanTree(Long courseId);

    /**
     * @description 保存课程计划(新增/修改)
     * @param dto
     * @return void
     * @author Mr.M
     * @date 2022/10/10 15:07
     */
    public void saveTeachplan(SaveTeachplanDto dto);
    /**
     * @description 教学计划绑定媒资
     * @param bindTeachplanMediaDto
     * @return com.xuecheng.content.model.po.TeachplanMedia
     * @author Mr.M
     * @date 2022/9/14 22:20
     */
    public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);

}

