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

package edu.uw.cse.ifrcdemo.setup.logic;

import edu.uw.cse.ifrcdemo.sharedlib.consts.ToolPropertiesConsts;
import edu.uw.cse.ifrcdemo.sharedlib.logic.LoginTask;
import edu.uw.cse.ifrcdemo.sharedlib.logic.ResetTask;
import edu.uw.cse.ifrcdemo.sharedlib.util.DialogUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.ErrorUtil;
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

import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class ResetActionListener extends SyncActionListener {

    private final Logger logger;

    public ResetActionListener(SyncActionPanel panel) {
        super(panel);
        this.logger = LogManager.getLogger(ResetActionListener.class);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        ResourceBundle translations =TranslationUtil.getTranslations();
        int result = DialogUtil.showWarningDialog(translations.getString(TranslationConsts.PERMANENTLY_DELETE_SERVER_WARNING));
        if (result != JOptionPane.YES_OPTION ) {
            return;
        }

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

        CloudEndpointInfo cloudEndpointInfo;
        try {
            cloudEndpointInfo = new CloudEndpointInfo(
                ioPanel.getCloudEndpointAddressText().getText(), ioPanel.getAppIdText().getText(), ioPanel.getUserNameText().getText(),
                String.valueOf(ioPanel.getPasswordText().getPassword()));
        } catch (MalformedURLException e1) {
            logger.catching(e1);
            DialogUtil.showErrorDialog(translations.getString(TranslationConsts.BAD_URL_ERROR));

            return;
        }
        String originalActionLabel = ioPanel.getActionButton().getText();
        ioPanel.getActionButton().setEnabled(false);
        ioPanel.getActionButton().setText(translations.getString(TranslationConsts.LOADING_LABEL));
        getPanel().getProgressBar().setVisible(true);

        LoginTask loginWorker = new LoginTask(cloudEndpointInfo, true);
        loginWorker.addPropertyChangeListener(getPanel().getProgressBar());

        CompletableFuture
            .supplyAsync(loginWorker::blockingExecute)
            .thenComposeAsync(status -> {
                if (status != SuitcaseSwingWorker.okCode) {
                    throw new IllegalStateException(status.toString());
                }

                ResetTask worker = new ResetTask(true);
                worker.addPropertyChangeListener(getPanel().getProgressBar());
                worker.addPropertyChangeListener(ioPanel);

                return CompletableFuture.supplyAsync(worker::blockingExecute);
            })
            .exceptionally(throwable -> {
                ioPanel.getActionButton().setEnabled(true);
                ioPanel.getActionButton().setText(originalActionLabel);

                return ErrorUtil.logException(throwable);
            });
    }
}
