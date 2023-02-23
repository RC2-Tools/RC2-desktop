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

import edu.uw.cse.ifrcdemo.sharedlib.model.stub.Delivery;

public class CsvDelivery extends BaseSyncRow implements Delivery {
  private String authorizationId;
  private String authorizationType;
  private String beneficiaryEntityId;
  private String customDeliveryFormId;
  private String customDeliveryRowId;
  private String dateCreated;
  private String distributionName;
  private String entitlementId;
  private String memberId;
  private String itemPackDescription;
  private String itemPackId;
  private String itemPackName;

  @Override
  public String getAuthorizationId() {
    return authorizationId;
  }

  @Override
  public void setAuthorizationId(String authorizationId) {
    this.authorizationId = authorizationId;
  }

  @Override
  public String getAuthorizationType() {
    return authorizationType;
  }

  @Override
  public void setAuthorizationType(String authorizationType) {
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
  public String getCustomDeliveryFormId() {
    return customDeliveryFormId;
  }

  @Override
  public void setCustomDeliveryFormId(String customDeliveryFormId) {
    this.customDeliveryFormId = customDeliveryFormId;
  }

  @Override
  public String getCustomDeliveryRowId() {
    return customDeliveryRowId;
  }

  @Override
  public void setCustomDeliveryRowId(String customDeliveryRowId) {
    this.customDeliveryRowId = customDeliveryRowId;
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
  public String getEntitlementId() {
    return entitlementId;
  }

  @Override
  public void setEntitlementId(String entitlementId) {
    this.entitlementId = entitlementId;
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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    CsvDelivery that = (CsvDelivery) o;

    if (getAuthorizationId() != null ? !getAuthorizationId().equals(that.getAuthorizationId()) : that.getAuthorizationId() != null)
      return false;
    if (getAuthorizationType() != null ? !getAuthorizationType().equals(that.getAuthorizationType()) : that.getAuthorizationType() != null)
      return false;
    if (getBeneficiaryEntityId() != null ? !getBeneficiaryEntityId().equals(that.getBeneficiaryEntityId()) : that.getBeneficiaryEntityId() != null)
      return false;
    if (getCustomDeliveryFormId() != null ? !getCustomDeliveryFormId().equals(that.getCustomDeliveryFormId()) : that.getCustomDeliveryFormId() != null)
      return false;
    if (getCustomDeliveryRowId() != null ? !getCustomDeliveryRowId().equals(that.getCustomDeliveryRowId()) : that.getCustomDeliveryRowId() != null)
      return false;
    if (getDateCreated() != null ? !getDateCreated().equals(that.getDateCreated()) : that.getDateCreated() != null)
      return false;
    if (getDistributionName() != null ? !getDistributionName().equals(that.getDistributionName()) : that.getDistributionName() != null)
      return false;
    if (getEntitlementId() != null ? !getEntitlementId().equals(that.getEntitlementId()) : that.getEntitlementId() != null)
      return false;
    if (getMemberId() != null ? !getMemberId().equals(that.getMemberId()) : that.getMemberId() != null) return false;
    if (getItemPackDescription() != null ? !getItemPackDescription().equals(that.getItemPackDescription()) : that.getItemPackDescription() != null)
      return false;
    if (getItemPackId() != null ? !getItemPackId().equals(that.getItemPackId()) : that.getItemPackId() != null)
      return false;
    return getItemPackName() != null ? getItemPackName().equals(that.getItemPackName()) : that.getItemPackName() == null;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (getAuthorizationId() != null ? getAuthorizationId().hashCode() : 0);
    result = 31 * result + (getAuthorizationType() != null ? getAuthorizationType().hashCode() : 0);
    result = 31 * result + (getBeneficiaryEntityId() != null ? getBeneficiaryEntityId().hashCode() : 0);
    result = 31 * result + (getCustomDeliveryFormId() != null ? getCustomDeliveryFormId().hashCode() : 0);
    result = 31 * result + (getCustomDeliveryRowId() != null ? getCustomDeliveryRowId().hashCode() : 0);
    result = 31 * result + (getDateCreated() != null ? getDateCreated().hashCode() : 0);
    result = 31 * result + (getDistributionName() != null ? getDistributionName().hashCode() : 0);
    result = 31 * result + (getEntitlementId() != null ? getEntitlementId().hashCode() : 0);
    result = 31 * result + (getMemberId() != null ? getMemberId().hashCode() : 0);
    result = 31 * result + (getItemPackDescription() != null ? getItemPackDescription().hashCode() : 0);
    result = 31 * result + (getItemPackId() != null ? getItemPackId().hashCode() : 0);
    result = 31 * result + (getItemPackName() != null ? getItemPackName().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "CsvDelivery{" +
        "authorizationId='" + authorizationId + '\'' +
        ", authorizationType='" + authorizationType + '\'' +
        ", beneficiaryEntityId='" + beneficiaryEntityId + '\'' +
        ", customDeliveryFormId='" + customDeliveryFormId + '\'' +
        ", customDeliveryRowId='" + customDeliveryRowId + '\'' +
        ", dateCreated='" + dateCreated + '\'' +
        ", distributionName='" + distributionName + '\'' +
        ", entitlementId='" + entitlementId + '\'' +
        ", memberId='" + memberId + '\'' +
        ", itemPackDescription='" + itemPackDescription + '\'' +
        ", itemPackId='" + itemPackId + '\'' +
        ", itemPackName='" + itemPackName + '\'' +
        '}';
  }
}
