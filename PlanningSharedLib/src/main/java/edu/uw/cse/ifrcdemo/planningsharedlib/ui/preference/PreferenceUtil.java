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

package edu.uw.cse.ifrcdemo.planningsharedlib.ui.preference;

import edu.uw.cse.ifrcdemo.planningsharedlib.model.profile.Rc2Profile;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.InternalFileStoreUtil;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

public class PreferenceUtil {

  private static final String SERVER_URL_KEY = "serverUrl";
  private static final String USERNAME_KEY = "username";
  private static final String IMPORT_DIRECTORY_KEY = "importDirectory";
  private static final String LAST_GOOD_SNAPSHOT_KEY = "lastGoodSnapshot";

  private static final String preferencesFile = "preferences.xml";

  private static final Logger logger = LogManager.getLogger(PreferenceUtil.class);

  public static void loadProfilePreferences()
      throws IOException, InvalidPreferencesFormatException, BackingStoreException {

    Preferences preferences = getPreferences();
    preferences.clear();

    String prefFilePath = InternalFileStoreUtil.getProfilePath() + File.separator + preferencesFile;
    File p = new File(prefFilePath);

    if (p.exists()) {
      preferences.importPreferences(new FileInputStream(p));
    }
  }

  public static List<Rc2Profile> readAllProfiles() throws IOException,
      BackingStoreException,
      InvalidPreferencesFormatException {

    String currProfile = InternalFileStoreUtil.getProfileName();

    List<String> profilesNameList = InternalFileStoreUtil.getProfilesList();

    try {
      List<Rc2Profile> profilesList = new ArrayList<>();

      for (String profile : profilesNameList) {
        InternalFileStoreUtil.setProfileName(profile);
        loadProfilePreferences();

        Rc2Profile rc2Profile = new Rc2Profile();
        rc2Profile.setName(profile);
        rc2Profile.setImportDirectory(getImportDirectory());
        rc2Profile.setLastGoodSnapshot(getLastKnownGoodSnapshot());
        rc2Profile.setServerUrl(getServerUrl());
        rc2Profile.setUsername(getUsername());

        profilesList.add(rc2Profile);
      }

      return profilesList;
    } finally {
      InternalFileStoreUtil.setProfileName(currProfile);
      loadProfilePreferences();
    }
  }

  public static void writeProfilePreferences() throws IOException, BackingStoreException {
    String prefFilePath = InternalFileStoreUtil.getProfilePath() + File.separator + preferencesFile;

    File p = new File(prefFilePath);
    p.createNewFile();

    Preferences preferences = getPreferences();
    preferences.exportNode(new FileOutputStream(prefFilePath));
  }

  public static String getServerUrl() {
    return getPreferences().get(SERVER_URL_KEY, null);
  }

  public static void setServerUrl(String serverUrl) {
    setPreference(serverUrl, SERVER_URL_KEY);
  }

  public static String getUsername() {
    return getPreferences().get(USERNAME_KEY, null);
  }

  public static void setUsername(String username) {
    setPreference(username, USERNAME_KEY);
  }

  public static String getImportDirectory() {
    return getPreferences().get(IMPORT_DIRECTORY_KEY, null);
  }

  public static void setImportDirectory(String directoryPath) {
    setPreference(directoryPath, IMPORT_DIRECTORY_KEY);
  }

  public static String getLastKnownGoodSnapshot() {
    return getPreferences().get(LAST_GOOD_SNAPSHOT_KEY, null);
  }

  public static void setLastKnownGoodSnapshot(String snapshot) {
    setPreference(snapshot, LAST_GOOD_SNAPSHOT_KEY);
  }

  public static void updatePreferencesFromForm(String serverUrl, String username,
      String importDir) {
    if (serverUrl != null && !Objects.equals(serverUrl, PreferenceUtil.getServerUrl())) {
      PreferenceUtil.setServerUrl(serverUrl);
    }

    if (username != null && !Objects.equals(username, PreferenceUtil.getUsername())) {
      PreferenceUtil.setUsername(username);
    }

    if (importDir != null && !Objects.equals(importDir, PreferenceUtil.getImportDirectory())) {
      PreferenceUtil.setImportDirectory(importDir);
    }
  }

  public static void updatePreferencesFromForm(String serverUrl,
                                               String username,
                                               String importDir,
                                               String profile) throws IOException, BackingStoreException {
    String currProfile = InternalFileStoreUtil.getProfileName();

    try {
      InternalFileStoreUtil.setProfileName(profile);
      getPreferences().clear();
      updatePreferencesFromForm(serverUrl, username, importDir);
      writeProfilePreferences();
    } finally {
      InternalFileStoreUtil.setProfileName(currProfile);
    }
  }

  private static void setPreference(String preferenceValue, String preferenceKey) {
    if (preferenceValue == null || preferenceValue.length() == 0) {
      return;
    }

    Preferences preferences = getPreferences();
    preferences.put(preferenceKey, preferenceValue);

    try {
      preferences.flush();
    } catch (BackingStoreException e) {
      logger.catching(Level.DEBUG, e);
    }
  }

  private static Preferences getPreferences() {
    return Preferences.userNodeForPackage(PreferenceUtil.class);
  }
}
