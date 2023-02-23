/*
 * Copyright (c) 2016-2022 University of Washington
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *  *  Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *  * Neither the name of the University of Washington nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE UNIVERSITY OF WASHINGTON AND CONTRIBUTORS “AS IS” AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE UNIVERSITY OF WASHINGTON OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package edu.uw.cse.ifrcdemo.distplan.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import com.sun.javafx.webkit.WebConsoleListener;
import edu.uw.cse.ifrcdemo.distplan.data.DataInstance;
import edu.uw.cse.ifrcdemo.distplan.data.DataRepos;
import edu.uw.cse.ifrcdemo.distplan.ui.localization.LocaleUtil;
import edu.uw.cse.ifrcdemo.distplan.ui.localization.PreferencesLocaleResolver;
import edu.uw.cse.ifrcdemo.distplan.ui.xlsx.XlsxPath;
import edu.uw.cse.ifrcdemo.distplan.util.FxDialogUtil;
import edu.uw.cse.ifrcdemo.distplan.util.InternalFileStoreUtil;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.sharedlib.consts.LogConsts;
import edu.uw.cse.ifrcdemo.sharedlib.consts.SentryConsts;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.IndividualStatus;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import io.sentry.Sentry;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import netscape.javascript.JSObject;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.logging.LogFile;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.core.MethodParameter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

@SpringBootApplication(scanBasePackages = { "edu.uw.cse.ifrcdemo" })
public class SpringExperimentApplication extends Application implements WebMvcConfigurer {

  private static final String SPRING_DEVTOOLS_RESTART_ENABLED = "spring.devtools.restart.enabled";
  private static final Logger logger = LogManager.getLogger(SpringExperimentApplication.class);

  public static final int PREF_WIDTH = 1200;
  public static final int PREF_HEIGHT = 768;
  public static final String START_URL = "http://localhost:8090/login";
  public static final String APP_TITLE = "RC 2 Relief Tool";

  private static ConfigurableApplicationContext ctx;
  private WebEngine webEngine;

  @Override
  public void start(Stage stage) {
    // create the scene
    stage.setTitle(APP_TITLE);
    FxDialogUtil.setOwningWindow(stage);
    Scene scene = new Scene(new Group());

    final WebView browser = new WebView();
    webEngine = browser.getEngine();
    browser.setPrefSize(PREF_WIDTH, PREF_HEIGHT);

    ScrollPane scrollPane = new ScrollPane();

    scrollPane.setContent(browser);
    scrollPane.setFitToHeight(true);
    scrollPane.setFitToWidth(true);

    webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
      @Override
      public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue,
          Worker.State newValue) {
        if (newValue == Worker.State.SUCCEEDED) {
          JSObject window = (JSObject) webEngine.executeScript("window");
          window.setMember("javafx", new JsCallback(scene.getWindow()));

          webEngine.executeScript("window.dispatchEvent(new Event('javafxReady'))");
        }
      }
    });

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
    stage.getIcons().add(new Image("/static/img/IFRC_Logo.png"));
    stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
      @Override
      public void handle(WindowEvent event) {
        try {
          ResourceBundle translations = TranslationUtil.getTranslations();
          ButtonType result =
              FxDialogUtil.showConfirmDialogAndWait(
                  translations.getString(TranslationConsts.EXIT),
                  translations.getString(TranslationConsts.THE_APPLICATION_WILL_CLOSE),
                  translations.getString(TranslationConsts.ARE_YOU_SURE_YOU_WANT_TO_EXIT));
          if (result == ButtonType.OK) {
            logger.error("Close Request");
            ctx.stop();
            ctx.close();
            stop();
          } else {
            event.consume();
          }
        } catch (Exception e) {
          logger.catching(e);
          Sentry.capture(e);
        }
      }
    });

    webEngine.load(START_URL);
    stage.show();
  }

  public static void main(String[] args) {
    System.setProperty(LogConsts.JAVA_LOG_MANAGER_PROPERTY, LogConsts.JAVA_LOG_MANAGER_VALUE);
    System.setProperty(SPRING_DEVTOOLS_RESTART_ENABLED, Boolean.FALSE.toString());
    System.setProperty(SentryConsts.SENTRY_RELEASE_VERSION_PROPERTY,
            SentryConsts.getRc2ReleaseVersionForSentry());
    System.setProperty(SentryConsts.SENTRY_DNS_PROPERTY, SentryConsts.SENTRY_DNS_VALUE);
    Sentry.init();

    // TODO: find a better solution
    try {
      String logFile = InternalFileStoreUtil.getProjectPath().resolve("rc2.log").toString();
      System.setProperty(LogFile.FILE_PROPERTY, logFile);
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      TranslationUtil.loadTranslationsFromLocale(LocaleUtil.getCurrentLocale());

      ctx = new SpringApplicationBuilder(SpringExperimentApplication.class).headless(false)
              .run(args);

      launch(args);
    } catch (Exception throwable) {
      logger.catching(throwable);
      System.err.println("Sending to Sentry!!!");
      Sentry.capture(throwable);
    }
  }

  @Bean
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public Logger logger(InjectionPoint injectionPoint) {
    Class<?> loggingClass = SpringExperimentApplication.class;

    MethodParameter methodParameter = injectionPoint.getMethodParameter();
    if (methodParameter != null) {
      // constructor injection
      loggingClass = methodParameter.getContainingClass();
    } else {
      Field field = injectionPoint.getField();

      if (field != null) {
        // field injection
        loggingClass = field.getDeclaringClass();
      }
    }

    return LogManager.getLogger(loggingClass);
  }

  @Bean("beneficiaryStatus")
  public Map<String, IndividualStatus> statusMap() {
    return new HashMap<>();
  }

  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper;
  }

  // TODO: Figure out why this is needed
  @Bean
  public LocaleResolver localeResolver(PreferencesLocaleResolver resolver) {
    return resolver;
  }

  @Bean
  @Scope(BeanDefinition.SCOPE_PROTOTYPE) // use prototype to prevent template caching
  public MustacheFactory mustacheFactory(Logger logger) {
    return new DefaultMustacheFactory(resourceName -> {
      try {
        return Files.newBufferedReader(Paths.get(resourceName), StandardCharsets.UTF_8);
      } catch (IOException e) {
        logger.catching(Level.INFO, e);
        return null;
      }
    });
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new LocaleChangeInterceptor());
    registry.addInterceptor(new HandlerInterceptor() {
      @Override
      public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (ex != null) {
          Sentry.capture(ex);
        }
      }
    });
  }

  public class JsCallback {
    private final Window window;

    public JsCallback(Window window) {
      this.window = window;
    }

    public void openFileDialogForCsv(String handlerFnName) {
      if (handlerFnName == null) {
        return;
      }

      File selectedCsv = null;
      try {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(TranslationUtil.getTranslations().getString(TranslationConsts.CHOOSE_CSV_FILE));
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter(GenConsts.CSV_FILE_EXTENSION, GenConsts.CSV_FILTER));

        selectedCsv = chooser.showOpenDialog(window);

        if (selectedCsv != null) {
          selectedCsv.getAbsolutePath();
        }
      } finally {
        if (selectedCsv != null && selectedCsv.length() > 0) {
          String selectedCsvStr = selectedCsv.getAbsolutePath().replace("\\", "\\\\");
          webEngine.executeScript(handlerFnName + "('" + selectedCsvStr + "')");
        } else {
          webEngine.executeScript(handlerFnName + "()");
        }
      }

    }

    public XlsxPath openFileDialogForXlsx() {
      FileChooser chooser = new FileChooser();
      chooser.setTitle(TranslationUtil.getTranslations().getString(TranslationConsts.CHOOSE_XLSX_LABEL));
      chooser
          .getExtensionFilters()
          .add(new FileChooser.ExtensionFilter(GenConsts.XLSX, GenConsts.XLSX_FILTER));

      File selectedXlsx = chooser.showOpenDialog(window);

      if (selectedXlsx != null) {
        return new XlsxPath(
            selectedXlsx.getAbsolutePath(),
            selectedXlsx.getName()
        );
      }

      return null;
    }

    public String openFileDialogForReportTemplate() {
      FileChooser chooser = new FileChooser();
      chooser.setTitle("Choose a Template");
      chooser
          .getExtensionFilters()
          .add(new FileChooser.ExtensionFilter("Report Template (.mustache)", "*.mustache"));

      return chooser.showOpenDialog(window).getAbsolutePath();
    }

    public void openSimpleFileDialog(String handlerFnName) {
      if (handlerFnName == null) {
        return;
      }
      String selectedDirectoryStr = null;
      try {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(TranslationUtil.getTranslations().getString(TranslationConsts.CHOOSE_DIRECTORY));
        File selectedDirectory = chooser.showDialog(window);
        if (selectedDirectory != null) {
          selectedDirectoryStr = selectedDirectory.getAbsolutePath().replace("\\", "\\\\");
        }
      } finally {
        if (selectedDirectoryStr != null && selectedDirectoryStr.length() > 0) {
          webEngine.executeScript(handlerFnName + "('" + selectedDirectoryStr + "')");
        } else {
          webEngine.executeScript(handlerFnName + "()");
        }
      }
    }

    public void openFxErrorDialog(String errorMessage) {
      if (errorMessage == null) {
        return;
      }
      FxDialogUtil.showErrorDialog(errorMessage);
    }

    public void openFxWarnDialog(String warnMessage) {
      if (warnMessage == null) {
        return;
      }
      FxDialogUtil.showWarningDialog(warnMessage);
    }

    public void openFxInfoDialog(String infoMessage) {
      if (infoMessage == null) {
        return;
      }
      FxDialogUtil.showInfoDialog(infoMessage);
    }

    public boolean openFxConfirmDialog(String confirmMessage) throws ExecutionException, InterruptedException {
      if (confirmMessage == null) {
        return false;
      }

      ButtonType buttonType = FxDialogUtil.showConfirmDialogAndWait(
          TranslationUtil.getTranslations().getString(TranslationConsts.WARNING_DIALOG_TITLE),
          confirmMessage,
          null
      );

      return buttonType == ButtonType.OK;
    }

    public void clearWebHistory() {
      WebHistory history = webEngine.getHistory();
      history.setMaxSize(0);
      ObservableList<WebHistory.Entry> historyList = history.getEntries();
      assert(historyList.size() == 0);
      if(historyList.size() == 0) {
        history.setMaxSize(100);
      }
    }

    public String getProfileName() {
      DataRepos repos = DataInstance.getDataRepos();
      if(repos != null) {
        String profile = repos.getCurrentProfile();
        if(profile != null) {
          return profile;
        }
      }
      return "";
    }
  }
}
