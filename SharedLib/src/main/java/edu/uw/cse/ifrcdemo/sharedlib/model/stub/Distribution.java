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

import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.DistVisitProgStatus;

public interface Distribution extends ModelStub {
  /**
   * REQUIRED: Date this distribution was created
   * @return
   */
  String getDateCreated();

  /**
   * REQUIRED: Date this distribution was created
   * @param dateCreated
   */
  void setDateCreated(String dateCreated);

  /**
   * OPTIONAL: Description of the distribution copied from the distribution table on the PC
   * @return
   */
  String getDescription();

  /**
   * OPTIONAL: Description of the distribution copied from the distribution table on the PC
   * @param description
   */
  void setDescription(String description);

  /**
   * OPTIONAL: Name of the distribution copied from the distribution table on the PC
   * @return
   */
  String getName();

  /**
   * OPTIONAL: Name of the distribution copied from the distribution table on the PC
   * @param name
   */
  void setName(String name);

  /**
   * REQUIRED: The status of the distribution (active, inactive, disabled, removed)
   * @return
   */
  DistVisitProgStatus getStatus();

  /**
   * REQUIRED: The status of the distribution (active, inactive, disabled, removed)
   * @param status
   */
  void setStatus(DistVisitProgStatus status);

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
   * REQUIRED: The foreign key for the region table on the PC
   * @return
   */
  String getLocationId();

  /**
   * REQUIRED: The foreign key for the region table on the PC
   * @param locationId
   */
  void setLocationId(String locationId);

  /**
   * OPTIONAL: The name of the location from the region table on the PC
   * @return
   */
  String getLocationName();

  /**
   * OPTIONAL: The name of the location from the region table on the PC
   * @param locationName
   */
  void setLocationName(String locationName);

  /**
   * OPTIONAL: Description of the location copied from the region table on the PC
   * @return
   */
  String getLocationDescription();

  /**
   * OPTIONAL: Description of the location copied from the region table on the PC
   * @param locationDescription
   */
  void setLocationDescription(String locationDescription);

  /**
   * OPTIONAL: This column is used to indicate multiple iterations of this distribution. This does
   * NOT reset anything, this is to indicate, for example, that we are continuing this distribution
   * on another day after a summary form has already been filled out
   * @return
   */
  String getSummaryVersion();

  /**
   * OPTIONAL: This column is used to indicate multiple iterations of this distribution. This does
   * NOT reset anything, this is to indicate, for example, that we are continuing this distribution
   * on another day after a summary form has already been filled out
   * @param summaryVersion
   */
  void setSummaryVersion(String summaryVersion);

  /**
   * OPTIONAL: The form ID for the supervisor summary form, to be launched for example at the end of a day
   * @return
   */
  String getSummaryFormId();

  /**
   * OPTIONAL: The form ID for the supervisor summary form, to be launched for example at the end of a day
   * @param summaryFormId
   */
  void setSummaryFormId(String summaryFormId);

  /**
   * OPTIONAL: The table ID for the supervisor summary form, to be launched for example at the
   * end of a day
   * @return
   */
  String getSummaryTableId();

  /**
   * OPTIONAL: The table ID for the supervisor summary form, to be launched for example at the
   * end of a day
   * @param summaryTableId
   */
  void setSummaryTableId(String summaryTableId);
}
