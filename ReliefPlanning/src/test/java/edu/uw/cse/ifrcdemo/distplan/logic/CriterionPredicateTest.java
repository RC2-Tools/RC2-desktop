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

package edu.uw.cse.ifrcdemo.distplan.logic;

import edu.uw.cse.ifrcdemo.distplan.CriterionFieldArgConverter;
import edu.uw.cse.ifrcdemo.planningsharedlib.logic.criterionpredicate.SimpleCriterionPredicate;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.AuthorizationCriterion;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.CriterionField;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.CriterionOperator;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.UntypedSyncRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CriterionPredicateTest {
  private UntypedSyncRow testRow;

  @BeforeEach
  void setupTestRow() {
    UntypedSyncRow row = new UntypedSyncRow();

    row.setRowId("rowId");

    Map<String, String> columns = row.getColumns();
    columns.put("test_str_field_1", "test_value");
    columns.put("test_str_field_2", "b");
    columns.put("test_str_field_3", "");
    columns.put("test_int_field_1", "12345");
    columns.put("test_int_field_2", "00005");
    columns.put("test_double_field_1", "0.1");
    columns.put("test_double_field_2", "0");

    testRow = row;
  }

  @ParameterizedTest(name = "{0} {1} {2} -> {3}")
  @CsvSource({
      // string equality
      "test//str_field_1, EQ, test_value, true",
      "test//str_field_1, LT, test_value, false",
      "test//str_field_1, GT, test_value, false",
      // string inequality -> always false
      "test//str_field_2, GT, a, false",
      "test//str_field_2, LT, a, false",
      // empty string
      "test//str_field_3, EQ, '', true",
      "test//str_field_3, LT, '', false",
      "test//str_field_3, GT, '', false",
      // int equality
      "test//int_field_1, EQ, 12345, true",
      "test//int_field_1, LT, 12345, false",
      "test//int_field_1, GT, 12345, false",
      // int inequality / negation
      "test//int_field_1, LT, 20000, true",
      "test//int_field_1, LT, 10000, false",
      "test//int_field_1, GT, 10000, true",
      "test//int_field_1, GT, 20000, false",
      // int with leading zero
      "test//int_field_2, LT, 10, true",
      // double equality
      "test//double_field_1, EQ, 0.1, true",
      "test//double_field_1, LT, 0.1, false",
      "test//double_field_1, GT, 0.1, false",
      // double inequality / negation
      "test//double_field_1, LT, 10.1, true",
      "test//double_field_1, GT, 10.1, false",
      "test//double_field_1, GT, 0.01, true",
      "test//double_field_1, LT, 0, false",
      // double positive/negative
      "test//double_field_2, EQ, 0, true",
      "test//double_field_2, LT, 1.1, true",
      "test//double_field_2, GT, -1.1, true",
      // null
      "test//field, EQ,, false",
      "test//field, LT,, false",
      "test//field, GT,, false",
      "test//field,, value, false",
      ", EQ, value, false"
  })
  void test(@ConvertWith(CriterionFieldArgConverter.class) CriterionField field,
            CriterionOperator op,
            String value,
            boolean assertion) {
    AuthorizationCriterion criterion = new AuthorizationCriterion(field, op, value);
    SimpleCriterionPredicate predicate = new SimpleCriterionPredicate(criterion);

    assertEquals(assertion, predicate.test(testRow));
  }

  @Test
  @DisplayName("Test a null row")
  void nullTest() {
    CriterionField field = new CriterionField("table", "column");
    AuthorizationCriterion criterion = new AuthorizationCriterion(field, CriterionOperator.EQ, "");
    SimpleCriterionPredicate predicate = new SimpleCriterionPredicate(criterion);

    assertFalse(predicate.test(null));
  }
}