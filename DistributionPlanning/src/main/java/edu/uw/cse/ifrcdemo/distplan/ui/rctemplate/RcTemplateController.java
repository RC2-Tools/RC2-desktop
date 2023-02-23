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

package edu.uw.cse.ifrcdemo.distplan.ui.rctemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uw.cse.ifrcdemo.distplan.data.DataInstance;
import edu.uw.cse.ifrcdemo.distplan.entity.Authorization;
import edu.uw.cse.ifrcdemo.distplan.entity.Distribution;
import edu.uw.cse.ifrcdemo.distplan.entity.RcTemplate;
import edu.uw.cse.ifrcdemo.distplan.entity.VisitProgram;
import edu.uw.cse.ifrcdemo.distplan.logic.EntitlementFilter;
import edu.uw.cse.ifrcdemo.distplan.model.GenerateBy;
import edu.uw.cse.ifrcdemo.distplan.model.authorization.Range;
import edu.uw.cse.ifrcdemo.distplan.model.criteria.AuthorizationCriterion;
import edu.uw.cse.ifrcdemo.distplan.model.rctemplate.RcDistributionTemplate;
import edu.uw.cse.ifrcdemo.distplan.model.rctemplate.RcTemplateRepository;
import edu.uw.cse.ifrcdemo.distplan.model.rctemplate.RcTemplateUtil;
import edu.uw.cse.ifrcdemo.distplan.model.rctemplate.RcVisitProgramTemplate;
import edu.uw.cse.ifrcdemo.distplan.ui.authorization.AuthorizationFormModel;
import edu.uw.cse.ifrcdemo.distplan.ui.demographics.InsufficientDemographicsDataException;
import edu.uw.cse.ifrcdemo.distplan.ui.demographics.TargetDemoDistribution;
import edu.uw.cse.ifrcdemo.distplan.ui.distribution.DistributionController;
import edu.uw.cse.ifrcdemo.distplan.ui.distribution.DistributionFormModel;
import edu.uw.cse.ifrcdemo.distplan.ui.visit.VisitController;
import edu.uw.cse.ifrcdemo.distplan.ui.visit.VisitFormModel;
import edu.uw.cse.ifrcdemo.distplan.util.FxDialogUtil;
import edu.uw.cse.ifrcdemo.sharedlib.model.config.AuxiliaryProperty;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationType;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.RcTemplateType;
import edu.uw.cse.ifrcdemo.translations.LogStr;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Logger;
import org.apache.wink.json4j.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.NoResultException;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static edu.uw.cse.ifrcdemo.distplan.util.FxDialogUtil.showScrollingExceptionDialog;

@Controller
@RequestMapping("/rctemplate")
public class RcTemplateController {
  private static final String FORM_MODEL = "templateList";
  private static final String TEMPLATE_LIST_TYPE = "type";
  private static final String VIEW_TEMPLATES = "rctemplate/viewTemplates";
  private static final String VIEW_DIST_TEMPLATE = "rctemplate/viewDistributionTemplate";
  private static final String VIEW_VISIT_TEMPLATE = "rctemplate/viewVisitTemplate";

  private final TargetDemoDistribution targetDemoDistribution;
  private final ObjectMapper objectMapper;
  private final Logger logger;

  public RcTemplateController(TargetDemoDistribution targetDemoDistribution,
                              ObjectMapper objectMapper,
                              Logger logger) {
    this.targetDemoDistribution = targetDemoDistribution;
    this.objectMapper = objectMapper;
    this.logger = logger;
  }

  @PostMapping("/apply/distribution")
  public String useDistTemplate(@Valid @ModelAttribute("applyTemplateForm")
                                      TemplateSelectFormModel templateForm,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) throws
      IOException, JSONException, InsufficientDemographicsDataException {
    if (!bindingResult.hasErrors()) {
      redirectAttributes.addFlashAttribute(
          DistributionController.FORM_MODEL_NAME,
          distributionFormModelFromTemplate(templateForm.getTemplateRowId(), true)
      );
    }

    return "redirect:/distribution/new";
  }

  @PostMapping("/apply/visit")
  public String useVisitTemplate(@Valid @ModelAttribute("applyTemplateForm")
                                       TemplateSelectFormModel templateForm,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) throws IOException, JSONException {
    if (!bindingResult.hasErrors()) {
      redirectAttributes.addFlashAttribute(
          VisitController.FORM_MODEL_NAME,
          visitFormModelFromTemplate(templateForm.getTemplateRowId())
      );
    }

    return "redirect:/visit/createVisit";
  }

  @GetMapping("/view/distribution")
  public ModelAndView viewDistTemplates() {
    ModelAndView modelAndView = new ModelAndView(VIEW_TEMPLATES);

    modelAndView.addObject(FORM_MODEL, getTemplatesAsFormModel(RcTemplateType.DISTRIBUTION));
    modelAndView.addObject(TEMPLATE_LIST_TYPE, "DistributionTemplate");

    return modelAndView;
  }

  @GetMapping("/view/distribution/{templateId}")
  public ModelAndView viewDistTemplate(@PathVariable String templateId) throws
      IOException, JSONException, InsufficientDemographicsDataException {
    return new ModelAndView(VIEW_DIST_TEMPLATE, "distForm", distributionFormModelFromTemplate(templateId, false));
  }

  @GetMapping("/view/visit")
  public ModelAndView viewVisitTemplates() {
    ModelAndView modelAndView = new ModelAndView(VIEW_TEMPLATES);

    modelAndView.addObject(FORM_MODEL, getTemplatesAsFormModel(RcTemplateType.VISIT_PROGRAM));
    modelAndView.addObject(TEMPLATE_LIST_TYPE, "VisitProgramTemplate");

    return modelAndView;
  }

  @GetMapping("/view/visit/{templateId}")
  public ModelAndView viewVisitTemplate(@PathVariable String templateId) throws IOException, JSONException {
    return new ModelAndView(VIEW_VISIT_TEMPLATE, "visitForm", visitFormModelFromTemplate(templateId));
  }

  private DistributionFormModel distributionFormModelFromTemplate(String templateId, boolean includeAuthDemo)
      throws IOException, JSONException, InsufficientDemographicsDataException {
    RcTemplateRepository rcTemplateRepository = DataInstance.getDataRepos().getRcTemplateRepository();
    AuxiliaryProperty auxiliaryProperty = DataInstance.getDataRepos().getAuxiliaryProperty();

    RcTemplate rcTemplate = rcTemplateRepository.findRcTemplate(templateId);
    Distribution distribution =
        new RcDistributionTemplate(rcTemplate.getJsonEncodingString()).getDistribution();

    DistributionFormModel distributionFormModel = new DistributionFormModel();

    distributionFormModel.setFieldSummaryReport(distribution.getSummaryForm());

    List<AuthorizationFormModel> authList = new ArrayList<>();
    for (Authorization authorization : distribution.getAuthorizations()) {
      AuthorizationFormModel authFormModel = new AuthorizationFormModel();
      authList.add(authFormModel);

      authFormModel.setId(UUID.randomUUID().toString());
      authFormModel.setItemId(authorization.getItemPack().getRowId());
      authFormModel.setDeliveryForm(authorization.getCustomDeliveryForm());
      authFormModel.setExtraFieldEntitlement(authorization.getExtraFieldEntitlements());
      authFormModel.setAssignSpecificBarcode(authorization.isAssignItemPackCode());
      authFormModel.setGenerateBy(authorization.getForIndividual() ?
          GenerateBy.HOUSEHOLD_MEMBER : GenerateBy.BENEFICIARY_UNIT);
      authFormModel.setBarcodeRange(authorization.getVoucherRanges());
      authFormModel.setBarcodeRangeList(authorization.getVoucherRanges());
      authFormModel.setBeneficiaryCriteria(authorization.getRules());
      authFormModel.setItemName(authorization.getItemPack().getName());

      if (auxiliaryProperty.getWorkflowMode() == AuthorizationType.REQUIRED_REGISTRATION) {
        authFormModel.setBeneficiaryIdRange(authorization.getBeneficiaryRanges());
      } else if (auxiliaryProperty.getWorkflowMode() == AuthorizationType.OPTIONAL_REGISTRATION) {
        authFormModel.setBeneficiaryIdRange(authorization.getVoucherRanges());
      }

      if (includeAuthDemo) {
        Stream<String> memberRowIds = EntitlementFilter.filter(
            DataInstance.getDataRepos().getCsvRepository(),
            authorization.getRules(),
            authFormModel.getGenerateBy()
        );

        if (auxiliaryProperty.getWorkflowMode() == AuthorizationType.REQUIRED_REGISTRATION) {
          authFormModel.setDemographicsModel(targetDemoDistribution.makeModelFromMember(memberRowIds));
        }
      }
    }
    distributionFormModel.setAuthorizations(authList);

    return distributionFormModel;
  }

  private VisitFormModel visitFormModelFromTemplate(String templateId) throws IOException, JSONException {
    RcTemplate rcTemplate = DataInstance
        .getDataRepos()
        .getRcTemplateRepository()
        .findRcTemplate(templateId);
    VisitProgram visitProgram =
        new RcVisitProgramTemplate(rcTemplate.getJsonEncodingString()).getVisitProgram();

    VisitFormModel visitFormModel = new VisitFormModel();
    visitFormModel.setVisitForm(visitProgram.getCustomVisitForm());
    visitFormModel.setGenerateBy(visitProgram.isForMember() ?
        GenerateBy.HOUSEHOLD_MEMBER : GenerateBy.BENEFICIARY_UNIT);
    visitFormModel.setBeneficiaryIdRange(rangeListToString(visitProgram.getBeneficiaryRanges()));
    visitFormModel.setBeneficiaryCriteria(criteriaToString(visitProgram.getRules()));

    return visitFormModel;
  }

  private List<ToTemplateFormModel> getTemplatesAsFormModel(RcTemplateType rcTemplateType) {
    return DataInstance
          .getDataRepos()
          .getRcTemplateRepository()
          .getRcTemplatesByType(rcTemplateType)
          .join()
          .stream()
          .map(rcTemplate -> new ToTemplateFormModel(rcTemplate.getName(), rcTemplate.getRowId()))
          .collect(Collectors.toList());
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

  @GetMapping("/export/{templateId}")
  public String exportTemplate(@PathVariable String templateId) {

    ResourceBundle translations = TranslationUtil.getTranslations();

    try {

      Path selectedOutputDir = null;
      try {
        selectedOutputDir = FxDialogUtil.showFileChooserDialog();
      } catch (ExecutionException e) {
        e.printStackTrace();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      RcTemplate distTemplate = DataInstance
          .getDataRepos()
          .getRcTemplateRepository()
          .findRcTemplate(templateId);

      if (distTemplate != null) {
        RcTemplateUtil.generateTemplateZip(distTemplate, selectedOutputDir);
        FxDialogUtil.showInfoDialog(translations.getString(TranslationConsts.TEMPLATE_EXPORTED_SUCCESSFULLY));
      }

    } catch (IOException | JSONException e) {
      logger.error(LogStr.LOG_FAILED_TO_EXPORT_TEMPLATE + '\n' + ExceptionUtils.getStackTrace(e));
      showScrollingExceptionDialog(translations.getString(TranslationConsts.FAILED_TO_EXPORT_TEMPLATE), e);
    } catch (NoResultException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, null, e);
    }

    return "redirect:/";
  }

  @GetMapping("/import")
  public String importTemplate() {

    ResourceBundle translations = TranslationUtil.getTranslations();

    try {

      Path selectedOutputDir = null;
      try {
        selectedOutputDir = FxDialogUtil.showTemplateChooserDialog();
      } catch (ExecutionException e) {
        e.printStackTrace();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      if(selectedOutputDir == null) {
        FxDialogUtil.showInfoDialog(translations.getString(TranslationConsts.TEMPLATE_NOT_FOUND_MSG));
      } else {
        File template = selectedOutputDir.toFile();
        if (template.exists()) {
          RcTemplateUtil.importTemplate(template);
          FxDialogUtil.showInfoDialog( translations.getString(TranslationConsts.TEMPLATE_IMPORT_SUCCESSFUL ));
        }
      }

    } catch (IOException | JSONException e) {
      logger.error(LogStr.LOG_FAILED_TO_IMPORT_TEMPLATE + '\n' + ExceptionUtils.getStackTrace(e));
      showScrollingExceptionDialog(translations.getString(TranslationConsts.TEMPLATE_IMPORT_FAILED), e);
    } catch (NoResultException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, null, e);
    }

    return "redirect:/";
  }
}
