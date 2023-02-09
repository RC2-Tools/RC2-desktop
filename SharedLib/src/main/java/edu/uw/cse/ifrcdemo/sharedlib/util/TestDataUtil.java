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

package edu.uw.cse.ifrcdemo.sharedlib.util;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import edu.uw.cse.ifrcdemo.sharedlib.consts.csv.MobileDbConsts;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.BeneficiaryEntityStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.IndividualStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.BaseSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvBeneficiaryEntity;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvIndividual;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.UntypedSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.BeneficiaryEntity;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.Individual;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.opendatakit.aggregate.odktables.rest.SavepointTypeManipulator;
import org.opendatakit.aggregate.odktables.rest.entity.RowFilterScope;

public class TestDataUtil {
  private static final String UUID_PREFIX = "uuid:";

  private static final String LOCALE = "default";
  private static final String SAVEPOINT_TYPE = SavepointTypeManipulator.complete();
  private static final String SAVEPOINT_CREATOR = "username:test_data_generator";
  private static final String CREATE_USER = "username:test_data_generator";
  private static final String LAST_UPDATE_USER = "username:test_data_generator";

  private static final RowFilterScope ROW_FILTER_SCOPE = new RowFilterScope(
      RowFilterScope.Access.FULL,
      CREATE_USER,
      "group_ro",
      "group_w",
      "group_p"
  );

  /**
   * Write out tables with generated noise
   *
   * @param rows number of rows for each table
   * @param strCols number of columns for the custom tables
   * @param extraMetadata
   * @param path
   * @throws IOException
   */
  public static void writeAll(int rows, int strCols, int intCols, int doubleCols, boolean extraMetadata, Path path) throws IOException {
    Function<List<? extends BaseSyncRow>, List<String>> getRowId = list -> list
        .stream()
        .map(BaseSyncRow::getRowId)
        .collect(Collectors.toList());

    List<UntypedSyncRow> customBeneficiaryEntities1 =
        generateUntyped("custom_" + MobileDbConsts.TableNames.BENEFICIARY_ENTITY_TABLE_NAME + "_1", rows / 3, strCols, intCols, doubleCols);
    List<UntypedSyncRow> customBeneficiaryEntities2 =
        generateUntyped("custom_" + MobileDbConsts.TableNames.BENEFICIARY_ENTITY_TABLE_NAME + "_2", rows / 3, strCols, intCols, doubleCols);

    List<UntypedSyncRow> customIndividuals1 =
        generateUntyped("custom_" + MobileDbConsts.TableNames.INDIVIDUAL_TABLE_NAME + "_1", rows / 3, strCols, intCols, doubleCols);
    List<UntypedSyncRow> customIndividuals2 =
        generateUntyped("custom_" + MobileDbConsts.TableNames.INDIVIDUAL_TABLE_NAME + "_2", rows / 3, strCols, intCols, doubleCols);

    List<CsvBeneficiaryEntity> beneficiaryEntities = Stream.of(
        generateBeneficiaryEntity("custom_" + MobileDbConsts.TableNames.BENEFICIARY_ENTITY_TABLE_NAME + "_1", getRowId.apply(customBeneficiaryEntities1)),
        generateBeneficiaryEntity("custom_" + MobileDbConsts.TableNames.BENEFICIARY_ENTITY_TABLE_NAME + "_2", getRowId.apply(customBeneficiaryEntities2)),
        generateBeneficiaryEntity(rows - rows / 3 - rows / 3)
    )
        .flatMap(Collection::stream)
        .collect(Collectors.toList());

    List<String> beneficiaryEntityRowIds = getRowId.apply(beneficiaryEntities);
    List<CsvIndividual> individuals = Stream.of(
        generateIndividual("custom_" + MobileDbConsts.TableNames.INDIVIDUAL_TABLE_NAME + "_1", getRowId.apply(customIndividuals1), beneficiaryEntityRowIds),
        generateIndividual("custom_" + MobileDbConsts.TableNames.INDIVIDUAL_TABLE_NAME + "_2", getRowId.apply(customIndividuals2), beneficiaryEntityRowIds),
        generateIndividual(rows - rows / 3 - rows / 3, beneficiaryEntityRowIds)
    )
        .flatMap(Collection::stream)
        .collect(Collectors.toList());

    CsvMapper mapper = CsvMapperUtil.getCsvMapper(extraMetadata);

    try(BufferedWriter writer = Files.newBufferedWriter(
        FileUtil.getPathToCSV(path, "custom_" + MobileDbConsts.TableNames.BENEFICIARY_ENTITY_TABLE_NAME + "_1"),
        StandardOpenOption.CREATE)) {
        writeValues(extraMetadata, customBeneficiaryEntities1, mapper, writer);
    }

    try(BufferedWriter writer = Files.newBufferedWriter(
        FileUtil.getPathToCSV(path, "custom_" + MobileDbConsts.TableNames.BENEFICIARY_ENTITY_TABLE_NAME + "_2"),
        StandardOpenOption.CREATE)) {
        writeValues(extraMetadata, customBeneficiaryEntities2, mapper, writer);
    }

    try(BufferedWriter writer = Files.newBufferedWriter(
        FileUtil.getPathToCSV(path, "custom_" + MobileDbConsts.TableNames.INDIVIDUAL_TABLE_NAME + "_1"),
        StandardOpenOption.CREATE)) {

        writeValues(extraMetadata, customIndividuals1, mapper, writer);
    }

    try(BufferedWriter writer = Files.newBufferedWriter(
        FileUtil.getPathToCSV(path, "custom_" + MobileDbConsts.TableNames.INDIVIDUAL_TABLE_NAME + "_2"),
        StandardOpenOption.CREATE)) {

        writeValues(extraMetadata, customIndividuals2, mapper, writer);
    }

    try(BufferedWriter writer = Files.newBufferedWriter(
        FileUtil.getPathToCSV(path, MobileDbConsts.TableNames.BENEFICIARY_ENTITY_TABLE_NAME),
        StandardOpenOption.CREATE)) {

        SequenceWriter sw = null;
        try {
            ObjectWriter objWriter = CsvMapperUtil.getWriter(CsvBeneficiaryEntity.class, BeneficiaryEntity.class, mapper, extraMetadata);
            sw = objWriter.writeValues(writer).writeAll(beneficiaryEntities);
        } catch (IOException e) {
            System.out.println("Failed to write table to disk: " + BeneficiaryEntity.class.getName());
            throw e;
        } finally {
            if(sw != null) {
                sw.close();
            }
        }
    }

    try(BufferedWriter writer = Files.newBufferedWriter(
        FileUtil.getPathToCSV(path, MobileDbConsts.TableNames.INDIVIDUAL_TABLE_NAME),
        StandardOpenOption.CREATE)) {

        SequenceWriter sw = null;
        try {
            ObjectWriter objWriter = CsvMapperUtil.getWriter(CsvIndividual.class, Individual.class, mapper, extraMetadata);
            sw = objWriter.writeValues(writer).writeAll(individuals);
        } catch (IOException e) {
            System.out.println("Failed to write table to disk: " + Individual.class.getName());
            throw e;
        } finally {
            if(sw != null) {
                sw.close();
            }
        }
    }
  }

    private static void writeValues(boolean extraMetadata, List<UntypedSyncRow> rowList, CsvMapper mapper, BufferedWriter writer) throws IOException {
        SequenceWriter sw = null;
        try {
            ObjectWriter objWriter = CsvMapperUtil.getWriter(mapper, rowList.get(0).getColumns().keySet(), extraMetadata);
            sw = objWriter.writeValues(writer).writeAll(rowList);
        } catch (IOException e) {
            System.out.println("Failed to write table to disk (list of rows)");
            throw e;
        } finally {
            if(sw != null) {
                sw.close();
            }
        }
    }

    public static List<CsvBeneficiaryEntity> generateBeneficiaryEntity(String customFormId,
                                                                     List<String> customTableRowIds) {
    // set seed to get more predictable result
    Random rand = new Random(customTableRowIds.size());

    // assume that formId == tableId
    String formId = MobileDbConsts.TableNames.BENEFICIARY_ENTITY_TABLE_NAME;

    return customTableRowIds
        .stream()
        .map(id -> {
          CsvBeneficiaryEntity row = new CsvBeneficiaryEntity();

          row.setFormId(formId);
          row.setBeneficiaryEntityId(randomIntString(5, rand));
          row.setCustomBeneficiaryEntityFormId(customFormId);
          row.setCustomBeneficiaryEntityRowId(customFormId == null ? null : id);
          row.setDateCreated(rfc1123Timestamp());
          row.setStatus(BeneficiaryEntityStatus.ENABLED);
          row.setStatusReason("status reason");

          return generateBaseSyncRow(row);
        })
        .collect(Collectors.toList());
  }

  /**
   * Generate without custom table
   *
   * @param count
   * @return
   */
  public static List<CsvBeneficiaryEntity> generateBeneficiaryEntity(int count) {
    return generateBeneficiaryEntity(null, IntStream.range(0, count).mapToObj(x -> "").collect(Collectors.toList()));
  }

  public static List<CsvIndividual> generateIndividual(String customFormId,
                                                       List<String> customTableRowIds,
                                                       List<String> beneficiaryEntityRowIds) {
    // set seed to get more predictable result
    Random rand = new Random(customTableRowIds.size());

    // assume that formId == tableId
    String formId = MobileDbConsts.TableNames.INDIVIDUAL_TABLE_NAME;

    return customTableRowIds
        .stream()
        .map(id -> {
          CsvIndividual csvIndividual = new CsvIndividual();

          csvIndividual.setFormId(formId);
          csvIndividual.setBeneficiaryEntityRowId(beneficiaryEntityRowIds.get(rand.nextInt(beneficiaryEntityRowIds.size())));
          csvIndividual.setBeneficiaryEntityStatus("b.e. status");
          csvIndividual.setCustomMemberFormId(customFormId);
          csvIndividual.setCustomMemberRowId(customFormId == null ? null : id);
          csvIndividual.setDateCreated(rfc1123Timestamp());
          csvIndividual.setMemberId(rand
              .ints(5, 0, 9)
              .mapToObj(String::valueOf)
              .collect(Collectors.joining())
          );
          csvIndividual.setStatus(IndividualStatus.ENABLED);
          csvIndividual.setStatusReason("status reason");

          return generateBaseSyncRow(csvIndividual);
        })
        .collect(Collectors.toList());
  }

  /**
   * Generate without custom table
   *
   * @param count
   * @param beneficiaryEntityRowIds
   * @return
   */
  public static List<CsvIndividual> generateIndividual(int count, List<String> beneficiaryEntityRowIds) {
    return generateIndividual(null, IntStream.range(0, count).mapToObj(x -> "").collect(Collectors.toList()), beneficiaryEntityRowIds);
  }

  public static List<UntypedSyncRow> generateUntyped(String formId, int rows, int strCols, int intCols, int doubleCols) {
    // set seed to get more predictable result
    Random rand = new Random(rows * (strCols + intCols + doubleCols) * formId.hashCode());

    List<String> strColumns = IntStream
        .range(0, strCols)
        .mapToObj(x -> (x % 2 == 0 ? "" : (formId + "_")) + "string_attribute_" + x)
        .collect(Collectors.toList());

    List<String> intColumns = IntStream
        .range(0, intCols)
        .mapToObj(x -> (x % 2 == 0 ? "" : (formId + "_")) + "int_attribute_" + x)
        .collect(Collectors.toList());

    List<String> doubleColumns = IntStream
        .range(0, doubleCols)
        .mapToObj(x -> (x % 2 == 0 ? "" : (formId + "_")) + "double_attribute_" + x)
        .collect(Collectors.toList());

    return Stream
        .generate(() -> {
          UntypedSyncRow row = new UntypedSyncRow();

          row.setFormId(formId);

          // fill each cell with a random 10-character alphabetic string
          row.getColumns().putAll(strColumns
              .stream()
              .collect(Collectors.toMap(Function.identity(), __ -> randomString(10, rand)))
          );

          row.getColumns().putAll(intColumns
              .stream()
              .collect(Collectors.toMap(Function.identity(), __ -> randomIntString(3, rand)))
          );

          row.getColumns().putAll(doubleColumns
              .stream()
              .collect(Collectors.toMap(Function.identity(), __ -> randomDoubleString(6, rand)))
          );

          return generateBaseSyncRow(row);
        })
        .limit(rows)
        .collect(Collectors.toList());
  }

  public static <T extends BaseSyncRow> T generateBaseSyncRow(T row) {
    row.setRowId(syncUuidGenerator());
    row.setLocale(LOCALE);
    row.setSavepointType(SAVEPOINT_TYPE);
    row.setSavepointTimestamp(rfc1123Timestamp());
    row.setSavepointCreator(SAVEPOINT_CREATOR);
    row.setCreateUser(CREATE_USER);
    row.setLastUpdateUser(LAST_UPDATE_USER);
    row.setDeleted(false);
    row.setDataETagAtModification(syncUuidGenerator());
    row.setRowETag(syncUuidGenerator());

    row.setRowFilterScope(ROW_FILTER_SCOPE);

    return row;
  }

  public static <T extends BaseSyncRow> T generateBaseSyncRow(Class<T> clazz) {
    try {
      return generateBaseSyncRow(clazz.newInstance());
    } catch (InstantiationException | IllegalAccessException e) {
      e.printStackTrace();
    }

    return null;
  }

  // TODO: move this
  public static String syncUuidGenerator() {
    return UUID_PREFIX + UUID.randomUUID().toString();
  }

  public static String rfc1123Timestamp() {
    return ZonedDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME);
  }

  public static String randomString(int length, Random rand) {
    return new String(rand.ints(length, 97, 122).toArray(), 0, length);
  }

  public static String randomIntString(int length, Random rand) {
    return rand
        .ints(length, 0, 9)
        .mapToObj(String::valueOf)
        .collect(Collectors.joining());
  }

  public static String randomDoubleString(int length, Random rand) {
    String intStr = randomIntString(length - 1, rand);

    // subtract 1 from length to avoid putting the decimal pt at the end
    int decimalIdx = rand.nextInt(length - 1);
    return intStr.substring(0, decimalIdx) + "." + intStr.substring(decimalIdx);
  }
}
