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

package edu.uw.cse.ifrcdemo.sharedlib.model.row;

import edu.uw.cse.ifrcdemo.sharedlib.model.stub.SyncRow;
import org.opendatakit.aggregate.odktables.rest.entity.RowFilterScope;

public class BaseSyncRow implements SyncRow {
  private String rowId;
  private String rowETag;
  private String dataETagAtModification;
  private boolean deleted;
  private String createUser;
  private String lastUpdateUser;
  private String formId;
  private String locale;
  private String savepointType;
  private String savepointTimestamp;
  private String savepointCreator;
  private RowFilterScope rowFilterScope;

  @Override
  public String getRowId() {
    return rowId;
  }

  @Override
  public void setRowId(String rowId) {
    this.rowId = rowId;
  }

  @Override
  public String getRowETag() {
    return rowETag;
  }

  @Override
  public void setRowETag(String rowETag) {
    this.rowETag = rowETag;
  }

  @Override
  public String getDataETagAtModification() {
    return dataETagAtModification;
  }

  @Override
  public void setDataETagAtModification(String dataETagAtModification) {
    this.dataETagAtModification = dataETagAtModification;
  }

  @Override
  public boolean getDeleted() {
    return deleted;
  }

  @Override
  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  @Override
  public String getCreateUser() {
    return createUser;
  }

  @Override
  public void setCreateUser(String createUser) {
    this.createUser = createUser;
  }

  @Override
  public String getLastUpdateUser() {
    return lastUpdateUser;
  }

  @Override
  public void setLastUpdateUser(String lastUpdateUser) {
    this.lastUpdateUser = lastUpdateUser;
  }

  @Override
  public String getFormId() {
    return formId;
  }

  @Override
  public void setFormId(String formId) {
    this.formId = formId;
  }

  @Override
  public String getLocale() {
    return locale;
  }

  @Override
  public void setLocale(String locale) {
    this.locale = locale;
  }

  @Override
  public String getSavepointType() {
    return savepointType;
  }

  @Override
  public void setSavepointType(String savepointType) {
    this.savepointType = savepointType;
  }

  @Override
  public String getSavepointTimestamp() {
    return savepointTimestamp;
  }

  @Override
  public void setSavepointTimestamp(String savepointTimestamp) {
    this.savepointTimestamp = savepointTimestamp;
  }

  @Override
  public String getSavepointCreator() {
    return savepointCreator;
  }

  @Override
  public void setSavepointCreator(String savepointCreator) {
    this.savepointCreator = savepointCreator;
  }

  @Override
  public RowFilterScope getRowFilterScope() {
    return rowFilterScope;
  }

  @Override
  public void setRowFilterScope(RowFilterScope rowFilterScope) {
    this.rowFilterScope = rowFilterScope;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    BaseSyncRow that = (BaseSyncRow) o;

    if (getDeleted() != that.getDeleted()) return false;
    if (getRowId() != null ? !getRowId().equals(that.getRowId()) : that.getRowId() != null) return false;
    if (getRowETag() != null ? !getRowETag().equals(that.getRowETag()) : that.getRowETag() != null) return false;
    if (getDataETagAtModification() != null ? !getDataETagAtModification().equals(that.getDataETagAtModification()) : that.getDataETagAtModification() != null)
      return false;
    if (getCreateUser() != null ? !getCreateUser().equals(that.getCreateUser()) : that.getCreateUser() != null)
      return false;
    if (getLastUpdateUser() != null ? !getLastUpdateUser().equals(that.getLastUpdateUser()) : that.getLastUpdateUser() != null)
      return false;
    if (getFormId() != null ? !getFormId().equals(that.getFormId()) : that.getFormId() != null) return false;
    if (getLocale() != null ? !getLocale().equals(that.getLocale()) : that.getLocale() != null) return false;
    if (getSavepointType() != null ? !getSavepointType().equals(that.getSavepointType()) : that.getSavepointType() != null)
      return false;
    if (getSavepointTimestamp() != null ? !getSavepointTimestamp().equals(that.getSavepointTimestamp()) : that.getSavepointTimestamp() != null)
      return false;
    if (getSavepointCreator() != null ? !getSavepointCreator().equals(that.getSavepointCreator()) : that.getSavepointCreator() != null)
      return false;
    return getRowFilterScope() != null ? getRowFilterScope().equals(that.getRowFilterScope()) : that.getRowFilterScope() == null;
  }

  @Override
  public int hashCode() {
    int result = getRowId() != null ? getRowId().hashCode() : 0;
    result = 31 * result + (getRowETag() != null ? getRowETag().hashCode() : 0);
    result = 31 * result + (getDataETagAtModification() != null ? getDataETagAtModification().hashCode() : 0);
    result = 31 * result + (getDeleted() ? 1 : 0);
    result = 31 * result + (getCreateUser() != null ? getCreateUser().hashCode() : 0);
    result = 31 * result + (getLastUpdateUser() != null ? getLastUpdateUser().hashCode() : 0);
    result = 31 * result + (getFormId() != null ? getFormId().hashCode() : 0);
    result = 31 * result + (getLocale() != null ? getLocale().hashCode() : 0);
    result = 31 * result + (getSavepointType() != null ? getSavepointType().hashCode() : 0);
    result = 31 * result + (getSavepointTimestamp() != null ? getSavepointTimestamp().hashCode() : 0);
    result = 31 * result + (getSavepointCreator() != null ? getSavepointCreator().hashCode() : 0);
    result = 31 * result + (getRowFilterScope() != null ? getRowFilterScope().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "BaseSyncRow{" +
        "rowId='" + rowId + '\'' +
        ", rowETag='" + rowETag + '\'' +
        ", dataETagAtModification='" + dataETagAtModification + '\'' +
        ", deleted=" + deleted +
        ", createUser='" + createUser + '\'' +
        ", lastUpdateUser='" + lastUpdateUser + '\'' +
        ", formId='" + formId + '\'' +
        ", locale='" + locale + '\'' +
        ", savepointType='" + savepointType + '\'' +
        ", savepointTimestamp='" + savepointTimestamp + '\'' +
        ", savepointCreator='" + savepointCreator + '\'' +
        ", rowFilterScope=" + rowFilterScope +
        '}';
  }
}
