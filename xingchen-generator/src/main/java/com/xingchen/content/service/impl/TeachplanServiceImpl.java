package com.xingchen.content.service.impl;

import com.xingchen.content.model.po.Teachplan;
import com.xingchen.content.mapper.TeachplanMapper;
import com.xingchen.content.service.TeachplanService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * 课程计划 服务实现类
 * </p>
 *
 * @author xingchen
 */
@Slf4j
@Service
public class TeachplanServiceImpl extends ServiceImpl<TeachplanMapper, Teachplan> implements TeachplanService {

}
