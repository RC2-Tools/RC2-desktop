/*
 * Copyright (c) 2016-2022 University of Washington
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *  *  Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *  * Neither the name of the University of Washington nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE UNIVERSITY OF WASHINGTON AND CONTRIBUTORS “AS IS” AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE UNIVERSITY OF WASHINGTON OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package edu.uw.cse.ifrcdemo.distplan.ui.login;

import edu.uw.cse.ifrcdemo.distplan.data.DataInstance;
import edu.uw.cse.ifrcdemo.distplan.sync.SyncUtil;
import edu.uw.cse.ifrcdemo.distplan.ui.preference.PreferenceUtil;
import edu.uw.cse.ifrcdemo.distplan.ui.util.SyncUiUtil;
import edu.uw.cse.ifrcdemo.distplan.util.FxDialogUtil;
import edu.uw.cse.ifrcdemo.distplan.util.InternalFileStoreUtil;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationType;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import org.apache.logging.log4j.Logger;
import org.opendatakit.suitcase.model.CloudEndpointInfo;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;

@Controller
@RequestMapping("/login")
@SessionAttributes(types = { LoginFormModel.class })
public class LoginController {

  private static final String LOGIN_FORM_MODEL = "loginFormModel";
  private static final String LOGIN_TEMPLATE = "login/loginProfile";
  private static final String LOGIN_LOCAL_DATA_TEMPLATE = "login/loginProfileLocal";

  private final Logger logger;

  public LoginController(Logger logger) {
    this.logger = logger;
  }

  @InitBinder
  public void initBinder(WebDataBinder webDataBinder) {
    webDataBinder.setDisallowedFields("profileList");
  }

  @ModelAttribute(LOGIN_FORM_MODEL)
  public LoginFormModel newLoginFormModel() {
    return new LoginFormModel();
  }

  @GetMapping("")
  public ModelAndView login(@ModelAttribute(LOGIN_FORM_MODEL) LoginFormModel loginFormModel,
                            @RequestParam(value = "profile", required = false) String profile)
      throws InvalidPreferencesFormatException, BackingStoreException, IOException {
    loginFormModel.setProfileList(PreferenceUtil.readAllProfiles());
    loginFormModel.setProfile(profile);

    return new ModelAndView(LOGIN_TEMPLATE);
  }

  @GetMapping("localData")
  public ModelAndView loginWithLocalData(@ModelAttribute(LOGIN_FORM_MODEL) LoginFormModel loginFormModel, @RequestParam String profile) {
    String profileImportDirectory = loginFormModel
        .getProfileList()
        .stream()
        .filter(p -> p.getName().equals(profile))
        .findAny()
        .orElseThrow(IllegalArgumentException::new)
        .getImportDirectory();

    loginFormModel.setInputDataDirectory(profileImportDirectory);
    loginFormModel.setProfile(profile);

    return new ModelAndView(LOGIN_LOCAL_DATA_TEMPLATE);
  }

  @PostMapping("downloadData")
  public ModelAndView loginPostDownload(@Valid @ModelAttribute(LOGIN_FORM_MODEL) LoginFormModel loginFormModel,
                                        BindingResult bindingResult,
                                        SessionStatus status) throws InterruptedException,
      IOException, BackingStoreException, InvalidPreferencesFormatException {
    if (bindingResult.hasErrors()) {
      return new ModelAndView(LOGIN_TEMPLATE);
    }

    InternalFileStoreUtil.setProfileName(loginFormModel.getProfile());
    PreferenceUtil.loadProfilePreferences();

    CloudEndpointInfo cloudEndpointInfo;
    try {
      cloudEndpointInfo = loginFormModel.toCloudEndpointInfo();
    } catch (MalformedURLException e) {
      logger.catching(e);

      String errLabel = TranslationUtil
          .getTranslations()
          .getString(TranslationConsts.ERROR_WHILE_DOWNLOADING_DATA);
      FxDialogUtil.showScrollingExceptionDialog(errLabel, e);
      return new ModelAndView(LOGIN_TEMPLATE);
    }

    try {
      SyncUtil.downloadToProfileTemp(cloudEndpointInfo);
    } catch (Exception e) {
      logger.error("Error while executing suitcase future", e);
      String errLabel = TranslationUtil
          .getTranslations()
          .getString(TranslationConsts.ERROR_WHILE_DOWNLOADING_DATA);
      FxDialogUtil.showScrollingExceptionDialog(errLabel, e);
      return new ModelAndView(LOGIN_TEMPLATE);
    }

    File selectedDirectory = InternalFileStoreUtil.getCurrentTempStoragePath().toFile();

    ModelAndView modelAndView = loginPostInternal(selectedDirectory, loginFormModel);
    status.setComplete();

    return modelAndView;
  }

  @PostMapping("localData")
  public ModelAndView loginPostLocalData(@Valid @ModelAttribute(LOGIN_FORM_MODEL) LoginFormModel loginFormModel,
                                         BindingResult bindingResult,
                                         SessionStatus status) throws InterruptedException,
      IOException, BackingStoreException, InvalidPreferencesFormatException {

    if (bindingResult.hasErrors()) {
      return new ModelAndView(LOGIN_LOCAL_DATA_TEMPLATE);
    }

    InternalFileStoreUtil.setProfileName(loginFormModel.getProfile());
    PreferenceUtil.loadProfilePreferences();

    String selectedDirectoryStr = loginFormModel.getInputDataDirectory();
    System.err.println("Directory Selected: " + selectedDirectoryStr);

    File selectedDirectory = new File(selectedDirectoryStr);
    ModelAndView modelAndView = loginPostInternal(selectedDirectory, loginFormModel);
    status.setComplete();

    return modelAndView;
  }

  @PostMapping("lastSnapshot")
  public ModelAndView loginPostLastSnapshot(@ModelAttribute(LOGIN_FORM_MODEL) LoginFormModel loginFormModel,
                                            SessionStatus status) throws IOException,
      InterruptedException, BackingStoreException, InvalidPreferencesFormatException {

    InternalFileStoreUtil.setProfileName(loginFormModel.getProfile());
    PreferenceUtil.loadProfilePreferences();

    String lastKnownGoodSnapshot = PreferenceUtil.getLastKnownGoodSnapshot();

    if (lastKnownGoodSnapshot == null) {
      FxDialogUtil.showErrorDialog(
          TranslationUtil.getTranslations().getString(TranslationConsts.LAST_GOOD_SNAPSHOT_UNAVAILABLE));

      return new ModelAndView(LOGIN_TEMPLATE);
    }

    loginFormModel.setType(DataInputSourceType.LAST_GOOD_SNAPSHOT.name());

    File selectedDirectory = InternalFileStoreUtil
        .getProfileSnapshotPath()
        .resolve(lastKnownGoodSnapshot)
        .toFile();

    ModelAndView modelAndView = loginPostInternal(selectedDirectory, loginFormModel);
    status.setComplete();

    return modelAndView;
  }

  private ModelAndView loginPostInternal(File selectedDirectory, LoginFormModel loginFormModel)
      throws InterruptedException, IOException {
    Objects.requireNonNull(selectedDirectory);
    Objects.requireNonNull(loginFormModel);

    ResourceBundle translations = TranslationUtil.getTranslations();
    boolean success = SyncUiUtil.handleReimportData(
        selectedDirectory, DataInputSourceType.valueOf(loginFormModel.getType()));

    if (!success) {
      return new ModelAndView(LOGIN_TEMPLATE);
    }

    PreferenceUtil.updatePreferencesFromForm(loginFormModel.getServerUrl(),
        loginFormModel.getUsername(), loginFormModel.getInputDataDirectory());

    PreferenceUtil.setLastKnownGoodSnapshot(
        InternalFileStoreUtil.getCurrentSnapshotStoragePath().getFileName().toString());

    try {
      PreferenceUtil.writeProfilePreferences();
    } catch (BackingStoreException e) {
      logger.catching(e);
    }

    AuthorizationType workflowMode = DataInstance.getDataRepos().getAuxiliaryProperty().getWorkflowMode();

    if (workflowMode == AuthorizationType.NO_REGISTRATION) {
      FxDialogUtil.showWarningDialog(
          translations.getString(TranslationConsts.DATA_LOADED_SUCCESSFULLY),
          translations.getString(TranslationConsts.PLANNING_NO_REGISTRATION_WARNING)
      );
    } else {
      FxDialogUtil.showInfoDialog(translations.getString(TranslationConsts.DATA_LOADED_SUCCESSFULLY));
    }

    return new ModelAndView("redirect:/");
  }
}
