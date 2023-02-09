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

package edu.uw.cse.ifrcdemo.distplan.logic;

import edu.uw.cse.ifrcdemo.distplan.CsvRepoArgConverter;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.csv.FileCsvRepository;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationType;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.DistVisitProgStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.EntitlementStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvAuthorization;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvBeneficiaryEntity;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvDistribution;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvEntitlement;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvIndividual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvValidatorTest {
  private CsvValidator csvValidator;

  @BeforeEach
  void initCsvValidator() {
    csvValidator = new CsvValidator();
  }

  @Test
  @DisplayName("Validate an empty Authorization")
  void emptyAuthorization() {
    CsvAuthorization authorization = new CsvAuthorization();
    assertFalse(csvValidator.validateAuthorization(new FileCsvRepository()).test(authorization));
  }

  @Test
  @DisplayName("Validate a partial Authorization")
  void partialAuthorization() {
    CsvAuthorization authorization = new CsvAuthorization();
    authorization.setType(AuthorizationType.REQUIRED_REGISTRATION);
    assertFalse(csvValidator.validateAuthorization(new FileCsvRepository()).test(authorization));

    authorization = new CsvAuthorization();
    authorization.setStatus(AuthorizationStatus.ACTIVE);
    assertFalse(csvValidator.validateAuthorization(new FileCsvRepository()).test(authorization));
  }

  @Test
  @DisplayName("Validate a valid Authorization")
  void validAuthorization() {
    CsvAuthorization authorization = new CsvAuthorization();

    authorization.setRowId("rowId");
    authorization.setType(AuthorizationType.REQUIRED_REGISTRATION);
    authorization.setStatus(AuthorizationStatus.ACTIVE);
    authorization.setDistributionId("uuid:0bd06ba7-e582-4d04-9188-de5897811cf6");

    CsvRepository repo = CsvRepoArgConverter.build("testFiles/sample_output");
    assertTrue(csvValidator.validateAuthorization(repo).test(authorization));
  }

  @Test
  @DisplayName("Validate an empty Entitlement")
  void emptyEntitlement() {
    assertFalse(csvValidator.validateEntitlement().test(new CsvEntitlement()));
  }

  @Test
  @DisplayName("Validate a valid Entitlement")
  void validEntitlement() {
    CsvEntitlement entitlement = new CsvEntitlement();

    entitlement.setRowId("rowId");
    entitlement.setAuthorizationId("authId");
    entitlement.setAuthorizationType(AuthorizationType.NO_REGISTRATION);
    entitlement.setDateCreated("date");
    entitlement.setIsOverride("false");
    entitlement.setItemId("itemPackId");
    entitlement.setStatus(EntitlementStatus.ENABLED);

    assertTrue(csvValidator.validateEntitlement().test(entitlement));
  }

  @Test
  @DisplayName("Validate an Entitlement with registration required but beneficiary entity id missing")
  void invalidEntitlementRegRequired() {
    CsvEntitlement entitlement = new CsvEntitlement();

    entitlement.setAuthorizationId("authId");
    entitlement.setAuthorizationType(AuthorizationType.REQUIRED_REGISTRATION);
    entitlement.setDateCreated("date");
    entitlement.setIsOverride("false");
    entitlement.setItemId("itemPackId");
    entitlement.setStatus(EntitlementStatus.ENABLED);

    assertFalse(csvValidator.validateEntitlement().test(entitlement));
  }

  @Test
  @DisplayName("Validate a valid Entitlement with registration required")
  void validEntitlementRegRequired() {
    CsvEntitlement entitlement = new CsvEntitlement();

    entitlement.setRowId("rowId");
    entitlement.setAuthorizationId("authId");
    entitlement.setAuthorizationType(AuthorizationType.REQUIRED_REGISTRATION);
    entitlement.setDateCreated("date");
    entitlement.setIsOverride("false");
    entitlement.setItemId("itemPackId");
    entitlement.setStatus(EntitlementStatus.ENABLED);
    entitlement.setBeneficiaryEntityId("beId");

    assertTrue(csvValidator.validateEntitlement().test(entitlement));
  }

  @Test
  @DisplayName("Validate an empty Beneficiary Entity")
  void emptyBeneficiaryEntity() {
    assertFalse(csvValidator.validateBeneficiaryEntity(new FileCsvRepository()).test(new CsvBeneficiaryEntity()));
  }

  @Test
  @DisplayName("Validate an empty Individual")
  void validateIndividual() {
    assertFalse(csvValidator.validateIndividual(new FileCsvRepository()).test(new CsvIndividual()));
  }

  @Test
  @DisplayName("Validate an empty Distribution")
  void emptyDistribution() {
    assertFalse(csvValidator.validateDistribution().test(new CsvDistribution()));
  }

  @Test
  @DisplayName("Validate a partial Distribution")
  void partialDistribution() {
    CsvDistribution csvDistribution = new CsvDistribution();

    csvDistribution.setRowId("rowId");
    csvDistribution.setStatus(DistVisitProgStatus.ACTIVE);

    assertFalse(csvValidator.validateDistribution().test(csvDistribution));
  }

  @Test
  @DisplayName("Validate a valid Distribution")
  void validDistribution() {
    CsvDistribution csvDistribution = new CsvDistribution();

    csvDistribution.setRowId("rowId");
    csvDistribution.setName("Distribution Name");
    csvDistribution.setStatus(DistVisitProgStatus.ACTIVE);

    assertTrue(csvValidator.validateDistribution().test(csvDistribution));
  }
}
