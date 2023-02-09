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

package edu.uw.cse.ifrcdemo.healthplan.ui.authorization;

import edu.uw.cse.ifrcdemo.healthplan.logic.EntitlementFilter;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.AuthorizationCriterion;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.authorization.AbsAuthorizationController;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.demographics.TargetDemoDistribution;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.formatter.CriteriaFormatter;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.formatter.RangeFormatter;
import edu.uw.cse.ifrcdemo.healthplan.util.BeneficiaryUtil;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.CriteriaUtil;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.GenerateBy;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.CriterionField;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.CriterionOperator;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.item.ItemRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.authorization.AuthorizationFormModel;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.demographics.DemographicsModel;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.demographics.InsufficientDemographicsDataException;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.item.ItemListModel;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/authorization")
@SessionAttributes(types = {
        AuthorizationFormModel.class
})
public class AuthorizationController extends AbsAuthorizationController {


    public AuthorizationController(ItemRepository itemRepository,
                                   CsvRepository csvRepository,
                                   AuxiliaryProperty auxiliaryProperty,
                                   TargetDemoDistribution targetDemoDistribution,
                                   CriteriaFormatter criteriaFormatter,
                                   RangeFormatter rangeFormatter,
                                   Logger logger) {
        super(itemRepository,csvRepository,auxiliaryProperty,targetDemoDistribution,criteriaFormatter,rangeFormatter,logger);
    }


    @PostMapping("new/criteria")
    @ResponseBody
    public String criteriaUpdate(@RequestBody List<List<AuthorizationCriterion>> criteria, @RequestParam GenerateBy generateBy) {
        long count = EntitlementFilter
                .filter(csvRepository, criteria, generateBy)
                .count();

        return Long.toString(count);
    }

    @PostMapping("new/recipient")
    public ResponseEntity<String> showRecipientListPost(@RequestBody List<List<AuthorizationCriterion>> criteria,
                                                        @RequestParam GenerateBy generateBy) throws IOException {
        List<String> ids = EntitlementFilter
                .filter(csvRepository, criteria, generateBy)
                .collect(Collectors.toList());

        Path outputPath = Files.createTempFile(ModuleConsts.PLANNING, GenConsts.CSV_FILE_EXTENSION);

        BeneficiaryUtil.writeDataForMemberAsCsv(ids, outputPath, csvRepository);

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

    @GetMapping("new/summary")
    public String newAuthSummary(@ModelAttribute(value = FORM_MODEL_NAME, binding = false) AuthorizationFormModel form) {
        if (auxiliaryProperty.getWorkflowMode() == AuthorizationType.REQUIRED_REGISTRATION) {
            Stream<String> memberRowIds = EntitlementFilter.filter(
                    csvRepository,
                    form.getBeneficiaryCriteria(),
                    form.getGenerateBy()
            );

            DemographicsModel demoModel = null;
            try {
                demoModel = targetDemoDistribution.makeModelFromMember(memberRowIds);
            } catch (InsufficientDemographicsDataException e) {
                logger.debug("Cannot produce demographics distribution: {}", e.getMessage());
            }

            form.setDemographicsModel(demoModel);
        }

        return AUTHORIZATION_NEW_SUMMARY;
    }
}