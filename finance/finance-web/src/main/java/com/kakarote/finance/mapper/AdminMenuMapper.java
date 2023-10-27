package com.kakarote.finance.mapper;
import com.kakarote.core.servlet.BaseMapper;
import com.kakarote.finance.entity.PO.AdminMenu;

import java.util.List;

/**
 * <p>
 * 后台菜单表 Mapper 接口
 * </p>
 *
 * @author zhangzhiwei
 * @since 2020-04-27
 */
public interface AdminMenuMapper extends BaseMapper<AdminMenu> {

    /**
     * 根据角色id集合查询AdminMenu
     * @param roleIds:角色id集合
     * @return menus
     */
    public List<AdminMenu> queryMenuListByRoleIds(List<Long> roleIds);

}
