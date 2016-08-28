/**
 * Created by francois on 2016-08-23.
 */
public class TwilioSmsRequest {
    private String Body;
    private String From;
    private String To;

    public String getBody() {
        return Body;
    }

    public void setBdy(String body) {
        Body = body;
    }

    public String getFrom() {
        return From;
    }

    public void setFrom(String from) {
        From = from;
    }

    public String getTo() {
        return To;
    }

    public void setTo(String to) {
        To = to;
    }
}
