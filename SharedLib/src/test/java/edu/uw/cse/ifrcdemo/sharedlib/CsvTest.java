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

package edu.uw.cse.ifrcdemo.sharedlib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import edu.uw.cse.ifrcdemo.sharedlib.model.config.AuxiliaryProperty;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationType;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.RegistrationMode;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvIndividual;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.UntypedSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.Individual;
import edu.uw.cse.ifrcdemo.sharedlib.util.CsvMapperUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.opendatakit.aggregate.odktables.rest.entity.RowFilterScope;

import java.io.IOException;

public class CsvTest {
  private static final String CSV_PATH = "csv/CsvTest/";
  private static final String INDIVIDUAL_CSV_PATH = CSV_PATH + "/individual_";
  private static final String INDIVIDUAL_EXTRA_CSV_PATH = INDIVIDUAL_CSV_PATH + "extra";

  @ParameterizedTest
  @ValueSource(strings = {
      INDIVIDUAL_EXTRA_CSV_PATH + ".csv",
      INDIVIDUAL_EXTRA_CSV_PATH + "_unordered_columns.csv",
      INDIVIDUAL_EXTRA_CSV_PATH + "_undefined_column.csv"
  })
  void testIndividualDeserialization_extra(String path) throws IOException {
    CsvMapper mapper = CsvMapperUtil.getCsvMapper(true);

    MappingIterator<CsvIndividual> iterator = CsvMapperUtil
        .getReader(CsvIndividual.class, mapper, true)
        .readValues(TestUtil.getResource(path));

    CsvIndividual expected = new CsvIndividual();

    expected.setRowId("uuid:1809fc08-2de3-4b1d-bb7a-9ff2845a1f8a");
    expected.setRowETag("uuid:7b259f93-f21c-4a20-ba7b-d95ab5775180");
    expected.setDataETagAtModification(null);
    expected.setDeleted(false);
    expected.setCreateUser("username:olive@mezuricloud.com");
    expected.setLastUpdateUser("username:olive@mezuricloud.com");
    expected.setFormId("registrationMember");
    expected.setLocale("default");
    expected.setSavepointType("COMPLETE");
    expected.setSavepointTimestamp("2017-06-01T00:59:56.743000000");
    expected.setSavepointCreator("username:olive@mezuricloud.com");
    expected.setRowFilterScope(new RowFilterScope(
        RowFilterScope.Access.HIDDEN,
        "username:olive@mezuricloud.com",
        "",
        "GROUP_NORTH",
        ""
    ));
    expected.setDateCreated("1500000000000");
    expected.setCustomMemberFormId("i_form_id");
    expected.setCustomMemberRowId("1");
    expected.setMemberId("1234");
    expected.setBeneficiaryEntityRowId("1001");
    expected.setBeneficiaryEntityStatus("1");
    expected.setStatus(null);
    expected.setStatusReason(null);

    // fixme:
    // 1. expected and actual should be swapped
    // 2. fix CSVs, use generator?
    Assertions.assertEquals(expected, iterator.nextValue());
  }

  @Test
  void testIndividualSerialization_extra() throws JsonProcessingException {
    CsvMapper mapper = CsvMapperUtil.getCsvMapper(true);

    CsvIndividual expected = new CsvIndividual();

    expected.setRowId("uuid:1809fc08-2de3-4b1d-bb7a-9ff2845a1f8a");
    expected.setRowETag("uuid:7b259f93-f21c-4a20-ba7b-d95ab5775180");
    expected.setDataETagAtModification(null);
    expected.setDeleted(false);
    expected.setCreateUser("username:olive@mezuricloud.com");
    expected.setLastUpdateUser("username:olive@mezuricloud.com");
    expected.setFormId("registrationMember");
    expected.setLocale("default");
    expected.setSavepointType("COMPLETE");
    expected.setSavepointTimestamp("2017-06-01T00:59:56.743000000");
    expected.setSavepointCreator("username:olive@mezuricloud.com");
    expected.setRowFilterScope(new RowFilterScope(
        RowFilterScope.Access.HIDDEN,
        "username:olive@mezuricloud.com",
        "",
        "GROUP_NORTH",
        ""
    ));
    expected.setDateCreated("1500000000000");
    expected.setCustomMemberFormId("i_form_id");
    expected.setCustomMemberRowId("1");
    expected.setMemberId("1234");
    expected.setBeneficiaryEntityRowId("1001");
    expected.setBeneficiaryEntityStatus("1");
    expected.setStatus(null);
    expected.setStatusReason(null);

    System.out.println(CsvMapperUtil.getWriter(CsvIndividual.class, Individual.class, mapper, true).writeValueAsString(expected));

    // TODO: write assertion
  }

  @Test
  void testConvert() {
    CsvIndividual expected = new CsvIndividual();

    expected.setRowId("uuid:1809fc08-2de3-4b1d-bb7a-9ff2845a1f8a");
    expected.setRowETag("uuid:7b259f93-f21c-4a20-ba7b-d95ab5775180");
    expected.setDataETagAtModification(null);
    expected.setDeleted(false);
    expected.setCreateUser("username:olive@mezuricloud.com");
    expected.setLastUpdateUser("username:olive@mezuricloud.com");
    expected.setFormId("registrationMember");
    expected.setLocale("default");
    expected.setSavepointType("COMPLETE");
    expected.setSavepointTimestamp("2017-06-01T00:59:56.743000000");
    expected.setSavepointCreator("username:olive@mezuricloud.com");
    expected.setRowFilterScope(new RowFilterScope(
        RowFilterScope.Access.HIDDEN,
        "username:olive@mezuricloud.com",
        "",
        "GROUP_NORTH",
        ""
    ));
    expected.setDateCreated("1500000000000");
    expected.setCustomMemberFormId("i_form_id");
    expected.setCustomMemberRowId("1");
    expected.setMemberId("1234");
    expected.setBeneficiaryEntityRowId("1001");
    expected.setBeneficiaryEntityStatus("1");
    expected.setStatus(null);
    expected.setStatusReason(null);

    System.out.println(CsvMapperUtil.convertToRow(CsvMapperUtil.getJsonMapper(), expected));

    // TODO: write assertion
  }

  @ParameterizedTest
  @ValueSource(strings = {
      INDIVIDUAL_EXTRA_CSV_PATH + ".csv",
      INDIVIDUAL_EXTRA_CSV_PATH + "_unordered_columns.csv",
      INDIVIDUAL_EXTRA_CSV_PATH + "_undefined_column.csv"
  })
  void testUntypedDeserialization_extra(String path) throws IOException {
    CsvMapper mapper = CsvMapperUtil.getCsvMapper(true);
    MappingIterator<UntypedSyncRow> rows = CsvMapperUtil
        .getReader(mapper, true)
        .readValues(TestUtil.getResource(path));

    System.out.println(rows.nextValue());

    // TODO: write assertion
  }

  @ParameterizedTest
  @ValueSource(strings = {
      INDIVIDUAL_EXTRA_CSV_PATH + ".csv",
      INDIVIDUAL_EXTRA_CSV_PATH + "_unordered_columns.csv",
      INDIVIDUAL_EXTRA_CSV_PATH + "_undefined_column.csv"
  })
  void testUntypedSerialization_extra(String path) throws IOException {
    CsvMapper mapper = CsvMapperUtil.getCsvMapper(true);

    UntypedSyncRow row = (UntypedSyncRow) CsvMapperUtil
        .getReader(mapper, true)
        .readValues(TestUtil.getResource(path))
        .nextValue();

    System.out.println(row);

    String written = CsvMapperUtil
        .getWriter(mapper, row.getColumns().keySet(), true)
        .writeValueAsString(row);

    System.out.println(written);

    // TODO: write assertion
  }

  @Test
  void testTablesConfigSerialization() throws JsonProcessingException {
    AuxiliaryProperty config = new AuxiliaryProperty();

    String beneficiaryEntityCustomFormId = "beCustomForm";
    String individualCustomFormId = "indCustomForm";

    config.setRegistrationMode(RegistrationMode.HOUSEHOLD);
    config.setWorkflowMode(AuthorizationType.REQUIRED_REGISTRATION);
    config.setBeneficiaryEntityCustomFormId(beneficiaryEntityCustomFormId);
    config.setCustomBeneficiaryRowIdColumn("custom_beneficiary_entity_row_id");

    Assertions.assertEquals(
        "{\"REGISTRATION_MODE\":\"HOUSEHOLD\",\"WORKFLOW_MODE\":\"REQUIRED_REGISTRATION\",\"BENEFICIARY_ENTITY_CUSTOM_FORM_ID\":\"beCustomForm\",\"MEMBER_CUSTOM_FORM_ID\":null,\"CUSTOM_BENEFICIARY_ROW_ID_COLUMN\":\"custom_beneficiary_entity_row_id\"}",
        new ObjectMapper().writerFor(AuxiliaryProperty.class).writeValueAsString(config)
    );

    config.setRegistrationMode(RegistrationMode.INDIVIDUAL);
    config.setMemberCustomFormId(individualCustomFormId);
    Assertions.assertEquals(
        "{\"REGISTRATION_MODE\":\"INDIVIDUAL\",\"WORKFLOW_MODE\":\"REQUIRED_REGISTRATION\",\"BENEFICIARY_ENTITY_CUSTOM_FORM_ID\":\"beCustomForm\",\"MEMBER_CUSTOM_FORM_ID\":\"indCustomForm\",\"CUSTOM_BENEFICIARY_ROW_ID_COLUMN\":\"custom_beneficiary_entity_row_id\"}",
        new ObjectMapper().writerFor(AuxiliaryProperty.class).writeValueAsString(config)
    );
  }
}
