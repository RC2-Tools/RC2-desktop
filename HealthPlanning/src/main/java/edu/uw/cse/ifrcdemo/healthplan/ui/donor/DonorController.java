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

package edu.uw.cse.ifrcdemo.healthplan.ui.donor;

import edu.uw.cse.ifrcdemo.healthplan.entity.Donor;
import edu.uw.cse.ifrcdemo.healthplan.model.DonorRepository;
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
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/donor")
public class DonorController {
    private static final String DONOR = "donor";
    private static final String NEW_DONOR = DONOR +"/newDonor";
    private static final String VIEW_DONORS = DONOR + "/viewDonors";
    private static final String EDIT_DONOR = DONOR + "/editDonor";

    private final DonorRepository donorRepository;
    private final Logger logger;

    public DonorController(DonorRepository donorRepository, Logger logger) {
        this.donorRepository = donorRepository;
        this.logger = logger;
    }

    @ModelAttribute
    public NewDonorFormModel getNewDonorFormModel() {
        return new NewDonorFormModel();
    }

    @GetMapping("new/task")
    public ModelAndView newDonor(@ModelAttribute NewDonorFormModel form) {
        return new ModelAndView(NEW_DONOR);
    }

    @GetMapping("new/other")
    public ModelAndView newDonorOther(@ModelAttribute NewDonorFormModel form) {
        return new ModelAndView(NEW_DONOR);
    }

    @PostMapping("new/{page}")
    public String newDonorPostPage(@ModelAttribute NewDonorFormModel form, @PathVariable String page) {
        Donor newDonor = new Donor();
        newDonor.setName(form.getName());
        newDonor.setDescription(form.getDescription());

        donorRepository.saveDonor(newDonor).join();

        if (page.equals("other")) {
            FxDialogUtil.showInfoDialog(String
                    .format(TranslationUtil.getTranslations().getString(TranslationConsts.CREATED_DONOR),
                            form.getName()));
            return "redirect:/other";
        } else {
            return "redirect:/healthtask/new";
        }
    }

    @GetMapping("view")
    public ModelAndView listDonors() {
        List<DonorListModel> donors = donorRepository
                .getDonorList()
                .stream()
                .map(DonorListModel::new)
                .collect(Collectors.toList());

        ModelAndView modelAndView = new ModelAndView(VIEW_DONORS);
        modelAndView.addObject("donorList", donors);
        modelAndView.addObject(new EditDonorFormModel());

        return modelAndView;
    }

    @GetMapping("edit/{rowId}")
    public ModelAndView editDonor(@PathVariable("rowId") String rowId) {
        EditDonorFormModel editDonorFormModel = new EditDonorFormModel();

        Donor donorByRowId = donorRepository.getDonorByRowId(rowId);
        editDonorFormModel.setName(donorByRowId.getName());
        editDonorFormModel.setDescription(donorByRowId.getDescription());

        ModelAndView modelAndView = new ModelAndView(EDIT_DONOR);
        modelAndView.addObject(editDonorFormModel);

        return modelAndView;
    }

    @PostMapping("edit/{rowId}")
    public ModelAndView editDonorPost(@PathVariable("rowId") String rowId,
                                     @Valid @ModelAttribute EditDonorFormModel editDonorFormModel,
                                     BindingResult bindingResult) {

        if (!bindingResult.hasErrors()) {
            try {
                donorRepository.editDonor(
                        rowId,
                        editDonorFormModel.getName(),
                        editDonorFormModel.getDescription()
                );

                return new ModelAndView("redirect:/donor/view", "clearHistory", "/donor/view");
            } catch (PersistenceException e) {
                if (!(e.getCause() instanceof ConstraintViolationException)) {
                    throw e;
                }

                if (e.getCause() instanceof ConstraintViolationException) {
                    FieldError fieldError = new FieldError(
                            "editDonorFormModel",
                            "name",
                            editDonorFormModel.getName(),
                            false,
                            null,
                            null,
                            TranslationUtil.getTranslations().getString(TranslationConsts.DONOR_UNIQ_NAME_ERROR)
                    );
                    bindingResult.addError(fieldError);
                }
            }
        }

        ModelAndView modelAndView = new ModelAndView(EDIT_DONOR);
        modelAndView.addObject(editDonorFormModel);

        return modelAndView;
    }
}

