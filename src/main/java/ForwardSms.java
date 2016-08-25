import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by francois on 2016-08-18.
 */
public class ForwardSms implements RequestHandler<TwilioSmsRequest, TwilioSmsResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(ForwardSms.class);
    private Properties config;

    public static void main(String[] args) {
        ForwardSms forwardSms = new ForwardSms();
        Properties config = forwardSms.getConfig();
        forwardSms.sendSlackMessage(config.getProperty("slack.web_hook"), "from", "message");
    }

    @Override
    public TwilioSmsResponse handleRequest(TwilioSmsRequest input, Context context) {

        config = getConfig();

        sendSlackMessage(config.getProperty("slack.web_hook"), input.getFrom(), input.getMessage());

        TwilioSmsResponse output = new TwilioSmsResponse();
        output.setResponse("Message Received");

        return output;
    }

    private Properties getConfig() {
        LOG.info("[getConfig]");
        AmazonS3Client s3Client = new AmazonS3Client();

        S3Object s3object = s3Client.getObject(new GetObjectRequest("vdda-config", "config.properties"));
        InputStream objectData = s3object.getObjectContent();

        Properties properties = new Properties();
        try {
            properties.load(objectData);
            objectData.close();
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error("load properties", e);
        }

        return properties;
    }

    private void sendSlackMessage(String webHook, String from, String message) {
        try {
            Unirest.post(webHook)
                    .body("{\"text\": \"A Twilio message has been received.\"," +
                            "\"attachments\":[{\"title\":\"From\",\"text\":\"" + from + "\",\"color\":\"#86c53c\",\"fields\":[{\"title\":\"Message\",\"value\":\"" + message + "\"}]}]}")
                    .asString().getBody();
        } catch (UnirestException e) {
            LOG.error("post to web hook", e);
        }
    }
}
