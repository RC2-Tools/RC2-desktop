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

package edu.uw.cse.ifrcdemo.healthplan.util;

import edu.uw.cse.ifrcdemo.healthplan.entity.HealthService;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.RowFilterScopeUtil;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvHealthService;
import org.opendatakit.aggregate.odktables.rest.entity.RowFilterScope;

public class HealthServiceUtil {

    public static CsvHealthService toCsvHealthService(HealthService hs) {
        CsvHealthService csvHealthTask = new CsvHealthService();

        csvHealthTask.setRowId(hs.getRowId());
        csvHealthTask.setName(hs.getName());

        if (hs.getDescription() != null) {
            csvHealthTask.setDescription(hs.getDescription());
        } else {
            csvHealthTask.setDescription(GenConsts.EMPTY_STRING);
        }

        Boolean endsWithReferrals = hs.getEndWithReferrals();
        if(endsWithReferrals != null) {
            csvHealthTask.setEndWithReferrals(endsWithReferrals.toString());
        } else {
            csvHealthTask.setEndWithReferrals(Boolean.FALSE.toString());
        }

        Boolean requiresReferral = hs.getRequiresReferral();
        if(requiresReferral != null) {
            csvHealthTask.setRequiresReferral(requiresReferral.toString());
        } else {
            csvHealthTask.setRequiresReferral(Boolean.FALSE.toString());
        }

        csvHealthTask.setServiceFormId(hs.getServiceFormId());
        csvHealthTask.setServiceTableId(hs.getServiceTableId());

        csvHealthTask.setReferralFormId(hs.getReferralFormId());
        csvHealthTask.setReferralTableId(hs.getReferralTableId());

        csvHealthTask.setRowFilterScope(RowFilterScopeUtil.emptyScope(RowFilterScope.Access.READ_ONLY));


        return csvHealthTask;
    }
}
