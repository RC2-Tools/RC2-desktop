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

import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationType;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.ExtraFieldEntitlements;

public interface Authorization extends ModelStub {
  /**
   * OPTIONAL: The form ID for the delivery form to be launched when delivering this authorization
   * @return
   */
  String getCustomDeliveryFormId();

  /**
   * OPTIONAL: The form ID for the delivery form to be launched when delivering this authorization
   * @param customDeliveryFormId
   */
  void setCustomDeliveryFormId(String customDeliveryFormId);

  /**
   * REQUIRED: Date this authorization was created
   * @return
   */
  String getDateCreated();

  /**
   * REQUIRED: Date this authorization was created
   * @param dateCreated
   */
  void setDateCreated(String dateCreated);

  /**
   * The setting for extra field entitlements
   * @return
   */

  ExtraFieldEntitlements getExtraFieldEntitlements();

  /**
   * The setting for extra field entitlements
   * @param extraFieldEntitlements
   */

  void setExtraFieldEntitlements(ExtraFieldEntitlements extraFieldEntitlements);

  /**
   * REQUIRED: True to indicate that entitlements should be generated for each qualifying individual
   * @return
   */
  String getForMember();

  /**
   * REQUIRED: True to indicate that entitlements should be generated for each qualifying individual
   * @param forMember
   */
  void setForMember(String forMember);

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

  /**
   * OPTIONAL: Valid range of item barcodes to deliver
   * @return
   */
  String getItemPackRanges();

  /**
   * OPTIONAL: Valid range of item barcodes to deliver
   * @param itemPackRanges
   */
  void setItemPackRanges(String itemPackRanges);

  /**
   * REQUIRED: The status of the authorization (enabled, disabled, etc)
   * @return
   */
  AuthorizationStatus getStatus();

  /**
   * REQUIRED: The status of the authorization (enabled, disabled, etc)
   * @param status
   */
  void setStatus(AuthorizationStatus status);

  /**
   * OPTIONAL: An optional reason for assigning the current status
   * @return
   */
  String getStatusReason();

  /**
   * OPTIONAL: An optional reason for assigning the current status
   * @param statusReason
   */
  void setStatusReason(String statusReason);

  /**
   * REQUIRED: The type of authorization (token, registration, etc)
   * @return
   */
  AuthorizationType getType();

  /**
   * REQUIRED: The type of authorization (token, registration, etc)
   * @param type
   */
  void setType(AuthorizationType type);

  /**
   * REQUIRED: The foreign key to the distribution for this authorization
   * @return
   */
  public String getDistributionId();

  /**
   * REQUIRED: The foreign key to the distribution for this authorization
   * @param distributionId
   */
  public void setDistributionId(String distributionId);

  /**
   * OPTIONAL: Name of the distribution
   * @return
   */
  public String getDistributionName();

  /**
   * OPTIONAL: Name of the distribution
   * @param distributionName
   */
  public void setDistributionName(String distributionName);
}
