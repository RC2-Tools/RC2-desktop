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

package edu.uw.cse.ifrcdemo.distplan.rctemplate;

import edu.uw.cse.ifrcdemo.distplan.ReliefAppSystem;
import edu.uw.cse.ifrcdemo.distplan.data.ReliefDataInstance;
import edu.uw.cse.ifrcdemo.distplan.data.ReliefDataRepos;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Authorization;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Distribution;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Entitlement;
import edu.uw.cse.ifrcdemo.distplan.entity.VisitProgram;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.AuthorizationCriterion;
import edu.uw.cse.ifrcdemo.distplan.model.distribution.DistExporter;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.distribution.DistributionRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.entitlement.EntitlementRepository;
import edu.uw.cse.ifrcdemo.distplan.model.rctemplate.RcDistributionTemplate;
import edu.uw.cse.ifrcdemo.distplan.model.rctemplate.RcTemplateTestBase;
import edu.uw.cse.ifrcdemo.distplan.model.rctemplate.RcTemplateUtil;
import edu.uw.cse.ifrcdemo.distplan.model.rctemplate.RcVisitProgramTemplate;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Item;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Location;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.RcTemplate;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.CriterionField;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.CriterionOperator;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.form.XlsxForm;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.item.ItemRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.rctemplate.RcTemplateRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.location.LocationRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.InternalFileStoreUtil;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationType;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.DistVisitProgStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.ExtraFieldEntitlements;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.RcTemplateType;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wink.json4j.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.NoResultException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class RcTemplateTest extends RcTemplateTestBase {

    private final Logger logger = LogManager.getLogger(RcTemplateTest.class);

    private static final List<?> EMPTY_LIST = new ArrayList();

    public static final String UNITTESTS_PROFILE = "rcunittests";

    public static Path getUnitTestProfilePath() throws IOException {
        return InternalFileStoreUtil.getProjectPath().resolve(InternalFileStoreUtil.PROFILES_PATH).resolve(UNITTESTS_PROFILE);
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
        loadSetupFiles();
    }

    @Test
    void useBlanketTemplateTest() {
        try {
            loadSetupFiles();

            File blanketTemplate = getResourceFile("testFiles/templatetest/distnoforms/RC2-Blanket.zip");
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
            assertEquals(disTemplate.getTemplateName(), "Blanket");

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
            assertEquals(entitlements.size(), 3);

        } catch (Exception e) {
            e.printStackTrace();
            fail(e);
        }
    }

    @Test
    void importCashCardTest() {
        try {
            loadSetupFiles();

            File cashTemplate = getResourceFile("testFiles/templatetest/distwforms/RC2-CashCard.zip");
            assertTrue(cashTemplate.exists());

            RcTemplateUtil.importTemplate(cashTemplate);

            ReliefDataRepos reliefDataRepos = ReliefDataInstance.getDataRepos();
            RcTemplateRepository rcTemplateRepository = reliefDataRepos.getRcTemplateRepository();

            List<RcTemplate> distTemplates = rcTemplateRepository
                    .getRcTemplatesByType(RcTemplateType.DISTRIBUTION)
                    .join();

            assertEquals(distTemplates.size(), 1);

            List<RcTemplate> visitProgramTemplates = rcTemplateRepository
                    .getRcTemplatesByType(RcTemplateType.VISIT_PROGRAM)
                    .join();

            assertEquals(visitProgramTemplates.size(), 0);

            // verify correct values.
            RcTemplate tmpTemplate = distTemplates.get(0);
            RcDistributionTemplate verifyDistTemplate = RcDistributionTemplate.ConvertToRcDistributionTemplate(tmpTemplate);
            verifyCashCardTemplate(verifyDistTemplate);

        } catch (Exception e) {
            e.printStackTrace();
            fail(e);
        }
    }

    @Test
    void importNexportNimportCashCardTest() {
        try {
            loadSetupFiles();

            File cashTemplate = getResourceFile("testFiles/templatetest/distwforms/RC2-CashCard.zip");
            assertTrue(cashTemplate.exists());

            RcTemplateUtil.importTemplate(cashTemplate);

            ReliefDataRepos reliefDataRepos = ReliefDataInstance.getDataRepos();
            RcTemplateRepository rcTemplateRepository = reliefDataRepos.getRcTemplateRepository();

            List<RcTemplate> distTemplates = rcTemplateRepository
                    .getRcTemplatesByType(RcTemplateType.DISTRIBUTION)
                    .join();

            assertEquals(distTemplates.size(), 1);

            // verify correct values.
            RcTemplate tmpTemplate = distTemplates.get(0);
            verifyCashCardTemplate(RcDistributionTemplate.ConvertToRcDistributionTemplate(tmpTemplate));

            // create template
            RcTemplateUtil.generateTemplateZip(tmpTemplate, getUnitTestProfilePath());

            Path exportZipPath = getUnitTestProfilePath().resolve(RcTemplateUtil.createTemplateFilename(tmpTemplate));
            File reimportTemplate = exportZipPath.toFile();
            assertTrue(reimportTemplate.exists());

            RcTemplateUtil.importTemplate(reimportTemplate);

            List<RcTemplate> distTemplatesSecondQuery = rcTemplateRepository
                    .getRcTemplatesByType(RcTemplateType.DISTRIBUTION)
                    .join();

            assertEquals(distTemplatesSecondQuery.size(), 2);

            for(int i=0; i < distTemplatesSecondQuery.size() ; i++) {
                // verify correct values.
                RcTemplate testTemplate = distTemplatesSecondQuery.get(i);
                verifyCashCardTemplate(RcDistributionTemplate.ConvertToRcDistributionTemplate(testTemplate));
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail(e);
        }
    }


    @Test
    void importPetVisitTest() {
        try {
            loadSetupFiles();

            File petVisitTemplate = getResourceFile("testFiles/templatetest/visitwforms/RC2-FindPets.zip");
            assertTrue(petVisitTemplate.exists());

            RcTemplateUtil.importTemplate(petVisitTemplate);

            ReliefDataRepos reliefDataRepos = ReliefDataInstance.getDataRepos();
            RcTemplateRepository rcTemplateRepository = reliefDataRepos.getRcTemplateRepository();

            List<RcTemplate> visitProgramTemplates = rcTemplateRepository
                    .getRcTemplatesByType(RcTemplateType.VISIT_PROGRAM)
                    .join();

            assertEquals(visitProgramTemplates.size(), 1);

            List<RcTemplate> distTemplates = rcTemplateRepository
                    .getRcTemplatesByType(RcTemplateType.DISTRIBUTION)
                    .join();

            assertEquals(distTemplates.size(), 0);

            // verify correct values.
            RcTemplate tmpTemplate = visitProgramTemplates.get(0);
            RcVisitProgramTemplate verifyVisitTemplate = RcVisitProgramTemplate.ConvertToRcVisitProgramTemplate(tmpTemplate);

            verifyPetVisitValues(verifyVisitTemplate);

        } catch (Exception e) {
            e.printStackTrace();
            fail(e);
        }
    }


    @Test
    void importNexportNimportPetVisitTest() {
        try {
            loadSetupFiles();

            File petVisitTemplate = getResourceFile("testFiles/templatetest/visitwforms/RC2-FindPets.zip");
            assertTrue(petVisitTemplate.exists());

            RcTemplateUtil.importTemplate(petVisitTemplate);

            ReliefDataRepos reliefDataRepos = ReliefDataInstance.getDataRepos();
            RcTemplateRepository rcTemplateRepository = reliefDataRepos.getRcTemplateRepository();

            List<RcTemplate> visitProgramTemplates = rcTemplateRepository
                    .getRcTemplatesByType(RcTemplateType.VISIT_PROGRAM)
                    .join();

            assertEquals(visitProgramTemplates.size(), 1);

            List<RcTemplate> distTemplates = rcTemplateRepository
                    .getRcTemplatesByType(RcTemplateType.DISTRIBUTION)
                    .join();

            assertEquals(distTemplates.size(), 0);

            // verify correct values.
            RcTemplate tmpTemplate = visitProgramTemplates.get(0);
            RcVisitProgramTemplate verifyVisitTemplate = RcVisitProgramTemplate.ConvertToRcVisitProgramTemplate(tmpTemplate);
            verifyPetVisitValues(verifyVisitTemplate);

            // create template
            RcTemplateUtil.generateTemplateZip(tmpTemplate, getUnitTestProfilePath());

            Path exportZipPath = getUnitTestProfilePath().resolve(RcTemplateUtil.createTemplateFilename(tmpTemplate));
            File reimportTemplate = exportZipPath.toFile();
            assertTrue(reimportTemplate.exists());

            RcTemplateUtil.importTemplate(reimportTemplate);

            List<RcTemplate> visTemplatesSecondQuery = rcTemplateRepository
                    .getRcTemplatesByType(RcTemplateType.VISIT_PROGRAM)
                    .join();

            assertEquals(visTemplatesSecondQuery.size(), 2);

            for(int i=0; i < visTemplatesSecondQuery.size() ; i++) {
                // verify correct values.
                RcTemplate testTemplate = visTemplatesSecondQuery.get(i);
                verifyPetVisitValues(RcVisitProgramTemplate.ConvertToRcVisitProgramTemplate(testTemplate));
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail(e);
        }
    }

    //////////////////////////////////////////////////////////
    //////////      PRIVATE HELPER FUNCTIONS        //////////
    //////////////////////////////////////////////////////////

    private void loadSetupFiles() {
        File inputDirectory = getResourceFile( "testFiles/templatetest/setupfiles");
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

    private void verifyCashCardTemplate(RcDistributionTemplate verifyDistTemplate) throws IOException, JSONException {

        assertEquals(verifyDistTemplate.getTemplateName(), "CashCard");
        Distribution verifyDist = verifyDistTemplate.getDistribution();

        assertEquals(verifyDist.getId(), null);
        assertEquals(verifyDist.getRowId(), null);
        assertEquals(verifyDist.getName(), null);
        assertEquals(verifyDist.getLocation(), null);
        assertEquals(verifyDist.getDescription(), null);
        assertEquals(verifyDist.getSummaryForm(), null);
        assertEquals(verifyDist.getSummaryVersion(), null);
        assertEquals(verifyDist.getStatus(), null);
        assertEquals(verifyDist.getStatusReason(), null);
        // TODO: WRB do we really need to propagate the date of the original distribution?
        assertEquals(verifyDist.getDateCreated(), Instant.ofEpochSecond(1559609289));

        List<Authorization> authList = verifyDist.getAuthorizations();
        assertEquals(authList.size(), 1);

        Authorization auth = authList.get(0);
        assertEquals(auth.getId(), null);
        assertEquals(auth.getRowId(), null);
        assertEquals(auth.getType(), AuthorizationType.REQUIRED_REGISTRATION);
        assertEquals(auth.isAssignItemCode(), false);
        assertEquals(auth.getCustomDeliveryForm(), new XlsxForm("ctp_delivery", "ctp_delivery"));
        assertEquals(auth.getForIndividual(), false);
        assertEquals(auth.getItemRanges(), EMPTY_LIST);
        assertEquals(auth.getBeneficiaryRanges(), EMPTY_LIST);
        assertEquals(auth.getVoucherRanges(), null); // TODO: WRB seems inconsistent
        assertEquals(auth.getExtraFieldEntitlements(), ExtraFieldEntitlements.NONE);
        assertEquals(auth.getStatus(), null);
        assertEquals(auth.getStatusReason(), null);
        // TODO: WRB do we really need to propagate the date of the original authorization?
        assertEquals(auth.getDateCreated(), Instant.ofEpochSecond(1559609289));

        List<List<AuthorizationCriterion>> ORRules = new ArrayList();
        List<AuthorizationCriterion> ANDRules = new ArrayList();
        CriterionField field = new CriterionField("ex_ind_mode_registration_demo", "gender");
        AuthorizationCriterion criterion = new AuthorizationCriterion(field, CriterionOperator.EQ, "female");
        ANDRules.add(criterion);
        ORRules.add(ANDRules);
        ORRules.add(new ArrayList()); // TODO: WRB check to see if this an error somewhere, why empty list?
        assertEquals(auth.getRules(), ORRules);

        // check item pack
        Item testItem = auth.getItem();
        verifyItemInDb(testItem);
        assertEquals(testItem.getRowId(), "1541ab2e-6c41-4843-8da5-1bab14e88ed4");
        assertEquals(testItem.getName(), "$200 Visa Card");
        assertEquals(testItem.getDescription(), "$200 prepaid Visa card");
    }

    private void verifyPetVisitValues(RcVisitProgramTemplate verifyVisitTemplate) {
        assertEquals(verifyVisitTemplate.getTemplateName(), "FindPets");
        VisitProgram visitProgram = verifyVisitTemplate.getVisitProgram();
        assertEquals(visitProgram.getId(), null);
        assertEquals(visitProgram.getRowId(), null);
        // TODO: WRB do we really need to propagate the date of the original visit?
        assertEquals(visitProgram.getDateCreated(), Instant.ofEpochSecond(1565290531));
        assertEquals(visitProgram.getName(), null);
        assertEquals(visitProgram.getDescription(), null);
        assertEquals(visitProgram.getLocation(), null);
        assertEquals(visitProgram.getBeneficiaryRanges(), EMPTY_LIST);
        assertEquals(visitProgram.isForMember(), true);
        assertEquals(visitProgram.getCustomVisitForm(), new XlsxForm("geotagger", "geotagger"));
        assertEquals(visitProgram.getRules(), EMPTY_LIST);
    }


}
