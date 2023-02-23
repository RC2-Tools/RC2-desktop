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

package edu.uw.cse.ifrcdemo.distplan.ui.report.custom.csv;

import edu.uw.cse.ifrcdemo.distplan.logic.EntitlementFilter;
import edu.uw.cse.ifrcdemo.distplan.logic.InternalCsvReportFields;
import edu.uw.cse.ifrcdemo.distplan.model.criteria.AuthorizationCriterion;
import edu.uw.cse.ifrcdemo.distplan.model.criteria.CriterionField;
import edu.uw.cse.ifrcdemo.distplan.model.criteria.CriterionOperator;
import edu.uw.cse.ifrcdemo.distplan.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.distplan.model.distribution.DistListDto;
import edu.uw.cse.ifrcdemo.distplan.model.distribution.DistributionRepository;
import edu.uw.cse.ifrcdemo.distplan.model.entitlement.EntitlementRepository;
import edu.uw.cse.ifrcdemo.distplan.model.visitprogram.VisitProgramListDto;
import edu.uw.cse.ifrcdemo.distplan.model.visitprogram.VisitProgramRepository;
import edu.uw.cse.ifrcdemo.distplan.ui.formatter.CriteriaFormatter;
import edu.uw.cse.ifrcdemo.distplan.util.BeneficiaryUtil;
import edu.uw.cse.ifrcdemo.distplan.util.CriteriaUtil;
import edu.uw.cse.ifrcdemo.distplan.util.FxDialogUtil;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/report/csv")
@SessionAttributes(types = CsvReportFormModel.class)
public class CsvReportController {
  private static final String REPORT_CSV_REPORT_TEMPLATE = "report/csvReport";

  private final Logger logger;

  private final CsvRepository csvRepo;
  private final DistributionRepository distributionRepo;
  private final EntitlementRepository entitlementRepo;
  private final VisitProgramRepository visitProgramRepo;

  private final CriteriaFormatter criteriaFormatter;

  public CsvReportController(Logger logger,
                             CsvRepository csvRepo,
                             DistributionRepository distributionRepo,
                             EntitlementRepository entitlementRepo,
                             VisitProgramRepository visitProgramRepo,
                             CriteriaFormatter criteriaFormatter) {
    this.logger = logger;
    this.csvRepo = csvRepo;
    this.distributionRepo = distributionRepo;
    this.entitlementRepo = entitlementRepo;
    this.visitProgramRepo = visitProgramRepo;
    this.criteriaFormatter = criteriaFormatter;
  }

  @InitBinder
  public void initBinder(WebDataBinder webDataBinder) {
    webDataBinder.setDisallowedFields("distributionSet", "visitProgramSet");
    webDataBinder.addCustomFormatter(criteriaFormatter, "criteria");
  }

  @ModelAttribute("csvReportFormModel")
  public CsvReportFormModel newCsvReportFormModel() {
    CsvReportFormModel formModel = new CsvReportFormModel();

    Map<String, String> distributionSet = distributionRepo
        .getDistributionListDto(false)
        .stream()
        .collect(Collectors.toMap(DistListDto::getRowId, DistListDto::getName));

    Map<String, String> visitProgramSet = visitProgramRepo
        .getVisitProgramListDto()
        .stream()
        .collect(Collectors.toMap(VisitProgramListDto::getRowId, VisitProgramListDto::getName));

    formModel.setDistributionSet(distributionSet);
    formModel.setVisitProgramSet(visitProgramSet);

    formModel.setIncludeUnrealized(true);
    formModel.setCriteria(new ArrayList<>());

    return formModel;
  }

  @GetMapping
  public ModelAndView csvReport(@ModelAttribute(value = "csvReportFormModel", binding = false)
                                      CsvReportFormModel formModel) {
    ModelAndView modelAndView = new ModelAndView(REPORT_CSV_REPORT_TEMPLATE);

    modelAndView.addObject("criteriaAttr", CriteriaUtil.buildCriteriaFieldMap(csvRepo));
    modelAndView.addObject("criteriaOp", CriterionOperator.values());

    return modelAndView;
  }

  @PostMapping
  public ModelAndView csvReportBeneficiaryPost(@ModelAttribute("csvReportFormModel") CsvReportFormModel formModel,
                                               SessionStatus sessionStatus) {
    String primaryFilter = formModel.getPrimaryFilter();

    List<List<AuthorizationCriterion>> criteria =
        CriteriaUtil.removeEmptyRules(CriteriaUtil.deepCopyRules(formModel.getCriteria()));

    // modify the filter to account for the chosen distribution or visit program
    if (!primaryFilter.equals("__all")) {
      String key;

      if (formModel.getDistributionSet().containsKey(primaryFilter)) {
        key = formModel.isIncludeUnrealized() ?
            InternalCsvReportFields.TARGETED_BY_DIST_INCLUDE_ALL :
            InternalCsvReportFields.TARGETED_BY_DIST;
      } else if (formModel.getVisitProgramSet().containsKey(primaryFilter)) {
        key = formModel.isIncludeUnrealized() ?
            InternalCsvReportFields.TARGETED_BY_VISIT_PROG_INCLUDE_ALL :
            InternalCsvReportFields.TARGETED_BY_VISIT_PROG;
      } else {
        logger.debug("Unrecognized value {}", primaryFilter);
        return new ModelAndView("redirect:csv");
      }

      if (criteria.isEmpty()) {
        criteria.add(new ArrayList<>());
      }

      CriterionField field = new CriterionField(InternalCsvReportFields.TABLE_ID, key);
      AuthorizationCriterion ac = new AuthorizationCriterion(field, CriterionOperator.EQ, primaryFilter);

      for (List<AuthorizationCriterion> qualification : criteria) {
        qualification.add(ac);
      }
    }

    List<String> relevantMembers = EntitlementFilter
        .filterForCsvReport(csvRepo, distributionRepo, entitlementRepo, visitProgramRepo, criteria)
        .collect(Collectors.toList());

    FxDialogUtil
        .showFileChooserDialogAsync()
        .thenAcceptAsync(savePath -> {
          Path csvPath = savePath.resolve(getCsvFileName(formModel));

          try {
            BeneficiaryUtil.writeDataForMemberAsCsv(relevantMembers, csvPath, csvRepo, visitProgramRepo);
          } catch (IOException e) {
            FxDialogUtil.showScrollingExceptionDialog(e.getMessage(), e);
          }

          FxDialogUtil.showInfoDialog("File saved to " + csvPath);
        });

    sessionStatus.setComplete();
    return new ModelAndView("redirect:csv");
  }

  private static String getCsvFileName(CsvReportFormModel formModel) {
    StringJoiner joiner = new StringJoiner("-", "", ".csv");

    return joiner
        .add("beneficiary")
        .add(LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuu-MM-dd_HH-mm-ss")))
        .toString();
  }
}
