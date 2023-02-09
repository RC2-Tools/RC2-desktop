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

package edu.uw.cse.ifrcdemo.planningsharedlib.ui.rctemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Authorization;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Distribution;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.AuthorizationCriterion;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.distribution.DistributionRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.item.ItemRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.location.LocationRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.authorization.AuthorizationFormModel;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.demographics.TargetDemoDistribution;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.distribution.DistributionFormModel;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.RcTemplate;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.GenerateBy;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.range.Range;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.rctemplate.RcTemplateRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.demographics.InsufficientDemographicsDataException;
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
public abstract class AbsRcTemplateController {
    protected static final String FORM_MODEL = "templateList";
    protected static final String TEMPLATE_LIST_TYPE = "type";
    protected static final String VIEW_TEMPLATES = "rctemplate/viewTemplates";
    protected static final String VIEW_DIST_TEMPLATE = "rctemplate/viewDistributionTemplate";
    protected static final String VIEW_VISIT_TEMPLATE = "rctemplate/viewVisitTemplate";

    protected final TargetDemoDistribution targetDemoDistribution;
    protected final ObjectMapper objectMapper;
    protected final Logger logger;

    protected abstract AuxiliaryProperty getAuxiliaryProperty();
    protected abstract RcTemplateRepository getRcTemplateRepository();
    protected abstract void generateTemplateZip(RcTemplate distTemplate, Path selectedOutputDir) throws IOException, JSONException, IllegalArgumentException;
    protected abstract void importTemplate(File templateZip) throws IOException, IllegalArgumentException, JSONException;

    public AbsRcTemplateController(TargetDemoDistribution targetDemoDistribution,
                                ObjectMapper objectMapper, Logger logger) {
        this.targetDemoDistribution = targetDemoDistribution;
        this.objectMapper = objectMapper;
        this.logger = logger;
    }

    @GetMapping("/view/distribution")
    public ModelAndView viewDistTemplates() {
        ModelAndView modelAndView = new ModelAndView(VIEW_TEMPLATES);

        modelAndView.addObject(FORM_MODEL, getTemplatesAsFormModel(RcTemplateType.DISTRIBUTION));
        modelAndView.addObject(TEMPLATE_LIST_TYPE, "DistributionTemplate");

        return modelAndView;
    }

    @GetMapping("/view/visit")
    public ModelAndView viewVisitTemplates() {
        ModelAndView modelAndView = new ModelAndView(VIEW_TEMPLATES);

        modelAndView.addObject(FORM_MODEL, getTemplatesAsFormModel(RcTemplateType.VISIT_PROGRAM));
        modelAndView.addObject(TEMPLATE_LIST_TYPE, "VisitProgramTemplate");

        return modelAndView;
    }

    protected List<ToTemplateFormModel> getTemplatesAsFormModel(RcTemplateType rcTemplateType) {
        return getRcTemplateRepository()
                .getRcTemplatesByType(rcTemplateType)
                .join()
                .stream()
                .map(rcTemplate -> new ToTemplateFormModel(rcTemplate.getName(), rcTemplate.getRowId()))
                .collect(Collectors.toList());
    }

    protected String rangeListToString(List<Range> ranges) throws JsonProcessingException {
        return objectMapper
                .writerFor(new TypeReference<List<Range>>() {})
                .writeValueAsString(ranges);
    }

    protected String criteriaToString(List<List<AuthorizationCriterion>> criteria) throws JsonProcessingException {
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

            RcTemplate distTemplate = getRcTemplateRepository()
                    .findRcTemplate(templateId);

            if (distTemplate != null) {
                generateTemplateZip(distTemplate, selectedOutputDir);
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
                    importTemplate(template);
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

