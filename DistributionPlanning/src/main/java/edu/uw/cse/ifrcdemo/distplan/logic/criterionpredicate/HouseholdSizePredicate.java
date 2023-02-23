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

package edu.uw.cse.ifrcdemo.distplan.logic.criterionpredicate;


import edu.uw.cse.ifrcdemo.distplan.model.criteria.AuthorizationCriterion;
import edu.uw.cse.ifrcdemo.distplan.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.sharedlib.consts.csv.MobileDbConsts;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvIndividual;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.UntypedSyncRow;
import edu.uw.cse.ifrcdemo.translations.LogStr;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.function.Predicate;

public class HouseholdSizePredicate implements Predicate<UntypedSyncRow> {

    private AuthorizationCriterion authorizationCriterion;
    private CsvRepository repository;
    private final Logger logger;

    public HouseholdSizePredicate(AuthorizationCriterion authorizationCriterion, CsvRepository repository) {
        this.authorizationCriterion = authorizationCriterion;
        this.repository = repository;
        this.logger = LogManager.getLogger(HouseholdSizePredicate.class);
    }

    @Override
    public boolean test(UntypedSyncRow untypedSyncRow) {
        if (untypedSyncRow == null || authorizationCriterion.getField() == null) {
            return false;
        }
        String criterion = authorizationCriterion.getValue();

        Long given = repository.readTypedCsv(CsvIndividual.class)
                .orElseThrow(IllegalStateException::new)
                .stream()
                .filter(p -> p.getBeneficiaryEntityRowId().equals(untypedSyncRow.getColumns().get(MobileDbConsts.TableNames.INDIVIDUAL_TABLE_NAME + "_" + MobileDbConsts.BENEFICIARY_ENTITY_ROW_ID)))
                .count();

        logger.trace(LogStr.LOG_TESTING_WITH_CRITERION, given::toString, authorizationCriterion::toString);

        BigDecimal criterionNumber = NumberUtils.createBigDecimal(criterion);
        BigDecimal givenNumber = new BigDecimal(given);

        switch (authorizationCriterion.getOperator()) {
            case EQ:
                return givenNumber.compareTo(criterionNumber) == 0;
            case GT:
                return givenNumber.compareTo(criterionNumber) > 0;
            case LT:
                return givenNumber.compareTo(criterionNumber) < 0;
            default:
                return false;
        }
    }
}
