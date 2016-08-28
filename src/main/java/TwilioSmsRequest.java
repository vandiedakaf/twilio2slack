/**
 * Created by francois on 2016-08-23.
 */
public class TwilioSmsRequest {
    private String Body;
    private String From;

    public String getBody() {
        return Body;
    }

    public void setBody(String Body) {
        this.Body = Body;
    }

    public String getFrom() {
        return From;
    }

    public void setFrom(String From) {
        this.From = From;
    }

    public String getTo() {
        return To;
    }

    public void setTo(String To) {
        this.To = To;
    }

    private String To;


}
