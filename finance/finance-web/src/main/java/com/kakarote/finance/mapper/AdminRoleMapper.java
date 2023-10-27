package com.kakarote.finance.mapper;
import com.kakarote.core.servlet.BaseMapper;
import com.kakarote.finance.entity.PO.AdminRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 角色表 Mapper 接口
 * </p>
 *
 * @author zhangzhiwei
 * @since 2020-04-27
 */
public interface AdminRoleMapper extends BaseMapper<AdminRole> {
    /**
     * 查询角色菜单
     *
     * @param parentId parentId
     * @param roleId   roleId
     * @return data
     */
    public List<Long> getRoleMenu(@Param("parentId") Long parentId, @Param("roleId") Long roleId);
}
