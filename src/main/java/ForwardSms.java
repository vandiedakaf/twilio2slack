import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Properties;

/**
 * Created by francois on 2016-08-18.
 */
public class ForwardSms {

    private static final Logger LOG = LoggerFactory.getLogger(ForwardSms.class);
    private static final String WEB_HOOK = "https://127.0.0.1"; // TODO

    public static void main(String[] args) {
        ForwardSms forwardSms = new ForwardSms();
        File configFile = forwardSms.getConfig();
        final Properties config = forwardSms.loadProperties(configFile);
        System.out.println(config.getProperty("slack.web_hook"));
    }

    public String processSms(String value, Context context) {
        final String from = "1234567890";
        final String message = "Use the pin 123456 to verify your account";

        final File configFile = getConfig();

        final Properties config = loadProperties(configFile);

        System.out.println(config.getProperty("slack.web_hook"));
//        sendSlackMessage(message, from);

        return String.valueOf(value);
    }

    private File getConfig() {
        System.out.println("getConfig...");
        AmazonS3Client s3Client = new AmazonS3Client();

        System.out.println("get object...");
        File localFile = new File("config2.properties");
        s3Client.getObject(new GetObjectRequest("vdda-config", "config.properties"), localFile);

        System.out.println("trying to read file...");
        System.out.println(localFile.exists() && localFile.canRead());

        return localFile;
    }

    private Properties loadProperties(File file) {
        FileInputStream fileInput = null;
        try {
            fileInput = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Properties properties = new Properties();
        try {
            properties.load(fileInput);
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
