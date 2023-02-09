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

package edu.uw.cse.ifrcdemo.planningsharedlib.ui.util;

import edu.uw.cse.ifrcdemo.planningsharedlib.data.InvalidCsvException;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.login.DataInputSourceType;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.FxDialogUtil;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.InternalFileStoreUtil;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.NoSuchFileException;
import java.util.ResourceBundle;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public class SyncUiUtil {
  private static final Logger logger = LogManager.getLogger(SyncUiUtil.class);

  public static boolean handleReimportData(
      File selectedDirectory,
      DataInputSourceType sourceType) throws InterruptedException {
    ResourceBundle translations = TranslationUtil.getTranslations();

    try {
      InternalFileStoreUtil.reimportData(selectedDirectory).get();
      return true;
    } catch (FileNotFoundException | NoSuchFileException e) {
      logger.catching(Level.INFO, e);

      String errMsg;
      if (sourceType == DataInputSourceType.RC2_SERVER) {
        errMsg = translations.getString(TranslationConsts.CSV_FILE_NOT_FOUND_RC2);
      } else {
        errMsg = translations.getString(TranslationConsts.CSV_FILE_NOT_FOUND_LOCAL);
      }

      FxDialogUtil.showScrollingErrorDialog(
          translations.getString(TranslationConsts.ERROR_WHILE_IMPORTING_DATA),
          errMsg,
          null,
          e.getMessage(),
          true
      );

      return false;
    } catch (InvalidCsvException e) {
      logger.catching(Level.INFO, e);

      FxDialogUtil.showScrollingErrorDialog(
          translations.getString(TranslationConsts.ERROR_WHILE_IMPORTING_DATA),
          translations.getString(TranslationConsts.CSV_VALIDATION_FAILED),
          null,
          e.getMessage(),
          true
      );

      return false;
    } catch (IOException | UncheckedIOException | IllegalAccessException e) {
      logger.catching(e);

      FxDialogUtil.showScrollingExceptionDialog(
          translations.getString(TranslationConsts.ERROR_WHILE_IMPORTING_DATA),
          translations.getString(TranslationConsts.CSV_IMPORT_IO_EXCEPTION),
          e,
          true
      );

      return false;
    } catch (ExecutionException | CompletionException | CancellationException e) {
      logger.error("Error while importing data", e);

      FxDialogUtil.showScrollingExceptionDialog(
          translations.getString(TranslationConsts.ERROR_WHILE_IMPORTING_DATA), e);

      return false;
    }
  }
}
