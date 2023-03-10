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

package edu.uw.cse.ifrcdemo.distreport.logic;

import edu.uw.cse.ifrcdemo.sharedlib.consts.ToolPropertiesConsts;
import edu.uw.cse.ifrcdemo.sharedlib.logic.DownloadAndValidateTask;
import edu.uw.cse.ifrcdemo.sharedlib.model.config.SaveDirectory;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.Module;
import edu.uw.cse.ifrcdemo.sharedlib.util.DialogUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.ErrorUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.SwingUtil;
import edu.uw.cse.ifrcdemo.sharedlib.view.IOPanel;
import edu.uw.cse.ifrcdemo.sharedlib.view.SyncActionListener;
import edu.uw.cse.ifrcdemo.sharedlib.view.SyncActionPanel;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opendatakit.suitcase.model.CloudEndpointInfo;
import org.opendatakit.suitcase.net.SuitcaseSwingWorker;
import org.opendatakit.suitcase.utils.FieldsValidatorUtils;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class DownloadReportActionListener extends SyncActionListener {

    private final Logger logger;

    public DownloadReportActionListener(SyncActionPanel panel) {
        super(panel);
        this.logger = LogManager.getLogger(DownloadReportActionListener.class);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ResourceBundle translations = TranslationUtil.getTranslations();
        sanitizeFields();

        IOPanel ioPanel = getPanel().getIOPanel();

        ioPanel.getPreferences().put(ToolPropertiesConsts.KEY_SERVER_URL, ioPanel.getCloudEndpointAddressText().getText());
        ioPanel.getPreferences().put(ToolPropertiesConsts.KEY_USERNAME, ioPanel.getUserNameText().getText());

        String error = FieldsValidatorUtils.checkLoginFields(
                ioPanel.getCloudEndpointAddressText().getText(), ioPanel.getAppIdText().getText(), ioPanel.getUserNameText().getText(),
                String.valueOf(ioPanel.getPasswordText().getPassword()), false
        );

        if (error != null) {
            DialogUtil.showErrorDialog(error);
            return;
        }

        String originalActionLabel = ioPanel.getActionButton().getText();
        ioPanel.getActionButton().setEnabled(false);
        ioPanel.getActionButton().setText(translations.getString(TranslationConsts.LOADING_LABEL));
        getPanel().getProgressBar().setVisible(true);

        String savePath = ioPanel.getPathChooserPanel().getPath();

        CloudEndpointInfo cloudEndpointInfo = null;
        try {
            cloudEndpointInfo = new CloudEndpointInfo(
                ioPanel.getCloudEndpointAddressText().getText(), ioPanel.getAppIdText().getText(), ioPanel.getUserNameText().getText(),
                String.valueOf(ioPanel.getPasswordText().getPassword()));
        } catch (MalformedURLException e1) {
            logger.catching(e1);
            DialogUtil.showErrorDialog(translations.getString(TranslationConsts.BAD_URL_ERROR));

            ioPanel.getActionButton().setEnabled(true);

            return;
        }

        SaveDirectory saveDirectory = new SaveDirectory(Paths.get(savePath));
        DownloadAndValidateTask downloader = new DownloadAndValidateTask(cloudEndpointInfo, saveDirectory, Module.RELIEF);
        downloader.addPropertyChangeListener(getPanel().getProgressBar());

        CompletableFuture
            .supplyAsync(downloader::blockingExecute)
            .thenAcceptAsync(status -> {
                if (status != SuitcaseSwingWorker.okCode) {
                    throw new IllegalStateException(status.toString());
                }

                getPanel().getProgressBar().setString(translations.getString(TranslationConsts.REPORT_GEN_MSG));
            }, SwingUtil::invokeOnEdt)
            .thenRunAsync(() -> ReportGenerator.generateReport(
                saveDirectory.getCurrentSaveDirectory(),
                Paths.get(ioPanel.getPathChooserPanel().getPath()),
                getPanel()
            ))
            .thenRunAsync(() -> {
                getPanel().getProgressBar().setString(translations.getString(TranslationConsts.DONE_LABEL));

                ioPanel.getActionButton().setEnabled(true);
                ioPanel.getActionButton().setText(originalActionLabel);
            }, SwingUtil::invokeOnEdt)
            .exceptionally(throwable -> {
                ioPanel.getActionButton().setEnabled(true);
                return ErrorUtil.handleException(throwable);
            });
    }
}
