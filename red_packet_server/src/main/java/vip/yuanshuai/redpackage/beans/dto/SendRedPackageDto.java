package vip.yuanshuai.redpackage.beans.dto;

import jakarta.validation.constraints.NotBlank;
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
    private String totalAmount;

    /**
     * 拆分红包个数
     */
    @NotBlank(message = "拆红包个数不能为空")
    private String redPackageNums;

    /**
     * 每个红包最小金额
     */
    @NotBlank(message = "红包最小金额不能为空")
    private String minAmount;

}
