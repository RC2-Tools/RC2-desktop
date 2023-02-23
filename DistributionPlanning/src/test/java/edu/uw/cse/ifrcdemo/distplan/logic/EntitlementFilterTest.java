package edu.uw.cse.ifrcdemo.distplan.logic;

import edu.uw.cse.ifrcdemo.distplan.CriterionFieldArgConverter;
import edu.uw.cse.ifrcdemo.distplan.CsvRepoArgConverter;
import edu.uw.cse.ifrcdemo.distplan.model.criteria.AuthorizationCriterion;
import edu.uw.cse.ifrcdemo.distplan.model.criteria.CriterionField;
import edu.uw.cse.ifrcdemo.distplan.model.criteria.CriterionOperator;
import edu.uw.cse.ifrcdemo.distplan.model.csv.CsvRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EntitlementFilterTest {
  private static final String CSV_PATH = "csv/50_member";

 @ParameterizedTest(name = "csv: {0}, {1} {2} {3} -> {4}, forIndividual: {5}")
  @CsvSource({
      CSV_PATH + ", members//custom_member_form_id, EQ, custom_members_1, 14, false",
      CSV_PATH + ", members//custom_member_form_id, EQ, custom_members_1, 16, true",
  })
  @DisplayName("Test criterion field in Member base table, 1 criterion, 1 case")
  void filter_memberBaseTable(@ConvertWith(CsvRepoArgConverter.class) CsvRepository csvRepository,
                              @ConvertWith(CriterionFieldArgConverter.class) CriterionField field,
                              CriterionOperator operator,
                              String value,
                              int expectedCount,
                              boolean forIndividual) {
    AuthorizationCriterion criterion = new AuthorizationCriterion(field, operator, value);
    List<List<AuthorizationCriterion>> rules = Collections.singletonList(Collections.singletonList(criterion));

    assertEquals(expectedCount, EntitlementFilter.filter(csvRepository, rules, forIndividual).count());
  }

  @ParameterizedTest(name = "csv: {0}, {1} {2} {3} -> {4}, forIndividual: {5}")
  @CsvSource({
      CSV_PATH + ", custom_members_1//custom_members_1_int_attribute_1, LT, 800, 13, false",
      CSV_PATH + ", custom_members_1//custom_members_1_int_attribute_1, LT, 800, 14, true",
      CSV_PATH + ", custom_members_1//custom_members_1_int_attribute_1, LT, 50, 2, false",
      CSV_PATH + ", custom_members_1//custom_members_1_int_attribute_1, LT, 50, 2, true",
  })
  @DisplayName("Test criterion field in Member custom table, 1 criterion, 1 case")
  void filter_memberCustomTable(@ConvertWith(CsvRepoArgConverter.class) CsvRepository csvRepository,
                                @ConvertWith(CriterionFieldArgConverter.class) CriterionField field,
                                CriterionOperator operator,
                                String value,
                                int expectedCount,
                                boolean forIndividual) {
    AuthorizationCriterion criterion = new AuthorizationCriterion(field, operator, value);
    List<List<AuthorizationCriterion>> rules = Collections.singletonList(Collections.singletonList(criterion));

    assertEquals(expectedCount, EntitlementFilter.filter(csvRepository, rules, forIndividual).count());
  }

  @ParameterizedTest(name = "csv: {0}, {1} {2} {3} -> {4}, forIndividual: {5}")
  @CsvSource({
      CSV_PATH + ", beneficiary_entities//custom_beneficiary_entity_form_id, EQ, custom_beneficiary_entities_2, 7, false",
      CSV_PATH + ", beneficiary_entities//custom_beneficiary_entity_form_id, EQ, custom_beneficiary_entities_2, 10, true",
  })
  @DisplayName("Test criterion field in Beneficiary Entity base table, 1 criterion, 1 case")
  void filter_beneficiaryEntityBaseTable(@ConvertWith(CsvRepoArgConverter.class) CsvRepository csvRepository,
                                         @ConvertWith(CriterionFieldArgConverter.class) CriterionField field,
                                         CriterionOperator operator,
                                         String value,
                                         int expectedCount,
                                         boolean forIndividual) {
    AuthorizationCriterion criterion = new AuthorizationCriterion(field, operator, value);
    List<List<AuthorizationCriterion>> rules = Collections.singletonList(Collections.singletonList(criterion));

    assertEquals(expectedCount, EntitlementFilter.filter(csvRepository, rules, forIndividual).count());
  }

  @ParameterizedTest(name = "csv: {0}, rule[{index}] -> {2}, forIndividual: {3}")
  @MethodSource("filter_multipleCriterion")
  @DisplayName("Test criterion field across tables, multiple criterion, 1 case")
  void filter_multipleCriterion(@ConvertWith(CsvRepoArgConverter.class) CsvRepository csvRepository,
                                List<List<AuthorizationCriterion>> rules,
                                int expectedCount,
                                boolean forIndividual) {
    assertEquals(expectedCount, EntitlementFilter.filter(csvRepository, rules, forIndividual).count());
  }

  static Stream<Arguments> filter_multipleCriterion() {
    List<List<AuthorizationCriterion>> rule1 = Collections.singletonList(Arrays.asList(
        new AuthorizationCriterion(new CriterionField("members", "custom_member_form_id"), CriterionOperator.EQ, "custom_members_2"),
        new AuthorizationCriterion(new CriterionField("custom_members_2", "custom_members_2_double_attribute_1"), CriterionOperator.LT, "1")
    ));

    return Stream.of(
        Arguments.of(CSV_PATH, rule1, 5, false),
        Arguments.of(CSV_PATH, rule1, 6, true)
    );
  }

  @ParameterizedTest(name = "csv: {0}, rule[{index}] -> {2}, forIndividual: {3}")
  @MethodSource("filter_multipleCases")
  @DisplayName("Test criterion field across tables, 1 criterion, multiple case")
  void filter_multipleCases(@ConvertWith(CsvRepoArgConverter.class) CsvRepository csvRepository,
                                List<List<AuthorizationCriterion>> rules,
                                int expectedCount,
                                boolean forIndividual) {
    assertEquals(expectedCount, EntitlementFilter.filter(csvRepository, rules, forIndividual).count());
  }

  static Stream<Arguments> filter_multipleCases() {
    List<List<AuthorizationCriterion>> rule1 = Arrays.asList(
        Collections.singletonList(new AuthorizationCriterion(new CriterionField("members", "custom_member_form_id"), CriterionOperator.EQ, "custom_members_1")),
        Collections.singletonList(new AuthorizationCriterion(new CriterionField("custom_members_2", "custom_members_2_double_attribute_1"), CriterionOperator.LT, "1"))
    );

    return Stream.of(
        Arguments.of(CSV_PATH, rule1, 14, false),
        Arguments.of(CSV_PATH, rule1, 22, true)
    );
  }

  @ParameterizedTest(name = "csv: {0}, rule[{index}] -> {2}, forIndividual: {3}")
  @MethodSource("filter_emptyRules")
  @DisplayName("Test empty and null rules, 0 criterion, 0 case")
  void filter_emptyRules(@ConvertWith(CsvRepoArgConverter.class) CsvRepository csvRepository,
                            List<List<AuthorizationCriterion>> rules,
                            int expectedCount,
                            boolean forIndividual) {
    assertEquals(expectedCount, EntitlementFilter.filter(csvRepository, rules, forIndividual).count());
  }

  static Stream<Arguments> filter_emptyRules() {
    List<List<AuthorizationCriterion>> rule1 = Collections.singletonList(Collections.emptyList());
    List<List<AuthorizationCriterion>> rule2 = null;

    return Stream.of(
        Arguments.of(CSV_PATH, rule1, 27, false),
        Arguments.of(CSV_PATH, rule1, 50, true),
        Arguments.of(CSV_PATH, rule2, 27, false),
        Arguments.of(CSV_PATH, rule2, 50, true)
    );
  }

  @ParameterizedTest(name = "csv: {0}, {1} {2} {3} -> {4}, forIndividual: {5}")
  @CsvSource({
          CSV_PATH + ", " + BuiltinAuthFields.BUILTIN_TABLE_ID + "//" + BuiltinAuthFields.HOUSEHOLD_SIZE_KEY + ", EQ, 1, 12, false",
          CSV_PATH + ", " + BuiltinAuthFields.BUILTIN_TABLE_ID + "//" + BuiltinAuthFields.HOUSEHOLD_SIZE_KEY + ", EQ, 2, 10, false",
          CSV_PATH + ", " + BuiltinAuthFields.BUILTIN_TABLE_ID + "//" + BuiltinAuthFields.HOUSEHOLD_SIZE_KEY + ", GT, 3, 2, false",
          CSV_PATH + ", " + BuiltinAuthFields.BUILTIN_TABLE_ID + "//" + BuiltinAuthFields.HOUSEHOLD_SIZE_KEY + ", GT, 3, 9, true",
  })
  @DisplayName("Test criterion field in Member base table, 1 criterion, 1 case")
  void filter_householdSize(@ConvertWith(CsvRepoArgConverter.class) CsvRepository csvRepository,
                            @ConvertWith(CriterionFieldArgConverter.class) CriterionField field,
                              CriterionOperator operator,
                              String value,
                              int expectedCount,
                              boolean forIndividual) {
    AuthorizationCriterion criterion = new AuthorizationCriterion(field, operator, value);
    List<List<AuthorizationCriterion>> rules = Collections.singletonList(Collections.singletonList(criterion));

    assertEquals(expectedCount, EntitlementFilter.filter(csvRepository, rules, forIndividual).count());
  }
}