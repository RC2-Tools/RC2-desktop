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

package edu.uw.cse.ifrcdemo.sharedlib.logic;

import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import org.apache.wink.json4j.JSONException;
import org.opendatakit.suitcase.model.CloudEndpointInfo;
import org.opendatakit.suitcase.net.SuitcaseSwingWorker;
import org.opendatakit.suitcase.net.SyncWrapper;
import org.opendatakit.suitcase.ui.DialogUtils;
import org.opendatakit.suitcase.ui.ProgressBarStatus;
import org.opendatakit.suitcase.ui.SuitcaseProgressBar;

public class LoginTask extends SuitcaseSwingWorker<Void> {

    private CloudEndpointInfo cloudEndpointInfo;
    private boolean isGUI;

    public LoginTask(CloudEndpointInfo cloudEndpointInfo, boolean isGUI) {
        super();

        this.cloudEndpointInfo = cloudEndpointInfo;
        this.isGUI = isGUI;
    }

    @Override
    protected Void doInBackground() throws IOException, JSONException {
        ResourceBundle translations = TranslationUtil.getTranslations();
        String msg = translations.getString(TranslationConsts.LOGGING_INTO_SRV_MSG);

        SyncWrapper syncWrapper = SyncWrapper.getInstance();
        publish(new ProgressBarStatus(0, msg, false));

        syncWrapper.reset();
        publish(new ProgressBarStatus(33, msg, false));

        syncWrapper.init(cloudEndpointInfo);
        publish(new ProgressBarStatus(66, msg, false));

        syncWrapper.setPrivilegesInfo();
        publish(new ProgressBarStatus(100, msg, false));

        return null;
    }

    @Override
    protected void finished() {
        ResourceBundle translations = TranslationUtil.getTranslations();
        try {
            get();

            publish(new ProgressBarStatus(0, translations.getString(TranslationConsts.LOGIN_SUCCESS_MSG), false));
        } catch (InterruptedException e) {
            e.printStackTrace();
            DialogUtils.showError(translations.getString(TranslationConsts.GENERIC_ERROR), isGUI);
            returnCode = SuitcaseSwingWorker.errorCode;
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();

            String errMsg;
            if (cause instanceof JSONException) {
                errMsg = translations.getString(TranslationConsts.BAD_CRED_ERROR);
            } else if (cause instanceof IOException) {
                errMsg = translations.getString(TranslationConsts.HTTP_IO_ERROR);
            } else {
                errMsg = translations.getString(TranslationConsts.GENERIC_ERROR);
            }
            publish(new ProgressBarStatus(0, SuitcaseProgressBar.PB_ERROR, false));
            DialogUtils.showError(errMsg, isGUI);
            cause.printStackTrace();
            returnCode = SuitcaseSwingWorker.errorCode;
        }
    }
}
