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

package edu.uw.cse.ifrcdemo.sharedlib.util;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.opendatakit.sync.client.SyncClient;

public class CsvSchemaUtil {
  public static CsvSchema buildSyncFrontMetadata(boolean extraMetadata) {
    CsvSchema.Builder builder =  CsvSchema.builder()
        .addColumn(SyncClient.ID_ROW_DEF)
        .addColumn(SyncClient.FORM_ID_ROW_DEF)
        .addColumn(SyncClient.LOCALE_ROW_DEF)
        .addColumn(SyncClient.SAVEPOINT_TYPE_ROW_DEF)
        .addColumn(SyncClient.SAVEPOINT_TIMESTAMP_ROW_DEF)
        .addColumn(SyncClient.SAVEPOINT_CREATOR_ROW_DEF);

    if (extraMetadata) {
      builder = builder
          .addColumn(SyncClient.CREATE_USER_ROW_DEF)
          .addColumn(SyncClient.LAST_UPDATE_USER_ROW_DEF)
          .addColumn(SyncClient.DELETED_ROW_DEF)
          .addColumn(SyncClient.DATA_ETAG_AT_MODIFICATION_ROW_DEF);
    }

    return builder.build();
  }

  public static CsvSchema buildSyncBackMetadata() {
    return CsvSchema.builder()
        .addColumn(SyncClient.DEFAULT_ACCESS_ROW_DEF)
        .addColumn(SyncClient.GROUP_MODIFY_ROW_DEF)
        .addColumn(SyncClient.GROUP_PRIVILEGED_ROW_DEF)
        .addColumn(SyncClient.GROUP_READ_ONLY_ROW_DEF)
        .addColumn(SyncClient.ROW_ETAG_ROW_DEF)
        .addColumn(SyncClient.ROW_OWNER_ROW_DEF)
        .build();
  }

  public static CsvSchema buildSchemaWithMetadata(Iterable<String> untypedColumns, boolean extraMetadata) {
    CsvSchema partial = CsvSchema.builder()
        .addColumns(untypedColumns, CsvSchema.ColumnType.STRING_OR_LITERAL)
        .build()
        .sortedBy(String::compareTo);

    return addMetadataColumns(partial, extraMetadata)
        .withHeader()
        .withColumnReordering(true);
  }

  public static <T> CsvSchema buildSchemaWithMetadata(Class<T> type, CsvMapper mapper, boolean extraMetadata) {
    return CsvSchemaUtil
        .addMetadataColumns(mapper.typedSchemaFor(type), extraMetadata)
        .withHeader()
        .withColumnReordering(true);
  }

  public static CsvSchema addMetadataColumns(CsvSchema schema, boolean extraMetadata) {
    return buildSyncFrontMetadata(extraMetadata)
        .withColumnsFrom(schema)
        .withColumnsFrom(buildSyncBackMetadata());
  }
}
