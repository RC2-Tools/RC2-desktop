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

package edu.uw.cse.ifrcdemo.sharedlib.model.stub;

public interface Delivery extends ModelStub, HasCustomTable {
  /**
   * REQUIRED: The foreign key for to the authorization that allowed this delivery
   * @return
   */
  String getAuthorizationId();

  /**
   * REQUIRED: The foreign key for to the authorization that allowed this delivery
   * @param authorizationId
   */
  void setAuthorizationId(String authorizationId);

  /**
   * REQUIRED: The type of authorization (token, registration, etc)
   * @return
   */
  String getAuthorizationType();

  /**
   * REQUIRED: The type of authorization (token, registration, etc)
   * @param authorizationType
   */
  void setAuthorizationType(String authorizationType);

  /**
   * REQUIRED: The beneficiary entity who received this delivery
   * @return
   */
  String getBeneficiaryEntityId();

  /**
   * REQUIRED: The beneficiary entity who received this delivery
   * @param beneficiaryEntityId
   */
  void setBeneficiaryEntityId(String beneficiaryEntityId);

  /**
   * OPTIONAL: Name of the custom delivery form (and corresponding table) that was filled out with this delivery
   * @return
   */
  String getCustomDeliveryFormId();

  /**
   * OPTIONAL: Name of the custom delivery form (and corresponding table) that was filled out with this delivery
   * @param customDeliveryFormId
   */
  void setCustomDeliveryFormId(String customDeliveryFormId);

  /**
   * OPTIONAL: Foreign key to the row in the custom delivery form table that corresponds with this row (if it exists)
   * @return
   */
  String getCustomDeliveryRowId();

  /**
   * OPTIONAL: Foreign key to the row in the custom delivery form table that corresponds with this row (if it exists)
   * @param customDeliveryRowId
   */
  void setCustomDeliveryRowId(String customDeliveryRowId);

  /**
   * REQUIRED: Date this delivery was created
   * @return
   */
  String getDateCreated();

  /**
   * REQUIRED: Date this delivery was created
   * @param dateCreated
   */
  void setDateCreated(String dateCreated);

  /**
   * REQUIRED: Name of the distribution
   * @return
   */
  String getDistributionName();

  /**
   * REQUIRED: Name of the distribution
   * @param distributionName
   */
  void setDistributionName(String distributionName);

  /**
   * OPTIONAL: Foreign key to the entitlement that was used to make this delivery (if it exists)
   * @return
   */
  String getEntitlementId();

  /**
   * OPTIONAL: Foreign key to the entitlement that was used to make this delivery (if it exists)
   * @param entitlementId
   */
  void setEntitlementId(String entitlementId);

  /**
   * OPTIONAL: The individual who received this delivery (if applicable)
   * @return
   */
  String getMemberId();

  /**
   * OPTIONAL: The individual who received this delivery (if applicable)
   * @param memberId
   */
  void setMemberId(String memberId);

  /**
   * OPTIONAL: Description of the item pack copied from the item pack table on the PC
   * @return
   */
  String getItemPackDescription();

  /**
   * OPTIONAL: Description of the item pack copied from the item pack table on the PC
   * @param itemPackDescription
   */
  void setItemPackDescription(String itemPackDescription);

  /**
   * REQUIRED: The foreign key for the item pack table on the PC
   * @return
   */
  String getItemPackId();

  /**
   * REQUIRED: The foreign key for the item pack table on the PC
   * @param itemPackId
   */
  void setItemPackId(String itemPackId);

  /**
   * OPTIONAL: The name of the item pack from the item pack table on the PC
   * @return
   */
  String getItemPackName();

  /**
   * OPTIONAL: The name of the item pack from the item pack table on the PC
   * @param itemPackName
   */
  void setItemPackName(String itemPackName);

  @Override
  default String getCustomTableFormId() {
    return getCustomDeliveryFormId();
  }

  @Override
  default void setCustomTableFormId(String id) {
    setCustomDeliveryFormId(id);
  }

  @Override
  default String getCustomTableRowId() {
    return getCustomDeliveryRowId();
  }

  @Override
  default void setCustomTableRowId(String id) {
    setCustomDeliveryRowId(id);
  }
}
