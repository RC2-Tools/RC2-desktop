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

package edu.uw.cse.ifrcdemo.distplan.logic;

import edu.uw.cse.ifrcdemo.distplan.logic.criterionpredicate.CriteriaPredicateFactory;
import edu.uw.cse.ifrcdemo.distplan.model.GenerateBy;
import edu.uw.cse.ifrcdemo.distplan.model.criteria.AuthorizationCriterion;
import edu.uw.cse.ifrcdemo.distplan.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.distplan.model.distribution.DistributionRepository;
import edu.uw.cse.ifrcdemo.distplan.model.entitlement.EntitlementRepository;
import edu.uw.cse.ifrcdemo.distplan.model.visitprogram.VisitProgramRepository;
import edu.uw.cse.ifrcdemo.distplan.util.BeneficiaryUtil;
import edu.uw.cse.ifrcdemo.distplan.util.CriteriaUtil;
import edu.uw.cse.ifrcdemo.distplan.util.ExportUtil;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.BeneficiaryEntityStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.IndividualStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvBeneficiaryEntity;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvIndividual;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvVisit;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.UntypedSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.SyncRow;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntitlementFilter {
  /**
   *
   * @param repository
   * @param rules
   * @param forMember
   * @return a list of individual row id
   */
  public static Stream<String> filter(CsvRepository repository,
                                      List<List<AuthorizationCriterion>> rules,
                                      boolean forMember) {
    CriteriaPredicateFactory predicateFactory = new CriteriaPredicateFactory(
        repository,
        null,
        null
    );
    Predicate<UntypedSyncRow> predicate = buildPredicateFromRules(rules, predicateFactory);

    return filterByPredicate(repository, null, predicate, forMember);
  }

  public static Stream<String> filter(CsvRepository repository,
                                      List<List<AuthorizationCriterion>> rules,
                                      GenerateBy generateBy) {
    return filter(repository, rules, generateBy == GenerateBy.HOUSEHOLD_MEMBER);
  }

  public static Stream<String> filterForCsvReport(CsvRepository csvRepository,
                                                  DistributionRepository distributionRepository,
                                                  EntitlementRepository entitlementRepository,
                                                  VisitProgramRepository visitProgramRepository,
                                                  List<List<AuthorizationCriterion>> rules) {
    CriteriaPredicateFactory predicateFactory = new CriteriaPredicateFactory(
        csvRepository,
        distributionRepository,
        entitlementRepository
    );
    Predicate<UntypedSyncRow> predicate = buildPredicateFromRules(rules, predicateFactory);

    return filterByPredicate(csvRepository, visitProgramRepository, predicate, true);
  }

  private static Stream<String> filterByPredicate(CsvRepository csvRepository,
                                                  VisitProgramRepository visitProgramRepository,
                                                  Predicate<UntypedSyncRow> predicate,
                                                  boolean forMember) {
    Map<String, CsvIndividual> indexedCsvIndividual = csvRepository
        .readIndexedTypedCsv(CsvIndividual.class)
        .orElseThrow(IllegalStateException::new);

    List<CsvVisit> extraVisits;
    if (visitProgramRepository != null) {
      extraVisits = ExportUtil.generateVisitUpdate(visitProgramRepository, csvRepository);
    } else {
      extraVisits = null;
    }

    // TODO: would this work if there is a qualifying beneficiary unit
    //       but that unit has no member?
    Stream<String> rowIdStream = indexedCsvIndividual
        .values()
        .stream()
        .map(member -> BeneficiaryUtil.getAllDataOnMember(member, csvRepository, visitProgramRepository, extraVisits))
        .filter(predicate)
        .map(SyncRow::getRowId);

    // filter stream so that at most 1 member from each beneficiary unit is included
    if (!forMember) {
      Map<String, CsvBeneficiaryEntity> indexedCsvBeneficiaryEntity = csvRepository
          .readIndexedTypedCsv(CsvBeneficiaryEntity.class)
          .orElseThrow(IllegalStateException::new);

      // group individual ids into a map such that beneficiary id -> list of individual id
      // for each entry in the map, pick the first in the list
      rowIdStream = rowIdStream.collect(Collectors.collectingAndThen(
          Collectors.groupingBy(id -> indexedCsvIndividual
              .get(id)
              .getBeneficiaryEntityRowId()
          ),
          memberGroupedByUnit -> memberGroupedByUnit
              .entrySet()
              .stream()
              .filter(entry -> BeneficiaryEntityStatus.ENABLED ==
                  indexedCsvBeneficiaryEntity.get(entry.getKey()).getStatus())
              .map(entry -> entry.getValue().get(0))
      ));
    } else {
      rowIdStream = rowIdStream.filter(memberRowId ->
          IndividualStatus.ENABLED == indexedCsvIndividual.get(memberRowId).getStatus()
      );
    }

    return rowIdStream;
  }

  private static Predicate<UntypedSyncRow> buildPredicateFromRules(List<List<AuthorizationCriterion>> rules,
                                                                   CriteriaPredicateFactory predicateFactory) {
    List<List<AuthorizationCriterion>> cleanedRules = CriteriaUtil.removeEmptyRules(rules);

    // collapse rules into 1 predicate
    return cleanedRules.isEmpty() ? (__ -> true) : cleanedRules
        .stream()
        .map(rule -> rule
            .stream()
            .map(predicateFactory::createPredicate)
            .reduce(Predicate::and)
            .orElse(__ -> true)
        )
        .reduce(Predicate::or)
        .orElse(__ -> true);
  }
}
