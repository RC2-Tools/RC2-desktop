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

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/distribution/edit/{distRowId}/authorization/{authRowId}")
public class AuthorizationEditController {
  public static final String FORM_MODEL_NAME = "editAuthForm";
  public static final String EDIT_AUTH_FLASH_ATTR = "modifiedAuth";

  private static final String AUTHORIZATION_EDIT = "authorization/editAuth";

  @InitBinder
  public void initBinder(WebDataBinder webDataBinder) {
    webDataBinder.setAllowedFields("barcodeRange");
  }

  @GetMapping
  public String editAuth(@ModelAttribute(FORM_MODEL_NAME) AuthorizationFormModel authorizationFormModel) {
    return AUTHORIZATION_EDIT;
  }

  @PostMapping
  public String editAuthPost(@PathVariable String authRowId,
                             @ModelAttribute(FORM_MODEL_NAME) AuthorizationFormModel authorizationFormModel,
                             RedirectAttributes redirectAttributes) {
    authorizationFormModel.setId(authRowId);
    redirectAttributes.addFlashAttribute(EDIT_AUTH_FLASH_ATTR, authorizationFormModel);

    return "redirect:/distribution/edit/{distRowId}";
  }
}
