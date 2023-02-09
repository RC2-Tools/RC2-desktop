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

package edu.uw.cse.ifrcdemo.planningsharedlib.util;

import edu.uw.cse.ifrcdemo.planningsharedlib.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.UntypedSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.HasCustomTable;
import edu.uw.cse.ifrcdemo.sharedlib.util.FileUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.StringUtil;

import java.util.Collections;
import java.util.Map;
import java.util.function.BiConsumer;

public class BeneficiaryUtilBase {


    public static String getFromCombinedRow(UntypedSyncRow combinedRow, String tableId, String column) {
      return combinedRow.getColumns().get(BeneficiaryUtilBase.colNameRemap(tableId, column));
    }


    protected static UntypedSyncRow mergeCustomTableRow(HasCustomTable baseTableRow,
                                                        CsvRepository repository,
                                                        UntypedSyncRow mergeTarget) {
      return BeneficiaryUtilBase.mergeCustomTableRow(
          baseTableRow,
          baseTableRow.getCustomTableFormId(),
          repository,
          mergeTarget
      );
    }

    protected static UntypedSyncRow mergeCustomTableRow(HasCustomTable baseTableRow,
                                                        String columnPrefix,
                                                        CsvRepository repository,
                                                        UntypedSyncRow mergeTarget) {
      if (StringUtil.isNullOrEmpty(baseTableRow.getCustomTableFormId()) ||
          StringUtil.isNullOrEmpty(baseTableRow.getCustomTableRowId())) {
        return mergeTarget;
      }

      return BeneficiaryUtilBase.addRowsFromTable(
          baseTableRow.getCustomTableFormId(),
          baseTableRow.getCustomTableRowId(),
          columnPrefix,
          mergeTarget,
          repository
      );
    }

    protected static UntypedSyncRow addRowsFromTable(String tableId,
                                                     String rowId,
                                                     UntypedSyncRow mergeTarget,
                                                     CsvRepository repository) {
      return BeneficiaryUtilBase.addRowsFromTable(tableId, rowId, tableId, mergeTarget, repository);
    }

    protected static UntypedSyncRow addRowsFromTable(String tableId,
                                                     String rowId,
                                                     String columnPrefix,
                                                     UntypedSyncRow mergeTarget,
                                                     CsvRepository repository) {
      repository
          .readIndexedUntypedCsv(FileUtil.getFileName(tableId))
          .map(table -> table.get(rowId))
          .map(UntypedSyncRow::getColumns)
          .orElseGet(Collections::emptyMap)
          .forEach(BeneficiaryUtilBase.remapPut(mergeTarget.getColumns(), columnPrefix));

      return mergeTarget;
    }

    private static String colNameRemap(String prefix, String column) {
      return prefix + GenConsts.UNDERSCORE + column;
    }

    protected static BiConsumer<String, String> remapPut(Map<String, String> map, String prefix) {
      return (k, v) -> map.put(BeneficiaryUtilBase.colNameRemap(prefix, k), v);
    }
}
