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

import edu.uw.cse.ifrcdemo.distplan.ReliefAppSystem;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Authorization;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Distribution;
import edu.uw.cse.ifrcdemo.distplan.entity.VisitProgram;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.AuthorizationCriterion;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Item;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.range.Range;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.CriterionField;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.CriterionOperator;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class RcTemplateTestBase  {

    protected static final String TEST_RC_TEMPLATE_OUTPUT_DIR = "TestRcTemplate";

    protected final File testOutputDir;

    public RcTemplateTestBase() {
        ReliefAppSystem.systemInit();
        String baseDir = System.getProperty("basedir");
        testOutputDir = new File(baseDir, TEST_RC_TEMPLATE_OUTPUT_DIR);
    }

    @BeforeEach
    void verifyProperlySetup() {
        if (testOutputDir.exists()) {
            cleanOutputDirectory();
            fail("Previous test did not properly clean up test directory");
        }
    }

    @AfterEach
    void cleanOutputDirectory() {
        try {
            if (testOutputDir.exists())
                FileUtils.deleteDirectory(testOutputDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertFalse(testOutputDir.exists());
    }

    protected boolean criterionIsEqual(AuthorizationCriterion criterion1, AuthorizationCriterion criterion2) {
        CriterionField field1 = criterion1.getField();
        CriterionField field2 = criterion2.getField();
        CriterionOperator op1 = criterion1.getOperator();
        CriterionOperator op2 = criterion2.getOperator();
        String value1 = criterion1.getValue();
        String value2 = criterion1.getValue();

        boolean field = field1 != null ? field1.equals(field2) : field2 == null;
        boolean op = op1 != null ? op1.equals(op2) : op2 == null;
        boolean value = value1 != null ? value1.equals(value2) : value2 == null;

        return field && op && value;
    }

    protected void verifyCriterionObjNotEqualButValuesEqual(List<List<AuthorizationCriterion>> authRules1, List<List<AuthorizationCriterion>> authRules2) {
        assertTrue(authRules1 != authRules2);
        assertEquals(authRules1.size(), authRules2.size());
        int counter = 0;
        for (List<AuthorizationCriterion> checkRules1 : authRules1) {
            for (AuthorizationCriterion checkCriterion1 : checkRules1) {
                counter++;
            }
        }

        int checkCounter = 0;
        for (List<AuthorizationCriterion> checkRules2 : authRules2) {
            for (AuthorizationCriterion checkCriterion2 : checkRules2) {
                checkCounter++;
            }
        }

        assertEquals(checkCounter, counter);

        int matchesFoundCount = 0;
        for (List<AuthorizationCriterion> checkRules1 : authRules1) {
            for (AuthorizationCriterion checkCriterion1 : checkRules1) {
                for (List<AuthorizationCriterion> checkRules2 : authRules2) {
                    assertTrue(checkRules1 != checkRules2);
                    for (AuthorizationCriterion checkCriterion2 : checkRules2) {
                        assertTrue(checkCriterion1 != checkCriterion2);
                        assertTrue(checkCriterion1.getField() != checkCriterion2.getField());
                        if (criterionIsEqual(checkCriterion1, checkCriterion2)) {
                            matchesFoundCount++;
                        }
                    }
                }
            }
        }

        assertEquals(matchesFoundCount, counter);
    }

    protected void verifyRangeObjNotEqualButValuesEqual(List<Range> ranges1, List<Range> ranges2) {
        if (ranges1 == null && ranges2 == null) {
            return;
        }
        assertTrue(ranges1 != ranges2);
        assertEquals(ranges1.size(), ranges2.size());
        for (Range checkRange1 : ranges1) {
            boolean matchFound = false;
            for (Range checkRange2 : ranges2) {
                assertTrue(checkRange1 != checkRange2);
                if (checkRange1.getMin() == checkRange2.getMin() &&
                        checkRange1.getMax() == checkRange2.getMax()) {
                    matchFound = true;
                }
            }
            assertTrue(matchFound);
        }
    }

    protected void verifyItemEqual(Item item1, Item item2) {
        if (item1 == null && item2 == null) {
            return;
        }
        assertEquals(item1.getId(), item2.getId());
        assertEquals(item1.getRowId(), item2.getRowId());
        assertEquals(item1.getName(), item2.getName());
        assertEquals(item1.getDescription(), item2.getDescription());
    }

    private boolean checkItemEqual(Item item1, Item item2) {
        if (item1 == null && item2 == null) {
            return true;
        }
        return Objects.equals(item1.getId(), item2.getId()) &&
                Objects.equals(item1.getRowId(), item2.getRowId()) &&
                Objects.equals(item1.getName(), item2.getName()) &&
                Objects.equals(item1.getDescription(), item2.getDescription());
    }

    protected void verifyAuthorizationEqual(Authorization auth1, Authorization auth2) {
        assertEquals(auth1.getId(), auth2.getId());
        assertEquals(auth1.getRowId(), auth2.getRowId());
        assertEquals(auth1.getType(), auth2.getType());
        assertEquals(auth1.isAssignItemCode(), auth2.isAssignItemCode());
        assertEquals(auth1.getCustomDeliveryForm(), auth2.getCustomDeliveryForm());
        if (auth1.getDateCreated() != null && auth2.getDateCreated() != null) {
            Long auth1sec = Long.valueOf(auth1.getDateCreated().getEpochSecond());
            Long auth2sec = Long.valueOf(auth2.getDateCreated().getEpochSecond());
            assertEquals(auth1sec, auth2sec);
        } else {
            assertEquals(auth1.getDateCreated(), auth2.getDateCreated());
        }
        assertEquals(auth1.getForIndividual(), auth2.getForIndividual());
        verifyItemEqual(auth1.getItem(), auth2.getItem());
        assertEquals(auth1.getItemRanges(), auth2.getItemRanges());
        assertEquals(auth1.getVoucherRanges(), auth2.getVoucherRanges());
        assertEquals(auth1.getExtraFieldEntitlements(), auth2.getExtraFieldEntitlements());
        assertEquals(auth1.getRules(), auth2.getRules());
        assertEquals(auth1.getStatus(), auth2.getStatus());
        assertEquals(auth1.getStatusReason(), auth2.getStatusReason());
    }

    private boolean checkAuthorizationEqual(Authorization auth1, Authorization auth2) {

        if (auth1.getDateCreated() != null && auth2.getDateCreated() != null) {
            Long auth1sec = Long.valueOf(auth1.getDateCreated().getEpochSecond());
            Long auth2sec = Long.valueOf(auth2.getDateCreated().getEpochSecond());
            if (!auth1sec.equals(auth2sec))
                return false;
        }

        if (!checkItemEqual(auth1.getItem(), auth2.getItem()))
            return false;

        boolean bool1 = Objects.equals(auth1.getId(), auth2.getId()) &&
                Objects.equals(auth1.getRowId(), auth2.getRowId()) &&
                Objects.equals(auth1.getType(), auth2.getType()) &&
                Objects.equals(auth1.isAssignItemCode(), auth2.isAssignItemCode()) &&
                Objects.equals(auth1.getCustomDeliveryForm(), auth2.getCustomDeliveryForm());

        boolean bool2 = Objects.equals(auth1.getForIndividual(), auth2.getForIndividual()) &&
                Objects.equals(auth1.getItemRanges(), auth2.getItemRanges()) &&
                Objects.equals(auth1.getVoucherRanges(), auth2.getVoucherRanges()) &&
                Objects.equals(auth1.getExtraFieldEntitlements(), auth2.getExtraFieldEntitlements()) &&
                Objects.equals(auth1.getRules(), auth2.getRules()) &&
                Objects.equals(auth1.getStatus(), auth2.getStatus()) &&
                Objects.equals(auth1.getStatusReason(), auth2.getStatusReason());

        return bool1 && bool2;
    }

    protected void verifyDistributionEqual(Distribution dist1, Distribution dist2) {
        assertEquals(dist1.getId(), dist2.getId());
        assertEquals(dist1.getRowId(), dist2.getRowId());

        List<Authorization> authList1 = dist1.getAuthorizations();
        List<Authorization> authList2 = dist2.getAuthorizations();
        if (authList1 != null && authList2 != null) {
            for (Authorization auth1 : authList1) {
                boolean foundMatch = false;
                for (Authorization auth2 : authList2) {
                    if (checkAuthorizationEqual(auth1, auth2)) {
                        foundMatch = true;
                    }
                }

                if (!foundMatch) {
                    fail("auths are not equal");
                }
            }
        } else if (authList1 == null && authList2 == null) {
            // both are null, so they are equal so verified
        } else {
            fail("auths are not equal");
        }

        if (dist1.getDateCreated() != null && dist2.getDateCreated() != null) {
            Long auth1sec = Long.valueOf(dist1.getDateCreated().getEpochSecond());
            Long auth2sec = Long.valueOf(dist2.getDateCreated().getEpochSecond());
            assertEquals(auth1sec, auth2sec);
        } else {
            assertEquals(dist1.getDateCreated(), dist2.getDateCreated());
        }
        assertEquals(dist1.getDescription(), dist2.getDescription());
        assertEquals(dist1.getLocation(), dist2.getLocation());
        assertEquals(dist1.getName(), dist2.getName());
        assertEquals(dist1.getStatus(), dist2.getStatus());
        assertEquals(dist1.getStatusReason(), dist2.getStatusReason());
        assertEquals(dist1.getSummaryForm(), dist2.getSummaryForm());
        assertEquals(dist1.getSummaryVersion(), dist2.getSummaryVersion());
    }

    protected void verifyVisitProgramEqual(VisitProgram visit1, VisitProgram visit2) {
        if (visit1 == null && visit2 == null) {
            return;
        }
        assertEquals(visit1.getId(), visit2.getId());
        assertEquals(visit1.getRowId(), visit2.getRowId());
        assertEquals(visit1.getCustomVisitForm(), visit2.getCustomVisitForm());
        if (visit1.getDateCreated() != null && visit2.getDateCreated() != null) {
            Long auth1sec = Long.valueOf(visit1.getDateCreated().getEpochSecond());
            Long auth2sec = Long.valueOf(visit2.getDateCreated().getEpochSecond());
            assertEquals(auth1sec, auth2sec);
        } else {
            assertEquals(visit1.getDateCreated(), visit2.getDateCreated());
        }
        assertEquals(visit1.getDescription(), visit2.getDescription());
        assertEquals(visit1.isForMember(), visit2.isForMember());
        assertEquals(visit1.getName(), visit2.getName());
        assertEquals(visit1.getRules(), visit2.getRules());
    }

    protected void verifyVisitProgramSameAsOriginalWithNulls(VisitProgram orginial, VisitProgram encoded) {
        if (orginial == null && encoded == null) {
            return;
        }
        assertNull(encoded.getId());
        assertNull(encoded.getRowId());
        assertEquals(orginial.getCustomVisitForm(), encoded.getCustomVisitForm());
        if (orginial.getDateCreated() != null && encoded.getDateCreated() != null) {
            Long auth1sec = Long.valueOf(orginial.getDateCreated().getEpochSecond());
            Long auth2sec = Long.valueOf(encoded.getDateCreated().getEpochSecond());
            assertEquals(auth1sec, auth2sec);
        } else {
            assertEquals(orginial.getDateCreated(), encoded.getDateCreated());
        }
        assertNull(encoded.getDescription());
        assertEquals(orginial.isForMember(), encoded.isForMember());
        assertNull(encoded.getName());
        assertEquals(orginial.getRules(), encoded.getRules());
    }
}
