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

public class VersionConsts {

    private static final String VERSION_PROPERTIES = "version";
    private static final String VERSION_KEY = "rc2.release.version";
    private static final String DEFAULT_RELEASE_VERSION = "XX";

    private static String rc2Version = null;

    public static final String getRc2ReleaseVersionForSentry() {
        if(rc2Version != null) {
            return rc2Version;
        }

        try {
            ResourceBundle bundle = ResourceBundle.getBundle(VERSION_PROPERTIES);
            String tmpStr = bundle.getString(VERSION_KEY);
            if(tmpStr == null)
                return DEFAULT_RELEASE_VERSION;

            String [] versions = tmpStr.split("\\.");
            if(versions.length == 3) {
                rc2Version = versions[0] + "-" + versions[1];
                return rc2Version;
            } else {
                return DEFAULT_RELEASE_VERSION;
            }
        } catch (MissingResourceException e) {
            return DEFAULT_RELEASE_VERSION;
        }
    }
}
