package com.kakarote.core.feign.examine.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JiaS
 * @date 2020/12/17
 */
@Data
public class ExamineGeneralBO {

    @ApiModelProperty("流程ID")
    private Long flowId;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("用户列表")
    private List<Long> userList;

    @ApiModelProperty("部门列表")
    private List<Long> deptList;

    @ApiModelProperty("角色列表")
    private List<Long> roleList;
}
