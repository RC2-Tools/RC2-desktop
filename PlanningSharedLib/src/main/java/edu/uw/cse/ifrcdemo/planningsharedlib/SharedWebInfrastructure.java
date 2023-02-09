/*
 * Copyright (c) 2016-2022 University of Washington
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *  Neither the name of the University of Washington nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE UNIVERSITY OF WASHINGTON AND CONTRIBUTORS “AS IS” AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE UNIVERSITY OF WASHINGTON OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package edu.uw.cse.ifrcdemo.planningsharedlib;

import com.sun.javafx.webkit.WebConsoleListener;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.FxDialogUtil;
import io.sentry.Sentry;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SharedWebInfrastructure {

    private static final Logger logger = LogManager.getLogger(SharedWebInfrastructure.class);

    private final WebView browser;
    private final WebEngine webEngine;
    private final Scene scene;
    private final Stage stage;

    public SharedWebInfrastructure(Stage stage) {
        this.stage = stage;
        this.browser = new WebView();
        this.webEngine = browser.getEngine();
        this.scene = new Scene(new Group());

        FxDialogUtil.setOwningWindow(stage);

        browser.setPrefSize(BaseAppSystem.PREF_WIDTH, BaseAppSystem.PREF_HEIGHT);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(browser);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);

        webEngine.setOnError(evt -> {
            logger.error(evt.getException());
            Sentry.capture(evt.getException());
        });

        WebConsoleListener.setDefaultListener((webView, message, lineNumber, sourceId) -> {
            Level level = Level.INFO;
            // there has to be a better way to do this
            if (message.startsWith("Error:")) {
                level = Level.ERROR;
            }

            logger.log(level, "source: {} line: {} message: {}", sourceId, lineNumber, message);
        });
        scene.setRoot(scrollPane);
        stage.setScene(scene);

    }

    public Scene getScene() {
        return scene;
    }

    public WebEngine getWebEngine() {
        return webEngine;
    }

    public void addListenerToWebEngine(ChangeListener<Worker.State> listener) {
        webEngine.getLoadWorker().stateProperty().addListener(listener);
    }

    public void setCloseHandler(EventHandler<WindowEvent> handler) {
        stage.setOnCloseRequest(handler);
    }

    public void loadUrl(String url) {
        webEngine.load(url);
    }

}
