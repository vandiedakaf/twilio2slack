import com.amazonaws.services.lambda.runtime.Context;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by francois on 2016-08-18.
 */
public class ForwardSms {

    private static final Logger LOG = LoggerFactory.getLogger(ForwardSms.class);
    private static final String WEB_HOOK = "https://127.0.0.1"; // TODO

    public String processSms(String value, Context context) {
        final String from = "1234567890";
        final String message = "Use the pin 123456 to verify your account";

        final Properties config = loadProperties();

        System.out.println(config.getProperty("slack.web_hook"));
//        sendSlackMessage(message, from);

        return String.valueOf(value);
    }

    private Properties loadProperties() {
        InputStream is = ForwardSms.class.getResourceAsStream("config.properties");
        Properties properties = new Properties();
        try {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    private void sendSlackMessage(String message, String from) {
        String webHookResponse;
        try {
            webHookResponse = Unirest.post(WEB_HOOK)
                    .body("{\"text\": \"A Twilio message has been delivered.\"," +
                            "\"attachments\":[{\"title\":\"Message\",\"text\":\"" + message + "\",\"color\":\"#86c53c\",\"fields\":[{\"title\":\"From\",\"value\":\"" + from + "\"}]}]}")
                    .asString().getBody();
        } catch (UnirestException ex) {
            LOG.warn("post to web hook", ex);
        }
    }

    public static void main(String[] args){
        ForwardSms forwardSms = new ForwardSms();
        final Properties config = forwardSms.loadProperties();
        System.out.println(config.getProperty("slack.web_hook"));
    }
}
