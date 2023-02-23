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

package edu.uw.cse.ifrcdemo.distplan.util;

import edu.uw.cse.ifrcdemo.distplan.entity.Region;
import edu.uw.cse.ifrcdemo.distplan.entity.VisitProgram;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvVisitProgram;
import org.opendatakit.aggregate.odktables.rest.TableConstants;
import org.opendatakit.aggregate.odktables.rest.entity.RowFilterScope;

import java.util.Locale;

public class VisitProgramUtil {
  public static CsvVisitProgram toCsvVisitProgram(VisitProgram visitProgram) {
    CsvVisitProgram csvVisitProgram = new CsvVisitProgram();

    csvVisitProgram.setRowId(visitProgram.getRowId());

    csvVisitProgram.setDateCreated(TableConstants.nanoSecondsFromMillis(
        visitProgram.getDateCreated().toEpochMilli(), Locale.ROOT));

    csvVisitProgram.setName(visitProgram.getName());

    if (visitProgram.getDescription() != null) {
      csvVisitProgram.setDescription(visitProgram.getDescription());
    } else {
      csvVisitProgram.setDescription(GenConsts.EMPTY_STRING);
    }

    csvVisitProgram.setStatus(visitProgram.getStatus());
    csvVisitProgram.setStatusReason(visitProgram.getStatusReason());

    csvVisitProgram.setForMember(String.valueOf(visitProgram.isForMember()));

    csvVisitProgram.setCustomVisitFormId(visitProgram.getCustomVisitForm().getFormId());
    csvVisitProgram.setCustomVisitTableId(visitProgram.getCustomVisitForm().getTableId());

    Region location = visitProgram.getRegion();
    csvVisitProgram.setLocationId(location.getRowId());
    csvVisitProgram.setLocationName(location.getName());
    if (location.getDescription() != null) {
      csvVisitProgram.setLocationDescription(location.getDescription());
    } else {
      csvVisitProgram.setLocationDescription(GenConsts.EMPTY_STRING);
    }

    csvVisitProgram.setRowFilterScope(RowFilterScopeUtil.emptyScope(RowFilterScope.Access.READ_ONLY));

    return csvVisitProgram;
  }
}
