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
import edu.uw.cse.ifrcdemo.healthplan.entity.HealthService;
import edu.uw.cse.ifrcdemo.healthplan.model.HealthServiceRepository;
import edu.uw.cse.ifrcdemo.healthplan.ui.healthservice.HealthServiceListModel;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class AddHealthServiceController {
    private static final String ADD_HEALTH_SERVICES = HealthTaskController.HEALTHTASK + "/newHealthTaskAddHealthService";
    public static final String HEALTH_TASK_ADD_SERVICE_FORM = "addHealthServiceToHealthServiceForm";


    @GetMapping("addhealthservices")
    public ModelAndView addHealthServicePopUp(@SessionAttribute(HealthTaskController.NEW_HEALTH_TASK_FORM) HealthTaskFormModel healthTask) {

        HealthServiceRepository healthServiceRepository = HealthDataInstance.getDataRepos().getHealthServiceRepository();

        List<HealthServiceListModel> healthServicesList = healthServiceRepository.getAllHealthServices()
                .stream()
                .map(HealthServiceListModel::new)
                .collect(Collectors.toList());
        AddHealthServiceToHealthTaskModel data = new AddHealthServiceToHealthTaskModel(healthServicesList);

        ModelAndView modelAndView = new ModelAndView(ADD_HEALTH_SERVICES);
        modelAndView.addObject(HEALTH_TASK_ADD_SERVICE_FORM, data);

        return modelAndView;
    }

    @PostMapping("addhealthservices")
    public String addHealthServicePopUpPost(@SessionAttribute(HealthTaskController.NEW_HEALTH_TASK_FORM) HealthTaskFormModel healthTask,
                                                  @ModelAttribute(HEALTH_TASK_ADD_SERVICE_FORM) AddHealthServiceToHealthTaskModel data) {

        String addedService = data.getChosenHealthService();
        HealthServiceRepository healthServiceRepository = HealthDataInstance.getDataRepos().getHealthServiceRepository();
        HealthService addedHealthService = healthServiceRepository.getHealthServiceByRowId(addedService);

        HealthServiceListModel service = new HealthServiceListModel(addedHealthService);


        List<HealthServiceListModel> existingServices = healthTask.getHealthServiceList();

        if (!existingServices.contains(service)) {
            existingServices.add(service);
        }

        return HealthTaskController.REDIRECT + HealthTaskController.NEW_HEALTH_TASK_SERVICES;
    }
}
