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

import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.DistVisitProgStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.VisitProgram;

public class CsvVisitProgram extends BaseSyncRow implements VisitProgram {
  private String customVisitFormId;
  private String customVisitTableId;
  private String dateCreated;
  private String description;
  private String forMember;
  private String name;
  private String locationId;
  private String locationName;
  private String locationDescription;
  private DistVisitProgStatus status;
  private String statusReason;

  @Override
  public String getCustomVisitFormId() {
    return customVisitFormId;
  }

  @Override
  public void setCustomVisitFormId(String customVisitFormId) {
    this.customVisitFormId = customVisitFormId;
  }

  @Override
  public String getCustomVisitTableId() {
    return customVisitTableId;
  }

  @Override
  public void setCustomVisitTableId(String customVisitTableId) {
    this.customVisitTableId = customVisitTableId;
  }

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
  public String getForMember() {
    return forMember;
  }

  @Override
  public void setForMember(String forMember) {
    this.forMember = forMember;
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
  public String getLocationId() { return locationId; }

  @Override
  public void setLocationId(String locationId) { this.locationId = locationId; }

  @Override
  public String getLocationName() { return locationName; }

  @Override
  public void setLocationName(String locationName) { this.locationName = locationName; }

  @Override
  public String getLocationDescription() { return locationDescription; }

  @Override
  public void setLocationDescription(String locationDescription) {
    this.locationDescription = locationDescription;
  }

  @Override
  public DistVisitProgStatus getStatus() {
    return status;
  }

  @Override
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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    CsvVisitProgram that = (CsvVisitProgram) o;

    if (customVisitFormId != null ? !customVisitFormId.equals(that.customVisitFormId) : that.customVisitFormId != null)
      return false;
    if (customVisitTableId != null ? !customVisitTableId.equals(that.customVisitTableId) : that.customVisitTableId != null)
      return false;
    if (dateCreated != null ? !dateCreated.equals(that.dateCreated) : that.dateCreated != null) return false;
    if (description != null ? !description.equals(that.description) : that.description != null) return false;
    if (forMember != null ? !forMember.equals(that.forMember) : that.forMember != null) return false;
    if (name != null ? !name.equals(that.name) : that.name != null) return false;
    if (locationId != null ? !locationId.equals(that.locationId) : that.locationId != null) return false;
    if (locationName != null ? !locationName.equals(that.locationName) : that.locationName != null) return false;
    if (locationDescription != null ? !locationDescription.equals(that.locationDescription) : that.locationDescription != null)
      return false;
    if (status != that.status) return false;
    return statusReason != null ? statusReason.equals(that.statusReason) : that.statusReason == null;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (customVisitFormId != null ? customVisitFormId.hashCode() : 0);
    result = 31 * result + (customVisitTableId != null ? customVisitTableId.hashCode() : 0);
    result = 31 * result + (dateCreated != null ? dateCreated.hashCode() : 0);
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + (forMember != null ? forMember.hashCode() : 0);
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (locationId != null ? locationId.hashCode() : 0);
    result = 31 * result + (locationName != null ? locationName.hashCode() : 0);
    result = 31 * result + (locationDescription != null ? locationDescription.hashCode() : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
    result = 31 * result + (statusReason != null ? statusReason.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "CsvVisitProgram{" +
        "customVisitFormId='" + customVisitFormId + '\'' +
        ", customVisitTableId='" + customVisitTableId + '\'' +
        ", dateCreated='" + dateCreated + '\'' +
        ", description='" + description + '\'' +
        ", forMember='" + forMember + '\'' +
        ", name='" + name + '\'' +
        ", locationId='" + locationId + '\'' +
        ", locationName='" + locationName + '\'' +
        ", locationDescription='" + locationDescription + '\'' +
        ", status=" + status +
        ", statusReason='" + statusReason + '\'' +
        "} " + super.toString();
  }
}
