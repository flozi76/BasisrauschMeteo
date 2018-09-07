package ch.icarosdev.webviewloadlib.domain;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class PageDefinition {

    public String isImage;
    public String pageName;
    public String urlToLoad;
    public String postUrlToExecuteBefore;
    public String postArguments;
    public int initialScale;
    public boolean showsourceurl;
    public boolean isCustomPage;

    public PageDefinition() {
        super();
    }

    public PageDefinition(String pageName, String urlToLoad,
                          String postUrlToExecuteBefore, String postArguments,
                          int initialScale,
                          String isImage) {
        super();
        this.pageName = pageName;
        this.urlToLoad = urlToLoad;
        this.postUrlToExecuteBefore = postUrlToExecuteBefore;
        this.postArguments = postArguments;
        this.initialScale = initialScale;
        this.isImage = isImage;
        this.showsourceurl = false;
        this.isCustomPage = false;
    }

    public String getPreparedUrl() {

        String dateToday = getDateString(0);
        String dateTomorrow = getDateString(1);

        return urlToLoad.replace("{XXXX_XXXX}", dateToday).replace("{YYYY_YYYY}", dateTomorrow);
    }

    private String getDateString(int offset) {
        Calendar cal = new GregorianCalendar();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        cal.add(Calendar.DATE, offset);
        dateFormat.setCalendar(cal);
        return dateFormat.format(cal.getTime());
    }

    public String getUrlToLoad() {
        return urlToLoad;
    }

    public void setUrlToLoad(String urlToLoad) {
        if (urlToLoad != null && !urlToLoad.startsWith("http")) {
            urlToLoad = "http://" + urlToLoad;
        }

        if (urlToLoad.endsWith(".jpg") || urlToLoad.endsWith(".gif") || urlToLoad.endsWith(".bmp") || urlToLoad.endsWith(".png")) {
            this.isImage = "true";
        }

        this.urlToLoad = urlToLoad;
    }
}
