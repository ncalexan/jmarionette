/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openqa.selenium.marionette;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.IOException;

public class MarionetteDriver extends RemoteWebDriver {
    public MarionetteDriver(MarionetteClient client, DesiredCapabilities desiredCapabilities, DesiredCapabilities requiredCapabilities) {
        super(new MarionetteCommandExecutor(client), desiredCapabilities, requiredCapabilities);
    }

    @Override
    protected void startClient() {
        try {
            getClient().connect();
        } catch (IOException e) {
            // TODO: do better.
            e.printStackTrace();
        }
    }

    public MarionetteClient getClient() {
        return ((MarionetteCommandExecutor) this.getCommandExecutor()).getClient();
    }
}
