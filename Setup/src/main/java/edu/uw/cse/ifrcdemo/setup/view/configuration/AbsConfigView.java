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

package edu.uw.cse.ifrcdemo.setup.view.configuration;

import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.RegistrationMode;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.LayoutManager;
import java.util.ResourceBundle;

public abstract class AbsConfigView extends JPanel {
    private final JComboBox<RegistrationMode> regModeJComboBox;
    private final JTextField beneficiaryEntityFormChooser;
    private final JTextField beneficiaryEntityIdColumnTextField;
    private final JTextField individualFormChooser;
    private final JButton saveBtn;
    private final JButton serverBtn;

    public AbsConfigView(LayoutManager layout) {
        super(layout);
        ResourceBundle translations = TranslationUtil.getTranslations();
        saveBtn = new JButton(translations.getString(TranslationConsts.SAVE_CONFIG_LOCAL_BTN_LABEL));
        serverBtn = new JButton(translations.getString(TranslationConsts.SAVE_CONFIG_SRV_BTN_LABEL));
        regModeJComboBox = new JComboBox<>();
        beneficiaryEntityFormChooser = new JTextField();
        beneficiaryEntityIdColumnTextField = new JTextField();
        individualFormChooser = new JTextField();

        regModeJComboBox.setEnabled(false);
        beneficiaryEntityFormChooser.setEnabled(false);
        beneficiaryEntityIdColumnTextField.setEnabled(false);
        individualFormChooser.setEnabled(false);
        saveBtn.setEnabled(false);
        serverBtn.setEnabled(false);
    }

    public JComboBox<RegistrationMode> getRegModeJComboBox() {
      return regModeJComboBox;
    }

    public JTextField getBeneficiaryEntityFormChooser() {
      return beneficiaryEntityFormChooser;
    }

    public JTextField getBeneficiaryEntityIdColumnTextField() {
      return beneficiaryEntityIdColumnTextField;
    }

    public JTextField getIndividualFormChooser() {
      return individualFormChooser;
    }

    public JButton getSaveBtn() {
      return saveBtn;
    }

    public JButton getServerBtn() { return serverBtn; }

    protected void addBeneficiaryEntityLabels(JPanel panel, ResourceBundle translations) {
        panel.add(new JLabel(translations.getString(TranslationConsts.REGISTRATION_MODE_LABEL), SwingConstants.LEFT));
        panel.add(new JLabel(translations.getString(TranslationConsts.BENEFICIARY_ENTITY_FORM_LABEL), SwingConstants.LEFT));
        panel.add(new JLabel(translations.getString(TranslationConsts.MEMBER_FORM_LABEL), SwingConstants.LEFT));
    }

    protected void addBeneficiaryEntityFields(JPanel panel) {
        panel.add(getRegModeJComboBox());
        panel.add(getBeneficiaryEntityFormChooser());
        panel.add(getIndividualFormChooser());
        panel.add(getBeneficiaryEntityIdColumnTextField());
    }

    protected void addExportButtonOptions(JPanel panel) {
        panel.add(getSaveBtn());
        panel.add(getServerBtn());
    }

}
