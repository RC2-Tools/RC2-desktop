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

import edu.uw.cse.ifrcdemo.mustachetopdf.PdfGenerator;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Logger;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static edu.uw.cse.ifrcdemo.planningsharedlib.util.FxDialogUtil.showScrollingExceptionDialog;

public class ControllerPdfUtil {
  public static void writeControllerPdf(WebContext webContext, String reportName,
      String templateLocation,
      Logger logger, TemplateEngine templateEngine) {
    Path outputPath;
    try {
      outputPath = Files.createTempFile(reportName, GenConsts.PDF_FILE_EXTENSION);
    } catch (IOException e1) {
      logger
          .error("Error while creating temp file for report \n" + ExceptionUtils.getStackTrace(e1));
      showScrollingExceptionDialog(
          TranslationUtil.getTranslations().getString(TranslationConsts.COULD_NOT_CREATE_TEMP_FILE),
          e1);
      throw new RuntimeException(e1);
    }

    String processedHtml = templateEngine.process(templateLocation, webContext);

    try {
      PdfGenerator.generateCustomPdf(processedHtml, outputPath);

      if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().open(outputPath.toFile());
      } else {
        logger.error("Desktop NOT supported");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
