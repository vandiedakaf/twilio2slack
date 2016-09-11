import java.util.Map;

/**
 * Created by francois on 2016-09-11.
 */
public class Parameters {
    private Map<String, String> path;
    private Map<String, String> queryString;
    private Map<String, String> header;

    public Map<String, String> getPath() {
        return path;
    }

    public void setPath(Map<String, String> path) {
        this.path = path;
    }

    public Map<String, String> getQueryString() {
        return queryString;
    }

    public void setQueryString(Map<String, String> queryString) {
        this.queryString = queryString;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }
}
