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

package edu.uw.cse.ifrcdemo.healthplan.ui.export;

import edu.uw.cse.ifrcdemo.healthplan.data.HealthDataRepos;
import edu.uw.cse.ifrcdemo.healthplan.ui.CommonHealthExportController;
import edu.uw.cse.ifrcdemo.healthplan.util.ExportUtil;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.export.ExportFormModel;
import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import javax.validation.Valid;
import org.apache.logging.log4j.Logger;
import org.apache.wink.json4j.JSONException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/export")
@SessionAttributes(types = { ExportFormModel.class })
public class ExportController extends CommonHealthExportController {
  private static final String EXPORT_TO_SERVER = "export/exportToServer";

  public ExportController(HealthDataRepos healthDataRepos,
                          ExportUtil exportUtil,
                          Logger logger) {
    super(healthDataRepos, exportUtil, logger);
  }

  @GetMapping("")
  public ModelAndView showServerSettings(
      @ModelAttribute("exportFormModel") ExportFormModel exportFormModel) {
    return setServerInfo(exportFormModel, EXPORT_TO_SERVER);
  }

  @PostMapping("")
  public ModelAndView exportSyncToServer(
      @Valid @ModelAttribute("exportFormModel") ExportFormModel exportFormModel,
      BindingResult bindingResult, SessionStatus status) throws IOException, JSONException, BackingStoreException, InvalidPreferencesFormatException {
    return helperExportSyncToServer(exportFormModel, bindingResult, status, EXPORT_TO_SERVER);
  }

}
