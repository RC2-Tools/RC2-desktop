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

package edu.uw.cse.ifrcdemo.planningsharedlib.util;

import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Authorization;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Entitlement;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Item;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.RowFilterScopeUtil;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationType;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvEntitlement;
import org.opendatakit.aggregate.odktables.rest.TableConstants;
import org.opendatakit.aggregate.odktables.rest.entity.RowFilterScope;

import java.util.Locale;

public class EntitlementUtil {
  public static CsvEntitlement toCsvEntitlement(Entitlement entitlement) {
    Authorization authorization = entitlement.getAuthorization();
    CsvEntitlement csvEntitlement = new CsvEntitlement();

    if (authorization.getType().equals(AuthorizationType.REQUIRED_REGISTRATION)) {
      csvEntitlement.setBeneficiaryEntityId(entitlement.getBeneficiaryUnitRcId());

      if (authorization.getForIndividual()) {
        csvEntitlement.setMemberId(entitlement.getMemberRcId());
      }

      csvEntitlement.setRowFilterScope(RowFilterScopeUtil.emptyScope(RowFilterScope.Access.HIDDEN));
    } else if (authorization.getType().equals(AuthorizationType.OPTIONAL_REGISTRATION)) {
      if (authorization.getForIndividual()) {
        csvEntitlement.setBeneficiaryEntityId(entitlement.getMemberRcId());
      } else {
        csvEntitlement.setBeneficiaryEntityId(entitlement.getBeneficiaryUnitRcId());
      }

      csvEntitlement.setRowFilterScope(RowFilterScopeUtil.emptyScope(RowFilterScope.Access.READ_ONLY));
    }

    csvEntitlement.setRowId(entitlement.getRowId());

    csvEntitlement.setDistributionName(authorization.getDistribution().getName());

    csvEntitlement.setAuthorizationId(authorization.getRowId());
    csvEntitlement.setAuthorizationType(authorization.getType());

    csvEntitlement.setIsOverride(String.valueOf(entitlement.isOverride()));

    if (entitlement.getAssignedItemCode() != null) {
      csvEntitlement.setAssignedItemCode(entitlement.getAssignedItemCode());
    } else {
      csvEntitlement.setAssignedItemCode(GenConsts.EMPTY_STRING);
    }

    Item item = authorization.getItem();
    csvEntitlement.setItemId(item.getRowId());
    csvEntitlement.setItemName(item.getName());
    if (item.getDescription() != null) {
      csvEntitlement.setItemDescription(item.getDescription());
    } else {
      csvEntitlement.setItemDescription(GenConsts.EMPTY_STRING);
    }

    csvEntitlement.setStatus(entitlement.getStatus());

    if (entitlement.getStatusReason() != null) {
      csvEntitlement.setStatusReason(entitlement.getStatusReason());
    } else {
      csvEntitlement.setStatusReason(GenConsts.EMPTY_STRING);
    }

    csvEntitlement.setDateCreated(TableConstants.nanoSecondsFromMillis(
        entitlement.getDateCreated().toEpochMilli(),
        Locale.ROOT
    ));

    // Added for auditing and groups
    csvEntitlement.setCreateUser(entitlement.getCreateUser());
    // Need to get the groupModify from the BeneficiaryEntity
    csvEntitlement.getRowFilterScope().setGroupReadOnly(entitlement.getGroupReadOnly());

    return csvEntitlement;
  }
}
