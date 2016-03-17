/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class MarionetteClient {
    private static final int MARIONETTE_PROTOCOL_NUMBER = 3;

    private static final int BUFFER_LENGTH = 4096;

    protected final AtomicInteger id = new AtomicInteger(0);

    protected final String host = "localhost";
    protected final int port = 2828;

    // TODO: synchronize all access.
    protected Socket socket;
    protected BufferedReader reader;
    protected OutputStreamWriter writer;

    protected boolean verbose = false;

    public MarionetteClient() {
    }

    public void connect() throws IOException {
        if (socket != null) {
            throw new IllegalStateException("already connected");
        }
        socket = new Socket(host, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        writer = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");

        final JsonObject handshakeJSON = new JsonParser().parse(readResponse()).getAsJsonObject();
        final int marionetteProtocol = handshakeJSON.get("marionetteProtocol").getAsInt();
        if (MARIONETTE_PROTOCOL_NUMBER != marionetteProtocol) {
            throw new IllegalStateException("bad marionetteProtocol in handshake, expected: " + MARIONETTE_PROTOCOL_NUMBER + ", got: " + marionetteProtocol);
        }
    }

    public void disconnect() throws IOException {
        if (socket == null) {
            throw new IllegalStateException("not connected");
        }
        try {
            socket.close();
        } finally {
            socket = null;
            reader = null;
            writer = null;
        }
    }

    public String readResponse() throws IOException {
        char[] buffer = new char[BUFFER_LENGTH];
        int numRead;

        int messageLength = -1;
        while ((numRead = reader.read(buffer, 0, 1)) > -1) {
            if (numRead < 1) {
                continue;
            }
            char digit = buffer[0];
            if (digit == ':') {
                break;
            }
            if ('0' <= digit && digit <= '9') {
                if (messageLength == -1) {
                    messageLength = 0;
                }
                messageLength = (10 * messageLength) + (digit - '0');
            } else {
                throw new RuntimeException("bad message length character: " + digit);
            }
        }
        if (messageLength < 1) {
            throw new RuntimeException("bad message length: " + messageLength);
        }

        // TODO: bound message length.
        char[] message = new char[messageLength];

        int remaining = messageLength;
        while (remaining > 0 && (numRead = reader.read(buffer, 0, remaining)) > -1) {
            if (numRead < 1) {
                continue;
            }

            System.arraycopy(buffer, 0, message, messageLength - remaining, numRead);
            remaining -= numRead;
        }

        return new String(message);
    }

    public JsonArray sendCommand(String command, JsonObject parameters) throws IOException {
        final int msgId = id.getAndIncrement();

        // TODO: check connection status.
        // [type, msgid, command, parameters]
        final JsonArray commandArray = new JsonArray();
        commandArray.add(0);
        commandArray.add(msgId);
        commandArray.add(command);
        commandArray.add(parameters);

        final String serialization = commandArray.toString();
        writer.write(Integer.toString(serialization.length()));
        writer.write(":");
        writer.write(serialization);
        writer.flush();

        if (verbose) {
            System.out.print(" -> ");
            System.out.print(Integer.toString(serialization.length()));
            System.out.print(":");
            System.out.println(serialization);
        }

        // [type, msgid, error, result]
        final String responseString = readResponse();

        if (verbose) {
            System.out.print(" <- ");
            System.out.println(responseString);
        }

        final JsonArray responseArray = new JsonParser().parse(responseString).getAsJsonArray();
        if (1 != responseArray.get(0).getAsInt()) {
            // TODO: handle (or drop) incoming commands.
            throw new IllegalStateException("got command when expecting response");
        }
        if (msgId != responseArray.get(1).getAsInt()) {
            throw new IllegalStateException("got response for wrong msgId, expected: " + msgId + ", got: " + responseArray.get(1));
        }
        return responseArray;
    }
}
