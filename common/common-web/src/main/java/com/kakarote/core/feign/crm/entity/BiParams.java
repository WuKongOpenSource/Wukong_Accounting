package com.kakarote.core.feign.crm.entity;

import com.kakarote.core.entity.PageEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author zhangzhiwei
 * bi参数
 */
@ApiModel("bi查询相关参数")
@Data
public class BiParams extends PageEntity {

    @ApiModelProperty("部门ID")
    private Long deptId;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("用户IDs")
    private List<Long> userIds;

    @ApiModelProperty("类型")
    private String type;

    @ApiModelProperty("开始时间")
    private String startTime;

    @ApiModelProperty("结束时间")
    private String endTime;

    @ApiModelProperty("0 部门 1员工")
    private Integer isUser = 1;

    @ApiModelProperty("类型ID")
    private Long typeId;

    @ApiModelProperty("年份")
    private Integer year;

    @ApiModelProperty("菜单ID")
    private Long menuId;

    @ApiModelProperty("月份")
    private Integer moneyType;

    @ApiModelProperty("数据类型")
    private Integer dataType;

    @ApiModelProperty("查询类型，跟进记录需要")
    private Integer queryType;

    @ApiModelProperty("crm类型")
    private Integer label;

    @ApiModelProperty("企业ID")
    private Long companyId;

    @ApiModelProperty("排序方式")
    private Integer order;

    @ApiModelProperty("排序字段")
    private String sortField;

    private Integer checkStatus;

    private Integer subUser;

    private String search;

    private Integer day;
}
