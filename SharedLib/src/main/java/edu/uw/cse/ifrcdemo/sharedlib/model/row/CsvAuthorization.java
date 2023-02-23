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

import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationType;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.ExtraFieldEntitlements;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.Authorization;

public class CsvAuthorization extends BaseSyncRow implements Authorization {
  private String customDeliveryFormId;
  private String dateCreated;
  private ExtraFieldEntitlements extraFieldEntitlements;
  private String forMember;
  private String itemPackDescription;
  private String itemPackId;
  private String itemPackName;
  private String itemPackRanges;
  private AuthorizationStatus status;
  private String statusReason;
  private AuthorizationType type;
  private String distributionId;
  private String distributionName;

  @Override
  public String getCustomDeliveryFormId() {
    return customDeliveryFormId;
  }

  @Override
  public void setCustomDeliveryFormId(String customDeliveryFormId) {
    this.customDeliveryFormId = customDeliveryFormId;
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
  public ExtraFieldEntitlements getExtraFieldEntitlements() {
    return extraFieldEntitlements;
  }

  @Override
  public void setExtraFieldEntitlements(ExtraFieldEntitlements mode) {
    this.extraFieldEntitlements = mode;
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
  public String getItemPackRanges() {
    return itemPackRanges;
  }

  @Override
  public void setItemPackRanges(String itemPackRanges) {
    this.itemPackRanges = itemPackRanges;
  }

  @Override
  public AuthorizationStatus getStatus() {
    return status;
  }

  @Override
  public void setStatus(AuthorizationStatus status) {
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
  public AuthorizationType getType() {
    return type;
  }

  @Override
  public void setType(AuthorizationType type) {
    this.type = type;
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
  public String getDistributionName() {
    return distributionName;
  }

  @Override
  public void setDistributionName(String distributionName) {
    this.distributionName = distributionName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    CsvAuthorization that = (CsvAuthorization) o;

    if (getCustomDeliveryFormId() != null ? !getCustomDeliveryFormId().equals(that.getCustomDeliveryFormId()) : that.getCustomDeliveryFormId() != null)
      return false;
    if (getDateCreated() != null ? !getDateCreated().equals(that.getDateCreated()) : that.getDateCreated() != null)
      return false;
    if (getExtraFieldEntitlements() != that.getExtraFieldEntitlements()) return false;
    if (getForMember() != null ? !getForMember().equals(that.getForMember()) : that.getForMember() != null)
      return false;
    if (getItemPackDescription() != null ? !getItemPackDescription().equals(that.getItemPackDescription()) : that.getItemPackDescription() != null)
      return false;
    if (getItemPackId() != null ? !getItemPackId().equals(that.getItemPackId()) : that.getItemPackId() != null)
      return false;
    if (getItemPackName() != null ? !getItemPackName().equals(that.getItemPackName()) : that.getItemPackName() != null)
      return false;
    if (getItemPackRanges() != null ? !getItemPackRanges().equals(that.getItemPackRanges()) : that.getItemPackRanges() != null)
      return false;
    if (getStatus() != that.getStatus()) return false;
    if (getStatusReason() != null ? !getStatusReason().equals(that.getStatusReason()) : that.getStatusReason() != null)
      return false;
    if (getType() != that.getType()) return false;
    if (getDistributionId() != null ? !getDistributionId().equals(that.getDistributionId()) : that.getDistributionId() != null)
      return false;
    return getDistributionName() != null ? getDistributionName().equals(that.getDistributionName()) : that.getDistributionName() == null;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (getCustomDeliveryFormId() != null ? getCustomDeliveryFormId().hashCode() : 0);
    result = 31 * result + (getDateCreated() != null ? getDateCreated().hashCode() : 0);
    result = 31 * result + (getExtraFieldEntitlements() != null ? getExtraFieldEntitlements().hashCode() : 0);
    result = 31 * result + (getForMember() != null ? getForMember().hashCode() : 0);
    result = 31 * result + (getItemPackDescription() != null ? getItemPackDescription().hashCode() : 0);
    result = 31 * result + (getItemPackId() != null ? getItemPackId().hashCode() : 0);
    result = 31 * result + (getItemPackName() != null ? getItemPackName().hashCode() : 0);
    result = 31 * result + (getItemPackRanges() != null ? getItemPackRanges().hashCode() : 0);
    result = 31 * result + (getStatus() != null ? getStatus().hashCode() : 0);
    result = 31 * result + (getStatusReason() != null ? getStatusReason().hashCode() : 0);
    result = 31 * result + (getType() != null ? getType().hashCode() : 0);
    result = 31 * result + (getDistributionId() != null ? getDistributionId().hashCode() : 0);
    result = 31 * result + (getDistributionName() != null ? getDistributionName().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "CsvAuthorization{" +
        "customDeliveryFormId='" + customDeliveryFormId + '\'' +
        ", dateCreated='" + dateCreated + '\'' +
        ", extraFieldEntitlements=" + extraFieldEntitlements +
        ", forMember='" + forMember + '\'' +
        ", itemPackDescription='" + itemPackDescription + '\'' +
        ", itemPackId='" + itemPackId + '\'' +
        ", itemPackName='" + itemPackName + '\'' +
        ", itemPackRanges='" + itemPackRanges + '\'' +
        ", status=" + status +
        ", statusReason='" + statusReason + '\'' +
        ", type=" + type +
        ", distributionId='" + distributionId + '\'' +
        ", distributionName='" + distributionName + '\'' +
        '}';
  }
}
