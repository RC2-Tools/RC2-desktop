package edu.uw.cse.ifrcdemo.distplan.rctemplate;

import edu.uw.cse.ifrcdemo.distplan.data.DataInstance;
import edu.uw.cse.ifrcdemo.distplan.data.DataRepos;
import edu.uw.cse.ifrcdemo.distplan.entity.Authorization;
import edu.uw.cse.ifrcdemo.distplan.entity.Distribution;
import edu.uw.cse.ifrcdemo.distplan.entity.Entitlement;
import edu.uw.cse.ifrcdemo.distplan.entity.ItemPack;
import edu.uw.cse.ifrcdemo.distplan.entity.RcTemplate;
import edu.uw.cse.ifrcdemo.distplan.entity.Region;
import edu.uw.cse.ifrcdemo.distplan.entity.VisitProgram;
import edu.uw.cse.ifrcdemo.distplan.model.criteria.AuthorizationCriterion;
import edu.uw.cse.ifrcdemo.distplan.model.criteria.CriterionField;
import edu.uw.cse.ifrcdemo.distplan.model.criteria.CriterionOperator;
import edu.uw.cse.ifrcdemo.distplan.model.distribution.DistExporter;
import edu.uw.cse.ifrcdemo.distplan.model.distribution.DistributionRepository;
import edu.uw.cse.ifrcdemo.distplan.model.entitlement.EntitlementRepository;
import edu.uw.cse.ifrcdemo.distplan.model.form.XlsxForm;
import edu.uw.cse.ifrcdemo.distplan.model.itempack.ItemPackRepository;
import edu.uw.cse.ifrcdemo.distplan.model.rctemplate.RcDistributionTemplate;
import edu.uw.cse.ifrcdemo.distplan.model.rctemplate.RcTemplateRepository;
import edu.uw.cse.ifrcdemo.distplan.model.rctemplate.RcTemplateTestBase;
import edu.uw.cse.ifrcdemo.distplan.model.rctemplate.RcTemplateUtil;
import edu.uw.cse.ifrcdemo.distplan.model.rctemplate.RcVisitProgramTemplate;
import edu.uw.cse.ifrcdemo.distplan.model.region.RegionRepository;
import edu.uw.cse.ifrcdemo.distplan.util.InternalFileStoreUtil;
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
            DataRepos repos = DataInstance.getDataRepos();
            RcTemplateRepository templateRepository = repos.getRcTemplateRepository();
            List<RcTemplate> distTemplates = templateRepository
                    .getRcTemplatesByType(RcTemplateType.DISTRIBUTION)
                    .join();

            assertEquals(distTemplates.size(), 1);

            RcTemplate rcTemplate = distTemplates.get(0);


            RcDistributionTemplate disTemplate = new RcDistributionTemplate(rcTemplate.getJsonEncodingString());
            assertEquals(disTemplate.getTemplateName(), "Blanket");

            // use template
            Region location = createTestLocation();
            Distribution dist = disTemplate.clone().getDistribution();
            dist.setName("UW Blanket Distribution");
            dist.setLocation(location);
            dist.setStatus(DistVisitProgStatus.ACTIVE);

            DistributionRepository distRepository = repos.getDistributionRepository();

            for (Authorization auth : dist.getAuthorizations()) {
                verifyItemInDb(auth.getItemPack());
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

            DataRepos dataRepos = DataInstance.getDataRepos();
            RcTemplateRepository rcTemplateRepository = dataRepos.getRcTemplateRepository();

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

            DataRepos dataRepos = DataInstance.getDataRepos();
            RcTemplateRepository rcTemplateRepository = dataRepos.getRcTemplateRepository();

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

            DataRepos dataRepos = DataInstance.getDataRepos();
            RcTemplateRepository rcTemplateRepository = dataRepos.getRcTemplateRepository();

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

            DataRepos dataRepos = DataInstance.getDataRepos();
            RcTemplateRepository rcTemplateRepository = dataRepos.getRcTemplateRepository();

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

    private Region createTestLocation() {
        RegionRepository regionRepository = DataInstance.getDataRepos().getRegionRepository();

        Region newRegion = new Region();
        newRegion.setName("UW-CSE");
        newRegion.setDescription("Gates Center");
        regionRepository.saveRegion(newRegion).join();
        return newRegion;
    }

    private void verifyItemInDb(ItemPack itemPack) {
        DataRepos repos = DataInstance.getDataRepos();
        ItemPackRepository itemPackRepository = repos.getItemPackRepository();

        try {
            itemPackRepository.getItemPackByRowId(itemPack.getRowId());
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
        assertEquals(auth.isAssignItemPackCode(), false);
        assertEquals(auth.getCustomDeliveryForm(), new XlsxForm("ctp_delivery", "ctp_delivery"));
        assertEquals(auth.getForIndividual(), false);
        assertEquals(auth.getItemPackRanges(), EMPTY_LIST);
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
        ItemPack testItemPack = auth.getItemPack();
        verifyItemInDb(testItemPack);
        assertEquals(testItemPack.getRowId(), "1541ab2e-6c41-4843-8da5-1bab14e88ed4");
        assertEquals(testItemPack.getName(), "$200 Visa Card");
        assertEquals(testItemPack.getDescription(), "$200 prepaid Visa card");
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
        assertEquals(visitProgram.getRegion(), null);
        assertEquals(visitProgram.getBeneficiaryRanges(), EMPTY_LIST);
        assertEquals(visitProgram.isForMember(), true);
        assertEquals(visitProgram.getCustomVisitForm(), new XlsxForm("geotagger", "geotagger"));
        assertEquals(visitProgram.getRules(), EMPTY_LIST);
    }


}
