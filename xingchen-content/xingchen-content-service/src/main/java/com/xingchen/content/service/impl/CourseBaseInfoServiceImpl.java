package com.xingchen.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.xingchen.base.exception.xingchenPlusException;
import com.xingchen.content.model.PageParams;
import com.xingchen.content.model.PageResult;
import com.xingchen.content.model.dto.AddCourseDto;
import com.xingchen.content.model.dto.CourseBaseInfoDto;
import com.xingchen.content.model.dto.EditCourseDto;
import com.xingchen.content.model.dto.QueryCourseParamsDto;
import com.xingchen.content.model.po.CourseBase;
import com.xingchen.content.model.po.CourseCategory;
import com.xingchen.content.model.po.CourseMarket;
import com.xingchen.content.mapper.CourseBaseMapper;
import com.xingchen.content.mapper.CourseCategoryMapper;
import com.xingchen.content.mapper.CourseMarketMapper;
import com.xingchen.content.service.CourseBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 课程基本信息 服务实现类
 * </p>
 *
 * @author xingchen
 */
@Slf4j
@Service
public class CourseBaseInfoServiceImpl extends ServiceImpl<CourseBaseMapper, CourseBase> implements CourseBaseInfoService {

    @Resource
    CourseBaseMapper courseBaseMapper;

    @Resource
    CourseMarketMapper courseMarketMapper;

    @Resource
    CourseCategoryMapper courseCategoryMapper;

    @Resource
    CourseMarketServiceImpl courseMarketService;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(Long companyId,PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();

        //机构id
        queryWrapper.eq(CourseBase::getCompanyId,companyId);

        //拼接查询条件 根据课程名称模糊查询  name like '%名称%'
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),CourseBase::getName,queryCourseParamsDto.getCourseName());

        //根据课程审核状态
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),CourseBase::getAuditStatus,202004);

        //根据课程发布状态
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()),CourseBase::getAuditStatus,queryCourseParamsDto.getPublishStatus());

        //分页参数
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());


        //分页查询E page 分页参数, @Param("ew") Wrapper<T> queryWrapper 查询条件
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);

        //数据
        List<CourseBase> items = pageResult.getRecords();
        //总记录数
        long total = pageResult.getTotal();


        //准备返回数据 List<T> items, long counts, long page, long pageSize
        PageResult<CourseBase> courseBasePageResult = new PageResult<>(items, total, pageParams.getPageNo(), pageParams.getPageSize());

        return courseBasePageResult;
    }

    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {
        //对参数进行合法性的校验
        //合法性校验
//        if (StringUtils.isBlank(dto.getName())) {
//            throw new xingchenPlusException("课程名称为空");
//        }
//
//        if (StringUtils.isBlank(dto.getMt())) {
//            throw new xingchenPlusException("课程分类为空");
//        }
//
//        if (StringUtils.isBlank(dto.getSt())) {
//            throw new xingchenPlusException("课程分类为空");
//        }
//
//        if (StringUtils.isBlank(dto.getGrade())) {
//            throw new xingchenPlusException("课程等级为空");
//        }
//
//        if (StringUtils.isBlank(dto.getTeachmode())) {
//            throw new xingchenPlusException("教育模式为空");
//        }
//
//        if (StringUtils.isBlank(dto.getUsers())) {
//            throw new xingchenPlusException("适应人群为空");
//        }
//
//        if (StringUtils.isBlank(dto.getCharge())) {
//            throw new xingchenPlusException("收费规则为空");
//        }
        //对数据进行封装，调用mapper进行数据持久化
        CourseBase courseBase = new CourseBase();
        //将传入dto的数据设置到 courseBase对象
//        courseBase.setName(dto.getName());
//        courseBase.setMt(dto.getMt());
//        courseBase.setSt(dto.getSt());
        //将dto中和courseBase属性名一样的属性值拷贝到courseBase
        BeanUtils.copyProperties(dto,courseBase);
        //设置机构id
        courseBase.setCompanyId(companyId);
        //创建时间
        courseBase.setCreateDate(LocalDateTime.now());
        //审核状态默认为未提交
        courseBase.setAuditStatus("202002");
        //发布状态默认为未发布
        courseBase.setStatus("203001");
        //课程基本表插入一条记录
        int insert = courseBaseMapper.insert(courseBase);
        //获取课程id
        Long courseId = courseBase.getId();
        CourseMarket courseMarket = new CourseMarket();
        //将dto中和courseMarket属性名一样的属性值拷贝到courseMarket
        BeanUtils.copyProperties(dto,courseMarket);
        courseMarket.setId(courseId);
//        //校验如果课程为收费，价格必须输入
//        String charge = dto.getCharge();
//        if(charge.equals("201001")){//收费
//            if(courseMarket.getPrice() == null || courseMarket.getPrice().floatValue()<=0){
//                throw new xingchenPlusException("课程为收费但是价格为空");
//            }
//        }

//        //向课程营销表插入一条记录
//        int insert1 = courseMarketMapper.insert(courseMarket);

//        if(insert<=0|| insert1<=0){
//            throw new xingchenPlusException("添加课程失败");
//        }
        int insert1 = this.saveCourseMarket(courseMarket);
        if(insert<1 || insert1<1){
            log.error("创建课程过程中出错:{}",dto);
            throw new RuntimeException("创建课程过程中出错");
        }

        //返回
        return getCourseBaseInfo(courseId);
    }

    /**
     * 抽取对营销的保存
     * @param courseMarket
     * @return
     */
    private int saveCourseMarket(CourseMarket courseMarket){


        String charge = courseMarket.getCharge();
        if(StringUtils.isBlank(charge)){
            xingchenPlusException.cast("收费规则没有选择");
        }
        if(charge.equals("201001")){
            if(courseMarket.getPrice()==null || courseMarket.getPrice().floatValue()<=0){
                xingchenPlusException.cast("课程为收费价格不能为空且必须大于0");

            }
        }

        //保存
        boolean b = courseMarketService.saveOrUpdate(courseMarket);

        return b?1:0;

    }

    /**
     * 根据课程id查询课程的基本和营销信息
     * @param courseId 课程id
     * @return 课程的信息
     */
    @Override
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId){
        //课程基本信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        //课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        //组成要返回的数据
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        if(courseMarket!=null){
            BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        }

        //向分类的名称查询出来
        //一级分类
        CourseCategory courseCategory = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategory.getName());
        //二级分类
        CourseCategory courseCategory2 = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategory2.getName());

        return courseBaseInfoDto;
    }
//    @Override
//    public CourseBaseInfoDto getCourseBaseInfo(Long courseId){
//
//        //基本信息
//        CourseBase courseBase = courseBaseMapper.selectById(courseId);
//
//        //营销信息
//        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
//
//        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
//        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
//        BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
//
//        //根据课程分类的id查询分类的名称
//        String mt = courseBase.getMt();
//        String st = courseBase.getSt();
//
//        CourseCategory mtCategory = courseCategoryMapper.selectById(mt);
//        CourseCategory stCategory = courseCategoryMapper.selectById(st);
//        if(mtCategory!=null){
//            //分类名称
//            String mtName = mtCategory.getName();
//            courseBaseInfoDto.setMtName(mtName);
//        }
//        if(stCategory!=null){
//            //分类名称
//            String stName = stCategory.getName();
//            courseBaseInfoDto.setStName(stName);
//        }
//
//
//        return courseBaseInfoDto;
//
//    }


    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto dto) {

        //校验
        //课程id
        Long id = dto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(id);
        if(courseBase==null){
            xingchenPlusException.cast("课程不存在");
        }

        //校验本机构只能修改本机构的课程
        if(!courseBase.getCompanyId().equals(companyId)){
            xingchenPlusException.cast("本机构只能修改本机构的课程");
        }

        //封装基本信息的数据
        BeanUtils.copyProperties(dto,courseBase);
        courseBase.setChangeDate(LocalDateTime.now());

        //更新课程基本信息
        int i = courseBaseMapper.updateById(courseBase);

        //封装营销信息的数据
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto,courseMarket);

        //校验如果课程为收费，必须输入价格且大于0
//        String charge = courseMarket.getCharge();
//        if(charge.equals("201001")){
//            if(courseMarket.getPrice()==null || courseMarket.getPrice().floatValue()<=0){
////                throw new RuntimeException("课程为收费价格不能为空且必须大于0");
//                XueChengPlusException.cast("课程为收费价格不能为空且必须大于0");
//
//            }
//        }

        //请求数据库
        //对营销表有则更新，没有则添加
//        boolean b = courseMarketService.saveOrUpdate(courseMarket);

        saveCourseMarket(courseMarket);
        //查询课程信息
        CourseBaseInfoDto courseBaseInfo = this.getCourseBaseInfo(id);
        return courseBaseInfo;
    }

}