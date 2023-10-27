package com.kakarote.finance.entity.BO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;


/**
 * <p>
 * 账套表
 * </p>
 *
 * @author dsc
 * @since 2021-08-28
 */
@Data
@ToString
@ApiModel("账套授权保存")
public class FinanceAccountAuthSaveBO {


    @ApiModelProperty(value = "账套id")
    private Long accountId;

    @ApiModelProperty(value = "授权员工")
    private List<FinanceUserRoleBO> userList;

}
