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

import edu.uw.cse.ifrcdemo.sharedlib.consts.LayoutViewConsts;
import edu.uw.cse.ifrcdemo.sharedlib.consts.ServerConsts;
import edu.uw.cse.ifrcdemo.sharedlib.consts.ToolPropertiesConsts;
import edu.uw.cse.ifrcdemo.sharedlib.util.FileUtil;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import org.opendatakit.suitcase.net.SuitcaseSwingWorker;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class IOPanel extends JPanel implements PropertyChangeListener {

    private static final String BAD_ARGUMENTS_EXCEPTION = "IF file is to be selected must provide the file selection label";

    private final JTextField sCloudEndpointAddressText;
    private final JTextField sAppIdText;
    private final JTextField sUserNameText;
    private final JPasswordField sPasswordText;
    private final JCheckBox sShowPassword;
    private final JButton sActionButton;
    private final PathChooserPanel pathChooserPanel;
    private final String buttonLabel;

    private final Preferences preferences;

    public IOPanel(String buttonLabel, boolean includeFileSelection, String fileSelectionLabel, boolean includeAppId)  throws IllegalArgumentException{
        super(new GridBagLayout());

        if(includeFileSelection) {
            if(fileSelectionLabel == null)
                throw new IllegalArgumentException(BAD_ARGUMENTS_EXCEPTION);
        }

        this.buttonLabel = buttonLabel;
        this.sCloudEndpointAddressText = new JTextField(1);
        this.sAppIdText = new JTextField(1);
        this.sUserNameText = new JTextField(1);
        this.sPasswordText = new JPasswordField(1);
        this.sShowPassword = new JCheckBox();
        this.sActionButton = new JButton();

        char defaultHide = sPasswordText.getEchoChar();
        sShowPassword.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                sPasswordText.setEchoChar((char) 0);
            } else {
                sPasswordText.setEchoChar(defaultHide);
            }
        });

        this.preferences = Preferences.userNodeForPackage(getClass());

        GridBagConstraints gbc = SharedLayoutDefault.getDefaultGbc();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;

        ResourceBundle translations = TranslationUtil.getTranslations();

        JPanel inputPanel;
        if (includeAppId) {
            inputPanel = new InputPanel(
                    new String[] {translations.getString(TranslationConsts.SERVER_ADDR_LABEL), translations.getString(TranslationConsts.APP_ID_LABEL), translations.getString(TranslationConsts.USERNAME_LABEL), translations.getString(TranslationConsts.PASSWORD_LABEL)},
                    new JTextField[] {sCloudEndpointAddressText, sAppIdText, sUserNameText, sPasswordText},
                    new String[] {
                            preferences.get(ToolPropertiesConsts.KEY_SERVER_URL, ServerConsts.SERVER_ADDR_DEFAULT),
                            ServerConsts.APP_ID_DEFAULT,
                            preferences.get(ToolPropertiesConsts.KEY_USERNAME, ServerConsts.USERNAME_DEFAULT),
                            ServerConsts.PASSWORD_DEFAULT
                    },
                    sShowPassword,
                    translations);
        } else {
            inputPanel = new InputPanel(
                    new String[] {translations.getString(TranslationConsts.SERVER_ADDR_LABEL), translations.getString(TranslationConsts.USERNAME_LABEL), translations.getString(TranslationConsts.PASSWORD_LABEL)},
                    new JTextField[] {sCloudEndpointAddressText, sUserNameText, sPasswordText},
                    new String[] {
                            preferences.get(ToolPropertiesConsts.KEY_SERVER_URL, ServerConsts.SERVER_ADDR_DEFAULT),
                            preferences.get(ToolPropertiesConsts.KEY_USERNAME, ServerConsts.USERNAME_DEFAULT),
                            ServerConsts.PASSWORD_DEFAULT
                    },
                    sShowPassword,
                    translations);
            this.sAppIdText.setText(ServerConsts.APP_ID_DEFAULT);
        }

        gbc.weighty = 85;
        gbc.insets = new Insets(30, 50, 0, 50);
        this.add(inputPanel, gbc);

        if (includeFileSelection) {
            pathChooserPanel = new PathChooserPanel(fileSelectionLabel, FileUtil.getDefaultSavePath().toAbsolutePath().toString());
            gbc.weighty = 1;
            this.add(pathChooserPanel, gbc);
        } else {
            pathChooserPanel = null;
        }

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        sActionButton.setText(this.buttonLabel);
        buttonPanel.add(sActionButton);

        gbc.weighty = 15;
        gbc.insets = new Insets(20, LayoutViewConsts.WINDOW_WIDTH / 4, 80, LayoutViewConsts.WINDOW_WIDTH / 4);
        this.add(buttonPanel, gbc);
    }

    public void setSyncAction(SyncActionListener customAction) {
        sActionButton.addActionListener(customAction);
    }

    public JTextField getCloudEndpointAddressText() {
        return sCloudEndpointAddressText;
    }

    public JTextField getAppIdText() {
        return sAppIdText;
    }

    public JTextField getUserNameText() {
        return sUserNameText;
    }

    public JPasswordField getPasswordText() {
        return sPasswordText;
    }

    public JButton getActionButton() {
        return sActionButton;
    }

    public PathChooserPanel getPathChooserPanel() {
        return pathChooserPanel;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getNewValue() != null && evt.getPropertyName().equals(SuitcaseSwingWorker.DONE_PROPERTY)) {
            // restore buttons
            sActionButton.setText(this.buttonLabel);
            sActionButton.setEnabled(true);
        }
    }
}