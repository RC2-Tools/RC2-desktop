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

package edu.uw.cse.ifrcdemo.distplan.ui.visit;

import edu.uw.cse.ifrcdemo.distplan.model.GenerateBy;
import edu.uw.cse.ifrcdemo.distplan.model.form.XlsxForm;
import edu.uw.cse.ifrcdemo.distplan.ui.demographics.DemographicsModel;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class VisitFormModel {
  @NotEmpty
  private String name;
  private String description;

  @NotEmpty
  private String locationId;
  private String locationName;

  @NotNull
  private XlsxForm visitForm;
  private GenerateBy generateBy;

  //    public List<List<AuthorizationCriterion>> criteriaRules; // TODO: type

  // TODO: Decide if there are common abstractions here with authorizations
  private String beneficiaryIdRange;
  private String beneficiaryCriteria;

  private DemographicsModel demographicsModel;

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

  public XlsxForm getVisitForm() {
    return visitForm;
  }

  public void setVisitForm(XlsxForm visitForm) {
    this.visitForm = visitForm;
  }

  public String getBeneficiaryIdRange() {
    return beneficiaryIdRange;
  }

  public void setBeneficiaryIdRange(String beneficiaryIdRange) {
    this.beneficiaryIdRange = beneficiaryIdRange;
  }

  public String getBeneficiaryCriteria() {
    return beneficiaryCriteria;
  }

  public void setBeneficiaryCriteria(String beneficiaryCriteria) {
    this.beneficiaryCriteria = beneficiaryCriteria;
  }

  public GenerateBy getGenerateBy() {
    return generateBy;
  }

  public void setGenerateBy(GenerateBy generateBy) {
    this.generateBy = generateBy;
  }

  public DemographicsModel getDemographicsModel() {
    return demographicsModel;
  }

  public void setDemographicsModel(DemographicsModel demographicsModel) {
    this.demographicsModel = demographicsModel;
  }

  @Override
  public String toString() {
    return "VisitFormModel{" +
        "name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", locationId='" + locationId + '\'' +
        ", locationName='" + locationName + '\'' +
        ", visitForm=" + visitForm +
        ", generateBy=" + generateBy +
        ", beneficiaryIdRange='" + beneficiaryIdRange + '\'' +
        ", beneficiaryCriteria='" + beneficiaryCriteria + '\'' +
        '}';
  }
}
