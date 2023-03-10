package com.xingchen.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xingchen.base.exception.CommonError;
import com.xingchen.base.exception.xingchenPlusException;


import com.xingchen.config.MultipartSupportConfig;
import com.xingchen.content.feignclient.MediaServiceClient;
import com.xingchen.content.feignclient.SearchServiceClient;
import com.xingchen.content.feignclient.model.CourseIndex;
import com.xingchen.content.mapper.CourseBaseMapper;
import com.xingchen.content.mapper.CourseMarketMapper;
import com.xingchen.content.mapper.CoursePublishMapper;
import com.xingchen.content.mapper.CoursePublishPreMapper;
import com.xingchen.content.model.dto.CourseBaseInfoDto;
import com.xingchen.content.model.dto.CoursePreviewDto;
import com.xingchen.content.model.dto.TeachplanDto;
import com.xingchen.content.model.po.CourseBase;
import com.xingchen.content.model.po.CourseMarket;
import com.xingchen.content.model.po.CoursePublish;
import com.xingchen.content.model.po.CoursePublishPre;
import com.xingchen.content.service.CourseBaseInfoService;
import com.xingchen.content.service.CoursePublishService;
import com.xingchen.content.service.TeachplanService;
import com.xingchen.content.service.jobhandler.CoursePublishTask;
import com.xingchen.messagesdk.model.po.MqMessage;
import com.xingchen.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * <p>
 * 课程发布 服务实现类
 * </p>
 *
 * @author xingchen
 */
@Slf4j
@Service
public class CoursePublishServiceImpl extends ServiceImpl<CoursePublishMapper, CoursePublish> implements CoursePublishService {

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
    MqMessageService mqMessageService;

    @Resource
    SearchServiceClient searchServiceClient;

    @Resource
    MediaServiceClient mediaServiceClient;

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

    /**
     * @description 保存消息表记录
     * @param courseId  课程id
     * @return void
     * @date 2022/9/20 16:32
     */
    private void saveCoursePublishMessage(Long courseId){
        MqMessage mqMessage = mqMessageService.addMessage(CoursePublishTask.MESSAGE_TYPE, String.valueOf(courseId), null, null);
        if(mqMessage!=null){
            xingchenPlusException.cast(CommonError.UNKOWN_ERROR);
        }
    }


    /**
     * 保存课程发布信息
     * @param courseId
     */
    private void saveCoursePublish(Long courseId) {

        //课程发布信息来源于预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);

        CoursePublish coursePublish = new CoursePublish();
        //拷贝到课程发布对象
        BeanUtils.copyProperties(coursePublishPre,coursePublish);
        //已发布
        coursePublish.setStatus("203002");

        CoursePublish coursePublishUpdate = coursePublishMapper.selectById(courseId);
        if(coursePublishUpdate == null){
            coursePublishMapper.insert(coursePublish);
        }else{
            coursePublishMapper.updateById(coursePublish);
        }
        //更新课程基本表的发布状态
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        //已发布
        courseBase.setStatus("203002");
        courseBaseMapper.updateById(courseBase);
    }

    @Override
    public File generateCourseHtml(Long courseId) {

        try {
            //配置freemarker
            Configuration configuration = new Configuration(Configuration.getVersion());

            //加载模板
            //选指定模板路径,classpath下templates下
            //得到classpath路径
            String classpath = this.getClass().getResource("/").getPath();
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            //设置字符编码
            configuration.setDefaultEncoding("utf-8");

            //指定模板文件名称
            Template template = configuration.getTemplate("course_template.ftl");

            //准备数据
            CoursePreviewDto coursePreviewInfo = getCoursePreviewInfo(courseId);

            Map<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            //静态化
            //参数1：模板，参数2：数据模型
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
            System.out.println(content);
            //将静态化内容输出到文件中
            InputStream inputStream = IOUtils.toInputStream(content);
            //输出流
            File course = File.createTempFile("course", ".html");
            FileOutputStream outputStream = new FileOutputStream(course);
            IOUtils.copy(inputStream, outputStream);
            return course;
        } catch (Exception e) {
            log.debug("课程静态化异常:{}",e.getMessage(),e);
        }
        return null;
    }

    /**
     * @param courseId
     * @param file     静态化文件
     * @return void
     * @description 上传课程静态化页面
     * @author Mr.M
     * @date 2022/9/23 16:59
     */
    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        //将file
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        //objectName 课程id.html
        String objectName = courseId+".html";
        //远程调用媒资管理服务上传文件
        String course = mediaServiceClient.uploadFile(multipartFile, "course", objectName);
        if(course == null){
            xingchenPlusException.cast("远程调用媒资服务上传文件失败");
        }
    }

    @Override
    public Boolean saveCourseIndex(Long courseId) {
        //查询课程发布表的数据
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        //...异常处理
        //组装数据
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish,courseIndex);

        //远程调用搜索服务创建索引
        Boolean result = searchServiceClient.add(courseIndex);
        if(!result){
            xingchenPlusException.cast("创建课程索引失败");
        }
        return result;
    }

    /**
     * 查询课程发布信息
     * @param courseId
     * @return
     */
    @Override
    public CoursePublish getCoursePublish(Long courseId){
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        return coursePublish ;
    }



//    //保存课程发布信息
//    private void saveCoursePublish(Long courseId) {
//
//        //课程发布信息来源于预发布表
//        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
//
//        CoursePublish coursePublish = new CoursePublish();
//        //拷贝到课程发布对象
//        BeanUtils.copyProperties(coursePublishPre,coursePublish);
//        coursePublish.setStatus("203002");//已发布
//
//        CoursePublish coursePublishUpdate = coursePublishMapper.selectById(courseId);
//        if(coursePublishUpdate == null){
//            coursePublishMapper.insert(coursePublish);
//        }else{
//            coursePublishMapper.updateById(coursePublish);
//        }
//        //更新课程基本表的发布状态
//        CourseBase courseBase = courseBaseMapper.selectById(courseId);
//        courseBase.setStatus("203002");//已发布
//        courseBaseMapper.updateById(courseBase);
//    }
//
//    //保存消息表
//    private void saveCoursePublishMessage(Long courseId) {
//        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
//        if(mqMessage == null){
//            xingchenPlusException.cast("添加消息记录失败");
//        }
//    }

}
