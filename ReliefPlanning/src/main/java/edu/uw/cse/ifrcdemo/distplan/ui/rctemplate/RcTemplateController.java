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

package edu.uw.cse.ifrcdemo.distplan.ui.rctemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uw.cse.ifrcdemo.distplan.data.ReliefDataInstance;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Authorization;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Distribution;
import edu.uw.cse.ifrcdemo.distplan.entity.VisitProgram;
import edu.uw.cse.ifrcdemo.distplan.logic.EntitlementFilter;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.AuthorizationCriterion;
import edu.uw.cse.ifrcdemo.distplan.model.rctemplate.RcDistributionTemplate;
import edu.uw.cse.ifrcdemo.distplan.model.rctemplate.RcTemplateUtil;
import edu.uw.cse.ifrcdemo.distplan.model.rctemplate.RcVisitProgramTemplate;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.authorization.AuthorizationFormModel;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.demographics.TargetDemoDistribution;
import edu.uw.cse.ifrcdemo.distplan.ui.distribution.DistributionController;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.distribution.DistributionFormModel;
import edu.uw.cse.ifrcdemo.distplan.ui.visit.VisitController;
import edu.uw.cse.ifrcdemo.distplan.ui.visit.VisitFormModel;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.RcTemplate;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.GenerateBy;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.range.Range;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.rctemplate.RcTemplateRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.demographics.InsufficientDemographicsDataException;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.rctemplate.AbsRcTemplateController;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.rctemplate.TemplateSelectFormModel;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.rctemplate.ToTemplateFormModel;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.FxDialogUtil;
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

import static edu.uw.cse.ifrcdemo.planningsharedlib.util.FxDialogUtil.showScrollingExceptionDialog;

@Controller
@RequestMapping("/rctemplate")
public class RcTemplateController extends AbsRcTemplateController {
  @Override
  protected AuxiliaryProperty getAuxiliaryProperty() {
    return ReliefDataInstance.getDataRepos().getAuxiliaryProperty();
  }

  @Override
  protected RcTemplateRepository getRcTemplateRepository() {
    return ReliefDataInstance.getDataRepos().getRcTemplateRepository();
  }

  @Override
  protected void generateTemplateZip(RcTemplate distTemplate, Path selectedOutputDir) throws IOException, JSONException, IllegalArgumentException {
    RcTemplateUtil.generateTemplateZip(distTemplate, selectedOutputDir);
  }

  @Override
  protected void importTemplate(File templateZip) throws IOException, IllegalArgumentException, JSONException {
    RcTemplateUtil.importTemplate(templateZip);
  }

  public RcTemplateController(TargetDemoDistribution targetDemoDistribution,
                              ObjectMapper objectMapper,
                              Logger logger) {
    super(targetDemoDistribution, objectMapper, logger);
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

  @GetMapping("/view/distribution/{templateId}")
  public ModelAndView viewDistTemplate(@PathVariable String templateId) throws
      IOException, JSONException, InsufficientDemographicsDataException {
    return new ModelAndView(VIEW_DIST_TEMPLATE, "distForm", distributionFormModelFromTemplate(templateId, false));
  }

  @GetMapping("/view/visit/{templateId}")
  public ModelAndView viewVisitTemplate(@PathVariable String templateId) throws IOException, JSONException {
    return new ModelAndView(VIEW_VISIT_TEMPLATE, "visitForm", visitFormModelFromTemplate(templateId));
  }

  private DistributionFormModel distributionFormModelFromTemplate(String templateId, boolean includeAuthDemo)
      throws IOException, JSONException, InsufficientDemographicsDataException {
    RcTemplateRepository rcTemplateRepository = ReliefDataInstance.getDataRepos().getRcTemplateRepository();
    AuxiliaryProperty auxiliaryProperty = ReliefDataInstance.getDataRepos().getAuxiliaryProperty();

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
      authFormModel.setItemId(authorization.getItem().getRowId());
      authFormModel.setDeliveryForm(authorization.getCustomDeliveryForm());
      authFormModel.setExtraFieldEntitlement(authorization.getExtraFieldEntitlements());
      authFormModel.setAssignSpecificBarcode(authorization.isAssignItemCode());
      authFormModel.setGenerateBy(authorization.getForIndividual() ?
          GenerateBy.HOUSEHOLD_MEMBER : GenerateBy.BENEFICIARY_UNIT);
      authFormModel.setBarcodeRange(authorization.getVoucherRanges());
      authFormModel.setBarcodeRangeList(authorization.getVoucherRanges());
      authFormModel.setBeneficiaryCriteria(authorization.getRules());
      authFormModel.setItemName(authorization.getItem().getName());

      if (auxiliaryProperty.getWorkflowMode() == AuthorizationType.REQUIRED_REGISTRATION) {
        authFormModel.setBeneficiaryIdRange(authorization.getBeneficiaryRanges());
      } else if (auxiliaryProperty.getWorkflowMode() == AuthorizationType.OPTIONAL_REGISTRATION) {
        authFormModel.setBeneficiaryIdRange(authorization.getVoucherRanges());
      }

      if (includeAuthDemo) {
        Stream<String> memberRowIds = EntitlementFilter.filter(
            ReliefDataInstance.getDataRepos().getCsvRepository(),
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
    RcTemplate rcTemplate = ReliefDataInstance
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
}
