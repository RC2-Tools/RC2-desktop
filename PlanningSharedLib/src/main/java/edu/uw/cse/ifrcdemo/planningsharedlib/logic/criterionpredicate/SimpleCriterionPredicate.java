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

package edu.uw.cse.ifrcdemo.planningsharedlib.logic.criterionpredicate;

import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.AuthorizationCriterion;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.BeneficiaryUtilBase;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.UntypedSyncRow;
import edu.uw.cse.ifrcdemo.translations.LogStr;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.function.Predicate;

public class SimpleCriterionPredicate implements Predicate<UntypedSyncRow> {
    private final AuthorizationCriterion authorizationCriterion;

    private final Logger logger;

    public SimpleCriterionPredicate(AuthorizationCriterion authorizationCriterion) {
        this.authorizationCriterion = authorizationCriterion;
        this.logger = LogManager.getLogger(SimpleCriterionPredicate.class);
    }

    @Override
    public boolean test(UntypedSyncRow untypedSyncRows) {
        if (untypedSyncRows == null || authorizationCriterion.getField() == null) {
            return false;
        }

        String criterion = authorizationCriterion.getValue();
        String given = BeneficiaryUtilBase.getFromCombinedRow(
                untypedSyncRows,
                authorizationCriterion.getField().getTableId(),
                authorizationCriterion.getField().getColumn()
        );

        if (given == null) {
            logger.debug(LogStr.LOG_TESTING_WITH_CRITERION_GIVEN_IS_NULL, authorizationCriterion::toString);
            return false;
        }

        logger.trace(LogStr.LOG_TESTING_WITH_CRITERION, given::toString, authorizationCriterion::toString);

        boolean isNumber = NumberUtils.isCreatable(criterion) && NumberUtils.isCreatable(given);
        BigDecimal criterionNumber = BigDecimal.ZERO, givenNumber = BigDecimal.ZERO;
        if (isNumber) {
            criterionNumber = NumberUtils.createBigDecimal(criterion);
            givenNumber = NumberUtils.createBigDecimal(given);
        }

        switch (authorizationCriterion.getOperator()) {
            case EQ:
                if (isNumber) {
                    logger.trace(LogStr.LOG_USING_NUMBER_COMPARATOR);
                    return givenNumber.compareTo(criterionNumber) == 0;
                }
                return criterion.equalsIgnoreCase(given);
            case NE:
                if (isNumber) {
                    logger.trace(LogStr.LOG_USING_NUMBER_COMPARATOR);
                    return givenNumber.compareTo(criterionNumber) != 0;
                }
                return !criterion.equalsIgnoreCase(given);
            case GT:
                if (isNumber) {
                    logger.trace(LogStr.LOG_USING_NUMBER_COMPARATOR);
                    return givenNumber.compareTo(criterionNumber) > 0;
                }
                return false; // GT has to be a number
            case LT:
                if (isNumber) {
                    logger.trace(LogStr.LOG_USING_NUMBER_COMPARATOR);
                    return givenNumber.compareTo(criterionNumber) < 0;
                }
                return false; // LT has to be a number
            default:
                return false;
        }
    }
}
