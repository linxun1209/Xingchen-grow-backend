package com.xingchen.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xingchen.media.po.MediaProcess;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author xingchen
 */
public interface MediaProcessMapper extends BaseMapper<MediaProcess> {
    @Select("SELECT * FROM media_process t where t.id % #{shardTotal} = #{shardIndex} LIMIT #{count}")
    public List<MediaProcess> selectListByShardIndex(@Param("shardTotal") int shardTotal, @Param("shardIndex") int shardIndex, @Param("count") int count);

}
