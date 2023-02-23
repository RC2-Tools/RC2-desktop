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

package edu.uw.cse.ifrcdemo.distplan.ui.demographics;

import edu.uw.cse.ifrcdemo.distplan.entity.Entitlement;
import edu.uw.cse.ifrcdemo.distplan.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.sharedlib.model.config.AuxiliaryProperty;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.Gender;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.RegistrationMode;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.BaseSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvBeneficiaryEntity;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvIndividual;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.UntypedSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.HasCustomTable;
import edu.uw.cse.ifrcdemo.sharedlib.util.FileUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.Rc2SpecificColumnsUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.StringUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class TargetDemoDistribution {
  public static final AgeRange AGE_RANGE_NA = new AgeRange(-1, -1);

  private static final List<AgeRange> AGE_RANGES;

  static {
    List<AgeRange> ageRanges = Arrays.asList(
        new AgeRange(0, 4),
        new AgeRange(5, 17),
        new AgeRange(18, 49),
        new AgeRange(50, Integer.MAX_VALUE),
        AGE_RANGE_NA
    );

    AGE_RANGES = Collections.unmodifiableList(ageRanges);
  }

  private final CsvRepository csvRepository;
  private final AuxiliaryProperty auxiliaryProperty;

  public TargetDemoDistribution(CsvRepository csvRepository,
                                AuxiliaryProperty auxiliaryProperty) {
    this.csvRepository = csvRepository;
    this.auxiliaryProperty = auxiliaryProperty;
  }

  public Map<Gender, Map<AgeRange, Integer>> calculateTableFromEnt(
      Collection<Entitlement> entitlements) throws InsufficientDemographicsDataException {
    return calculateTable(entitlementsToId(entitlements).stream());
  }

  public Map<Gender, Map<AgeRange, Integer>> calculateTableFromMember(Stream<String> memberRowId)
      throws InsufficientDemographicsDataException {
    if (auxiliaryProperty.getRegistrationMode() == RegistrationMode.HOUSEHOLD) {
      return calculateTable(memberRowId);
    }

    Map<String, CsvIndividual> memberTable = csvRepository
        .readIndexedTypedCsv(CsvIndividual.class)
        .orElseThrow(IllegalStateException::new);

    memberRowId = memberRowId
        .map(memberTable::get)
        .map(CsvIndividual::getBeneficiaryEntityRowId);

    return calculateTable(memberRowId);
  }

  public DemographicsModel makeModel(Map<Gender, Map<AgeRange, Integer>> fullTable) {
    DemographicsModel formModel = new DemographicsModel();

    formModel.setGenderAgeDistribution(fullTable);
    formModel.setAgeMarginalDistribution(calculateAgeMarginal(fullTable));
    formModel.setGenderMarginalDistribution(calculateGenderMarginal(fullTable));

    int total = 0;
    for (Integer value : formModel.getGenderMarginalDistribution().values()) {
      total += value;
    }
    formModel.setTotal(total);

    return formModel;
  }

  public DemographicsModel makeModelFromEnt(List<Entitlement> entitlements)
      throws InsufficientDemographicsDataException {
    return makeModel(calculateTableFromEnt(entitlements));
  }

  public DemographicsModel makeModelFromMember(Stream<String> memberRowId)
      throws InsufficientDemographicsDataException {
    return makeModel(calculateTableFromMember(memberRowId));
  }

  private Map<Gender, Map<AgeRange, Integer>> calculateTable(Stream<String> beneficiaryRowId)
      throws InsufficientDemographicsDataException {
    RegistrationMode idType = auxiliaryProperty.getRegistrationMode();

    if (idType == RegistrationMode.INDIVIDUAL) {
      return calculateTableInternal(retrieveCustomTableRows(beneficiaryRowId, CsvBeneficiaryEntity.class));
    } else {
      return calculateTableInternal(retrieveCustomTableRows(beneficiaryRowId, CsvIndividual.class));
    }
  }

  private Map<Gender, Map<AgeRange, Integer>> calculateTableInternal(
      List<Map<String, String>> customRows) throws InsufficientDemographicsDataException {
    LocalDate ageRefDate = LocalDate.now(ZoneOffset.UTC);

    Map<Gender, Map<AgeRange, Integer>> distMap = makeGenderMap();

    for (Map<String, String> customTableData : customRows) {

      String genderStr = Rc2SpecificColumnsUtil.getGender(customTableData);
      String dobStr = Rc2SpecificColumnsUtil.getDateOfBirth(customTableData);

      if (genderStr == null || dobStr == null) {
        throw new InsufficientDemographicsDataException("Gender or DOB missing from custom "
            + "beneficiary table!  Could not create demographics information!");
      }

      Gender gender = Gender.RC2GenderValueOf(genderStr);

      AgeRange ageRange;
      if (!dobStr.equals("")) {
        LocalDate dob = DemographicsUtil.extractDob(dobStr);
        ageRange = DemographicsUtil.findAgeRange(dob, ageRefDate, AGE_RANGES);
      } else {
        ageRange = AGE_RANGE_NA;
      }

      Map<AgeRange, Integer> genderDist = distMap.computeIfAbsent(gender, __ -> makeAgeMap());
      genderDist.compute(ageRange, (__, currCount) -> currCount != null ? currCount + 1 : 1);
    }

    // fill in the gaps with 0
    for (Gender gender : Gender.values()) {
      Map<AgeRange, Integer> genderDist = distMap.computeIfAbsent(gender, __ -> makeAgeMap());
      AGE_RANGES.forEach(ageRange -> genderDist.putIfAbsent(ageRange, 0));
    }

    return distMap;
  }

  private Set<String> entitlementsToId(Collection<Entitlement> entitlements) {
    // the relevant information is stored in the member's custom table when in HOUSEHOLD mode
    // otherwise the information is stored in the beneficiary entity's custom table
    boolean dataInMemberTable = auxiliaryProperty.getRegistrationMode() == RegistrationMode.HOUSEHOLD;

    return entitlements
        .stream()
        .map(dataInMemberTable ? Entitlement::getIndividualId : Entitlement::getBeneficiaryEntityId)
        .filter(StringUtil::isNotNullAndNotEmpty)
        .collect(Collectors.toSet());
  }

  private <T extends BaseSyncRow & HasCustomTable> List<Map<String, String>> retrieveCustomTableRows(
      Stream<String> beneficiaryRowId, Class<T> tableClass) {
    Map<String, T> baseTable = csvRepository
        .readIndexedTypedCsv(tableClass)
        .orElseThrow(IllegalStateException::new);

    return beneficiaryRowId
        .map(baseTable::get)
        .filter(Objects::nonNull)
        .map(this::getCustomTableRow)
        .collect(Collectors.toList());
  }

  private Map<String, String> getCustomTableRow(HasCustomTable baseTableRow) {
    return csvRepository
        .readIndexedUntypedCsv(FileUtil.getFileName(baseTableRow.getCustomTableFormId()))
        .map(row -> row.get(baseTableRow.getCustomTableRowId()))
        .map(UntypedSyncRow::getColumns)
        .orElseGet(Collections::emptyMap);
  }

  private Map<Gender, Integer> calculateGenderMarginal(Map<Gender, Map<AgeRange, Integer>> fullTable) {
    return fullTable
        .entrySet()
        .stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            map -> map.getValue().values().stream().reduce(0, Integer::sum),
            Integer::sum,
            TargetDemoDistribution::makeGenderMap
        ));
  }

  private Map<AgeRange, Integer> calculateAgeMarginal(Map<Gender, Map<AgeRange, Integer>> fullTable) {
    return fullTable
        .entrySet()
        .stream()
        .flatMap(map -> map.getValue().entrySet().stream())
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            Integer::sum,
            TargetDemoDistribution::makeAgeMap
        ));
  }

  private static <T> Map<Gender, T> makeGenderMap() {
    Map<Gender, T> map = new LinkedHashMap<>();

    map.put(Gender.FEMALE, null);
    map.put(Gender.MALE, null);

    return map;
  }

  private static <T> Map<AgeRange, T> makeAgeMap() {
    Map<AgeRange, T> map = new LinkedHashMap<>();

    for (AgeRange ageRange : AGE_RANGES) {
      map.put(ageRange, null);
    }

    return map;
  }
}
