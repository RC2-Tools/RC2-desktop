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

package edu.uw.cse.ifrcdemo.distplan.ui.report.custom.pdf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import edu.uw.cse.ifrcdemo.distplan.logic.EntitlementFilter;
import edu.uw.cse.ifrcdemo.distplan.logic.InternalCsvReportFields;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.AuthorizationCriterion;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.distribution.DistListDto;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.distribution.DistributionRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.entitlement.EntitlementRepository;
import edu.uw.cse.ifrcdemo.distplan.model.visitprogram.VisitProgramListDto;
import edu.uw.cse.ifrcdemo.distplan.model.visitprogram.VisitProgramRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.formatter.CriteriaFormatter;
import edu.uw.cse.ifrcdemo.distplan.util.BeneficiaryUtil;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.CriteriaUtil;
import edu.uw.cse.ifrcdemo.distplan.util.ExportUtil;
import edu.uw.cse.ifrcdemo.distreport.consts.ReportConsts;
import edu.uw.cse.ifrcdemo.distreport.logic.ReportGenerator;
import edu.uw.cse.ifrcdemo.distreport.model.ReportMetadata;
import edu.uw.cse.ifrcdemo.distreport.model.ReportUntypedSyncRow;
import edu.uw.cse.ifrcdemo.mustachetopdf.PdfGenerator;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.CriterionField;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.CriterionOperator;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.FxDialogUtil;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.InternalFileStoreUtil;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.BaseSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvAuthorization;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvAuthorizationReport;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvBeneficiaryEntity;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvDelivery;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvDistribution;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvEntitlement;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvIndividual;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvVisit;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvVisitProgram;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.UntypedSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.HasCustomTable;
import edu.uw.cse.ifrcdemo.sharedlib.util.FileUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.StringUtil;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/report/pdf")
@SessionAttributes(types = PdfReportFormModel.class)
public class PdfReportController {
  private static final String FORM_MODEL_NAME = "pdfReportFormModel";
  private static final String PDF_REPORT_TEMPLATE = "report/pdfReport";

  private final Logger logger;

  private final CsvRepository csvRepo;
  private final DistributionRepository distributionRepo;
  private final EntitlementRepository entitlementRepo;
  private final VisitProgramRepository visitProgramRepo;

  private final CriteriaFormatter criteriaFormatter;
  private final MustacheFactory mustacheFactory;
  private final ObjectMapper objectMapper;

  public PdfReportController(Logger logger,
                             CsvRepository csvRepo,
                             DistributionRepository distributionRepo,
                             EntitlementRepository entitlementRepo,
                             VisitProgramRepository visitProgramRepo,
                             CriteriaFormatter criteriaFormatter,
                             MustacheFactory mustacheFactory,
                             ObjectMapper objectMapper) {
    this.logger = logger;
    this.csvRepo = csvRepo;
    this.distributionRepo = distributionRepo;
    this.entitlementRepo = entitlementRepo;
    this.visitProgramRepo = visitProgramRepo;
    this.criteriaFormatter = criteriaFormatter;
    this.mustacheFactory = mustacheFactory;
    this.objectMapper = objectMapper;
  }

  @InitBinder
  public void initBinder(WebDataBinder webDataBinder) {
    webDataBinder.setDisallowedFields("distributionSet", "visitProgramSet");
    webDataBinder.addCustomFormatter(criteriaFormatter, "criteria");
  }

  @ModelAttribute(FORM_MODEL_NAME)
  public PdfReportFormModel newPdfReportFormModel() {
    PdfReportFormModel formModel = new PdfReportFormModel();

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
  public ModelAndView pdfReport(@ModelAttribute(value = FORM_MODEL_NAME, binding = false)
                                      PdfReportFormModel pdfReportFormModel) {
    ModelAndView modelAndView = new ModelAndView(PDF_REPORT_TEMPLATE);

    modelAndView.addObject("criteriaAttr", CriteriaUtil.buildCriteriaFieldMap(csvRepo));
    modelAndView.addObject("criteriaOp", CriterionOperator.values());

    return modelAndView;
  }

  @PostMapping
  public ModelAndView pdfReportPost(@ModelAttribute(FORM_MODEL_NAME) PdfReportFormModel formModel,
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
        return new ModelAndView("redirect:pdf");
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

    Mustache mustacheTemplate = mustacheFactory.compile(formModel.getTemplatePath());

    Map<String, Object> scopes = buildScopes(compileRelevantBeneficiaryDate(criteria));
    scopes.put(ReportConsts.METADATA_SCOPE_NAME, new ReportMetadata());
    scopes.put(ReportConsts.CRITERIA_SCOPE_NAME, formModel.getCriteria()); // use the unmodified criteria

    Document jsoupDoc;
    try(Writer writer = new StringWriter()) {
      mustacheTemplate.execute(writer, scopes);
      jsoupDoc = Jsoup.parse(writer.toString());

      Element scriptElement = jsoupDoc.head().getElementById("reportGenerateData");
      // if a script is supplied with the template,
      // use the script to generate more scopes then re-execute the template
      if (scriptElement != null) {
        String scriptContent = scriptElement.data();

        if (StringUtil.isNotNullAndNotEmpty(scriptContent)) {
          Map<String, Object> generatedScope = updateScopeFromScript(convertScopesToJson(scopes), scriptContent);
          scopes.put(ReportConsts.CUSTOM_DATA_SCOPE_NAME, generatedScope);

          logger.debug(generatedScope);

          try(Writer rewriter = new StringWriter()) {
            mustacheTemplate.execute(rewriter, scopes);
            jsoupDoc = Jsoup.parse(rewriter.toString());
          }
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    org.w3c.dom.Document reportDocument = new W3CDom().fromJsoup(jsoupDoc);

    FxDialogUtil
        .showFileChooserDialogAsync()
        .thenAcceptAsync(savePath -> {
          Path pdfPath = savePath.resolve(getPdfFileName(formModel));

          try {
            PdfGenerator.generatePdf(reportDocument, pdfPath);
          } catch (Exception e) {
            FxDialogUtil.showScrollingExceptionDialog(e.getMessage(), e);
          }

          FxDialogUtil.showInfoDialog("File saved to " + pdfPath);
        });

    sessionStatus.setComplete();
    return new ModelAndView("redirect:pdf");
  }

  private List<UntypedSyncRow> compileRelevantBeneficiaryDate(List<List<AuthorizationCriterion>> rules) {
    Map<String, CsvIndividual> indexedMember = csvRepo
        .readIndexedTypedCsv(CsvIndividual.class)
        .orElseThrow(IllegalStateException::new);

    List<CsvVisit> extraVisits = ExportUtil.generateVisitUpdate(visitProgramRepo, csvRepo);

    return EntitlementFilter
        .filterForCsvReport(csvRepo, distributionRepo, entitlementRepo, visitProgramRepo, rules)
        .map(indexedMember::get)
        .map(member -> BeneficiaryUtil.getAllDataOnMember(member, csvRepo, visitProgramRepo, extraVisits))
        .collect(Collectors.toList());
  }

  private Map<String, Object> buildScopes(List<UntypedSyncRow> relevantBeneficiaries) {
    // base table classes except for member
    List<? extends Class<? extends BaseSyncRow>> baseTables = Arrays.asList(
        CsvBeneficiaryEntity.class,
        CsvDistribution.class,
        CsvAuthorization.class,
        CsvEntitlement.class,
        CsvDelivery.class,
        CsvAuthorizationReport.class,
        CsvVisitProgram.class,
        CsvVisit.class
    );

    Set<String> relevantBeneficiaryEntityRowId = relevantBeneficiaries
        .stream()
        .map(r -> r.getColumns().get("members_beneficiary_entity_row_id"))
        .collect(Collectors.toSet());

    Map<String, List<UntypedSyncRow>> baseCustomTableScopes = new HashMap<>();
    Set<String> customTables = new HashSet<>();

    Set<String> beneficiaryEntityCustomTables = new HashSet<>();
    Set<String> relevantBeneficiaryEntityCustomRows = new HashSet<>();

    for (Class<? extends BaseSyncRow> table : baseTables) {
      String filename = FileUtil.getFileName(table);
      String tableName = FileUtil.getTableName(table);

      List<UntypedSyncRow> rows = csvRepo
          .readUntypedCsv(filename)
          .orElseThrow(IllegalStateException::new);

      // if this table is the beneficiary entity table
      // it needs to be filtered to keep only the relevant beneficiaries
      // add keep track of the relevant custom row row id
      Predicate<BaseSyncRow> rowFilter;
      if (table.equals(CsvBeneficiaryEntity.class)) {
        rowFilter = row -> relevantBeneficiaryEntityRowId.contains(row.getRowId());

        rows = rows
            .stream()
            .filter(rowFilter)
            .collect(Collectors.toList());

        List<CsvBeneficiaryEntity> relevants = csvRepo
            .readTypedCsv(CsvBeneficiaryEntity.class)
            .orElseThrow(IllegalStateException::new)
            .stream()
            .filter(rowFilter)
            .collect(Collectors.toList());

        beneficiaryEntityCustomTables.addAll(relevants
            .stream()
            .map(HasCustomTable::getCustomTableFormId)
            .collect(Collectors.toSet())
        );

        relevantBeneficiaryEntityCustomRows.addAll(relevants
            .stream()
            .map(HasCustomTable::getCustomTableRowId)
            .collect(Collectors.toSet()));
      } else {
        rowFilter = row -> true;
      }

      baseCustomTableScopes.put(tableName, rows);

      if (HasCustomTable.class.isAssignableFrom(table)) {
        Set<String> tableCustomTables = csvRepo
            .readTypedCsv(table)
            .orElseThrow(IllegalStateException::new)
            .stream()
            .filter(rowFilter)
            .map(HasCustomTable.class::cast)
            .map(HasCustomTable::getCustomTableFormId)
            .collect(Collectors.toSet());
        customTables.addAll(tableCustomTables);
      }
    }

    for (String customTable : customTables) {
      String fileName = FileUtil.getFileName(customTable);

      List<UntypedSyncRow> rows = csvRepo
          .readUntypedCsv(fileName)
          .orElseGet(Collections::emptyList);

      if (beneficiaryEntityCustomTables.contains(customTable)) {
        rows = rows
            .stream()
            .filter(row -> relevantBeneficiaryEntityCustomRows.contains(row.getRowId()))
            .collect(Collectors.toList());
      }

      baseCustomTableScopes.put(customTable, rows);
    }

    Map<String, List<ReportUntypedSyncRow>> wrappedTables =
        ReportGenerator.buildWrappedTables(baseCustomTableScopes, InternalFileStoreUtil.getCurrentSnapshotStoragePath());

    Map<String, Object> scopes = new HashMap<>();
    scopes.putAll(baseCustomTableScopes);
    scopes.putAll(wrappedTables);

    // do this at the end
    // it contains base + custom table already
    scopes.put(FileUtil.getTableName(CsvIndividual.class), relevantBeneficiaries);

    return scopes;
  }

  private Map<String, String> convertScopesToJson(Map<String, Object> scopes) {
    return scopes
        .entrySet()
        .stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> {
              try {
                return objectMapper.writeValueAsString(entry.getValue());
              } catch (JsonProcessingException e) {
                return "";
              }
            }
        ));
  }

  private Map<String, Object> updateScopeFromScript(Map<String, String> jsonTables, String script) {
    return CompletableFuture.supplyAsync(() -> {
      WebEngine webEngine = new WebEngine();

      webEngine.executeScript("window.reportData = {};");
      for (Map.Entry<String, String> table : jsonTables.entrySet()) {
        webEngine.executeScript("window.reportData['" + table.getKey() + "'] = " + table.getValue());
      }

      Object scriptResult = webEngine.executeScript(script);
      String resultJson = ((JSObject) webEngine.executeScript("JSON")).call("stringify", scriptResult).toString();

      Map<String, Object> map;
      try {
        map = objectMapper.readValue(resultJson, new TypeReference<Map<String, Object>>() {});
      } catch (IOException e) {
        logger.catching(Level.INFO, e);
        map = Collections.emptyMap();
      }

      return map;
    }, Platform::runLater)
        .join();
  }

  private static String getPdfFileName(PdfReportFormModel formModel) {
    StringJoiner joiner = new StringJoiner("-", "", ".pdf");

    return joiner
        .add("report")
        .add(LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuu-MM-dd_HH-mm-ss")))
        .toString();
  }
}
