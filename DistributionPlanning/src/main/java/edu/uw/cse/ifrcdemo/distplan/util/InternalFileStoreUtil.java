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

package edu.uw.cse.ifrcdemo.distplan.util;

import edu.uw.cse.ifrcdemo.distplan.data.DataInstance;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.sharedlib.consts.VersionConsts;
import io.github.soc.directories.ProjectDirectories;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InternalFileStoreUtil {
  public static final String DEFAULT_PROFILE = "default";

  public static final String PROFILES_PATH = "pf" + VersionConsts.getRc2ReleaseVersionForSentry();
  public static final String XLSX_STORAGE_PATH = "xlsx";
  static final String SNAPSHOT_PATH = "in";
  static final String OUTPUT_PATH = "out";
  static final String TEMP_PATH = "tmp";

  private static ProjectDirectories directories;
  private static String profileName;
  private static Path snapshotPath;
  private static Path outputPath;
  private static Path tempPath;

  private static ProjectDirectories getProjectDirectories()throws IOException {
    if(directories == null) {
      directories = ProjectDirectories.from("edu.uw.cse.ifrcdemo", "RC2", "Relief");
      Path dataDir = Paths.get(directories.dataDir);
      if(dataDir.toString().endsWith("data"))
        Files.createDirectories(dataDir.getParent());
      else
        Files.createDirectories(dataDir);
    }
    return directories;
  }

  public static Path getProjectPath() throws IOException {
    Path dataDir = Paths.get(getProjectDirectories().dataDir);
    if(dataDir.toString().endsWith("data"))
      return dataDir.getParent();
    else
      return dataDir;
  }

  public static String getProfileName() {
    if(profileName == null) {
      profileName = DEFAULT_PROFILE;
    }
    return profileName;
  }

  public static void setProfileName(String newProfileName) {
    profileName = sanitizeProfileName(newProfileName);
  }

  private static String sanitizeProfileName(String newProfileName) {
    return newProfileName.replaceAll("\\W+", GenConsts.UNDERSCORE);
  }


  public static Path getCurrentSnapshotStoragePath() {
    return snapshotPath;
  }

  public static Path getCurrentOutputStoragePath() {
    return outputPath;
  }

  public static Path getCurrentTempStoragePath() {
    return tempPath;
  }

  public static Path getXlsxFormStoragePath() throws IOException{
    return getProfilePath().resolve(XLSX_STORAGE_PATH);
  }

  public static Path getProfilePath() throws IOException {
    return getProjectPath().resolve(PROFILES_PATH).resolve(getProfileName());
  }

  public static Path getProfileSnapshotPath() throws IOException {
    return getProfilePath().resolve(SNAPSHOT_PATH);
  }

  public static boolean createProfilePath(String newProfileName) throws IOException {
    String profileName = sanitizeProfileName(newProfileName);
    Path path = getProjectPath().resolve(PROFILES_PATH).resolve(profileName);
    return path.toFile().mkdirs();
  }

  public static List<String> getProfilesList() throws IOException {
    Path profileParentDir = getProjectPath().resolve(PROFILES_PATH);

    // Make sure that the default profile always exists
    if (!Files.exists(profileParentDir)) {
      createProfilePath(DEFAULT_PROFILE);
    }

    List<String> profileNames;
    try (Stream<Path> paths = Files.walk(profileParentDir, 1)) {
      profileNames = paths
          .filter(path -> !path.equals(profileParentDir))
          .filter(Files::isDirectory)
          .map(path -> path.getName(path.getNameCount() - 1).toString())
          .sorted(String::compareToIgnoreCase)
          .collect(Collectors.toList());
    }

    return profileNames;
  }

  public static CompletableFuture<Void> reimportData(File selectedDirectory) throws IOException, IllegalAccessException {
    return importData(getProfileName(), selectedDirectory);
  }

  public static CompletableFuture<Void> importData(String profileName, File selectedDirectory) throws IOException, IllegalAccessException {
    List<String> missingTables = CsvFileUtil.checkDirForRequiredFiles(selectedDirectory.toPath());
    if (!missingTables.isEmpty()) {
      throw new FileNotFoundException("Missing " + String.join(", ", missingTables));
    }
    // clear old profile information
    setNoProfile();

    // setup new profile
    setProfileName(profileName);
    setCurrentSnapshotPath();
    FileUtils.copyDirectory(selectedDirectory, snapshotPath.toFile());

    return DataInstance.loadInputDataSource(getProfilePath(), snapshotPath);
  }

  public static void setNoProfile() throws IllegalAccessException {
    profileName = null;
    snapshotPath = null;
    outputPath = null;
    tempPath = null;
    DataInstance.clearRepos();
  }

//////////////////////////////////////////////

  public static void setCurrentSnapshotPath() throws IOException {
    setCurrentPath(SNAPSHOT_PATH);
  }

  public static void setCurrentOutputPath() throws IOException {
    setCurrentPath(OUTPUT_PATH);
  }

  public static void setCurrentTempPath() throws IOException {
    setCurrentPath(TEMP_PATH);
  }

  private static void setCurrentPath(String valueForPath) throws IOException {
    DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
    Date date = new Date();

    // TODO: WRB make sure impossible for second folder

    switch (valueForPath) {
    case SNAPSHOT_PATH:
      snapshotPath = getProfilePath().resolve(valueForPath).resolve(dateFormat.format(date));
      snapshotPath.toFile().mkdirs();
      break;
    case OUTPUT_PATH:
      outputPath = getProfilePath().resolve(valueForPath).resolve(dateFormat.format(date));
      outputPath.toFile().mkdirs();
      break;
    case TEMP_PATH:
      tempPath = getProfilePath().resolve(valueForPath).resolve(dateFormat.format(date));
      tempPath.toFile().mkdirs();
      break;
    }
  }
}
