package com.kakarote.finance.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONObject;
import com.kakarote.common.utils.UserUtil;
import com.kakarote.core.servlet.BaseServiceImpl;
import com.kakarote.finance.common.AccountSet;
import com.kakarote.finance.constant.FinanceConst;
import com.kakarote.finance.entity.PO.AdminMenu;
import com.kakarote.finance.entity.PO.FinanceAccountUser;
import com.kakarote.finance.mapper.FinanceAccountUserMapper;
import com.kakarote.finance.service.IAdminMenuService;
import com.kakarote.finance.service.IFinanceAccountUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 账套员工对应关系表 服务实现类
 * </p>
 *
 * @author dsc
 * @since 2021-08-29
 */
@Service
public class FinanceAccountUserServiceImpl extends BaseServiceImpl<FinanceAccountUserMapper, FinanceAccountUser> implements IFinanceAccountUserService {

    @Autowired
    private IAdminMenuService adminMenuService;



    public List<Long> getRoleList() {
        List<FinanceAccountUser> accountUserList = lambdaQuery()
                .eq(FinanceAccountUser::getAccountId, AccountSet.getAccountSetId())
                .eq(FinanceAccountUser::getUserId, UserUtil.getUserId())
                .isNotNull(FinanceAccountUser::getRoleId).list();
        if (CollUtil.isEmpty(accountUserList)) {
            return new ArrayList<>();
        }
        return accountUserList.stream().map(FinanceAccountUser::getRoleId).collect(Collectors.toList());
    }

    /**
     * 查询用户所属权限
     *
     * @return obj
     */
    @Override
    public JSONObject financeAuth() {
/*        List<AdminMenu> adminMenus = queryMenuListByAdmin();
        if (CollUtil.isEmpty(adminMenus)) {
            return new JSONObject();
        }*/
        //查询所有菜单
        List<AdminMenu> menus = adminMenuService.queryMenuList(UserUtil.getUserId());
        JSONObject jsonObject = createMenu(new HashSet<>(menus), 0L);
        JSONObject result = new JSONObject();
        result.put(FinanceConst.FINANCE_SERVICE, jsonObject.getJSONObject(FinanceConst.FINANCE_SERVICE));
        return result;
    }

    /**
     * 财务管理获取角色权限
     *
     * @return
     */
    public List<AdminMenu> queryMenuListByAdmin() {
        if (UserUtil.isAdmin()) {
            return adminMenuService.queryMenuList(UserUtil.getUserId());
        }
        List<Long> roleList = getRoleList();
        if (roleList.size() > 0) {
            return adminMenuService.queryMenuListByRoleIds(roleList);
        }
        return new ArrayList<>();
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
}
