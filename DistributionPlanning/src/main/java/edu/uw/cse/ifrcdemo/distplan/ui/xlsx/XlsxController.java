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

package edu.uw.cse.ifrcdemo.distplan.ui.xlsx;

import edu.uw.cse.ifrcdemo.distplan.util.InternalFileStoreUtil;
import edu.uw.cse.ifrcdemo.sharedlib.consts.FileConsts;
import edu.uw.cse.ifrcdemo.sharedlib.util.OdkPathUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.XlsxUtil;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/xlsx")
public class XlsxController {
  private final Logger logger;

  public XlsxController(Logger logger) {
    this.logger = logger;
  }

  @PostMapping("file")
  public StreamingResponseBody getXlsxFilePost(@RequestBody String path) {
    Path filePath = Paths.get(path);

    if (Files.isDirectory(filePath) || Files.notExists(filePath)) {
      return null;
    }

    return outputStream -> Files.copy(filePath, outputStream);
  }

  @PostMapping("post")
  public ResponseEntity<String> convertedFormPost(@ModelAttribute ConvertedXlsx convertedXlsx) throws IOException {
    boolean formUpdateCompatible = checkFormUpdate(convertedXlsx);
    if (!formUpdateCompatible) {
      String errMsg = TranslationUtil.getTranslations().getString(TranslationConsts.INCOMPATIBLE_FORM_UPDATE);
      return ResponseEntity.badRequest().body(errMsg);
    }

    Path xlsxFormStoragePath = InternalFileStoreUtil.getXlsxFormStoragePath();

    Path tablePath = OdkPathUtil.getTablePath(xlsxFormStoragePath, convertedXlsx.getTableId());
    Path formPath = OdkPathUtil.getFormPath(tablePath, convertedXlsx.getFormId());

    if (Files.exists(formPath)) {
      FileUtils.deleteDirectory(formPath.toFile());
    }
    Files.createDirectories(formPath);

    try (Writer writer = Files.newBufferedWriter(formPath.resolve(FileConsts.FORM_DEF_JSON))) {
      writer.write(convertedXlsx.getFormDef());
    }

    try (Writer writer = Files.newBufferedWriter(tablePath.resolve(FileConsts.DEFINITION_CSV))) {
      writer.write(convertedXlsx.getDefinition());
    }

    try (Writer writer = Files.newBufferedWriter(tablePath.resolve(FileConsts.PROPERTIES_CSV))) {
      writer.write(convertedXlsx.getProperties());
    }

    try (Writer writer = Files.newBufferedWriter(tablePath.resolve(FileConsts.TABLE_SPECIFIC_DEF_JS))) {
      writer.write(convertedXlsx.getTableSpecificDefinitions());
    }

    XlsxUtil.organizeFormLevelFiles(
        Paths.get(convertedXlsx.getXlsxPath()),
        convertedXlsx.getTableId(),
        convertedXlsx.getFormId(),
        xlsxFormStoragePath
    );

    return ResponseEntity.ok().build();
  }

  private static boolean checkFormUpdate(ConvertedXlsx newForm) throws IOException {
    Path tablePath = InternalFileStoreUtil.getCurrentSnapshotStoragePath().resolve(newForm.getTableId());

    if (Files.notExists(tablePath)) {
      // this is a new table, always compatible
      return true;
    }

    String existingDefCsv = new String(
        Files.readAllBytes(tablePath.resolve(FileConsts.DEFINITION_CSV)),
        StandardCharsets.UTF_8
    );

    return newForm.getDefinition().trim().equals(existingDefCsv.trim());
  }
}
