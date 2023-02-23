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

import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.IndividualStatus;

public interface Individual extends ModelStub, HasCustomTable {
  /**
   * REQUIRED: Foreign key to the beneficiary_entity that this individual belongs to
   * @return
   */
  String getBeneficiaryEntityRowId();

  /**
   * REQUIRED: Foreign key to the beneficiary_entity that this individual belongs to
   * @param beneficiaryEntityRowId
   */
  void setBeneficiaryEntityRowId(String beneficiaryEntityRowId);

  /**
   * OPTIONAL: Can indicate if there is a problem with this individual’s beneficiary entity or if they are floating without one
   * @return
   */
  String getBeneficiaryEntityStatus();

  /**
   * OPTIONAL: Can indicate if there is a problem with this individual’s beneficiary entity or if they are floating without one
   * @param beneficiaryEntityStatus
   */
  void setBeneficiaryEntityStatus(String beneficiaryEntityStatus);

  /**
   * REQUIRED: The date this individual was registered
   * @return
   */
  String getDateCreated();

  /**
   * REQUIRED: The date this individual was registered
   * @param dateCreated
   */
  void setDateCreated(String dateCreated);

  /**
   * OPTIONAL: The id of the custom form to launch when registering a new individual
   * @return
   */
  String getCustomMemberFormId();

  /**
   * OPTIONAL: The id of the custom form to launch when registering a new individual
   * @param customMemberFormId
   */
  void setCustomMemberFormId(String customMemberFormId);

  /**
   * OPTIONAL: Foreign key to the custom form instance that corresponds with this individual
   * @return
   */
  String getCustomMemberRowId();

  /**
   * OPTIONAL: Foreign key to the custom form instance that corresponds with this individual
   * @param customMemberRowId
   */
  void setCustomMemberRowId(String customMemberRowId);

  /**
   * REQUIRED: The Red Cross generated ID for this individual (can optionally be the same as beneficiary_entity_id)
   * @return
   */
  String getMemberId();

  /**
   * REQUIRED: The Red Cross generated ID for this individual (can optionally be the same as beneficiary_entity_id)
   * @param memberId
   */
  void setMemberId(String memberId);

  /**
   * REQUIRED: The status of the individual (enabled, disabled, etc)
   * @return
   */
  IndividualStatus getStatus();

  /**
   * REQUIRED: The status of the individual (enabled, disabled, etc)
   * @param status
   */
  void setStatus(IndividualStatus status);

  /**
   * OPTIONAL: Reason for the assigning the current status
   * @return
   */
  String getStatusReason();

  /**
   * OPTIONAL: Reason for the assigning the current status
   * @param statusReason
   */
  void setStatusReason(String statusReason);

  @Override
  default String getCustomTableFormId() {
    return getCustomMemberFormId();
  }

  @Override
  default void setCustomTableFormId(String id) {
    setCustomMemberFormId(id);
  }

  @Override
  default String getCustomTableRowId() {
    return getCustomMemberRowId();
  }

  @Override
  default void setCustomTableRowId(String id) {
    setCustomMemberRowId(id);
  }
}
