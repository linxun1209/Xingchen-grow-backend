package com.xingchen.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.xingchen.base.exception.xingchenPlusException;
import com.xingchen.content.mapper.CourseBaseMapper;
import com.xingchen.content.mapper.CourseMarketMapper;
import com.xingchen.content.mapper.CoursePublishMapper;
import com.xingchen.content.model.dto.CourseBaseInfoDto;
import com.xingchen.content.model.dto.CoursePreviewDto;
import com.xingchen.content.model.dto.TeachplanDto;
import com.xingchen.content.model.po.CourseBase;
import com.xingchen.content.model.po.CourseMarket;
import com.xingchen.content.model.po.CoursePublish;
import com.xingchen.content.model.po.CoursePublishPre;
import com.xingchen.content.mapper.CoursePublishPreMapper;
import com.xingchen.content.service.CourseBaseInfoService;
import com.xingchen.content.service.CoursePublishPreService;
import com.xingchen.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 课程发布 服务实现类
 * </p>
 *
 * @author xingchen
 */
@Slf4j
@Service
public class CoursePublishPreServiceImpl extends ServiceImpl<CoursePublishPreMapper, CoursePublishPre> implements CoursePublishPreService {

    @Resource
    CourseBaseInfoService courseBaseInfoService;

    @Resource
    TeachplanService teachplanService;

    @Resource
    CourseBaseMapper courseBaseMapper;

    @Resource
    CourseMarketMapper courseMarketMapper;

    @Resource
    CoursePublishPreMapper coursePublishPreMapper;

    @Resource
    CoursePublishMapper coursePublishMapper;


    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {

        //基本信息、营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);

        //教学计划
        List<TeachplanDto> teachplayTree = teachplanService.findTeachplanTree(courseId);

        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseInfo);
        coursePreviewDto.setTeachplans(teachplayTree);
        return coursePreviewDto;
    }

    @Override
    public void commitAudit(Long companyId, Long courseId) {
        //约束校验
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        //课程审核状态
        String auditStatus = courseBase.getAuditStatus();
        //当前审核状态为已提交不允许再次提交
        if ("202003".equals(auditStatus)) {
            xingchenPlusException.cast("当前为等待审核状态，审核完成可以再次提交。");
        }
        //本机构只允许提交本机构的课程
        if (!courseBase.getCompanyId().equals(companyId)) {
            xingchenPlusException.cast("不允许提交其它机构的课程。");
        }

        //课程图片是否填写
        if (StringUtils.isEmpty(courseBase.getPic())) {
            xingchenPlusException.cast("提交失败，请上传课程图片");
        }

        //查询课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        if (teachplanTree.size() <= 0) {
            xingchenPlusException.cast("提交失败，还没有添加课程计划");
        }

        //封装数据，基本信息、营销信息、课程计划信息、师资信息
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        //查询基本信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        BeanUtils.copyProperties(courseBaseInfo, coursePublishPre);
        //将课程计划信息转json
        String teachplanTreeJson = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachplanTreeJson);
        //课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        //转为json
        String courseMarketJson = JSON.toJSONString(courseMarket);
        //将课程营销信息json数据放入课程预发布表
        coursePublishPre.setMarket(courseMarketJson);

        //课程预发布表初始审核状态
        coursePublishPre.setStatus("202003");

        CoursePublishPre coursePublishPre1 = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre1 == null) {
            coursePublishPreMapper.insert(coursePublishPre);
        } else {
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        //更新课程基本表的审核状态
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);

    }

    @Override
    public void publish(Long companyId, Long courseId) {
        //约束校验
        //查询课程预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPre == null){
            xingchenPlusException.cast("请先提交课程审核，审核通过才可以发布");
        }
        //本机构只允许提交本机构的课程
        if(!coursePublishPre.getCompanyId().equals(companyId)){
            xingchenPlusException.cast("不允许提交其它机构的课程。");
        }


        //课程审核状态
        String auditStatus = coursePublishPre.getStatus();
        //审核通过方可发布
        if(!"202004".equals(auditStatus)){
            xingchenPlusException.cast("操作失败，课程审核通过方可发布。");
        }

        //保存课程发布信息
        saveCoursePublish(courseId);

        //保存消息表
        saveCoursePublishMessage(courseId);

        //删除课程预发布表对应记录
        coursePublishPreMapper.deleteById(courseId);

    }


    //保存课程发布信息
    private void saveCoursePublish(Long courseId) {

        //课程发布信息来源于预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);

        CoursePublish coursePublish = new CoursePublish();
        //拷贝到课程发布对象
        BeanUtils.copyProperties(coursePublishPre,coursePublish);
        coursePublish.setStatus("203002");//已发布

        CoursePublish coursePublishUpdate = coursePublishMapper.selectById(courseId);
        if(coursePublishUpdate == null){
            coursePublishMapper.insert(coursePublish);
        }else{
            coursePublishMapper.updateById(coursePublish);
        }
        //更新课程基本表的发布状态
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus("203002");//已发布
        courseBaseMapper.updateById(courseBase);
    }

    //保存消息表
    private void saveCoursePublishMessage(Long courseId) {

    }

}