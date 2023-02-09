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

import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.DistVisitProgStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.Distribution;

public class CsvDistribution extends BaseSyncRow implements Distribution {
  private String dateCreated;
  private String description;
  private String name;
  private DistVisitProgStatus status;
  private String statusReason;
  private String locationId;
  private String locationName;
  private String locationDescription;
  private String summaryVersion;
  private String summaryFormId;
  private String summaryTableId;

  @Override
  public String getDateCreated() {
    return dateCreated;
  }

  @Override
  public void setDateCreated(String dateCreated) {
    this.dateCreated = dateCreated;
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
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public DistVisitProgStatus getStatus() {
    return status;
  }

  public void setStatus(DistVisitProgStatus status) {
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
  public String getLocationDescription() {
    return locationDescription;
  }

  @Override
  public void setLocationDescription(String locationDescription) {
    this.locationDescription = locationDescription;
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
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    if (!super.equals(o))
      return false;

    CsvDistribution that = (CsvDistribution) o;

    if (getDateCreated() != null ?
        !getDateCreated().equals(that.getDateCreated()) :
        that.getDateCreated() != null)
      return false;
    if (getDescription() != null ?
        !getDescription().equals(that.getDescription()) :
        that.getDescription() != null)
      return false;
    if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null)
      return false;
    if (getStatus() != null ? !getStatus().equals(that.getStatus()) : that.getStatus() != null)
      return false;
    if (getStatusReason() != null ?
        !getStatusReason().equals(that.getStatusReason()) :
        that.getStatusReason() != null)
      return false;
    if (getLocationId() != null ?
        !getLocationId().equals(that.getLocationId()) :
        that.getLocationId() != null)
      return false;
    if (getLocationName() != null ?
        !getLocationName().equals(that.getLocationName()) :
        that.getLocationName() != null)
      return false;
    if (getLocationDescription() != null ?
        !getLocationDescription().equals(that.getLocationDescription()) :
        that.getLocationDescription() != null)
      return false;
    if (getSummaryVersion() != null ?
        !getSummaryVersion().equals(that.getSummaryVersion()) :
        that.getSummaryVersion() != null)
      return false;
    if (getSummaryFormId() != null ?
        !getSummaryFormId().equals(that.getSummaryFormId()) :
        that.getSummaryFormId() != null)
      return false;
    return getSummaryTableId() != null ?
        getSummaryTableId().equals(that.getSummaryTableId()) :
        that.getSummaryTableId() == null;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (getDateCreated() != null ? getDateCreated().hashCode() : 0);
    result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
    result = 31 * result + (getName() != null ? getName().hashCode() : 0);
    result = 31 * result + (getStatus() != null ? getStatus().hashCode() : 0);
    result = 31 * result + (getStatusReason() != null ? getStatusReason().hashCode() : 0);
    result = 31 * result + (getLocationId() != null ? getLocationId().hashCode() : 0);
    result = 31 * result + (getLocationName() != null ? getLocationName().hashCode() : 0);
    result =
        31 * result + (getLocationDescription() != null ? getLocationDescription().hashCode() : 0);
    result = 31 * result + (getSummaryVersion() != null ? getSummaryVersion().hashCode() : 0);
    result = 31 * result + (getSummaryFormId() != null ? getSummaryFormId().hashCode() : 0);
    result = 31 * result + (getSummaryTableId() != null ? getSummaryTableId().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "CsvDistribution{" + "dateCreated='" + dateCreated + '\'' + ", description='"
        + description + '\'' + ", name='" + name + '\'' + ", status='" + status + '\''
        + ", statusReason='" + statusReason + '\'' + ", locationId='" + locationId + '\''
        + ", locationName='" + locationName + '\'' + ", locationDescription='" + locationDescription
        + '\'' + ", summaryVersion='" + summaryVersion + '\'' + ", summaryFormId='" + summaryFormId
        + '\'' + ", summaryTableId='" + summaryTableId + '\'' + '}';
  }
}
