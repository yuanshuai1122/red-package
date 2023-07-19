package com.atguigu.redpackage.beans.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @program: red-package
 * @description: 发红包dto
 * @author: yuanshuai
 * @create: 2023-07-19 18:46
 **/
@Data
public class SendRedPackageDto {

    /**
     * 总金额
     */
    @NotBlank(message = "总金额不能为空")
    private String totalMoney;

    /**
     * 拆分红包个数
     */
    @NotNull(message = "红包个数不能为空")
    @Min(value = 1, message = "至少拆分一个红包")
    private Integer redPackageNumber;

}
