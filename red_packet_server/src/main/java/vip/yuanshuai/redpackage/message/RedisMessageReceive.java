package vip.yuanshuai.redpackage.message;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import vip.yuanshuai.redpackage.beans.vo.RedPackgeVo;
import vip.yuanshuai.redpackage.util.WebSocketRemoteContainerUtil;

/**
 * redis广播消息处理类
 */
@Component
public class RedisMessageReceive {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 接收redis广播消息的方法
     */
    public void receiveMessage(String message) {
        System.out.println("----------收到消息了message：" + message);
        //序列化消息
        Object msg = redisTemplate.getValueSerializer().deserialize(message.getBytes());
        if (null != msg) {
            RedPackgeVo redPackgeVo = new Gson().fromJson(msg.toString(), RedPackgeVo.class);
            //WebSocket发送消息
            WebSocketRemoteContainerUtil.sendMsg(redPackgeVo, redisTemplate);
        }
    }

}
