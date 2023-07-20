package vip.yuanshuai.redpackage.service;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import vip.yuanshuai.redpackage.beans.dto.SendRedPackageDto;
import vip.yuanshuai.redpackage.common.Result;
import vip.yuanshuai.redpackage.common.ResultCodeEnum;
import vip.yuanshuai.redpackage.constant.Constant;
import vip.yuanshuai.redpackage.util.RedPackageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * @program: red-package
 * @description: 红包服务
 * @author: yuanshuai
 * @create: 2023-07-19 18:41
 **/
@Service
@Slf4j
public class RedPackageV1Service {

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 发红包
     *
     * @param dto DTO
     * @return {@link Result}<{@link String}>
     */
    public Result<String> sendRedPackage(SendRedPackageDto dto) {
        //1 拆红包，将总金额totalMoney拆分为redPackageNumber个子红包
        BigDecimal totalMoney = new BigDecimal(dto.getTotalMoney());
        BigDecimal[] splitRedPackages = RedPackageUtil.splitRedPackageAlgorithm(totalMoney, dto.getRedPackageNumber());
        log.info("拆红包: {}", JSON.toJSONString(splitRedPackages));
        //2 发红包并保存进list结构里面且设置过期时间
        String key = IdUtil.simpleUUID();
        redisTemplate.opsForList().leftPushAll(Constant.RED_PACKAGE_KEY + key, splitRedPackages);
        redisTemplate.expire(Constant.RED_PACKAGE_KEY + key, 1, TimeUnit.DAYS);

        //3 发红包OK，返回前台显示
        return Result.build(key, ResultCodeEnum.SUCCESS);

    }
}
