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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Authorization;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Item;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.range.Range;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.RowFilterScopeUtil;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvAuthorization;
import org.opendatakit.aggregate.odktables.rest.TableConstants;
import org.opendatakit.aggregate.odktables.rest.entity.RowFilterScope;

import java.util.List;
import java.util.Locale;

public class AuthorizationUtil {
  public static CsvAuthorization toCsvAuthorization(Authorization authorization) {
    CsvAuthorization csvAuthorization = new CsvAuthorization();

    csvAuthorization.setRowId(authorization.getRowId());

    csvAuthorization.setType(authorization.getType());
    csvAuthorization.setForMember(String.valueOf(authorization.getForIndividual()));

    Item item = authorization.getItem();
    csvAuthorization.setItemId(item.getRowId());
    csvAuthorization.setItemName(item.getName());
    if (item.getDescription() != null) {
      csvAuthorization.setItemDescription(item.getDescription());
    } else {
      csvAuthorization.setItemDescription(GenConsts.EMPTY_STRING);
    }

    csvAuthorization.setItemRanges(jacksonConverter(authorization.getItemRanges()));
    csvAuthorization.setExtraFieldEntitlements(authorization.getExtraFieldEntitlements());

    csvAuthorization.setStatus(authorization.getStatus());
    csvAuthorization.setStatusReason(GenConsts.EMPTY_STRING);

    csvAuthorization.setDistributionId(authorization.getDistribution().getRowId());
    csvAuthorization.setDistributionName(authorization.getDistribution().getName());

    if (authorization.getCustomDeliveryForm() != null) {
      csvAuthorization.setCustomDeliveryFormId(authorization.getCustomDeliveryForm().getFormId());
    } else {
      csvAuthorization.setCustomDeliveryFormId(GenConsts.EMPTY_STRING);
    }

    csvAuthorization.setDateCreated(TableConstants.nanoSecondsFromMillis(
        authorization.getDateCreated().toEpochMilli(),
        Locale.ROOT
    ));

    csvAuthorization.setRowFilterScope(RowFilterScopeUtil.emptyScope(RowFilterScope.Access.READ_ONLY));

    return csvAuthorization;
  }

  // TODO: find better solution
  private static String jacksonConverter(List<Range> rangeList) {
    try {
      return new ObjectMapper().writerFor(new TypeReference<List<Range>>() {}).writeValueAsString(rangeList);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
