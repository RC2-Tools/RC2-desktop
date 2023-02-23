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

package edu.uw.cse.ifrcdemo.download;


import edu.uw.cse.ifrcdemo.download.view.DownloadActionListener;
import edu.uw.cse.ifrcdemo.sharedlib.consts.LogConsts;
import edu.uw.cse.ifrcdemo.sharedlib.consts.SentryConsts;
import edu.uw.cse.ifrcdemo.sharedlib.util.UIInitUtil;
import edu.uw.cse.ifrcdemo.sharedlib.view.SyncActionPanel;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import io.sentry.Sentry;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import java.util.ResourceBundle;

public class Download {

    public static void main(String[] args) {
        TranslationUtil.loadTranslationsFromArgs(args);
        System.setProperty(LogConsts.JAVA_LOG_MANAGER_PROPERTY, LogConsts.JAVA_LOG_MANAGER_VALUE);
        System.setProperty(SentryConsts.SENTRY_RELEASE_VERSION_PROPERTY, SentryConsts.getRc2ReleaseVersionForSentry());
        System.setProperty(SentryConsts.SENTRY_DNS_PROPERTY, SentryConsts.SENTRY_DNS_VALUE);
        Sentry.init();

        try {
            // Set System L&F
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Silently do nothing when this exception occurs. We failed to set the look and feel to the system default.
            // This should fall back to the Java cross platform 'metal' default, which looks worse but should still
            // work fine.
        }
		
        ResourceBundle translations =  TranslationUtil.getTranslations();
        UIInitUtil.configureDefaultFonts();


        JFrame frame = new JFrame(translations.getString(TranslationConsts.DOWNLOAD_APP_NAME));

        UIInitUtil.setFrameSize(frame, 8, 5);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        SyncActionPanel panel = new SyncActionPanel(translations.getString(TranslationConsts.DOWNLOAD_AND_VALIDATE_BTN_LABEL), true, translations.getString(TranslationConsts.SAVE_PATH_LABEL), false);
        panel.setSyncAction(new DownloadActionListener(panel));
        frame.add(panel);
        frame.setVisible(true);
    }
}