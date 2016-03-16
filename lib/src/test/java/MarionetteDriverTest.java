import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import static org.hamcrest.CoreMatchers.startsWith;

public class MarionetteDriverTest {

    private MarionetteClient client;
    private MarionetteDriver driver;

    @Before
    public void setUp() throws Exception {
        client = new MarionetteClient();
        driver = new MarionetteDriver(client, DesiredCapabilities.firefox(), null);
    }

    @Test
    public void testGetSessionId() {
        Assert.assertNotNull(driver.getSessionId());
    }

    @Test
    public void test() {
        Assert.assertNotNull(driver.getWindowHandle());
    }

    @Test
    public void testGet() {
        driver.get("https://mozilla.org");
        Assert.assertThat(driver.getCurrentUrl(), startsWith("https://www.mozilla.org"));
    }

    @Test
    public void testBackForward() {
        driver.navigate().to("https://mozilla.org");
        Assert.assertThat(driver.getCurrentUrl(), startsWith("https://www.mozilla.org"));
        driver.navigate().to("https://google.com");
        Assert.assertThat(driver.getCurrentUrl(), startsWith("https://www.google"));
        driver.navigate().back();
        Assert.assertThat(driver.getCurrentUrl(), startsWith("https://www.mozilla.org"));
        driver.navigate().forward();
        Assert.assertThat(driver.getCurrentUrl(), startsWith("https://www.google"));
    }

    @Test
    public void testX() {
        driver.navigate().to("https://mozilla.org");
        final WebElement element = driver.findElement(By.id("home"));
        Assert.assertNotNull(element);
        Assert.assertEquals("body", element.getTagName());
    }
}
