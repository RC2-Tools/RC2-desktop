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

package edu.uw.cse.ifrcdemo.planningsharedlib.ui.authorization;

import edu.uw.cse.ifrcdemo.planningsharedlib.model.GenerateBy;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.AuthorizationCriterion;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.CriterionField;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.CriterionOperator;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.item.ItemRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.demographics.DemographicsModel;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.demographics.InsufficientDemographicsDataException;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.demographics.TargetDemoDistribution;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.formatter.CriteriaFormatter;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.formatter.RangeFormatter;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.item.ItemListModel;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.CriteriaUtil;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.FxDialogUtil;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.sharedlib.consts.ModuleConsts;
import edu.uw.cse.ifrcdemo.sharedlib.model.config.AuxiliaryProperty;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationType;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.ExtraFieldEntitlements;
import edu.uw.cse.ifrcdemo.translations.LogStr;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import javafx.scene.control.ButtonType;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/authorization")
@SessionAttributes(types = {
        AuthorizationFormModel.class
})
public abstract class AbsAuthorizationController {
    public static final String FORM_MODEL_NAME = "newAuthForm";
    public static final String NEW_AUTH_FLASH_ATTR = "newAuth";

    protected static final String DISTRIBUTION_NEW_ITEM_DELIVERY = "authorization/newAuthItemDelivery";
    protected static final String DISTRIBUTION_NEW_BENEFICIARY = "authorization/newAuthBeneficiary";
    protected static final String AUTHORIZATION_NEW_SUMMARY = "authorization/newAuthSummary";

    protected final ItemRepository itemRepository;
    protected final CsvRepository csvRepository;
    protected final AuxiliaryProperty auxiliaryProperty;
    protected final TargetDemoDistribution targetDemoDistribution;

    protected final CriteriaFormatter criteriaFormatter;
    protected final RangeFormatter rangeFormatter;

    protected final Logger logger;

    public AbsAuthorizationController(ItemRepository itemRepository,
                                   CsvRepository csvRepository,
                                   AuxiliaryProperty auxiliaryProperty,
                                   TargetDemoDistribution targetDemoDistribution,
                                   CriteriaFormatter criteriaFormatter,
                                   RangeFormatter rangeFormatter,
                                   Logger logger) {
        this.itemRepository = itemRepository;
        this.csvRepository = csvRepository;
        this.auxiliaryProperty = auxiliaryProperty;
        this.targetDemoDistribution = targetDemoDistribution;
        this.criteriaFormatter = criteriaFormatter;
        this.rangeFormatter = rangeFormatter;
        this.logger = logger;
    }

    @InitBinder
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.setDisallowedFields("id");
        webDataBinder.setDisallowedFields("itemName");
        webDataBinder.setDisallowedFields("demographicsModel");
        webDataBinder.addCustomFormatter(criteriaFormatter, "beneficiaryCriteria");
        webDataBinder.addCustomFormatter(rangeFormatter, "barcodeRange", "beneficiaryIdRange");
    }

    @ModelAttribute(FORM_MODEL_NAME)
    public AuthorizationFormModel newAuthorizationFormModel() {
        AuthorizationFormModel authorizationFormModel = new AuthorizationFormModel();

        authorizationFormModel.setGenerateBy(GenerateBy.HOUSEHOLD_MEMBER);

        return authorizationFormModel;
    }

    @GetMapping("new")
    public ModelAndView newAuthItemDelivery(@ModelAttribute(FORM_MODEL_NAME) AuthorizationFormModel form) {
        return getModelAndViewForAuthItemDelivery();
    }

    @PostMapping("new")
    public ModelAndView newAuthItemDeliveryPost(@Valid @ModelAttribute(FORM_MODEL_NAME) AuthorizationFormModel form,
                                                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return getModelAndViewForAuthItemDelivery();
        }

        if (form.getDeliveryForm() == null) {
            ResourceBundle translations = TranslationUtil.getTranslations();
            ButtonType result = null;
            try {
                result = FxDialogUtil.showConfirmDialogAndWait(translations.getString(TranslationConsts.WARNING_DIALOG_TITLE),
                        translations.getString(TranslationConsts.DELIVERY_FORM_NOT_USED_WARNING), null);
            } catch (ExecutionException e) {
                logger.error(LogStr.LOG_FAILED_TO_CREATE_FX_DIALOG + '\n'
                        + ExceptionUtils.getStackTrace(e));
            } catch (InterruptedException e) {
                logger.error(LogStr.LOG_FAILED_TO_CREATE_FX_DIALOG + '\n'
                        + ExceptionUtils.getStackTrace(e));
            }
            if (result != ButtonType.OK) {
                return getModelAndViewForAuthItemDelivery();
            }
        }

        String itemName = itemRepository.getItemByRowId(form.getItemId()).getName();
        form.setItemName(itemName);

        return new ModelAndView("redirect:new/beneficiary");
    }

    @PostMapping(value = "new", params = "item")
    public String newAuthItemDeliveryPost(String item,
                                          @ModelAttribute(FORM_MODEL_NAME) AuthorizationFormModel form) {
        return "redirect:/item/new/auth";
    }

    private ModelAndView getModelAndViewForAuthItemDelivery() {
        ModelAndView modelAndView = new ModelAndView(DISTRIBUTION_NEW_ITEM_DELIVERY);

        List<ItemListModel> items = itemRepository.getItemList().stream().map(ItemListModel::new)
                .collect(Collectors.toList());

        modelAndView.addObject("itemList", items);
        modelAndView.addObject("extraFieldEnt", ExtraFieldEntitlements.values());

        return modelAndView;
    }

    @GetMapping("new/beneficiary")
    public ModelAndView newAuthBeneficiary(@ModelAttribute(FORM_MODEL_NAME) AuthorizationFormModel form) {
        ModelAndView modelAndView = new ModelAndView(DISTRIBUTION_NEW_BENEFICIARY);

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

    @PostMapping("new/beneficiary")
    public ModelAndView newAuthBeneficiaryPost(@ModelAttribute(FORM_MODEL_NAME) AuthorizationFormModel form)
            throws ExecutionException, InterruptedException {
        if (CriteriaUtil.removeEmptyRules(form.getBeneficiaryCriteria()).isEmpty()) {
            ResourceBundle translations = TranslationUtil.getTranslations();

            ButtonType buttonType = FxDialogUtil.showConfirmDialogAndWait(
                    translations.getString(TranslationConsts.WARNING_DIALOG_TITLE),
                    translations.getString(TranslationConsts.WARNING_DIALOG_TITLE),
                    translations.getString(TranslationConsts.CRITERIA_EMPTY_WARNING)
            );

            if (buttonType != ButtonType.OK) {
                return newAuthBeneficiary(form);
            }
        }

        return new ModelAndView("redirect:summary");
    }

    @PostMapping("new/summary")
    public String newAuthSummaryPost(@ModelAttribute(value = FORM_MODEL_NAME, binding = false) AuthorizationFormModel form,
                                     SessionStatus sessionStatus,
                                     RedirectAttributes redirectAttributes) {
        form.setId(UUID.randomUUID().toString());
        redirectAttributes.addFlashAttribute(NEW_AUTH_FLASH_ATTR, form);

        sessionStatus.setComplete();

        return "redirect:/distribution/new/authorization";
    }

    @PostMapping("new/cancel")
    public String cancelNewAuthPost(SessionStatus sessionStatus) {
        sessionStatus.setComplete();

        return "redirect:/distribution/new/authorization";
    }
}
