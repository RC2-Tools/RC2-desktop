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

package edu.uw.cse.ifrcdemo.planningsharedlib.ui.location;

import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Location;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.location.LocationRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.FxDialogUtil;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
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
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.PersistenceException;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/location")
public class LocationController {
  private static final String LOCATION_NEW_LOCATION = "location/newLocation";
  private static final String LOCATION_VIEW_LOCATIONS = "location/viewLocations";
  private static final String LOCATION_EDIT_LOCATION = "location/editLocation";

  private final LocationRepository locationRepository;
  private final Logger logger;

  public LocationController(LocationRepository locationRepository, Logger logger) {
    this.locationRepository = locationRepository;
    this.logger = logger;
  }

  @GetMapping("new/dist")
  public ModelAndView newLocationDist() {
    return newLocation();
  }

  @GetMapping("new/visit")
  public ModelAndView newLocationVisit() {
    return newLocation();
  }

  @GetMapping("new/healthtask")
  public ModelAndView newLocationHealthTask() {
    return newLocation();
  }

  @GetMapping("new/other")
  public ModelAndView newLocationOther() {
    return newLocation();
  }

  private ModelAndView newLocation() {
    return new ModelAndView(LOCATION_NEW_LOCATION).addObject("newLocForm", new NewLocationFormModel());
  }

  // TODO: restrict page values
  @PostMapping("new/{page}")
  public String newLocationPost(@ModelAttribute("newLocForm") NewLocationFormModel form, @PathVariable String page) {
    Location newLocation = new Location();
    newLocation.setName(form.getName());
    newLocation.setDescription(form.getDescription());
    locationRepository.saveLocation(newLocation).join();

    if (page.equals("visit")) {
      return "redirect:/visit/createVisit";
    } else if (page.equals("dist")) {
      return "redirect:/distribution/new";
    } else if (page.equals("healthtask")) {
      return "redirect:/healthtask/new";
    } else {
      FxDialogUtil.showInfoDialog(String
          .format(TranslationUtil.getTranslations().getString(TranslationConsts.CREATED_LOCATION),
              form.getName()));
      return "redirect:/other";
    }
  }

  @GetMapping("view")
  public ModelAndView listLocation() {
    List<LocationListModel> locations = locationRepository
        .getAllLocations()
        .stream()
        .map(LocationListModel::new)
        .collect(Collectors.toList());

    ModelAndView modelAndView = new ModelAndView(LOCATION_VIEW_LOCATIONS);
    modelAndView.addObject("locList", locations);
    modelAndView.addObject(new EditLocationFormModel());

    return modelAndView;
  }

  @GetMapping("edit/{rowId}")
  public ModelAndView editLocation(@PathVariable("rowId") String rowId) {
    EditLocationFormModel editLocationFormModel = new EditLocationFormModel();

    Location locationByRowId = locationRepository.getLocationByRowId(rowId);
    editLocationFormModel.setName(locationByRowId.getName());
    editLocationFormModel.setDescription(locationByRowId.getDescription());

    ModelAndView modelAndView = new ModelAndView(LOCATION_EDIT_LOCATION);
    modelAndView.addObject(editLocationFormModel);

    return modelAndView;
  }

  @PostMapping("edit/{rowId}")
  public ModelAndView editLocationPost(@PathVariable("rowId") String rowId,
                                       @Valid @ModelAttribute EditLocationFormModel editLocationFormModel,
                                       BindingResult bindingResult) {

    if (!bindingResult.hasErrors()) {
      try {
        locationRepository.editLocation(
            rowId,
            editLocationFormModel.getName(),
            editLocationFormModel.getDescription()
        );

        return new ModelAndView("redirect:/location/view", "clearHistory", "/location/view");
      } catch (PersistenceException e) {
        if (!(e.getCause() instanceof ConstraintViolationException)) {
          throw e;
        }

        if (e.getCause() instanceof ConstraintViolationException) {
          FieldError fieldError = new FieldError(
              "editLocationFormModel",
              "name",
              editLocationFormModel.getName(),
              false,
              null,
              null,
              TranslationUtil.getTranslations().getString(TranslationConsts.REGION_UNIQ_NAME_ERROR)
          );
          bindingResult.addError(fieldError);
        }
      }
    }

    ModelAndView modelAndView = new ModelAndView(LOCATION_EDIT_LOCATION);
    modelAndView.addObject(editLocationFormModel);

    return modelAndView;
  }
}
