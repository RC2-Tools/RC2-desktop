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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import edu.uw.cse.ifrcdemo.sharedlib.model.jacksonmixin.RowFilterScopeCsvMixin;
import edu.uw.cse.ifrcdemo.sharedlib.model.jacksonmixin.SuitcaseSyncRowMixin;
import edu.uw.cse.ifrcdemo.sharedlib.model.jacksonmixin.SyncRowCsvMixin;
import edu.uw.cse.ifrcdemo.sharedlib.model.jacksonmixin.SyncRowExtraCsvMixin;
import edu.uw.cse.ifrcdemo.sharedlib.model.json.JsonSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.BaseSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvSuitcaseSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.UntypedSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.ModelStub;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.SuitcaseSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.SyncRow;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.RowFilterScope;

public class CsvMapperUtil {
  public static CsvMapper getCsvMapper(boolean extraMetadata) {
    Class mixin = extraMetadata ? SyncRowExtraCsvMixin.class : SyncRowCsvMixin.class;

    return (CsvMapper) new CsvMapper()
        .enable(CsvParser.Feature.FAIL_ON_MISSING_COLUMNS)
        .enable(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS) // always quote strings to make testing easier
        .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
        .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
        .addMixIn(BaseSyncRow.class, mixin)
        .addMixIn(RowFilterScope.class, RowFilterScopeCsvMixin.class)
        .addMixIn(SuitcaseSyncRow.class, SuitcaseSyncRowMixin.class);
  }

  public static ObjectMapper getJsonMapper() {
    return getCsvMapper(true)
        .addMixIn(BaseSyncRow.class, Row.class) // override mixins with classes provided by Cloud Endpoint
        .addMixIn(RowFilterScope.class, RowFilterScope.class); // override mixins with classes provided by Cloud Endpoint
  }

  public static Row convertToRow(ObjectMapper mapper, SyncRow source) {
    JsonSyncRow row = mapper.convertValue(source, JsonSyncRow.class);
    row.setValues(Row.convertFromMap(row.columns));

    return row;
  }

  /**
   * Reader for well defined CSVs
   *
   * @param type
   * @param mapper
   * @param extraMetadata
   * @param <T> type of type
   * @return
   */
  public static <T extends BaseSyncRow> ObjectReader getReader(Class<T> type, CsvMapper mapper, boolean extraMetadata) {
    return mapper
        .readerFor(type)
        .with(CsvSchemaUtil.buildSchemaWithMetadata(type, mapper, extraMetadata));
  }

  /**
   * Reader for other CSVs.
   * {@link #getReader(Class, CsvMapper, boolean)}
   *
   * @param mapper
   * @param extraMetadata
   * @return
   */
  public static ObjectReader getReader(CsvMapper mapper, boolean extraMetadata) {
    return getReader(UntypedSyncRow.class, mapper, extraMetadata);
  }

  /**
   * Writer for well defined CSVs
   *
   * @param type
   * @param stub
   * @param mapper
   * @param extraMetadata
   * @param <U> type of stub
   * @param <T> type of type
   * @return
   */
  public static <U extends ModelStub, T extends U> ObjectWriter getWriter(Class<T> type, Class<U> stub, CsvMapper mapper, boolean extraMetadata) {
    if (type == stub) {
      throw new IllegalArgumentException("T cannot be equal to U");
    }

    return mapper
        .writerFor(type)
        .with(CsvSchemaUtil.buildSchemaWithMetadata(stub, mapper, extraMetadata));
  }

  /**
   * Writer for other CSVs
   * {@link #getWriter(Class, Class, CsvMapper, boolean)}
   *
   * @param mapper
   * @param columns
   * @param extraMetadata
   * @return
   */
  public static ObjectWriter getWriter(CsvMapper mapper, Iterable<String> columns, boolean extraMetadata) {
    return mapper
        .writerFor(UntypedSyncRow.class)
        .with(CsvSchemaUtil.buildSchemaWithMetadata(columns, extraMetadata));
  }

  /**
   * Writer for CsvSuitcaseSyncRow
   *
   * @param stub
   * @param mapper
   * @param extraMetadata
   * @param <U>
   * @return
   */
  public static <U extends ModelStub> ObjectWriter getWriterForSuitcaseSyncRow(Class<U> stub, CsvMapper mapper, boolean extraMetadata) {
    return mapper
        .writerFor(CsvSuitcaseSyncRow.class)
        .with(mapper
            .typedSchemaFor(SuitcaseSyncRow.class)
            .withColumnsFrom(CsvSchemaUtil.buildSchemaWithMetadata(stub, mapper, extraMetadata))
            .withHeader()
            .withColumnReordering(true)
        );
  }

  public static ObjectWriter getWriterForSuitcaseSyncRow(Iterable<String> columns, CsvMapper mapper, boolean extraMetadata) {
    return mapper
        .writerFor(CsvSuitcaseSyncRow.class)
        .with(mapper
            .typedSchemaFor(SuitcaseSyncRow.class)
            .withColumnsFrom(CsvSchemaUtil.buildSchemaWithMetadata(columns, extraMetadata))
            .withHeader()
            .withColumnReordering(true)
        );
  }
}
