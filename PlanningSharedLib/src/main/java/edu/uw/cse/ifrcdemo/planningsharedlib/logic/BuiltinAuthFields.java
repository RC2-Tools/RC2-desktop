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

package edu.uw.cse.ifrcdemo.planningsharedlib.logic;

import edu.uw.cse.ifrcdemo.planningsharedlib.logic.criterionpredicate.DateCreatedPredicate;
import edu.uw.cse.ifrcdemo.planningsharedlib.logic.criterionpredicate.HouseholdSizePredicate;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.AuthorizationCriterion;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.GenerateBy;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.CriterionField;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.UntypedSyncRow;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

public class BuiltinAuthFields implements BuiltinCriteriaFields {
  public static final String BUILTIN_TABLE_ID = "__distribution_planning_builtin";

  public static final String HOUSEHOLD_SIZE_KEY = "__hh_size";
  public static final String MEMBER_DATE_CREATED_KEY = "__member_date_created";
  public static final String BENEFICIARY_UNIT_DATE_CREATED_KEY = "__beneficiary_unit_date_created";

  public static final Set<CriterionField> FIELDS;

  static {
    Set<CriterionField> set = new TreeSet<>(Comparator.comparing(CriterionField::getColumn));

    set.add(new CriterionField(BUILTIN_TABLE_ID, HOUSEHOLD_SIZE_KEY));
    set.add(new CriterionField(BUILTIN_TABLE_ID, MEMBER_DATE_CREATED_KEY));
    set.add(new CriterionField(BUILTIN_TABLE_ID, BENEFICIARY_UNIT_DATE_CREATED_KEY));

    FIELDS = Collections.unmodifiableSet(set);
  }

  private final CsvRepository csvRepository;

  public BuiltinAuthFields(CsvRepository csvRepository) {
    this.csvRepository = csvRepository;
  }

  @Override
  public Predicate<UntypedSyncRow> createPredicate(AuthorizationCriterion criterion) {
    switch (criterion.getField().getColumn()) {
      case BuiltinAuthFields.HOUSEHOLD_SIZE_KEY:
        return new HouseholdSizePredicate(criterion, csvRepository);

      case BuiltinAuthFields.MEMBER_DATE_CREATED_KEY:
        return new DateCreatedPredicate(criterion, GenerateBy.HOUSEHOLD_MEMBER);

      case BuiltinAuthFields.BENEFICIARY_UNIT_DATE_CREATED_KEY:
        return new DateCreatedPredicate(criterion, GenerateBy.BENEFICIARY_UNIT);

      default:
        throw new IllegalArgumentException();
    }
  }
}
