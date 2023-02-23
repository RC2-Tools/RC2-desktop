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

import edu.uw.cse.ifrcdemo.distplan.model.GenerateBy;
import edu.uw.cse.ifrcdemo.distplan.model.criteria.AuthorizationCriterion;
import edu.uw.cse.ifrcdemo.sharedlib.consts.csv.MobileDbConsts;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.UntypedSyncRow;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opendatakit.aggregate.odktables.rest.TableConstants;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.function.Predicate;

public class DateCreatedPredicate implements Predicate<UntypedSyncRow> {
  private final AuthorizationCriterion authorizationCriterion;
  private final GenerateBy generateBy;
  private final Logger logger;

  public DateCreatedPredicate(AuthorizationCriterion authorizationCriterion, GenerateBy generateBy) {
    this.authorizationCriterion = authorizationCriterion;
    this.generateBy = generateBy;
    this.logger = LogManager.getLogger(DateCreatedPredicate.class);
  }

  @Override
  public boolean test(UntypedSyncRow untypedSyncRow) {
    if (untypedSyncRow == null || untypedSyncRow.getRowId() == null) {
      return false;
    }

    // choose the appropriate date_created field based on forMember value
    // use member's date_create when forMember == true
    // use beneficiary unit's when forMember == false
    String tableId = generateBy == GenerateBy.HOUSEHOLD_MEMBER ?
        MobileDbConsts.TableNames.INDIVIDUAL_TABLE_NAME :
        MobileDbConsts.TableNames.BENEFICIARY_ENTITY_TABLE_NAME;
    String givenTimeString = untypedSyncRow.getColumns().get(tableId + "_" + MobileDbConsts.DATE_CREATED_COLUMN);

    ZonedDateTime criterion;
    ZonedDateTime given;
    try {
      criterion = LocalDate
          .parse(authorizationCriterion.getValue(), DateTimeFormatter.ISO_LOCAL_DATE)
          .atStartOfDay(ZoneOffset.UTC);

      given = Instant
          .ofEpochMilli(TableConstants.milliSecondsFromNanos(givenTimeString, Locale.ROOT))
          .atZone(ZoneOffset.UTC)
          .truncatedTo(ChronoUnit.DAYS);
    } catch (IllegalArgumentException e) {
      // given contains malformed datetime
      logger.catching(Level.WARN, e);
      return false;
    }

    switch (authorizationCriterion.getOperator()) {
      case EQ:
        return given.compareTo(criterion) == 0;
      case GT:
        return given.compareTo(criterion) > 0;
      case LT:
        return given.compareTo(criterion) < 0;
      default:
        return false;
    }
  }
}
