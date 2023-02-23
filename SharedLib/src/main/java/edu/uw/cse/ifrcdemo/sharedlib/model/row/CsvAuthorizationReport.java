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

import edu.uw.cse.ifrcdemo.sharedlib.model.stub.AuthorizationReport;

public class CsvAuthorizationReport extends BaseSyncRow implements AuthorizationReport {
  private String authorizationId;
  private String reportVersion;
  private String summaryFormId;
  private String summaryRowId;
  private String user;

  @Override
  public String getAuthorizationId() {
    return authorizationId;
  }

  @Override
  public void setAuthorizationId(String authorizationId) {
    this.authorizationId = authorizationId;
  }

  @Override
  public String getReportVersion() {
    return reportVersion;
  }

  @Override
  public void setReportVersion(String reportVersion) {
    this.reportVersion = reportVersion;
  }

  @Override
  public String getSummaryFormId() {
    return summaryFormId;
  }

  @Override
  public void setSummaryFormId(String summaryFormId) {
    this.summaryFormId = summaryFormId;
  }

  @Override
  public String getSummaryRowId() {
    return summaryRowId;
  }

  @Override
  public void setSummaryRowId(String summaryRowId) {
    this.summaryRowId = summaryRowId;
  }

  @Override
  public String getUser() {
    return user;
  }

  @Override
  public void setUser(String user) {
    this.user = user;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    CsvAuthorizationReport that = (CsvAuthorizationReport) o;

    if (getAuthorizationId() != null ? !getAuthorizationId().equals(that.getAuthorizationId()) : that.getAuthorizationId() != null)
      return false;
    if (getReportVersion() != null ? !getReportVersion().equals(that.getReportVersion()) : that.getReportVersion() != null) return false;
    if (getSummaryFormId() != null ? !getSummaryFormId().equals(that.getSummaryFormId()) : that.getSummaryFormId() != null)
      return false;
    if (getSummaryRowId() != null ? !getSummaryRowId().equals(that.getSummaryRowId()) : that.getSummaryRowId() != null)
      return false;
    return getUser() != null ? getUser().equals(that.getUser()) : that.getUser() == null;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (getAuthorizationId() != null ? getAuthorizationId().hashCode() : 0);
    result = 31 * result + (getReportVersion() != null ? getReportVersion().hashCode() : 0);
    result = 31 * result + (getSummaryFormId() != null ? getSummaryFormId().hashCode() : 0);
    result = 31 * result + (getSummaryRowId() != null ? getSummaryRowId().hashCode() : 0);
    result = 31 * result + (getUser() != null ? getUser().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "CsvAuthorizationReport{" +
        "authorizationId='" + authorizationId + '\'' +
        ", reportVersion='" + reportVersion + '\'' +
        ", summaryFormId='" + summaryFormId + '\'' +
        ", summaryRowId='" + summaryRowId + '\'' +
        ", user='" + user + '\'' +
        "} " + super.toString();
  }
}
