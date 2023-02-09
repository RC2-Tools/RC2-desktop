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
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class SingleDirectoryPanel extends JPanel {

  private final JTextField dataDirTextField;
  private final JButton generateBtn;

  public SingleDirectoryPanel(String continueText, String dataDirText) {
    super(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

    this.dataDirTextField = new JTextField();
    this.generateBtn = new JButton(continueText);

    add(buildDataDirChooserPanel(dataDirText));
    add(buildButtonPanel(), BorderLayout.PAGE_END);
  }


  public SingleDirectoryPanel() {
    this(TranslationUtil.getTranslations().getString(TranslationConsts.CONTINUE_LABEL),
            TranslationUtil.getTranslations().getString(TranslationConsts.CHOOSE_INPUT_DIRECTORY_MSG));
  }

  public JTextField getDataDirTextField() {
    return dataDirTextField;
  }

  public JButton getBottomBtn() {
    return generateBtn;
  }

  private JPanel buildDataDirChooserPanel(String label) {
    JPanel panel = new JPanel(new GridBagLayout());

    GridBagConstraints gbc = SharedLayoutDefault.getDefaultGbc();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.LINE_START;
    gbc.gridy = 0;

    gbc.weightx = 2;
    panel.add(new JLabel(label, SwingConstants.LEADING), gbc);
    gbc.weightx = 8;
    panel.add(getDataDirTextField(), gbc);

    return panel;
  }

  private JPanel buildButtonPanel() {
    JPanel panel = new JPanel(new GridLayout(1, 1));

    panel.add(getBottomBtn());

    return panel;
  }
}
