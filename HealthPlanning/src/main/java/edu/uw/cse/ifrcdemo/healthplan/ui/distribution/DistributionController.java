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

package edu.uw.cse.ifrcdemo.healthplan.ui.distribution;

import edu.uw.cse.ifrcdemo.healthplan.data.HealthDataInstance;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Authorization;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Distribution;
import edu.uw.cse.ifrcdemo.healthplan.logic.EntitlementFilter;
import edu.uw.cse.ifrcdemo.healthplan.model.distribution.DistExporter;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.distribution.DistributionRepository;
import edu.uw.cse.ifrcdemo.healthplan.model.rctemplate.RcDistributionTemplate;
import edu.uw.cse.ifrcdemo.healthplan.ui.authorization.AuthorizationController;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.authorization.AuthorizationFormModel;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.demographics.TargetDemoDistribution;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.distribution.AbsDistributionController;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.rctemplate.TemplateSelectFormModel;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.rctemplate.ToTemplateFormModel;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.RcTemplate;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.GenerateBy;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.item.ItemRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.rctemplate.RcTemplateRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.location.LocationRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.demographics.DemographicsModel;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.demographics.InsufficientDemographicsDataException;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.distribution.DistributionFormModel;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.distribution.DistributionStatusUpdateFormModel;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.distribution.FinalDistributionCheck;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.location.LocationListModel;
import edu.uw.cse.ifrcdemo.sharedlib.model.config.AuxiliaryProperty;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationType;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.DistVisitProgStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.RcTemplateType;
import org.apache.logging.log4j.Logger;
import org.apache.wink.json4j.JSONException;
import org.springframework.boot.actuate.health.Health;
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
public class DistributionController extends AbsDistributionController {
    private final DistExporter distExporter;

    public DistributionController(DistExporter distExporter,
                                  TargetDemoDistribution targetDemoDistribution,
                                  CsvRepository csvRepository,
                                  Logger logger) {
        super(targetDemoDistribution, csvRepository, logger);
        this.distExporter = distExporter;
    }

    @Override
    protected AuxiliaryProperty getAuxiliaryProperty() {
        return HealthDataInstance.getDataRepos().getAuxiliaryProperty();
    }

    @Override
    protected DistributionRepository getDistributionRepository() {
        return HealthDataInstance.getDataRepos().getDistributionRepository();
    }

    @Override
    protected ItemRepository getItemRepository() {
        return HealthDataInstance.getDataRepos().getItemRepository();
    }

    @Override
    protected RcTemplateRepository getRcTemplateRepository() {
        return HealthDataInstance.getDataRepos().getRcTemplateRepository();
    }

    @Override
    protected LocationRepository getLocationRepository() {
        return HealthDataInstance.getDataRepos().getLocationRepository();
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

    @PostMapping("new/summary")
    public ModelAndView newDistSummaryPost(@ModelAttribute(value = FORM_MODEL_NAME, binding = false) DistributionFormModel form,
                                           SessionStatus sessionStatus) {

        Distribution toInsert = super.newDistSummaryPostHelper(form);

        // TODO: these 2 operations should be done in the same transaction
        getDistributionRepository().saveDistribution(toInsert);
        distExporter.exportDistribution(toInsert);

        sessionStatus.setComplete();

        return new ModelAndView("redirect:/distribution", "clearHistory", "/");
    }

    @GetMapping("view/{distRowId}")
    public ModelAndView viewDist(@PathVariable String distRowId) {
        AuxiliaryProperty auxiliaryProperty = HealthDataInstance.getDataRepos().getAuxiliaryProperty();

        Distribution distribution;
        try {
            distribution = HealthDataInstance
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
            authFormModel.setItemId(authorization.getItem().getRowId());
            authFormModel.setItemName(authorization.getItem().getName());
            authFormModel.setDeliveryForm(authorization.getCustomDeliveryForm());
            authFormModel.setExtraFieldEntitlement(authorization.getExtraFieldEntitlements());
            authFormModel.setAssignSpecificBarcode(authorization.isAssignItemCode());
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

    // TODO move template to shared library
    @PostMapping("template")
    public ResponseEntity<String> saveAsTemplatePost(@RequestBody ToTemplateFormModel toTemplate) throws IOException, JSONException {
        DistributionRepository distributionRepository = HealthDataInstance.getDataRepos().getDistributionRepository();
        RcTemplateRepository rcTemplateRepository = HealthDataInstance.getDataRepos().getRcTemplateRepository();

        Distribution distributionByRowId = distributionRepository
                .getDistributionByRowId(toTemplate.getRowId());

        RcTemplate template = new RcDistributionTemplate(toTemplate.getTemplateName(), distributionByRowId).getTemplate();
        rcTemplateRepository.saveRcTemplate(template);

        return ResponseEntity.ok().build();
    }

    @Override
    protected DemographicsModel demographicsModelFromFormModel(DistributionFormModel formModel) {
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

