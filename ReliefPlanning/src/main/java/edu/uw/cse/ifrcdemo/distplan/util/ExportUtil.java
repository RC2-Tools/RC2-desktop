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

package edu.uw.cse.ifrcdemo.distplan.util;

import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import edu.uw.cse.ifrcdemo.distplan.entity.VisitProgram;
import edu.uw.cse.ifrcdemo.distplan.logic.EntitlementFilter;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.authorization.AuthorizationRepository;
import edu.uw.cse.ifrcdemo.distplan.model.distribution.DistExporter;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.distribution.DistributionRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.entitlement.EntitlementRepository;
import edu.uw.cse.ifrcdemo.distplan.model.visitprogram.VisitProgramRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.*;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationType;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.DistVisitProgStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.IndividualStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.SuitcaseUploadOperation;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.BaseSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvAuthorization;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvDistribution;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvEntitlement;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvIndividual;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvSuitcaseSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvVisit;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvVisitProgram;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.Authorization;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.Distribution;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.Entitlement;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.ModelStub;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.Visit;
import edu.uw.cse.ifrcdemo.sharedlib.util.CsvDiffGenerator;
import edu.uw.cse.ifrcdemo.sharedlib.util.CsvMapperUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.FileUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.TestDataUtil;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.opendatakit.aggregate.odktables.rest.entity.RowFilterScope;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ExportUtil {
    private final DistributionRepository distributionRepository;
    private final AuthorizationRepository authorizationRepository;
    private final EntitlementRepository entitlementRepository;
    private final VisitProgramRepository visitProgramRepository;
    private final CsvRepository csvRepository;
    private final DistExporter distExporter;
    Map<String, IndividualStatus> statusMap;

    private final Logger logger;

    public ExportUtil(DistributionRepository distributionRepository,
                      AuthorizationRepository authorizationRepository,
                      EntitlementRepository entitlementRepository,
                      VisitProgramRepository visitProgramRepository,
                      CsvRepository csvRepository,
                      DistExporter distExporter,
                      Logger logger,
                      Map<String, IndividualStatus> statusMap
    ) {
        this.distributionRepository = distributionRepository;
        this.authorizationRepository = authorizationRepository;
        this.entitlementRepository = entitlementRepository;
        this.visitProgramRepository = visitProgramRepository;
        this.csvRepository = csvRepository;
        this.distExporter = distExporter;
        this.logger = logger;
        this.statusMap = statusMap;
    }

    public void export(Path outputPath) {
        distributionRepository
            .getAllDistributions()
            .forEach(distExporter::exportDistribution);

        writeChangesForEntitlement(outputPath);
        writeChangesForAuthorization(outputPath);
        writeChangesForDistribution(outputPath);
        writeChangesForVisitProgram(outputPath);
        writeChangesForVisit(outputPath);
        writeChangesForIndividual(statusMap, outputPath);
    }

    private static Map<VisitProgram, List<String>> applyFilterToVisitProgram(VisitProgramRepository programRepository,
                                                                             CsvRepository csvRepo) {
        return programRepository
            .getVisitProgramsWithStatus(DistVisitProgStatus.ACTIVE)
            .stream()
            .collect(Collectors.toMap(
                Function.identity(),
                x -> EntitlementFilter
                    .filter(csvRepo, x.getRules(), x.isForMember())
                    .collect(Collectors.toList())
            ));
    }

    private static CsvIndividual joinCsvIndividualAndStatusMap(CsvIndividual csvIndividual,
        Map<String, IndividualStatus> statusMap) {
        csvIndividual.setStatus(statusMap.get(csvIndividual.getRowId()));
        return csvIndividual;
    }

    private boolean writeChangesForIndividual(
        Map<String, IndividualStatus> statusMap,
        Path outputPath) {

        CsvMapper mapper = CsvMapperUtil.getCsvMapper(true);
        Path csvOutputPath = outputPath.resolve(FileUtil.getFileName(CsvIndividual.class));


        try (BufferedWriter writer = Files.newBufferedWriter(csvOutputPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            SequenceWriter sequenceWriter =
                CsvMapperUtil.getWriterForSuitcaseSyncRow(edu.uw.cse.ifrcdemo.sharedlib.model.stub.Individual.class, mapper, true).writeValues(writer)) {
            Map<String, CsvIndividual> indexedCsvIndividual = csvRepository
                .readIndexedTypedCsv(CsvIndividual.class)
                .orElseThrow(IllegalStateException::new);

            List<CsvSuitcaseSyncRow> csvIndList = indexedCsvIndividual
                .entrySet()
                .stream()
                .filter(ent -> statusMap.containsKey(ent.getKey()))
                .map(ent -> joinCsvIndividualAndStatusMap(ent.getValue(), statusMap))
                .map(row -> new CsvSuitcaseSyncRow<>(SuitcaseUploadOperation.FORCE_UPDATE, row))
                .collect(Collectors.toList());

            sequenceWriter
                .writeAll(csvIndList);
            return true;
        } catch (IOException e) {
            ResourceBundle translations = TranslationUtil.getTranslations();
            FxDialogUtil.showErrorDialog(
                String.format(translations.getString(TranslationConsts.CSV_IO_EXCEPTION_ERROR),
                    FileUtil.getFileName(CsvIndividual.class))
            );
            logger.catching(Level.ERROR, e);
            return false;
        }
    }

    private boolean writeChangesForDistribution(Path outputPath) {
        Stream<CsvDistribution> distFromDb = distributionRepository
            .getAllDistributions()
            .stream()
            .filter(dist -> dist.getStatus() != DistVisitProgStatus.REMOVED)
            .map(DistributionUtil::toCsvDistribution);

        Map<String, CsvDistribution> indexedDist = csvRepository
            .readIndexedTypedCsv(CsvDistribution.class)
            .orElseThrow(IllegalStateException::new);

        Stream<CsvDistribution> tokenDistFromCsv = csvRepository
            .readTypedCsv(CsvAuthorization.class)
            .orElseThrow(IllegalAccessError::new)
            .stream()
            .filter(auth -> auth.getType().equals(AuthorizationType.NO_REGISTRATION))
            .map(CsvAuthorization::getDistributionId)
            .map(indexedDist::get);

        List<CsvDistribution> newDistList = Stream
            .concat(distFromDb, tokenDistFromCsv)
            .collect(Collectors.toList());

        return writeChanges(outputPath, newDistList, Distribution.class, CsvDistribution.class);
    }

    private boolean writeChangesForEntitlement(Path outputPath) {
        List<CsvEntitlement> newEntList = entitlementRepository
            .getEntitlements()
            .join()
            .stream()
            .filter(ent -> ent.getAuthorization().getStatus() != AuthorizationStatus.REMOVED)
            .map(EntitlementUtil::toCsvEntitlement)
            .collect(Collectors.toList());

        return writeChanges(outputPath, newEntList, Entitlement.class, CsvEntitlement.class);
    }

    private boolean writeChangesForAuthorization(Path outputPath) {
        List<CsvAuthorization> csvAuthorizations = csvRepository
            .readTypedCsv(CsvAuthorization.class)
            .orElseThrow(IllegalStateException::new);

        List<CsvAuthorization> newAuthList = Stream
            .concat(
                Arrays
                    .asList(AuthorizationStatus.ACTIVE, AuthorizationStatus.DISABLED, AuthorizationStatus.INACTIVE)
                    .parallelStream()
                    .map(status -> authorizationRepository
                        .getAuthorizations(status)
                        .thenApply(list -> list.stream().map(AuthorizationUtil::toCsvAuthorization))
                    )
                    .flatMap(CompletableFuture::join),
                csvAuthorizations
                    .stream()
                    .filter(auth -> auth.getType().equals(AuthorizationType.NO_REGISTRATION))
            )
            .collect(Collectors.toList());

        return writeChanges(outputPath, newAuthList, Authorization.class, CsvAuthorization.class);
    }

    private boolean writeChangesForVisitProgram(Path outputPath) {
        CsvMapper mapper = CsvMapperUtil.getCsvMapper(true);
        Path csvOutputPath = outputPath.resolve(FileUtil.getFileName(CsvVisitProgram.class));

        List<CsvSuitcaseSyncRow<CsvVisitProgram>> suitcaseVisitProgramList = visitProgramRepository
            .getVisitPrograms(false)
            .join()
            .stream()
            .map(VisitProgramUtil::toCsvVisitProgram)
            .map(vp -> new CsvSuitcaseSyncRow<>(SuitcaseUploadOperation.FORCE_UPDATE, vp))
            .collect(Collectors.toList());

        try (BufferedWriter writer = Files.newBufferedWriter(csvOutputPath);
             SequenceWriter sequenceWriter = CsvMapperUtil.getWriterForSuitcaseSyncRow(edu.uw.cse.ifrcdemo.sharedlib.model.stub.VisitProgram.class, mapper, true).writeValues(writer)) {
            sequenceWriter.writeAll(suitcaseVisitProgramList);
        } catch (IOException e) {
            ResourceBundle translations = TranslationUtil.getTranslations();
            FxDialogUtil.showErrorDialog(
                String.format(translations.getString(TranslationConsts.CSV_IO_EXCEPTION_ERROR), FileUtil.getFileName(CsvVisitProgram.class))
            );
            logger.catching(Level.ERROR, e);
            return false;
        }

        return true;
    }

    public static List<CsvVisit> generateVisitUpdate(VisitProgramRepository visitProgramRepository,
                                                     CsvRepository csvRepository) {
        // TODO: Revisit visits generation

        Map<VisitProgram, List<String>> visitProgramVisitsMap =
            applyFilterToVisitProgram(visitProgramRepository, csvRepository);

        Map<String, CsvIndividual> indexedCsvMembers = csvRepository
            .readIndexedTypedCsv(CsvIndividual.class)
            .orElseThrow(IllegalStateException::new);

        List<CsvVisit> toUpdate = new ArrayList<>();

        for (Map.Entry<VisitProgram, List<String>> entry : visitProgramVisitsMap.entrySet()) {
            for (String rowId : entry.getValue()) {
                String beneficiaryEntityRowId = indexedCsvMembers.get(rowId).getBeneficiaryEntityRowId();

                Optional<CsvVisit> visit = csvRepository
                    .readTypedCsv(CsvVisit.class)
                    .orElseGet(Collections::emptyList)
                    .stream()
                    .filter(v -> v.getVisitProgramId().equals(entry.getKey().getRowId()))
                    .filter(v -> v.getBeneficiaryUnitId().equals(beneficiaryEntityRowId))
                    .findAny();

                if (visit.isPresent()) {
                    // this record is already there
                    continue;
                }

                CsvVisit csvVisit = new CsvVisit();

                csvVisit.setBeneficiaryUnitId(beneficiaryEntityRowId);
                csvVisit.setMemberId(rowId);
                csvVisit.setVisitProgramId(entry.getKey().getRowId());

                // set to null for device
                csvVisit.setCustomVisitRowId(null);

                csvVisit.setCustomVisitFormId(entry.getKey().getCustomVisitForm().getFormId());
                csvVisit.setCustomVisitTableId(entry.getKey().getCustomVisitForm().getTableId());

                csvVisit.setRowId(TestDataUtil.syncUuidGenerator());
                csvVisit.setRowFilterScope(RowFilterScopeUtil.emptyScope(RowFilterScope.Access.FULL));

                toUpdate.add(csvVisit);
            }
        }

        return toUpdate;
    }

    private boolean writeChangesForVisit(Path outputPath) {
        List<CsvSuitcaseSyncRow<CsvVisit>> toUpdate = generateVisitUpdate(visitProgramRepository, csvRepository)
            .stream()
            .map(visit -> new CsvSuitcaseSyncRow<>(SuitcaseUploadOperation.FORCE_UPDATE, visit))
            .collect(Collectors.toList());

        CsvMapper mapper = CsvMapperUtil.getCsvMapper(true);
        Path csvOutputPath = outputPath.resolve(FileUtil.getFileName(CsvVisit.class));

        try (BufferedWriter writer = Files.newBufferedWriter(csvOutputPath);
             SequenceWriter sequenceWriter = CsvMapperUtil.getWriterForSuitcaseSyncRow(Visit.class, mapper, true).writeValues(writer)) {
            sequenceWriter.writeAll(toUpdate);
        } catch (IOException e) {
            ResourceBundle translations = TranslationUtil.getTranslations();
            FxDialogUtil.showErrorDialog(
                String.format(translations.getString(TranslationConsts.CSV_IO_EXCEPTION_ERROR), FileUtil.getFileName(CsvVisit.class))
            );
            logger.catching(Level.ERROR, e);
            return false;
        }

        return true;
    }

    private <T extends BaseSyncRow> boolean writeChanges(Path outputPath,
                                                         Collection<T> updatedRows,
                                                         Class<? extends ModelStub> stubClass,
                                                         Class<T> csvClass) {
        CsvMapper mapper = CsvMapperUtil.getCsvMapper(true);
        Path csvOutputPath = outputPath.resolve(FileUtil.getFileName(csvClass));

        try (BufferedWriter writer = Files.newBufferedWriter(csvOutputPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
             SequenceWriter sequenceWriter = CsvMapperUtil.getWriterForSuitcaseSyncRow(stubClass, mapper, true).writeValues(writer)) {
            Map<String, T> origCsvIndexed = csvRepository
                .readIndexedTypedCsv(csvClass)
                .orElseThrow(IllegalStateException::new);

            for (T updatedRow : updatedRows) {
                CsvUtil.copyMetadataFields(origCsvIndexed.get(updatedRow.getRowId()), updatedRow);
            }

            sequenceWriter.writeAll(CsvDiffGenerator.generate(origCsvIndexed.values(), updatedRows));
        } catch (IOException e) {
            ResourceBundle translations = TranslationUtil.getTranslations();
            FxDialogUtil.showErrorDialog(String.format(
                translations.getString(TranslationConsts.CSV_IO_EXCEPTION_ERROR),
                FileUtil.getFileName(csvClass)
            ));
            logger.catching(Level.ERROR, e);

            return false;
        }

        return true;
    }
}
