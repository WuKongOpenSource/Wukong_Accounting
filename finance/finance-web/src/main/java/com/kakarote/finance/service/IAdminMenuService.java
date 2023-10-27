package com.kakarote.finance.service;

import com.alibaba.fastjson.JSONObject;
import com.kakarote.core.servlet.BaseService;
import com.kakarote.finance.common.admin.AdminRoleTypeEnum;
import com.kakarote.finance.entity.PO.AdminMenu;

import java.util.List;

/**
 * <p>
 * 后台菜单表 服务类
 * </p>
 *
 * @author zhangzhiwei
 * @since 2020-04-27
 */
public interface IAdminMenuService extends BaseService<AdminMenu> {
    /**
     * 查询用户所拥有的菜单权限
     *
     * @param userId    用户列表
     * @return 菜单权限的并集
     */
    public List<AdminMenu> queryMenuList(Long userId);


    /**
     * 通过角色列表查询菜单列表
     *
     * @param roleIds 角色ids
     * @return 菜单
     */
    public List<AdminMenu> queryMenuListByRoleIds(List<Long> roleIds);

    /**
     * 根据类型查询菜单
     *
     * @param typeEnum type
     * @return data
     */
    public JSONObject getMenuListByType(AdminRoleTypeEnum typeEnum);


    /**
     * 查询所有菜单
     *
     * @return 菜单列表
     * @author zhangzhiwei
     */
    List<AdminMenu> queryAllMenuList();


}
