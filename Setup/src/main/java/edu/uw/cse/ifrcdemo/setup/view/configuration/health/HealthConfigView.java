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

package edu.uw.cse.ifrcdemo.setup.view.configuration.health;

import edu.uw.cse.ifrcdemo.setup.view.configuration.AbsConfigView;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.sharedlib.view.SharedLayoutDefault;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ResourceBundle;

public class HealthConfigView extends AbsConfigView {

    public HealthConfigView() {
        super(new GridBagLayout());

        ResourceBundle translations = TranslationUtil.getTranslations();
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = SharedLayoutDefault.getDefaultGbc();

        gbc.gridy = 0;
        gbc.weighty = 9.5;
        gbc.weightx = 2;
        add(buildLabelsPanel(translations), gbc);
        gbc.weightx = 8;
        add(buildInputPanel(), gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weighty = 0.5;
        add(buildButtonsPanel(), gbc);

        getRegModeJComboBox().setEnabled(true);
    }

    private JPanel buildLabelsPanel(ResourceBundle translations) {
        JPanel panel = new JPanel(new GridLayout(5, 1));

        super.addBeneficiaryEntityLabels(panel, translations);

        StringBuffer tmp = new StringBuffer();
        tmp.append(GenConsts.OPEN_HTML);
        tmp.append(GenConsts.OPEN_HTML_BODY);
        tmp.append(translations.getString(TranslationConsts.BENEFICIARY_ENTITY_ID_COL_LABEL_PART_ONE));
        tmp.append(GenConsts.HTML_BR);
        tmp.append(translations.getString(TranslationConsts.BENEFICIARY_ENTITY_ID_COL_LABEL_PART_TWO));
        tmp.append(GenConsts.CLOSE_HTML_BODY);
        tmp.append(GenConsts.CLOSE_HTML);
        panel.add(new JLabel(tmp.toString(), SwingConstants.LEFT));

        return panel;
    }

    private JPanel buildInputPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 1));

        super.addBeneficiaryEntityFields(panel);

        return panel;
    }

    private JPanel buildButtonsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2));

        super.addExportButtonOptions(panel);

        return panel;
    }
}
