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

package edu.uw.cse.ifrcdemo.distplan.ui.authorization;

import edu.uw.cse.ifrcdemo.distplan.model.GenerateBy;
import edu.uw.cse.ifrcdemo.distplan.model.authorization.Range;
import edu.uw.cse.ifrcdemo.distplan.model.criteria.AuthorizationCriterion;
import edu.uw.cse.ifrcdemo.distplan.model.form.XlsxForm;
import edu.uw.cse.ifrcdemo.distplan.ui.demographics.DemographicsModel;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.ExtraFieldEntitlements;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

public class AuthorizationFormModel {
  private String id;

  @NotEmpty
  private String itemId;

  private String itemName;

  private XlsxForm deliveryForm;

  @NotNull
  private ExtraFieldEntitlements extraFieldEntitlement;

  private boolean assignSpecificBarcode;

  private GenerateBy generateBy;

  private List<Range> barcodeRange;
  private List<Range> beneficiaryIdRange;
  private List<List<AuthorizationCriterion>> beneficiaryCriteria;

  private List<Range> barcodeRangeList;

  private DemographicsModel demographicsModel;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getItemId() {
    return itemId;
  }

  public void setItemId(String itemId) {
    this.itemId = itemId;
  }

  public String getItemName() {
    return itemName;
  }

  public void setItemName(String itemName) {
    this.itemName = itemName;
  }

  public XlsxForm getDeliveryForm() {
    return deliveryForm;
  }

  public void setDeliveryForm(XlsxForm deliveryForm) {
    this.deliveryForm = deliveryForm;
  }

  public ExtraFieldEntitlements getExtraFieldEntitlement() {
    return extraFieldEntitlement;
  }

  public void setExtraFieldEntitlement(ExtraFieldEntitlements extraFieldEntitlement) {
    this.extraFieldEntitlement = extraFieldEntitlement;
  }

  public boolean isAssignSpecificBarcode() {
    return assignSpecificBarcode;
  }

  public void setAssignSpecificBarcode(boolean assignSpecificBarcode) {
    this.assignSpecificBarcode = assignSpecificBarcode;
  }

  public GenerateBy getGenerateBy() {
    return generateBy;
  }

  public void setGenerateBy(GenerateBy generateBy) {
    this.generateBy = generateBy;
  }

  public List<Range> getBarcodeRange() {
    return barcodeRange;
  }

  public void setBarcodeRange(List<Range> barcodeRange) {
    this.barcodeRange = barcodeRange;
  }

  public List<Range> getBeneficiaryIdRange() {
    return beneficiaryIdRange;
  }

  public void setBeneficiaryIdRange(List<Range> beneficiaryIdRange) {
    this.beneficiaryIdRange = beneficiaryIdRange;
  }

  public List<List<AuthorizationCriterion>> getBeneficiaryCriteria() {
    return beneficiaryCriteria;
  }

  public void setBeneficiaryCriteria(List<List<AuthorizationCriterion>> beneficiaryCriteria) {
    this.beneficiaryCriteria = beneficiaryCriteria;
  }

  public List<Range> getBarcodeRangeList() {
    return barcodeRangeList;
  }

  public void setBarcodeRangeList(List<Range> barcodeRangeList) {
    this.barcodeRangeList = barcodeRangeList;
  }

  public DemographicsModel getDemographicsModel() {
    return demographicsModel;
  }

  public void setDemographicsModel(DemographicsModel demographicsModel) {
    this.demographicsModel = demographicsModel;
  }

  @Override
  public String toString() {
    return "AuthorizationFormModel{" +
        "id='" + id + '\'' +
        ", itemId='" + itemId + '\'' +
        ", itemName='" + itemName + '\'' +
        ", deliveryForm=" + deliveryForm +
        ", extraFieldEntitlement=" + extraFieldEntitlement +
        ", assignSpecificBarcode=" + assignSpecificBarcode +
        ", generateBy=" + generateBy +
        ", barcodeRange=" + barcodeRange +
        ", beneficiaryIdRange=" + beneficiaryIdRange +
        ", beneficiaryCriteria=" + beneficiaryCriteria +
        ", barcodeRangeList=" + barcodeRangeList +
        ", demographicsModel=" + demographicsModel +
        '}';
  }
}
