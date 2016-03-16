import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.startsWith;

public class MarionetteClientTest {
    private MarionetteClient client;

    @Before
    public void before() throws IOException {
        client = new MarionetteClient();
        client.connect();
    }

    @Test
    public void testSendCommand() throws Exception {
        final JSONObject parameters = new JSONObject();
        parameters.put("capabilities", new JSONObject());
        final JSONArray response1 = client.sendCommand("newSession", parameters);
        Assert.assertThat(response1.toString(), startsWith("[1,0,null,"));

        final JSONArray response2 = client.sendCommand("newSession", parameters);
        Assert.assertThat(response2.toString(), startsWith("[1,1,{"));
    }
}
