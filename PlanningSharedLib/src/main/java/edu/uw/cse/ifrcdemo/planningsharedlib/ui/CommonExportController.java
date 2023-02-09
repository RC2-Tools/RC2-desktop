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

package edu.uw.cse.ifrcdemo.planningsharedlib.ui;

import edu.uw.cse.ifrcdemo.planningsharedlib.ui.export.ExportFormModel;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.preference.PreferenceUtil;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.FxDialogUtil;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.InternalFileStoreUtil;
import edu.uw.cse.ifrcdemo.sharedlib.consts.FileConsts;
import edu.uw.cse.ifrcdemo.sharedlib.consts.ToolPropertiesConsts;
import edu.uw.cse.ifrcdemo.translations.LogStr;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Logger;
import org.apache.wink.json4j.JSONException;
import org.opendatakit.suitcase.model.CloudEndpointInfo;
import org.opendatakit.sync.client.SyncClient;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
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
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;

import static edu.uw.cse.ifrcdemo.planningsharedlib.util.FxDialogUtil.showScrollingExceptionDialog;

public abstract class CommonExportController extends CommonImportController {

    public CommonExportController(Logger logger) {
        super(logger);
    }

    protected abstract void validateNUpload(ResourceBundle translations, CloudEndpointInfo cloudEndpointInfo, Path outputPath) throws IOException, JSONException;
    protected abstract Path export(ResourceBundle translations) throws IOException;

    @ModelAttribute("exportFormModel")
    public ExportFormModel newExportFormModel() {
        return new ExportFormModel();
    }

    protected void createAppProperties(ResourceBundle translations, CloudEndpointInfo cloudEndpointInfo, Path outputPath) throws IOException {
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
    }

    protected Path getNewOutputPath(ResourceBundle translations) throws IOException {
        Path outputPath;
        try {
          InternalFileStoreUtil.setCurrentOutputPath();
          outputPath = InternalFileStoreUtil.getCurrentOutputStoragePath();
        } catch (IOException e) {
          logger.error(LogStr.LOG_FAILED_TO_CREATE_DATED_OUTPUT_DIRECTORY + '\n' + ExceptionUtils.getStackTrace(e));
          showScrollingExceptionDialog(translations.getString(TranslationConsts.COULD_NOT_CREATE_DATED_OUTPUT_DIRECTORY), e);
          throw(e);
        }
        return outputPath;
    }

    protected void exportToServer(ResourceBundle translations, CloudEndpointInfo cloudEndpointInfo) throws IOException, JSONException {
        // setup export folder
        Path outputPath = export(translations);

        // populate app.properties with server url
        createAppProperties(translations, cloudEndpointInfo, outputPath);

        // Create model and IO objects. These will verify and read the csvs
        validateNUpload(translations, cloudEndpointInfo, outputPath);
    }

    protected ModelAndView setServerInfo(@ModelAttribute("exportFormModel") ExportFormModel exportFormModel, String viewName) {
        String serverUrl = PreferenceUtil.getServerUrl();
        if (serverUrl != null) {
            exportFormModel.setServerUrl(serverUrl);
        }

        String username = PreferenceUtil.getUsername();
        if (username != null) {
            exportFormModel.setUsername(username);
        }

        return new ModelAndView(viewName);
    }

    protected ModelAndView helperExportSyncToServer(@ModelAttribute("exportFormModel") @Valid ExportFormModel exportFormModel, BindingResult bindingResult, SessionStatus status, String viewName) throws IOException, JSONException, BackingStoreException, InvalidPreferencesFormatException {
        if (bindingResult.hasErrors()) {
            return new ModelAndView(viewName);
        }

        ResourceBundle translations = TranslationUtil.getTranslations();
        CloudEndpointInfo cloudEndpointInfo = exportFormModel.toCloudEndpointInfo();

        updateServerPreferences(exportFormModel.toCloudEndpointInfo());

        exportToServer(translations, cloudEndpointInfo);

        status.setComplete();
        return new ModelAndView(REDIRECT_TO_ROOT);
    }


    protected ModelAndView helperExportImportSyncToServer(@ModelAttribute("exportFormModel") @Valid ExportFormModel exportFormModel, BindingResult bindingResult, SessionStatus status, String viewName) throws IOException, InvalidPreferencesFormatException, BackingStoreException, InterruptedException, JSONException {
        if (bindingResult.hasErrors()) {
            return new ModelAndView(viewName);
        }

        ResourceBundle translations = TranslationUtil.getTranslations();
        CloudEndpointInfo cloudEndpointInfo;
        try {
            cloudEndpointInfo = exportFormModel.toCloudEndpointInfo();
        } catch (MalformedURLException e) {
            logger.catching(e);
            FxDialogUtil.showScrollingExceptionDialog(
                    translations.getString(TranslationConsts.ERROR_WHILE_DOWNLOADING_DATA), e);
            return new ModelAndView(viewName);
        }

        if (!downlandNReimport(cloudEndpointInfo)) return new ModelAndView(viewName);

        updateServerPreferences(exportFormModel.toCloudEndpointInfo());

        exportToServer(translations, cloudEndpointInfo);

        status.setComplete();
        return new ModelAndView(REDIRECT_TO_ROOT);
    }

}
