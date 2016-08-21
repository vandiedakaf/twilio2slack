import com.amazonaws.services.lambda.runtime.Context;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by francois on 2016-08-18.
 */
public class ForwardSms {

    private static final Logger LOG = LoggerFactory.getLogger(ForwardSms.class);
    private static final String WEB_HOOK = "https://127.0.0.1"; // TODO

    public String processSms(String value, Context context) {
        final String from = "1234567890";
        final String message = "Use the pin 123456 to verify your account";
        sendSlackMessage(message, from);

        return String.valueOf(value);
    }

    private void sendSlackMessage(String message, String from){
        String webHookResponse;
        try {
            webHookResponse = Unirest.post(WEB_HOOK)
                    .body("{\"text\": \"A Twilio message has been delivered.\"," +
                            "\"attachments\":[{\"title\":\"Message\",\"text\":\""+message+"\",\"color\":\"#86c53c\",\"fields\":[{\"title\":\"From\",\"value\":\""+from+"\"}]}]}")
                    .asString().getBody();
        } catch (UnirestException ex) {
            LOG.warn("post to web hook", ex);
        }
    }
}
