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

import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.IndividualStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.Individual;

public class CsvIndividual extends BaseSyncRow implements Individual {
  private String beneficiaryEntityRowId;
  private String beneficiaryEntityStatus;
  private String dateCreated;
  private String customMemberFormId;
  private String customMemberRowId;
  private String memberId;
  private IndividualStatus status;
  private String statusReason;

  @Override
  public String getBeneficiaryEntityRowId() {
    return beneficiaryEntityRowId;
  }

  @Override
  public void setBeneficiaryEntityRowId(String beneficiaryEntityRowId) {
    this.beneficiaryEntityRowId = beneficiaryEntityRowId;
  }

  @Override
  public String getBeneficiaryEntityStatus() {
    return beneficiaryEntityStatus;
  }

  @Override
  public void setBeneficiaryEntityStatus(String beneficiaryEntityStatus) {
    this.beneficiaryEntityStatus = beneficiaryEntityStatus;
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
  public String getCustomMemberFormId() {
    return customMemberFormId;
  }

  @Override
  public void setCustomMemberFormId(String customMemberFormId) {
    this.customMemberFormId = customMemberFormId;
  }

  @Override
  public String getCustomMemberRowId() {
    return customMemberRowId;
  }

  @Override
  public void setCustomMemberRowId(String customMemberRowId) {
    this.customMemberRowId = customMemberRowId;
  }

  @Override
  public String getMemberId() {
    return memberId;
  }

  @Override
  public void setMemberId(String memberId) {
    this.memberId = memberId;
  }

  @Override
  public IndividualStatus getStatus() {
    return status;
  }

  @Override
  public void setStatus(IndividualStatus status) {
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

    CsvIndividual that = (CsvIndividual) o;

    if (getBeneficiaryEntityRowId() != null ? !getBeneficiaryEntityRowId().equals(that.getBeneficiaryEntityRowId()) : that.getBeneficiaryEntityRowId() != null)
      return false;
    if (getBeneficiaryEntityStatus() != null ? !getBeneficiaryEntityStatus().equals(that.getBeneficiaryEntityStatus()) : that.getBeneficiaryEntityStatus() != null)
      return false;
    if (getDateCreated() != null ? !getDateCreated().equals(that.getDateCreated()) : that.getDateCreated() != null)
      return false;
    if (getCustomMemberFormId() != null ? !getCustomMemberFormId().equals(that.getCustomMemberFormId()) : that.getCustomMemberFormId() != null)
      return false;
    if (getCustomMemberRowId() != null ? !getCustomMemberRowId().equals(that.getCustomMemberRowId()) : that.getCustomMemberRowId() != null)
      return false;
    if (getMemberId() != null ? !getMemberId().equals(that.getMemberId()) : that.getMemberId() != null)
      return false;
    if (getStatus() != null ? !getStatus().equals(that.getStatus()) : that.getStatus() != null) return false;
    return getStatusReason() != null ? getStatusReason().equals(that.getStatusReason()) : that.getStatusReason() == null;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (getBeneficiaryEntityRowId() != null ? getBeneficiaryEntityRowId().hashCode() : 0);
    result = 31 * result + (getBeneficiaryEntityStatus() != null ? getBeneficiaryEntityStatus().hashCode() : 0);
    result = 31 * result + (getDateCreated() != null ? getDateCreated().hashCode() : 0);
    result = 31 * result + (getCustomMemberFormId() != null ? getCustomMemberFormId().hashCode() : 0);
    result = 31 * result + (getCustomMemberRowId() != null ? getCustomMemberRowId().hashCode() : 0);
    result = 31 * result + (getMemberId() != null ? getMemberId().hashCode() : 0);
    result = 31 * result + (getStatus() != null ? getStatus().hashCode() : 0);
    result = 31 * result + (getStatusReason() != null ? getStatusReason().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "CsvIndividual{" +
        "beneficiaryEntityRowId='" + beneficiaryEntityRowId + '\'' +
        ", beneficiaryEntityStatus='" + beneficiaryEntityStatus + '\'' +
        ", dateCreated='" + dateCreated + '\'' +
        ", customMemberFormId='" + customMemberFormId + '\'' +
        ", customMemberRowId='" + customMemberRowId + '\'' +
        ", memberId='" + memberId + '\'' +
        ", status='" + status + '\'' +
        ", statusReason='" + statusReason + '\'' +
        "} " + super.toString();
  }
}
