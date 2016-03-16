import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
        final JsonObject parameters = new JsonObject();
        parameters.add("capabilities", new JsonObject());
        final JsonArray response1 = client.sendCommand("newSession", parameters);
        Assert.assertThat(response1.toString(), startsWith("[1,0,null,"));

        final JsonArray response2 = client.sendCommand("newSession", parameters);
        Assert.assertThat(response2.toString(), startsWith("[1,1,{"));
    }
}
