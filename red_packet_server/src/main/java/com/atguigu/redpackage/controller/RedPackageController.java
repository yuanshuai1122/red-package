package com.atguigu.redpackage.controller;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.atguigu.redpackage.common.Result;
import com.atguigu.redpackage.common.ResultCodeEnum;
import com.atguigu.redpackage.constant.Constant;
import com.atguigu.redpackage.util.RedPackageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @auther zzyy
 * @create 2023-02-10 14:58
 */
@Slf4j
@Tag(name = "红包雨接口管理v1")
@RestController
@RequestMapping(value = "/api/v1")
public class RedPackageController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Operation(summary = "发红包")
    @GetMapping(value = "/send/{totalMoney}/{redPackageNumber}")
    public Result<String> sendRedPackage(@Parameter(name = "totalMoney", description = "红包金额", required = true) @PathVariable int totalMoney,
                                         @Parameter(name = "redPackageNumber", description = "红包个数", required = true) @PathVariable int redPackageNumber) {
        //1 拆红包，将总金额totalMoney拆分为redPackageNumber个子红包
        Integer[] splitRedPackages = RedPackageUtil.splitRedPackageAlgorithm(totalMoney, redPackageNumber);
        log.info("拆红包: {}", JSON.toJSONString(splitRedPackages));
        //2 发红包并保存进list结构里面且设置过期时间
        String key = IdUtil.simpleUUID();
        redisTemplate.opsForList().leftPushAll(Constant.RED_PACKAGE_KEY + key, splitRedPackages);
        redisTemplate.expire(Constant.RED_PACKAGE_KEY + key, 1, TimeUnit.DAYS);

        //3 发红包OK，返回前台显示
        return Result.build(key, ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "抢红包")
    @GetMapping(value = "/rob1/{redPackageKey}")
    public Result robRedPackage1(@Parameter(name = "redPackageKey", description = "红包标识", required = true) @PathVariable String redPackageKey,
                                 @Parameter(name = "token", description = "用户标识", required = true) @RequestHeader("token") String token) {
        //1 验证某个用户是否抢过红包，不可以多抢
        Object redPackage = redisTemplate.opsForHash().get(Constant.RED_PACKAGE_CONSUME_KEY + redPackageKey, token);
        //2 没有抢过可以去抢红包，否则返回-2表示该用户抢过红包了
        if (null == redPackage) {
            //2.1 从红包池(list)里面出队一个作为该客户抢的红包，抢到了一个红包
            Object partRedPackage = redisTemplate.opsForList().leftPop(Constant.RED_PACKAGE_KEY + redPackageKey);
            if (partRedPackage != null) {
                //2.2 抢到红包后需要记录进入hash结构，表示谁抢到了多少钱的某个子红包
                redisTemplate.opsForHash().put(Constant.RED_PACKAGE_CONSUME_KEY + redPackageKey, token, partRedPackage);
                log.info("用户:{} 抢到了多少钱的红包：{}", token, partRedPackage);
                //TODO 后续异步进mysql或者MQ进一步做统计处理,每一年你发出多少红包，抢到了多少红包，年度总结
                return Result.build(partRedPackage, ResultCodeEnum.SUCCESS);
            }
            // 抢完了
            log.info("红包池已抢空，红包标识：{}", redPackageKey);
            return Result.build(null, ResultCodeEnum.RED_PACKAGE_FINISHED);
        }
        //3 某个用户抢过了，不可以作弊抢多次
        return Result.build(null, ResultCodeEnum.RED_PACKAGE_REAPT);
    }

    @Operation(summary = "领取记录")
    @GetMapping(value = "/record1/{redPackageKey}")
    public Result<Object> redPackageRecord1(@Parameter(name = "redPackageKey", description = "红包标识", required = true) @PathVariable String redPackageKey,
                                            @Parameter(name = "token", description = "用户标识", required = true) @RequestHeader("token") String token) {
        Map<String, Integer> map = redisTemplate.opsForHash().entries(Constant.RED_PACKAGE_CONSUME_KEY + redPackageKey);
        //当前用户的领取金额
        //return Result.build(map.get(token), ResultCodeEnum.SUCCESS);
        //全部用户的领取金额
        return Result.build(map, ResultCodeEnum.SUCCESS);
    }

}
