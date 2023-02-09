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

import edu.uw.cse.ifrcdemo.sharedlib.consts.FileConsts;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.sharedlib.consts.ToolPropertiesConsts;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.Module;
import edu.uw.cse.ifrcdemo.sharedlib.model.db.HealthMobileDbModel;
import edu.uw.cse.ifrcdemo.sharedlib.model.db.MobileDbModel;
import edu.uw.cse.ifrcdemo.sharedlib.model.db.ReliefMobileDbModel;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.BaseSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvAuthorization;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvEntitlement;
import edu.uw.cse.ifrcdemo.sharedlib.net.SuitcaseWrapper;
import edu.uw.cse.ifrcdemo.sharedlib.util.DialogUtil;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.zip.DataFormatException;
import org.apache.wink.json4j.JSONException;
import org.opendatakit.suitcase.model.CloudEndpointInfo;
import org.opendatakit.suitcase.net.SuitcaseSwingWorker;
import org.opendatakit.suitcase.ui.ProgressBarStatus;
import org.opendatakit.suitcase.ui.SuitcaseProgressBar;
import org.opendatakit.sync.client.SyncClient;

public class VerifyAndUploadTask extends SuitcaseSwingWorker<Void> {

    // TODO: Make this actually verify and upload
    // TODO: Add back logging

    private static final List<Class<? extends BaseSyncRow>> requiredUploadCSVs;

    static {
        List<Class<? extends BaseSyncRow>> list = new LinkedList<>();
        // TODO: Should add these as required csvs only when uploading after distribution planning module. This will makes sense once we automate modules together
        //list.add(CsvAuthorization.class);
        //list.add(CsvEntitlement.class);

        requiredUploadCSVs = Collections.unmodifiableList(list);
    }

    private final CloudEndpointInfo cloudEndpointInfo;
    private final Path savePath;
    private final Module moduleType;

    public VerifyAndUploadTask(CloudEndpointInfo cloudEndpointInfo, Path savePath, Module moduleType) {
        super();
        this.cloudEndpointInfo = cloudEndpointInfo;
        this.savePath = savePath;
        this.moduleType = moduleType;
    }

    @Override
    protected Void doInBackground() throws IOException, IllegalStateException, IllegalArgumentException, JSONException, DataFormatException {
        ResourceBundle translations = TranslationUtil.getTranslations();
        // TODO: re-enable checks
        publish(new ProgressBarStatus(10, translations.getString(TranslationConsts.BEGIN_VERIFYNUPLOAD_TASK_MSG), false));

        // populate app.properties with server url
        Path appPropertiesPath = savePath.resolve(SyncClient.ASSETS_DIR).resolve(FileConsts.APP_PROPERTIES);
        Properties appProperties = new Properties();
        if (Files.exists(appPropertiesPath)) {
            try (InputStream stream = Files.newInputStream(appPropertiesPath)) {
                appProperties.loadFromXML(stream);
            }
        }

        appProperties.setProperty(ToolPropertiesConsts.KEY_SERVER_URL, cloudEndpointInfo.getHostUrl());
        appProperties.setProperty(ToolPropertiesConsts.KEY_COMMON_TRANSLATIONS_LOCALE, "");

        // assets directory might not exist
        Files.createDirectories(savePath.resolve(SyncClient.ASSETS_DIR));
        try (OutputStream stream =
                 Files.newOutputStream(appPropertiesPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            appProperties.storeToXML(stream, null);
        }

        // Create model and IO objects. These will verify and read the csvs

        publish(new ProgressBarStatus(20, translations.getString(TranslationConsts.BEGIN_VERIFICATION_MSG), false));

        MobileDbModel model;

        if(moduleType == Module.RELIEF) {
            model = new ReliefMobileDbModel(savePath, requiredUploadCSVs);
        } else if (moduleType == Module.HEALTH) {
            model = new HealthMobileDbModel(savePath, requiredUploadCSVs);
        } else {
            throw new IllegalArgumentException("RC2 module undefined for VerifyAndUploadTask");
        }

        // Validate the tables to make sure hand editing hasn't created invalid CSVs
        Validator validator = new Validator(model);
        validator.resolveAllRowIdConflicts();
        // TODO: Any other validity checks necessary?

        List<CsvEntitlement> entitlements = model.getTable(CsvEntitlement.class);
        List<CsvAuthorization> authorizations = model.getTable(CsvAuthorization.class);



        publish(new ProgressBarStatus(30, translations.getString(TranslationConsts.UPLOAD_TASK_MSG), false));

        SuitcaseWrapper suitcaseWrapper = new SuitcaseWrapper(cloudEndpointInfo, savePath);
        boolean success = suitcaseWrapper.uploadAllTables();


        if (success) {
            publish(new ProgressBarStatus(100, translations.getString(TranslationConsts.VERIFYNUPLOAD_SUCCESS_MSG), false));

            if (entitlements != null && authorizations != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(entitlements.size());
                sb.append(GenConsts.SPACE);
                sb.append(translations.getString(TranslationConsts.NEW_OR_MODIFIED_ENTITLEMENTS));
                sb.append(GenConsts.SPACE);
                sb.append(authorizations.size());
                sb.append(GenConsts.SPACE);
                sb.append(translations.getString(TranslationConsts.NEW_OR_MODIFIED_AUTHORIZATIONS_SUCCESSFULLY_UPLOADED));

                DialogUtil.showConfirmDialog(translations.getString(TranslationConsts.SUCCESS_LABEL), sb.toString());
            }

        } else {
            publish(new ProgressBarStatus(0, translations.getString(TranslationConsts.VERIFYNUPLOAD_ERRORS_MSG), false));

            if (entitlements != null && authorizations != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(entitlements.size());
                sb.append(GenConsts.SPACE);
                sb.append(translations.getString(TranslationConsts.NEW_OR_MODIFIED_ENTITLEMENTS));
                sb.append(GenConsts.SPACE);
                sb.append(authorizations.size());
                sb.append(GenConsts.SPACE);
                sb.append(translations.getString(TranslationConsts.NEW_OR_MODIFIED_AUTHORIZATIONS_UPLOADED_TO_SERVER_WITH_ERRORS));
                DialogUtil.showConfirmDialog(translations.getString(TranslationConsts.FINISHED_WITH_ERRORS_MSG), sb.toString());
            }
        }

        return null;
    }

    @Override
    protected void finished() {
        ResourceBundle translations = TranslationUtil.getTranslations();
        try {
            get();

        } catch (InterruptedException e) {
            e.printStackTrace();
            publish(new ProgressBarStatus(0, SuitcaseProgressBar.PB_ERROR, false));
            DialogUtil.showErrorDialog(translations.getString(TranslationConsts.GENERIC_ERROR));
            returnCode = SuitcaseSwingWorker.errorCode;
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();

            String errMsg;
            if (cause instanceof JSONException) {
                errMsg = translations.getString(TranslationConsts.BAD_CRED_ERROR);
            } else if (cause instanceof IOException) {
                errMsg = translations.getString(TranslationConsts.HTTP_IO_ERROR);
            } else if (cause instanceof IllegalArgumentException) {
                errMsg = cause.getMessage();
            } else {
                errMsg = translations.getString(TranslationConsts.GENERIC_ERROR);
            }

            cause.printStackTrace();
            publish(new ProgressBarStatus(0, SuitcaseProgressBar.PB_ERROR, false));
            DialogUtil.showErrorDialog(errMsg);
            returnCode = SuitcaseSwingWorker.errorCode;
        } finally {
            setIndeterminate(false);
        }
    }
}
