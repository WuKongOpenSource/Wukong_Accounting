package com.kakarote.finance.entity.BO;

import com.kakarote.core.entity.PageEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangzhiwei
 * @since 2021-08-24
 */
@Data
@ApiModel(value = "FinanceAdjuvantCarte对象", description = "")
@EqualsAndHashCode(callSuper = false)
public class FinanceAdjuvantCarteBO extends PageEntity {

    @ApiModelProperty(value = "名称")
    private String carteName;

    @ApiModelProperty(value = "核算表id")
    private Long adjuvantId;

    @ApiModelProperty(value = "搜索条件")
    private String search;

    @ApiModelProperty(value = "状态 1.正常启用 2.正常禁用 3.删除")
    private Integer status;

    @ApiModelProperty(value = "标签 1 线索 2 客户 3 联系人 4 产品 5 商机 6 合同 7回款 8 发票 9 部门 10 员工 11 自定义")
    private Integer label;


}
