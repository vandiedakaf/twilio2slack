import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

/**
 * Created by francois on 2016-09-02.
 */
public class TwilioSmsResponseTest {
    private final String expected = "Some Response";

    // http://stackoverflow.com/questions/21354311/junit-test-of-setters-and-getters-of-instance-variables
    @Test
    public void testSetter_setsProperly() throws NoSuchFieldException, IllegalAccessException {
        //given
        final TwilioSmsResponse pojo = new TwilioSmsResponse();

        //when
        pojo.setResponse(expected);

        //then
        final Field field = pojo.getClass().getDeclaredField("response");
        field.setAccessible(true);
        assertEquals("Fields didn't match", field.get(pojo), expected);
    }

    @Test
    public void testGetter_getsValue() throws NoSuchFieldException, IllegalAccessException {
        //given
        final TwilioSmsResponse pojo = new TwilioSmsResponse();
        final Field field = pojo.getClass().getDeclaredField("response");
        field.setAccessible(true);
        field.set(pojo, expected);

        //when
        final String result = pojo.getResponse();

        //then
        assertEquals("Field wasn't retrieved properly", result, expected);
    }

}
