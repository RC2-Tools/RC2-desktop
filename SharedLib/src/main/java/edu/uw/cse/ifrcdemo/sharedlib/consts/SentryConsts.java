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

package edu.uw.cse.ifrcdemo.sharedlib.consts;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class SentryConsts {
    public static final String SENTRY_DNS_PROPERTY = "sentry.dsn";
 //   public static final String SENTRY_DNS_VALUE = "https://5253a0658bb54d3b989faacc7d045345@sentry.io/1258267";
   public static final String SENTRY_DNS_VALUE = "";

    public static final String SENTRY_RELEASE_VERSION_PROPERTY = "sentry.release";
    private static final String SENTRY_DEFAULT_RELEASE_VERSION = "0.X.X";
    private static final String SENTRY_PROPERTIES = "sentry";
    private static final String VERSION_KEY = "sentry.release.version";

    private static String sentryVersion = null;

    public static final String getRc2ReleaseVersionForSentry() {
        if(sentryVersion != null) {
            return sentryVersion;
        }

        try {
            ResourceBundle bundle = ResourceBundle.getBundle(SENTRY_PROPERTIES);
            sentryVersion = bundle.getString(VERSION_KEY);
            return sentryVersion;
        } catch (MissingResourceException e) {
            return SENTRY_DEFAULT_RELEASE_VERSION;
        }
    }
}
