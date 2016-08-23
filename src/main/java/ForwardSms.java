import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Created by francois on 2016-08-18.
 */
public class ForwardSms {

    private static final Logger LOG = LoggerFactory.getLogger(ForwardSms.class);
    private static final String WEB_HOOK = "https://127.0.0.1"; // TODO

    public static void main(String[] args) {
        ForwardSms forwardSms = new ForwardSms();
        Properties config = forwardSms.getConfig();

        System.out.println(config.getProperty("slack.web_hook"));
    }

    public String processSms(String value, Context context) {
        final String from = "1234567890";
        final String message = "Use the pin 123456 to verify your account";

        Properties config = getConfig();

        System.out.println(config.getProperty("slack.web_hook"));
//        sendSlackMessage(message, from);

        return String.valueOf(value);
    }

    private Properties getConfig() {
        System.out.println("[getConfig]");
        AmazonS3Client s3Client = new AmazonS3Client();

        S3Object s3object = s3Client.getObject(new GetObjectRequest("vdda-config", "config.properties"));
        InputStream objectData = s3object.getObjectContent();

        Properties properties = new Properties();
        try {
            properties.load(objectData);
            objectData.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties;
    }

    private void sendSlackMessage(String message, String from) {
        try {
            Unirest.post(WEB_HOOK)
                    .body("{\"text\": \"A Twilio message has been received.\"," +
                            "\"attachments\":[{\"title\":\"Message\",\"text\":\"" + message + "\",\"color\":\"#86c53c\",\"fields\":[{\"title\":\"From\",\"value\":\"" + from + "\"}]}]}")
                    .asString().getBody();
        } catch (UnirestException ex) {
            LOG.warn("post to web hook", ex);
        }
    }
}
