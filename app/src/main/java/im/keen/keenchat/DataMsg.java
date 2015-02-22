package im.keen.keenchat;

/**
 * Created by user on 2015/2/5.
 */
public class DataMsg {
    private Integer id;
    private Integer user_id;
    private Integer channel_id;
    private String message_text;
    private long time;

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getUser_id() {
        return user_id;
    }
    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }
    public Integer getChannel_id() {
        return channel_id;
    }
    public void setChannel_id(Integer channel_id) {
        this.channel_id = channel_id;
    }
    public String getMessage_text() {
        return message_text;
    }
    public void setMessage_text(String message_text) {
        this.message_text = message_text;
    }
    public long getTime() {
        return time;
    }
    public void setTime(long time) {
        this.time = time;
    }
}
