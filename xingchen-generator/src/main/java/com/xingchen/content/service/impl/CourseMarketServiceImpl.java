package com.xingchen.content.service.impl;

import com.xingchen.content.model.po.CourseMarket;
import com.xingchen.content.mapper.CourseMarketMapper;
import com.xingchen.content.service.CourseMarketService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * 课程营销信息 服务实现类
 * </p>
 *
 * @author xingchen
 */
@Slf4j
@Service
public class CourseMarketServiceImpl extends ServiceImpl<CourseMarketMapper, CourseMarket> implements CourseMarketService {

}
