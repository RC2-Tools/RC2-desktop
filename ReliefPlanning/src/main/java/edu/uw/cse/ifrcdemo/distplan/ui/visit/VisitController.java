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

package edu.uw.cse.ifrcdemo.distplan.ui.visit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uw.cse.ifrcdemo.distplan.data.ReliefDataInstance;
import edu.uw.cse.ifrcdemo.distplan.entity.VisitProgram;
import edu.uw.cse.ifrcdemo.distplan.logic.EntitlementFilter;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.AuthorizationCriterion;
import edu.uw.cse.ifrcdemo.distplan.model.rctemplate.RcVisitProgramTemplate;
import edu.uw.cse.ifrcdemo.distplan.model.visitprogram.VisitProgramRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.demographics.TargetDemoDistribution;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.distribution.DistributionStatusUpdateFormModel;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.rctemplate.TemplateSelectFormModel;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.rctemplate.ToTemplateFormModel;
import edu.uw.cse.ifrcdemo.distplan.util.BeneficiaryUtil;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.CriteriaUtil;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.RcTemplate;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.GenerateBy;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.range.Range;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.CriterionField;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.CriterionOperator;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.rctemplate.RcTemplateRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.location.LocationRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.demographics.DemographicsModel;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.demographics.InsufficientDemographicsDataException;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.location.LocationListModel;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.FxDialogUtil;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.sharedlib.consts.ModuleConsts;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.DistVisitProgStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.RcTemplateType;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import javafx.scene.control.ButtonType;
import org.apache.logging.log4j.Logger;
import org.apache.wink.json4j.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.NoResultException;
import javax.validation.Valid;
import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/visit")
@SessionAttributes(types = { VisitFormModel.class })
public class VisitController {
  public static final String FORM_MODEL_NAME = "newVisitForm";

  private static final String VISIT_VISIT_MENU = "visit/visitMenu";
  private static final String VISIT_VIEW_VISITS = "visit/viewVisits";
  private static final String VISIT_CREATE_VISIT = "visit/createVisit";
  private static final String VISIT_TEMPLATE = "visit/template";
  private static final String VISIT_BENE = "visit/newVisitBeneficiary";
  private static final String VISIT_SUMMARY = "visit/createSummary";
  private static final String VISIT_VIEW = "visit/viewVisit";

  private final CsvRepository csvRepository;
  private final VisitProgramRepository visitProgramRepository;
  private final TargetDemoDistribution targetDemoDistribution;
  private final ObjectMapper objectMapper;
  private final Logger logger;

  public VisitController(CsvRepository csvRepository,
                         VisitProgramRepository visitProgramRepository,
                         TargetDemoDistribution targetDemoDistribution,
                         ObjectMapper objectMapper,
                         Logger logger) {
    this.csvRepository = csvRepository;
    this.visitProgramRepository = visitProgramRepository;
    this.targetDemoDistribution = targetDemoDistribution;
    this.objectMapper = objectMapper;
    this.logger = logger;
  }

  @InitBinder
  public void initBinder(WebDataBinder webDataBinder) {
    webDataBinder.setDisallowedFields("locationName");
  }

  @ModelAttribute(FORM_MODEL_NAME)
  public VisitFormModel newVisitFormModel() {
    VisitFormModel visitFormModel = new VisitFormModel();

    visitFormModel.setGenerateBy(GenerateBy.HOUSEHOLD_MEMBER);

    return visitFormModel;
  }

  @GetMapping("")
  public String visitsMenu() {
    return VISIT_VISIT_MENU;
  }

  @GetMapping("viewVisits")
  public ModelAndView viewVisits() {
    VisitProgramRepository visitProgramRepository = ReliefDataInstance.getDataRepos().getVisitProgramRepository();
    ModelAndView modelAndView = new ModelAndView(VISIT_VIEW_VISITS);
    modelAndView.addObject("visitList", visitProgramRepository.getVisitProgramListDto());
    modelAndView.addObject("statusValues", DistVisitProgStatus.values());

    return modelAndView;
  }

  @GetMapping("viewVisits/{visitProgramRowId}")
  public ModelAndView viewVisit(@PathVariable String visitProgramRowId) throws JsonProcessingException {
    VisitProgram visitProgram;
    try {
      visitProgram = ReliefDataInstance
          .getDataRepos()
          .getVisitProgramRepository()
          .getVisitProgramByRowId(visitProgramRowId);
    } catch (NoResultException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, null, e);
    }

    VisitFormModel visitFormModel = new VisitFormModel();

    visitFormModel.setName(visitProgram.getName());
    visitFormModel.setLocationName(visitProgram.getLocation().getName());
    visitFormModel.setVisitForm(visitProgram.getCustomVisitForm());
    visitFormModel.setGenerateBy(visitProgram.isForMember() ?
        GenerateBy.HOUSEHOLD_MEMBER : GenerateBy.BENEFICIARY_UNIT);
    visitFormModel.setBeneficiaryIdRange(rangeListToString(visitProgram.getBeneficiaryRanges()));
    visitFormModel.setBeneficiaryCriteria(criteriaToString(visitProgram.getRules()));

    Stream<String> memberRowIds = EntitlementFilter
        .filter(
            csvRepository,
            convertCriteria(visitFormModel.getBeneficiaryCriteria()),
            visitFormModel.getGenerateBy()
        );

    ModelAndView modelAndView = new ModelAndView(VISIT_VIEW);
    modelAndView.addObject(FORM_MODEL_NAME, visitFormModel);

    DemographicsModel demoModel = null;
    try {
      demoModel = targetDemoDistribution.makeModelFromMember(memberRowIds);
    } catch (InsufficientDemographicsDataException e) {
      logger.debug("Cannot produce demographics distribution: {}", e.getMessage());
    }
    visitFormModel.setDemographicsModel(demoModel);

    return modelAndView;
  }

  @GetMapping("createVisit")
  public ModelAndView newVisit(@ModelAttribute(FORM_MODEL_NAME) VisitFormModel form) {
    ModelAndView modelAndView = new ModelAndView(VISIT_CREATE_VISIT);
    List<LocationListModel> locList = ReliefDataInstance
        .getDataRepos()
        .getLocationRepository()
        .getAllLocations()
        .stream()
        .map(LocationListModel::new)
        .collect(Collectors.toList());

    modelAndView.addObject("locList", locList);

    List<RcTemplate> distTemplates = ReliefDataInstance
        .getDataRepos()
        .getRcTemplateRepository()
        .getRcTemplatesByType(RcTemplateType.VISIT_PROGRAM)
        .join();

    modelAndView.addObject("visitTemplates", distTemplates);
    modelAndView.addObject("applyTemplateForm", new TemplateSelectFormModel());

    return modelAndView;
  }

  @PostMapping("createVisit")
  public ModelAndView newVisitPost(@Valid @ModelAttribute(FORM_MODEL_NAME) VisitFormModel form,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      ModelAndView modelAndView = new ModelAndView(VISIT_CREATE_VISIT);
      List<LocationListModel> locList = ReliefDataInstance
          .getDataRepos()
          .getLocationRepository()
          .getAllLocations()
          .stream()
          .map(LocationListModel::new)
          .collect(Collectors.toList());

      modelAndView.addObject("locList", locList);

      List<RcTemplate> distTemplates = ReliefDataInstance
          .getDataRepos()
          .getRcTemplateRepository()
          .getRcTemplatesByType(RcTemplateType.VISIT_PROGRAM)
          .join();

      modelAndView.addObject("visitTemplates", distTemplates);
      modelAndView.addObject("applyTemplateForm", new TemplateSelectFormModel());

      return modelAndView;
    }

    LocationRepository locationRepository = ReliefDataInstance.getDataRepos().getLocationRepository();
    String locName = locationRepository
        .getLocationByRowId(form.getLocationId())
        .getName();

    form.setLocationName(locName);

    return new ModelAndView("redirect:createVisitBeneficiary");
  }

  @PostMapping(value = "createVisit", params = "location")
  public String newVisitPost(String location, @ModelAttribute(FORM_MODEL_NAME) VisitFormModel form) {
    return "redirect:/location/new/visit";
  }

  @GetMapping("createVisitBeneficiary")
  public ModelAndView newVisitBeneficiaryCriteria(@ModelAttribute(FORM_MODEL_NAME) VisitFormModel form) {
    ModelAndView modelAndView = new ModelAndView(VISIT_BENE);

    Map<String, Set<CriterionField>> criteriaAttr = CriteriaUtil.buildCriteriaFieldMap(csvRepository);

    if (criteriaAttr.size() < 2) {
      ResourceBundle translations = TranslationUtil.getTranslations();
      FxDialogUtil.showWarningDialog(
          translations.getString(TranslationConsts.NO_REGISTRATION_DATA_AVAILABLE),
          translations.getString(TranslationConsts.NO_REGISTRATION_DATA_IMPLICATION)
      );
    }

    modelAndView.addObject("criteriaAttr", criteriaAttr);
    modelAndView.addObject("criteriaOp", CriterionOperator.values());
    modelAndView.addObject("generateByEnum", GenerateBy.values());

    return modelAndView;
  }

  @PostMapping("createVisitBeneficiary")
  public ModelAndView newVisitBeneficiaryCriteraPost(@ModelAttribute(FORM_MODEL_NAME) VisitFormModel form)
      throws ExecutionException, InterruptedException {
    List<List<AuthorizationCriterion>> criteria = convertCriteria(form.getBeneficiaryCriteria());
    if (CriteriaUtil.removeEmptyRules(criteria).isEmpty()) {
      ResourceBundle translations = TranslationUtil.getTranslations();

      ButtonType buttonType = FxDialogUtil.showConfirmDialogAndWait(
          translations.getString(TranslationConsts.WARNING_DIALOG_TITLE),
          translations.getString(TranslationConsts.WARNING_DIALOG_TITLE),
          translations.getString(TranslationConsts.VISIT_CRITERIA_EMPTY_WARNING)
      );

      if (buttonType != ButtonType.OK) {
        return newVisitBeneficiaryCriteria(form);
      }
    }

    return new ModelAndView("redirect:createSummary");
  }

  @GetMapping("createSummary")
  public ModelAndView newVisitSummary(@ModelAttribute(value = FORM_MODEL_NAME, binding = false) VisitFormModel form) {
    Stream<String> memberRowIds = EntitlementFilter
        .filter(
            csvRepository,
            convertCriteria(form.getBeneficiaryCriteria()),
            form.getGenerateBy()
        );

    ModelAndView modelAndView = new ModelAndView(VISIT_SUMMARY);
    DemographicsModel demoModel = null;
    try {
      demoModel = targetDemoDistribution.makeModelFromMember(memberRowIds);
    } catch (InsufficientDemographicsDataException e) {
      logger.debug("Cannot produce demographics distribution: {}", e.getMessage());
    }
    form.setDemographicsModel(demoModel);

    return modelAndView;
  }

  @PostMapping("createSummary")
  public ModelAndView newVisitSummaryPost(@ModelAttribute(value = FORM_MODEL_NAME, binding = false) VisitFormModel form,
                                    SessionStatus status) {
    LocationRepository locationRepository = ReliefDataInstance.getDataRepos().getLocationRepository();
    VisitProgramRepository visitProgramRepository = ReliefDataInstance.getDataRepos().getVisitProgramRepository();

    VisitProgram program = new VisitProgram();

    program.setStatus(DistVisitProgStatus.ACTIVE);
    program.setName(form.getName());
    program.setDescription(form.getDescription());
    program.setLocation(locationRepository.getLocationByRowId(form.getLocationId()));
    program.setCustomVisitForm(form.getVisitForm());

    program.setRules(convertCriteria(form.getBeneficiaryCriteria()));
    program.setBeneficiaryRanges(convertRange(form.getBeneficiaryIdRange()));
    program.setForMember(GenerateBy.BENEFICIARY_UNIT != form.getGenerateBy());

    visitProgramRepository.saveVisitProgram(program);
    status.setComplete();

    return new ModelAndView("redirect:/beneficiary", "clearHistory", "/");
  }

  @GetMapping("template")
  public String viewTemplate() {
    return VISIT_TEMPLATE;
  }

  @PostMapping("template")
  public ResponseEntity<String> saveAsTemplatePost(@RequestBody ToTemplateFormModel toTemplate) throws IOException, JSONException {
    VisitProgramRepository visitProgramRepository = ReliefDataInstance.getDataRepos().getVisitProgramRepository();

    RcTemplateRepository rcTemplateRepository = ReliefDataInstance.getDataRepos().getRcTemplateRepository();

    VisitProgram visitProgramByRowId = visitProgramRepository
        .getVisitProgramByRowId(toTemplate.getRowId());

    RcTemplate template = new RcVisitProgramTemplate(toTemplate.getTemplateName(), visitProgramByRowId).getTemplate();
    rcTemplateRepository.saveRcTemplate(template);

    return ResponseEntity.ok().build();
  }

  @PostMapping("criteria")
  @ResponseBody
  public String criteriaUpdate(@RequestBody List<List<AuthorizationCriterion>> criteria, @RequestParam GenerateBy generateBy) {
    long count = EntitlementFilter
        .filter(csvRepository, criteria, generateBy)
        .count();

    return Long.toString(count);
  }

  @PostMapping("recipient")
  public ResponseEntity<String> showRecipientListPost(@RequestBody List<List<AuthorizationCriterion>> criteria,
                                                      @RequestParam GenerateBy generateBy) throws IOException {
    List<String> ids = EntitlementFilter
        .filter(csvRepository, criteria, generateBy)
        .collect(Collectors.toList());

    Path outputPath = Files.createTempFile(ModuleConsts.PLANNING, GenConsts.CSV_FILE_EXTENSION);

    BeneficiaryUtil.writeDataForMemberAsCsv(ids, outputPath, csvRepository, visitProgramRepository);

    // set the file to readonly so that
    // the user doesn't get the impression that
    // editing the file has any effect
    outputPath.toFile().setReadOnly();

    if (Desktop.isDesktopSupported()) {
      Desktop.getDesktop().open(outputPath.toFile());
    } else {
      logger.error("Desktop NOT supported");
    }

    // TODO: return file location to UI

    return ResponseEntity.ok().build();
  }

  @PostMapping("status")
  public ResponseEntity<String> visitProgStatusUpdate(@RequestBody DistributionStatusUpdateFormModel update) {
    ReliefDataInstance
        .getDataRepos()
        .getVisitProgramRepository()
        .updateVisitProgramStatus(update.getRowId(), update.getStatus());

    return ResponseEntity.ok().build();
  }

  private List<Range> convertRange(String json) {
    try {
      return objectMapper.readerFor(Range.class).<Range>readValues(json).readAll();
    } catch (IOException e) {
      logger.info("cannot convert", e);

      return Collections.emptyList();
    }
  }

  private List<List<AuthorizationCriterion>> convertCriteria(String json) {
    try {
      return objectMapper
          .readerFor(new TypeReference<List<AuthorizationCriterion>>() {})
          .<List<AuthorizationCriterion>>readValues(json)
          .readAll();
    } catch (IOException e) {
      logger.info("cannot convert", e);

      return Collections.emptyList();
    }
  }

  private String rangeListToString(List<Range> ranges) throws JsonProcessingException {
    return objectMapper
        .writerFor(new TypeReference<List<Range>>() {})
        .writeValueAsString(ranges);
  }

  private String criteriaToString(List<List<AuthorizationCriterion>> criteria) throws JsonProcessingException {
    return objectMapper
        .writerFor(new TypeReference<List<List<AuthorizationCriterion>>>() {})
        .writeValueAsString(criteria);
  }
}
