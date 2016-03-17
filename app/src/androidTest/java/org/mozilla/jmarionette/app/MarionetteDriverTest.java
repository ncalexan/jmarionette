/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.jmarionette.app;

import android.test.InstrumentationTestCase;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.marionette.MarionetteClient;
import org.openqa.selenium.marionette.MarionetteDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import static org.hamcrest.CoreMatchers.startsWith;

public class MarionetteDriverTest extends InstrumentationTestCase {
    protected MarionetteClient client;
    protected MarionetteDriver driver;

    @Before
    public void setUp() throws Exception {
        client = new MarionetteClient();
        client.verbose = true;
        driver = new MarionetteDriver(client, DesiredCapabilities.firefox(), null);
    }

    @Test
    public void testGetSessionId() {
        org.junit.Assert.assertNotNull(driver.getSessionId());
    }

    @Test
    public void testGetWindowHandle() {
        org.junit.Assert.assertNotNull(driver.getWindowHandle());
    }

    @Test
    public void testGet() {
        driver.get("https://mozilla.org");
        org.junit.Assert.assertThat(driver.getCurrentUrl(), startsWith("https://www.mozilla.org"));
    }

    @Test
    public void testBackForward() {
        driver.navigate().to("https://mozilla.org");
        org.junit.Assert.assertThat(driver.getCurrentUrl(), startsWith("https://www.mozilla.org"));
        driver.navigate().to("https://google.com");
        org.junit.Assert.assertThat(driver.getCurrentUrl(), startsWith("https://www.google"));
        driver.navigate().back();
        org.junit.Assert.assertThat(driver.getCurrentUrl(), startsWith("https://www.mozilla.org"));
        driver.navigate().forward();
        org.junit.Assert.assertThat(driver.getCurrentUrl(), startsWith("https://www.google"));
    }

    @Test
    public void testFindElement() {
        driver.navigate().to("https://mozilla.org");
        final WebElement element = driver.findElement(By.id("home"));
        org.junit.Assert.assertNotNull(element);
        org.junit.Assert.assertEquals("body", element.getTagName());
    }
}
