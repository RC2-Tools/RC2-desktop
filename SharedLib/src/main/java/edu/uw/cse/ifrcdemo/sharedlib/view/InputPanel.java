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

package edu.uw.cse.ifrcdemo.sharedlib.view;

import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class InputPanel extends JPanel{

    private GridBagConstraints gbc;

    public InputPanel(String[] labels, JTextField[] textFields, String[] defaultText, JCheckBox showPassword, ResourceBundle translations) {
        super(new GridBagLayout());

        if ((labels.length != textFields.length) || (textFields.length != defaultText.length)) {
            throw new IllegalArgumentException(translations.getString(TranslationConsts.ARRAYS_HAVE_UNEQUAL_LENGTH_ERROR));
        }

        this.gbc = SharedLayoutDefault.getDefaultGbc();
        this.gbc.gridx = GridBagConstraints.RELATIVE;
        this.gbc.gridy = 0;

        buildLabelPanel(labels, showPassword, translations);
        buildTextPanel(textFields, defaultText, showPassword);
    }

    private void buildLabelPanel(String[] labels, JCheckBox showPassword, ResourceBundle translations) {
        int length = labels.length;
        if(showPassword != null) {
            length++;
        }
        JPanel labelPanel = new JPanel(new GridLayout(length, 1));
        gbc.weightx = 1;
        this.add(labelPanel, gbc);

        for (String label : labels) {
            labelPanel.add(new JLabel(label));
        }
        if(showPassword != null) {
            labelPanel.add(new JLabel(translations.getString(TranslationConsts.SHOW_PASSWORD_LABEL)));
        }
    }

    private void buildTextPanel(JTextField[] textFields, String[] defaultText, JCheckBox showPassword) {
        int length = textFields.length;
        if(showPassword != null) {
            length++;
        }
        JPanel inputPanel = new JPanel(new GridLayout(length, 1));
        gbc.weightx = 9;
        this.add(inputPanel, gbc);

        for (int i = 0; i < textFields.length; i++) {
            if (!defaultText[i].isEmpty()) {
                textFields[i].setText(defaultText[i]);
            }
            textFields[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            inputPanel.add(textFields[i]);
        }

        if(showPassword != null) {
            inputPanel.add(showPassword);
        }
    }
}
