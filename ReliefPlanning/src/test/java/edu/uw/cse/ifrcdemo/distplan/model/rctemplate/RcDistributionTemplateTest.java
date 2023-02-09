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

package edu.uw.cse.ifrcdemo.distplan.model.rctemplate;

import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Authorization;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Distribution;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.AuthorizationCriterion;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Item;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.range.Range;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.CriterionField;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.CriterionOperator;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.form.XlsxForm;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationType;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.ExtraFieldEntitlements;
import org.apache.wink.json4j.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class RcDistributionTemplateTest extends RcTemplateTestBase {

    private static final String TEST_ROW_ID = "testRowId";
    private static final String TEST_ROW_DIST_ID = "testRowDistId";
    private static final String TEST_NAME = "testName";
    private static final String TEST_DESCRIPTION = "testDescription";

    private static final String TEST_CRITERION_VALUE_1 = "15";
    private static final String TEST_CRITERION_FIELD_KEY_1 = "testKey";
    private static final String TEST_CRITERION_DISPLAY_NAME_KEY_1 = "testDisplayNameKey";

    private static final String TEST_CRITERION_VALUE_2 = "1234";
    private static final String TEST_CRITERION_FIELD_KEY_2 = "testKeyDance";
    private static final String TEST_CRITERION_DISPLAY_NAME_KEY_2 = "testDisplayNameKeyDance";

    private static final String TEST_CRITERION_VALUE_3 = "1";
    private static final String TEST_CRITERION_FIELD_KEY_3 = "testKeyJourney";
    private static final String TEST_CRITERION_DISPLAY_NAME_KEY_3 = "testDisplayNameKeyJOurney";

    private static final String TEST_CRITERION_VALUE_4 = "Songs To Live For";
    private static final String TEST_CRITERION_FIELD_KEY_4 = "testKeySong";
    private static final String TEST_CRITERION_DISPLAY_NAME_KEY_4 = "testDisplayNameKeySong";

    private static final String TEST_CRITERION_VALUE_5 = "Mistake";
    private static final String TEST_CRITERION_FIELD_KEY_5 = "testKeyNOtreally";
    private static final String TEST_CRITERION_DISPLAY_NAME_KEY_5 = "testDisplayNameKeyBoots";
    private static final String TEST_FORM_X_TABLE_ID = "form_X_tableId";
    private static final String TEST_FORM_X_FORM_ID = "form_X_formId";
    private static final String TEST_SUMMARY_FORM_Y_TABLE_ID = "testSummaryFormY_tableId";
    private static final String TEST_SUMMARY_FORM_Y_FORM_ID = "testSummaryFormY_formId";
    private static final String TEST_SUMMARY_FORM_Z_TABLE_ID = "testSummaryFormZZZZZ_tableId";
    private static final String TEST_SUMMARY_FORM_Z_FORM_ID = "testSummaryFormZZZZZ_formId";

    private static final String TEST_TEMPLATE_NAME = "testTemplateName";
    private static final String TEST_DISTRIBUTION_DIR = "testDistTemplateDir";
    private static final String TEST_TEMPLATE = "TestTemplate";

    public RcDistributionTemplateTest() {
        super();
    }

    @Test
    void writeDistributionTemplate() {
        testOutputDir.mkdir();
        File testDistributionDir = new File(testOutputDir, TEST_DISTRIBUTION_DIR);

        Distribution dist = createTestDistribution1();
        RcDistributionTemplate testDistTemplate = new RcDistributionTemplate(TEST_TEMPLATE_NAME, dist);
        try {
            testDistTemplate.writeEncodingDirectory(testDistributionDir);
            File testJSONFile = new File(testDistributionDir, RcDistributionTemplate.FILENAME);
            RcDistributionTemplate distTemplate = new RcDistributionTemplate(testJSONFile);
            verifyDistributionEqual(dist, distTemplate.getDistribution());
        } catch (Exception e) {
            e.printStackTrace();
            fail("exception: " + e.getMessage());
        }
    }


    @Test
    void encodeDistribution() {
        Distribution testDistribution = new Distribution();
        testDistribution.setRowId(TEST_ROW_DIST_ID);

        List<Authorization> testAuthList = new ArrayList<>();
        Authorization testAuth = createTestAuthorization1();
        testAuthList.add(testAuth);
        testDistribution.setAuthorizations(testAuthList);

        Instant testTime = Instant.now();
        testDistribution.setDateCreated(testTime);

        XlsxForm summaryForm = new XlsxForm(TEST_SUMMARY_FORM_Y_TABLE_ID, TEST_SUMMARY_FORM_Y_FORM_ID);
        testDistribution.setSummaryForm(summaryForm);

        try {
            JSONObject jsonDistribution = RcDistributionTemplate.createJSONfromDistribution(testDistribution);
            Distribution dist = RcDistributionTemplate.createDistributionFromJSON(jsonDistribution);
            verifyDistributionEqual(testDistribution, dist);

            assertEquals(dist.getRowId(), TEST_ROW_DIST_ID);
            Long authTime = Long.valueOf(dist.getDateCreated().getEpochSecond());
            Long verifyTime = Long.valueOf(testTime.getEpochSecond());
            assertEquals(authTime, verifyTime);

            List<Authorization> authList = dist.getAuthorizations();
            assertEquals(1, authList.size());
            Authorization auth = authList.get(0);
            verifyAuthorizationEqual(auth, testAuth);

            assertEquals(summaryForm, dist.getSummaryForm());

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    void encodeDistributionWNulls() {
        Distribution testDistribution = new Distribution();

        try {
            JSONObject jsonDistribution = RcDistributionTemplate.createJSONfromDistribution(testDistribution);
            Distribution dist = RcDistributionTemplate.createDistributionFromJSON(jsonDistribution);
            verifyDistributionEqual(testDistribution, dist);

            assertEquals(dist.getRowId(), null);
            assertEquals(dist.getDateCreated(), null);
            assertEquals(dist.getAuthorizations(), null);
            assertEquals(dist.getSummaryForm(), null);

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    void encodeItem() {
        Item testItem = new Item();
        testItem.setRowId(TEST_ROW_ID);
        testItem.setName(TEST_NAME);
        testItem.setDescription(TEST_DESCRIPTION);

        try {
            JSONObject jsonItem = RcDistributionTemplate.createJSONFromItem(testItem);
            Item item = RcDistributionTemplate.createItemFromJSON(jsonItem);
            verifyItemEqual(testItem, item);

            assertEquals(item.getRowId(), TEST_ROW_ID);
            assertEquals(item.getName(), TEST_NAME);
            assertEquals(item.getDescription(), TEST_DESCRIPTION);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    @Test
    void encodeItemWNulls() {
        Item testItem = new Item();
        testItem.setRowId(null);
        testItem.setName(null);
        testItem.setDescription(null);

        try {
            JSONObject jsonItem = RcDistributionTemplate.createJSONFromItem(testItem);
            Item item = RcDistributionTemplate.createItemFromJSON(jsonItem);
            verifyItemEqual(testItem, item);

            assertEquals(item.getRowId(), null);
            assertEquals(item.getName(), null);
            assertEquals(item.getDescription(), null);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    void encodeItemWOneNulls() {
        Item testItem = new Item();
        testItem.setRowId(TEST_ROW_ID);
        testItem.setName(null);
        testItem.setDescription(TEST_DESCRIPTION);

        try {
            JSONObject jsonItem = RcDistributionTemplate.createJSONFromItem(testItem);
            Item item = RcDistributionTemplate.createItemFromJSON(jsonItem);
            verifyItemEqual(testItem, item);

            assertEquals(item.getRowId(), TEST_ROW_ID);
            assertEquals(item.getName(), null);
            assertEquals(item.getDescription(), TEST_DESCRIPTION);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    void encodeAuthorization() {
        Authorization testAuth = new Authorization();
        testAuth.setRowId(TEST_ROW_ID);

        Instant testTime = Instant.now();
        testAuth.setDateCreated(testTime);

        testAuth.setType(AuthorizationType.REQUIRED_REGISTRATION);

        testAuth.setAssignItemCode(true);

        testAuth.setForIndividual(true);

        Item testItem = new Item();
        testItem.setRowId(TEST_ROW_ID);
        testItem.setName(TEST_NAME);
        testItem.setDescription(TEST_DESCRIPTION);
        testAuth.setItem(testItem);

        List<Range> itemRanges = new ArrayList<>();
        itemRanges.add(new Range(0, 100));
        itemRanges.add(new Range(101, 1001));
        itemRanges.add(new Range(1110, 4000));
        testAuth.setItemRanges(itemRanges);

        testAuth.setExtraFieldEntitlements(ExtraFieldEntitlements.MANY);

        List<Range> voucherRanges = new ArrayList<>();
        voucherRanges.add(new Range(5, 5100));
        voucherRanges.add(new Range(14101, 121001));
        voucherRanges.add(new Range(113242110, 64644000));
        testAuth.setVoucherRanges(voucherRanges);

        List<List<AuthorizationCriterion>> rules = new ArrayList<>();
        List<AuthorizationCriterion> listCriteria = new ArrayList<>();

        CriterionField field = new CriterionField(TEST_CRITERION_FIELD_KEY_1, TEST_CRITERION_DISPLAY_NAME_KEY_1);
        AuthorizationCriterion criteria = new AuthorizationCriterion(field, CriterionOperator.EQ, TEST_CRITERION_VALUE_1);
        listCriteria.add(criteria);

        field = new CriterionField(TEST_CRITERION_FIELD_KEY_2, TEST_CRITERION_DISPLAY_NAME_KEY_2);
        criteria = new AuthorizationCriterion(field, CriterionOperator.LT, TEST_CRITERION_VALUE_2);
        listCriteria.add(criteria);

        field = new CriterionField(TEST_CRITERION_FIELD_KEY_3, TEST_CRITERION_DISPLAY_NAME_KEY_3);
        criteria = new AuthorizationCriterion(field, CriterionOperator.GT, TEST_CRITERION_VALUE_3);
        listCriteria.add(criteria);

        rules.add(listCriteria);
        listCriteria = new ArrayList<>();

        field = new CriterionField(TEST_CRITERION_FIELD_KEY_4, TEST_CRITERION_DISPLAY_NAME_KEY_4);
        criteria = new AuthorizationCriterion(field, CriterionOperator.LT, TEST_CRITERION_VALUE_4);
        listCriteria.add(criteria);

        field = new CriterionField(TEST_CRITERION_FIELD_KEY_5, TEST_CRITERION_DISPLAY_NAME_KEY_5);
        criteria = new AuthorizationCriterion(field, CriterionOperator.GT, TEST_CRITERION_VALUE_5);
        listCriteria.add(criteria);

        rules.add(listCriteria);

        testAuth.setRules(rules);
        XlsxForm customDeliveryForm = new XlsxForm(TEST_FORM_X_TABLE_ID, TEST_FORM_X_FORM_ID);
        testAuth.setCustomDeliveryForm(customDeliveryForm);

        try {
            JSONObject jsonAuth = RcDistributionTemplate.createJSONfromAuthorization(testAuth);
            Authorization auth = RcDistributionTemplate.createAuthorizationFromJSON(jsonAuth);
            verifyAuthorizationEqual(testAuth, auth);

            assertEquals(auth.getRowId(), TEST_ROW_ID);
            Long authTime = Long.valueOf(auth.getDateCreated().getEpochSecond());
            Long verifyTime = Long.valueOf(testTime.getEpochSecond());
            assertEquals(authTime, verifyTime);
            assertEquals(auth.getType(), AuthorizationType.REQUIRED_REGISTRATION);
            assertEquals(auth.isAssignItemCode(), true);
            assertEquals(auth.getForIndividual(), true);
            verifyItemEqual(auth.getItem(), testItem);
            assertEquals(auth.getItemRanges(), itemRanges);
            assertEquals(auth.getExtraFieldEntitlements(), ExtraFieldEntitlements.MANY);
            assertEquals(auth.getVoucherRanges(), voucherRanges);
            assertEquals(auth.getRules(), rules);
            assertEquals(auth.getCustomDeliveryForm(), customDeliveryForm);
            assertEquals(auth.getStatus(), null);
            assertEquals(auth.getStatusReason(), null);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    @Test
    void encodeAuthorizationWNull() {
        Authorization testAuth = new Authorization();

        try {
            JSONObject jsonAuth = RcDistributionTemplate.createJSONfromAuthorization(testAuth);
            Authorization auth = RcDistributionTemplate.createAuthorizationFromJSON(jsonAuth);
            verifyAuthorizationEqual(testAuth, auth);

            assertEquals(auth.getRowId(), null);
            assertEquals(auth.getDateCreated(), null);
            assertEquals(auth.getType(), null);
            assertEquals(auth.isAssignItemCode(), false);
            assertEquals(auth.getForIndividual(), false);
            assertEquals(auth.getItem(), null);
            assertEquals(auth.getItemRanges(), null);
            assertEquals(auth.getVoucherRanges(), null);
            assertEquals(auth.getRules(), null);
            assertEquals(auth.getCustomDeliveryForm(), null);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    @Test
    void testClone() {
        Distribution testDistribution = new Distribution();
        testDistribution.setRowId(TEST_ROW_DIST_ID);

        List<Authorization> testAuthList = new ArrayList<>();
        Authorization testAuth = createTestAuthorization1();
        testAuthList.add(testAuth);
        testDistribution.setAuthorizations(testAuthList);

        Instant testTime = Instant.now();
        testDistribution.setDateCreated(testTime);

        XlsxForm summaryForm = new XlsxForm(TEST_SUMMARY_FORM_Y_TABLE_ID, TEST_SUMMARY_FORM_Y_FORM_ID);
        testDistribution.setSummaryForm(summaryForm);

        try {
            RcDistributionTemplate template = new RcDistributionTemplate(TEST_TEMPLATE, testDistribution);
            RcDistributionTemplate testTemplate = (RcDistributionTemplate) template.clone();

            assertEquals(template.getTemplateName(), testTemplate.getTemplateName());
            verifyDistributionEqual(template.getDistribution(), testTemplate.getDistribution());

            Distribution testDistribution1 = template.getDistribution();
            Distribution testDistribution2 = testTemplate.getDistribution();

            assertTrue(testDistribution1 != testDistribution2);
            assertNull(testDistribution1.getId());
            assertNull(testDistribution2.getId());
            assertNull(testDistribution1.getRowId());
            assertNull(testDistribution2.getRowId());
            assertEquals(testDistribution1.getDescription(), testDistribution2.getDescription());
            assertNull(testDistribution1.getLocation());
            assertNull(testDistribution2.getLocation());
            assertEquals(testDistribution1.getName(), testDistribution2.getName());
            assertNull(testDistribution1.getStatus());
            assertNull(testDistribution2.getStatus());
            assertNull(testDistribution1.getStatusReason());
            assertNull(testDistribution2.getStatusReason());
            assertEquals(testDistribution1.getSummaryForm(), testDistribution2.getSummaryForm());
            assertNull(testDistribution1.getSummaryVersion());
            assertNull(testDistribution2.getSummaryVersion());

            List<Authorization> authList1 = testDistribution1.getAuthorizations();
            List<Authorization> authList2 = testDistribution2.getAuthorizations();
            assertTrue(authList1 != authList2);
            assertEquals(authList1.size(), 1);
            assertEquals(authList2.size(), 1);

            Authorization auth1 = authList1.get(0);
            Authorization auth2 = authList2.get(0);
            assertTrue(auth1 != auth2);
            verifyAuthorizationEqual(auth1, auth2);
            assertNull(auth1.getId());
            assertNull(auth2.getId());
            assertNull(auth1.getRowId());
            assertNull(auth2.getRowId());
            assertEquals(auth1.getType(), auth2.getType());
            assertEquals(auth1.isAssignItemCode(), auth2.isAssignItemCode());
            assertEquals(auth1.getForIndividual(), auth2.getForIndividual());
            assertEquals(auth1.getExtraFieldEntitlements(), auth2.getExtraFieldEntitlements());
            assertNull(auth1.getStatus());
            assertNull(auth2.getStatus());
            assertNull(auth1.getStatusReason());
            assertNull(auth2.getStatusReason());

            // check item pack & ranges
            Item item1 = auth1.getItem();
            Item item2 = auth2.getItem();
            assertTrue(item1 != item2);
            verifyItemEqual(item1, item2);
            assertNull(item1.getId());
            assertNull(item2.getId());
            assertEquals(item1.getRowId(), item2.getRowId());
            assertEquals(item1.getName(), item2.getName());
            assertEquals(item1.getDescription(), item2.getDescription());
            assertNotNull(auth1.getItemRanges());
            assertNotNull(auth2.getItemRanges());
            verifyRangeObjNotEqualButValuesEqual(auth1.getItemRanges(), auth2.getItemRanges());

            // check voucher & beneficiary ranges
            assertNotNull(auth1.getVoucherRanges());
            assertNotNull(auth2.getVoucherRanges());
            verifyRangeObjNotEqualButValuesEqual(auth1.getVoucherRanges(), auth2.getVoucherRanges());
            assertNotNull(auth1.getBeneficiaryRanges());
            assertNotNull(auth2.getBeneficiaryRanges());
            verifyRangeObjNotEqualButValuesEqual(auth1.getBeneficiaryRanges(), auth2.getBeneficiaryRanges());

            // check authorization criteria
            verifyCriterionObjNotEqualButValuesEqual(auth1.getRules(), auth2.getRules());

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }


    private Authorization createTestAuthorization1() {
        Authorization testAuth = new Authorization();

        Instant testTime = Instant.now();
        testAuth.setDateCreated(testTime);

        testAuth.setType(AuthorizationType.REQUIRED_REGISTRATION);

        testAuth.setAssignItemCode(true);

        testAuth.setForIndividual(true);

        Item testItem = new Item();
        testItem.setRowId(TEST_ROW_ID);
        testItem.setName(TEST_NAME);
        testItem.setDescription(TEST_DESCRIPTION);
        testAuth.setItem(testItem);

        List<Range> itemRanges = new ArrayList<>();
        itemRanges.add(new Range(0, 100));
        itemRanges.add(new Range(101, 1001));
        itemRanges.add(new Range(1110, 4000));
        testAuth.setItemRanges(itemRanges);


        List<Range> voucherRanges = new ArrayList<>();
        voucherRanges.add(new Range(5, 5100));
        voucherRanges.add(new Range(14101, 121001));
        voucherRanges.add(new Range(113242110, 64644000));
        testAuth.setVoucherRanges(voucherRanges);

        List<Range> beneficiaryRanges = new ArrayList<>();
        beneficiaryRanges.add(new Range(1, 12100));
        beneficiaryRanges.add(new Range(14157, 111515));
        beneficiaryRanges.add(new Range(11324103, 64644030));
        testAuth.setBeneficiaryRanges(beneficiaryRanges);

        List<List<AuthorizationCriterion>> rules = new ArrayList<>();
        List<AuthorizationCriterion> listCriteria = new ArrayList<>();

        CriterionField field = new CriterionField(TEST_CRITERION_FIELD_KEY_1, TEST_CRITERION_DISPLAY_NAME_KEY_1);
        AuthorizationCriterion criteria = new AuthorizationCriterion(field, CriterionOperator.EQ, TEST_CRITERION_VALUE_1);
        listCriteria.add(criteria);

        field = new CriterionField(TEST_CRITERION_FIELD_KEY_2, TEST_CRITERION_DISPLAY_NAME_KEY_2);
        criteria = new AuthorizationCriterion(field, CriterionOperator.LT, TEST_CRITERION_VALUE_2);
        listCriteria.add(criteria);

        field = new CriterionField(TEST_CRITERION_FIELD_KEY_3, TEST_CRITERION_DISPLAY_NAME_KEY_3);
        criteria = new AuthorizationCriterion(field, CriterionOperator.GT, TEST_CRITERION_VALUE_3);
        listCriteria.add(criteria);

        rules.add(listCriteria);
        listCriteria = new ArrayList<>();

        field = new CriterionField(TEST_CRITERION_FIELD_KEY_4, TEST_CRITERION_DISPLAY_NAME_KEY_4);
        criteria = new AuthorizationCriterion(field, CriterionOperator.LT, TEST_CRITERION_VALUE_4);
        listCriteria.add(criteria);

        field = new CriterionField(TEST_CRITERION_FIELD_KEY_5, TEST_CRITERION_DISPLAY_NAME_KEY_5);
        criteria = new AuthorizationCriterion(field, CriterionOperator.GT, TEST_CRITERION_VALUE_5);
        listCriteria.add(criteria);

        rules.add(listCriteria);

        testAuth.setRules(rules);
        testAuth.setCustomDeliveryForm(new XlsxForm(TEST_FORM_X_TABLE_ID, TEST_FORM_X_FORM_ID));

        return testAuth;
    }

    private Distribution createTestDistribution1() {
        Distribution testDistribution = new Distribution();;

        List<Authorization> testAuthList = new ArrayList<>();
        Authorization testAuth = createTestAuthorization1();
        testAuthList.add(testAuth);
        testDistribution.setAuthorizations(testAuthList);

        Instant testTime = Instant.now();
        testDistribution.setDateCreated(testTime);

        testDistribution.setSummaryForm(new XlsxForm(TEST_SUMMARY_FORM_Z_TABLE_ID, TEST_SUMMARY_FORM_Z_FORM_ID));
        return testDistribution;
    }

}
