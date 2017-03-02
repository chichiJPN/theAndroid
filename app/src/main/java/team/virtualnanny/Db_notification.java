package team.virtualnanny;

public class Db_notification {
    private String status;
    private String title;
    private String content;

    public Db_notification() {}

    public Db_notification(String status,
                           String title,
                           String content
                    ) {
        this.status = status;
        this.title = title;
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
