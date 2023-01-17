package com.xingchen.media.service.impl;

import com.xingchen.media.model.po.MediaProcessHistory;
import com.xingchen.media.mapper.MediaProcessHistoryMapper;
import com.xingchen.media.service.MediaProcessHistoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Resource;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xingchen
 */
@Slf4j
@Service
public class MediaProcessHistoryServiceImpl extends ServiceImpl<MediaProcessHistoryMapper, MediaProcessHistory> implements MediaProcessHistoryService {

}
