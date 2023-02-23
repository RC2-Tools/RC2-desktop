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

import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.BeneficiaryEntityStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.BeneficiaryEntity;

public class CsvBeneficiaryEntity extends BaseSyncRow implements BeneficiaryEntity {
  private String beneficiaryEntityId;
  private String customBeneficiaryEntityFormId;
  private String customBeneficiaryEntityRowId;
  private String dateCreated;
  private BeneficiaryEntityStatus status;
  private String statusReason;

  @Override
  public String getBeneficiaryEntityId() {
    return beneficiaryEntityId;
  }

  @Override
  public void setBeneficiaryEntityId(String beneficiaryEntityId) {
    this.beneficiaryEntityId = beneficiaryEntityId;
  }

  @Override
  public String getCustomBeneficiaryEntityFormId() {
    return customBeneficiaryEntityFormId;
  }

  @Override
  public void setCustomBeneficiaryEntityFormId(String customBeneficiaryEntityFormId) {
    this.customBeneficiaryEntityFormId = customBeneficiaryEntityFormId;
  }

  @Override
  public String getCustomBeneficiaryEntityRowId() {
    return customBeneficiaryEntityRowId;
  }

  @Override
  public void setCustomBeneficiaryEntityRowId(String customBeneficiaryEntityRowId) {
    this.customBeneficiaryEntityRowId = customBeneficiaryEntityRowId;
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
  public BeneficiaryEntityStatus getStatus() {
    return status;
  }

  @Override
  public void setStatus(BeneficiaryEntityStatus status) {
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

    CsvBeneficiaryEntity that = (CsvBeneficiaryEntity) o;

    if (getBeneficiaryEntityId() != null ? !getBeneficiaryEntityId().equals(that.getBeneficiaryEntityId()) : that.getBeneficiaryEntityId() != null)
      return false;
    if (getCustomBeneficiaryEntityFormId() != null ? !getCustomBeneficiaryEntityFormId().equals(that.getCustomBeneficiaryEntityFormId()) : that.getCustomBeneficiaryEntityFormId() != null)
      return false;
    if (getCustomBeneficiaryEntityRowId() != null ? !getCustomBeneficiaryEntityRowId().equals(that.getCustomBeneficiaryEntityRowId()) : that.getCustomBeneficiaryEntityRowId() != null)
      return false;
    if (getDateCreated() != null ? !getDateCreated().equals(that.getDateCreated()) : that.getDateCreated() != null)
      return false;
    if (getStatus() != null ? !getStatus().equals(that.getStatus()) : that.getStatus() != null) return false;
    return getStatusReason() != null ? getStatusReason().equals(that.getStatusReason()) : that.getStatusReason() == null;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (getBeneficiaryEntityId() != null ? getBeneficiaryEntityId().hashCode() : 0);
    result = 31 * result + (getCustomBeneficiaryEntityFormId() != null ? getCustomBeneficiaryEntityFormId().hashCode() : 0);
    result = 31 * result + (getCustomBeneficiaryEntityRowId() != null ? getCustomBeneficiaryEntityRowId().hashCode() : 0);
    result = 31 * result + (getDateCreated() != null ? getDateCreated().hashCode() : 0);
    result = 31 * result + (getStatus() != null ? getStatus().hashCode() : 0);
    result = 31 * result + (getStatusReason() != null ? getStatusReason().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "CsvBeneficiaryEntity{" +
        "beneficiaryEntityId='" + beneficiaryEntityId + '\'' +
        ", customBeneficiaryEntityFormId='" + customBeneficiaryEntityFormId + '\'' +
        ", customBeneficiaryEntityRowId='" + customBeneficiaryEntityRowId + '\'' +
        ", dateCreated='" + dateCreated + '\'' +
        ", status='" + status + '\'' +
        ", statusReason='" + statusReason + '\'' +
        "} " + super.toString();
  }
}
