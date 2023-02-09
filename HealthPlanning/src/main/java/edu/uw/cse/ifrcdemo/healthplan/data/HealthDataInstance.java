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

package edu.uw.cse.ifrcdemo.healthplan.data;

import edu.uw.cse.ifrcdemo.healthplan.logic.CsvValidator;
import edu.uw.cse.ifrcdemo.healthplan.model.DonorRepository;
import edu.uw.cse.ifrcdemo.healthplan.model.HealthServiceRepository;
import edu.uw.cse.ifrcdemo.healthplan.model.HealthServicesForTaskRespository;
import edu.uw.cse.ifrcdemo.healthplan.model.HealthTaskRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.data.InvalidCsvException;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.authorization.AuthorizationRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.authorization.DbAuthorizationRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.csv.FileCsvRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.distribution.DistributionRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.entitlement.EntitlementRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.item.ItemRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.location.LocationRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.preference.PreferencesStore;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.rctemplate.RcTemplateRepository;
import edu.uw.cse.ifrcdemo.healthplan.util.AuxiliaryPropertyUtil;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.CsvFileUtil;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.DbUtil;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.sharedlib.model.config.AuxiliaryProperty;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.*;
import edu.uw.cse.ifrcdemo.sharedlib.util.FileUtil;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import javax.persistence.EntityManagerFactory;

public class HealthDataInstance {

    public static final String DB_EXTERNAL_FILENAME = "health-planning.db";
    public static final String HEALTH_DATABASE = "HealthDatabase";
    private static HealthDataRepos repos;

    private static boolean refresh = false;

    public static HealthDataRepos getDataRepos() {
        return repos;
    }

    public static void clearRepos() throws IllegalAccessException {
        if (refresh) {
            throw new IllegalAccessException("Cannot Clear Repo when in middle of loading data");
        }
        if(repos != null) {
            repos.close();
            repos = null;
        }
    }

    //returns true if input data was successfully loaded
    public static synchronized CompletableFuture<Void> loadInputDataSource(Path profile, Path snapshot)
        throws IOException {

        if (refresh)
            return null;

        if (!Files.exists(profile) || !Files.isDirectory(profile)) {
            throw new IOException("Unable to find input directory");
        }
        refresh = true;
        repos = null;

        Path dbPath = DbUtil.copyDb(profile, DB_EXTERNAL_FILENAME);

        EntityManagerFactory emf = DbUtil.createEntityManagerFactory(dbPath.toAbsolutePath().toString(), HEALTH_DATABASE);


        LocationRepository locationRepository = new LocationRepository(emf);

        CsvRepository csvRepo = new FileCsvRepository();
        RcTemplateRepository templateRepository = new RcTemplateRepository(emf);
        ItemRepository itemRepository = new ItemRepository(emf);

        AuthorizationRepository authorizationRepository = new DbAuthorizationRepository(emf);
        DistributionRepository distributionRepository = new DistributionRepository(emf);
        EntitlementRepository entitlementRepository = new EntitlementRepository(emf);

        PreferencesStore preferencesStore = new PreferencesStore();
        preferencesStore.setInputPath(snapshot);
        AuxiliaryProperty auxiliaryProperty = AuxiliaryPropertyUtil.readConfigJsonFile(snapshot);

        HealthServiceRepository healthServiceRepository = new HealthServiceRepository(emf);
        DonorRepository donorRepository = new DonorRepository(emf);
        HealthTaskRepository healthTaskRepository = new HealthTaskRepository(emf);
        HealthServicesForTaskRespository hsftRepository = new HealthServicesForTaskRespository(emf);

        HealthDataInstance.readAndValidateCsvs(snapshot, csvRepo);
        return entitlementRepository
                .updateOrInsertFromCsv(csvRepo)
                .thenRunAsync(() -> {
                    repos = new HealthDataRepos(profile.getFileName().toString(),
                        emf,
                        authorizationRepository,
                        auxiliaryProperty,
                        csvRepo,
                        distributionRepository,
                        entitlementRepository,
                        templateRepository,
                        locationRepository,
                        itemRepository,
                        preferencesStore,
                        healthServiceRepository,
                        donorRepository,
                        healthTaskRepository,
                        hsftRepository
                    );
                    refresh = false;
                });
//        repos = new HealthDataRepos(profile.getFileName().toString(),
//                emf,
//                authorizationRepository,
//                auxiliaryProperty,
//                csvRepo,
//                distributionRepository,
//                entitlementRepository,
//                templateRepository,
//                locationRepository,
//                itemRepository,
//                preferencesStore,
//                healthServiceRepository,
//                donorRepository,
//                healthTaskRepository,
//                hsftRepository
//        );
//        refresh = false;
//        return CompletableFuture.runAsync(() -> {;});
    }


 ///////////////////////////////////////////////
 /////// HELPERS ///////////
 ///////////////////////////

    private static void readAndValidateCsvs(Path dataPath, CsvRepository csvRepo) throws IOException {
        try {
            String errors = readAllCsvs(dataPath, csvRepo)
                .thenApplyAsync(HealthDataInstance::validateCsvs)
                .join();

            if (errors != null) {
                throw new InvalidCsvException(errors);
            }
        } catch (CompletionException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }

            if (e.getCause() instanceof UncheckedIOException) {
                throw ((UncheckedIOException) e.getCause()).getCause();
            }

            throw e;
        }
    }

    private static CompletableFuture<CsvRepository> readAllCsvs(Path dataPath, CsvRepository csvRepo) {
        return CompletableFuture
            .allOf(
                CsvFileUtil.readBaseTableWithCustomTable(dataPath, CsvBeneficiaryEntity.class, csvRepo),
                CsvFileUtil.readBaseTableWithCustomTable(dataPath, CsvIndividual.class, csvRepo),
                CsvFileUtil.readBaseTable(dataPath, CsvHealthTask.class, csvRepo),
                CsvFileUtil.readBaseTable(dataPath, CsvHealthService.class, csvRepo),
                CsvFileUtil.readBaseTable(dataPath, CsvHealthServicesForTask.class, csvRepo),
                    CsvFileUtil.readBaseTable(dataPath, CsvAuthorization.class, csvRepo),
                    CsvFileUtil.readBaseTable(dataPath, CsvDistribution.class, csvRepo),
                    CsvFileUtil.readBaseTable(dataPath, CsvEntitlement.class, csvRepo),
                    CsvFileUtil.readBaseTable(dataPath, CsvAuthorization.class, csvRepo),
                    CsvFileUtil.readBaseTable(dataPath, CsvDistribution.class, csvRepo)
            )
            .thenApplyAsync(__ -> csvRepo);
    }

    private static String validateCsvs(CsvRepository csvRepo) {
        CsvValidator csvValidator = new CsvValidator();

        boolean beneficiaryEntitiesStatus = csvRepo
            .readTypedCsv(CsvBeneficiaryEntity.class)
            .orElseThrow(IllegalStateException::new)
            .stream()
            .allMatch(csvValidator.validateBeneficiaryEntity(csvRepo));

        boolean entitlementsStatus = csvRepo
                .readTypedCsv(CsvEntitlement.class)
                .orElseThrow(IllegalStateException::new)
                .stream()
                .allMatch(csvValidator.validateEntitlement());

        boolean authorizationStatus = csvRepo
                .readTypedCsv(CsvAuthorization.class)
                .orElseThrow(IllegalStateException::new)
                .stream()
                .allMatch(csvValidator.validateAuthorization(csvRepo));

        boolean distributionStatus = csvRepo
                .readTypedCsv(CsvDistribution.class)
                .orElseThrow(IllegalStateException::new)
                .stream()
                .allMatch(csvValidator.validateDistribution());

        boolean individualsStatus = csvRepo
            .readTypedCsv(CsvIndividual.class)
            .orElseThrow(IllegalStateException::new)
            .stream()
            .allMatch(csvValidator.validateIndividual(csvRepo));

        boolean healthTaskStatus =
            csvRepo.readTypedCsv(CsvHealthTask.class).orElseThrow(IllegalStateException::new)
                    .stream().allMatch(csvValidator.validateHealthTask(csvRepo));

        boolean healthServiceStatus = csvRepo
                .readTypedCsv(CsvHealthService.class)
                .orElseThrow(IllegalStateException::new)
                .stream()
                .allMatch(csvValidator.validateHealthService(csvRepo));

        boolean healthServicesForTaskStatus = csvRepo
                .readTypedCsv(CsvHealthServicesForTask.class)
                .orElseThrow(IllegalStateException::new)
                .stream()
                .allMatch(csvValidator.validateHealthServicesForTask(csvRepo));

        List<String> invalidTables = new ArrayList<>();
        if (!beneficiaryEntitiesStatus) {
            invalidTables.add(FileUtil.getFileName(CsvBeneficiaryEntity.class));
        }

        if (!individualsStatus) {
            invalidTables.add(FileUtil.getFileName(CsvIndividual.class));
        }

        if (!healthTaskStatus) {
            invalidTables.add(FileUtil.getFileName(CsvHealthTask.class));
        }

        if (!healthServiceStatus) {
            invalidTables.add(FileUtil.getFileName(CsvHealthService.class));
        }

        if (!healthServicesForTaskStatus) {
            invalidTables.add(FileUtil.getFileName(CsvHealthServicesForTask.class));
        }

        if (!invalidTables.isEmpty()) {
            String invalidCsvMsg = TranslationUtil
                .getTranslations()
                .getString(TranslationConsts.INVALID_CSV_ERROR);

            invalidTables.add(0, invalidCsvMsg);
            return String.join(GenConsts.NEW_LINE, invalidTables);
        }

        return null;
    }
}
