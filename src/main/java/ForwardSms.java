import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.twilio.sdk.TwilioUtils;
import org.apache.commons.codec.binary.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by francois on 2016-08-18.
 */
public class ForwardSms implements RequestHandler<TwilioSmsRequest, TwilioSmsResponse> {

    private static final Logger log = LoggerFactory.getLogger(ForwardSms.class);
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

        if (!configSet()) {
            output.setResponse("<response>Config Required</response>");
            return output;
        }

        Gson gson = new GsonBuilder().create();
        log.info(gson.toJson(input));

        log.info("***** query ****");
        input.getQuerystring().forEach((k,v) -> log.info(k + ": " + v));

        if (!validateTwilioRequest(input)) {
            output.setResponse("<response>Not Authorised</response>");
            return output;
        }

        sendSlackMessage(config.getProperty("slack.web_hook"), input);
        output.setResponse("<response>Message Forwarded</response>");

        return output;
    }

    private boolean configSet() {
        if (config.getProperty("gateway.url") == null) {
            log.warn("gateway.url not found.");
            return false;
        }
        if (config.getProperty("slack.web_hook") == null) {
            log.warn("slack.web_hook not found.");
            return false;
        }
        if (config.getProperty("twilio.auth_token") == null) {
            log.warn("twilio.auth_token not found.");
            return false;
        }
        return true;
    }

    // https://www.twilio.com/docs/api/security
    private boolean validateTwilioRequest(TwilioSmsRequest input){
        TwilioUtils util = new TwilioUtils(config.getProperty("twilio.auth_token"));

        log.info(input.getHeader().get("X-Twilio-Signature"));
        log.info(config.getProperty("gateway.url"));
        attemptValidationSignature(config.getProperty("gateway.url"), input.getQuerystring());
        attemptValidationSignature(config.getProperty("gateway.url"), null);

        StringBuilder testUrl = new StringBuilder(config.getProperty("gateway.url") + "?");
        input.getQuerystring().forEach((k,v) -> testUrl.append(k + "=" + v + "&"));
        String finalUrl = testUrl.substring(0,testUrl.length()-1);
        log.info("finalUrl: " + finalUrl);
        attemptValidationSignature(finalUrl, null);

        return util.validateRequest(input.getHeader().get("X-Twilio-Signature"), config.getProperty("gateway.url"), input.getQuerystring());
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
            log.error("load properties", e);
        }

        return properties;
    }

    private String attemptValidationSignature(String url, Map<String,String> params) {
        SecretKeySpec signingKey = new SecretKeySpec(config.getProperty("twilio.auth_token").getBytes(), "HmacSHA1");

        try {
            //initialize the hash algortihm
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);

            //sort the params alphabetically, and append the key and value of each to the url
            StringBuffer data = new StringBuffer(url);
            if (params != null) {
                List<String> sortedKeys = new ArrayList<String>( params.keySet());
                Collections.sort(sortedKeys);

                for (String s: sortedKeys) {
                    data.append(s);
                    String v = "";
                    if (params.get(s) != null) {
                        v = params.get(s);
                    }
                    data.append(v);
                }
            }

            //compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(data.toString().getBytes("UTF-8"));

            //base64-encode the hmac
            String signature = new String(org.apache.commons.codec.binary.Base64.encodeBase64(rawHmac));

            log.info("signature: " + signature);

            return signature;
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (InvalidKeyException e) {
            return null;
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    private void sendSlackMessage(String webHook, TwilioSmsRequest input) {
        try {
            Unirest.post(webHook)
                    .body(String.format("{\"text\": \"A Twilio message for %s has been received.\"," +
                            "\"attachments\":[{\"title\":\"From\",\"text\":\"%s\",\"color\":\"#86c53c\",\"fields\":[{\"title\":\"Message\",\"value\":\"%s\"}]}]}", input.getQuerystring().get("To"), input.getQuerystring().get("From"), input.getQuerystring().get("Body")))
                    .asString().getBody();
        } catch (UnirestException e) {
            log.error("post to web hook", e);
        }
    }
}
