import java.util.Map;

/**
 * Created by francois on 2016-08-23.
 */
public class TwilioSmsRequest {
    private Map<String, String> path;
    private Map<String, String> querystring;
    private Map<String, String> header;

    public Map<String, String> getPath() {
        return path;
    }

    public void setPath(Map<String, String> path) {
        this.path = path;
    }

    public Map<String, String> getQuerystring() {
        return querystring;
    }

    public void setQuerystring(Map<String, String> querystring) {
        this.querystring = querystring;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }
}
