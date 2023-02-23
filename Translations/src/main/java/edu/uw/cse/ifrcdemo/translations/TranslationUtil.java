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

package edu.uw.cse.ifrcdemo.translations;

import java.util.Locale;
import java.util.ResourceBundle;

public class TranslationUtil {

    public static final String TRANSLATIONS_DEF = "translations";
    public static final String DEFAULT_LANGUAGE = "en";
    public static final String DEFAULT_COUNTRY = "US";

    private static ResourceBundle translations;

    public static void loadTranslationsFromArgs(String[] args) {
        String language;
        String country;

        if (args.length != 2) {
            language = new String(DEFAULT_LANGUAGE);
            country = new String(DEFAULT_COUNTRY);
        } else {
            language = args[0];
            country = args[1];
        }

        Locale currentLocale = new Locale(language, country);
        translations = ResourceBundle.getBundle(TRANSLATIONS_DEF, currentLocale, new Utf8Control());
    }

    public static void loadTranslationsFromLocale(Locale locale) {
        translations = ResourceBundle.getBundle(TRANSLATIONS_DEF, locale, new Utf8Control());
    }

    public static ResourceBundle getTranslations() {
        if(translations == null) {
            Locale currentLocale = new Locale(new String(DEFAULT_LANGUAGE),  new String(DEFAULT_COUNTRY));
            translations = ResourceBundle.getBundle(TRANSLATIONS_DEF, currentLocale, new Utf8Control());
        }
        return translations;
    }
}
