package edu.uw.cse.ifrcdemo.distplan.logic;

import edu.uw.cse.ifrcdemo.distplan.CsvRepoArgConverter;
import edu.uw.cse.ifrcdemo.distplan.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvBeneficiaryEntity;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvIndividual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvValidatorCrossTableTest {
  private static final String CSV_PATH = "csv/50_member";

  private CsvValidator csvValidator;

  @BeforeEach
  void initCsvValidator() {
    csvValidator = new CsvValidator();
  }

  @ParameterizedTest
  @ValueSource(strings = {CSV_PATH})
  @DisplayName("Validate valid Individuals")
  void validIndividual(@ConvertWith(CsvRepoArgConverter.class) CsvRepository csvRepository) {
    assertAll(csvRepository
        .readTypedCsv(CsvIndividual.class)
        .orElseThrow(IllegalStateException::new)
        .stream()
        .map(row ->
            () -> assertTrue(csvValidator.validateIndividual(csvRepository).test(row), row::getRowId)
        )
    );
  }

  @ParameterizedTest
  @ValueSource(strings = {CSV_PATH})
  @DisplayName("Validate valid Beneficiary Entities")
  void validBeneficiaryEntity(@ConvertWith(CsvRepoArgConverter.class) CsvRepository csvRepository) {
    assertAll(csvRepository
        .readTypedCsv(CsvBeneficiaryEntity.class)
        .orElseThrow(IllegalStateException::new)
        .stream()
        .map(row ->
            () -> assertTrue(csvValidator.validateBeneficiaryEntity(csvRepository).test(row), row::getRowId)
        )
    );
  }
}