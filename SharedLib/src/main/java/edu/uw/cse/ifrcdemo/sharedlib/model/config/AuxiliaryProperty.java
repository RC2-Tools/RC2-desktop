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

package edu.uw.cse.ifrcdemo.sharedlib.model.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationType;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.Module;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.RegistrationMode;

public class AuxiliaryProperty {
  private static final String MODULE = "MODULE";
  private static final String REGISTRATION_MODE = "REGISTRATION_MODE";
  private static final String WORKFLOW_MODE = "WORKFLOW_MODE";
  private static final String BENEFICIARY_ENTITY_CUSTOM_FORM_ID = "BENEFICIARY_ENTITY_CUSTOM_FORM_ID";
  private static final String MEMBER_CUSTOM_FORM_ID = "MEMBER_CUSTOM_FORM_ID";
  private static final String CUSTOM_BENEFICIARY_ROW_ID_COLUMN = "CUSTOM_BENEFICIARY_ROW_ID_COLUMN";

  private Module module;
  private RegistrationMode registrationMode;
  private AuthorizationType workflowMode;
  private String beneficiaryEntityCustomFormId;
  private String memberCustomFormId;
  private String customBeneficiaryRowIdColumn;

  @JsonProperty(MODULE)
  public Module getModule() {
    return module;
  }

  public void setModule(Module registrationMode) {
    this.module = registrationMode;
  }

  @JsonProperty(REGISTRATION_MODE)
  public RegistrationMode getRegistrationMode() {
    return registrationMode;
  }

  public void setRegistrationMode(RegistrationMode registrationMode) {
    this.registrationMode = registrationMode;
  }

  @JsonProperty(WORKFLOW_MODE)
  public AuthorizationType getWorkflowMode() {
    return workflowMode;
  }

  public void setWorkflowMode(AuthorizationType workflowMode) {
    this.workflowMode = workflowMode;
  }

  @JsonProperty(BENEFICIARY_ENTITY_CUSTOM_FORM_ID)
  public String getBeneficiaryEntityCustomFormId() {
    return beneficiaryEntityCustomFormId;
  }

  public void setBeneficiaryEntityCustomFormId(String beneficiaryEntityCustomFormId) {
    this.beneficiaryEntityCustomFormId = beneficiaryEntityCustomFormId;
  }

  @JsonProperty(MEMBER_CUSTOM_FORM_ID)
  public String getMemberCustomFormId() {
    return memberCustomFormId;
  }

  public void setMemberCustomFormId(String memberCustomFormId) {
    this.memberCustomFormId = memberCustomFormId;
  }

  @JsonProperty(CUSTOM_BENEFICIARY_ROW_ID_COLUMN)
  public String getCustomBeneficiaryRowIdColumn() {
    return customBeneficiaryRowIdColumn;
  }

  public void setCustomBeneficiaryRowIdColumn(String customBeneficiaryRowIdColumn) {
    this.customBeneficiaryRowIdColumn = customBeneficiaryRowIdColumn;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AuxiliaryProperty that = (AuxiliaryProperty) o;

    if (getRegistrationMode() != that.getRegistrationMode()) return false;
    if (getWorkflowMode() != that.getWorkflowMode()) return false;
    if (getBeneficiaryEntityCustomFormId() != null ? !getBeneficiaryEntityCustomFormId().equals(that.getBeneficiaryEntityCustomFormId()) : that.getBeneficiaryEntityCustomFormId() != null)
      return false;
    if (getMemberCustomFormId() != null ? !getMemberCustomFormId().equals(that.getMemberCustomFormId()) : that.getMemberCustomFormId() != null)
      return false;
    return getCustomBeneficiaryRowIdColumn() != null ? getCustomBeneficiaryRowIdColumn().equals(that.getCustomBeneficiaryRowIdColumn()) : that.getCustomBeneficiaryRowIdColumn() == null;
  }

  @Override
  public int hashCode() {
    int result = getRegistrationMode() != null ? getRegistrationMode().hashCode() : 0;
    result = 31 * result + (getWorkflowMode() != null ? getWorkflowMode().hashCode() : 0);
    result = 31 * result + (getBeneficiaryEntityCustomFormId() != null ? getBeneficiaryEntityCustomFormId().hashCode() : 0);
    result = 31 * result + (getMemberCustomFormId() != null ? getMemberCustomFormId().hashCode() : 0);
    result = 31 * result + (getCustomBeneficiaryRowIdColumn() != null ? getCustomBeneficiaryRowIdColumn().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "AuxiliaryProperty{" +
        "module=" + registrationMode +
        ", registrationMode=" + registrationMode +
        ", workflowMode=" + workflowMode +
        ", beneficiaryEntityCustomFormId='" + beneficiaryEntityCustomFormId + '\'' +
        ", memberCustomFormId='" + memberCustomFormId + '\'' +
        ", customBeneficiaryRowIdColumn='" + customBeneficiaryRowIdColumn + '\'' +
        '}';
  }
}
