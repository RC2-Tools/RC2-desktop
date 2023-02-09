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

package edu.uw.cse.ifrcdemo.distreport.model;

import edu.uw.cse.ifrcdemo.distreport.util.AttachmentUtil;
import edu.uw.cse.ifrcdemo.sharedlib.consts.csv.MobileDbConsts;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.UntypedSyncRow;
import org.opendatakit.aggregate.odktables.rest.TableConstants;
import org.opendatakit.aggregate.odktables.rest.entity.RowFilterScope;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public class ReportUntypedSyncRow extends UntypedSyncRow {
  private final UntypedSyncRow row;
  private final String tableId;
  private final Path dataPath;

  private final Function<String, String> attachment;
  private final String parsed_date_created;

  public UntypedSyncRow getRow() {
    return row;
  }

  public String getTableId() {
    return tableId;
  }

  public Path getDataPath() {
    return dataPath;
  }

  public Function<String, String> getAttachment() {
    return attachment;
  }

  public String getParsed_date_created() {
    return parsed_date_created;
  }

  public ReportUntypedSyncRow(UntypedSyncRow row, String tableId, Path dataPath) {
    this.row = row;
    this.tableId = tableId;
    this.dataPath = dataPath;
    this.attachment = this::attachment;
    Long timestamp = TableConstants.milliSecondsFromNanos(getColumns().get(MobileDbConsts.DATE_CREATED_COLUMN), Locale.ROOT);
    this.parsed_date_created = timestamp != null ? Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME) : null;
  }

  @Override
  public Map<String, String> getColumns() {
    return getRow().getColumns();
  }

  @Override
  public String getRowId() {
    return getRow().getRowId();
  }

  @Override
  public void setRowId(String rowId) {
    getRow().setRowId(rowId);
  }

  @Override
  public String getRowETag() {
    return getRow().getRowETag();
  }

  @Override
  public void setRowETag(String rowETag) {
    getRow().setRowETag(rowETag);
  }

  @Override
  public String getDataETagAtModification() {
    return getRow().getDataETagAtModification();
  }

  @Override
  public void setDataETagAtModification(String dataETagAtModification) {
    getRow().setDataETagAtModification(dataETagAtModification);
  }

  @Override
  public boolean getDeleted() {
    return getRow().getDeleted();
  }

  @Override
  public void setDeleted(boolean deleted) {
    getRow().setDeleted(deleted);
  }

  @Override
  public String getCreateUser() {
    return getRow().getCreateUser();
  }

  @Override
  public void setCreateUser(String createUser) {
    getRow().setCreateUser(createUser);
  }

  @Override
  public String getLastUpdateUser() {
    return getRow().getLastUpdateUser();
  }

  @Override
  public void setLastUpdateUser(String lastUpdateUser) {
    getRow().setLastUpdateUser(lastUpdateUser);
  }

  @Override
  public String getFormId() {
    return getRow().getFormId();
  }

  @Override
  public void setFormId(String formId) {
    getRow().setFormId(formId);
  }

  @Override
  public String getLocale() {
    return getRow().getLocale();
  }

  @Override
  public void setLocale(String locale) {
    getRow().setLocale(locale);
  }

  @Override
  public String getSavepointType() {
    return getRow().getSavepointType();
  }

  @Override
  public void setSavepointType(String savepointType) {
    getRow().setSavepointType(savepointType);
  }

  @Override
  public String getSavepointTimestamp() {
    return getRow().getSavepointTimestamp();
  }

  @Override
  public void setSavepointTimestamp(String savepointTimestamp) {
    getRow().setSavepointTimestamp(savepointTimestamp);
  }

  @Override
  public String getSavepointCreator() {
    return getRow().getSavepointCreator();
  }

  @Override
  public void setSavepointCreator(String savepointCreator) {
    getRow().setSavepointCreator(savepointCreator);
  }

  @Override
  public RowFilterScope getRowFilterScope() {
    return getRow().getRowFilterScope();
  }

  @Override
  public void setRowFilterScope(RowFilterScope rowFilterScope) {
    getRow().setRowFilterScope(rowFilterScope);
  }

  private String attachment(String column) {
    try {
      return AttachmentUtil.toDataUri(getDataPath(), getTableId(), getRowId(), getColumns().get(column + MobileDbConsts.URI_FRAGMENT_COLUMN_SUFFIX), getColumns().get(column + MobileDbConsts.CONTENT_TYPE_COLUMN_SUFFIX));
    } catch (Exception e) {
      return null;
    }
  }

}
