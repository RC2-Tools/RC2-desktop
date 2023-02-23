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

package edu.uw.cse.ifrcdemo.distreport;

import edu.uw.cse.ifrcdemo.distreport.consts.ReportConsts;
import edu.uw.cse.ifrcdemo.distreport.consts.ViewConsts;
import edu.uw.cse.ifrcdemo.distreport.logic.DownloadReportActionListener;
import edu.uw.cse.ifrcdemo.distreport.view.MainFrame;
import edu.uw.cse.ifrcdemo.distreport.view.manual.ManualPresenter;
import edu.uw.cse.ifrcdemo.distreport.view.menu.MenuPresenter;
import edu.uw.cse.ifrcdemo.distreport.view.menu.MenuView;
import edu.uw.cse.ifrcdemo.sharedlib.consts.LogConsts;
import edu.uw.cse.ifrcdemo.sharedlib.consts.SentryConsts;
import edu.uw.cse.ifrcdemo.sharedlib.util.SwingUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.UIInitUtil;
import edu.uw.cse.ifrcdemo.sharedlib.view.BannerPanel;
import edu.uw.cse.ifrcdemo.sharedlib.view.MainPanel;
import edu.uw.cse.ifrcdemo.sharedlib.view.NavigationPresenter;
import edu.uw.cse.ifrcdemo.sharedlib.view.NavigationView;
import edu.uw.cse.ifrcdemo.sharedlib.view.SingleDirectoryPanel;
import edu.uw.cse.ifrcdemo.sharedlib.view.SyncActionPanel;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import io.sentry.Sentry;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.util.ResourceBundle;

public class Report {


	public static void main(String[] args) {
		TranslationUtil.loadTranslationsFromArgs(args);

		System.setProperty(LogConsts.JAVA_LOG_MANAGER_PROPERTY, LogConsts.JAVA_LOG_MANAGER_PROPERTY);
		System.setProperty(ReportConsts.MUSTACHE_DEBUG_PROPERTY, Boolean.toString(true));
		System.setProperty(SentryConsts.SENTRY_RELEASE_VERSION_PROPERTY, SentryConsts.getRc2ReleaseVersionForSentry());
		System.setProperty(SentryConsts.SENTRY_DNS_PROPERTY, SentryConsts.SENTRY_DNS_VALUE);
		Sentry.init();

		SwingUtil.setSystemLookAndFeel();

		SwingUtilities.invokeLater(() -> {
			ResourceBundle translations = TranslationUtil.getTranslations();
			UIInitUtil.configureDefaultFonts();

			JFrame frame = new MainFrame();
			UIInitUtil.setFrameSize(frame, 8, 5);
			MainPanel mainPanel = new MainPanel(frame.getWidth(), frame.getHeight() - (frame.getHeight() / 5));
			MenuView menuView = new MenuView();
			BannerPanel banner = new BannerPanel(frame.getWidth(), frame.getHeight() / 5);
			NavigationView navigationView = new NavigationView();
			SingleDirectoryPanel manualReport = new SingleDirectoryPanel();
			SyncActionPanel autoReport = new SyncActionPanel(translations.getString(TranslationConsts.REPORT_DOWNLOAD_BTN_LABEL), true, translations.getString(TranslationConsts.SAVE_PATH_LABEL), false);
			autoReport.setSyncAction(new DownloadReportActionListener(autoReport));

			ManualPresenter manualPresenter = new ManualPresenter(manualReport);
			NavigationPresenter navigationPresenter = new NavigationPresenter(mainPanel, menuView.getPanel(), navigationView, ViewConsts.CARD_MENU);
			MenuPresenter menuPresenter = new MenuPresenter(mainPanel, menuView);

			banner.addControls(navigationView);

			mainPanel.add(menuView.getPanel(), ViewConsts.CARD_MENU);
			mainPanel.add(manualReport, ViewConsts.CARD_MANUAL);
			mainPanel.add(autoReport, ViewConsts.CARD_SERVER);

			mainPanel.getLayoutMngr().show(mainPanel, ViewConsts.CARD_MENU);

			frame.add(banner, BorderLayout.NORTH);
			frame.add(mainPanel, BorderLayout.CENTER);
			frame.pack();
			frame.setVisible(true);
		});
	}
}
