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

package edu.uw.cse.ifrcdemo.sharedlib.model.stub;

import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.BeneficiaryEntityStatus;

public interface BeneficiaryEntity extends ModelStub, HasCustomTable {
  /**
   * REQUIRED: The Red Cross generated ID for the beneficiary entity (but not the primary key to allow for duplicates)
   * @return
   */
  String getBeneficiaryEntityId();

  /**
   * REQUIRED: The Red Cross generated ID for the beneficiary entity (but not the primary key to allow for duplicates)
   * @param beneficiaryEntityId
   */
  void setBeneficiaryEntityId(String beneficiaryEntityId);

  /**
   * OPTIONAL: The id of the custom form to launch when registering a new beneficiary entity
   * @return
   */
  String getCustomBeneficiaryEntityFormId();

  /**
   * OPTIONAL: The id of the custom form to launch when registering a new beneficiary entity
   * @param customBeneficiaryEntityFormId
   */
  void setCustomBeneficiaryEntityFormId(String customBeneficiaryEntityFormId);

  /**
   * OPTIONAL: Foreign key to the custom form instance that corresponds with this beneficiary entity
   * @return
   */
  String getCustomBeneficiaryEntityRowId();

  /**
   * OPTIONAL: Foreign key to the custom form instance that corresponds with this beneficiary entity
   * @param customBeneficiaryEntityRowId
   */
  void setCustomBeneficiaryEntityRowId(String customBeneficiaryEntityRowId);

  /**
   * REQUIRED: The date this entity was registered
   * @return
   */
  String getDateCreated();

  /**
   * REQUIRED: The date this entity was registered
   * @param dateCreated
   */
  void setDateCreated(String dateCreated);

  /**
   * REQUIRED: The status of the beneficiary entity (enabled, disabled, etc)
   * @return
   */
  BeneficiaryEntityStatus getStatus();

  /**
   * REQUIRED: The status of the beneficiary entity (enabled, disabled, etc)
   * @param status
   */
  void setStatus(BeneficiaryEntityStatus status);

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
    return getCustomBeneficiaryEntityFormId();
  }

  @Override
  default void setCustomTableFormId(String id) {
    setCustomBeneficiaryEntityFormId(id);
  }

  @Override
  default String getCustomTableRowId() {
    return getCustomBeneficiaryEntityRowId();
  }

  @Override
  default void setCustomTableRowId(String id) {
    setCustomBeneficiaryEntityRowId(id);
  }
}
