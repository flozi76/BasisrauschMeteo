package ch.icarosdev.pagesdatabase;

/**
 * Created with IntelliJ IDEA.
 * User: Florian
 * Date: 06.10.13
 * Time: 17:07
 * To change this template use File | Settings | File Templates.
 */
public class Page {

    //private variables
    int id;
    String name;
    String url;
    String category;

    // Empty constructor
    public Page() {

    }

    // constructor
    public Page(int id, String name, String url, String category) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.category = category;
    }

    // constructor
    public Page(String name, String url, String category) {
        this.name = name;
        this.url = url;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
