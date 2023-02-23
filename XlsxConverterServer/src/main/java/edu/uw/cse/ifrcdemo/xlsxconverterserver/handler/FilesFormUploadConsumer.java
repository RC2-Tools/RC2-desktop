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

package edu.uw.cse.ifrcdemo.xlsxconverterserver.handler;

import edu.uw.cse.ifrcdemo.sharedlib.util.OdkPathUtil;
import edu.uw.cse.ifrcdemo.xlsxconverterserver.consts.FileConst;
import edu.uw.cse.ifrcdemo.xlsxconverterserver.consts.RequestConst;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Function;

public class FilesFormUploadConsumer implements FormUploadConsumer {
  private static final String LOG_REQUEST_UNABLE_TO_FIND_PATH_IGNORED = "Request: {}, tableId: {}, formId: {}, part: {}, unable to find path, ignored.";

  private Function<String, Path> pathResolver;
  private final Logger logger;

  public Function<String, Path> getPathResolver() {
    return pathResolver;
  }

  public void setPathResolver(Function<String, Path> pathResolver) {
    this.pathResolver = pathResolver;
  }

  public FilesFormUploadConsumer() {
    this(null);
  }

  public FilesFormUploadConsumer(Function<String, Path> pathResolver) {
    this.pathResolver = pathResolver;
    this.logger = LogManager.getLogger(FilesFormUploadConsumer.class);
  }

  @Override
  public void accept(Function<String, String> paramGetter, String partName, InputStream inputStream) throws Exception {
    String tableId = paramGetter.apply(RequestConst.POST_FORM_TABLE_ID_PARAM);
    String formId = paramGetter.apply(RequestConst.POST_FORM_FORM_ID_PARAM);
    String requestId = paramGetter.apply(RequestConst.REQUEST_ID_PARAM);

    Path basePath = pathResolver.apply(requestId);

    if (basePath == null) {
      logger.warn(
              LOG_REQUEST_UNABLE_TO_FIND_PATH_IGNORED,
          requestId,
          tableId,
          formId,
          paramGetter
      );

      return;
    }

    Path tablePath = OdkPathUtil.getTablePath(basePath, tableId);
    Path formPath = OdkPathUtil.getFormPath(tablePath, formId);
    Files.createDirectories(formPath);

    // among the expected files, only formDef.json goes to the form directory
    // everything else goes to the table directory
    Path outputFilePath = (partName.equals(FileConst.FORM_DEF_JSON) ? formPath : tablePath).resolve(partName);

    Files.copy(inputStream, outputFilePath, StandardCopyOption.REPLACE_EXISTING);
  }
}
