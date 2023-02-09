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

import edu.uw.cse.ifrcdemo.sharedlib.model.stub.Visit;
import java.util.Objects;

public class CsvVisit extends BaseSyncRow implements Visit {
  private String beneficiaryUnitId;
  private String customVisitFormId;
  private String customVisitRowId;
  private String customVisitTableId;
  private String memberId;
  private String visitProgramId;

  @Override
  public String getBeneficiaryUnitId() {
    return beneficiaryUnitId;
  }

  @Override
  public void setBeneficiaryUnitId(String beneficiaryUnitId) {
    this.beneficiaryUnitId = beneficiaryUnitId;
  }

  @Override
  public String getCustomVisitFormId() {
    return customVisitFormId;
  }

  @Override
  public void setCustomVisitFormId(String customVisitFormId) {
    this.customVisitFormId = customVisitFormId;
  }

  @Override
  public String getCustomVisitRowId() {
    return customVisitRowId;
  }

  @Override
  public void setCustomVisitRowId(String customVisitRowId) {
    this.customVisitRowId = customVisitRowId;
  }

  @Override
  public String getCustomVisitTableId() {
    return customVisitTableId;
  }

  @Override
  public void setCustomVisitTableId(String customVisitTableId) {
    this.customVisitTableId = customVisitTableId;
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
  public String getVisitProgramId() {
    return visitProgramId;
  }

  @Override
  public void setVisitProgramId(String visitProgramId) {
    this.visitProgramId = visitProgramId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    CsvVisit csvVisit = (CsvVisit) o;
    return Objects.equals(getBeneficiaryUnitId(), csvVisit.getBeneficiaryUnitId()) &&
        Objects.equals(getCustomVisitFormId(), csvVisit.getCustomVisitFormId()) &&
        Objects.equals(getCustomVisitRowId(), csvVisit.getCustomVisitRowId()) &&
        Objects.equals(getCustomVisitTableId(), csvVisit.getCustomVisitTableId()) &&
        Objects.equals(getMemberId(), csvVisit.getMemberId()) &&
        Objects.equals(getVisitProgramId(), csvVisit.getVisitProgramId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getBeneficiaryUnitId(), getCustomVisitFormId(),
        getCustomVisitRowId(), getCustomVisitTableId(), getMemberId(), getVisitProgramId());
  }

  @Override
  public String toString() {
    return "CsvVisit{" +
        "beneficiaryUnitId='" + beneficiaryUnitId + '\'' +
        ", customVisitFormId='" + customVisitFormId + '\'' +
        ", customVisitRowId='" + customVisitRowId + '\'' +
        ", customVisitTableId='" + customVisitTableId + '\'' +
        ", memberId='" + memberId + '\'' +
        ", visitProgramId='" + visitProgramId + '\'' +
        '}';
  }
}
