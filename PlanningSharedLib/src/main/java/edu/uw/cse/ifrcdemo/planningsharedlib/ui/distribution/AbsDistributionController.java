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

package edu.uw.cse.ifrcdemo.planningsharedlib.ui.distribution;

import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Authorization;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Distribution;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.RcTemplate;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.GenerateBy;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.distribution.DistributionRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.item.ItemRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.location.LocationRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.rctemplate.RcTemplateRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.authorization.AuthorizationFormModel;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.demographics.DemographicsModel;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.demographics.InsufficientDemographicsDataException;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.demographics.TargetDemoDistribution;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.location.LocationListModel;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.rctemplate.TemplateSelectFormModel;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.rctemplate.ToTemplateFormModel;
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
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
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

public abstract class AbsDistributionController {
    public static final String FORM_MODEL_NAME = "newDistForm";

    protected static final String DISTRIBUTION_MENU_TEMPLATE = "distribution/distributionMenu";
    protected static final String DISTRIBUTION_NEW_BASIC_INFO_TEMPLATE = "distribution/newDistributionBasicInfo";
    protected static final String DISTRIBUTION_NEW_AUTHORIZATION = "distribution/newDistributionAuthorizations";
    protected static final String DISTRIBUTION_NEW_SUMMARY = "distribution/newDistributionSummary";
    protected static final String DISTRIBUTION_VIEW_LIST = "distribution/viewDistributionList";
    protected static final String DISTRIBUTION_VIEW = "distribution/viewDistribution";

    protected final TargetDemoDistribution targetDemoDistribution;
    protected final CsvRepository csvRepository;
    protected final Logger logger;

    protected abstract AuxiliaryProperty getAuxiliaryProperty();
    protected abstract DistributionRepository getDistributionRepository();
    protected abstract RcTemplateRepository getRcTemplateRepository();
    protected abstract ItemRepository getItemRepository();
    protected abstract LocationRepository getLocationRepository();


    public AbsDistributionController(TargetDemoDistribution targetDemoDistribution,
                                  CsvRepository csvRepository,
                                  Logger logger) {
        this.targetDemoDistribution = targetDemoDistribution;
        this.csvRepository = csvRepository;
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
        List<LocationListModel> locList = getLocationRepository()
                .getAllLocations()
                .stream()
                .map(LocationListModel::new)
                .collect(Collectors.toList());

        modelAndView.addObject("locList", locList);

        List<RcTemplate> distTemplates = getRcTemplateRepository()
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
        LocationRepository locationRepository = getLocationRepository();
        RcTemplateRepository rcTemplateRepository = getRcTemplateRepository();
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView(DISTRIBUTION_NEW_BASIC_INFO_TEMPLATE);

            // TODO: cache this
            List<LocationListModel> locList = locationRepository
                    .getAllLocations()
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

        String locName = locationRepository
                .getLocationByRowId(form.getLocationId())
                .getName();

        form.setLocationName(locName);

        return new ModelAndView("redirect:new/authorization");
    }

    @PostMapping(value = "new", params = "location")
    public String newDistBasicInfoPost(String location,
                                       @ModelAttribute(FORM_MODEL_NAME) DistributionFormModel form) {
        return "redirect:/location/new/dist";
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
        AuxiliaryProperty auxiliaryProperty = getAuxiliaryProperty();

        ModelAndView modelAndView = new ModelAndView(DISTRIBUTION_NEW_SUMMARY);

        if (auxiliaryProperty.getWorkflowMode() == AuthorizationType.REQUIRED_REGISTRATION) {
            form.setDemographicsModel(demographicsModelFromFormModel(form));
        }

        return modelAndView;
    }

    protected Distribution newDistSummaryPostHelper(@ModelAttribute(value = FORM_MODEL_NAME, binding = false) DistributionFormModel form) {
        ItemRepository itemRepository = getItemRepository();
        AuxiliaryProperty auxiliaryProperty = getAuxiliaryProperty();
        LocationRepository locationRepository = getLocationRepository();;

        List<Authorization> authToInsert = new ArrayList<>();
        for (AuthorizationFormModel authFormModel : form.getAuthorizations()) {
            Authorization authorization = new Authorization();

            authorization.setRowId(authFormModel.getId());
            authorization.setType(auxiliaryProperty.getWorkflowMode());

            authorization.setAssignItemCode(authFormModel.isAssignSpecificBarcode());
            authorization.setItem(itemRepository.getItemByRowId(authFormModel.getItemId()));
            authorization.setItemRanges(authFormModel.getBarcodeRange());
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
        toInsert.setLocation(locationRepository.getLocationByRowId(form.getLocationId()));
        toInsert.setName(form.getName());
        toInsert.setSummaryForm(form.getFieldSummaryReport());
        toInsert.setStatus(DistVisitProgStatus.ACTIVE);

        return toInsert;
    }

    @GetMapping("view")
    public ModelAndView viewDistList(@RequestParam(defaultValue = "false") boolean removed) {
        DistributionRepository distributionRepository = getDistributionRepository();
        ModelAndView modelAndView = new ModelAndView(DISTRIBUTION_VIEW_LIST);
        modelAndView.addObject("distList", distributionRepository.getDistributionListDto(removed));
        modelAndView.addObject("statusValues", DistVisitProgStatus.values());
        modelAndView.addObject("removed", removed);

        return modelAndView;
    }


    @PostMapping("status")
    public ResponseEntity<String> distStatusUpdate(@RequestBody DistributionStatusUpdateFormModel update) {
    // TODO:
        DistributionRepository distributionRepository = getDistributionRepository();
        Optional<Long> distId = distributionRepository
                .getAllDistributions()
                .stream()
                .filter(d -> update.getRowId().equals(d.getRowId()))
                .findAny()
                .map(Distribution::getId);

        distId.ifPresent(id -> distributionRepository.updateDistributionStatus(id, update.getStatus()));
        return ResponseEntity.ok().build();
    }

    protected static Predicate<AuthorizationFormModel> matchesRowId(@NonNull String rowId) {
        return authFormModel -> rowId.equals(authFormModel.getId());
    }

    abstract protected DemographicsModel demographicsModelFromFormModel(DistributionFormModel formModel);
}
