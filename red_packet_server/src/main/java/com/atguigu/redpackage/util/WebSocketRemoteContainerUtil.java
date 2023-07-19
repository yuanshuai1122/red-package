package com.atguigu.redpackage.util;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.atguigu.redpackage.constant.Constant;
import com.atguigu.redpackage.beans.vo.RedPackgeVo;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket工具类
 */
@Slf4j
public class WebSocketRemoteContainerUtil {

    //保存用户与Session关系
    // 注意：websocket的Session不能序列化到redis，只能在内存中保存，集群环境配合redis广播，通知所有用户
    private static Map<String, Session> tokenToSessionMap = new ConcurrentHashMap<>();

    public static void addSession(String token, Session session) {
        tokenToSessionMap.put(token, session);
    }

    public static void removeSession(String token) {
        tokenToSessionMap.remove(token);
    }

    public static Session getSession(String token) {
        return tokenToSessionMap.get(token);
    }

    public static void addTokenToActivity(String activityKey, String token, RedisTemplate redisTemplate) {
        redisTemplate.boundSetOps(Constant.RED_PACKAGE_ACTIVITY_KEY + activityKey).add(token);
    }

    public static void removeTokenToActivity(String activityKey, String token, RedisTemplate redisTemplate) {
        redisTemplate.boundSetOps(Constant.RED_PACKAGE_ACTIVITY_KEY + activityKey).remove(token);
    }

    public static Set<String> getActivityTokenList(String activityKey, RedisTemplate redisTemplate) {
        return (Set<String>) redisTemplate.boundSetOps(Constant.RED_PACKAGE_ACTIVITY_KEY + activityKey).members();
    }

    /**
     * 群发消息
     *
     * @param redPackgeVo
     * @param redisTemplate
     */
    public static void sendMsg(RedPackgeVo redPackgeVo, RedisTemplate redisTemplate) {
        Set<String> tokenSet = WebSocketRemoteContainerUtil.getActivityTokenList(redPackgeVo.getActivityKey(), redisTemplate);
        log.info("接收人：{}", JSON.toJSONString(tokenSet));
        if (!CollectionUtils.isEmpty(tokenSet)) {
            for (String token : tokenSet) {
                Session session = WebSocketRemoteContainerUtil.getSession(token);
                if (null != session) {
                    session.getAsyncRemote().sendText(JSON.toJSONString(redPackgeVo, SerializerFeature.DisableCircularReferenceDetect));//异步发送消息.
                    //记录用户已开启
                    redisTemplate.opsForHash().put(Constant.RED_PACKAGE_USER_KEY + redPackgeVo.getActivityKey(), token, 1);
                    //设置过期时间
                    redisTemplate.expire(Constant.RED_PACKAGE_USER_KEY + redPackgeVo.getActivityKey(), redPackgeVo.getDuration()+10000, TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    /**
     * 单发消息
     *
     * @param session
     * @param redPackgeVo
     * @param redisTemplate
     */
    public static void sendMsg(Session session, String token, RedPackgeVo redPackgeVo, RedisTemplate redisTemplate) {
        session.getAsyncRemote().sendText(JSON.toJSONString(redPackgeVo, SerializerFeature.DisableCircularReferenceDetect));//异步发送消息.
        //记录用户已开启
        redisTemplate.opsForHash().put(Constant.RED_PACKAGE_USER_KEY + redPackgeVo.getActivityKey(), token, 1);
        //设置过期时间
        redisTemplate.expire(Constant.RED_PACKAGE_USER_KEY + redPackgeVo.getActivityKey(), redPackgeVo.getDuration()+10000, TimeUnit.MILLISECONDS);
    }


}
