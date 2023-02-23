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
import edu.uw.cse.ifrcdemo.sharedlib.consts.ServerConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.opendatakit.suitcase.net.SuitcaseSwingWorker;
import org.opendatakit.suitcase.net.SyncWrapper;
import org.opendatakit.suitcase.ui.DialogUtils;
import org.opendatakit.suitcase.ui.ProgressBarStatus;
import org.opendatakit.suitcase.ui.SuitcaseProgressBar;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class ResetTask extends SuitcaseSwingWorker<Void> {

    private String version;
    private boolean isGUI;

    public ResetTask(boolean isGUI) {
        super();
        this.version = ServerConsts.SYNC_PROTOCOL_VERSION;
        this.isGUI = isGUI;
    }

    @Override
    protected Void doInBackground() throws JSONException, IOException, InterruptedException {
        ResourceBundle translations = TranslationUtil.getTranslations();
        publish(new ProgressBarStatus(0, translations.getString(TranslationConsts.DELETING_IN_PROGRESS_MSG), false));

        SyncWrapper syncWrapper = SyncWrapper.getInstance();

        // first delete all app level files
        publish(new ProgressBarStatus(0, translations.getString(TranslationConsts.STAGE_ONE_DELETE_APP_LEVEL_FILES), false));
        JSONArray appFiles = syncWrapper.getManifestForAppLevelFiles(version).getJSONArray(ServerConsts.FILES_KEY);
        for (int i = 0; i < appFiles.size(); i++) {
            String filename = appFiles.getJSONObject(i).getString(ServerConsts.FILENAME_KEY);
            syncWrapper.deleteFile(filename, version);

            int progress = (int) ((double) i + 1 / appFiles.size() * 100);
            publish(new ProgressBarStatus(floorNCeilingProgress(progress), translations.getString(TranslationConsts.STAGE_ONE_DELETED) + filename, null));
        }
        setProgress(100);

        // then delete all table definitions
        publish(new ProgressBarStatus(0, translations.getString(TranslationConsts.STAGE_TWO_DELETING_TABLE_DEFINITIONS), false));
        Set<String> tables = syncWrapper.updateTableList();
        int tableCounter = 0;
        for (String table : tables) {
            // for large data sets deletion might timeout
            // so tables must be repeatedly deleted

            int retryCounter = 1;
            int status;
            int progress = (int) ((double) tableCounter / tables.size() * 100);
            while ((status = syncWrapper.deleteTableDefinition(table)) == 500) {
                String msg = translations.getString(TranslationConsts.STAGE_TWO_DELETING) + GenConsts.SPACE + table + GenConsts.SPACE + translations.getString(TranslationConsts.RETRY_MSG) + retryCounter++;
                publish(new ProgressBarStatus(floorNCeilingProgress(progress), msg, null));
            }
            if (status < 200 || status > 299) {
                throw new IllegalStateException(translations.getString(TranslationConsts.UNEXPECTED_STATUS_CODE_ERROR) + status);
            }
            String msg = translations.getString(TranslationConsts.STAGE_TWO_DELETED) + GenConsts.SPACE + table;
            tableCounter++;
            progress = (int) ((double) tableCounter / tables.size() * 100);
            publish(new ProgressBarStatus(floorNCeilingProgress(progress), msg, false));
        }
        setProgress(100);

        publish(new ProgressBarStatus(10, translations.getString(TranslationConsts.STAGE_THREE_DELETING_TABLES_IN_BAD_STATES), false));
        // the table id and schemaETag can be anything
        while ((syncWrapper.deleteTableDefinition(ServerConsts.TABLE, ServerConsts.ETAG)) == 500);

        Thread.sleep(ServerConsts.RESET_FINISH_WAIT);
        syncWrapper.updateTableList();
        setProgress(100);
        return null;
    }

    @Override
    protected void finished() {
        ResourceBundle translations = TranslationUtil.getTranslations();
        try {
            get();

            publish(new ProgressBarStatus(0, translations.getString(TranslationConsts.FINISHED_RESETTING_SRV_MSG), false));
        } catch (InterruptedException e) {
            e.printStackTrace();
            publish(new ProgressBarStatus(0, SuitcaseProgressBar.PB_ERROR, false));
            DialogUtils.showError(translations.getString(TranslationConsts.GENERIC_ERROR), isGUI);
            returnCode = SuitcaseSwingWorker.errorCode;
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();

            String errMsg;
            if (cause instanceof IOException) {
                errMsg = translations.getString(TranslationConsts.HTTP_IO_ERROR);
            } else if (cause instanceof JSONException || cause instanceof IllegalStateException) {
                errMsg = translations.getString(TranslationConsts.VISIT_WEB_ERROR);
            } else {
                errMsg = translations.getString(TranslationConsts.GENERIC_ERROR);
            }

            cause.printStackTrace();
            publish(new ProgressBarStatus(0, SuitcaseProgressBar.PB_ERROR, false));
            DialogUtils.showError(errMsg, isGUI);
            returnCode = SuitcaseSwingWorker.errorCode;
        } finally {
            setIndeterminate(false);
        }
    }

    private int floorNCeilingProgress(int progress) {
        if(progress < 0)
            return 0;
        else if(progress > 100)
            return 100;
        else
            return progress;
    }
}
