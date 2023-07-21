package vip.yuanshuai.redpackage.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vip.yuanshuai.redpackage.beans.dto.SendRedPackageDto;
import vip.yuanshuai.redpackage.common.Result;
import vip.yuanshuai.redpackage.common.ResultCodeEnum;
import vip.yuanshuai.redpackage.service.RedPackageV1Service;

import java.math.BigDecimal;


/**
 * 红包雨接口管理v1
 *
 * @author yuanshuai
 * @date 2023/07/21
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1")
public class RedPackageV1Controller {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedPackageV1Service redPackageV1Service;


    /**
     * 发红包
     *
     * @param dto DTO 发红包参数
     * @return {@link Result}<{@link String}>
     */
    @PostMapping(value = "/send")
    public Result sendRedPackage(@Valid @RequestBody SendRedPackageDto dto) {
        try {
            // 验证红包个数不能小于1
            int redNums = Integer.parseInt(dto.getRedPackageNums());
            if (redNums <= 0) {
                return Result.build(null, ResultCodeEnum.PARAMS_ERROR);
            }
            // 验证总金额 和 最小金额 不能小于0
            BigDecimal total = new BigDecimal(dto.getTotalAmount());
            BigDecimal min = new BigDecimal(dto.getMinAmount());
            if (total.compareTo(BigDecimal.ZERO) < 0 || min.compareTo(BigDecimal.ZERO) < 0) {
                return Result.build(null, ResultCodeEnum.PARAMS_ERROR);
            }

            return redPackageV1Service.sendRedPackage(redNums, total, min);
        }catch (Exception e) {
            log.error("请求发红包接口发生异常， e：{}", e.toString());
            return Result.build(null, ResultCodeEnum.PARAMS_ERROR);
        }

    }

//    @Operation(summary = "抢红包")
//    @GetMapping(value = "/rob1/{redPackageKey}")
//    public Result robRedPackage1(@Parameter(name = "redPackageKey", description = "红包标识", required = true) @PathVariable String redPackageKey,
//                                 @Parameter(name = "token", description = "用户标识", required = true) @RequestHeader("token") String token) {
//        //1 验证某个用户是否抢过红包，不可以多抢
//        Object redPackage = redisTemplate.opsForHash().get(Constant.RED_PACKAGE_CONSUME_KEY + redPackageKey, token);
//        //2 没有抢过可以去抢红包，否则返回-2表示该用户抢过红包了
//        if (null == redPackage) {
//            //2.1 从红包池(list)里面出队一个作为该客户抢的红包，抢到了一个红包
//            Object partRedPackage = redisTemplate.opsForList().leftPop(Constant.RED_PACKAGE_KEY + redPackageKey);
//            if (partRedPackage != null) {
//                //2.2 抢到红包后需要记录进入hash结构，表示谁抢到了多少钱的某个子红包
//                redisTemplate.opsForHash().put(Constant.RED_PACKAGE_CONSUME_KEY + redPackageKey, token, partRedPackage);
//                log.info("用户:{} 抢到了多少钱的红包：{}", token, partRedPackage);
//                //TODO 后续异步进mysql或者MQ进一步做统计处理,每一年你发出多少红包，抢到了多少红包，年度总结
//                return Result.build(partRedPackage, ResultCodeEnum.SUCCESS);
//            }
//            // 抢完了
//            log.info("红包池已抢空，红包标识：{}", redPackageKey);
//            return Result.build(null, ResultCodeEnum.RED_PACKAGE_FINISHED);
//        }
//        //3 某个用户抢过了，不可以作弊抢多次
//        return Result.build(null, ResultCodeEnum.RED_PACKAGE_REAPT);
//    }
//
//    @Operation(summary = "领取记录")
//    @GetMapping(value = "/record1/{redPackageKey}")
//    public Result<Object> redPackageRecord1(@Parameter(name = "redPackageKey", description = "红包标识", required = true) @PathVariable String redPackageKey,
//                                            @Parameter(name = "token", description = "用户标识", required = true) @RequestHeader("token") String token) {
//        Map<String, Integer> map = redisTemplate.opsForHash().entries(Constant.RED_PACKAGE_CONSUME_KEY + redPackageKey);
//        //当前用户的领取金额
//        //return Result.build(map.get(token), ResultCodeEnum.SUCCESS);
//        //全部用户的领取金额
//        return Result.build(map, ResultCodeEnum.SUCCESS);
//    }

}
