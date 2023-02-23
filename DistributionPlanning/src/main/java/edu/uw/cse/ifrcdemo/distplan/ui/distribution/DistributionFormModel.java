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

package edu.uw.cse.ifrcdemo.distplan.ui.distribution;

import edu.uw.cse.ifrcdemo.distplan.model.form.XlsxForm;
import edu.uw.cse.ifrcdemo.distplan.ui.authorization.AuthorizationFormModel;
import edu.uw.cse.ifrcdemo.distplan.ui.demographics.DemographicsModel;

import javax.validation.constraints.NotEmpty;
import java.util.List;

public class DistributionFormModel {
  @NotEmpty
  private String name;

  private String description;

  @NotEmpty
  private String locationId;

  private String locationName;

  private XlsxForm fieldSummaryReport;

  @NotEmpty(groups = FinalDistributionCheck.class, message = "{NoItemInDistribution}")
  private List<AuthorizationFormModel> authorizations;

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

  public XlsxForm getFieldSummaryReport() {
    return fieldSummaryReport;
  }

  public void setFieldSummaryReport(XlsxForm fieldSummaryReport) {
    this.fieldSummaryReport = fieldSummaryReport;
  }

  public List<AuthorizationFormModel> getAuthorizations() {
    return authorizations;
  }

  public void setAuthorizations(List<AuthorizationFormModel> authorizations) {
    this.authorizations = authorizations;
  }

  public DemographicsModel getDemographicsModel() {
    return demographicsModel;
  }

  public void setDemographicsModel(DemographicsModel demographicsModel) {
    this.demographicsModel = demographicsModel;
  }

  @Override
  public String toString() {
    return "DistributionFormModel{" +
        "name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", locationId='" + locationId + '\'' +
        ", locationName='" + locationName + '\'' +
        ", fieldSummaryReport=" + fieldSummaryReport +
        ", authorizations=" + authorizations +
        ", demographicsModel=" + demographicsModel +
        '}';
  }
}
