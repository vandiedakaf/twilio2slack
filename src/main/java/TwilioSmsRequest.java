import java.util.Map;

/**
 * Created by francois on 2016-08-23.
 */
public class TwilioSmsRequest {
    private Map<String, String> parameters;
    private Map<String, String> header;

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }
}
