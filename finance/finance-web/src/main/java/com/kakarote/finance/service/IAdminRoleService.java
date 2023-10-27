package com.kakarote.finance.service;

import com.alibaba.fastjson.JSONObject;
import com.kakarote.core.servlet.BaseService;
import com.kakarote.finance.common.admin.AdminRoleTypeEnum;
import com.kakarote.finance.entity.PO.AdminRole;

import java.util.List;

/**
 * <p>
 * 角色表 服务类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2020-04-27
 */
public interface IAdminRoleService extends BaseService<AdminRole> {
    /**
     * 查询用户所属权限
     *
     * @param userId 当前用户ID
     * @return obj
     */
    public JSONObject auth(Long userId);

    /**
     * 根据类型查询角色
     *
     * @param roleTypeEnum type
     * @return data
     */
    public List<AdminRole> getRoleByType(AdminRoleTypeEnum roleTypeEnum);



}


