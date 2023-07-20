package com.atguigu.redpackage.controller;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.atguigu.redpackage.common.Result;
import com.atguigu.redpackage.common.ResultCodeEnum;
import com.atguigu.redpackage.constant.Constant;
import com.atguigu.redpackage.beans.dto.RedPackgeDto;
import com.atguigu.redpackage.beans.vo.RedPackgeVo;
import com.atguigu.redpackage.util.RedPackageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @auther zzyy
 * @create 2023-02-10 14:58
 */
@Slf4j
@Tag(name = "红包雨接口管理v2")
@RestController
@RequestMapping(value = "/api/v2")
public class RedPackageV2Controller {

    @Autowired
    private RedisTemplate redisTemplate;


    @Operation(summary = "添加活动")
    @PostMapping(value = "/add")
    public Result<String> addRedPackage(@Parameter(name = "redPackgeDto", description = "红包雨活动实体", required = true) @RequestBody RedPackgeDto redPackgeDto) {
        // 1.记录活动信息
        //生成活动唯一标识，相当于活动的id标识（活动唯一）
        redPackgeDto.setActivityKey("hd_" + IdUtil.simpleUUID());
        //保存活动，我们可以配置很多活动，互不影响
        redisTemplate.opsForSet().add(Constant.RED_PACKAGE_LIST_KEY, redPackgeDto);

        // 2.活动开始后才初始化红包雨相关信息，保证所有用户同一时刻抢红包（公平、准点、公正）
        // 2.1 计算活动开始的剩余时间：单位秒
        LocalDateTime localDateTime = redPackgeDto.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        long delayTime = ChronoUnit.SECONDS.between(LocalDateTime.now(),localDateTime);
        // 2.2 启动定时任务，注：正式环境可改为rabbitmq/rocketmq延迟消息，当前只是模拟
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {
            //2.2.1 活动开始，拆红包，将总金额totalMoney拆分为redPackageNumber个子红包
            BigDecimal[] splitRedPackages = RedPackageUtil.splitRedPackageAlgorithm(new BigDecimal(String.valueOf(redPackgeDto.getTotalMoney())), redPackgeDto.getRedPackageNumber());//拆分红包算法通过后获得的多个子红包数组
            log.info("拆红包: {}", JSON.toJSONString(splitRedPackages));
            // 2.2.2 红包并保存进list结构里面且设置过期时间
            String key = IdUtil.simpleUUID();
            redisTemplate.opsForList().leftPushAll(Constant.RED_PACKAGE_KEY + key, splitRedPackages);
            // 红包雨的持续时间为过期时间，由于页面有倒计时，需要把倒计时时间加上
            redisTemplate.expire(Constant.RED_PACKAGE_KEY + key, redPackgeDto.getDuration()+10000, TimeUnit.MILLISECONDS);

            // 2.2.3构建前端红包雨活动数据
            RedPackgeVo redPackgeVo = new RedPackgeVo();
            redPackgeVo.setGenerationRate(redPackgeDto.getGenerationRate());
            redPackgeVo.setDuration(redPackgeDto.getDuration());
            redPackgeVo.setActivityKey(redPackgeDto.getActivityKey());
            redPackgeVo.setRedPackageKey(key);
            //保存红包雨活动数据，后续会使用
            redisTemplate.opsForValue().set(Constant.RED_PACKAGE_INFO_KEY + redPackgeVo.getActivityKey(), redPackgeVo, redPackgeDto.getDuration()+10000, TimeUnit.MILLISECONDS);
            // 2.2.4redis广播信息，服务器收到广播消息后，websocket推送消息给前端用户开启红包雨活动
            redisTemplate.convertAndSend(Constant.RED_PACKAGE_REDIS_QUEUE_KEY, JSON.toJSONString(redPackgeVo));
            log.info("红包雨活动广播：{}", JSON.toJSONString(redPackgeVo));
        }, delayTime, TimeUnit.SECONDS);
        executor.shutdown();
        return Result.build(redPackgeDto.getActivityKey(), ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "活动列表")
    @GetMapping(value = "/list")
    public Result<List<RedPackgeDto>> listRedPackage() {
        Set<RedPackgeDto> redPackgeDtoSet = redisTemplate.opsForSet().members(Constant.RED_PACKAGE_LIST_KEY);
        if (!CollectionUtils.isEmpty(redPackgeDtoSet)) {
            List<RedPackgeDto> redPackgeDtoList = new ArrayList<>(redPackgeDtoSet);
            //排序
            Collections.sort(redPackgeDtoList, new Comparator<RedPackgeDto>() {
                @Override
                public int compare(RedPackgeDto p1, RedPackgeDto p2) {
                    return p2.getDate().compareTo(p1.getDate());
                }
            });
            return Result.build(redPackgeDtoList, ResultCodeEnum.SUCCESS);
        }
        return Result.build(null, ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "抢红包")
    @GetMapping(value = "/rob/{redPackageKey}")
    public Result robRedPackage2(@Parameter(name = "redPackageKey", description = "红包标识", required = true) @PathVariable String redPackageKey,
                                 @Parameter(name = "token", description = "用户标识", required = true) @RequestHeader("token") String token) {
        //1 不限制用户抢红包个数。谁快谁抢的多
        //1.1 从红包池(list)里面出队一个作为该客户抢的红包，抢到了一个红包
        Object partRedPackage = redisTemplate.opsForList().leftPop(Constant.RED_PACKAGE_KEY + redPackageKey);
        if (partRedPackage != null) {
            //1.2 抢到红包后需要记录进入list结构(红包金额多个，可重复，如：1,1,2,3)，表示谁抢到了多少钱的某个子红包
            redisTemplate.opsForList().leftPush(Constant.RED_PACKAGE_CONSUME_KEY + redPackageKey + ":" + token, partRedPackage);
            //设置过期时间，相当活动时间长一点即可
            redisTemplate.expire(Constant.RED_PACKAGE_CONSUME_KEY + redPackageKey + ":" + token, 1, TimeUnit.HOURS);
            log.info("用户:{} 抢到了多少钱的红包：{}", token, partRedPackage);
            //TODO 后续异步进mysql或者MQ进一步做统计处理,每一年你发出多少红包，抢到了多少红包，年度总结
        }
        log.info("红包池已抢空，红包标识：{}", redPackageKey);
        return Result.build(null, ResultCodeEnum.SUCCESS);
    }

    @Operation(summary = "领取记录")
    @GetMapping(value = "/record/{redPackageKey}")
    public Result<Integer> redPackageRecord2(@Parameter(name = "redPackageKey", description = "红包标识", required = true) @PathVariable String redPackageKey,
                                             @Parameter(name = "token", description = "用户标识", required = true) @RequestHeader("token") String token) {
        //获取当前用户抢过的红包列表
        long size = redisTemplate.opsForList().size(Constant.RED_PACKAGE_CONSUME_KEY + redPackageKey + ":" + token);
        List<Integer> list = redisTemplate.opsForList().range(Constant.RED_PACKAGE_CONSUME_KEY + redPackageKey + ":" + token, 0, size - 1);
        //当前用户红包总金额
        Integer total = list.stream().reduce(0, Integer::sum);
        return Result.build(total, ResultCodeEnum.SUCCESS);
    }

}
