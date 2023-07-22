package vip.yuanshuai.redpackage.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import vip.yuanshuai.redpackage.beans.dto.SendRedPackageDto;
import vip.yuanshuai.redpackage.common.Result;
import vip.yuanshuai.redpackage.common.ResultCodeEnum;
import vip.yuanshuai.redpackage.constant.Constant;
import vip.yuanshuai.redpackage.service.RedPackageV1Service;

import java.math.BigDecimal;
import java.util.Map;


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


    /**
     * 抢红包
     *
     * @param redPackageKey 红包密钥
     * @param token         token
     * @return {@link Result}
     */
    @GetMapping(value = "/rob")
    public Result robRedPackage(@RequestParam("redPackageKey") String redPackageKey, @RequestHeader("token") String token) {

        return redPackageV1Service.robRedPackage(redPackageKey, token);
    }


    /**
     * 领取记录
     *
     * @param redPackageKey 红包密钥
     * @param token         令牌
     * @return {@link Result}<{@link Object}>
     */
    @GetMapping(value = "/record")
    public Result<Object> redPackageRecord1(@RequestParam("redPackageKey") String redPackageKey, @RequestHeader("token") String token) {
        Map<String, Integer> map = redisTemplate.opsForHash().entries(Constant.RED_PACKAGE_CONSUME_KEY + redPackageKey);
        //当前用户的领取金额
        //return Result.build(map.get(token), ResultCodeEnum.SUCCESS);
        //全部用户的领取金额
        return Result.build(map, ResultCodeEnum.SUCCESS);
    }

}
