import com.amazonaws.services.lambda.runtime.Context;

/**
 * Created by francois on 2016-08-18.
 */
public class ForwardSms {

    public String processSms(String value, Context context) {
        return String.valueOf(value);
    }
}
