package com.atguigu.redpackage.controller;

import com.atguigu.redpackage.constant.Constant;
import com.atguigu.redpackage.beans.vo.RedPackgeVo;
import com.atguigu.redpackage.util.WebSocketRemoteContainerUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * WebSocket接口测试工具：http://www.jsons.cn/websocket/
 * 接口地址：ws://139.198.163.91:8888/api/websocket/{activityKey}/{token}
 */
@Slf4j
@Tag(name = "即时通讯接口管理")
@ServerEndpoint(value = "/api/websocket/{activityKey}/{token}")
@Component
public class WebSocketApiController {

    private static RedisTemplate redisTemplate;

    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        WebSocketApiController.redisTemplate = redisTemplate;
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("activityKey") String activityKey, @PathParam("token") String token) {
        log.info("连接成功，活动：{}，用户：{}", activityKey, token);
        //添加token与session关联
        WebSocketRemoteContainerUtil.addSession(token, session);
        //添加token与活动关联
        WebSocketRemoteContainerUtil.addTokenToActivity(activityKey, token, redisTemplate);

        //活动开始后，新进入的用户，直接通知活动开始（活动开始后redPackgeVo对象才会生成）
        RedPackgeVo redPackgeVo = (RedPackgeVo) redisTemplate.opsForValue().get(Constant.RED_PACKAGE_INFO_KEY + activityKey);
        if (null != redPackgeVo) {
            //活动开始后，同一个用户不能重复开启红包雨
            Object object = redisTemplate.opsForHash().get(Constant.RED_PACKAGE_USER_KEY + activityKey, token);
            //没有参与过活动
            if (null == object) {
                //通知当前用户开启红包雨活动
                WebSocketRemoteContainerUtil.sendMsg(session, token, redPackgeVo, redisTemplate);
            }
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session, @PathParam("activityKey") String activityKey, @PathParam("token") String token) {
        log.info("退出连接，活动：{}，用户：{}", activityKey, token);

        WebSocketRemoteContainerUtil.removeSession(token);
        WebSocketRemoteContainerUtil.removeTokenToActivity(activityKey, token, redisTemplate);
    }

    /**
     * 发生错误时调用+
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("发生错误");
        error.printStackTrace();
    }

}
