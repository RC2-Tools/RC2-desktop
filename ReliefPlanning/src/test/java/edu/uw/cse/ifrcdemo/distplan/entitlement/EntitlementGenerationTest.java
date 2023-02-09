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

package edu.uw.cse.ifrcdemo.distplan.entitlement;

import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import edu.uw.cse.ifrcdemo.distplan.ReliefAppSystem;
import edu.uw.cse.ifrcdemo.distplan.data.ReliefDataInstance;
import edu.uw.cse.ifrcdemo.distplan.data.ReliefDataRepos;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Authorization;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Distribution;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Entitlement;
import edu.uw.cse.ifrcdemo.distplan.model.distribution.DistExporter;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.distribution.DistributionRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.entitlement.EntitlementRepository;
import edu.uw.cse.ifrcdemo.distplan.model.rctemplate.RcDistributionTemplate;
import edu.uw.cse.ifrcdemo.distplan.model.rctemplate.RcTemplateTestBase;
import edu.uw.cse.ifrcdemo.distplan.model.rctemplate.RcTemplateUtil;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.AuthorizationUtil;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.DistributionUtil;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.EntitlementUtil;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Item;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Location;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.RcTemplate;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.item.ItemRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.rctemplate.RcTemplateRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.location.LocationRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.InternalFileStoreUtil;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.DistVisitProgStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.RcTemplateType;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.BaseSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvAuthorization;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvDistribution;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvEntitlement;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.ModelStub;
import edu.uw.cse.ifrcdemo.sharedlib.util.CsvDiffGenerator;
import edu.uw.cse.ifrcdemo.sharedlib.util.CsvMapperUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.FileUtil;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.NoResultException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class EntitlementGenerationTest extends RcTemplateTestBase {

    private final Logger logger = LogManager.getLogger(EntitlementGenerationTest.class);

    public static final String UNITTESTS_PROFILE = "rcunittests";
    private static final String TEST_OUTPUT_PATH = "testoutput";

    public static Path getUnitTestProfilePath() throws IOException {
        return InternalFileStoreUtil.getProjectPath().resolve(InternalFileStoreUtil.PROFILES_PATH).resolve(UNITTESTS_PROFILE);
    }

    private static Path getUnitTestOutputPath() throws IOException {
        return getUnitTestProfilePath().resolve(TEST_OUTPUT_PATH);
    }

    @BeforeEach
    void verifyProperlySetup() {
        ReliefAppSystem.systemInit();
        File testOutputDir = null;
        try {
            testOutputDir = getUnitTestProfilePath().toFile();
            if (testOutputDir.exists()) {
                cleanOutputDirectory();
                fail("Previous test did not properly clean up test directory");
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail("problem with test file");
        }
    }

    @AfterEach
    void cleanOutputDirectory() {
        try {
            InternalFileStoreUtil.setNoProfile();
        } catch (Exception e) {
            e.printStackTrace();
            fail("problem with test file", e);
        }

        File testOutputDir = null;
        try {
            testOutputDir = getUnitTestProfilePath().toFile();

            if (testOutputDir.exists()) {
                FileUtils.deleteDirectory(testOutputDir);
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail("problem with test file");
        } finally {
            try {
                testOutputDir = getUnitTestProfilePath().toFile();
                if (testOutputDir != null) {
                    assertFalse(testOutputDir.exists());
                }
            } catch (Exception e) {
                // do nothing
            }
        }
    }

    @Test
    void basicImportFilesTest() {
        loadSetupFiles("testFiles/entitlement_test/startingdata");
    }

    @Test
    void useBlanketTemplateTest() {
        try {
            loadSetupFiles("testFiles/entitlement_test/startingdata");

            File blanketTemplate = getResourceFile("testFiles/entitlement_test/RC2-BlanketTemplate.zip");
            assertTrue(blanketTemplate.exists());

            RcTemplateUtil.importTemplate(blanketTemplate);
            ReliefDataRepos repos = ReliefDataInstance.getDataRepos();
            RcTemplateRepository templateRepository = repos.getRcTemplateRepository();
            List<RcTemplate> distTemplates = templateRepository
                    .getRcTemplatesByType(RcTemplateType.DISTRIBUTION)
                    .join();

            assertEquals(distTemplates.size(), 1);

            RcTemplate rcTemplate = distTemplates.get(0);


            RcDistributionTemplate disTemplate = new RcDistributionTemplate(rcTemplate.getJsonEncodingString());
            assertEquals(disTemplate.getTemplateName(), "BlanketTemplate");

            // use template
            Location location = createTestLocation();
            Distribution dist = disTemplate.clone().getDistribution();
            dist.setName("UW Blanket Distribution");
            dist.setLocation(location);
            dist.setStatus(DistVisitProgStatus.ACTIVE);

            DistributionRepository distRepository = repos.getDistributionRepository();

            for (Authorization auth : dist.getAuthorizations()) {
                verifyItemInDb(auth.getItem());
                auth.setStatus(AuthorizationStatus.ACTIVE);
            }

            dist = distRepository.saveDistribution(dist);

            DistExporter exporter = new DistExporter(logger);
            exporter.exportDistribution(dist);

            List<Authorization> auths = dist.getAuthorizations();
            assertEquals(auths.size(), 1);

            EntitlementRepository entitlementRepository = repos.getEntitlementRepository();

            List<Entitlement> entitlements = entitlementRepository.getEntitlements(auths.get(0)).get();
            assertEquals(entitlements.size(), 4);

            assertEntitlementsUnique(entitlements);

            List<CsvEntitlement> csvEntitlements = entitlements
                    .stream()
                    .map(EntitlementUtil::toCsvEntitlement)
                    .collect(Collectors.toList());

            List<CsvAuthorization> csvAuths = auths
                .stream()
                .map(AuthorizationUtil::toCsvAuthorization)
                .collect(Collectors.toList());

            Files.createDirectories(getUnitTestOutputPath());
            File inputDirectory = getResourceFile("testFiles/entitlement_test/startingdata");
            File outputDirectory = getUnitTestOutputPath().toFile();
            assertTrue(inputDirectory.exists() && inputDirectory.isDirectory());
            assertTrue(outputDirectory.exists() && outputDirectory.isDirectory());
            FileUtils.copyDirectory(inputDirectory,outputDirectory);

            writeRows(
                csvEntitlements,
                edu.uw.cse.ifrcdemo.sharedlib.model.stub.Entitlement.class,
                CsvEntitlement.class,
                repos.getCsvRepository()
            );

            writeRows(
                Collections.singleton(DistributionUtil.toCsvDistribution(dist)),
                edu.uw.cse.ifrcdemo.sharedlib.model.stub.Distribution.class,
                CsvDistribution.class,
                repos.getCsvRepository()
            );

            writeRows(
                csvAuths,
                edu.uw.cse.ifrcdemo.sharedlib.model.stub.Authorization.class,
                CsvAuthorization.class,
                repos.getCsvRepository()
            );

            loadSetupFilesAbsolute(outputDirectory);
            repos = ReliefDataInstance.getDataRepos();

            List<Entitlement> dbEntitlementsAfterRead = repos
                .getEntitlementRepository()
                .getEntitlements()
                .join();

            assertEquals(4, dbEntitlementsAfterRead.size());
            assertEntitlementsUnique(dbEntitlementsAfterRead);

            exporter = new DistExporter(logger);
            exporter.exportDistribution(dist);

            auths = dist.getAuthorizations();
            assertEquals(auths.size(), 1);

            entitlementRepository = repos.getEntitlementRepository();

            entitlements = entitlementRepository.getEntitlements(auths.get(0)).get();
            assertEquals(entitlements.size(), 4);
            assertEntitlementsUnique(entitlements);

        } catch (Exception e) {
            e.printStackTrace();
            fail(e);
        }
    }

    //////////////////////////////////////////////////////////
    //////////      PRIVATE HELPER FUNCTIONS        //////////
    //////////////////////////////////////////////////////////

    private void loadSetupFiles(String path) {
        File inputDirectory = getResourceFile(path);
        assertTrue(inputDirectory.exists());
        CompletableFuture<Void> importFuture = null;
        try {
            importFuture = InternalFileStoreUtil.importData(UNITTESTS_PROFILE, inputDirectory);
            importFuture.get();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception during import", e);
        }
    }

    private void loadSetupFilesAbsolute(File inputDirectory) {
        assertTrue(inputDirectory.exists());
        CompletableFuture<Void> importFuture = null;
        try {
            importFuture = InternalFileStoreUtil.importData(UNITTESTS_PROFILE, inputDirectory);
            importFuture.get();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception during import", e);
        }
    }

    private File getResourceFile(String resourcePath) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        return new File(classLoader.getResource(resourcePath).getPath());
    }

    private Location createTestLocation() {
        LocationRepository locationRepository = ReliefDataInstance.getDataRepos().getLocationRepository();

        Location newLocation = new Location();
        newLocation.setName("UW-CSE");
        newLocation.setDescription("Gates Center");
        locationRepository.saveLocation(newLocation).join();
        return newLocation;
    }

    private void verifyItemInDb(Item item) {
        ReliefDataRepos repos = ReliefDataInstance.getDataRepos();
        ItemRepository itemRepository = repos.getItemRepository();

        try {
            itemRepository.getItemByRowId(item.getRowId());
        } catch (NoResultException e) {
            fail(e);
        }
    }

    private <T extends BaseSyncRow> void writeRows(Collection<T> updatedRows,
                                                   Class<? extends ModelStub> stubClass,
                                                   Class<T> csvClass,
                                                   CsvRepository csvRepository) throws IOException {
        CsvMapper mapper = CsvMapperUtil.getCsvMapper(true);
        Path csvOutputPath = getUnitTestOutputPath().resolve(FileUtil.getFileName(csvClass));

        try (BufferedWriter writer = Files.newBufferedWriter(csvOutputPath);
             SequenceWriter sequenceWriter = CsvMapperUtil
                 .getWriterForSuitcaseSyncRow(stubClass, mapper, true)
                 .writeValues(writer)) {

            List<T> origCsv = csvRepository
                .readTypedCsv(csvClass)
                .orElseThrow(IllegalStateException::new);

            sequenceWriter.writeAll(CsvDiffGenerator.generate(origCsv, updatedRows));
        }

        assertTrue(Files.exists(csvOutputPath));
        assertTrue(Files.size(csvOutputPath) > 0);
    }

    private void assertEntitlementsUnique(Collection<Entitlement> entitlements) {
        long uniqueCount = entitlements
            .stream()
            .map(ent -> ent.getAuthorization().getRowId() + ent.getBeneficiaryEntityId() + ent.getIndividualId())
            .distinct()
            .count();

        assertEquals(entitlements.size(), uniqueCount);
    }
}
