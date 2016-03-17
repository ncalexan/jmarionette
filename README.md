# jmarionette

jmarionette is a WebDriver implementation that expects to talk to a
[Firefox Marionette](https://developer.mozilla.org/en-US/docs/Mozilla/QA/Marionette)
server.  It implements some portion of the
[Marionette socket protocol](https://developer.mozilla.org/en-US/docs/Mozilla/QA/Marionette/Protocol)
directly.

**jmarionette is a proof of concept (at best)!**

It is a rough replacement for running the Selenium RemoteWebDriver against a
[wires](https://github.com/jgraham/wires) server mapping the WebDriver HTTP REST
API to the Marionette socket protocol.  Running `wires` on Android test devices
to talk to Marionette inside an active Fennec instance is non-trivial.

## Code layout

The `lib` folder includes a tiny Marionette socket client and the
MarionetteDriver implementation.  You'll need to run a Firefox with Marionette
enabled and listening on port 2828 to run the included *test* suite.

The `app` folder defines a test Android App.  It uses the `lib` project in its
on-device *androidTest* suite.  You'll need to start a Fennec with Marionette
enabled and listening on port 2828 to run the included *androidTest* suite.
