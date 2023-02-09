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

package edu.uw.cse.ifrcdemo.sharedlib.model.row;

import edu.uw.cse.ifrcdemo.sharedlib.model.stub.Program;

public class CsvHealthTask extends BaseSyncRow implements Program {
  private String date;
  private String description;
  private String distributionId;
  private String funderId;
  private String locationId;
  private String locationName;
  private String name;
  private String status;
  private String statusReason;
  private String summaryFormId;
  private String summaryTableId;
  private String summaryVersion;

  @Override
  public String getDate() {
    return date;
  }

  @Override
  public void setDate(String date) {
    this.date = date;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String getDistributionId() {
    return distributionId;
  }

  @Override
  public void setDistributionId(String distributionId) {
    this.distributionId = distributionId;
  }

  @Override
  public String getFunderId() {
    return funderId;
  }

  @Override
  public void setFunderId(String funderId) {
    this.funderId = funderId;
  }

  @Override
  public String getLocationId() {
    return locationId;
  }

  @Override
  public void setLocationId(String locationId) {
    this.locationId = locationId;
  }

  @Override
  public String getLocationName() {
    return locationName;
  }

  @Override
  public void setLocationName(String locationName) {
    this.locationName = locationName;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getStatus() {
    return status;
  }

  @Override
  public void setStatus(String status) {
    this.status = status;
  }

  @Override
  public String getStatusReason() {
    return statusReason;
  }

  @Override
  public void setStatusReason(String statusReason) {
    this.statusReason = statusReason;
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
  public String getSummaryTableId() {
    return summaryTableId;
  }

  @Override
  public void setSummaryTableId(String summaryTableId) {
    this.summaryTableId = summaryTableId;
  }

  @Override
  public String getSummaryVersion() {
    return summaryVersion;
  }

  @Override
  public void setSummaryVersion(String summaryVersion) {
    this.summaryVersion = summaryVersion;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    CsvHealthTask that = (CsvHealthTask) o;

    if (date != null ? !date.equals(that.date) : that.date != null) return false;
    if (description != null ? !description.equals(that.description) : that.description != null) return false;
    if (distributionId != null ? !distributionId.equals(that.distributionId) : that.distributionId != null)
      return false;
    if (funderId != null ? !funderId.equals(that.funderId) : that.funderId != null) return false;
    if (locationId != null ? !locationId.equals(that.locationId) : that.locationId != null) return false;
    if (locationName != null ? !locationName.equals(that.locationName) : that.locationName != null) return false;
    if (name != null ? !name.equals(that.name) : that.name != null) return false;
    if (status != null ? !status.equals(that.status) : that.status != null) return false;
    if (statusReason != null ? !statusReason.equals(that.statusReason) : that.statusReason != null) return false;
    if (summaryFormId != null ? !summaryFormId.equals(that.summaryFormId) : that.summaryFormId != null) return false;
    if (summaryTableId != null ? !summaryTableId.equals(that.summaryTableId) : that.summaryTableId != null)
      return false;
    return summaryVersion != null ? summaryVersion.equals(that.summaryVersion) : that.summaryVersion == null;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (date != null ? date.hashCode() : 0);
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + (distributionId != null ? distributionId.hashCode() : 0);
    result = 31 * result + (funderId != null ? funderId.hashCode() : 0);
    result = 31 * result + (locationId != null ? locationId.hashCode() : 0);
    result = 31 * result + (locationName != null ? locationName.hashCode() : 0);
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
    result = 31 * result + (statusReason != null ? statusReason.hashCode() : 0);
    result = 31 * result + (summaryFormId != null ? summaryFormId.hashCode() : 0);
    result = 31 * result + (summaryTableId != null ? summaryTableId.hashCode() : 0);
    result = 31 * result + (summaryVersion != null ? summaryVersion.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "CsvProgram{" +
        "date='" + date + '\'' +
        ", description='" + description + '\'' +
        ", distributionId='" + distributionId + '\'' +
        ", funder='" + funderId + '\'' +
        ", locationId='" + locationId + '\'' +
        ", locationName='" + locationName + '\'' +
        ", name='" + name + '\'' +
        ", status='" + status + '\'' +
        ", statusReason='" + statusReason + '\'' +
        ", summaryFormId='" + summaryFormId + '\'' +
        ", summaryTableId='" + summaryTableId + '\'' +
        ", summaryVersion='" + summaryVersion + '\'' +
        "} " + super.toString();
  }
}
