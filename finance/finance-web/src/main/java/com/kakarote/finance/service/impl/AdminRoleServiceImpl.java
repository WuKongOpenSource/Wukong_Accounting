package com.kakarote.finance.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kakarote.common.utils.UserUtil;
import com.kakarote.core.common.cache.AdminCacheKey;
import com.kakarote.core.common.enums.DataAuthEnum;
import com.kakarote.core.redis.Redis;
import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.finance.common.admin.AdminRoleTypeEnum;
import com.kakarote.finance.entity.PO.AdminMenu;
import com.kakarote.finance.entity.PO.AdminRole;
import com.kakarote.finance.mapper.AdminRoleMapper;
import com.kakarote.finance.service.IAdminMenuService;
import com.kakarote.finance.service.IAdminRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * <p>
 * 角色表 服务实现类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2020-04-27
 */
@Service
public class AdminRoleServiceImpl extends BaseServiceImpl<AdminRoleMapper, AdminRole> implements IAdminRoleService {

    @Autowired
    private IAdminMenuService adminMenuService;

    @Autowired
    private Redis redis;

    /**
     * 查询用户所属权限
     *
     * @return obj
     */
    @Override
    public JSONObject auth(Long userId) {
        String cacheKey = AdminCacheKey.USER_AUTH_CACHE_KET + userId.toString();
        if (redis.exists(cacheKey)) {
            return redis.get(cacheKey);
        }
        List<AdminMenu> adminMenus = adminMenuService.queryMenuList(userId);
        JSONObject jsonObject = createMenu(new HashSet<>(adminMenus), 0L);
        redis.setex(cacheKey, 300, jsonObject);
        return jsonObject;
    }

    private JSONObject createMenu(Set<AdminMenu> adminMenuList, Long parentId) {
        JSONObject jsonObject = new JSONObject();
        adminMenuList.forEach(adminMenu -> {
            if (Objects.equals(parentId, adminMenu.getParentId())) {
                if (Objects.equals(1, adminMenu.getMenuType())) {
                    JSONObject object = createMenu(adminMenuList, adminMenu.getMenuId());
                    if (!object.isEmpty()) {
                        jsonObject.put(adminMenu.getRealm(), object);
                    }
                } else {
                    jsonObject.put(adminMenu.getRealm(), Boolean.TRUE);
                }
            }
        });
        return jsonObject;
    }
    /**
     * 根据类型查询角色
     *
     * @param roleTypeEnum type
     * @return data
     */
    @Override
    public List<AdminRole> getRoleByType(AdminRoleTypeEnum roleTypeEnum) {
        List<AdminRole> recordList = lambdaQuery()
                .eq(AdminRole::getRoleType, roleTypeEnum.getType())
                .orderByAsc(AdminRole::getSorting)
                .list();
        String realm = roleTypeEnum.getName();
        LambdaQueryWrapper<AdminMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(AdminMenu::getMenuId);
        wrapper.eq(AdminMenu::getParentId, 0L);
        wrapper.eq(AdminMenu::getRealm, realm);
        AdminMenu adminMenu = adminMenuService.getOne(wrapper);
        if (adminMenu != null) {
            Long pid = adminMenuService.getOne(wrapper).getMenuId();
            if (recordList.size() != 0) {
                recordList.forEach(record -> {
                    Map<String, List<Long>> map = new HashMap<>();
                    List<Long> data = getBaseMapper().getRoleMenu(pid, record.getRoleId());
                    map.put("data", data);;
                });
            }
        }
        return recordList;
    }

}
