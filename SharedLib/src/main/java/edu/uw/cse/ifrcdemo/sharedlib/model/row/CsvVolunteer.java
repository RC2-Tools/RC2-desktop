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

import edu.uw.cse.ifrcdemo.sharedlib.model.stub.Volunteer;

public class CsvVolunteer extends BaseSyncRow implements Volunteer {
  private String volunteerExternalId;
  private String dateCreated;
  private String customVolunteerFormId;
  private String customVolunteerInstanceId;
  private String status;
  private String statusReason;

  @Override
  public String getVolunteerExternalId() {
    return volunteerExternalId;
  }

  @Override
  public void setVolunteerExternalId(String volunteerExternalId) {
    this.volunteerExternalId = volunteerExternalId;
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
  public String getCustomVolunteerFormId() {
    return customVolunteerFormId;
  }

  @Override
  public void setCustomVolunteerFormId(String customVolunteerFormId) {
    this.customVolunteerFormId = customVolunteerFormId;
  }

  @Override
  public String getCustomVolunteerInstanceId() {
    return customVolunteerInstanceId;
  }

  @Override
  public void setCustomVolunteerInstanceId(String customVolunteerInstanceId) {
    this.customVolunteerInstanceId = customVolunteerInstanceId;
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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    CsvVolunteer that = (CsvVolunteer) o;

    if (volunteerExternalId != null ? !volunteerExternalId.equals(that.volunteerExternalId) : that.volunteerExternalId != null)
      return false;
    if (dateCreated != null ? !dateCreated.equals(that.dateCreated) : that.dateCreated != null) return false;
    if (customVolunteerFormId != null ? !customVolunteerFormId.equals(that.customVolunteerFormId) : that.customVolunteerFormId != null)
      return false;
    if (customVolunteerInstanceId != null ? !customVolunteerInstanceId.equals(that.customVolunteerInstanceId) : that.customVolunteerInstanceId != null)
      return false;
    if (status != null ? !status.equals(that.status) : that.status != null) return false;
    return statusReason != null ? statusReason.equals(that.statusReason) : that.statusReason == null;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (volunteerExternalId != null ? volunteerExternalId.hashCode() : 0);
    result = 31 * result + (dateCreated != null ? dateCreated.hashCode() : 0);
    result = 31 * result + (customVolunteerFormId != null ? customVolunteerFormId.hashCode() : 0);
    result = 31 * result + (customVolunteerInstanceId != null ? customVolunteerInstanceId.hashCode() : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
    result = 31 * result + (statusReason != null ? statusReason.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "CsvVolunteer{" +
        "volunteerExternalId='" + volunteerExternalId + '\'' +
        ", dateCreated='" + dateCreated + '\'' +
        ", customVolunteerFormId='" + customVolunteerFormId + '\'' +
        ", customVolunteerInstanceId='" + customVolunteerInstanceId + '\'' +
        ", status='" + status + '\'' +
        ", statusReason='" + statusReason + '\'' +
        "} " + super.toString();
  }
}
