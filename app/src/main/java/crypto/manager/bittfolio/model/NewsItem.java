package crypto.manager.bittfolio.model;

/**
 * Created by ghodk on 2/5/2018.
 */

public class NewsItem {

    private String url;
    private String title;
    private String source;
    private String time;
    private String contents;

    public NewsItem(String url, String title, String source, String time, String contents) {
        this.url = url;
        this.title = title;
        this.source = source;
        this.time = time;
        this.contents = contents;
    }

    public NewsItem() {

    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }
}
