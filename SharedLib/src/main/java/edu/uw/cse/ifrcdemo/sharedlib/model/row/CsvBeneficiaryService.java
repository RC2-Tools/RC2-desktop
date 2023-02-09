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

import edu.uw.cse.ifrcdemo.sharedlib.model.stub.BeneficiaryService;

public class CsvBeneficiaryService extends BaseSyncRow implements BeneficiaryService {
  private String beneficiaryEntityId;
  private String programId;
  private String serviceId;
  private String customFormId;
  private String customTableId;
  private String customFormInstanceId;
  private String datePerformed;

  @Override
  public String getBeneficiaryEntityId() {
    return beneficiaryEntityId;
  }

  @Override
  public void setBeneficiaryEntityId(String beneficiaryEntityId) {
    this.beneficiaryEntityId = beneficiaryEntityId;
  }

  @Override
  public String getProgramId() {
    return programId;
  }

  @Override
  public void setProgramId(String programId) {
    this.programId = programId;
  }

  @Override
  public String getServiceId() {
    return serviceId;
  }

  @Override
  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  @Override
  public String getCustomFormId() {
    return customFormId;
  }

  @Override
  public void setCustomFormId(String customFormId) {
    this.customFormId = customFormId;
  }

  @Override
  public String getCustomTableId() {
    return customTableId;
  }

  @Override
  public void setCustomTableId(String customTableId) {
    this.customTableId = customTableId;
  }

  @Override
  public String getCustomFormInstanceId() {
    return customFormInstanceId;
  }

  @Override
  public void setCustomFormInstanceId(String customFormInstanceId) {
    this.customFormInstanceId = customFormInstanceId;
  }

  @Override
  public String getDatePerformed() {
    return datePerformed;
  }

  @Override
  public void setDatePerformed(String datePerformed) {
    this.datePerformed = datePerformed;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    CsvBeneficiaryService that = (CsvBeneficiaryService) o;

    if (beneficiaryEntityId != null ? !beneficiaryEntityId.equals(that.beneficiaryEntityId) : that.beneficiaryEntityId != null)
      return false;
    if (programId != null ? !programId.equals(that.programId) : that.programId != null) return false;
    if (serviceId != null ? !serviceId.equals(that.serviceId) : that.serviceId != null) return false;
    if (customFormId != null ? !customFormId.equals(that.customFormId) : that.customFormId != null) return false;
    if (customTableId != null ? !customTableId.equals(that.customTableId) : that.customTableId != null) return false;
    if (customFormInstanceId != null ? !customFormInstanceId.equals(that.customFormInstanceId) : that.customFormInstanceId != null)
      return false;
    return datePerformed != null ? datePerformed.equals(that.datePerformed) : that.datePerformed == null;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (beneficiaryEntityId != null ? beneficiaryEntityId.hashCode() : 0);
    result = 31 * result + (programId != null ? programId.hashCode() : 0);
    result = 31 * result + (serviceId != null ? serviceId.hashCode() : 0);
    result = 31 * result + (customFormId != null ? customFormId.hashCode() : 0);
    result = 31 * result + (customTableId != null ? customTableId.hashCode() : 0);
    result = 31 * result + (customFormInstanceId != null ? customFormInstanceId.hashCode() : 0);
    result = 31 * result + (datePerformed != null ? datePerformed.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "CsvBeneficiaryService{" +
        "beneficiaryEntityId='" + beneficiaryEntityId + '\'' +
        ", programId='" + programId + '\'' +
        ", serviceId='" + serviceId + '\'' +
        ", customFormId='" + customFormId + '\'' +
        ", customTableId='" + customTableId + '\'' +
        ", customFormInstanceId='" + customFormInstanceId + '\'' +
        ", datePerformed='" + datePerformed + '\'' +
        "} " + super.toString();
  }
}
