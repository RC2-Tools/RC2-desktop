package edu.uw.cse.ifrcdemo.distplan.logic;

import edu.uw.cse.ifrcdemo.distplan.CsvRepoArgConverter;
import edu.uw.cse.ifrcdemo.distplan.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.distplan.model.csv.FileCsvRepository;
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
    entitlement.setItemPackId("itemPackId");
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
    entitlement.setItemPackId("itemPackId");
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
    entitlement.setItemPackId("itemPackId");
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
