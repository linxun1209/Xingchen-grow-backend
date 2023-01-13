package com.xingchen.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.xingchen.model.po.MqMessageHistory;
import com.xingchen.service.mapper.MqMessageHistoryMapper;
import com.xingchen.service.service.MqMessageHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xingchen
 */
@Slf4j
@Service
public class MqMessageHistoryServiceImpl extends ServiceImpl<MqMessageHistoryMapper, MqMessageHistory> implements MqMessageHistoryService {

}
