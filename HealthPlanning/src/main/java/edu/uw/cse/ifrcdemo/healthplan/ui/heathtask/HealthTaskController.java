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

package edu.uw.cse.ifrcdemo.healthplan.ui.heathtask;

import edu.uw.cse.ifrcdemo.healthplan.data.HealthDataInstance;
import edu.uw.cse.ifrcdemo.healthplan.entity.HealthTask;
import edu.uw.cse.ifrcdemo.healthplan.entity.ServicesForProgram;
import edu.uw.cse.ifrcdemo.healthplan.logic.HealthTaskStatus;
import edu.uw.cse.ifrcdemo.healthplan.model.HealthServicesForTaskRespository;
import edu.uw.cse.ifrcdemo.healthplan.model.HealthTaskRepository;
import edu.uw.cse.ifrcdemo.healthplan.ui.formatter.HealthServiceFormatter;
import edu.uw.cse.ifrcdemo.healthplan.ui.healthservice.HealthServiceListModel;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.location.LocationRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.location.LocationListModel;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.PersistenceException;
import javax.validation.Valid;
import org.apache.logging.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/healthtask")
@SessionAttributes(types = {
        HealthTaskFormModel.class
})
public class HealthTaskController {
  public static final String HEALTHTASK = "healthtask";
  private static final String HEALTH_TASK_MENU = HEALTHTASK + "/healthTaskMenu";
  public static final String NEW_HEALTH_TASK_START = HEALTHTASK + "/newHealthTaskStart";
  public static final String NEW_HEALTH_TASK_SERVICES = HEALTHTASK + "/newHealthTaskServices";
  private static final String VIEW_HEALTH_TASK = HEALTHTASK + "/viewHealthTasks";
  private static final String EDIT_HEALTH_TASK = HEALTHTASK + "/editHealthTask";
   public static final String REDIRECT = "redirect:/";
  public static final String NEW_HEALTH_TASK_FORM = "newHealthTaskForm";

  private final Logger logger;
  private final HealthTaskRepository healthTaskRepository;
  private final HealthServiceFormatter healthServiceFormatter;

  public HealthTaskController(HealthTaskRepository healthTaskRepository, Logger logger) {
    this.healthTaskRepository = healthTaskRepository;
    this.logger = logger;
    this.healthServiceFormatter = new HealthServiceFormatter();
  }


  @ModelAttribute(NEW_HEALTH_TASK_FORM)
  public HealthTaskFormModel newHealthTaskFormModel() {
    HealthTaskFormModel healthTaskFormModel = new HealthTaskFormModel();
    healthTaskFormModel.setHealthServiceList(new ArrayList<>());

    return healthTaskFormModel;
  }

  @GetMapping("")
  public String healthTaskMenu() {
    return HEALTH_TASK_MENU;
  }

  @GetMapping("new")
  public ModelAndView newHealthTask(@ModelAttribute(NEW_HEALTH_TASK_FORM) HealthTaskFormModel form) {
    ModelAndView modelAndView = new ModelAndView(NEW_HEALTH_TASK_START);

    List<LocationListModel> locList = HealthDataInstance
            .getDataRepos()
            .getLocationRepository()
            .getAllLocations()
            .stream()
            .map(LocationListModel::new)
            .collect(Collectors.toList());

    modelAndView.addObject("locList", locList);

    return modelAndView;
  }


  // TODO: restrict page values
  @PostMapping("new")
  public ModelAndView newHealthTaskStartPost(@Valid @ModelAttribute(NEW_HEALTH_TASK_FORM) HealthTaskFormModel form,
                                       BindingResult bindingResult) {

    LocationRepository locationRepository = HealthDataInstance.getDataRepos().getLocationRepository();

    if (bindingResult.hasErrors()) {
      ModelAndView modelAndView = new ModelAndView(NEW_HEALTH_TASK_START);

      // TODO: cache this
      List<LocationListModel> locList = locationRepository
              .getAllLocations()
              .stream()
              .map(LocationListModel::new)
              .collect(Collectors.toList());

      modelAndView.addObject("locList", locList);

      return modelAndView;
    }

    String locId = form.getLocationId();
    if(locId != null && !GenConsts.EMPTY_STRING.equals(locId)) {
      form.setLocationName(locationRepository
              .getLocationByRowId(form.getLocationId())
              .getName());
    }

    return new ModelAndView(NEW_HEALTH_TASK_SERVICES);
  }

  @GetMapping("newHealthTaskServices")
  public ModelAndView newHealthTaskServicesPost(@ModelAttribute(NEW_HEALTH_TASK_FORM) HealthTaskFormModel form,
                                                @RequestParam("remove") Optional<String> serviceID) {
    // remove the service from the list if the parameter {remove=?} is passed in
    if (serviceID.isPresent()) {
      form.getHealthServiceList().removeIf(healthService -> healthService.getId().equals(serviceID.get()));
    }
    return new ModelAndView(NEW_HEALTH_TASK_SERVICES);
  }

  @PostMapping("newHealthTaskServices")
  public ModelAndView newHealthTaskServicesPost(@Valid @ModelAttribute(NEW_HEALTH_TASK_FORM) HealthTaskFormModel form,
                                                BindingResult bindingResult, SessionStatus sessionStatus) {

    HealthTaskRepository healthTaskRepository = HealthDataInstance.getDataRepos().getHealthTaskRepository();
    HealthServicesForTaskRespository hsftRepo = HealthDataInstance.getDataRepos().getHealthServicesForTaskRespository();

    String newHealthTaskRowId = UUID.randomUUID().toString();

    HealthTask healthTask = new HealthTask();
    healthTask.setRowId(newHealthTaskRowId);
    healthTask.setName(form.getName());
    healthTask.setDescription(form.getDescription());
    healthTask.setLocationId(form.getLocationId());
    healthTask.setLocationName(form.getLocationName());
    healthTask.setStatus(HealthTaskStatus.ENABLED.name());

    healthTaskRepository.saveHealthTask(healthTask).join();

    for(HealthServiceListModel service : form.getHealthServiceList()) {
      ServicesForProgram sfp = new ServicesForProgram();
      sfp.setServiceId(service.getId());
      sfp.setProgramId(newHealthTaskRowId);
      hsftRepo.saveHealthServiceForTask(sfp).join();
    }

    sessionStatus.setComplete();

    return new ModelAndView(REDIRECT + HEALTHTASK, "clearHistory", "/");
  }

    @PostMapping(value = "new", params = "location")
  public String newHealthTaskPost(String location,
                                     @ModelAttribute(NEW_HEALTH_TASK_FORM) HealthTaskFormModel form) {
    return "redirect:/location/new/healthtask";
  }

  @GetMapping("view")
  public ModelAndView listHealthTasks() {
    List<HealthTaskListModel> healthTasks = healthTaskRepository
        .getAllHealthTasks()
        .stream()
        .map(HealthTaskListModel::new)
        .collect(Collectors.toList());

    ModelAndView modelAndView = new ModelAndView(VIEW_HEALTH_TASK);
    modelAndView.addObject("healthTasksList", healthTasks);
    modelAndView.addObject(new EditHealthTaskFormModel());

    return modelAndView;
  }

  @GetMapping("edit/{rowId}")
  public ModelAndView editHealthTask(@PathVariable("rowId") String rowId) {
    EditHealthTaskFormModel editTaskServiceFormModel = new EditHealthTaskFormModel();

    HealthTask healthTaskByRowId = healthTaskRepository.getHealthTaskByRowId(rowId);
    editTaskServiceFormModel.setName(healthTaskByRowId.getName());
    editTaskServiceFormModel.setDescription(healthTaskByRowId.getDescription());
    editTaskServiceFormModel.setStatus(HealthTaskStatus.convert(healthTaskByRowId.getStatus()));

    ModelAndView modelAndView = new ModelAndView(EDIT_HEALTH_TASK);
    modelAndView.addObject(editTaskServiceFormModel);

    return modelAndView;
  }

  @PostMapping("edit/{rowId}")
  public ModelAndView editHealthTaskPost(@PathVariable("rowId") String rowId,
                                            @Valid @ModelAttribute EditHealthTaskFormModel editHealthTaskFormModel,
                                            BindingResult bindingResult) {

    if (!bindingResult.hasErrors()) {
      try {
        healthTaskRepository.editHealthTask(rowId,editHealthTaskFormModel);

        return new ModelAndView("redirect:/healthtask/view", "clearHistory", "/healthtask/view");
      } catch (PersistenceException e) {
        if (!(e.getCause() instanceof org.hibernate.exception.ConstraintViolationException)) {
          throw e;
        }

        if (e.getCause() instanceof ConstraintViolationException) {
          FieldError fieldError = new FieldError(
                  "editHealthTaskFormModel",
                  "name",
                  editHealthTaskFormModel.getName(),
                  false,
                  null,
                  null,
                  TranslationUtil.getTranslations().getString(TranslationConsts.REGION_UNIQ_NAME_ERROR)
          );
          bindingResult.addError(fieldError);
        }
      }
    }

    ModelAndView modelAndView = new ModelAndView(EDIT_HEALTH_TASK);
    modelAndView.addObject(editHealthTaskFormModel);

    return modelAndView;
  }



}
