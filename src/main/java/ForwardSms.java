import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.twilio.sdk.TwilioUtils;
import org.apache.commons.logging.Log;
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
    private static final String CONFIG_BUCKET;

    static {
        Properties properties = new Properties();

        try (InputStream is = ForwardSms.class.getResourceAsStream("config.properties")) {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        CONFIG_BUCKET = properties.getProperty("config.bucket");
    }

    @Override
    public TwilioSmsResponse handleRequest(TwilioSmsRequest input, Context context) {

        TwilioSmsResponse output = new TwilioSmsResponse();

        config = getS3Config();

        if (!validateTwilioRequest(input)) {
            output.setResponse("Not authorised");
            return output;
        }

        sendSlackMessage(config.getProperty("slack.web_hook"), input);
        output.setResponse("Message Forwarded");

        return output;
    }

    // https://www.twilio.com/docs/api/security
    private boolean validateTwilioRequest(TwilioSmsRequest input){
        TwilioUtils util = new TwilioUtils(config.getProperty("twilio.auth_token"));

        LOG.info("Parameters " + input.getParams());
//        return util.validateRequest(input.getSignature(), config.getProperty("gateway.url"), params);
        return true;
    }

    private Properties getS3Config() {
        Properties properties = new Properties();

        AmazonS3Client s3Client = new AmazonS3Client();
        S3Object s3object = s3Client.getObject(new GetObjectRequest(CONFIG_BUCKET, "config.properties"));

        try (InputStream objectData = s3object.getObjectContent()) {
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
                    .body(String.format("{\"text\": \"A Twilio message for %s has been received.\"," +
                            "\"attachments\":[{\"title\":\"From\",\"text\":\"%s\",\"color\":\"#86c53c\",\"fields\":[{\"title\":\"Message\",\"value\":\"%s\"}]}]}", input.getTo(), input.getFrom(), input.getBody()))
                    .asString().getBody();
        } catch (UnirestException e) {
            LOG.error("post to web hook", e);
        }
    }
}
