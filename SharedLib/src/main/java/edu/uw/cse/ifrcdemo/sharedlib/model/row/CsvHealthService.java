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

import edu.uw.cse.ifrcdemo.sharedlib.model.stub.Service;

public class CsvHealthService extends BaseSyncRow implements Service {
  private String description;
  private String endWithReferrals;
  private String name;
  private String requiresReferral;
  private String serviceFormId;
  private String serviceTableId;
  private String referralFormId;
  private String referralTableId;

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String getEndWithReferrals() {
    return endWithReferrals;
  }

  @Override
  public void setEndWithReferrals(String endWithReferrals) {
    this.endWithReferrals = endWithReferrals;
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
  public String getRequiresReferral() {
    return requiresReferral;
  }

  @Override
  public void setRequiresReferral(String requiresReferral) {
    this.requiresReferral = requiresReferral;
  }

  @Override
  public String getServiceFormId() {
    return serviceFormId;
  }

  @Override
  public void setServiceFormId(String serviceFormId) {
    this.serviceFormId = serviceFormId;
  }

  @Override
  public String getServiceTableId() {
    return serviceTableId;
  }

  @Override
  public void setServiceTableId(String serviceTableId) {
    this.serviceTableId = serviceTableId;
  }

  @Override
  public String getReferralFormId() {
    return referralFormId;
  }

  @Override
  public void setReferralFormId(String referralFormId) {
    this.referralFormId = referralFormId;
  }

  @Override
  public String getReferralTableId() {
    return referralTableId;
  }

  @Override
  public void setReferralTableId(String referralTableId) {
    this.referralTableId = referralTableId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    CsvHealthService that = (CsvHealthService) o;

    if (description != null ? !description.equals(that.description) : that.description != null) return false;
    if (endWithReferrals != null ? !endWithReferrals.equals(that.endWithReferrals) : that.endWithReferrals != null)
      return false;
    if (name != null ? !name.equals(that.name) : that.name != null) return false;
    if (requiresReferral != null ? !requiresReferral.equals(that.requiresReferral) : that.requiresReferral != null)
      return false;
    if (serviceFormId != null ? !serviceFormId.equals(that.serviceFormId) : that.serviceFormId != null) return false;
    if (serviceTableId != null ? !serviceTableId.equals(that.serviceTableId) : that.serviceTableId != null)
      return false;
    if (referralFormId != null ? !referralFormId.equals(that.referralFormId) : that.referralFormId != null)
      return false;
    return referralTableId != null ? referralTableId.equals(that.referralTableId) : that.referralTableId == null;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + (endWithReferrals != null ? endWithReferrals.hashCode() : 0);
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (requiresReferral != null ? requiresReferral.hashCode() : 0);
    result = 31 * result + (serviceFormId != null ? serviceFormId.hashCode() : 0);
    result = 31 * result + (serviceTableId != null ? serviceTableId.hashCode() : 0);
    result = 31 * result + (referralFormId != null ? referralFormId.hashCode() : 0);
    result = 31 * result + (referralTableId != null ? referralTableId.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "CsvService{" +
        "description='" + description + '\'' +
        ", endWithReferrals='" + endWithReferrals + '\'' +
        ", name='" + name + '\'' +
        ", requiresReferral='" + requiresReferral + '\'' +
        ", serviceFormId='" + serviceFormId + '\'' +
        ", serviceTableId='" + serviceTableId + '\'' +
        ", referralFormId='" + referralFormId + '\'' +
        ", referralTableId='" + referralTableId + '\'' +
        "} " + super.toString();
  }
}
