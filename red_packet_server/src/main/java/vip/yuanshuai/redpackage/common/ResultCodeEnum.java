package vip.yuanshuai.redpackage.common;

import lombok.Getter;

@Getter // 提供获取属性值的getter方法
public enum ResultCodeEnum {

    SUCCESS(200, "操作成功"),
    RED_PACKAGE_FINISHED(201, "红包抢完了"),
    RED_PACKAGE_REAPT(202, "你已经抢过红包了，不能重复抢"),
    PARAMS_ERROR(203, "参数错误"),
    LOGIN_AUTH(208, "用户未登录"),

    ;

    private Integer code;      // 业务状态码
    private String message;    // 响应消息

    private ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
