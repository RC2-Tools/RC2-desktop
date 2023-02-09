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

package edu.uw.cse.ifrcdemo.healthplan.util;

import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import edu.uw.cse.ifrcdemo.healthplan.logic.HealthTaskStatus;
import edu.uw.cse.ifrcdemo.healthplan.model.HealthServiceRepository;
import edu.uw.cse.ifrcdemo.healthplan.model.HealthServicesForTaskRespository;
import edu.uw.cse.ifrcdemo.healthplan.model.HealthTaskRepository;
import edu.uw.cse.ifrcdemo.healthplan.model.distribution.DistExporter;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.authorization.AuthorizationRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.distribution.DistributionRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.entitlement.EntitlementRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.*;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.*;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.*;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.*;
import edu.uw.cse.ifrcdemo.sharedlib.util.CsvDiffGenerator;
import edu.uw.cse.ifrcdemo.sharedlib.util.CsvMapperUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.FileUtil;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class ExportUtil {
    private final HealthTaskRepository healthTaskRepository;
    private final HealthServiceRepository healthServiceRepository;
    private final HealthServicesForTaskRespository hsftRepository;
    private final DistributionRepository distributionRepository;
    private final AuthorizationRepository authorizationRepository;
    private final EntitlementRepository entitlementRepository;
    private final CsvRepository csvRepository;
    private final DistExporter distExporter;
    Map<String, IndividualStatus> statusMap;

    private final Logger logger;

    public ExportUtil(HealthTaskRepository healthTaskRespository,
                      HealthServiceRepository healthServiceRepository,
                      HealthServicesForTaskRespository healthServicesForTaskRespository,
                      DistributionRepository distributionRepository,
                      AuthorizationRepository authorizationRepository,
                      EntitlementRepository entitlementRepository,
                      CsvRepository csvRepository,
                      DistExporter distExporter,
                      Logger logger,
                      Map<String, IndividualStatus> statusMap
    ) {
        this.healthTaskRepository = healthTaskRespository;
        this.healthServiceRepository = healthServiceRepository;
        this.distributionRepository = distributionRepository;
        this.authorizationRepository = authorizationRepository;
        this.entitlementRepository = entitlementRepository;
        this.hsftRepository = healthServicesForTaskRespository;
        this.csvRepository = csvRepository;
        this.logger = logger;
        this.statusMap = statusMap;
        this.distExporter = distExporter;
    }

    public void export(Path outputPath) {
        distributionRepository
                .getAllDistributions()
                .forEach(distExporter::exportDistribution);

        writeChangesForHealthTask(outputPath);
        writeChangesForHealthServices(outputPath);
        writeChangesForHealthServicesForTask(outputPath);
        writeChangesForEntitlement(outputPath);
        writeChangesForAuthorization(outputPath);
        writeChangesForDistribution(outputPath);
        //    writeChangesForIndividual(statusMap, outputPath);
    }


    private boolean writeChangesForHealthTask(Path outputPath) {
        List<CsvHealthTask> htFromDb = healthTaskRepository.getAllHealthTasks()
                .stream()
                .filter(ht -> !ht.getStatus().equals(HealthTaskStatus.DISABLED.name()))
                .map(HealthTaskUtil::toCsvHealthTask)
                .collect(Collectors.toList());

      //  Map<String, CsvHealthTask> indexedHt = csvRepository
      //          .readIndexedTypedCsv(CsvHealthTask.class)
      //          .orElseThrow(IllegalStateException::new);


        return writeChanges(outputPath, htFromDb, Program.class, CsvHealthTask.class);
    }

    private boolean writeChangesForHealthServices(Path outputPath) {

        List<CsvHealthService> serviceFromDb = healthServiceRepository.getAllHealthServices()
                .stream()
                .map(HealthServiceUtil::toCsvHealthService)
                .collect(Collectors.toList());

        Map<String, CsvHealthService> indexedHt = csvRepository
                .readIndexedTypedCsv(CsvHealthService.class)
                .orElseThrow(IllegalStateException::new);


        return writeChanges(outputPath, serviceFromDb, Service.class, CsvHealthService.class);
    }

    private boolean writeChangesForHealthServicesForTask(Path outputPath) {

        List<CsvHealthServicesForTask> activeServicesForTasks = new ArrayList<CsvHealthServicesForTask>();

        List<CsvHealthTask> activeHtFromDb = healthTaskRepository.getAllHealthTasks()
                .stream()
                .filter(ht -> !ht.getStatus().equals(HealthTaskStatus.DISABLED.name()))
                .map(HealthTaskUtil::toCsvHealthTask)
                .collect(Collectors.toList());

        for(CsvHealthTask ht : activeHtFromDb) {
            List<CsvHealthServicesForTask> servicesForTask = hsftRepository.getHealthServicesForTask(ht.getRowId())
                .stream()
                .map(HealthServicesForTaskUtil::toCsvHealthServicesForTask)
                .collect(Collectors.toList());

            if(servicesForTask != null && !servicesForTask.isEmpty()) {
                activeServicesForTasks.addAll(servicesForTask);
            }
        }

        return writeChanges(outputPath, activeServicesForTasks, ServicesForProgram.class, CsvHealthServicesForTask.class);
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
