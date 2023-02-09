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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Authorization;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Distribution;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.distribution.DistributionRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.authorization.AuthorizationEditController;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.authorization.AuthorizationFormModel;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.range.Range;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/distribution/edit/{distRowId}")
@SessionAttributes(types = {
    DistributionEditFormModel.class
})
public class DistributionEditController {
  public static final String FORM_MODEL_NAME = "editDistForm";

  private static final String DISTRIBUTION_EDIT = "distribution/editDistribution";

  private final DistributionRepository distributionRepository;
  private final ObjectMapper objectMapper;
  private final Logger logger;

  public DistributionEditController(DistributionRepository distributionRepository,
                                    ObjectMapper objectMapper,
                                    Logger logger) {
    this.distributionRepository = distributionRepository;
    this.objectMapper = objectMapper;
    this.logger = logger;
  }

  @InitBinder
  public void initBinder(WebDataBinder webDataBinder) {
    webDataBinder.setDisallowedFields("authorizations");
  }

  @ModelAttribute
  public void newDistributionFormModel(@PathVariable String distRowId,
                                       Model model) throws JsonProcessingException {
    DistributionEditFormModel currFormModel = (DistributionEditFormModel) model.asMap().get(FORM_MODEL_NAME);

    // only set the model when the rowId changed
    if (currFormModel != null && distRowId.equals(currFormModel.getRowId())) {
      return;
    }

    Distribution distribution;
    try {
      distribution = distributionRepository.getDistributionByRowId(distRowId);
    } catch (NoResultException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, null, e);
    }

    DistributionEditFormModel editFormModel = new DistributionEditFormModel();
    editFormModel.setRowId(distRowId);
    editFormModel.setName(distribution.getName());
    editFormModel.setDescription(distribution.getDescription());

    Map<String, AuthorizationFormModel> authList = new HashMap<>();
    for (Authorization authorization : distribution.getAuthorizations()) {
      AuthorizationFormModel authFormModel = new AuthorizationFormModel();
      authList.put(authorization.getRowId(), authFormModel);

      authFormModel.setId(authorization.getRowId());
      authFormModel.setItemId(authorization.getItem().getRowId());
      authFormModel.setItemName(authorization.getItem().getName());

      authFormModel.setBarcodeRange(authorization.getItemRanges());
      authFormModel.setBarcodeRangeList(authorization.getItemRanges());
    }
    editFormModel.setAuthorizations(authList);

    model.addAttribute(FORM_MODEL_NAME, editFormModel);
  }

  @GetMapping
  public String editDist(@PathVariable String distRowId,
                         @ModelAttribute(FORM_MODEL_NAME) DistributionEditFormModel distFormModel,
                         @ModelAttribute(
                             value = AuthorizationEditController.EDIT_AUTH_FLASH_ATTR,
                             binding = false) AuthorizationFormModel authFormModel,
                         HttpServletRequest request) {
    if (RequestContextUtils.getInputFlashMap(request) != null &&
        RequestContextUtils.getInputFlashMap(request).containsValue(authFormModel)) {
      AuthorizationFormModel authToModify = distFormModel
          .getAuthorizations()
          .get(authFormModel.getId());

      if (authToModify != null) {
        authToModify.setBarcodeRange(authFormModel.getBarcodeRange());
        authToModify.setBarcodeRangeList(authFormModel.getBarcodeRange());
      }
    }

    return DISTRIBUTION_EDIT;
  }

  @GetMapping(params = "edit")
  public String editAuth(@ModelAttribute(FORM_MODEL_NAME) DistributionEditFormModel form,
                         @RequestParam String edit,
                         RedirectAttributes redirAttr) {
    AuthorizationFormModel formToEdit = form.getAuthorizations().get(edit);

    if (formToEdit != null) {
      redirAttr.addFlashAttribute(AuthorizationEditController.FORM_MODEL_NAME, formToEdit);
      return "redirect:/distribution/edit/{distRowId}/authorization/" + edit;
    }

    return "redirect:/distribution/edit/{distRowId}";
  }

  @PostMapping
  public String editDistPost(@PathVariable String distRowId,
                             @Valid @ModelAttribute(FORM_MODEL_NAME)
                                 DistributionEditFormModel distributionEditFormModel,
                             BindingResult bindingResult,
                             SessionStatus sessionStatus) {
    if (bindingResult.hasErrors()) {
      return DISTRIBUTION_EDIT;
    }

    Distribution distEntity = distributionRepository.getDistributionByRowId(distRowId);

    distEntity.setName(distributionEditFormModel.getName());
    for (Authorization authorization : distEntity.getAuthorizations()) {
      List<Range> newBarcodeRange = distributionEditFormModel
          .getAuthorizations()
          .get(authorization.getRowId())
          .getBarcodeRangeList();

      authorization.setItemRanges(newBarcodeRange);
    }

    distributionRepository.saveDistribution(distEntity);

    sessionStatus.setComplete();

    return "redirect:/distribution/view";
  }
}
