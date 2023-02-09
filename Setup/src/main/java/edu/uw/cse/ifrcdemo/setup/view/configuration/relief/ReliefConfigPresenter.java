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

package edu.uw.cse.ifrcdemo.setup.view.configuration.relief;

import edu.uw.cse.ifrcdemo.setup.consts.SetupConsts;
import edu.uw.cse.ifrcdemo.setup.view.configuration.AbsConfigPresenter;
import edu.uw.cse.ifrcdemo.sharedlib.model.config.AuxiliaryProperty;
import edu.uw.cse.ifrcdemo.sharedlib.model.config.SaveDirectory;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationType;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.Module;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.RegistrationMode;
import edu.uw.cse.ifrcdemo.sharedlib.view.MainPanel;
import edu.uw.cse.ifrcdemo.sharedlib.view.SyncActionPanel;
import edu.uw.cse.ifrcdemo.xlsxconverterserver.handler.MappingFormUploadConsumer;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class ReliefConfigPresenter extends AbsConfigPresenter {

  private final ReliefConfigView reliefConfigView;

  public ReliefConfigPresenter(MainPanel mainPanel, ReliefConfigView reliefConfigView, SaveDirectory saveDirectory, MappingFormUploadConsumer formUploadConsumer, SyncActionPanel syncPanel) {
    super(Module.RELIEF, mainPanel, saveDirectory, formUploadConsumer, reliefConfigView, syncPanel);
    this.reliefConfigView = reliefConfigView;

    setupWorkflowModeComboBox();
  }

  private void setupWorkflowModeComboBox() {
    JComboBox<AuthorizationType> workflowModeComboBox = reliefConfigView.getWorkflowModeJComboBox();

    workflowModeComboBox.setModel(new DefaultComboBoxModel<>(AuthorizationType.values()));
    workflowModeComboBox.addItemListener(new WorkflowModeSelectionListener());
    workflowModeComboBox.setSelectedIndex(-1);
  }


  private class WorkflowModeSelectionListener implements ItemListener {
    @Override
    public void itemStateChanged(ItemEvent e) {
      if (e.getStateChange() != ItemEvent.SELECTED) {
        return;
      }

      refreshClickableComponents();
    }
  }

  public void addAuxiliaryProperty(AuxiliaryProperty auxProp) {
    auxProp.setWorkflowMode((AuthorizationType) (reliefConfigView.getWorkflowModeJComboBox().getSelectedItem()));
  }

  @Override
  public String getBaseFilesZipName() {
    return SetupConsts.RELIEF_BASE_FILES_RESOURCE_NAME;
  }

  @Override
  protected void refreshClickableComponents() {
    Object selectedWorkflowMode = reliefConfigView.getWorkflowModeJComboBox().getSelectedItem();

    if (selectedWorkflowMode == AuthorizationType.NO_REGISTRATION) {
      reliefConfigView.getRegModeJComboBox().setEnabled(false);
      reliefConfigView.getBeneficiaryEntityFormChooser().setEnabled(false);
      reliefConfigView.getBeneficiaryEntityIdColumnTextField().setEnabled(false);
      reliefConfigView.getIndividualFormChooser().setEnabled(false);

      reliefConfigView.getSaveBtn().setEnabled(true);
      reliefConfigView.getServerBtn().setEnabled(true);
    } else if (selectedWorkflowMode == AuthorizationType.ID_ONLY_REGISTRATION) {
      reliefConfigView.getRegModeJComboBox().setEnabled(false);
      reliefConfigView.getBeneficiaryEntityFormChooser().setEnabled(false);
      reliefConfigView.getBeneficiaryEntityIdColumnTextField().setEnabled(false);
      reliefConfigView.getIndividualFormChooser().setEnabled(false);

      reliefConfigView.getSaveBtn().setEnabled(true);
      reliefConfigView.getServerBtn().setEnabled(true);

      reliefConfigView
              .getRegModeJComboBox()
              .setSelectedItem(RegistrationMode.INDIVIDUAL);

      super.setToRegMode(RegistrationMode.INDIVIDUAL);
    } else {
      super.refreshClickableComponents();
    }
  }

}
