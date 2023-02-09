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

package edu.uw.cse.ifrcdemo.planningsharedlib;

import edu.uw.cse.ifrcdemo.planningsharedlib.ui.localization.LocaleUtil;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.InternalFileStoreUtil;
import edu.uw.cse.ifrcdemo.sharedlib.consts.LogConsts;
import edu.uw.cse.ifrcdemo.sharedlib.consts.SentryConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import io.sentry.Sentry;
import org.springframework.boot.logging.LogFile;

import java.io.IOException;

public class BaseAppSystem {
    // constants
    public static final int PREF_WIDTH = 1200;
    public static final int PREF_HEIGHT = 768;

    private static final String SPRING_DEVTOOLS_RESTART_ENABLED = "spring.devtools.restart.enabled";
    private static final String RC_2_LOG = "rc2.log";

    // shared global variables
    protected static boolean systemInit = false;

    public static void initAllConfigs() {
       initLogging();
       initSentry();
       initTranslations();
    }

    public static void initLogging() {
        System.setProperty(LogConsts.JAVA_LOG_MANAGER_PROPERTY, LogConsts.JAVA_LOG_MANAGER_VALUE);
        try {
            String logFile = InternalFileStoreUtil.getProjectPath().resolve(RC_2_LOG).toString();
            System.setProperty(LogFile.FILE_PROPERTY, logFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void initSentry() {
        System.setProperty(SPRING_DEVTOOLS_RESTART_ENABLED, Boolean.FALSE.toString());
        System.setProperty(SentryConsts.SENTRY_RELEASE_VERSION_PROPERTY,
                SentryConsts.getRc2ReleaseVersionForSentry());
        System.setProperty(SentryConsts.SENTRY_DNS_PROPERTY, SentryConsts.SENTRY_DNS_VALUE);
        Sentry.init();
    }

    public static void initTranslations() {
        TranslationUtil.loadTranslationsFromLocale(LocaleUtil.getCurrentLocale());
    }

}
