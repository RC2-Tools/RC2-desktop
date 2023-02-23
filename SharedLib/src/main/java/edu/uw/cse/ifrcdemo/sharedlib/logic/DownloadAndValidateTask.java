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

package edu.uw.cse.ifrcdemo.sharedlib.logic;

import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.sharedlib.consts.ModuleConsts;
import edu.uw.cse.ifrcdemo.sharedlib.model.config.SaveDirectory;
import edu.uw.cse.ifrcdemo.sharedlib.model.db.MobileDbModel;
import edu.uw.cse.ifrcdemo.sharedlib.net.SuitcaseWrapper;
import edu.uw.cse.ifrcdemo.sharedlib.util.DialogUtil;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import org.apache.wink.json4j.JSONException;
import org.opendatakit.suitcase.model.CloudEndpointInfo;
import org.opendatakit.suitcase.net.SuitcaseSwingWorker;
import org.opendatakit.suitcase.ui.ProgressBarStatus;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;



public class DownloadAndValidateTask extends SuitcaseSwingWorker<Void> {

    // TODO: Add back logging

    private CloudEndpointInfo cloudEndpointInfo;
    private SaveDirectory saveDirectory;
    private boolean withAttachment;

    private boolean suitcaseComplete;

    public DownloadAndValidateTask(CloudEndpointInfo cloudEndpointInfo, SaveDirectory saveDirectory, boolean withAttachment) {
        super();
        this.cloudEndpointInfo = cloudEndpointInfo;
        this.saveDirectory = saveDirectory;
        this.withAttachment = withAttachment;
    }

    public DownloadAndValidateTask(CloudEndpointInfo cloudEndpointInfo, SaveDirectory saveDirectory) {
        this(cloudEndpointInfo, saveDirectory, true);
    }

    // default save directory
    public DownloadAndValidateTask(CloudEndpointInfo cloudEndpointInfo) throws IOException {
        this(cloudEndpointInfo, new SaveDirectory(), true);
    }

    @Override
    protected Void doInBackground() throws IOException, IllegalStateException, IllegalArgumentException, JSONException {
        ResourceBundle translations = TranslationUtil.getTranslations();
        suitcaseComplete = false;


        publish(new ProgressBarStatus(0, translations.getString(TranslationConsts.CREATE_DIR_MSG), false));

        // Create directory structure
        this.saveDirectory.setCurrentSaveDirectory(ModuleConsts.DOWNLOAD);
        Path datedPath = this.saveDirectory.getCurrentSaveDirectory();
        SuitcaseWrapper suitcaseWrapper = new SuitcaseWrapper(cloudEndpointInfo, datedPath);


        // Download Tables
        publish(new ProgressBarStatus(50, translations.getString(TranslationConsts.DOWNLOADING_TABLES_MSG), false));

        suitcaseWrapper.downloadAllTables(withAttachment, false);
        suitcaseWrapper.downloadConfigFiles();

        // Validate csvs
        publish(new ProgressBarStatus(90, translations.getString(TranslationConsts.VALIDATING_MSG), false));

        suitcaseComplete = true;

        // Create model
            MobileDbModel model = new MobileDbModel(datedPath, MobileDbModel.tableDefinition);

        if (model == null) {
            throw new IOException(translations.getString(TranslationConsts.FAILED_TO_READ_DATABASE_FILES_FROM_DISK_ERROR));
        }

        Validator resolver = new Validator(model);
        resolver.resolveBeneficiaryEntityIdConflicts();
        //resolver.resolveIndividualIdConflicts();
        // TODO: Does anything else need to be validated?

        publish(new ProgressBarStatus(100, translations.getString(TranslationConsts.VALIDATION_COMPLETE_MSG), false));

        return null;
    }

    @Override
    protected void finished() {
        ResourceBundle translations = TranslationUtil.getTranslations();
        try {
            get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            DialogUtil.showErrorDialog(translations.getString(TranslationConsts.GENERIC_ERROR));
            setString(translations.getString(TranslationConsts.ERROR_LABEL));
            setProgress(100);
            returnCode = SuitcaseSwingWorker.errorCode;
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();

            String errMsg;

            if (cause instanceof IllegalArgumentException) {
                errMsg = cause.getMessage();
            } else if (suitcaseComplete) {
                errMsg = translations.getString(TranslationConsts.VALIDATION_ERROR) + GenConsts.NEW_LINE + cause.getMessage();
            } else if (cause instanceof IOException) {
                errMsg = translations.getString(TranslationConsts.HTTP_IO_ERROR);
            } else if (cause instanceof JSONException) {
                errMsg = translations.getString(TranslationConsts.BAD_CRED_ERROR);
            } else {
                errMsg = translations.getString(TranslationConsts.GENERIC_ERROR);
            }

            cause.printStackTrace();
            DialogUtil.showErrorDialog(errMsg);
            setString(translations.getString(TranslationConsts.ERROR_LABEL));
            setProgress(0);
            returnCode = SuitcaseSwingWorker.errorCode;
        } finally {
            setIndeterminate(false);
        }
    }
}
