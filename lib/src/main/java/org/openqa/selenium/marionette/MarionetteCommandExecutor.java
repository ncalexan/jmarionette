/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openqa.selenium.marionette;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.openqa.selenium.remote.BeanToJsonConverter;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.ErrorCodes;
import org.openqa.selenium.remote.JsonToBeanConverter;
import org.openqa.selenium.remote.Response;
import org.openqa.selenium.remote.SessionId;

import java.io.IOException;
import java.util.HashMap;

public class MarionetteCommandExecutor implements CommandExecutor {
    private final ErrorCodes errorCodes = new ErrorCodes();
    private final BeanToJsonConverter beanToJsonConverter = new BeanToJsonConverter();
    private final JsonToBeanConverter jsonToBeanConverter = new JsonToBeanConverter();

    final MarionetteClient client;

    public MarionetteCommandExecutor(MarionetteClient client) {
        this.client = client;
    }

    public MarionetteClient getClient() {
        return client;
    }

    @Override
    public Response execute(Command command) throws IOException {
        final JsonObject parametersJson = new JsonParser().parse(beanToJsonConverter.convert(command.getParameters())).getAsJsonObject();
        final SessionId sessionId = command.getSessionId();
        if (sessionId != null) {
            parametersJson.addProperty("sessionId", sessionId.toString());
        }

        // Ad-hoc mapping from Selenium/RemoteWebDriver commands to Marionette commands.
        if (DriverCommand.NEW_SESSION.equals(command.getName())) {
            parametersJson.remove("desiredCapabilities");
            JsonElement requiredCapabilities = parametersJson.remove("requiredCapabilities");
            if (requiredCapabilities == null) {
                requiredCapabilities = new JsonObject();
            }
            parametersJson.add("capabilities", requiredCapabilities);
        }

        final JsonArray responseArray = client.sendCommand(command.getName(), parametersJson);

        final Response response = new Response(sessionId);

        if (!responseArray.get(2).isJsonNull()) {
            final JsonObject error = responseArray.get(2).getAsJsonObject();
            // [1,1,{"message":"Session already running","error":"webdriver error","stacktrace":null},null]
            response.setStatus(ErrorCodes.toStatus(error.get("error").getAsString()));
            response.setState(error.get("message").getAsString());
            response.setValue(jsonToBeanConverter.convert(HashMap.class, error));
            return response;
        }

        response.setStatus(ErrorCodes.SUCCESS);
        response.setState(errorCodes.toState(ErrorCodes.SUCCESS));
        final JsonObject result = responseArray.get(3).getAsJsonObject();
        // [1,0,null,{"sessionId":"702c8160-ba6d-514c-97e1-fc7de86bd251","capabilities":{"specificationLevel":0,"platform":"DARWIN","acceptSslCerts":false,"browserVersion":"48.0a1","browserName":"Firefox","XULappId":"{ec8030f7-c20a-464f-9b0e-13a3a9e97384}","raisesAccessibilityExceptions":false,"rotatable":false,"appBuildId":"20160316030233","takesElementScreenshot":true,"version":"48.0a1","platformVersion":"15.3.0","platformName":"Darwin","proxy":{},"device":"desktop","takesScreenshot":true}}]
        // Ad-hoc approach here: result is always a JSON object; the "value" key (if present) is an arbitrary JSON value.
        // We should always be able to convert the JSON object to a Map, and then extract the value key as a POJO.
        // We return "value" if that key exists, otherwise the whole result object.
        final HashMap map = jsonToBeanConverter.convert(HashMap.class, result.toString());
        final Object value = map.get("value");
        if (value != null) {
            response.setValue(value);
        } else {
            response.setValue(map);
        }

        return response;
    }
}
