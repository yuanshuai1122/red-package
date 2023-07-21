package vip.yuanshuai.redpackage.beans.vo;

import lombok.Data;

/**
 * 红包雨活动信息类
 *
 * @author yuanshuai
 * @date 2023/07/21
 */
@Data
public class RedPackgeVo {


    /**
     * 红包雨活持续时长:单位ms
     */
    private Integer duration;


    /**
     * 红包生成速率:单位ms
     */
    private Integer generationRate;


    /**
     * 红包雨标识
     */
    private String redPackageKey;


    /**
     * 红包雨活动标识
     */
    private String activityKey;
}
