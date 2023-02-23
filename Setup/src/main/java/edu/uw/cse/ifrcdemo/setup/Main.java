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

package edu.uw.cse.ifrcdemo.setup;

import edu.uw.cse.ifrcdemo.setup.consts.ViewConsts;
import edu.uw.cse.ifrcdemo.setup.logic.ConfigUploadActionListener;
import edu.uw.cse.ifrcdemo.setup.logic.ResetActionListener;
import edu.uw.cse.ifrcdemo.setup.view.MainFrame;
import edu.uw.cse.ifrcdemo.setup.view.barcode.BarcodePanel;
import edu.uw.cse.ifrcdemo.setup.view.configuration.ConfigPresenter;
import edu.uw.cse.ifrcdemo.setup.view.configuration.ConfigView;
import edu.uw.cse.ifrcdemo.setup.view.menu.MenuPresenter;
import edu.uw.cse.ifrcdemo.setup.view.menu.MenuView;
import edu.uw.cse.ifrcdemo.sharedlib.consts.SentryConsts;
import edu.uw.cse.ifrcdemo.sharedlib.model.config.SaveDirectory;
import edu.uw.cse.ifrcdemo.sharedlib.util.ErrorUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.SwingUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.UIInitUtil;
import edu.uw.cse.ifrcdemo.sharedlib.view.BannerPanel;
import edu.uw.cse.ifrcdemo.sharedlib.view.MainPanel;
import edu.uw.cse.ifrcdemo.sharedlib.view.NavigationPresenter;
import edu.uw.cse.ifrcdemo.sharedlib.view.NavigationView;
import edu.uw.cse.ifrcdemo.sharedlib.view.SyncActionPanel;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import edu.uw.cse.ifrcdemo.xlsxconverterserver.XlsxConverterServer;
import edu.uw.cse.ifrcdemo.xlsxconverterserver.handler.FormUploadConsumer;
import edu.uw.cse.ifrcdemo.xlsxconverterserver.handler.MappingFormUploadConsumer;
import io.sentry.Sentry;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;


public class Main {

  private static final int PANEL_WIDTH = ViewConsts.WINDOW_WIDTH;
  private static final int PANEL_HEIGHT = ViewConsts.WINDOW_HEIGHT;

  public static void main(String[] args) {
    TranslationUtil.loadTranslationsFromArgs(args);

    System.setProperty(SentryConsts.SENTRY_RELEASE_VERSION_PROPERTY, SentryConsts.getRc2ReleaseVersionForSentry());
    System.setProperty(SentryConsts.SENTRY_DNS_PROPERTY, SentryConsts.SENTRY_DNS_VALUE);
    Sentry.init();

    SwingUtil.setSystemLookAndFeel();

    MappingFormUploadConsumer formUploadHandler = new MappingFormUploadConsumer();

    CompletableFuture
        .runAsync(new InitRunnable(formUploadHandler), SwingUtil::invokeOnEdt)
        .thenRunAsync(new XlsxServerRunnable(ErrorUtil::handleFatalError, formUploadHandler))
        .exceptionally(ErrorUtil::handleFatalError);
  }

  private static class InitRunnable implements Runnable {
    private final MappingFormUploadConsumer formUploadConsumer;

    private InitRunnable(MappingFormUploadConsumer formUploadConsumer) {
      this.formUploadConsumer = formUploadConsumer;
    }

    @Override
    public void run() {
      ResourceBundle translations = TranslationUtil.getTranslations();

      UIInitUtil.configureDefaultFonts();
      JFrame frame = new MainFrame();

      MainPanel mainPanel = new MainPanel(frame.getWidth(), frame.getHeight() - (frame.getHeight() / 5));

      BannerPanel banner = new BannerPanel(frame.getWidth(), frame.getHeight() / 5);
      MenuView menuView = new MenuView();
      NavigationView navigationView = new NavigationView();
      ConfigView configView = new ConfigView();
      SaveDirectory saveDirectory;
      try {
        saveDirectory = new SaveDirectory();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      BarcodePanel barcodePanel = new BarcodePanel();
      SyncActionPanel resetPanel = new SyncActionPanel(translations.getString(TranslationConsts.RESET_SRV_BTN_LABEL), false, null, false);
      resetPanel.setSyncAction(new ResetActionListener(resetPanel));
      SyncActionPanel uploadConfigurationPanel = new SyncActionPanel(translations.getString(TranslationConsts.UPLOAD_SRV_BTN_LABEL),false, null, false);
      uploadConfigurationPanel.setSyncAction(new ConfigUploadActionListener(uploadConfigurationPanel, saveDirectory));

      NavigationPresenter navigationPresenter = new NavigationPresenter(mainPanel, menuView.getPanel(), navigationView, ViewConsts.CARD_MENU);
      MenuPresenter menuPresenter = new MenuPresenter(mainPanel, menuView);
      ConfigPresenter configPresenter = new ConfigPresenter(mainPanel, configView, saveDirectory, formUploadConsumer);

      banner.addControls(navigationView);

      mainPanel.add(menuView.getPanel(), ViewConsts.CARD_MENU);
      mainPanel.add(configView, ViewConsts.CARD_CONFIG);
      mainPanel.add(barcodePanel, ViewConsts.CARD_BARCODE);
      mainPanel.add(resetPanel, ViewConsts.CARD_RESET);
      mainPanel.add(uploadConfigurationPanel, ViewConsts.CARD_UPLOAD_CONFIG);


      mainPanel.getLayoutMngr().show(mainPanel, ViewConsts.CARD_MENU);



      frame.add(banner, BorderLayout.NORTH);
      frame.add(mainPanel, BorderLayout.CENTER);
      frame.pack();
      frame.setVisible(true);
    }
  }

  private static class XlsxServerRunnable implements Runnable {
    private final Consumer<Exception> initExceptionHandler;
    private final FormUploadConsumer formUploadConsumer;

    private XlsxServerRunnable(Consumer<Exception> initExceptionHandler,
                               FormUploadConsumer formUploadConsumer) {
      this.initExceptionHandler = initExceptionHandler;
      this.formUploadConsumer = formUploadConsumer;
    }

    @Override
    public void run() {
      XlsxConverterServer instance = XlsxConverterServer.getInstance();
      instance.init(initExceptionHandler);

      instance.configureXlsxGet();
      instance.configureXlsxPost(formUploadConsumer);
      instance.configureXlsxPostError();
    }
  }
}
