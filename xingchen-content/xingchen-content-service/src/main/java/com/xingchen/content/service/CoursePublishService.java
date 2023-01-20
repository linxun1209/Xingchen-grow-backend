package com.xingchen.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xingchen.content.model.dto.CoursePreviewDto;
import com.xingchen.content.model.po.CoursePublish;

import java.io.File;


/**
 * <p>
 * 课程发布 服务类
 * </p>
 *
 * @author xingchen
 * @since 2023-01-13
 */
public interface CoursePublishService extends IService<CoursePublish> {

    /**
     * @param courseId 课程id
     * @return com.xuecheng.content.model.dto.CoursePreviewDto
     * @description 获取课程预览信息
     * @author Mr.M
     * @date 2022/9/16 15:36
     */
    public CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /**
     * @param courseId 课程id
     * @return void
     * @description 提交审核
     * @author Mr.M
     * @date 2022/9/18 10:31
     */
    public void commitAudit(Long companyId, Long courseId);

    /**
     * @param companyId 机构id
     * @param courseId  课程id
     * @return void
     * @description 课程发布接口
     * @author Mr.M
     * @date 2022/9/20 16:23
     */
    public void publish(Long companyId, Long courseId);

    /**
     * @param courseId 课程id
     * @return File 静态化文件
     * @description 课程静态化
     * @author Mr.M
     * @date 2022/9/23 16:59
     */
    public File generateCourseHtml(Long courseId);

    /**
     * @param file 静态化文件
     * @return void courseId file
     * @description 上传课程静态化页面
     * @date 2022/9/23 16:59
     */
    public void uploadCourseHtml(Long courseId, File file);

    /**
     * 创建索引
     * @param courseId
     * @return
     */
    public Boolean saveCourseIndex(Long courseId) ;
}
