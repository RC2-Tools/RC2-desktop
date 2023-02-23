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

import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

public class SyncActionPanel extends JPanel {

    private static final String BAD_ARGUMENTS_EXCEPTION = "IF file is to be selected must provide the file selection label";

    private ProgressBar progressBar;
    private IOPanel ioPanel;


    public SyncActionPanel(String buttonLabel, boolean includeFileSelection, String fileSelectionLabel, boolean includeAppId) throws IllegalArgumentException {
        super(new GridBagLayout());
        if(includeFileSelection) {
            if(fileSelectionLabel == null)
                throw new IllegalArgumentException(BAD_ARGUMENTS_EXCEPTION);
        }
        this.progressBar = buildProgressBar();
        this.ioPanel = new IOPanel(buttonLabel, includeFileSelection, fileSelectionLabel, includeAppId);

        GridBagConstraints gbc = SharedLayoutDefault.getDefaultGbc();
        gbc.weighty = 0.8;
        gbc.gridx = 0;
        add(this.ioPanel, gbc);

        gbc.weighty = 0.2;
        gbc.ipady = 10;
        add(getProgressBar(), gbc);
    }

    public void setSyncAction(SyncActionListener customAction) {
        this.ioPanel.setSyncAction(customAction);
    }

    private ProgressBar buildProgressBar() {
        ProgressBar pb = new ProgressBar();
        pb.setVisible(false);

        return pb;
    }

    public ProgressBar getProgressBar() {
        return this.progressBar;
    }

    public IOPanel getIOPanel() { return this.ioPanel; }

}