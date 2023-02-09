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

import edu.uw.cse.ifrcdemo.planningsharedlib.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationType;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.BeneficiaryEntityStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.IndividualStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.*;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.HasCustomTable;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.Individual;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.SyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.util.FileUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.StringUtil;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class BaseCsvValidator {

    private static final String NON_EMPTY_STRING = "STRING";

    public Predicate<CsvBeneficiaryEntity> validateBeneficiaryEntity(CsvRepository csvRepo) {
      return beneficiaryEntityDisabled()
          .or(validateBeneficiaryEntityColumns()
              .and(noCustomTable().or(formIdValidator(csvRepo)))
              .and(noCustomTable().or(customTableRowIdValidator(csvRepo)))
          );
    }

    protected Predicate<CsvBeneficiaryEntity> beneficiaryEntityDisabled() {
      return csvBeneficiaryEntity -> csvBeneficiaryEntity.getStatus() == BeneficiaryEntityStatus.DISABLED;
    }

    protected Predicate<CsvBeneficiaryEntity> validateBeneficiaryEntityColumns() {
      return columnValidator(Stream.of(
          SyncRow::getRowId,
          CsvBeneficiaryEntity::getBeneficiaryEntityId,
          CsvBeneficiaryEntity::getDateCreated,
          CsvBeneficiaryEntity::getStatus
      ));
    }

    public Predicate<CsvDistribution> validateDistribution() {
        return columnValidator(Stream.of(
                SyncRow::getRowId,
                CsvDistribution::getName,
                CsvDistribution::getStatus
        ));
    }

    public Predicate<CsvAuthorization> validateAuthorization(CsvRepository csvRepo) {
        return this.<CsvAuthorization>columnValidator(Stream.of(
                        SyncRow::getRowId,
                        CsvAuthorization::getType,
                        CsvAuthorization::getStatus,
                        CsvAuthorization::getDistributionId
                ))
                .and(crossTableIdValidator(
                        CsvDistribution.class,
                        CsvAuthorization::getDistributionId,
                        csvRepo
                ));
    }

    public Predicate<CsvEntitlement> validateEntitlement() {
        return columnValidator(Stream.of(
                SyncRow::getRowId,
                CsvEntitlement::getAuthorizationId,
                CsvEntitlement::getAuthorizationType,
                CsvEntitlement::getDateCreated,
                CsvEntitlement::getIsOverride,
                CsvEntitlement::getItemId,
                CsvEntitlement::getStatus,
                csvEntitlement -> csvEntitlement.getAuthorizationType() == AuthorizationType.REQUIRED_REGISTRATION ?
                        csvEntitlement.getBeneficiaryEntityId() :
                        NON_EMPTY_STRING // just needs some non-empty string
        ));
    }

    public Predicate<CsvIndividual> validateIndividual(CsvRepository csvRepo) {
      return individualDisabled()
          .or(validateIndividualColumns()
              .and(noCustomTable().or(formIdValidator(csvRepo)))
              .and(noCustomTable().or(customTableRowIdValidator(csvRepo)))
              .and(crossTableIdValidator(CsvBeneficiaryEntity.class, Individual::getBeneficiaryEntityRowId, csvRepo))
          );
    }

    protected Predicate<CsvIndividual> individualDisabled() {
      return csvIndividual -> csvIndividual.getStatus() == IndividualStatus.DISABLED;
    }

    protected Predicate<CsvIndividual> validateIndividualColumns() {
      return columnValidator(Stream.of(
          SyncRow::getRowId,
          CsvIndividual::getBeneficiaryEntityRowId,
          CsvIndividual::getDateCreated,
          CsvIndividual::getStatus
      ));
    }

    protected <T extends BaseSyncRow> Predicate<T> columnValidator(Stream<Function<T, Object>> valueExtractors) {
      return valueExtractors
          .map(keyGetter -> (Predicate<T>) row -> {
            Object value = keyGetter.apply(row);
            return value != null && (!(value instanceof String) || !((String) value).isEmpty());
          })
          .reduce(Predicate::and)
          .orElse(__ -> true);
    }

    protected Predicate<HasCustomTable> formIdValidator(CsvRepository csvRepo) {
      return formIdValidator(csvRepo, HasCustomTable::getCustomTableFormId);
    }

    protected <T> Predicate<T> formIdValidator(CsvRepository csvRepo, Function<T, String> colExtractor) {
      return colExtractor
          .andThen(FileUtil::getFileName)
          .andThen(csvRepo::readUntypedCsv)
          .andThen(Optional::isPresent)
          ::apply;
    }

    protected Predicate<HasCustomTable> customTableRowIdValidator(CsvRepository csvRepo) {
      return row -> csvRepo
          .readIndexedUntypedCsv(FileUtil.getFileName(row.getCustomTableFormId()))
          .orElseGet(Collections::emptyMap)
          .containsKey(row.getCustomTableRowId());
    }

    protected Predicate<HasCustomTable> noCustomTable() {
      return row -> StringUtil.isNullOrEmpty(row.getCustomTableFormId()) &&
          StringUtil.isNullOrEmpty(row.getCustomTableRowId());
    }

    protected Predicate<HasCustomTable> noCustomTableRowId() {
      return row -> StringUtil.isNullOrEmpty(row.getCustomTableRowId());
    }

    protected <T extends BaseSyncRow> Predicate<T> crossTableIdValidator(Function<T, String> formIdExtractor,
                                                                         Function<T, String> refIdExtractor,
                                                                         CsvRepository csvRepo) {
      return row -> csvRepo
          .readIndexedUntypedCsv(FileUtil.getFileName(formIdExtractor.apply(row)))
          .orElseGet(Collections::emptyMap)
          .containsKey(refIdExtractor.apply(row));
    }

    protected <T extends BaseSyncRow> Predicate<T> crossTableIdValidator(Class<? extends BaseSyncRow> foreignTable,
                                                                         Function<T, String> refIdExtractor,
                                                                         CsvRepository csvRepo) {
      return row -> csvRepo
          .readIndexedTypedCsv(foreignTable)
          .orElseGet(Collections::emptyMap)
          .containsKey(refIdExtractor.apply(row));
    }
}
