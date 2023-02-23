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

import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationType;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.EntitlementStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.Entitlement;

public class CsvEntitlement extends BaseSyncRow implements Entitlement {
  private String assignedItemPackCode;
  private String authorizationId;
  private AuthorizationType authorizationType;
  private String beneficiaryEntityId;
  private String dateCreated;
  private String distributionName;
  private String memberId;
  private String isOverride;
  private String itemPackDescription;
  private String itemPackId;
  private String itemPackName;
  private EntitlementStatus status;
  private String statusReason;

  @Override
  public String getAssignedItemPackCode() {
    return assignedItemPackCode;
  }

  @Override
  public void setAssignedItemPackCode(String assignedItemPackCode) {
    this.assignedItemPackCode = assignedItemPackCode;
  }

  @Override
  public String getAuthorizationId() {
    return authorizationId;
  }

  @Override
  public void setAuthorizationId(String authorizationId) {
    this.authorizationId = authorizationId;
  }

  @Override
  public AuthorizationType getAuthorizationType() {
    return authorizationType;
  }

  @Override
  public void setAuthorizationType(AuthorizationType authorizationType) {
    this.authorizationType = authorizationType;
  }

  @Override
  public String getBeneficiaryEntityId() {
    return beneficiaryEntityId;
  }

  @Override
  public void setBeneficiaryEntityId(String beneficiaryEntityId) {
    this.beneficiaryEntityId = beneficiaryEntityId;
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
  public String getDistributionName() {
    return distributionName;
  }

  @Override
  public void setDistributionName(String distributionName) {
    this.distributionName = distributionName;
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
  public String getIsOverride() {
    return isOverride;
  }

  @Override
  public void setIsOverride(String isOverride) {
    this.isOverride = isOverride;
  }

  @Override
  public String getItemPackDescription() {
    return itemPackDescription;
  }

  @Override
  public void setItemPackDescription(String itemPackDescription) {
    this.itemPackDescription = itemPackDescription;
  }

  @Override
  public String getItemPackId() {
    return itemPackId;
  }

  @Override
  public void setItemPackId(String itemPackId) {
    this.itemPackId = itemPackId;
  }

  @Override
  public String getItemPackName() {
    return itemPackName;
  }

  @Override
  public void setItemPackName(String itemPackName) {
    this.itemPackName = itemPackName;
  }

  @Override
  public EntitlementStatus getStatus() {
    return status;
  }

  @Override
  public void setStatus(EntitlementStatus status) {
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

    CsvEntitlement that = (CsvEntitlement) o;

    if (getAssignedItemPackCode() != null ? !getAssignedItemPackCode().equals(that.getAssignedItemPackCode()) : that.getAssignedItemPackCode() != null)
      return false;
    if (getAuthorizationId() != null ? !getAuthorizationId().equals(that.getAuthorizationId()) : that.getAuthorizationId() != null)
      return false;
    if (getAuthorizationType() != that.getAuthorizationType()) return false;
    if (getBeneficiaryEntityId() != null ? !getBeneficiaryEntityId().equals(that.getBeneficiaryEntityId()) : that.getBeneficiaryEntityId() != null)
      return false;
    if (getDateCreated() != null ? !getDateCreated().equals(that.getDateCreated()) : that.getDateCreated() != null)
      return false;
    if (getDistributionName() != null ? !getDistributionName().equals(that.getDistributionName()) : that.getDistributionName() != null)
      return false;
    if (getMemberId() != null ? !getMemberId().equals(that.getMemberId()) : that.getMemberId() != null) return false;
    if (getIsOverride() != null ? !getIsOverride().equals(that.getIsOverride()) : that.getIsOverride() != null)
      return false;
    if (getItemPackDescription() != null ? !getItemPackDescription().equals(that.getItemPackDescription()) : that.getItemPackDescription() != null)
      return false;
    if (getItemPackId() != null ? !getItemPackId().equals(that.getItemPackId()) : that.getItemPackId() != null)
      return false;
    if (getItemPackName() != null ? !getItemPackName().equals(that.getItemPackName()) : that.getItemPackName() != null)
      return false;
    if (getStatus() != that.getStatus()) return false;
    return getStatusReason() != null ? getStatusReason().equals(that.getStatusReason()) : that.getStatusReason() == null;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (getAssignedItemPackCode() != null ? getAssignedItemPackCode().hashCode() : 0);
    result = 31 * result + (getAuthorizationId() != null ? getAuthorizationId().hashCode() : 0);
    result = 31 * result + (getAuthorizationType() != null ? getAuthorizationType().hashCode() : 0);
    result = 31 * result + (getBeneficiaryEntityId() != null ? getBeneficiaryEntityId().hashCode() : 0);
    result = 31 * result + (getDateCreated() != null ? getDateCreated().hashCode() : 0);
    result = 31 * result + (getDistributionName() != null ? getDistributionName().hashCode() : 0);
    result = 31 * result + (getMemberId() != null ? getMemberId().hashCode() : 0);
    result = 31 * result + (getIsOverride() != null ? getIsOverride().hashCode() : 0);
    result = 31 * result + (getItemPackDescription() != null ? getItemPackDescription().hashCode() : 0);
    result = 31 * result + (getItemPackId() != null ? getItemPackId().hashCode() : 0);
    result = 31 * result + (getItemPackName() != null ? getItemPackName().hashCode() : 0);
    result = 31 * result + (getStatus() != null ? getStatus().hashCode() : 0);
    result = 31 * result + (getStatusReason() != null ? getStatusReason().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "CsvEntitlement{" +
        "assignedItemPackCode='" + assignedItemPackCode + '\'' +
        ", authorizationId='" + authorizationId + '\'' +
        ", authorizationType=" + authorizationType +
        ", beneficiaryEntityId='" + beneficiaryEntityId + '\'' +
        ", dateCreated='" + dateCreated + '\'' +
        ", distributionDescription='" + distributionName + '\'' +
        ", memberId='" + memberId + '\'' +
        ", isOverride='" + isOverride + '\'' +
        ", itemPackDescription='" + itemPackDescription + '\'' +
        ", itemPackId='" + itemPackId + '\'' +
        ", itemPackName='" + itemPackName + '\'' +
        ", status=" + status +
        ", statusReason='" + statusReason + '\'' +
        '}';
  }
}
