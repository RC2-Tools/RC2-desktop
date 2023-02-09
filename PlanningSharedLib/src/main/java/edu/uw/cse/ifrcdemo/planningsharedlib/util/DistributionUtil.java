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

import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Distribution;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Location;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.RowFilterScopeUtil;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvDistribution;
import org.opendatakit.aggregate.odktables.rest.TableConstants;
import org.opendatakit.aggregate.odktables.rest.entity.RowFilterScope;

import java.util.Locale;

public class DistributionUtil {

  public static CsvDistribution toCsvDistribution(Distribution distribution) {
    CsvDistribution csvDistribution = new CsvDistribution();

    csvDistribution.setRowId(distribution.getRowId());
    csvDistribution.setName(distribution.getName());

    if (distribution.getDescription() != null) {
      csvDistribution.setDescription(distribution.getDescription());
    } else {
      csvDistribution.setDescription(GenConsts.EMPTY_STRING);
    }

    Location location = distribution.getLocation();
    csvDistribution.setLocationId(location.getRowId());
    csvDistribution.setLocationName(location.getName());
    if (location.getDescription() != null) {
      csvDistribution.setLocationDescription(location.getDescription());
    } else {
      csvDistribution.setLocationDescription(GenConsts.EMPTY_STRING);
    }

    csvDistribution.setStatus(distribution.getStatus());
    csvDistribution.setStatusReason(GenConsts.EMPTY_STRING);

    if (distribution.getSummaryForm() != null) {
      csvDistribution.setSummaryFormId(distribution.getSummaryForm().getFormId());
      csvDistribution.setSummaryTableId(distribution.getSummaryForm().getTableId());
      csvDistribution.setSummaryVersion(distribution.getSummaryVersion());
    } else {
      csvDistribution.setSummaryFormId(GenConsts.EMPTY_STRING);
      csvDistribution.setSummaryTableId(GenConsts.EMPTY_STRING);
      csvDistribution.setSummaryVersion(GenConsts.EMPTY_STRING);
    }

    csvDistribution.setDateCreated(TableConstants.nanoSecondsFromMillis(
        distribution.getDateCreated().toEpochMilli(),
        Locale.ROOT
    ));
    csvDistribution.setRowFilterScope(RowFilterScopeUtil.emptyScope(RowFilterScope.Access.READ_ONLY));

    return csvDistribution;
  }

}
