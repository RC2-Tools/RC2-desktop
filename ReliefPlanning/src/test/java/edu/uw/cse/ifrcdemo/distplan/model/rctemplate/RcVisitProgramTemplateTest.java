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

import edu.uw.cse.ifrcdemo.distplan.entity.VisitProgram;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.AuthorizationCriterion;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Location;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.range.Range;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.CriterionField;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.CriterionOperator;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.form.XlsxForm;
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

public class RcVisitProgramTemplateTest extends RcTemplateTestBase {


    private static final String TEST_ROW_ID = "testVisitRowId";
    private static final String TEST_VISIT_PROGRAM_FORM_TABLE_ID = "testVisitProgramFormId_tableId";
    private static final String TEST_VISIT_PROGRAM_FORM_FORM_ID = "testVisitProgramFormId_formId";

    private static final String TEST_CRITERION_VALUE_1 = "145";
    private static final String TEST_CRITERION_FIELD_KEY_1 = "testKey4";
    private static final String TEST_CRITERION_DISPLAY_NAME_KEY_1 = "test1DisplayNameKey4";

    private static final String TEST_CRITERION_VALUE_2 = "12344";
    private static final String TEST_CRITERION_FIELD_KEY_2 = "testKeyDance4";
    private static final String TEST_CRITERION_DISPLAY_NAME_KEY_2 = "test1DisplayNameKeyDance4";

    private static final String TEST_CRITERION_VALUE_3 = "114";
    private static final String TEST_CRITERION_FIELD_KEY_3 = "testKeyJourney4";
    private static final String TEST_CRITERION_DISPLAY_NAME_KEY_3 = "testDisplayNameKeyJOurney4";

    private static final String TEST_CRITERION_VALUE_4 = "1Songs To Live For4";
    private static final String TEST_CRITERION_FIELD_KEY_4 = "testKeySong4";
    private static final String TEST_CRITERION_DISPLAY_NAME_KEY_4 = "testDisplayNameKeySong4";

    private static final String TEST_CRITERION_VALUE_5 = "1Mistake4";
    private static final String TEST_CRITERION_FIELD_KEY_5 = "testKeyNOtreally4";
    private static final String TEST_CRITERION_DISPLAY_NAME_KEY_5 = "testDisplayNameKeyBoots4";

    private static final String TEST_VISIT_DIR = "testVisitTemplateDir";
    private static final String VISIT_TEMPLATE = "visitTemplate";
    private static final String TEST_TEMPLATE = "testTemplate";
    private static final String VISIT_DESCRIPTION = "VisitDscpt";
    private static final String VISIT_NAME_IS_AWESOME = "VisitNameIsAwesome";

    public RcVisitProgramTemplateTest() {
        super();
    }

    @Test
    void writeVisitTemplate() {
        testOutputDir.mkdir();
        File testVisitProgramDir = new File(testOutputDir, TEST_VISIT_DIR);

        VisitProgram testVisitProgram = createTestVisitProgram1();
        RcVisitProgramTemplate testVisitProgramTemplate = new RcVisitProgramTemplate(VISIT_TEMPLATE, testVisitProgram);

        try {
            testVisitProgramTemplate.writeEncodingDirectory(testVisitProgramDir);
            File testJSONFile = new File(testVisitProgramDir, RcVisitProgramTemplate.FILENAME);
            RcVisitProgramTemplate visitTemplate = new RcVisitProgramTemplate(testJSONFile);
            verifyVisitProgramSameAsOriginalWithNulls(testVisitProgram, visitTemplate.getVisitProgram());
        } catch (Exception e) {
            e.printStackTrace();
            fail("exception: " + e.getMessage());
        }

    }

    @Test
    void encodeVisitProgram() {
        VisitProgram testVisitProgram = new VisitProgram();
        testVisitProgram.setRowId(TEST_ROW_ID);
        XlsxForm customVisitForm = new XlsxForm(TEST_VISIT_PROGRAM_FORM_TABLE_ID, TEST_VISIT_PROGRAM_FORM_FORM_ID);
        testVisitProgram.setCustomVisitForm(customVisitForm);

        Instant testTime = Instant.now();
        testVisitProgram.setDateCreated(testTime);

        testVisitProgram.setForMember(true);

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

        testVisitProgram.setRules(rules);

        try {
            JSONObject jsonVisitProgram = RcVisitProgramTemplate.createJSONFromVisitProgram(testVisitProgram);
            VisitProgram visitProgram = RcVisitProgramTemplate.createVisitProgramFromJSON(jsonVisitProgram);
            verifyVisitProgramEqual(testVisitProgram, visitProgram);

            assertEquals(visitProgram.getRowId(), TEST_ROW_ID);
            assertEquals(visitProgram.getCustomVisitForm(), customVisitForm);

            Long visitProgramTime = Long.valueOf(visitProgram.getDateCreated().getEpochSecond());
            Long verifyTime = Long.valueOf(testTime.getEpochSecond());
            assertEquals(visitProgramTime, verifyTime);

            assertEquals(visitProgram.isForMember(), true);
            assertEquals(visitProgram.getRules(), rules);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    @Test
    void encodeVisitProgramWNulls() {
        VisitProgram testVisitProgram = new VisitProgram();

        try {
            JSONObject jsonVisitProgram = RcVisitProgramTemplate.createJSONFromVisitProgram(testVisitProgram);
            VisitProgram visitProgram = RcVisitProgramTemplate.createVisitProgramFromJSON(jsonVisitProgram);
            verifyVisitProgramEqual(testVisitProgram, visitProgram);

            assertEquals(visitProgram.getRowId(), null);
            assertEquals(visitProgram.getCustomVisitForm(), null);
            assertEquals(visitProgram.getDateCreated(), null);
            assertEquals(visitProgram.getRules(), null);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    void testClone() {
        VisitProgram testVisitProgram = createTestVisitProgram1();
        try{

            RcVisitProgramTemplate template = new RcVisitProgramTemplate(TEST_TEMPLATE, testVisitProgram);
            RcVisitProgramTemplate testTemplate = (RcVisitProgramTemplate) template.clone();

            assertEquals(template.getTemplateName(), testTemplate.getTemplateName());
            verifyVisitProgramEqual(template.getVisitProgram(), testTemplate.getVisitProgram());

            VisitProgram visitProgram1 = template.getVisitProgram();
            VisitProgram visitProgram2 = testTemplate.getVisitProgram();

           assertTrue(visitProgram1 != visitProgram2);
            assertNull(visitProgram1.getId());
            assertNull(visitProgram2.getId());
            assertNull(visitProgram1.getRowId());
            assertNull(visitProgram2.getRowId());
            assertNull(visitProgram1.getLocation());
            assertNull(visitProgram2.getLocation());

            assertEquals(visitProgram1.getName(), visitProgram2.getName());
            assertEquals(visitProgram1.getDescription(), visitProgram2.getDescription());
            assertEquals(visitProgram1.getCustomVisitForm(), visitProgram2.getCustomVisitForm());
            assertEquals(visitProgram1.isForMember(), visitProgram2.isForMember());

            assertNotNull(visitProgram1.getBeneficiaryRanges());
            assertNotNull(visitProgram2.getBeneficiaryRanges());
            verifyRangeObjNotEqualButValuesEqual(visitProgram1.getBeneficiaryRanges(), visitProgram2.getBeneficiaryRanges());

            // check authorization criteria
            verifyCriterionObjNotEqualButValuesEqual(visitProgram1.getRules(), visitProgram2.getRules());

        }catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private VisitProgram createTestVisitProgram1() {
        VisitProgram testVisitProgram = new VisitProgram();
        XlsxForm customVisitForm = new XlsxForm(TEST_VISIT_PROGRAM_FORM_TABLE_ID, TEST_VISIT_PROGRAM_FORM_FORM_ID);
        testVisitProgram.setCustomVisitForm(customVisitForm);
        testVisitProgram.setDateCreated(Instant.now());
        testVisitProgram.setForMember(true);
        testVisitProgram.setName(VISIT_NAME_IS_AWESOME);
        testVisitProgram.setLocation(new Location());

        List<Range> beneficiaryRanges = new ArrayList<>();
        beneficiaryRanges.add(new Range(1, 12100));
        beneficiaryRanges.add(new Range(14157, 111515));
        beneficiaryRanges.add(new Range(11324103, 64644030));
        testVisitProgram.setBeneficiaryRanges(beneficiaryRanges);

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

        testVisitProgram.setRules(rules);

        return testVisitProgram;
    }

}
