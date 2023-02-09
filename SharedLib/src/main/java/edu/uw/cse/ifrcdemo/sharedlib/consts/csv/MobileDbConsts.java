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

package edu.uw.cse.ifrcdemo.sharedlib.consts.csv;

public class MobileDbConsts {

    public static final String BENEFICIARY_ENTITY_ROW_ID = "beneficiary_entity_row_id";
    public static final String ROW_ID = "rowId";
    public static final String AUTHORIZATION_ID_COLUMN = "authorization_id";
    public static final String IS_OVERRIDE_COLUMN = "is_override";
    public static final String AUTHORIZATION_TYPE_COLUMN = "authorization_type";
    public static final String AUTH_TYPE_VALUE_NONE = "NONE";
    public static final String DATE_CREATED_COLUMN = "date_created";
    public static final String URI_FRAGMENT_COLUMN_SUFFIX = "_uriFragment";
    public static final String CONTENT_TYPE_COLUMN_SUFFIX = "_contentType";

    public static final class TableNames {
        public static final String AUTHORIZATION_TABLE_NAME = "authorizations";
        public static final String ENTITLEMENT_TABLE_NAME = "entitlements";
        public static final String BENEFICIARY_ENTITY_TABLE_NAME = "beneficiary_entities";
        public static final String INDIVIDUAL_TABLE_NAME = "members";
        public static final String DELIVERY_TABLE_NAME = "deliveries";
        public static final String AUTHORIZATION_REPORT_TABLE_NAME = "distribution_reports";
        public static final String VISIT_PROGRAM_TABLE_NAME = "visit_programs";
        public static final String VISIT_TABLE_NAME = "visits";
        public static final String DISTRIBUTION_TABLE_NAME = "distributions";
    }

    public static final class HealthTableNames {
        public static final String PROGRAMS_TABLE_NAME = "programs";
        public static final String SERVICES_TABLE_NAME = "services";
        public static final String SERVICES_FOR_PROGRAM_TABLE_NAME = "services_for_program";
    }
}
