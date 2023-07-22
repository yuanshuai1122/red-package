package vip.yuanshuai.redpackage.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import vip.yuanshuai.redpackage.common.Result;
import vip.yuanshuai.redpackage.common.ResultCodeEnum;
import vip.yuanshuai.redpackage.constant.Constant;
import vip.yuanshuai.redpackage.util.RedPackageUtil;

import java.math.BigDecimal;
import java.util.List;
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
     * @param redNums 红包个数
     * @param total   总金额
     * @param min     每个红包最小金额
     * @return {@link Result}
     */
    public Result sendRedPackage(int redNums, BigDecimal total, BigDecimal min) {
        //1 拆红包，将总金额total拆分为redNums个子红包
        List<BigDecimal> splitRedPackages = RedPackageUtil.splitRedPackage(total, min, new BigDecimal(String.valueOf(redNums)));
        log.info("拆红包结果: {}", new Gson().toJson(splitRedPackages));
        //2 发红包并保存进list结构里面且设置过期时间
        String key = RedPackageUtil.generateUUID();
        // 设置红包拆分缓存 由于这里是发红包不存在并发 理论上可以写成两行
        redisTemplate.opsForList().leftPushAll(Constant.RED_PACKAGE_KEY + key, splitRedPackages);
        // 设置过期时间  默认24小时退回
        redisTemplate.expire(Constant.RED_PACKAGE_KEY + key, 1, TimeUnit.DAYS);

        //3 发红包OK，返回前台显示
        return Result.build(key, ResultCodeEnum.SUCCESS);
    }

    /**
     * 抢红包
     *
     * @param redPackageKey 红包密钥
     * @param token         token
     * @return {@link Result}
     */
    public Result robRedPackage(String redPackageKey, String token) {
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
}
