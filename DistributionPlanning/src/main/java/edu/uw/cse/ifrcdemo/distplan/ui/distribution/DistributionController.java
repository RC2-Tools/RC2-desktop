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

package edu.uw.cse.ifrcdemo.distplan.ui.distribution;

import edu.uw.cse.ifrcdemo.distplan.data.DataInstance;
import edu.uw.cse.ifrcdemo.distplan.entity.Authorization;
import edu.uw.cse.ifrcdemo.distplan.entity.Distribution;
import edu.uw.cse.ifrcdemo.distplan.entity.RcTemplate;
import edu.uw.cse.ifrcdemo.distplan.logic.EntitlementFilter;
import edu.uw.cse.ifrcdemo.distplan.model.GenerateBy;
import edu.uw.cse.ifrcdemo.distplan.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.distplan.model.distribution.DistExporter;
import edu.uw.cse.ifrcdemo.distplan.model.distribution.DistributionRepository;
import edu.uw.cse.ifrcdemo.distplan.model.itempack.ItemPackRepository;
import edu.uw.cse.ifrcdemo.distplan.model.rctemplate.RcDistributionTemplate;
import edu.uw.cse.ifrcdemo.distplan.model.rctemplate.RcTemplateRepository;
import edu.uw.cse.ifrcdemo.distplan.model.region.RegionRepository;
import edu.uw.cse.ifrcdemo.distplan.model.visitprogram.VisitProgramRepository;
import edu.uw.cse.ifrcdemo.distplan.ui.authorization.AuthorizationController;
import edu.uw.cse.ifrcdemo.distplan.ui.authorization.AuthorizationFormModel;
import edu.uw.cse.ifrcdemo.distplan.ui.demographics.DemographicsModel;
import edu.uw.cse.ifrcdemo.distplan.ui.demographics.InsufficientDemographicsDataException;
import edu.uw.cse.ifrcdemo.distplan.ui.demographics.TargetDemoDistribution;
import edu.uw.cse.ifrcdemo.distplan.ui.location.LocationListModel;
import edu.uw.cse.ifrcdemo.distplan.ui.rctemplate.TemplateSelectFormModel;
import edu.uw.cse.ifrcdemo.distplan.ui.rctemplate.ToTemplateFormModel;
import edu.uw.cse.ifrcdemo.sharedlib.model.config.AuxiliaryProperty;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationType;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.DistVisitProgStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.RcTemplateType;
import org.apache.logging.log4j.Logger;
import org.apache.wink.json4j.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/distribution")
@SessionAttributes(types = {
    DistributionFormModel.class
})
public class DistributionController {
  public static final String FORM_MODEL_NAME = "newDistForm";

  private static final String DISTRIBUTION_MENU_TEMPLATE = "distribution/distributionMenu";
  private static final String DISTRIBUTION_NEW_BASIC_INFO_TEMPLATE = "distribution/newDistributionBasicInfo";
  private static final String DISTRIBUTION_NEW_AUTHORIZATION = "distribution/newDistributionAuthorizations";
  private static final String DISTRIBUTION_NEW_SUMMARY = "distribution/newDistributionSummary";
  private static final String DISTRIBUTION_VIEW_LIST = "distribution/viewDistributionList";
  private static final String DISTRIBUTION_VIEW = "distribution/viewDistribution";

  private final DistExporter distExporter;
  private final TargetDemoDistribution targetDemoDistribution;
  private final CsvRepository csvRepository;
  private final VisitProgramRepository visitProgramRepository;
  private final Logger logger;

  public DistributionController(DistExporter distExporter,
                                TargetDemoDistribution targetDemoDistribution,
                                CsvRepository csvRepository,
                                VisitProgramRepository visitProgramRepository,
                                Logger logger) {
    this.distExporter = distExporter;
    this.targetDemoDistribution = targetDemoDistribution;
    this.csvRepository = csvRepository;
    this.visitProgramRepository = visitProgramRepository;
    this.logger = logger;
  }

  @InitBinder
  public void initBinder(WebDataBinder webDataBinder) {
    webDataBinder.setDisallowedFields("authorizations");
    webDataBinder.setDisallowedFields("locationName");
  }

  @ModelAttribute(FORM_MODEL_NAME)
  public DistributionFormModel newDistributionFormModel() {
    DistributionFormModel distributionFormModel = new DistributionFormModel();
    distributionFormModel.setAuthorizations(new ArrayList<>());

    return distributionFormModel;
  }

  @GetMapping("")
  public String distributionMenu() {
    return DISTRIBUTION_MENU_TEMPLATE;
  }

  @GetMapping("new")
  public ModelAndView newDistribution(@ModelAttribute(FORM_MODEL_NAME) DistributionFormModel form) {
    ModelAndView modelAndView = new ModelAndView(DISTRIBUTION_NEW_BASIC_INFO_TEMPLATE);

    // TODO: cache this
    List<LocationListModel> locList = DataInstance
        .getDataRepos()
        .getRegionRepository()
        .getAllRegion()
        .stream()
        .map(LocationListModel::new)
        .collect(Collectors.toList());

    modelAndView.addObject("locList", locList);

    List<RcTemplate> distTemplates = DataInstance
        .getDataRepos()
        .getRcTemplateRepository()
        .getRcTemplatesByType(RcTemplateType.DISTRIBUTION)
        .join();

    modelAndView.addObject("distTemplates", distTemplates);
    modelAndView.addObject("applyTemplateForm", new TemplateSelectFormModel());
    modelAndView.addObject("clearHistory", "/distribution");

    return modelAndView;
  }

  @PostMapping("new")
  public ModelAndView newDistBasicInfoPost(@Valid @ModelAttribute(FORM_MODEL_NAME) DistributionFormModel form,
                                           BindingResult bindingResult) {
    RegionRepository regionRepository = DataInstance.getDataRepos().getRegionRepository();
    RcTemplateRepository rcTemplateRepository = DataInstance.getDataRepos().getRcTemplateRepository();

    if (bindingResult.hasErrors()) {
      ModelAndView modelAndView = new ModelAndView(DISTRIBUTION_NEW_BASIC_INFO_TEMPLATE);

      // TODO: cache this
      List<LocationListModel> locList = regionRepository
          .getAllRegion()
          .stream()
          .map(LocationListModel::new)
          .collect(Collectors.toList());

      modelAndView.addObject("locList", locList);

      List<RcTemplate> distTemplates = rcTemplateRepository
          .getRcTemplatesByType(RcTemplateType.DISTRIBUTION)
          .join();

      modelAndView.addObject("distTemplates", distTemplates);
      modelAndView.addObject("applyTemplateForm", new TemplateSelectFormModel());

      return modelAndView;
    }

    String locName = regionRepository
        .getRegionByRowId(form.getLocationId())
        .getName();

    form.setLocationName(locName);

    return new ModelAndView("redirect:new/authorization");
  }

  @PostMapping(value = "new", params = "location")
  public String newDistBasicInfoPost(String location,
                                     @ModelAttribute(FORM_MODEL_NAME) DistributionFormModel form) {
    return "redirect:/location/new/dist";
  }

  @GetMapping("new/authorization")
  public ModelAndView newDistAuthorizations(@ModelAttribute(FORM_MODEL_NAME) DistributionFormModel form,
                                      @ModelAttribute(
                                          value = AuthorizationController.NEW_AUTH_FLASH_ATTR,
                                          binding = false
                                      ) AuthorizationFormModel auth,
                                      HttpServletRequest request) {
    // Returning from adding or editing a new authorization
    if (RequestContextUtils.getInputFlashMap(request) != null &&
        RequestContextUtils.getInputFlashMap(request).containsValue(auth)) {

      form.getAuthorizations().removeIf(matchesRowId(auth.getId()));
      form.getAuthorizations().add(auth);
    }

    return new ModelAndView(DISTRIBUTION_NEW_AUTHORIZATION, "clearHistory", "/distribution/new");
  }

  @GetMapping(value = "new/authorization", params = "remove")
  public String newDistAuthorizations(@ModelAttribute(FORM_MODEL_NAME) DistributionFormModel form,
                                      @RequestParam String remove) {
    logger.debug("remove '{}'", remove);

    form
        .getAuthorizations()
        .removeIf(matchesRowId(remove));

    return "redirect:authorization";
  }

  @GetMapping(value = "new/authorization", params = "edit")
  public ModelAndView newDistAuthorizations(@ModelAttribute(FORM_MODEL_NAME) DistributionFormModel form,
                                      @RequestParam String edit,
                                      RedirectAttributes redirAttr) {
    logger.debug("edit '{}'", edit);

    form
        .getAuthorizations()
        .stream()
        .filter(matchesRowId(edit))
        .findAny()
        .ifPresent(authToEdit ->
            redirAttr.addFlashAttribute(AuthorizationController.FORM_MODEL_NAME, authToEdit));

    return new ModelAndView(
        "redirect:/authorization/new",
        "clearHistory",
        "/distribution/new/authorization"
    );
  }

  @PostMapping("new/authorization")
  public String newDistAuthorizationsPost(@Validated(FinalDistributionCheck.class)
                                            @ModelAttribute(FORM_MODEL_NAME) DistributionFormModel form,
                                          BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return DISTRIBUTION_NEW_AUTHORIZATION;
    }

    return "redirect:summary";
  }

  @GetMapping("new/summary")
  public ModelAndView newDistSummary(@ModelAttribute(value = FORM_MODEL_NAME, binding = false) DistributionFormModel form) {
    AuxiliaryProperty auxiliaryProperty = DataInstance.getDataRepos().getAuxiliaryProperty();

    ModelAndView modelAndView = new ModelAndView(DISTRIBUTION_NEW_SUMMARY);

    if (auxiliaryProperty.getWorkflowMode() == AuthorizationType.REQUIRED_REGISTRATION) {
      form.setDemographicsModel(demographicsModelFromFormModel(form));
    }

    return modelAndView;
  }

  @PostMapping("new/summary")
  public ModelAndView newDistSummaryPost(@ModelAttribute(value = FORM_MODEL_NAME, binding = false) DistributionFormModel form,
                                   SessionStatus sessionStatus) {

    ItemPackRepository itemPackRepository = DataInstance.getDataRepos().getItemPackRepository();
    AuxiliaryProperty auxiliaryProperty = DataInstance.getDataRepos().getAuxiliaryProperty();
    RegionRepository regionRepository = DataInstance.getDataRepos().getRegionRepository();
    DistributionRepository distributionRepository = DataInstance.getDataRepos().getDistributionRepository();

    List<Authorization> authToInsert = new ArrayList<>();
    for (AuthorizationFormModel authFormModel : form.getAuthorizations()) {
      Authorization authorization = new Authorization();

      authorization.setRowId(authFormModel.getId());
      authorization.setType(auxiliaryProperty.getWorkflowMode());

      authorization.setAssignItemPackCode(authFormModel.isAssignSpecificBarcode());
      authorization.setItemPack(itemPackRepository.getItemPackByRowId(authFormModel.getItemId()));
      authorization.setItemPackRanges(authFormModel.getBarcodeRange());
      authorization.setExtraFieldEntitlements(authFormModel.getExtraFieldEntitlement());
      authorization.setCustomDeliveryForm(authFormModel.getDeliveryForm());

      authorization.setRules(authFormModel.getBeneficiaryCriteria());
      authorization.setForIndividual(GenerateBy.BENEFICIARY_UNIT != authFormModel.getGenerateBy());

      authorization.setStatus(AuthorizationStatus.ACTIVE);

      if (auxiliaryProperty.getWorkflowMode() == AuthorizationType.REQUIRED_REGISTRATION) {
        authorization.setBeneficiaryRanges(authFormModel.getBeneficiaryIdRange());
      } else if (auxiliaryProperty.getWorkflowMode() == AuthorizationType.OPTIONAL_REGISTRATION) {
        authorization.setVoucherRanges(authFormModel.getBeneficiaryIdRange());
      }

      authToInsert.add(authorization);
    }

    Distribution toInsert = new Distribution();
    toInsert.setAuthorizations(authToInsert);
    toInsert.setLocation(regionRepository.getRegionByRowId(form.getLocationId()));
    toInsert.setName(form.getName());
    toInsert.setSummaryForm(form.getFieldSummaryReport());
    toInsert.setStatus(DistVisitProgStatus.ACTIVE);

    // TODO: these 2 operations should be done in the same transaction
    distributionRepository.saveDistribution(toInsert);
    distExporter.exportDistribution(toInsert);

    sessionStatus.setComplete();

    return new ModelAndView("redirect:/distribution", "clearHistory", "/");
  }

  @GetMapping("view")
  public ModelAndView viewDistList(@RequestParam(defaultValue = "false") boolean removed) {
    DistributionRepository distributionRepository = DataInstance.getDataRepos().getDistributionRepository();
    ModelAndView modelAndView = new ModelAndView(DISTRIBUTION_VIEW_LIST);
    modelAndView.addObject("distList", distributionRepository.getDistributionListDto(removed));
    modelAndView.addObject("statusValues", DistVisitProgStatus.values());
    modelAndView.addObject("removed", removed);

    return modelAndView;
  }

  @GetMapping("view/{distRowId}")
  public ModelAndView viewDist(@PathVariable String distRowId) {
    AuxiliaryProperty auxiliaryProperty = DataInstance.getDataRepos().getAuxiliaryProperty();

    Distribution distribution;
    try {
      distribution = DataInstance
          .getDataRepos()
          .getDistributionRepository()
          .getDistributionByRowId(distRowId);
    } catch (NoResultException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, null, e);
    }

    DistributionFormModel distributionFormModel = new DistributionFormModel();

    distributionFormModel.setName(distribution.getName());
    distributionFormModel.setDescription(distribution.getDescription());
    distributionFormModel.setLocationName(distribution.getLocation().getName());
    distributionFormModel.setFieldSummaryReport(distribution.getSummaryForm());

    List<AuthorizationFormModel> authList = new ArrayList<>();
    for (Authorization authorization : distribution.getAuthorizations()) {
      AuthorizationFormModel authFormModel = new AuthorizationFormModel();
      authList.add(authFormModel);

      authFormModel.setId(authorization.getRowId());
      authFormModel.setItemId(authorization.getItemPack().getRowId());
      authFormModel.setItemName(authorization.getItemPack().getName());
      authFormModel.setDeliveryForm(authorization.getCustomDeliveryForm());
      authFormModel.setExtraFieldEntitlement(authorization.getExtraFieldEntitlements());
      authFormModel.setAssignSpecificBarcode(authorization.isAssignItemPackCode());
      authFormModel.setGenerateBy(authorization.getForIndividual() ?
          GenerateBy.HOUSEHOLD_MEMBER : GenerateBy.BENEFICIARY_UNIT);
      authFormModel.setBarcodeRange(authorization.getVoucherRanges());
      authFormModel.setBeneficiaryCriteria(authorization.getRules());

      if (auxiliaryProperty.getWorkflowMode() == AuthorizationType.REQUIRED_REGISTRATION) {
        authFormModel.setBeneficiaryIdRange(authorization.getBeneficiaryRanges());
      } else if (auxiliaryProperty.getWorkflowMode() == AuthorizationType.OPTIONAL_REGISTRATION) {
        authFormModel.setBeneficiaryIdRange(authorization.getVoucherRanges());
      }

      Stream<String> entitledMembers = EntitlementFilter
          .filter(
              csvRepository,
              authFormModel.getBeneficiaryCriteria(),
              authFormModel.getGenerateBy()
          );

      if (auxiliaryProperty.getWorkflowMode() == AuthorizationType.REQUIRED_REGISTRATION) {
        DemographicsModel demographicsModel = null;
        try {
          demographicsModel = targetDemoDistribution.makeModelFromMember(entitledMembers);
        } catch (InsufficientDemographicsDataException e) {
          logger.debug("Cannot produce demographics distribution: {}", e.getMessage());
        }
        authFormModel.setDemographicsModel(demographicsModel);
      }
    }
    distributionFormModel.setAuthorizations(authList);

    ModelAndView modelAndView = new ModelAndView(DISTRIBUTION_VIEW);
    modelAndView.addObject(FORM_MODEL_NAME, distributionFormModel);

    if (auxiliaryProperty.getWorkflowMode() == AuthorizationType.REQUIRED_REGISTRATION) {
      distributionFormModel.setDemographicsModel(demographicsModelFromFormModel(distributionFormModel));
    }

    return modelAndView;
  }

  @PostMapping("status")
  public ResponseEntity<String> distStatusUpdate(@RequestBody DistributionStatusUpdateFormModel update) {
    DistributionRepository distributionRepository = DataInstance.getDataRepos().getDistributionRepository();
    // TODO:
    Optional<Long> distId = distributionRepository
        .getAllDistributions()
        .stream()
        .filter(d -> update.getRowId().equals(d.getRowId()))
        .findAny()
        .map(Distribution::getId);

    distId.ifPresent(id -> distributionRepository.updateDistributionStatus(id, update.getStatus()));
    return ResponseEntity.ok().build();
  }

  @PostMapping("template")
  public ResponseEntity<String> saveAsTemplatePost(@RequestBody ToTemplateFormModel toTemplate) throws IOException, JSONException {
    DistributionRepository distributionRepository = DataInstance.getDataRepos().getDistributionRepository();
    RcTemplateRepository rcTemplateRepository = DataInstance.getDataRepos().getRcTemplateRepository();

    Distribution distributionByRowId = distributionRepository
        .getDistributionByRowId(toTemplate.getRowId());

    RcTemplate template = new RcDistributionTemplate(toTemplate.getTemplateName(), distributionByRowId).getTemplate();
    rcTemplateRepository.saveRcTemplate(template);

    return ResponseEntity.ok().build();
  }

  private static Predicate<AuthorizationFormModel> matchesRowId(@NonNull String rowId) {
    return authFormModel -> rowId.equals(authFormModel.getId());
  }

  private DemographicsModel demographicsModelFromFormModel(DistributionFormModel formModel) {
    Stream<String> memberRowIds = formModel
        .getAuthorizations()
        .stream()
        .flatMap(auth -> EntitlementFilter.filter(
            csvRepository,
            auth.getBeneficiaryCriteria(),
            auth.getGenerateBy()
        ))
        .distinct();

    try {
      return targetDemoDistribution.makeModelFromMember(memberRowIds);
    } catch (InsufficientDemographicsDataException e) {
      logger.debug("Cannot produce demographics distribution: {}", e.getMessage());
      return null;
    }
  }
}
