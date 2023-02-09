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

import edu.uw.cse.ifrcdemo.planningsharedlib.sync.SyncUtil;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.login.DataInputSourceType;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.preference.PreferenceUtil;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.util.SyncUiUtil;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.InternalFileStoreUtil;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import org.apache.logging.log4j.Logger;
import org.opendatakit.suitcase.model.CloudEndpointInfo;

import java.io.File;
import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;

import static edu.uw.cse.ifrcdemo.planningsharedlib.util.FxDialogUtil.showScrollingExceptionDialog;

public class CommonImportController {
    public static final String REDIRECT_TO_ROOT = "redirect:/";
    protected final Logger logger;

    public CommonImportController(Logger logger) {
        this.logger = logger;
    }

    protected boolean downlandNReimport(CloudEndpointInfo cloudEndpointInfo) throws IOException, InvalidPreferencesFormatException, BackingStoreException, InterruptedException {
        try {
            SyncUtil.downloadToProfileTemp(cloudEndpointInfo);
        } catch (Exception e) {
            logger.error("Error while executing suitcase future", e);
            showScrollingExceptionDialog(
                    TranslationUtil.getTranslations().getString(TranslationConsts.ERROR_WHILE_DOWNLOADING_DATA), e);
            return false;
        }

        File downloadedDirectory = InternalFileStoreUtil.getCurrentTempStoragePath().toFile();

        return reimport(
                downloadedDirectory,
            DataInputSourceType.RC2_SERVER
        );


    }

    protected boolean reimport(File selectedDirectory, DataInputSourceType sourceType) throws InterruptedException, InvalidPreferencesFormatException, IOException, BackingStoreException {
        boolean success = SyncUiUtil.handleReimportData(
                selectedDirectory,
                sourceType);

        if(success) {
            PreferenceUtil.loadProfilePreferences();
            PreferenceUtil.setLastKnownGoodSnapshot(
                    InternalFileStoreUtil.getCurrentSnapshotStoragePath().getFileName().toString());

            try {
                PreferenceUtil.writeProfilePreferences();
            } catch (BackingStoreException | IOException e) {
                logger.catching(e);
            }
        }

        return success;
    }

    protected void updateServerPreferences(CloudEndpointInfo cloudEndpointInfo) throws IOException, BackingStoreException, InvalidPreferencesFormatException {
        PreferenceUtil.loadProfilePreferences();

        PreferenceUtil.updatePreferencesFromForm(cloudEndpointInfo.getHostUrl(),
                cloudEndpointInfo.getUserName(), null);

        try {
            PreferenceUtil.writeProfilePreferences();
        } catch (BackingStoreException e) {
            logger.catching(e);
            e.printStackTrace();
        }
    }

    protected void updateInputDataDirectory(String inputDataDirectory) throws IOException, BackingStoreException, InvalidPreferencesFormatException {
        PreferenceUtil.loadProfilePreferences();

        PreferenceUtil.updatePreferencesFromForm(null,
                null, inputDataDirectory);

        try {
            PreferenceUtil.writeProfilePreferences();
        } catch (BackingStoreException e) {
            logger.catching(e);
            e.printStackTrace();
        }
    }
}
