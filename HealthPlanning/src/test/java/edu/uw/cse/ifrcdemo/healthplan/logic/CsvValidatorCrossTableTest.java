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

package edu.uw.cse.ifrcdemo.healthplan.logic;

import edu.uw.cse.ifrcdemo.healthplan.CsvRepoArgConverter;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.csv.CsvRepository;
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