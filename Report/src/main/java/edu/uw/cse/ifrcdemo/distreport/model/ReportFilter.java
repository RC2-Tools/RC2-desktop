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

package edu.uw.cse.ifrcdemo.distreport.model;


import edu.uw.cse.ifrcdemo.sharedlib.consts.csv.MobileDbConsts;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.UntypedSyncRow;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ReportFilter {

    Map<String, List<UntypedSyncRow>> wrappedTables;

    Function<String, Long> overrides;
    Function<String, Long> generated;

    public ReportFilter(Map<String, List<UntypedSyncRow>> wrappedTables) {
        this.wrappedTables = wrappedTables;
        this.overrides = this::numOverrides;
        this.generated = this::numGenerated;
    }


    private long numOverrides(String key) {
        if (!this.wrappedTables.containsKey(key)) {
            return 0;
        }
        return this.wrappedTables.get(key)
                .stream()
                .filter(row -> row.getColumns().containsKey(MobileDbConsts.IS_OVERRIDE_COLUMN)
                        && Boolean.parseBoolean(row.getColumns().get(MobileDbConsts.IS_OVERRIDE_COLUMN)))
                .count();
    }

    private long numGenerated(String key) {
        if (!this.wrappedTables.containsKey(key)) {
            return 0;
        }
        return this.wrappedTables.get(key)
                .stream()
                .filter(row -> row.getColumns().containsKey(MobileDbConsts.IS_OVERRIDE_COLUMN)
                        && !Boolean.parseBoolean(row.getColumns().get(MobileDbConsts.IS_OVERRIDE_COLUMN))
                        && row.getColumns().containsKey(MobileDbConsts.AUTHORIZATION_TYPE_COLUMN)
                        && !row.getColumns().get(MobileDbConsts.AUTHORIZATION_TYPE_COLUMN).equals(MobileDbConsts.AUTH_TYPE_VALUE_NONE))
                .count();
    }
}
