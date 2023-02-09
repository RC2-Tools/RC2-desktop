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

package edu.uw.cse.ifrcdemo.healthplan.logic;

import edu.uw.cse.ifrcdemo.planningsharedlib.logic.BuiltinCriteriaFields;
import edu.uw.cse.ifrcdemo.planningsharedlib.logic.criterionpredicate.TargetedByDistributionPredicate;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.AuthorizationCriterion;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.distribution.DistributionRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.entitlement.EntitlementRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.UntypedSyncRow;

import java.util.function.Predicate;

public class InternalCsvReportFields implements BuiltinCriteriaFields {
    public static final String TABLE_ID = "__internal_csv_report";

    public static final String TARGETED_BY_DIST = "__targeted_by_distribution";
    public static final String TARGETED_BY_DIST_INCLUDE_ALL = "__targeted_by_distribution_all";

    private final CsvRepository csvRepository;
    private final DistributionRepository distributionRepository;
    private final EntitlementRepository entitlementRepository;

    public InternalCsvReportFields(CsvRepository csvRepository,
                                   DistributionRepository distributionRepository,
                                   EntitlementRepository entitlementRepository) {
        this.csvRepository = csvRepository;
        this.distributionRepository = distributionRepository;
        this.entitlementRepository = entitlementRepository;
    }

    @Override
    public Predicate<UntypedSyncRow> createPredicate(AuthorizationCriterion criterion) {
        switch (criterion.getField().getColumn()) {
            case TARGETED_BY_DIST:
                return new TargetedByDistributionPredicate(distributionRepository, entitlementRepository, csvRepository, criterion, false);

            case TARGETED_BY_DIST_INCLUDE_ALL:
                return new TargetedByDistributionPredicate(distributionRepository, entitlementRepository, csvRepository, criterion, true);

            default:
                throw new IllegalArgumentException();
        }
    }
}
