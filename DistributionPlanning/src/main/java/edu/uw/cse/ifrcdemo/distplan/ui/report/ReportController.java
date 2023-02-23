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

package edu.uw.cse.ifrcdemo.distplan.ui.report;

import edu.uw.cse.ifrcdemo.distplan.data.DataInstance;
import edu.uw.cse.ifrcdemo.distplan.data.DataRepos;
import edu.uw.cse.ifrcdemo.distplan.entity.Authorization;
import edu.uw.cse.ifrcdemo.distplan.entity.Distribution;
import edu.uw.cse.ifrcdemo.distplan.entity.Entitlement;
import edu.uw.cse.ifrcdemo.distplan.entity.ItemPack;
import edu.uw.cse.ifrcdemo.distplan.entity.Region;
import edu.uw.cse.ifrcdemo.distplan.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.distplan.model.distribution.DistributionRepository;
import edu.uw.cse.ifrcdemo.distplan.model.entitlement.EntitlementRepository;
import edu.uw.cse.ifrcdemo.distplan.model.form.XlsxForm;
import edu.uw.cse.ifrcdemo.distplan.ui.demographics.AgeRange;
import edu.uw.cse.ifrcdemo.distplan.ui.demographics.DemographicsModel;
import edu.uw.cse.ifrcdemo.distplan.ui.demographics.InsufficientDemographicsDataException;
import edu.uw.cse.ifrcdemo.distplan.ui.demographics.TargetDemoDistribution;
import edu.uw.cse.ifrcdemo.distplan.util.ControllerUtil;
import edu.uw.cse.ifrcdemo.distplan.util.FxDialogUtil;
import edu.uw.cse.ifrcdemo.sharedlib.model.config.AuxiliaryProperty;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationType;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.Gender;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvAuthorization;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvBeneficiaryEntity;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvDelivery;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvDistribution;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvIndividual;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.UntypedSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.SyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.util.FileUtil;
import edu.uw.cse.ifrcdemo.translations.LogStr;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static edu.uw.cse.ifrcdemo.distplan.ui.util.ControllerPdfUtil.writeControllerPdf;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

@Controller
@RequestMapping("/report")
@SessionAttributes(types = { ReportDistributionPlanFormModel.class })
public class ReportController {

  private static final String REPORT_MENU_OPTIONS = "report/reportMenu";
  private static final String REPORT_DIST_PLAN = "report/distributionPlan";
  private static final String REPORT_DIST_LIST = "report/distributionList";
  private static final String REPORT_DIST_ITEM_LIST = "report/distributionItemList";
  private static final String REPORT_SUMMARY = "report/summary";
  private static final String REPORT_DELIVERIES = "report/deliveriesMenu";

  private static Logger logger;

  private final TemplateEngine templateEngine;
  private final TargetDemoDistribution targetDemoDistribution;

  public ReportController(Logger logger,
                          TemplateEngine templateEngine,
                          TargetDemoDistribution targetDemoDistribution) {
    ReportController.logger = logger;

    this.templateEngine = templateEngine;
    this.targetDemoDistribution = targetDemoDistribution;
  }

  @ModelAttribute("reportDistributionPlanFormModel")
  public ReportDistributionPlanFormModel newReportDistributionPlanFormModel() {
    ReportDistributionPlanFormModel reportDistributionPlanFormModel = new ReportDistributionPlanFormModel();
    return reportDistributionPlanFormModel;
  }

  @GetMapping("")
  public ModelAndView reportMenu(@ModelAttribute("reportDistributionPlanFormModel")
                                       ReportDistributionPlanFormModel reportDistributionPlanFormModel,
                                 @ModelAttribute("isNoRegMode") boolean isNoRegMode) {
    DataRepos dataRepos = DataInstance.getDataRepos();

    List<ReportDistribution> reportDistributionList;
    if (!isNoRegMode) {
      reportDistributionList = dataRepos
          .getDistributionRepository()
          .getDistributionListDto(false)
          .stream()
          .map(distListDto -> new ReportDistribution(distListDto.getName(), distListDto.getRowId()))
          .collect(Collectors.toList());
    } else {
      Map<String, CsvDistribution> indexedCsvDist = dataRepos
          .getCsvRepository()
          .readIndexedTypedCsv(CsvDistribution.class)
          .orElseThrow(IllegalStateException::new);

      reportDistributionList = dataRepos
          .getCsvRepository()
          .readTypedCsv(CsvAuthorization.class)
          .orElseThrow(IllegalStateException::new)
          .stream()
          .filter(auth -> AuthorizationType.NO_REGISTRATION == auth.getType())
          .filter(auth -> indexedCsvDist.containsKey(auth.getDistributionId()))
          .map(auth -> new ReportDistribution(auth.getDistributionName(), auth.getDistributionId()))
          .collect(Collectors.toList());
    }
    reportDistributionPlanFormModel.setReportDistributions(reportDistributionList);

    return new ModelAndView(REPORT_MENU_OPTIONS);
  }

  @GetMapping("distributionPlan")
  public ModelAndView generateDistPlan(@ModelAttribute("reportDistributionPlanFormModel")
      ReportDistributionPlanFormModel reportDistributionPlanFormModel, HttpServletRequest request,
      HttpServletResponse response, SessionStatus status) {

    generateDistPlanCommon(reportDistributionPlanFormModel);

    status.setComplete();

    return new ModelAndView(REPORT_DIST_PLAN);
  }

  private void generateDistPlanCommon(@ModelAttribute("reportDistributionPlanFormModel")
      ReportDistributionPlanFormModel reportDistributionPlanFormModel) {
    DistributionRepository distributionRepository = DataInstance.getDataRepos()
        .getDistributionRepository();
    AuxiliaryProperty auxiliaryProperty = DataInstance.getDataRepos().getAuxiliaryProperty();
    EntitlementRepository entitlementRepository = DataInstance.getDataRepos()
        .getEntitlementRepository();

    // Get the appropriate distribution
    Distribution selectedDistribution = distributionRepository
        .getDistributionByRowId(reportDistributionPlanFormModel.getDistributionRowId());

    // Set values based on auxiliaryProperties
    reportDistributionPlanFormModel.setDistributionName(selectedDistribution.getName());
    reportDistributionPlanFormModel.setWorkflowMode(auxiliaryProperty.getWorkflowMode().toString());
    reportDistributionPlanFormModel
        .setRegistrationMode(auxiliaryProperty.getRegistrationMode().toString());
    reportDistributionPlanFormModel
        .setRegistrationForm(auxiliaryProperty.getBeneficiaryEntityCustomFormId());

    // Set the region
    Region distributionRegion = selectedDistribution.getLocation();
    if (distributionRegion != null) {
      reportDistributionPlanFormModel.setRegion(distributionRegion.getName());
    }

    // Get the relevant authorizations for a distribution
    // Find the relevant entitlements, delivery forms, and items
    List<Authorization> selectedAuthorizations = selectedDistribution.getAuthorizations();
    List<Entitlement> entitlementList = new ArrayList<>();
    List<ReportItemSummary> reportItemSummaryList = new ArrayList<>();
    StringBuilder deliveryForms = new StringBuilder();
    int numOfDeliveryForms = 0;
    for (Authorization selectedAuthorization : selectedAuthorizations) {
      XlsxForm customDeliveryForm = selectedAuthorization.getCustomDeliveryForm();
      if (customDeliveryForm != null) {
        if (numOfDeliveryForms > 0) {
          deliveryForms.append(", ");
        }
        deliveryForms.append(customDeliveryForm.getFormId());
        numOfDeliveryForms++;

      }
      ItemPack itemPack = selectedAuthorization.getItemPack();
      setReportItemSummaryFromItemPack(entitlementRepository, entitlementList,
          reportItemSummaryList, selectedAuthorization, itemPack);
    }
    reportDistributionPlanFormModel.setReportItemSummaryList(reportItemSummaryList);

    if (numOfDeliveryForms > 0) {
      reportDistributionPlanFormModel.setDistributionForm(deliveryForms.toString());
    }

    // Only show target distribution if workflow mode is AuthorizationType.REQUIRED_REGISTRATION
    AuthorizationType authorizationType = auxiliaryProperty.getWorkflowMode();
    if (Objects.equals(authorizationType, AuthorizationType.REQUIRED_REGISTRATION)) {
      // Add the necessary age ranges
      Map<AgeRange, Integer> fDist = new LinkedHashMap<>();
      Map<AgeRange, Integer> mDist = new LinkedHashMap<>();
      Map<AgeRange, Integer> totDist = new LinkedHashMap<>();

      calculateTargetDistributions(entitlementList, fDist, mDist, totDist);

      // TODO: Make this more efficient!
      Map<String, Integer> totalDist = new LinkedHashMap<>();
      Map<String, Integer> femaleDist = new LinkedHashMap<>();
      Map<String, Integer> maleDist = new LinkedHashMap<>();
      convertDistMapForDisplay(fDist, femaleDist);
      convertDistMapForDisplay(mDist, maleDist);
      convertDistMapForDisplay(totDist, totalDist);

      reportDistributionPlanFormModel.setFemaleAgeDistribution(femaleDist);
      reportDistributionPlanFormModel.setMaleAgeDistribution(maleDist);
      reportDistributionPlanFormModel.setTotalAgeDistribution(totalDist);
    }
  }

  @PostMapping("distributionPlan")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<String> generateDistPlanPost(@ModelAttribute(
      "reportDistributionPlanFormModel")
      ReportDistributionPlanFormModel reportDistributionPlanFormModel, HttpServletRequest request,
      HttpServletResponse response, SessionStatus status) {

    WebContext webContext = new WebContext(request, response, request.getServletContext());

    generateDistPlanCommon(reportDistributionPlanFormModel);

    // Write out the report
    webContext.setVariable("reportDistributionPlanFormModel", reportDistributionPlanFormModel);
    writeReport(webContext, "distribution_plan_report", "report/distributionPlanReport.html");

    return new ResponseEntity<>("Success!", HttpStatus.OK);
  }

  private void calculateTargetDistributions(List<Entitlement> entitlementList,
                                            Map<AgeRange, Integer> fDist,
                                            Map<AgeRange, Integer> mDist,
                                            Map<AgeRange, Integer> totDist) {
    DemographicsModel demographicsModel = null;
    try {
      demographicsModel = targetDemoDistribution.makeModelFromEnt(entitlementList);
    } catch (InsufficientDemographicsDataException idde) {
      FxDialogUtil.showWarningDialog(TranslationUtil.getTranslations().getString(
          TranslationConsts.INSUFFICIENT_BENEFICIARY_DATA_TO_CREATE_DEMOGRAPHICS));
      logger.error(LogStr.LOG_INSUFFICIENT_BENEFICIARY_DATA_TO_CREATE_DEMOGRAPHICS + '\n' +
          ExceptionUtils.getStackTrace(idde));
    }

    if (demographicsModel != null) {
      fDist.putAll(demographicsModel.getGenderAgeDistribution().get(Gender.FEMALE));
      mDist.putAll(demographicsModel.getGenderAgeDistribution().get(Gender.MALE));
      totDist.putAll(demographicsModel.getAgeMarginalDistribution());
    }
  }

  private void convertDistMapForDisplay(Map<AgeRange, Integer> dist,
      Map<String, Integer> displayDist) {
    Integer totalVal = 0;
    Integer prevEnd = null;
    for (Map.Entry<AgeRange, Integer> entry : dist.entrySet()) {
      String ageBucket;
      if (entry.getKey().equals(TargetDemoDistribution.AGE_RANGE_NA)) {
        ageBucket = "N/A";
      } else if (Objects.equals(entry.getKey().getEnd(), Integer.MAX_VALUE)) {
        ageBucket = "Over " + prevEnd;
      } else {
        ageBucket = entry.getKey().getStart() + " to " + entry.getKey().getEnd();
      }

      displayDist.put(ageBucket, entry.getValue());
      totalVal += entry.getValue();
      prevEnd = entry.getKey().getEnd() + 1;
    }

    displayDist.put("Total", totalVal);
  }

  @GetMapping("distributionList")
  public ModelAndView generateDistList(@ModelAttribute("reportDistributionPlanFormModel")
      ReportDistributionPlanFormModel reportDistributionPlanFormModel, HttpServletRequest request,
      HttpServletResponse response, SessionStatus status) {

    generateDistListCommon(reportDistributionPlanFormModel);

    status.setComplete();

    return new ModelAndView(REPORT_DIST_LIST);
  }

  private void generateDistListCommon(@ModelAttribute("reportDistributionPlanFormModel")
      ReportDistributionPlanFormModel reportDistributionPlanFormModel) {
    DistributionRepository distributionRepository = DataInstance.getDataRepos()
        .getDistributionRepository();
    EntitlementRepository entitlementRepository = DataInstance.getDataRepos()
        .getEntitlementRepository();
    CsvRepository csvRepository = DataInstance.getDataRepos().getCsvRepository();
    AuxiliaryProperty auxiliaryProperty = DataInstance.getDataRepos().getAuxiliaryProperty();

    // Find the relevant distribution
    Distribution selectedDistribution = distributionRepository
        .getDistributionByRowId(reportDistributionPlanFormModel.getDistributionRowId());

    // Set the region
    Region distributionRegion = selectedDistribution.getLocation();
    if (distributionRegion != null) {
      reportDistributionPlanFormModel.setRegion(distributionRegion.getName());
    }

    List<Entitlement> entitlementList = new ArrayList<>();
    List<ReportItemSummary> reportItemSummaryList = new ArrayList<>();

    // Get the relevant authorization for a distribution
    List<Authorization> selectedAuthorizations = selectedDistribution.getAuthorizations();

    // Get the entitlements and items for the respective authorizations
    for (Authorization selectedAuthorization : selectedAuthorizations) {
      ItemPack itemPack = selectedAuthorization.getItemPack();
      setReportItemSummaryFromItemPack(entitlementRepository, entitlementList,
          reportItemSummaryList, selectedAuthorization, itemPack);
    }

    reportDistributionPlanFormModel.setReportItemSummaryList(reportItemSummaryList);

    // Count the number of entitlements per beneficiary entity
    Map<String, Map<Authorization, Long>> groupedEntsByRcId = entitlementList.stream().collect(
        groupingBy(Entitlement::getBeneficiaryEntityId,
            groupingBy(Entitlement::getAuthorization, counting())));

    Map<String, Long> beneToFamilySize = csvRepository.readTypedCsv(CsvIndividual.class)
        .orElseThrow(IllegalStateException::new).stream()
        .collect(groupingBy(CsvIndividual::getBeneficiaryEntityRowId, counting()));

    Map<String, CsvBeneficiaryEntity> benes = csvRepository
        .readIndexedTypedCsv(CsvBeneficiaryEntity.class).orElse(new HashMap<>());

    // Populate list of entitlements per RC id
    List<ReportEntityItems> reportEntityItemsList = new ArrayList<>();
    for (Map.Entry<String, Map<Authorization, Long>> entry : groupedEntsByRcId.entrySet()) {
      ReportEntityItems reportEntityItems = new ReportEntityItems();
      CsvBeneficiaryEntity bene = benes.get(entry.getKey());
      setRcIdAndFamilySize(beneToFamilySize, entry.getKey(), reportEntityItems, bene,
          auxiliaryProperty);

      Map<Authorization, Long> authMap = entry.getValue();
      Map<String, Integer> itemMap = new HashMap<>();
      for (Map.Entry<Authorization, Long> authEntry : authMap.entrySet()) {
        itemMap.put(authEntry.getKey().getItemPack().getName(), authEntry.getValue().intValue());
      }
      reportEntityItems.setItemMap(itemMap);
      reportEntityItemsList.add(reportEntityItems);
    }
    reportDistributionPlanFormModel.setReportEntityItemsList(reportEntityItemsList);
  }

  @PostMapping("distributionList")
  @ResponseStatus(value = HttpStatus.OK)
  public ResponseEntity<String> generateDistListReport(@ModelAttribute(
      "reportDistributionPlanFormModel")
      ReportDistributionPlanFormModel reportDistributionPlanFormModel, HttpServletRequest request,
      HttpServletResponse response) {

    WebContext webContext = new WebContext(request, response, request.getServletContext());
    generateDistListCommon(reportDistributionPlanFormModel);

    // Write out the report
    webContext.setVariable("reportDistributionPlanFormModel", reportDistributionPlanFormModel);
    writeReport(webContext, "distribution_list_report", "report/distributionListReport.html");

    return new ResponseEntity<>("Success!", HttpStatus.OK);
  }

  private void setRcIdAndFamilySize(Map<String, Long> beneToFamilySize,
      String beneKey, ReportEntityItems reportEntityItems,
      CsvBeneficiaryEntity bene, AuxiliaryProperty auxiliaryProperty) {
    if (!Objects.equals(auxiliaryProperty.getWorkflowMode(),
        AuthorizationType.REQUIRED_REGISTRATION))
      return;

    if (bene != null) {
      reportEntityItems.setRcId(bene.getBeneficiaryEntityId());
      Long size = beneToFamilySize.getOrDefault(beneKey, null);
      if (size != null) {
        reportEntityItems.setFamilySize(size.intValue());
      }
    }
  }

  private void writeReport(WebContext webContext, String reportName, String templateLocation) {
    writeControllerPdf(webContext, reportName, templateLocation, logger, templateEngine);
  }

  @GetMapping("distributionItemList")
  public ModelAndView generateDistItemList(@ModelAttribute("reportDistributionPlanFormModel")
      ReportDistributionPlanFormModel reportDistributionPlanFormModel, HttpServletRequest request,
      HttpServletResponse response, SessionStatus status) {

    generateDistItemListCommon(reportDistributionPlanFormModel);

    status.setComplete();

    return new ModelAndView(REPORT_DIST_ITEM_LIST);
  }

  private void generateDistItemListCommon(@ModelAttribute("reportDistributionPlanFormModel")
      ReportDistributionPlanFormModel reportDistributionPlanFormModel) {
    DistributionRepository distributionRepository = DataInstance.getDataRepos()
        .getDistributionRepository();
    EntitlementRepository entitlementRepository = DataInstance.getDataRepos()
        .getEntitlementRepository();
    CsvRepository csvRepository = DataInstance.getDataRepos().getCsvRepository();
    AuxiliaryProperty auxiliaryProperty = DataInstance.getDataRepos().getAuxiliaryProperty();

    // Find the relevant distribution
    Distribution selectedDistribution = distributionRepository
        .getDistributionByRowId(reportDistributionPlanFormModel.getDistributionRowId());

    // Set the region
    Region distributionRegion = selectedDistribution.getLocation();
    if (distributionRegion != null) {
      reportDistributionPlanFormModel.setRegion(distributionRegion.getName());
    }

    List<Entitlement> entitlementList = new ArrayList<>();
    List<ReportItemSummary> reportItemSummaryList = new ArrayList<>();

    // Get the relevant authorization for a distribution
    List<Authorization> selectedAuthorizations = selectedDistribution.getAuthorizations();

    // Get the entitlements and items for the respective authorizations
    for (Authorization selectedAuthorization : selectedAuthorizations) {
      ItemPack itemPack = selectedAuthorization.getItemPack();
      setReportItemSummaryFromItemPack(entitlementRepository, entitlementList,
          reportItemSummaryList, selectedAuthorization, itemPack);
    }

    reportDistributionPlanFormModel.setReportItemSummaryList(reportItemSummaryList);

    // Count the number of entitlements per beneficiary entity
    // TODO: Name things consistently or provide comments for mappings
    Map<String, Map<Authorization, Long>> groupedEnts =
        entitlementList.stream().collect(groupingBy(Entitlement::getBeneficiaryUnitRcId,
            groupingBy(Entitlement::getAuthorization, counting())));

    // Make a set of Authorization ids
    Set<String> selectedAuthorizationIds =
        selectedAuthorizations.stream()
            .map(Authorization::getRowId)
            .collect(Collectors.toSet());

    // Count the number of deliveries per beneficiary entity
    List<CsvDelivery> deliveryList = csvRepository.readTypedCsv(CsvDelivery.class)
        .orElse(new ArrayList<>())
        .stream()
        .filter(x -> selectedAuthorizationIds.contains(x.getAuthorizationId()))
        .collect(Collectors.toList());

    Map<String, Map<String, Long>> groupedDeliveries =
        deliveryList.stream().collect(groupingBy(CsvDelivery::getBeneficiaryEntityId,
        groupingBy(CsvDelivery::getAuthorizationId, counting())));

    Map<String, Long> beneToFamilySize = csvRepository.readTypedCsv(CsvIndividual.class)
        .orElseThrow(IllegalStateException::new)
        .stream()
        .collect(groupingBy(CsvIndividual::getBeneficiaryEntityRowId, counting()));

    Map<String, CsvBeneficiaryEntity> benes = csvRepository
        .readIndexedTypedCsv(CsvBeneficiaryEntity.class).orElse(new HashMap<>());

    // Populate list of entitlements per RC id
    List<ReportEntityItems> reportEntityItemsList = new ArrayList<>();
    for (Map.Entry<String, Map<Authorization, Long>> entry : groupedEnts.entrySet()) {
      ReportEntityItems reportEntityItems = new ReportEntityItems();
      List<Map.Entry<String, CsvBeneficiaryEntity>> beneRes = benes.entrySet().stream().filter(bene -> {
        return Objects.equals(bene.getValue().getBeneficiaryEntityId(), entry.getKey());
      }).collect(Collectors.toList());

      if (beneRes.size() == 1 && beneRes.get(0).getValue() != null) {
        setRcIdAndFamilySize(beneToFamilySize, beneRes.get(0).getValue().getRowId(),
            reportEntityItems, beneRes.get(0).getValue(), auxiliaryProperty);
      }

      Map<Authorization, Long> authMap = entry.getValue();
      Map<String, Integer> itemMap = new HashMap<>();
      for (Map.Entry<Authorization, Long> authEntry : authMap.entrySet()) {
        Integer amount = null;
        Map<String, Long> delivery = groupedDeliveries.getOrDefault(entry.getKey(), null);
        if (delivery != null) {
          Long deliveredCnt = delivery.getOrDefault(authEntry.getKey().getRowId(), null);
          if (deliveredCnt != null) {
            amount = deliveredCnt.intValue();
          }
        }
        itemMap.put(authEntry.getKey().getItemPack().getName(), amount);
      }
      reportEntityItems.setItemMap(itemMap);
      reportEntityItemsList.add(reportEntityItems);
    }
    reportDistributionPlanFormModel.setReportEntityItemsList(reportEntityItemsList);

    reportDistributionPlanFormModel.setReportEntityItemsList(reportEntityItemsList);
  }

  @PostMapping("distributionItemList")
  @ResponseStatus(value = HttpStatus.OK)
  public ResponseEntity<String> generateDistItemListPost(@ModelAttribute("reportDistributionPlanFormModel")
      ReportDistributionPlanFormModel reportDistributionPlanFormModel, HttpServletRequest request,
      HttpServletResponse response) {

    WebContext webContext = new WebContext(request, response, request.getServletContext());
    generateDistItemListCommon(reportDistributionPlanFormModel);

    // Write out the report
    webContext.setVariable("reportDistributionPlanFormModel", reportDistributionPlanFormModel);
    writeReport(webContext, "distribution_item_list_report", "report"
        + "/distributionItemListReport.html");

    return new ResponseEntity<>("Success!", HttpStatus.OK);
  }

  private void setReportItemSummaryFromItemPack(EntitlementRepository entitlementRepository,
      List<Entitlement> entitlementList, List<ReportItemSummary> reportItemSummaryList,
      Authorization selectedAuthorization, ItemPack itemPack) {
    if (itemPack != null) {
      ReportItemSummary reportItemSummary = new ReportItemSummary();

      reportItemSummary.setName(itemPack.getName());

      addEntitlementsForAuthorization(
          entitlementRepository, entitlementList, selectedAuthorization);

      reportItemSummary.setTotal(entitlementList.size());

      reportItemSummaryList.add(reportItemSummary);
    }
  }

  private void addEntitlementsForAuthorization(
      EntitlementRepository entitlementRepository, List<Entitlement> entitlementList,
      Authorization selectedAuthorization) {
    List<Entitlement> selectedEntitlementList = entitlementRepository
        .getEntitlements(selectedAuthorization).join();
    entitlementList.addAll(selectedEntitlementList);
  }

  @GetMapping("summaryReport")
  public ModelAndView generateSummary(@ModelAttribute("reportDistributionPlanFormModel")
      ReportDistributionPlanFormModel reportDistributionPlanFormModel, HttpServletRequest request,
      HttpServletResponse response, SessionStatus status) {

    ModelAndView modelAndView = new ModelAndView(REPORT_SUMMARY);

    generateSummaryCommon(reportDistributionPlanFormModel);

    status.setComplete();

    return modelAndView;
  }

  private void generateSummaryCommon(@ModelAttribute("reportDistributionPlanFormModel")
      ReportDistributionPlanFormModel reportDistributionPlanFormModel) {
    DistributionRepository distributionRepository = DataInstance.getDataRepos()
        .getDistributionRepository();
    CsvRepository csvRepository = DataInstance.getDataRepos().getCsvRepository();
    EntitlementRepository entitlementRepository = DataInstance.getDataRepos()
        .getEntitlementRepository();
    AuxiliaryProperty auxiliaryProperty = DataInstance.getDataRepos().getAuxiliaryProperty();

    // Get the appropriate distribution
    Distribution selectedDistribution = distributionRepository
        .getDistributionByRowId(reportDistributionPlanFormModel.getDistributionRowId());

    // Set the name for the distribution
    reportDistributionPlanFormModel.setDistributionName(selectedDistribution.getName());

    // Set the region
    Region distributionRegion = selectedDistribution.getLocation();
    if (distributionRegion != null) {
      reportDistributionPlanFormModel.setRegion(distributionRegion.getName());
    }

    List<Entitlement> entitlementList = new ArrayList<>();

    // Get the relevant authorizations for a distribution
    List<Authorization> selectedAuthorizations = selectedDistribution.getAuthorizations();

    // Make a set of Authorization ids
    Set<String> selectedAuthorizationIds = selectedAuthorizations.stream()
        .map(Authorization::getRowId).collect(Collectors.toSet());
    // Count the number of deliveries per beneficiary entity
    List<CsvDelivery> deliveryList = csvRepository.readTypedCsv(CsvDelivery.class)
        .orElse(new ArrayList<>())
        .stream()
        .filter(x -> selectedAuthorizationIds.contains(x.getAuthorizationId()))
        .collect(Collectors.toList());

    List<ReportItemSummary> reportItemSummaryList = new ArrayList<>();

    for (Authorization selectedAuthorization : selectedAuthorizations) {
      ReportItemSummary reportItemSummary = new ReportItemSummary();
      ItemPack itemPack = selectedAuthorization.getItemPack();
      reportItemSummary.setName(itemPack.getName());

      addEntitlementsForAuthorization(entitlementRepository, entitlementList, selectedAuthorization);

      Long deliveryCnt = deliveryList.stream().filter(x -> Objects.equals(x.getAuthorizationId(),
          selectedAuthorization.getRowId())).count();
      reportItemSummary.setTotal(deliveryCnt != null ? deliveryCnt.intValue() : null);
      reportItemSummaryList.add(reportItemSummary);
    }

    reportDistributionPlanFormModel.setReportItemSummaryList(reportItemSummaryList);

    reportDistributionPlanFormModel.setTotalItemsDistributed(deliveryList.size());

    // Only show target distribution if workflow mode is AuthorizationType.REQUIRED_REGISTRATION
    AuthorizationType authorizationType = auxiliaryProperty.getWorkflowMode();
    if (Objects.equals(authorizationType, AuthorizationType.REQUIRED_REGISTRATION)) {

      // Calculate the ages of the beneficiaries and put it into a map
      // Add the necessary age ranges
      Map<AgeRange, Integer> fDist = new LinkedHashMap<>();
      Map<AgeRange, Integer> mDist = new LinkedHashMap<>();
      Map<AgeRange, Integer> totDist = new LinkedHashMap<>();

      Map<AgeRange, Integer> reachedFDist = new LinkedHashMap<>();
      Map<AgeRange, Integer> reachedMDist = new LinkedHashMap<>();
      Map<AgeRange, Integer> reachedTotDist = new LinkedHashMap<>();

      Map<AgeRange, Integer> absentFDist = new LinkedHashMap<>();
      Map<AgeRange, Integer> absentMDist = new LinkedHashMap<>();
      Map<AgeRange, Integer> absentTotDist = new LinkedHashMap<>();

      // Calculate total distribution
      calculateTargetDistributions(entitlementList, fDist, mDist, totDist);

      // Calculate reached distribution
      Set<String> deliveredEntitlementIds =
          deliveryList.stream().map(CsvDelivery::getEntitlementId).collect(Collectors.toSet());
      List<Entitlement> reachedEntitlementList =
          entitlementList
              .stream()
              .filter(entitlement -> {return deliveredEntitlementIds.contains(entitlement.getRowId());})
              .collect(Collectors.toList());
      calculateTargetDistributions(reachedEntitlementList, reachedFDist, reachedMDist, reachedTotDist);

      // Calculate absent = total - reached;
      List<Entitlement> absentEntitlementList =
          entitlementList
              .stream()
              .filter(entitlement -> {return !deliveredEntitlementIds.contains(entitlement.getRowId());})
              .collect(Collectors.toList());
      calculateTargetDistributions(absentEntitlementList, absentFDist, absentMDist, absentTotDist);

      List<String> members = entitlementList.stream().map(Entitlement::getIndividualId).distinct().collect(Collectors.toList());

      reportDistributionPlanFormModel.setTotalIndividuals(members.size());

      long familyCount = members.stream()
          .map(memberId -> csvRepository.readIndexedTypedCsv(CsvIndividual.class)
          .map(memberCsv -> {
            if (memberCsv.get(memberId) != null) {
              return memberCsv.get(memberId).getBeneficiaryEntityRowId();
            } else { return null; }
            }))
          .filter(Optional::isPresent)
          .distinct()
          .count();

      reportDistributionPlanFormModel.setTotalBeneficiaryEntities((int) familyCount);

      // TODO: Make this more efficient!
      Map<String, Integer> totalDist = new LinkedHashMap<>();
      Map<String, Integer> femaleDist = new LinkedHashMap<>();
      Map<String, Integer> maleDist = new LinkedHashMap<>();

      // Reached means if the beneficiary_entity has received any item from the distribution
      // Absent means the beneficiary_entity has received nothing from the distribution
      // TODO: Add selection to be able to generate reports on each authorization
      Map<String, Integer> reachedTotalDist = new LinkedHashMap<>();
      Map<String, Integer> reachedFemaleDist = new LinkedHashMap<>();
      Map<String, Integer> reachedMaleDist = new LinkedHashMap<>();

      Map<String, Integer> absentTotalDist = new LinkedHashMap<>();
      Map<String, Integer> absentFemaleDist = new LinkedHashMap<>();
      Map<String, Integer> absentMaleDist = new LinkedHashMap<>();

      convertDistMapForDisplay(fDist, femaleDist);
      convertDistMapForDisplay(mDist, maleDist);
      convertDistMapForDisplay(totDist, totalDist);

      convertDistMapForDisplay(absentFDist, absentFemaleDist);
      convertDistMapForDisplay(absentMDist, absentMaleDist);
      convertDistMapForDisplay(absentTotDist, absentTotalDist);

      convertDistMapForDisplay(reachedFDist, reachedFemaleDist);
      convertDistMapForDisplay(reachedMDist, reachedMaleDist);
      convertDistMapForDisplay(reachedTotDist, reachedTotalDist);

      reportDistributionPlanFormModel.setFemaleAgeDistribution(femaleDist);
      reportDistributionPlanFormModel.setMaleAgeDistribution(maleDist);
      reportDistributionPlanFormModel.setTotalAgeDistribution(totalDist);

      reportDistributionPlanFormModel.setAbsentFemaleAgeDistribution(absentFemaleDist);
      reportDistributionPlanFormModel.setAbsentMaleAgeDistribution(absentMaleDist);
      reportDistributionPlanFormModel.setAbsentTotalAgeDistribution(absentTotalDist);

      reportDistributionPlanFormModel.setReachedFemaleAgeDistribution(reachedFemaleDist);
      reportDistributionPlanFormModel.setReachedMaleAgeDistribution(reachedMaleDist);
      reportDistributionPlanFormModel.setReachedTotalAgeDistribution(reachedTotalDist);
    }
  }

  @PostMapping("summaryReport")
  @ResponseStatus(value = HttpStatus.OK)
  public ResponseEntity<String> generateSummaryPost(@ModelAttribute(
      "reportDistributionPlanFormModel")
      ReportDistributionPlanFormModel reportDistributionPlanFormModel, HttpServletRequest request,
      HttpServletResponse response) {

    WebContext webContext = new WebContext(request, response, request.getServletContext());
    generateSummaryCommon(reportDistributionPlanFormModel);

    // Write out the report
    webContext.setVariable("reportDistributionPlanFormModel", reportDistributionPlanFormModel);
    writeReport(webContext, "summary_report", "report/summaryReport.html");

    return new ResponseEntity<>("Success!", HttpStatus.OK);
  }

  @GetMapping("deliveries")
  public ModelAndView deliveriesMenu(@ModelAttribute("reportDistributionPlanFormModel")
                                           ReportDistributionPlanFormModel reportDistributionPlanFormModel,
                                     @ModelAttribute("isNoRegMode") boolean isNoRegMode) {
    DataRepos dataRepos = DataInstance.getDataRepos();
    List<ReportAuthorization> reportAuthorizationList;

    if (!isNoRegMode) {
      DistributionRepository distributionRepository = dataRepos
          .getDistributionRepository();

      // Find the relevant distribution
      Distribution selectedDistribution = distributionRepository
          .getDistributionByRowId(reportDistributionPlanFormModel.getDistributionRowId());

      List<Authorization> authorizationList = selectedDistribution.getAuthorizations();
      reportAuthorizationList = authorizationList.stream()
          .map(authorization -> new ReportAuthorization(authorization.getItemPack().getName(),
              authorization.getRowId()))
          .collect(Collectors.toList());
    } else {
      reportAuthorizationList = dataRepos
          .getCsvRepository()
          .readTypedCsv(CsvAuthorization.class)
          .orElseThrow(IllegalStateException::new)
          .stream()
          .filter(auth -> auth.getDistributionId().equals(reportDistributionPlanFormModel.getDistributionRowId()))
          .findAny()
          .map(auth -> new ReportAuthorization(auth.getDistributionName(), auth.getRowId()))
          .map(Collections::singletonList)
          .orElseThrow(IllegalArgumentException::new);
    }

    ReportDeliveriesFormData reportDeliveriesFormData = new ReportDeliveriesFormData();
    reportDeliveriesFormData.setReportAuthorizations(reportAuthorizationList);
    reportDistributionPlanFormModel.setReportDeliveriesFormData(reportDeliveriesFormData);

    return new ModelAndView(REPORT_DELIVERIES);
  }

  @PostMapping("deliveries")
  @ResponseStatus(value = HttpStatus.OK)
  public ResponseEntity<String> generateDeliveriesCsv(
      @ModelAttribute("reportDistributionPlanFormModel")
          ReportDistributionPlanFormModel reportDistributionPlanFormModel) {

    CsvRepository csvRepository = DataInstance.getDataRepos().getCsvRepository();

    // Get the deliveries for this authorization
    List<String> deliveryIdList = csvRepository.readTypedCsv(CsvDelivery.class)
        .orElse(new ArrayList<>())
        .stream()
        .filter(delivery -> Objects.equals(
            delivery.getAuthorizationId(),
            reportDistributionPlanFormModel.getReportDeliveriesFormData().getAuthorizationRowId()
        ))
        .map(SyncRow::getRowId)
        .collect(Collectors.toList());

    Stream<String> ids = deliveryIdList.stream();

    Map<String, CsvDelivery> deliveryTyped = csvRepository
        .readIndexedTypedCsv(CsvDelivery.class)
        .orElseThrow(IllegalStateException::new);

    Map<String, UntypedSyncRow> deliveryUntyped = csvRepository
        .readIndexedUntypedCsv(FileUtil.getFileName(CsvDelivery.class))
        .orElseThrow(IllegalStateException::new);

    List<UntypedSyncRow> deliveries = ids
        .map(deliveryTyped::get)
        .map(delivery -> {
          UntypedSyncRow mergedRow = new UntypedSyncRow();

          // start with all columns in delivery base table
          mergedRow.getColumns().putAll(deliveryUntyped.get(delivery.getRowId()).getColumns());
          // merge in delivery custom table columns
          ControllerUtil.mergeCustomTableRow(delivery, csvRepository, mergedRow);

          mergedRow.setRowId(delivery.getRowId());
          return mergedRow;
        })
        .collect(Collectors.toList());

    if (deliveries.size() <= 0) {
      FxDialogUtil.showWarningDialog(TranslationUtil.getTranslations().getString(
          TranslationConsts.NO_DATA_TO_GENERATE_CSV));
      return new ResponseEntity<>("Success!", HttpStatus.OK);
    }

    ControllerUtil.writeOutCustomCsv(deliveries, logger);

    return new ResponseEntity<>("Success!", HttpStatus.OK);
  }
}
