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

package edu.uw.cse.ifrcdemo.distplan.ui.sync;

import edu.uw.cse.ifrcdemo.distplan.data.DataRepos;
import edu.uw.cse.ifrcdemo.distplan.sync.CopyForms;
import edu.uw.cse.ifrcdemo.distplan.sync.SyncUtil;
import edu.uw.cse.ifrcdemo.distplan.ui.export.ExportFormModel;
import edu.uw.cse.ifrcdemo.distplan.ui.login.DataInputSourceType;
import edu.uw.cse.ifrcdemo.distplan.ui.preference.PreferenceUtil;
import edu.uw.cse.ifrcdemo.distplan.ui.util.SyncUiUtil;
import edu.uw.cse.ifrcdemo.distplan.util.ExportUtil;
import edu.uw.cse.ifrcdemo.distplan.util.FxDialogUtil;
import edu.uw.cse.ifrcdemo.distplan.util.InternalFileStoreUtil;
import edu.uw.cse.ifrcdemo.sharedlib.consts.FileConsts;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.sharedlib.consts.ToolPropertiesConsts;
import edu.uw.cse.ifrcdemo.sharedlib.logic.Validator;
import edu.uw.cse.ifrcdemo.sharedlib.model.db.MobileDbModel;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.BaseSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvAuthorization;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvEntitlement;
import edu.uw.cse.ifrcdemo.sharedlib.net.SuitcaseWrapper;
import edu.uw.cse.ifrcdemo.translations.LogStr;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Logger;
import org.apache.wink.json4j.JSONException;
import org.opendatakit.suitcase.model.CloudEndpointInfo;
import org.opendatakit.sync.client.SyncClient;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;

import static edu.uw.cse.ifrcdemo.distplan.util.FxDialogUtil.showInfoDialog;
import static edu.uw.cse.ifrcdemo.distplan.util.FxDialogUtil.showScrollingExceptionDialog;

@Controller
@RequestMapping("/sync")
@SessionAttributes(types = { ExportFormModel.class })
public class SyncController {
    private static final String SYNC_WITH_SERVER = "sync/syncWithServer";

    private final DataRepos dataRepos;
    private final ExportUtil exportUtil;
    private final Logger logger;

    public SyncController(DataRepos repos,
                          ExportUtil exportUtil,
                          Logger logger) {
        this.dataRepos = repos;
        this.exportUtil = exportUtil;
        this.logger = logger;
    }

    @ModelAttribute("exportFormModel")
    public ExportFormModel newExportFormModel() {
        return new ExportFormModel();
    }

    @GetMapping("")
    public ModelAndView showServerSettings(
            @ModelAttribute("exportFormModel") ExportFormModel exportFormModel) {

        String serverUrl = PreferenceUtil.getServerUrl();
        if (serverUrl != null) {
            exportFormModel.setServerUrl(serverUrl);
        }

        String username = PreferenceUtil.getUsername();
        if (username != null) {
            exportFormModel.setUsername(username);
        }

        return new ModelAndView(SYNC_WITH_SERVER);
    }

    @PostMapping("")
    public ModelAndView exportImportSyncToServer(
            @Valid @ModelAttribute("exportFormModel") ExportFormModel exportFormModel,
            BindingResult bindingResult, SessionStatus status) throws IOException, JSONException, BackingStoreException, InvalidPreferencesFormatException, ExecutionException, InterruptedException {
        if (bindingResult.hasErrors()) {
            return new ModelAndView(SYNC_WITH_SERVER);
        }

        ResourceBundle translations = TranslationUtil.getTranslations();
        PreferenceUtil.loadProfilePreferences();

        CloudEndpointInfo cloudEndpointInfo;
        try {
            cloudEndpointInfo = exportFormModel.toCloudEndpointInfo();
        } catch (MalformedURLException e) {
            logger.catching(e);
            FxDialogUtil.showScrollingExceptionDialog(
                translations.getString(TranslationConsts.ERROR_WHILE_DOWNLOADING_DATA), e);
            return new ModelAndView(SYNC_WITH_SERVER);
        }

        try {
            SyncUtil.downloadToProfileTemp(cloudEndpointInfo);
        } catch (Exception e) {
            logger.error("Error while executing suitcase future", e);
            showScrollingExceptionDialog(
                translations.getString(TranslationConsts.ERROR_WHILE_DOWNLOADING_DATA), e);
            return new ModelAndView(SYNC_WITH_SERVER);
        }

        boolean importSuccess = SyncUiUtil.handleReimportData(
            InternalFileStoreUtil.getCurrentTempStoragePath().toFile(),
            DataInputSourceType.RC2_SERVER
        );

        if (!importSuccess) {
            return new ModelAndView(SYNC_WITH_SERVER);
        }

        // update importDir on login only, not on sync
        PreferenceUtil.updatePreferencesFromForm(exportFormModel.getServerUrl(),
                exportFormModel.getUsername(), null);

        PreferenceUtil.setLastKnownGoodSnapshot(
                InternalFileStoreUtil.getCurrentSnapshotStoragePath().getFileName().toString());

        try {
            PreferenceUtil.writeProfilePreferences();
        } catch (BackingStoreException e) {
            logger.catching(e);
        }

        Path outputPath;
        try {
            InternalFileStoreUtil.setCurrentOutputPath();
            outputPath = InternalFileStoreUtil.getCurrentOutputStoragePath();
        } catch (IOException e) {
            logger.error(LogStr.LOG_FAILED_TO_CREATE_DATED_OUTPUT_DIRECTORY + '\n' + ExceptionUtils.getStackTrace(e));
            showScrollingExceptionDialog(translations.getString(TranslationConsts.COULD_NOT_CREATE_DATED_OUTPUT_DIRECTORY), e);
            throw (e);
        }

        exportUtil.export(outputPath);

        CopyForms copy = new CopyForms(dataRepos);
        copy.copyForms(outputPath);

        // populate app.properties with server url
        Path appPropertiesPath = outputPath.resolve(SyncClient.ASSETS_DIR).resolve(FileConsts.APP_PROPERTIES);
        Properties appProperties = new Properties();
        if (Files.exists(appPropertiesPath)) {
            try (InputStream stream = Files.newInputStream(appPropertiesPath)) {
                appProperties.loadFromXML(stream);
            } catch (IOException e) {
                logger.error(LogStr.LOG_FAILED_TO_LOAD_APP_PROPERTIES + '\n' + ExceptionUtils.getStackTrace(e));
                showScrollingExceptionDialog(translations.getString(TranslationConsts.FAILED_TO_LOAD_APP_PROPERTIES), e);
                throw (e);
            }
        }

        appProperties.setProperty(ToolPropertiesConsts.KEY_SERVER_URL, cloudEndpointInfo.getHostUrl());
        // TODO: Should this be set like this?
        appProperties.setProperty(ToolPropertiesConsts.KEY_COMMON_TRANSLATIONS_LOCALE, "");

        // assets directory might not exist
        try {
            Files.createDirectories(outputPath.resolve(SyncClient.ASSETS_DIR));
            OutputStream stream =
                    Files.newOutputStream(appPropertiesPath, StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING);
            appProperties.storeToXML(stream, null);
        } catch (IOException e) {
            logger.error(LogStr.LOG_FAILED_TO_SAVE_APP_PROPERTIES + '\n' + ExceptionUtils.getStackTrace(e));
            showScrollingExceptionDialog(translations.getString(TranslationConsts.FAILED_TO_SAVE_APP_PROPERTIES), e);
            throw (e);
        }

        // Create model and IO objects. These will verify and read the csvs
        List<Class<? extends BaseSyncRow>> requiredUploadCSVs;
        List<Class<? extends BaseSyncRow>> list = new LinkedList<>();
        requiredUploadCSVs = Collections.unmodifiableList(list);
        MobileDbModel model = null;
        try {
            model = new MobileDbModel(outputPath, requiredUploadCSVs);
        } catch (IOException e) {
            logger.error(LogStr.LOG_FAILED_TO_VALIDATE_MOBILE_DATABASE + '\n' + ExceptionUtils.getStackTrace(e));
            showScrollingExceptionDialog(translations.getString(TranslationConsts.FAILED_TO_VALIDATE_MOBILE_DATABASE), e);
            throw (e);
        }

        // Validate the tables to make sure hand editing hasn't created invalid CSVs
        Validator validator = new Validator(model);
        validator.resolveAllRowIdConflicts();
        // TODO: Any other validity checks necessary?

        List<CsvEntitlement> entitlements = model.getTable(CsvEntitlement.class);
        List<CsvAuthorization> authorizations = model.getTable(CsvAuthorization.class);

        SuitcaseWrapper suitcaseWrapper = new SuitcaseWrapper(cloudEndpointInfo, outputPath);
        boolean success = false;
        try {
            success = suitcaseWrapper.uploadAllTables();
        } catch (IOException | JSONException e) {
            logger.error(LogStr.LOG_ERROR_OCCURRED_WHILE_UPLOADING_TABLES + '\n' + ExceptionUtils.getStackTrace(e));
            showScrollingExceptionDialog(translations.getString(TranslationConsts.ERROR_OCCURRED_WHILE_UPLOADING_TABLES), e);
            throw (e);
        }

        if (success) {
            if (entitlements != null && authorizations != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(entitlements.size());
                sb.append(GenConsts.SPACE);
                sb.append(translations.getString(TranslationConsts.NEW_OR_MODIFIED_ENTITLEMENTS));
                sb.append(GenConsts.SPACE);
                sb.append(authorizations.size());
                sb.append(GenConsts.SPACE);
                sb.append(translations.getString(TranslationConsts.NEW_OR_MODIFIED_AUTHORIZATIONS_SUCCESSFULLY_UPLOADED));
                showInfoDialog(sb.toString());
            }

        } else {
            if (entitlements != null && authorizations != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(entitlements.size());
                sb.append(GenConsts.SPACE);
                sb.append(translations.getString(TranslationConsts.NEW_OR_MODIFIED_ENTITLEMENTS));
                sb.append(GenConsts.SPACE);
                sb.append(authorizations.size());
                sb.append(GenConsts.SPACE);
                sb.append(translations.getString(TranslationConsts.NEW_OR_MODIFIED_AUTHORIZATIONS_UPLOADED_TO_SERVER_WITH_ERRORS));
                showInfoDialog(sb.toString());
            }
        }

        status.setComplete();

        return new ModelAndView("redirect:/");
    }
}
