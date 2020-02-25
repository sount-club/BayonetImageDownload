package cc.winfo.service.bayonet.entity;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Date;

/**
 * @author lizhichao
 * @description
 * @date 2020/2/25 15:41
 */
public class BayonetEvent {



    private Long eventId;

    @JSONField(format = "yyyy/MM/dd HH:mm:ss")
    private Date time;

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "BayonetEvent{" +
                "eventId=" + eventId +
                ", time=" + time +
                '}';
    }
}
