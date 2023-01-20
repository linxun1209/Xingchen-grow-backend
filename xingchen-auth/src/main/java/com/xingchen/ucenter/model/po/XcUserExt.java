package com.xingchen.ucenter.model.po;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xingchen
 * @version V1.0
 * @Package com.xuecheng.ucenter.model.po
 * @date 2023/1/19 22:17
 */
@Data
public class XcUserExt extends XcUser {
    /**
     * 用户权限
     */
    List<String> permissions = new ArrayList<>();
}