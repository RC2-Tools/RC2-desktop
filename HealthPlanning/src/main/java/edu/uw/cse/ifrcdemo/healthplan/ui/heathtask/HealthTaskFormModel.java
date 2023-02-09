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

package edu.uw.cse.ifrcdemo.healthplan.ui.heathtask;

import edu.uw.cse.ifrcdemo.healthplan.ui.healthservice.HealthServiceListModel;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.form.XlsxForm;
import java.util.List;
import javax.validation.constraints.NotEmpty;

public class HealthTaskFormModel {
  private String id;
  @NotEmpty
  private String name;
  private String description;
  private String startDate;
  private String endDate;
  private String status;
  private String statusReason;
  @NotEmpty
  private String locationId;
  private String locationName;
  private String summaryVersion;
  private XlsxForm summaryForm;
  private String distributionId;
  private String funderId;

  private List<HealthServiceListModel> healthServiceList;

  public List<HealthServiceListModel> getHealthServiceList() {
    return healthServiceList;
  }

  public void setHealthServiceList(List<HealthServiceListModel> healthServiceList) {
    this.healthServiceList = healthServiceList;
  }



  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getStartDate() {
    return startDate;
  }

  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }

  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getStatusReason() {
    return statusReason;
  }

  public void setStatusReason(String statusReason) {
    this.statusReason = statusReason;
  }

  public String getLocationId() {
    return locationId;
  }

  public void setLocationId(String locationId) {
    this.locationId = locationId;
  }

  public String getLocationName() {
    return locationName;
  }

  public void setLocationName(String locationName) {
    this.locationName = locationName;
  }

  public String getSummaryVersion() {
    return summaryVersion;
  }

  public void setSummaryVersion(String summaryVersion) {
    this.summaryVersion = summaryVersion;
  }

  public XlsxForm getSummaryForm() {
    return summaryForm;
  }

  public void setSummaryForm(XlsxForm summaryForm) {
    this.summaryForm = summaryForm;
  }

  public String getDistributionId() {
    return distributionId;
  }

  public void setDistributionId(String distributionId) {
    this.distributionId = distributionId;
  }

  public String getFunderId() {
    return funderId;
  }

  public void setFunderId(String funderId) {
    this.funderId = funderId;
  }

}
