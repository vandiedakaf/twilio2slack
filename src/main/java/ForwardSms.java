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

    @Override
    public TwilioSmsResponse handleRequest(TwilioSmsRequest input, Context context) {

        // TODO authenticate X-Twilio-Signature -- https://www.twilio.com/docs/api/security

        config = getConfig();

        sendSlackMessage(config.getProperty("slack.web_hook"), input);

        TwilioSmsResponse output = new TwilioSmsResponse();
        output.setResponse("Message Forwarded");

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

    private void sendSlackMessage(String webHook, TwilioSmsRequest input) {
        try {
            Unirest.post(webHook)
                    .body("{\"text\": \"A Twilio message for " + input.getTo() + " has been received.\"," +
                            "\"attachments\":[{\"title\":\"From\",\"text\":\"" + input.getFrom() + "\",\"color\":\"#86c53c\",\"fields\":[{\"title\":\"Message\",\"value\":\"" + input.getBody() + "\"}]}]}")
                    .asString().getBody();
        } catch (UnirestException e) {
            LOG.error("post to web hook", e);
        }
    }
}
