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

package edu.uw.cse.ifrcdemo.healthplan.ui;

import edu.uw.cse.ifrcdemo.healthplan.data.HealthDataRepos;
import edu.uw.cse.ifrcdemo.healthplan.ui.sync.HealthCopyForms;
import edu.uw.cse.ifrcdemo.healthplan.util.ExportUtil;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.CommonExportController;
import edu.uw.cse.ifrcdemo.sharedlib.logic.Validator;
import edu.uw.cse.ifrcdemo.sharedlib.model.db.HealthMobileDbModel;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.BaseSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.net.SuitcaseWrapper;
import edu.uw.cse.ifrcdemo.translations.LogStr;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Logger;
import org.apache.wink.json4j.JSONException;
import org.opendatakit.suitcase.model.CloudEndpointInfo;

import static edu.uw.cse.ifrcdemo.planningsharedlib.util.FxDialogUtil.showScrollingExceptionDialog;

public class CommonHealthExportController extends CommonExportController {
    private final HealthDataRepos healthDataRepos;
    private final ExportUtil exportUtil;

    public CommonHealthExportController(HealthDataRepos healthDataRepos, ExportUtil exportUtil, Logger logger) {
        super(logger);
        this.healthDataRepos = healthDataRepos;
        this.exportUtil = exportUtil;
    }

    protected Path export(ResourceBundle translations) throws IOException {
        Path outputPath = getNewOutputPath(translations);
        exportUtil.export(outputPath);

        HealthCopyForms copyForms = new HealthCopyForms(healthDataRepos);
        copyForms.copyForms(outputPath);
        return outputPath;
    }

    protected void validateNUpload(ResourceBundle translations, CloudEndpointInfo cloudEndpointInfo, Path outputPath) throws IOException, JSONException {
      // Create model and IO objects. These will verify and read the csvs
      List<Class<? extends BaseSyncRow>> requiredUploadCSVs;
      List<Class<? extends BaseSyncRow>> list = new LinkedList<>();
      requiredUploadCSVs = Collections.unmodifiableList(list);
      HealthMobileDbModel model = null;
      try {
        model = new HealthMobileDbModel(outputPath, requiredUploadCSVs);
      } catch (IOException e) {
        logger.error(LogStr.LOG_FAILED_TO_VALIDATE_MOBILE_DATABASE + '\n' + ExceptionUtils.getStackTrace(e));
        showScrollingExceptionDialog(translations.getString(TranslationConsts.FAILED_TO_VALIDATE_MOBILE_DATABASE), e);
        throw(e);
      }

      // Validate the tables to make sure hand editing hasn't created invalid CSVs
      Validator validator = new Validator(model);
      validator.resolveAllRowIdConflicts();
      // TODO: Any other validity checks necessary?

      SuitcaseWrapper suitcaseWrapper = new SuitcaseWrapper(cloudEndpointInfo, outputPath);
      boolean success = false;
      try {
        success = suitcaseWrapper.uploadAllTables();
      } catch (IOException | JSONException e) {
        logger.error(LogStr.LOG_ERROR_OCCURRED_WHILE_UPLOADING_TABLES + '\n' + ExceptionUtils.getStackTrace(e));
        showScrollingExceptionDialog(translations.getString(TranslationConsts.ERROR_OCCURRED_WHILE_UPLOADING_TABLES), e);
        throw(e);
      }

      // TODO: do some verification
    }

}
