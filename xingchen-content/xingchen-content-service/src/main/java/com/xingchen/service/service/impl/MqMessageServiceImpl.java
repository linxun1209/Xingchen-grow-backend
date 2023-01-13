package com.xingchen.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.xingchen.model.po.MqMessage;
import com.xingchen.service.mapper.MqMessageMapper;
import com.xingchen.service.service.MqMessageService;
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
public class MqMessageServiceImpl extends ServiceImpl<MqMessageMapper, MqMessage> implements MqMessageService {

}
