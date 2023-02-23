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

package edu.uw.cse.ifrcdemo.distplan.data;

import edu.uw.cse.ifrcdemo.distplan.logic.CsvValidator;
import edu.uw.cse.ifrcdemo.distplan.model.authorization.AuthorizationRepository;
import edu.uw.cse.ifrcdemo.distplan.model.authorization.DbAuthorizationRepository;
import edu.uw.cse.ifrcdemo.distplan.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.distplan.model.csv.FileCsvRepository;
import edu.uw.cse.ifrcdemo.distplan.model.distribution.DistributionRepository;
import edu.uw.cse.ifrcdemo.distplan.model.entitlement.EntitlementRepository;
import edu.uw.cse.ifrcdemo.distplan.model.itempack.ItemPackRepository;
import edu.uw.cse.ifrcdemo.distplan.model.preference.PreferencesStore;
import edu.uw.cse.ifrcdemo.distplan.model.rctemplate.RcTemplateRepository;
import edu.uw.cse.ifrcdemo.distplan.model.region.RegionRepository;
import edu.uw.cse.ifrcdemo.distplan.model.visitprogram.VisitProgramRepository;
import edu.uw.cse.ifrcdemo.distplan.util.AuxiliaryPropertyUtil;
import edu.uw.cse.ifrcdemo.distplan.util.CsvFileUtil;
import edu.uw.cse.ifrcdemo.distplan.util.DbUtil;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.sharedlib.model.config.AuxiliaryProperty;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvAuthorization;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvAuthorizationReport;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvBeneficiaryEntity;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvDelivery;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvDistribution;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvEntitlement;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvIndividual;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvVisit;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvVisitProgram;
import edu.uw.cse.ifrcdemo.sharedlib.util.FileUtil;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;

import javax.persistence.EntityManagerFactory;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

public class DataInstance {

    private static DataRepos repos;

    private static boolean refresh = false;

    public static DataRepos getDataRepos() {
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

        Path dbPath = DbUtil.copyDb(profile);

        EntityManagerFactory emf = DbUtil.createEntityManagerFactory(dbPath.toAbsolutePath().toString());

        AuthorizationRepository authorizationRepository = new DbAuthorizationRepository(emf);
        DistributionRepository distributionRepository = new DistributionRepository(emf);
        EntitlementRepository entitlementRepository = new EntitlementRepository(emf);
        ItemPackRepository itemPackRepository = new ItemPackRepository(emf);
        RcTemplateRepository templateRepository = new RcTemplateRepository(emf);
        RegionRepository regionRepository = new RegionRepository(emf);
        VisitProgramRepository visitProgramRepository = new VisitProgramRepository(emf);

        CsvRepository csvRepo = new FileCsvRepository();
        PreferencesStore preferencesStore = new PreferencesStore();
        preferencesStore.setInputPath(snapshot);
        AuxiliaryProperty auxiliaryProperty = AuxiliaryPropertyUtil.readConfigJsonFile(snapshot);

        DataInstance.readAndValidateCsvs(snapshot, csvRepo);

        return entitlementRepository
            .updateOrInsertFromCsv(csvRepo)
            .thenRunAsync(() -> {
                repos = new DataRepos(profile.getFileName().toString(),
                    emf,
                    authorizationRepository,
                    auxiliaryProperty,
                    csvRepo,
                    distributionRepository,
                    entitlementRepository,
                    itemPackRepository,
                    templateRepository,
                    regionRepository,
                    visitProgramRepository,
                    preferencesStore
                );
                refresh = false;
            });
    }


 ///////////////////////////////////////////////
 /////// HELPERS ///////////
 ///////////////////////////

    private static void readAndValidateCsvs(Path dataPath, CsvRepository csvRepo) throws IOException {
        try {
            String errors = readAllCsvs(dataPath, csvRepo)
                .thenApplyAsync(DataInstance::validateCsvs)
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
                CsvFileUtil.readBaseTableWithCustomTable(dataPath, CsvVisit.class, csvRepo),
                CsvFileUtil.readBaseTableWithCustomTable(dataPath, CsvVisitProgram.class,
                    CsvVisitProgram::getCustomVisitTableId, csvRepo),
                CsvFileUtil.readBaseTable(dataPath, CsvEntitlement.class, csvRepo),
                CsvFileUtil.readBaseTable(dataPath, CsvAuthorization.class, csvRepo),
                CsvFileUtil.readBaseTable(dataPath, CsvDistribution.class, csvRepo),
                CsvFileUtil.readBaseTableWithCustomTable(dataPath, CsvDelivery.class, csvRepo),
                CsvFileUtil.readBaseTable(dataPath, CsvAuthorizationReport.class, csvRepo)
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

        boolean individualsStatus = csvRepo
            .readTypedCsv(CsvIndividual.class)
            .orElseThrow(IllegalStateException::new)
            .stream()
            .allMatch(csvValidator.validateIndividual(csvRepo));

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


        boolean visitProgramStatus = csvRepo
            .readTypedCsv(CsvVisitProgram.class)
            .orElseThrow(IllegalStateException::new)
            .stream()
            .allMatch(csvValidator.validateVisitProgram(csvRepo));

        boolean visitStatus = csvRepo
            .readTypedCsv(CsvVisit.class)
            .orElseThrow(IllegalStateException::new)
            .stream()
            .allMatch(csvValidator.validateVisit(csvRepo));

        boolean distributionStatus = csvRepo
            .readTypedCsv(CsvDistribution.class)
            .orElseThrow(IllegalStateException::new)
            .stream()
            .allMatch(csvValidator.validateDistribution());

        List<String> invalidMsgs = new ArrayList<>();
        if (!beneficiaryEntitiesStatus) {
            invalidMsgs.add("***************");
            invalidMsgs.add(FileUtil.getFileName(CsvBeneficiaryEntity.class));
            invalidMsgs.add("---------------");
            invalidMsgs.add("RCID and ROW_ID");
            invalidMsgs.add("---------------");
            List<CsvBeneficiaryEntity> beneficiaryProblemRows = csvRepo
                    .readTypedCsv(CsvBeneficiaryEntity.class)
                    .orElseThrow(IllegalStateException::new)
                    .stream().filter(csvValidator.validateBeneficiaryEntity(csvRepo).negate()).
                            collect(Collectors.toList());
            for(CsvBeneficiaryEntity entity : beneficiaryProblemRows) {
                invalidMsgs.add(entity.getBeneficiaryEntityId() + GenConsts.SPACE + entity.getRowId());
            }
            invalidMsgs.add("***************");
        }

        if (!individualsStatus) {
            invalidMsgs.add("***************");
            invalidMsgs.add(FileUtil.getFileName(CsvIndividual.class));
            invalidMsgs.add("---------------");
            invalidMsgs.add("ROW_ID");
            invalidMsgs.add("---------------");
            List<CsvIndividual> individualProblemRows = csvRepo
                    .readTypedCsv(CsvIndividual.class)
                    .orElseThrow(IllegalStateException::new)
                    .stream().filter(csvValidator.validateIndividual(csvRepo).negate()).
                            collect(Collectors.toList());
            for(CsvIndividual entity : individualProblemRows) {
                invalidMsgs.add(entity.getRowId());
            }
            invalidMsgs.add("***************");
        }

        if (!entitlementsStatus) {
            invalidMsgs.add("***************");
            invalidMsgs.add(FileUtil.getFileName(CsvEntitlement.class));
            invalidMsgs.add("***************");
        }

        if (!authorizationStatus) {
            invalidMsgs.add("***************");
            invalidMsgs.add(FileUtil.getFileName(CsvAuthorization.class));
            invalidMsgs.add("***************");
        }

        if (!visitProgramStatus) {
            invalidMsgs.add("***************");
            invalidMsgs.add(FileUtil.getFileName(CsvVisitProgram.class));
            invalidMsgs.add("***************");
        }

        if (!visitStatus) {
            invalidMsgs.add("***************");
            invalidMsgs.add(FileUtil.getFileName(CsvVisit.class));
            invalidMsgs.add("---------------");
            invalidMsgs.add("ROW_ID");
            invalidMsgs.add("---------------");
            List<CsvVisit> visitProblemRows = csvRepo
                    .readTypedCsv(CsvVisit.class)
                    .orElseThrow(IllegalStateException::new)
                    .stream().filter(csvValidator.validateVisit(csvRepo).negate()).
                            collect(Collectors.toList());
            for(CsvVisit entity : visitProblemRows) {
                invalidMsgs.add(entity.getRowId());
            }
            invalidMsgs.add("***************");
        }

        if (!distributionStatus) {
            invalidMsgs.add(FileUtil.getFileName(CsvDistribution.class));
        }

        if (!invalidMsgs.isEmpty()) {
            String invalidCsvMsg = TranslationUtil
                .getTranslations()
                .getString(TranslationConsts.INVALID_CSV_ERROR);

            invalidMsgs.add(0, invalidCsvMsg);
            return String.join(GenConsts.NEW_LINE, invalidMsgs);
        }

        return null;
    }
}
