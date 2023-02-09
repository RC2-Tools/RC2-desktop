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

package edu.uw.cse.ifrcdemo.healthplan.ui.beneficiary;

import edu.uw.cse.ifrcdemo.healthplan.data.HealthDataInstance;
import edu.uw.cse.ifrcdemo.healthplan.data.HealthDataRepos;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.beneficiary.AbsBeneficiaryController;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.beneficiary.BeneficiaryFormModel;
import edu.uw.cse.ifrcdemo.sharedlib.model.config.AuxiliaryProperty;
import java.util.UUID;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

@Controller
@RequestMapping("/beneficiary")
@SessionAttributes(types = { BeneficiaryFormModel.class })
public class BeneficiaryController extends AbsBeneficiaryController {

  public BeneficiaryController(Logger logger) {
    super(logger);
  }

  protected AuxiliaryProperty getAuxiliaryProperty() {
    return HealthDataInstance.getDataRepos().getAuxiliaryProperty();
  }

  protected CsvRepository getCsvRepository() {
    return HealthDataInstance.getDataRepos().getCsvRepository();
  }

  protected UUID getDataReposVersion() {
    HealthDataRepos repos = HealthDataInstance.getDataRepos();
    if(repos == null) {
      return null;
    }
    return repos.getVersion();
  }

  @PostMapping("changeStatusSummary")
  public String changeBeneficiaryStatusSummaryPost(
          @ModelAttribute("beneficiaryFormModel") BeneficiaryFormModel beneficiaryFormModel,
          SessionStatus status) {
    changeBeneficiaryStatus(beneficiaryFormModel);

    status.setComplete();

    return "redirect:/";
  }
}
