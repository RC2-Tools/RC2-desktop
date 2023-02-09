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

package edu.uw.cse.ifrcdemo.distplan.ui;

import edu.uw.cse.ifrcdemo.distplan.data.ReliefDataRepos;
import edu.uw.cse.ifrcdemo.distplan.sync.CopyForms;
import edu.uw.cse.ifrcdemo.distplan.util.ExportUtil;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.CommonExportController;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.sharedlib.logic.Validator;
import edu.uw.cse.ifrcdemo.sharedlib.model.db.ReliefMobileDbModel;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.BaseSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvAuthorization;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvEntitlement;
import edu.uw.cse.ifrcdemo.sharedlib.net.SuitcaseWrapper;
import edu.uw.cse.ifrcdemo.translations.LogStr;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Logger;
import org.apache.wink.json4j.JSONException;
import org.opendatakit.suitcase.model.CloudEndpointInfo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import static edu.uw.cse.ifrcdemo.planningsharedlib.util.FxDialogUtil.showInfoDialog;
import static edu.uw.cse.ifrcdemo.planningsharedlib.util.FxDialogUtil.showScrollingExceptionDialog;

public class CommonReliefExportController extends CommonExportController {
    private final ReliefDataRepos reliefDataRepos;
    private final ExportUtil exportUtil;

    public CommonReliefExportController(ReliefDataRepos reliefDataRepos, ExportUtil exportUtil, Logger logger) {
        super(logger);
        this.reliefDataRepos = reliefDataRepos;
        this.exportUtil = exportUtil;
    }

    protected Path export(ResourceBundle translations) throws IOException {
        Path outputPath = getNewOutputPath(translations);
        exportUtil.export(outputPath);

        CopyForms copy = new CopyForms(reliefDataRepos);
        copy.copyForms(outputPath);
        return outputPath;
    }


    protected void validateNUpload(ResourceBundle translations, CloudEndpointInfo cloudEndpointInfo, Path outputPath) throws IOException, JSONException {
        // Create model and IO objects. These will verify and read the csvs
        List<Class<? extends BaseSyncRow>> requiredUploadCSVs;
        List<Class<? extends BaseSyncRow>> list = new LinkedList<>();
        requiredUploadCSVs = Collections.unmodifiableList(list);
        ReliefMobileDbModel model = null;
        try {
            model = new ReliefMobileDbModel(outputPath, requiredUploadCSVs);
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
    }

}
