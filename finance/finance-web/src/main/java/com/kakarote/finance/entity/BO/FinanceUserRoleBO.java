package com.kakarote.finance.entity.BO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@ToString
@ApiModel("账套授权保存")
public class FinanceUserRoleBO {


    @ApiModelProperty(value = "员工id")
    private Long userId;

    @ApiModelProperty(value = "角色id")
    private List<Long> roleIdList = new ArrayList<>();

}