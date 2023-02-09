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

package edu.uw.cse.ifrcdemo.healthplan.ui.healthservice;

import edu.uw.cse.ifrcdemo.healthplan.entity.HealthService;
import edu.uw.cse.ifrcdemo.healthplan.model.HealthServiceRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.FxDialogUtil;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.PersistenceException;
import javax.validation.Valid;
import org.apache.logging.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/healthservice")
public class HealthServiceController {
  public static final String HEALTHSERVICE = "healthservice";
  private static final String HEALTH_SERVICE_MENU = HEALTHSERVICE + "/healthServiceMenu";
  private static final String NEW_HEALTH_SERVICE = HEALTHSERVICE + "/newHealthService";
  private static final String VIEW_HEALTH_SERVICE = HEALTHSERVICE + "/viewHealthServices";
  private static final String EDIT_HEALTH_SERVICE = HEALTHSERVICE + "/editHealthService";
  public static final String REDIRECT = "redirect:/";


  private final HealthServiceRepository healthServiceRepository;
  private final Logger logger;

  public HealthServiceController(HealthServiceRepository healthServiceRepository, Logger logger) {
    this.healthServiceRepository = healthServiceRepository;
    this.logger = logger;
  }

  @GetMapping("")
  public String healthServiceMenu() {
    return HEALTH_SERVICE_MENU;
  }

  @GetMapping("new/healthservice")
  public ModelAndView newHealthService() {
    return new ModelAndView(NEW_HEALTH_SERVICE).addObject("newHealthServiceForm", new HealthServiceFormModel());
  }


  // TODO: restrict page values
  @PostMapping("new/{page}")
  public ModelAndView newHealthServicePost(@ModelAttribute("newHealthServiceForm") HealthServiceFormModel form, @PathVariable String page,
                                           SessionStatus sessionStatus) {
    // a service form is required
    if(form.getServiceForm() == null) {
      FxDialogUtil.showWarningDialog("MISSING SERVICE FORM", "A form for the Service is required");
      ModelAndView modelAndView = new ModelAndView(NEW_HEALTH_SERVICE);
      modelAndView.addObject("newHealthServiceForm", form);

      return modelAndView;
    }

    HealthService newHealthService = new HealthService();
    newHealthService.setName(form.getName());
    newHealthService.setDescription(form.getDescription());
    newHealthService.setEndWithReferrals(form.getEndWithReferrals());
    newHealthService.setRequiresReferral(form.getRequiresReferral());
    newHealthService.setServiceFormId(form.getServiceForm().getFormId());
    newHealthService.setServiceTableId((form.getServiceForm().getTableId()));
    // referral form not required
    if(form.getReferralForm() != null) {
      newHealthService.setReferralFormId(form.getServiceForm().getFormId());
      newHealthService.setReferralTableId(form.getServiceForm().getTableId());
    }
    healthServiceRepository.saveHealthService(newHealthService).join();

    sessionStatus.setComplete();
    if (page.equals(HEALTHSERVICE)) {
      FxDialogUtil.showInfoDialog(String
              .format(TranslationUtil.getTranslations().getString(TranslationConsts.CREATED_HEALTH_SERVICE),
                      form.getName()));
      return new ModelAndView(REDIRECT + HEALTHSERVICE, "clearHistory", "/");
    } else {
      return new ModelAndView(REDIRECT);
    }
  }

  @GetMapping("view")
  public ModelAndView listHealthServices() {
    List<HealthServiceListModel> healthServices = healthServiceRepository
        .getAllHealthServices()
        .stream()
        .map(HealthServiceListModel::new)
        .collect(Collectors.toList());

    ModelAndView modelAndView = new ModelAndView(VIEW_HEALTH_SERVICE);
    modelAndView.addObject("healthServicesList", healthServices);
    modelAndView.addObject(new EditHealthServiceFormModel());

    return modelAndView;
  }

  @GetMapping("edit/{rowId}")
  public ModelAndView editHealthService(@PathVariable("rowId") String rowId) {
    EditHealthServiceFormModel editHealthServiceFormModel = new EditHealthServiceFormModel();

    HealthService healthServiceByRowId = healthServiceRepository.getHealthServiceByRowId(rowId);
    editHealthServiceFormModel.setName(healthServiceByRowId.getName());
    editHealthServiceFormModel.setDescription(healthServiceByRowId.getDescription());
    editHealthServiceFormModel.setEndWithReferrals(healthServiceByRowId.getEndWithReferrals());
    editHealthServiceFormModel.setRequiresReferral(healthServiceByRowId.getRequiresReferral());

    ModelAndView modelAndView = new ModelAndView(EDIT_HEALTH_SERVICE);
    modelAndView.addObject(editHealthServiceFormModel);

    return modelAndView;
  }

  @PostMapping("edit/{rowId}")
  public ModelAndView editHealthServicePost(@PathVariable("rowId") String rowId,
                                       @Valid @ModelAttribute EditHealthServiceFormModel editHealthServiceFormModel,
                                       BindingResult bindingResult) {

    if (!bindingResult.hasErrors()) {
      try {
        healthServiceRepository.editHealthService(rowId,editHealthServiceFormModel);

        return new ModelAndView("redirect:/healthservice/view", "clearHistory", "/healthservice/view");
      } catch (PersistenceException e) {
        if (!(e.getCause() instanceof ConstraintViolationException)) {
          throw e;
        }

        if (e.getCause() instanceof ConstraintViolationException) {
          FieldError fieldError = new FieldError(
              "editHealthServiceFormModel",
              "name",
                  editHealthServiceFormModel.getName(),
              false,
              null,
              null,
              TranslationUtil.getTranslations().getString(TranslationConsts.REGION_UNIQ_NAME_ERROR)
          );
          bindingResult.addError(fieldError);
        }
      }
    }

    ModelAndView modelAndView = new ModelAndView(EDIT_HEALTH_SERVICE);
    modelAndView.addObject(editHealthServiceFormModel);

    return modelAndView;
  }
}
