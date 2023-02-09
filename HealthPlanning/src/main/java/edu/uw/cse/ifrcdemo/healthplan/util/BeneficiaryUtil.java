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

package edu.uw.cse.ifrcdemo.healthplan.util;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.BeneficiaryUtilBase;
import edu.uw.cse.ifrcdemo.sharedlib.consts.csv.MobileDbConsts;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvBeneficiaryEntity;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvIndividual;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.UntypedSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.util.CsvMapperUtil;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BeneficiaryUtil extends BeneficiaryUtilBase {
  public static UntypedSyncRow getAllDataOnMember(CsvIndividual member,
                                                  CsvRepository csvRepository) {
    UntypedSyncRow mergedRow = new UntypedSyncRow();

    // start with all columns in individual's base table
    addRowsFromTable(
        MobileDbConsts.TableNames.INDIVIDUAL_TABLE_NAME, member.getRowId(), mergedRow, csvRepository);

    // merge in individual's custom table columns
    mergeCustomTableRow(member, csvRepository, mergedRow);

    String beneficiaryEntityId = member.getBeneficiaryEntityRowId();
    // merge in corresponding beneficiary entity's base table columns
    addRowsFromTable(
        MobileDbConsts.TableNames.BENEFICIARY_ENTITY_TABLE_NAME, beneficiaryEntityId, mergedRow, csvRepository);

    // merge in corresponding beneficiary entity's custom table columns
    CsvBeneficiaryEntity beneficiaryEntity = csvRepository
        .readIndexedTypedCsv(CsvBeneficiaryEntity.class)
        .orElseThrow(IllegalStateException::new)
        .get(beneficiaryEntityId);
    mergeCustomTableRow(beneficiaryEntity, csvRepository, mergedRow);

    // commented out because visits aren't in health
    // iterate through the visit entries and find the relevant ones
    // Visits are added twice
    //  1. Columns prefixed by table name
    //  2. Columns prefixed by visit program id
//    List<CsvVisit> relevantVisits = csvRepository
//        .readTypedCsv(CsvVisit.class)
//        .orElseThrow(IllegalStateException::new)
//        .stream()
//        .filter(v -> member.getRowId().equals(v.getMemberId()))
//        .collect(Collectors.toList());

//    relevantVisits.forEach(v -> mergeCustomTableRow(v, csvRepository, mergedRow));
//
//    Map<String, List<CsvVisit>> visitsByVisitProgram = relevantVisits
//        .stream()
//        .collect(Collectors.groupingBy(CsvVisit::getVisitProgramId));

    mergedRow.setRowId(member.getRowId());
    return mergedRow;
  }

  public static void writeDataForMemberAsCsv(Collection<String> memberIds,
                                             Path csvPath,
                                             CsvRepository csvRepository) throws IOException {
    Map<String, CsvIndividual> indexedMembers = csvRepository
            .readIndexedTypedCsv(CsvIndividual.class)
            .orElseThrow(IllegalStateException::new);

    Stream<CsvIndividual> filteredMembers = memberIds
            .stream()
            .map(indexedMembers::get);

    writeMemberDataCsv(filteredMembers, csvPath, csvRepository);
  }

  public static void writeDataForAllMemberAsCsv(Path csvPath, CsvRepository csvRepository) throws IOException {
    Stream<CsvIndividual> allMembers = csvRepository
            .readTypedCsv(CsvIndividual.class)
            .orElseThrow(IllegalStateException::new)
            .stream();

    writeMemberDataCsv(allMembers, csvPath, csvRepository);
  }

  private static void writeMemberDataCsv(Stream<CsvIndividual> memberBaseTable,
                                         Path csvPath,
                                         CsvRepository csvRepository) throws IOException {

    List<UntypedSyncRow> memberData = memberBaseTable
            .map(member -> BeneficiaryUtil.getAllDataOnMember(member, csvRepository))
            .collect(Collectors.toList());

    Set<String> columnSet = memberData
            .stream()
            .map(UntypedSyncRow::getColumns)
            .map(Map::keySet)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());

    CsvMapper csvMapper = CsvMapperUtil.getCsvMapper(false);
    ObjectWriter csvWriter = CsvMapperUtil.getWriter(csvMapper, columnSet, false);

    try (BufferedWriter writer = Files.newBufferedWriter(csvPath);
         SequenceWriter sw = csvWriter.writeValues(writer)) {
      sw.writeAll(memberData);
    }
  }
}
