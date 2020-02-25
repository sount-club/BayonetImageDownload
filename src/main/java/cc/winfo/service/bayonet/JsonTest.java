package cc.winfo.service.bayonet;

import cc.winfo.service.bayonet.entity.BayonetEvent;
import com.alibaba.fastjson.JSON;

/**
 * @author lizhichao
 * @description
 * @date 2020/2/25 15:45
 */
public class JsonTest {

    public static void main(String[] args) {
        String json = "{\"eventId\":1235,\"time\":\"2019/3/8 9:58:57\"}";

        BayonetEvent bayonetEvent = JSON.parseObject(json, BayonetEvent.class);

        System.out.println(bayonetEvent);
    }
}
