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

package edu.uw.cse.ifrcdemo.distplan.ui.report;

import java.util.List;
import java.util.Map;

public class ReportDistributionPlanFormModel {
  private String distributionName;
  private String distributionRowId;
  private String registrationMode;
  private String workflowMode;
  private String registrationForm;
  private String distributionForm;
  private String location;

  private Integer totalItemsDistributed;
  private Integer totalBeneficiaryEntities;
  private Integer totalIndividuals;

  private List<ReportItemSummary> reportItemSummaryList;
  private List<ReportDistribution> reportDistributions;
  private List<ReportEntityItems> reportEntityItemsList;

  private Map<String, Integer> femaleAgeDistribution;
  private Map<String, Integer> maleAgeDistribution;
  private Map<String, Integer> totalAgeDistribution;

  private Map<String, Integer> reachedFemaleAgeDistribution;
  private Map<String, Integer> reachedMaleAgeDistribution;
  private Map<String, Integer> reachedTotalAgeDistribution;

  private Map<String, Integer> absentFemaleAgeDistribution;
  private Map<String, Integer> absentMaleAgeDistribution;
  private Map<String, Integer> absentTotalAgeDistribution;

  private ReportDeliveriesFormData reportDeliveriesFormData;

  public String getDistributionName() {
    return distributionName;
  }

  public void setDistributionName(String distributionName) {
    this.distributionName = distributionName;
  }

  public String getDistributionRowId() {
    return distributionRowId;
  }

  public void setDistributionRowId(String distributionRowId) {
    this.distributionRowId = distributionRowId;
  }

  public String getRegistrationMode() {
    return registrationMode;
  }

  public void setRegistrationMode(String registrationMode) {
    this.registrationMode = registrationMode;
  }

  public String getWorkflowMode() {
    return workflowMode;
  }

  public void setWorkflowMode(String workflowMode) {
    this.workflowMode = workflowMode;
  }

  public String getRegistrationForm() {
    return registrationForm;
  }

  public void setRegistrationForm(String registrationForm) {
    this.registrationForm = registrationForm;
  }

  public String getDistributionForm() {
    return distributionForm;
  }

  public void setDistributionForm(String distributionForm) {
    this.distributionForm = distributionForm;
  }

  public List<ReportItemSummary> getReportItemSummaryList() {
    return reportItemSummaryList;
  }

  public void setReportItemSummaryList(List<ReportItemSummary> reportItemSummaryList) {
    this.reportItemSummaryList = reportItemSummaryList;
  }

  public Map<String, Integer> getFemaleAgeDistribution() {
    return femaleAgeDistribution;
  }

  public void setFemaleAgeDistribution(Map<String, Integer> femaleAgeDistribution) {
    this.femaleAgeDistribution = femaleAgeDistribution;
  }

  public Map<String, Integer> getMaleAgeDistribution() {
    return maleAgeDistribution;
  }

  public void setMaleAgeDistribution(Map<String, Integer> maleAgeDistribution) {
    this.maleAgeDistribution = maleAgeDistribution;
  }

  public Map<String, Integer> getTotalAgeDistribution() {
    return totalAgeDistribution;
  }

  public void setTotalAgeDistribution(Map<String, Integer> totalAgeDistribution) {
    this.totalAgeDistribution = totalAgeDistribution;
  }

  public List<ReportDistribution> getReportDistributions() {
    return reportDistributions;
  }

  public void setReportDistributions(List<ReportDistribution> reportDistributions) {
    this.reportDistributions = reportDistributions;
  }

  public List<ReportEntityItems> getReportEntityItemsList() {
    return reportEntityItemsList;
  }

  public void setReportEntityItemsList(List<ReportEntityItems> reportEntityItemsList) {
    this.reportEntityItemsList = reportEntityItemsList;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public Map<String, Integer> getReachedFemaleAgeDistribution() {
    return reachedFemaleAgeDistribution;
  }

  public void setReachedFemaleAgeDistribution(Map<String, Integer> reachedFemaleAgeDistribution) {
    this.reachedFemaleAgeDistribution = reachedFemaleAgeDistribution;
  }

  public Map<String, Integer> getReachedMaleAgeDistribution() {
    return reachedMaleAgeDistribution;
  }

  public void setReachedMaleAgeDistribution(Map<String, Integer> reachedMaleAgeDistribution) {
    this.reachedMaleAgeDistribution = reachedMaleAgeDistribution;
  }

  public Map<String, Integer> getReachedTotalAgeDistribution() {
    return reachedTotalAgeDistribution;
  }

  public void setReachedTotalAgeDistribution(Map<String, Integer> reachedTotalAgeDistribution) {
    this.reachedTotalAgeDistribution = reachedTotalAgeDistribution;
  }

  public Map<String, Integer> getAbsentFemaleAgeDistribution() {
    return absentFemaleAgeDistribution;
  }

  public void setAbsentFemaleAgeDistribution(Map<String, Integer> absentFemaleAgeDistribution) {
    this.absentFemaleAgeDistribution = absentFemaleAgeDistribution;
  }

  public Map<String, Integer> getAbsentMaleAgeDistribution() {
    return absentMaleAgeDistribution;
  }

  public void setAbsentMaleAgeDistribution(Map<String, Integer> absentMaleAgeDistribution) {
    this.absentMaleAgeDistribution = absentMaleAgeDistribution;
  }

  public Map<String, Integer> getAbsentTotalAgeDistribution() {
    return absentTotalAgeDistribution;
  }

  public void setAbsentTotalAgeDistribution(Map<String, Integer> absentTotalAgeDistribution) {
    this.absentTotalAgeDistribution = absentTotalAgeDistribution;
  }

  public Integer getTotalItemsDistributed() {
    return totalItemsDistributed;
  }

  public void setTotalItemsDistributed(Integer totalItemsDistributed) {
    this.totalItemsDistributed = totalItemsDistributed;
  }

  public Integer getTotalBeneficiaryEntities() {
    return totalBeneficiaryEntities;
  }

  public void setTotalBeneficiaryEntities(Integer totalBeneficiaryEntities) {
    this.totalBeneficiaryEntities = totalBeneficiaryEntities;
  }

  public Integer getTotalIndividuals() {
    return totalIndividuals;
  }

  public void setTotalIndividuals(Integer totalIndividuals) {
    this.totalIndividuals = totalIndividuals;
  }

  public ReportDeliveriesFormData getReportDeliveriesFormData() {
    return reportDeliveriesFormData;
  }

  public void setReportDeliveriesFormData(ReportDeliveriesFormData reportDeliveriesFormData) {
    this.reportDeliveriesFormData = reportDeliveriesFormData;
  }

  @Override
  public String toString() {
    return "ReportDistributionPlanFormModel{" + "distributionName='" + distributionName + '\''
        + ", distributionRowId='" + distributionRowId + '\'' + ", registrationMode='"
        + registrationMode + '\'' + ", workflowMode='" + workflowMode + '\''
        + ", registrationForm='" + registrationForm + '\'' + ", distributionForm='"
        + distributionForm + '\'' + ", region='" + location + '\'' + ", totalItemsDistributed="
        + totalItemsDistributed + ", totalBeneficiaryEntities=" + totalBeneficiaryEntities
        + ", totalIndividuals=" + totalIndividuals + ", reportItemSummaryList="
        + reportItemSummaryList + ", reportDistributions=" + reportDistributions
        + ", reportEntityItemsList=" + reportEntityItemsList + ", femaleAgeDistribution="
        + femaleAgeDistribution + ", maleAgeDistribution=" + maleAgeDistribution
        + ", totalAgeDistribution=" + totalAgeDistribution + ", reachedFemaleAgeDistribution="
        + reachedFemaleAgeDistribution + ", reachedMaleAgeDistribution="
        + reachedMaleAgeDistribution + ", reachedTotalAgeDistribution="
        + reachedTotalAgeDistribution + ", absentFemaleAgeDistribution="
        + absentFemaleAgeDistribution + ", absentMaleAgeDistribution=" + absentMaleAgeDistribution
        + ", absentTotalAgeDistribution=" + absentTotalAgeDistribution
        + ", reportDeliveriesFormData=" + reportDeliveriesFormData + '}';
  }
}
