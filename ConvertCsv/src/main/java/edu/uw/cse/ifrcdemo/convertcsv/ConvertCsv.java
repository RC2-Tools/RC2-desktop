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

package edu.uw.cse.ifrcdemo.convertcsv;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.sharedlib.consts.csv.MobileDbConsts;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.BeneficiaryEntityStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.IndividualStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.SuitcaseUploadOperation;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvBeneficiaryEntity;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvIndividual;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvSuitcaseSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.UntypedSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.BeneficiaryEntity;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.Individual;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.SyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.util.CsvMapperUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.StringUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.TestDataUtil;
import org.opendatakit.aggregate.odktables.rest.TableConstants;
import org.opendatakit.aggregate.odktables.rest.entity.RowFilterScope;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PrimitiveIterator;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;


public abstract class ConvertCsv {
  private static final String UUID_PREFIX = "uuid:";
  private static final String EMPTY_STR = "";
  private static final String BENEFICIARY_ENTITIES_CSV = MobileDbConsts.TableNames.BENEFICIARY_ENTITY_TABLE_NAME + GenConsts.CSV_FILE_EXTENSION;
  private static final String MEMBERS_CSV = MobileDbConsts.TableNames.INDIVIDUAL_TABLE_NAME + GenConsts.CSV_FILE_EXTENSION;

  private final String[] inputColumns;
  private final String[] outputColumns;
  private final List<CsvBeneficiaryEntity> beneficiaryBaseTable;
  private final List<UntypedSyncRow> beneficiaryCustomTable;
  private final List<CsvIndividual> memberBaseTable;
  private final String customBenEntityFormId;
  private final String groupId;

  private final Map<String, String> customBeneficiaryEntityRowIdToBaseRowId;

  final Map<String, ColumnMatch> columnMatch;
  final Map<String, Choices> choicesVerify;

  private String dateCreatedColumn;
  private String rowIdColumn;

  private String rcidColunm;

  abstract void createMappings();

  public ConvertCsv(String customBenEntityFormId, String[] inputColumns, String[] outputColumns, String groupId) throws IOException {
    this.inputColumns = inputColumns;
    this.outputColumns = outputColumns;
    this.columnMatch = new HashMap<String, ColumnMatch>();
    this.choicesVerify = new HashMap<String, Choices>();
    this.beneficiaryBaseTable = new ArrayList<>();
    this.beneficiaryCustomTable = new ArrayList<>();
    this.memberBaseTable = new ArrayList<>();
    this.customBeneficiaryEntityRowIdToBaseRowId = new HashMap<>();
    this.customBenEntityFormId = customBenEntityFormId;
    this.groupId = groupId;
    this.dateCreatedColumn = null;

    createMappings();
  }

  void setDateCreatedColumn(String dateCreatedColumn) {
    this.dateCreatedColumn = dateCreatedColumn;
  }

  void setRowIdColumn(String rowIdColumn) {
    this.rowIdColumn = rowIdColumn;
  }

  public void setRcidColunm(String rcidColunm) {
    this.rcidColunm = rcidColunm;
  }

  void convertByAssigningRcid(String importCsv, String csvWritePath, PrimitiveIterator.OfInt rcIdRange) throws IOException {
    List<UntypedSyncRow> hhRows = readCsv(Paths.get(importCsv));

    processHouseholdsNCreateNewRcids(hhRows, rcIdRange);

    writeBeneficiaryEntityTables(Paths.get(csvWritePath));
    writeMemberTables(Paths.get(csvWritePath));
  }


  void convert(String importCsv, String csvWritePath) throws IOException {
    List<UntypedSyncRow> hhRows = readCsv(Paths.get(importCsv));

    processHouseholdsNCreateNewRcids(hhRows, null);

    writeBeneficiaryEntityTables(Paths.get(csvWritePath));
    writeMemberTables(Paths.get(csvWritePath));
  }


  private void processHouseholdsNCreateNewRcids(List<UntypedSyncRow> rawRows, PrimitiveIterator.OfInt rcIdRange) throws IOException {

    for (UntypedSyncRow rawRow : rawRows) {
      CsvBeneficiaryEntity entityBaseRow = new CsvBeneficiaryEntity();
      UntypedSyncRow entityCustomRow = new UntypedSyncRow();

      beneficiaryCustomTable.add(entityCustomRow);
      beneficiaryBaseTable.add(entityBaseRow);

      for (String column : inputColumns) {
        ColumnMatch columnObj = columnMatch.get(column);
        if(columnObj == null) {
          throw new IOException("Did not find the column to process!!!! : " + column);
        }
        String colValue = rawRow.getColumns().get(columnObj.getInputColumnName());
        Choices choices = choicesVerify.get(columnObj.getInputColumnName());
        if(choices != null && colValue != null && !colValue.isEmpty()) {
          String verifiedChoice = choices.verifyValidValue(colValue);
          if(verifiedChoice != null) {
            colValue = verifiedChoice;
          } else {
            throw new IllegalStateException("Choice is not a valid option: " + colValue + " for column " + columnObj.getOutputColumnName());
          }
        }
        entityCustomRow.getColumns().put(columnObj.getOutputColumnName(), colValue);
      }

      String dateCreated = null;
      if(dateCreatedColumn != null) {
        dateCreated = convertDateCreated(rawRow.getColumns().get(dateCreatedColumn));
      } else {
        dateCreated = TableConstants.nanoSecondsFromMillis(System.currentTimeMillis(), TableConstants.TIMESTAMP_LOCALE);;
      }

      String customRowId;
      if(rowIdColumn != null) {
        String stringNumber = rawRow.getColumns().get(rowIdColumn);
        Long number = Long.parseLong(stringNumber);
        UUID uuid = new UUID(0L, number.longValue());
        customRowId = UUID_PREFIX + uuid.toString();
      } else {
        customRowId = TestDataUtil.syncUuidGenerator();
      }

      entityCustomRow.setRowId(customRowId);
      entityCustomRow.setRowFilterScope(new RowFilterScope(RowFilterScope.Access.HIDDEN, EMPTY_STR, EMPTY_STR, groupId, EMPTY_STR));
      entityCustomRow.setFormId(customBenEntityFormId);

      entityBaseRow.setRowId(TestDataUtil.syncUuidGenerator());
      entityBaseRow.setRowFilterScope(new RowFilterScope(RowFilterScope.Access.HIDDEN, EMPTY_STR, EMPTY_STR, groupId, EMPTY_STR));
      if(dateCreated != null) {
        entityBaseRow.setDateCreated(dateCreated);
      }
      if(rcidColunm != null) {
        String rcid = rawRow.getColumns().get(rcidColunm);
        if(rcid != null && !rcid.isEmpty()) {
          entityBaseRow.setBeneficiaryEntityId(rcid);
        } else {
          throw new IllegalStateException("No RCID specified for row!!!");
        }
      } else if(rcIdRange != null) {
        entityBaseRow.setBeneficiaryEntityId(rcIdRange.next().toString());
      } else {
        throw new IllegalStateException("No RCID specified!!! Please either specify the column to read the RCID from or specify the number range to create new RCIDs");
      }
      entityBaseRow.setCustomTableFormId(customBenEntityFormId);
      entityBaseRow.setCustomTableRowId(entityCustomRow.getRowId());
      entityBaseRow.setStatus(BeneficiaryEntityStatus.ENABLED);

      customBeneficiaryEntityRowIdToBaseRowId.put(entityCustomRow.getRowId(), entityBaseRow.getRowId());

      CsvIndividual individualBaseRow = new CsvIndividual();
      individualBaseRow.setRowId(TestDataUtil.syncUuidGenerator());
      individualBaseRow.setRowFilterScope(new RowFilterScope(RowFilterScope.Access.FULL, EMPTY_STR, EMPTY_STR, groupId, EMPTY_STR));
      if(dateCreated != null) {
        individualBaseRow.setDateCreated(dateCreated);
      }
      individualBaseRow.setStatus(IndividualStatus.ENABLED);
      individualBaseRow.setBeneficiaryEntityRowId(entityBaseRow.getRowId());
      memberBaseTable.add(individualBaseRow);
    }

    checkCustomTableIdUnique(beneficiaryCustomTable);
  }


  private void writeBeneficiaryEntityTables(Path writePath) throws IOException {
    Files.createDirectories(writePath);

    CsvMapper csvMapper = CsvMapperUtil.getCsvMapper(true);

    ObjectWriter baseTableWriter = CsvMapperUtil.getWriterForSuitcaseSyncRow(BeneficiaryEntity.class, csvMapper, true);
    ObjectWriter customTableWriter = CsvMapperUtil.getWriterForSuitcaseSyncRow(Arrays.asList(outputColumns), csvMapper, true);

    try (Writer bufferedWriter = Files.newBufferedWriter(writePath.resolve(BENEFICIARY_ENTITIES_CSV), StandardCharsets.UTF_8);
         SequenceWriter sequenceWriter = baseTableWriter.writeValues(bufferedWriter)) {
      for (CsvBeneficiaryEntity baseRow : beneficiaryBaseTable) {
        sequenceWriter.write(new CsvSuitcaseSyncRow<>(SuitcaseUploadOperation.FORCE_UPDATE, baseRow));
      }
    }

    try (Writer bufferedWriter = Files.newBufferedWriter(writePath.resolve(customBenEntityFormId + GenConsts.CSV_FILE_EXTENSION), StandardCharsets.UTF_8);
         SequenceWriter sequenceWriter = customTableWriter.writeValues(bufferedWriter)) {
      for (UntypedSyncRow customRow : beneficiaryCustomTable) {
        sequenceWriter.write(new CsvSuitcaseSyncRow<>(SuitcaseUploadOperation.FORCE_UPDATE, customRow));
      }
    }
  }

  private void writeMemberTables(Path writePath) throws IOException {
    Files.createDirectories(writePath);

    CsvMapper csvMapper = CsvMapperUtil.getCsvMapper(true);

    ObjectWriter baseTableWriter = CsvMapperUtil.getWriterForSuitcaseSyncRow(Individual.class, csvMapper, true);
   
    try (Writer bufferedWriter = Files.newBufferedWriter(writePath.resolve(MEMBERS_CSV), StandardCharsets.UTF_8);
         SequenceWriter sequenceWriter = baseTableWriter.writeValues(bufferedWriter)) {
      for (CsvIndividual baseRow : memberBaseTable) {
        sequenceWriter.write(new CsvSuitcaseSyncRow<>(SuitcaseUploadOperation.FORCE_UPDATE, baseRow));
      }
    }
    
  }

  private static List<UntypedSyncRow> readCsv(Path csvPath) throws IOException {
    CsvMapper csvMapper = (CsvMapper) CsvMapperUtil
        .getCsvMapper(true)
        .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
    ObjectReader reader = CsvMapperUtil.getReader(csvMapper, true);

    try (InputStream inputStream = Files.newInputStream(csvPath);
         MappingIterator<UntypedSyncRow> it = reader.readValues(inputStream)) {
      return it.readAll();
    }
  }
  
  private static String valueToLowerCase(String key, String value) {
    if (StringUtil.isNullOrEmpty(value)) {
      return value;
    }

    return value.toLowerCase();
  }

  private static BiFunction<String, String, String> lookupValueFromMap(Map<String, String> map) {
    return (__, oldValue) -> map.getOrDefault(oldValue, oldValue);
  }

  private static String mapGender(String key, String value) {
    if (value.equals("Prefer not to answer")) {
      return "No_Answer";
    }

    return value;
  }

  private static String convertDateCreated(String date) {
    LocalDateTime datetime = LocalDateTime.parse(date, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    return TableConstants.nanoSecondsFromMillis(datetime.toInstant(ZoneOffset.UTC).toEpochMilli(), TableConstants.TIMESTAMP_LOCALE);
  }

  private static String convertBirthDate(String key, String value) {
    if (StringUtil.isNullOrEmpty(value)) {
      return value;
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/uuuu");
    LocalDateTime datetime;
    try {
      datetime = LocalDateTime.of(LocalDate.parse(value, formatter), LocalTime.MIN);
    } catch (Exception e) {
      System.out.println("Malformed birth_date " + value);
      return value;
    }
    return TableConstants.nanoSecondsFromMillis(datetime.toInstant(ZoneOffset.UTC).toEpochMilli(), TableConstants.TIMESTAMP_LOCALE);
  }

  private static void checkCustomTableIdUnique(List<UntypedSyncRow> rows) {
    Map<String, Long> idCount = rows
        .stream()
        .map(SyncRow::getRowId)
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

    idCount.values().removeIf(count -> count < 2);
    if (idCount.size() > 0) {
      throw new IllegalArgumentException("Duplicate IDs in custom table. " + idCount);
    }
  }
}
