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

package edu.uw.cse.ifrcdemo.planningsharedlib.ui.profile;

import edu.uw.cse.ifrcdemo.planningsharedlib.ui.preference.PreferenceUtil;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.FxDialogUtil;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.InternalFileStoreUtil;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.io.IOException;
import java.util.TreeSet;
import java.util.prefs.BackingStoreException;

@Controller
@RequestMapping("/profile")
public class ProfileController {

  public static final String NEW_PROFILE_TEMPLATE = "profile/newProfile";

  private final Logger logger;

  public ProfileController(Logger logger) {
    this.logger = logger;
  }

  @ModelAttribute("profileFormModel")
  public ProfileFormModel newLoginFormModel() {
    return new ProfileFormModel();
  }

  @GetMapping("new")
  public ModelAndView newProfile(@ModelAttribute ProfileFormModel profileFormModel) {
    return new ModelAndView(NEW_PROFILE_TEMPLATE);
  }

  @PostMapping("new")
  public ModelAndView newProfilePost(@Valid @ModelAttribute ProfileFormModel profileFormModel,
                                     BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return newProfile(profileFormModel);
    }

    try {
      TreeSet<String> profileNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
      profileNames.addAll(InternalFileStoreUtil.getProfilesList());

      if (profileNames.contains(profileFormModel.getProfileName())) {
        bindingResult.addError(new FieldError(
            "profileFormModel",
            "profileName",
            profileFormModel.getProfileName(),
            false,
            null,
            null,
            TranslationUtil.getTranslations().getString(TranslationConsts.DUPLICATE_PROFILE_NAME)
        ));

        return newProfile(profileFormModel);
      } else {
        InternalFileStoreUtil.createProfilePath(profileFormModel.getProfileName());
        PreferenceUtil.updatePreferencesFromForm(
            profileFormModel.getServerUrl(),
            profileFormModel.getServerUsername(),
            null,
            profileFormModel.getProfileName()
        );
      }

    } catch (IOException | BackingStoreException e) {
      logger.catching(e);
      FxDialogUtil.showScrollingExceptionDialog("Error while creating profile", e);
    }

    return new ModelAndView(
        "redirect:/login?profile={name}",
        "name",
        profileFormModel.getProfileName()
    );
  }
}
