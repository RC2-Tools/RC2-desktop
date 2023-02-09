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

import edu.uw.cse.ifrcdemo.sharedlib.model.stub.Referral;

public class CsvReferral extends BaseSyncRow implements Referral {
  private String beneficiaryEntityId;
  private String dateIssued;
  private String notes;
  private String serviceId;
  private String status;
  private String statusDateUpdated;
  private String statusReason;
  private String customReferralFormId;
  private String customReferralTableId;
  private String customReferralRowId;

  @Override
  public String getBeneficiaryEntityId() {
    return beneficiaryEntityId;
  }

  @Override
  public void setBeneficiaryEntityId(String beneficiaryEntityId) {
    this.beneficiaryEntityId = beneficiaryEntityId;
  }

  @Override
  public String getDateIssued() {
    return dateIssued;
  }

  @Override
  public void setDateIssued(String dateIssued) {
    this.dateIssued = dateIssued;
  }

  @Override
  public String getNotes() {
    return notes;
  }

  @Override
  public void setNotes(String notes) {
    this.notes = notes;
  }

  @Override
  public String getServiceId() {
    return serviceId;
  }

  @Override
  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
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
  public String getStatusDateUpdated() {
    return statusDateUpdated;
  }

  @Override
  public void setStatusDateUpdated(String statusDateUpdated) {
    this.statusDateUpdated = statusDateUpdated;
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
  public String getCustomReferralFormId() {
    return customReferralFormId;
  }

  @Override
  public void setCustomReferralFormId(String customReferralFormId) {
    this.customReferralFormId = customReferralFormId;
  }

  @Override
  public String getCustomReferralTableId() {
    return customReferralTableId;
  }

  @Override
  public void setCustomReferralTableId(String customReferralTableId) {
    this.customReferralTableId = customReferralTableId;
  }

  @Override
  public String getCustomReferralRowId() {
    return customReferralRowId;
  }

  @Override
  public void setCustomReferralRowId(String customReferralRowId) {
    this.customReferralRowId = customReferralRowId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    CsvReferral that = (CsvReferral) o;

    if (beneficiaryEntityId != null ? !beneficiaryEntityId.equals(that.beneficiaryEntityId) : that.beneficiaryEntityId != null)
      return false;
    if (dateIssued != null ? !dateIssued.equals(that.dateIssued) : that.dateIssued != null) return false;
    if (notes != null ? !notes.equals(that.notes) : that.notes != null) return false;
    if (serviceId != null ? !serviceId.equals(that.serviceId) : that.serviceId != null) return false;
    if (status != null ? !status.equals(that.status) : that.status != null) return false;
    if (statusDateUpdated != null ? !statusDateUpdated.equals(that.statusDateUpdated) : that.statusDateUpdated != null)
      return false;
    if (statusReason != null ? !statusReason.equals(that.statusReason) : that.statusReason != null) return false;
    if (customReferralFormId != null ? !customReferralFormId.equals(that.customReferralFormId) : that.customReferralFormId != null)
      return false;
    if (customReferralTableId != null ? !customReferralTableId.equals(that.customReferralTableId) : that.customReferralTableId != null)
      return false;
    return customReferralRowId != null ? customReferralRowId.equals(that.customReferralRowId) : that.customReferralRowId == null;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (beneficiaryEntityId != null ? beneficiaryEntityId.hashCode() : 0);
    result = 31 * result + (dateIssued != null ? dateIssued.hashCode() : 0);
    result = 31 * result + (notes != null ? notes.hashCode() : 0);
    result = 31 * result + (serviceId != null ? serviceId.hashCode() : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
    result = 31 * result + (statusDateUpdated != null ? statusDateUpdated.hashCode() : 0);
    result = 31 * result + (statusReason != null ? statusReason.hashCode() : 0);
    result = 31 * result + (customReferralFormId != null ? customReferralFormId.hashCode() : 0);
    result = 31 * result + (customReferralTableId != null ? customReferralTableId.hashCode() : 0);
    result = 31 * result + (customReferralRowId != null ? customReferralRowId.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "CsvReferral{" +
        "beneficiaryEntityId='" + beneficiaryEntityId + '\'' +
        ", dateIssued='" + dateIssued + '\'' +
        ", notes='" + notes + '\'' +
        ", serviceId='" + serviceId + '\'' +
        ", status='" + status + '\'' +
        ", statusDateUpdated='" + statusDateUpdated + '\'' +
        ", statusReason='" + statusReason + '\'' +
        ", customReferralFormId='" + customReferralFormId + '\'' +
        ", customReferralTableId='" + customReferralTableId + '\'' +
        ", customReferralRowId='" + customReferralRowId + '\'' +
        "} " + super.toString();
  }
}
