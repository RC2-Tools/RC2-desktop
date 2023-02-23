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

package edu.uw.cse.ifrcdemo.distplan.util;

import edu.uw.cse.ifrcdemo.distplan.logic.BuiltinAuthFields;
import edu.uw.cse.ifrcdemo.distplan.model.criteria.AuthorizationCriterion;
import edu.uw.cse.ifrcdemo.distplan.model.criteria.CriterionField;
import edu.uw.cse.ifrcdemo.distplan.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.BaseSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvBeneficiaryEntity;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvIndividual;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvVisit;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.HasCustomTable;
import edu.uw.cse.ifrcdemo.sharedlib.util.FileUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.StringUtil;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class CriteriaUtil {
  public static Map<String, Set<CriterionField>> buildCriteriaFieldMap(CsvRepository csvRepository) {
    Map<String, Set<CriterionField>> attrMap = new LinkedHashMap<>();

    attrMap.put(
        TranslationUtil.getTranslations().getString(TranslationConsts.CRITERIA_CATEGORY_CALCULATED),
        BuiltinAuthFields.FIELDS
    );
    addColFromTable(CsvIndividual.class, attrMap, csvRepository);
    addColFromTable(CsvBeneficiaryEntity.class, attrMap, csvRepository);
    addColFromCustomTable(CsvVisit.class, attrMap, csvRepository);

    // attr set is empty when there is no custom table
    attrMap.values().removeIf(Set::isEmpty);

    return attrMap;
  }

  public static List<List<AuthorizationCriterion>> removeEmptyRules(List<List<AuthorizationCriterion>> rules) {
    if (rules == null) {
      return new ArrayList<>();
    }

    return rules
        .stream()
        .filter(list -> !list.isEmpty())
        .collect(Collectors.toList());
  }

  public static List<List<AuthorizationCriterion>> deepCopyRules(List<List<AuthorizationCriterion>> criteria) {
    List<List<AuthorizationCriterion>> copy = new ArrayList<>(criteria.size());

    for (List<AuthorizationCriterion> qualification : criteria) {
      copy.add(new ArrayList<>(qualification));
    }

    return copy;
  }

  private static TreeSet<CriterionField> csvToColSet(String tableId, CsvRepository csvRepository) {
    return csvRepository
        .readUntypedCsv(FileUtil.getFileName(tableId))
        .filter(list -> !list.isEmpty())
        .map(x -> x.get(0).getColumns().keySet())
        .orElse(Collections.emptySet())
        .stream()
        .map(col -> new CriterionField(tableId, col))
        .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(CriterionField::getColumn))));
  }

  private static <T extends BaseSyncRow & HasCustomTable> void addColFromCustomTable(Class<T> baseTableClass,
                                                                                     Map<String, Set<CriterionField>> attrMap,
                                                                                     CsvRepository csvRepository) {
    csvRepository
        .readTypedCsv(baseTableClass)
        .orElse(Collections.emptyList())
        .stream()
        .map(HasCustomTable.class::cast)
        .map(HasCustomTable::getCustomTableFormId)
        .distinct()
        .filter(StringUtil::isNotNullAndNotEmpty)
        .forEach(table -> attrMap.put(table, csvToColSet(table, csvRepository)));
  }

  private static <T extends BaseSyncRow & HasCustomTable> void addColFromTable(Class<T> tableClass,
                                                                               Map<String, Set<CriterionField>> attrMap,
                                                                               CsvRepository csvRepository) {
    attrMap.put(
        FileUtil.getTableName(tableClass),
        csvToColSet(FileUtil.getTableName(tableClass), csvRepository)
    );

    addColFromCustomTable(tableClass, attrMap, csvRepository);
  }
}
