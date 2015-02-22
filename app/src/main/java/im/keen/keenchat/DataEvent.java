package im.keen.keenchat;

/**
 * Created by user on 2015/2/5.
 */
public class DataEvent {
    private int id;
    private String title;
    private String description;

    private boolean isFullEvent;
    //Todo: expand on idea vs event system.

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isFullEvent() {
        return isFullEvent;
    }

    public void setFullEvent(boolean isFullEvent) {
        this.isFullEvent = isFullEvent;
    }

}