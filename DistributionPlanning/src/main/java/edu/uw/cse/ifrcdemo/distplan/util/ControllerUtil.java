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

package edu.uw.cse.ifrcdemo.distplan.util;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import edu.uw.cse.ifrcdemo.distplan.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.sharedlib.consts.ModuleConsts;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.UntypedSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.HasCustomTable;
import edu.uw.cse.ifrcdemo.sharedlib.util.CsvMapperUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.FileUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.StringUtil;
import org.apache.logging.log4j.Logger;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;

public class ControllerUtil {

  public static void writeOutCustomCsv(List<UntypedSyncRow> customRow, Logger logger) {
    CsvMapper csvMapper = CsvMapperUtil.getCsvMapper(true);
    ObjectWriter csvWriter = CsvMapperUtil.getWriter(csvMapper, customRow.get(0).getColumns().keySet(), true);

    Path outputPath;
    try {
      outputPath = Files.createTempFile(ModuleConsts.PLANNING, GenConsts.CSV_FILE_EXTENSION);
    } catch (IOException e1) {
      // TODO:
      throw new RuntimeException(e1);
    }

    try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardOpenOption.CREATE);
        SequenceWriter sw = csvWriter.writeValues(writer)) {
      sw.writeAll(customRow);
      // set the file to readonly so that
      // the user doesn't get the impression that
      // editing the file has any effect
      outputPath.toFile().setReadOnly();

      if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().open(outputPath.toFile());
      } else {
        logger.error("Desktop NOT supported");
      }
    } catch (IOException e1) {
      throw new UncheckedIOException(e1);
    }
  }

  // TODO: Consolidate into util function
  public static UntypedSyncRow mergeCustomTableRow(HasCustomTable baseTableRow,
      CsvRepository repository,
      UntypedSyncRow mergeTarget) {
    if (StringUtil.isNullOrEmpty(baseTableRow.getCustomTableFormId()) ||
        StringUtil.isNullOrEmpty(baseTableRow.getCustomTableRowId())) {
      return mergeTarget;
    }

    mergeTarget
        .getColumns()
        .putAll(repository
            .readIndexedUntypedCsv(FileUtil.getFileName(baseTableRow.getCustomTableFormId()))
            .map(table -> table.get(baseTableRow.getCustomTableRowId()))
            .map(UntypedSyncRow::getColumns)
            .orElseGet(Collections::emptyMap)
        );

    return mergeTarget;
  }
}
