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

package edu.uw.cse.ifrcdemo.distplan.logic.criterionpredicate;

import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.AuthorizationCriterion;
import edu.uw.cse.ifrcdemo.distplan.util.BeneficiaryUtil;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.CriterionOperator;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.sharedlib.consts.csv.MobileDbConsts;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvVisit;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.UntypedSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.util.StringUtil;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TargetedByVisitProgramPredicate implements Predicate<UntypedSyncRow> {
  private final AuthorizationCriterion authorizationCriterion;
  private final boolean includeUnrealized;

  private final Map<VisitLookup, Boolean> visitCompletionMap;

  public TargetedByVisitProgramPredicate(CsvRepository csvRepository,
                                         AuthorizationCriterion authorizationCriterion,
                                         boolean includeUnrealized) {
    this.authorizationCriterion = authorizationCriterion;
    this.includeUnrealized = includeUnrealized;

    this.visitCompletionMap = csvRepository
        .readTypedCsv(CsvVisit.class)
        .orElseThrow(IllegalStateException::new)
        .stream()
        .collect(Collectors.toMap(
            visit -> new VisitLookup(visit.getVisitProgramId(), visit.getBeneficiaryUnitId()),
            visit -> StringUtil.isNotNullAndNotEmpty(visit.getCustomTableRowId()),
            Boolean::logicalOr
        ));
  }

  @Override
  public boolean test(UntypedSyncRow untypedSyncRow) {
    if (untypedSyncRow == null || authorizationCriterion.getField() == null) {
      return false;
    }

    // only the EQ operator is supported
    if (authorizationCriterion.getOperator() != CriterionOperator.EQ) {
      return false;
    }

    String beneficiaryRowId = BeneficiaryUtil.getFromCombinedRow(
        untypedSyncRow,
        MobileDbConsts.TableNames.INDIVIDUAL_TABLE_NAME,
        MobileDbConsts.BENEFICIARY_ENTITY_ROW_ID
    );

    VisitLookup lookup = new VisitLookup(authorizationCriterion.getValue(), beneficiaryRowId);

    boolean found = visitCompletionMap.containsKey(lookup);
    if (found && !includeUnrealized) {
      // check the visit was indeed completed
      found = visitCompletionMap.get(lookup);
    }

    return found;
  }

  private static class VisitLookup {
    private final String visitProgramRowId;
    private final String beneficiaryRowId;

    private VisitLookup(String visitProgramRowId, String beneficiaryRowId) {
      this.visitProgramRowId = visitProgramRowId;
      this.beneficiaryRowId = beneficiaryRowId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      VisitLookup that = (VisitLookup) o;

      if (visitProgramRowId != null ? !visitProgramRowId.equals(that.visitProgramRowId) : that.visitProgramRowId != null)
        return false;
      return beneficiaryRowId != null ? beneficiaryRowId.equals(that.beneficiaryRowId) : that.beneficiaryRowId == null;
    }

    @Override
    public int hashCode() {
      int result = visitProgramRowId != null ? visitProgramRowId.hashCode() : 0;
      result = 31 * result + (beneficiaryRowId != null ? beneficiaryRowId.hashCode() : 0);
      return result;
    }
  }
}
