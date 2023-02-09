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

package edu.uw.cse.ifrcdemo.planningsharedlib.ui.beneficiary;

import edu.uw.cse.ifrcdemo.planningsharedlib.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.ControllerUtil;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.FxDialogUtil;
import edu.uw.cse.ifrcdemo.sharedlib.model.config.AuxiliaryProperty;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.Gender;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.IndividualStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.RegistrationMode;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvBeneficiaryEntity;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvIndividual;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.UntypedSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.util.FileUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.StringUtil;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbsBeneficiaryController {
    private static final String BENEFICIARY_MENU_OPTIONS = "beneficiary/beneficiaryMenu";
    private static final String BENEFICIARY_STATUS_CHANGE = "beneficiary/changeStatus";
    private static final String BENEFICIARY_STATUS_CHANGE_SUMMARY = "beneficiary/changeStatusSummary";

    public static final String REDIRECT_CHANGE_STATUS_SUMMARY = "redirect:changeStatusSummary";

    protected final Logger logger;

    private final List<BeneficiaryFormData> beneficiaries;
    private final Map<String, IndividualStatus> statusMap;
    private UUID version;

    protected abstract AuxiliaryProperty getAuxiliaryProperty();
    protected abstract CsvRepository getCsvRepository();
    protected abstract UUID getDataReposVersion();

    public AbsBeneficiaryController(Logger logger) {
        this.logger = logger;
        this.beneficiaries = new ArrayList<>();
        this.statusMap = new HashMap<>();
        this.version = UUID.randomUUID();
    }

    @ModelAttribute("beneficiaryFormModel")
    public BeneficiaryFormModel newBeneficiaryFormModel() {
      BeneficiaryFormModel beneficiaryFormModel = new BeneficiaryFormModel();
      beneficiaryFormModel.setCurrPage(0);
      beneficiaryFormModel.setElementsPerPage(2);
      beneficiaryFormModel.setStartElementIndex(0);
      beneficiaryFormModel.setEndElementIndex(0);
      return beneficiaryFormModel;
    }

    @GetMapping("")
    public String distributionMenu() {
        return BENEFICIARY_MENU_OPTIONS;
    }

    @GetMapping("changeStatus")
    public ModelAndView changeBeneficiaryStatus(
            @ModelAttribute("beneficiaryFormModel") BeneficiaryFormModel beneficiaryFormModel,
            @RequestParam(value="currPage", required=false) String cPage,
            @RequestParam(value="elementsPerPage", required = false) String epp,
            @RequestParam(value="searchName", required = false) String sn) {

        UUID dataRepoVersion = getDataReposVersion();
        if(dataRepoVersion == null) {
           return new ModelAndView(BENEFICIARY_STATUS_CHANGE);
        }
        if (!version.equals(dataRepoVersion)) {
            readInBeneficiariesFromCsv();
            version = getDataReposVersion();
//      readInStaticBeneficiaries();
        }

        // Always set the beneficiaries to the original set as we may
        // need to search for a name
        beneficiaryFormModel.setBeneficiaries(beneficiaries);
        beneficiaryFormModel.setTotalBeneficiarySetSize(beneficiaries.size());

        // Change the set of beneficiaries based on search criteria if any given
        beneficiaryFormModel.searchForBeneficiaryByNameOrRcId();

        return new ModelAndView(BENEFICIARY_STATUS_CHANGE);
    }

    private void readInStaticBeneficiaries() {
        beneficiaries.add(new BeneficiaryFormData("1", "Barack", "Obama",
                (new GregorianCalendar(1961, Calendar.AUGUST, 4).getTime()), true));
        beneficiaries.add(new BeneficiaryFormData("2","George", "Bush",
                (new GregorianCalendar(1946, Calendar.JULY, 6)).getTime(), true));
        beneficiaries.add(new BeneficiaryFormData("3","Bill", "Clinton",
                (new GregorianCalendar(1946, Calendar.AUGUST, 19)).getTime(), true));
        beneficiaries.add(new BeneficiaryFormData("4","Ronald", "Reagan",
                (new GregorianCalendar(1911, Calendar.FEBRUARY, 6)).getTime(), true));
    }


    private void readInBeneficiariesFromCsv() {
        // TODO: Group members by household in the list
        // Read in the member base table
        CsvRepository csvRepository = getCsvRepository();
        Map<String, CsvIndividual> indMap = csvRepository.readIndexedTypedCsv(CsvIndividual.class)
                .orElse(new HashMap<>());

        String customMemberTable = null;
        String customMemberRowId = null;
        String rcId = null;

        // Loop through the members
        List<BeneficiaryFormData> beneList = new ArrayList<>();
        for (Map.Entry<String, CsvIndividual> entry : indMap.entrySet()) {
            CsvIndividual indRow = entry.getValue();

            // This is dependent upon the auxiliary properties file!!
            RegistrationMode registrationMode = getAuxiliaryProperty().getRegistrationMode();

            // Get the custom_member_form_id
            if (Objects.equals(registrationMode, RegistrationMode.INDIVIDUAL)) {
                // Need to pull beneficiary entity for individual mode
                Map<String, CsvBeneficiaryEntity> csvBeneMap = csvRepository
                        .readIndexedTypedCsv(CsvBeneficiaryEntity.class).orElse(new HashMap<>());
                if (csvBeneMap.containsKey(indRow.getBeneficiaryEntityRowId())) {
                    customMemberTable =
                            csvBeneMap.get(indRow.getBeneficiaryEntityRowId()).getCustomBeneficiaryEntityFormId();
                    customMemberRowId =
                            csvBeneMap.get(indRow.getBeneficiaryEntityRowId()).getCustomBeneficiaryEntityRowId();
                    rcId = csvBeneMap.get(indRow.getBeneficiaryEntityRowId()).getBeneficiaryEntityId();
                }
            } else {
                customMemberTable = indRow.getCustomMemberFormId();
                customMemberRowId = indRow.getCustomMemberRowId();
                rcId = indRow.getMemberId();
            }

            // Read in the csv for the custom_member_form_id
            // TODO: This should be cached if possible
            Map<String, UntypedSyncRow> customMemberMap = null;
            if (customMemberTable != null) {
                customMemberMap = csvRepository.readIndexedUntypedCsv(FileUtil.getFileName(customMemberTable))
                        .orElse(new HashMap<>());
            }

            BeneficiaryFormData bfd = new BeneficiaryFormData();
            // Set the member row id
            bfd.setRowId(entry.getKey());
            bfd.setRcId(rcId);

            // Set the member status
            IndividualStatus beneStatus = entry.getValue().getStatus();
            bfd.setStatus(Objects.equals(beneStatus, IndividualStatus.ENABLED));

            // Set the display values for the beneficiaryFormData if
            // they are available in the custom_member_form_id
            // This will mean that these values will need to be named this way for all
            // custom_member_form_id's
            if (customMemberRowId != null && customMemberMap != null) {
                UntypedSyncRow customMemberRow = customMemberMap.get(customMemberRowId);

                Map<String, String> customMemberRowCols = customMemberRow.getColumns();
                bfd.setRowId(entry.getKey());

                for (String key : Arrays.asList("first_name", "firstName")) {
                    if (customMemberRowCols.containsKey(key)) {
                        bfd.setFirstName(customMemberRowCols.get(key));
                    }
                }

                for (String key : Arrays.asList("last_name", "lastName")) {
                    if (customMemberRowCols.containsKey(key)) {
                        bfd.setLastName(customMemberRowCols.get(key));
                    }
                }

                for (String key : Arrays.asList("birth_date", "birthdate", "birthDate")) {
                    String dobStr = customMemberRowCols.get(key);

                    // TODO: Use something better than date!
                    if (StringUtil.isNotNullAndNotEmpty(dobStr)) {
                        Date bDate = null;
                        try {
                            bDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS").parse(dobStr);
                        } catch (ParseException e) {
                            logger.debug("Unable to parse date {}", e.getMessage());
                        }
                        bfd.setDateOfBirth(bDate);
                        break;
                    }
                }

                if (customMemberRowCols.containsKey("gender")) {
                    Gender gender = Gender.NA;
                    try {
                        gender = Gender.RC2GenderValueOf(customMemberRowCols.get("gender"));
                    } catch (IllegalArgumentException e) {
                        logger.debug("Unable to parse {}", e.getMessage());
                    }
                    bfd.setGender(gender);
                }
            }
            beneList.add(bfd);
        }

        beneficiaries.clear();
        beneficiaries.addAll(beneList);
    }

    @PostMapping("changeStatus")
    public String changeBeneficiaryStatusPost(
            @ModelAttribute("beneficiaryFormModel") BeneficiaryFormModel beneficiaryFormModel) {

        return REDIRECT_CHANGE_STATUS_SUMMARY;
    }

    @GetMapping("changeStatusSummary")
    public ModelAndView changeBeneficiaryStatusSummary(
            @ModelAttribute("beneficiaryFormModel") BeneficiaryFormModel beneficiaryFormModel) {

        return new ModelAndView(BENEFICIARY_STATUS_CHANGE_SUMMARY, "beneficiaryFormModel",
                beneficiaryFormModel);
    }


    protected void changeBeneficiaryStatus(@ModelAttribute("beneficiaryFormModel") BeneficiaryFormModel beneficiaryFormModel) {
      CsvRepository csvRepository = getCsvRepository();
      Map<String, CsvIndividual> indMap = csvRepository.readIndexedTypedCsv(CsvIndividual.class)
          .orElse(new HashMap<>());

      for (BeneficiaryFormData bfd : beneficiaryFormModel.getBeneficiaries()) {
        if (indMap.containsKey(bfd.getRowId())) {
          IndividualStatus bfdStatus = bfd.getStatus() ? IndividualStatus.ENABLED :
              IndividualStatus.DISABLED;
          if (!Objects.equals(bfdStatus, indMap.get(bfd.getRowId()).getStatus())) {
            statusMap.put(bfd.getRowId(), bfdStatus);
            // Change the CsvIndividual to use the new status so that
            // entitlements will not be generated when individual is disabled
            indMap.get(bfd.getRowId()).setStatus(bfdStatus);
          }
        }
      }
    }

    @GetMapping("downloadData")
    public String downloadBeneficiaryData() {
      CsvRepository csvRepository = getCsvRepository();

      Map<String, CsvIndividual> individualTyped = csvRepository
          .readIndexedTypedCsv(CsvIndividual.class)
          .orElseThrow(IllegalStateException::new);

      Stream<String> ids = individualTyped.keySet().stream();

      // TODO: consolidate this into a util function
      Map<String, UntypedSyncRow> individualUntyped = csvRepository
          .readIndexedUntypedCsv(FileUtil.getFileName(CsvIndividual.class))
          .orElseThrow(IllegalStateException::new);

      Map<String, UntypedSyncRow> beneficiaryUnitUntyped = csvRepository
          .readIndexedUntypedCsv(FileUtil.getFileName(CsvBeneficiaryEntity.class))
          .orElseThrow(IllegalStateException::new);

      Map<String, CsvBeneficiaryEntity> beneficiaryUnitTyped = csvRepository
          .readIndexedTypedCsv(CsvBeneficiaryEntity.class)
          .orElseThrow(IllegalStateException::new);

      List<UntypedSyncRow> members = ids
          .map(individualTyped::get)
          .map(member -> {
            UntypedSyncRow mergedRow = new UntypedSyncRow();

            // start with all columns in individual's base table
            mergedRow.getColumns().putAll(individualUntyped.get(member.getRowId()).getColumns());
            // merge in individual's custom table columns
            ControllerUtil.mergeCustomTableRow(member, csvRepository, mergedRow);

            String beneficiaryEntityId = member.getBeneficiaryEntityRowId();
            // merge in corresponding beneficiary entity's base table columns
            mergedRow
                .getColumns()
                .putAll(beneficiaryUnitUntyped.get(beneficiaryEntityId).getColumns());

            // finally merge in corresponding beneficiary entity's custom table columns
            ControllerUtil.mergeCustomTableRow(beneficiaryUnitTyped.get(beneficiaryEntityId),
                csvRepository,
                mergedRow);

            mergedRow.setRowId(member.getRowId());
            return mergedRow;
          })
          .collect(Collectors.toList());

      if (members.size() <= 0) {
        FxDialogUtil.showWarningDialog(TranslationUtil.getTranslations().getString(
            TranslationConsts.NO_DATA_TO_GENERATE_CSV));
        return BENEFICIARY_MENU_OPTIONS;
      }

      ControllerUtil.writeOutCustomCsv(members, logger);
      return BENEFICIARY_MENU_OPTIONS;
    }
}
