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

package edu.uw.cse.ifrcdemo.sharedlib.view;

import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class PathChooserPanel extends JPanel {
    private static final int LABEL_WEIGHT = 5;
    private static final int PATH_TEXT_WEIGHT = 90;
    private static final int BUTTON_WEIGHT = 5;

    private JLabel label;
    private JTextField pathText;
    private JButton browseBtn;

    public PathChooserPanel(String label, final String defaultPath) {
        super(new GridBagLayout());

        GridBagConstraints gbc = SharedLayoutDefault.getDefaultGbc();
        gbc.gridx = GridBagConstraints.RELATIVE;
        gbc.gridy = 0;

        this.label = new JLabel(label);
        this.label.setHorizontalAlignment(JLabel.CENTER);
        gbc.weightx = LABEL_WEIGHT;
        add(this.label, gbc);

        this.pathText = new JTextField(1);
        this.pathText.setText(defaultPath);
        this.pathText.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        gbc.weightx = PATH_TEXT_WEIGHT;
        add(this.pathText, gbc);

        this.browseBtn = new JButton();
        this.browseBtn.setText(GenConsts.ELLIPSIS);
        this.browseBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setCurrentDirectory(new File(defaultPath));

                int result = chooser.showSaveDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    pathText.setText(chooser.getSelectedFile().toString());
                }
            }
        });
        gbc.weightx = BUTTON_WEIGHT;
        add(this.browseBtn, gbc);
    }

    public String getPath() {
        pathText.setText(sanitizePath(pathText.getText()));

        return pathText.getText();
    }

    private static String sanitizePath(String path) {
        return path.replaceAll("^\\s+", "");
    }
}