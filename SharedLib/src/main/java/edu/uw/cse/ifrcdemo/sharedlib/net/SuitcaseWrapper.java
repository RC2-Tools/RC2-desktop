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

package edu.uw.cse.ifrcdemo.sharedlib.net;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uw.cse.ifrcdemo.sharedlib.consts.FileConsts;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.sharedlib.consts.ServerConsts;
import edu.uw.cse.ifrcdemo.sharedlib.consts.SuitcaseConsts;
import edu.uw.cse.ifrcdemo.sharedlib.logic.LoginTask;
import edu.uw.cse.ifrcdemo.sharedlib.logic.UpdateTask;
import edu.uw.cse.ifrcdemo.sharedlib.suitcase.SuitcaseCliArgsBuilder;
import edu.uw.cse.ifrcdemo.sharedlib.util.FileUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.OdkPathUtil;
import edu.uw.cse.ifrcdemo.translations.LogStr;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;
import org.opendatakit.aggregate.odktables.rest.entity.PrivilegesInfo;
import org.opendatakit.aggregate.odktables.rest.entity.TableResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableResourceList;
import org.opendatakit.suitcase.model.CloudEndpointInfo;
import org.opendatakit.suitcase.ui.DialogUtils;
import org.opendatakit.suitcase.ui.SuitcaseCLI;
import org.opendatakit.suitcase.utils.FieldsValidatorUtils;
import org.opendatakit.sync.client.FileUtils;
import org.opendatakit.sync.client.SyncClient;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.opendatakit.suitcase.net.SuitcaseSwingWorker.okCode;

public class SuitcaseWrapper {
    public static final String ASSETS_CONFIG_JSON_PATH_ON_SERVER = "assets/" + FileConsts.RC2_CONFIG_JSON;
    private static final String HTTP_SCHEME = "http://";
    private static final String HTTPS_SCHEME = "https://";

    private CloudEndpointInfo cloudEndpointInfo;
    private SyncClient syncClient;
    private Path basePath;

    public SuitcaseWrapper(CloudEndpointInfo cloudEndpointInfo, Path path) {
        this.basePath = path;

        this.syncClient = new SyncClient();
        try {
            this.syncClient.init(new URL(cloudEndpointInfo.getHostUrl()).getHost(), cloudEndpointInfo.getUserName(), cloudEndpointInfo.getPassword());
        } catch (MalformedURLException e) { /* guaranteed to not happen */ }

        this.cloudEndpointInfo = attemptHttpUpgrade(syncClient, cloudEndpointInfo);
    }

    public Path getBasePath() {
        return this.basePath;
    }

    public CloudEndpointInfo getCloudEndpointInfo() {
        return cloudEndpointInfo;
    }

    public SyncClient getSyncClient() {
        return syncClient;
    }

    public boolean uploadAllTables() throws IOException, JSONException {
        // App level assets
        String assetsDir = FileUtils.getAssetsDirPath(getBasePath().toAbsolutePath().toString());
        ArrayList<String> assetsFiles = recurseDir(new File(assetsDir));
        for (String filePath : assetsFiles) {
            String relativePathOnServer = filePath.substring(getBasePath().toAbsolutePath().toString().length() + 1);
            uploadFile(filePath, relativePathOnServer);
        }

        boolean success = true;

        Path tablesPath = getBasePath().resolve(SyncClient.TABLES_DIR);
        if (Files.exists(tablesPath)) {
            Files
                .find(tablesPath, 1, (path, attr) -> attr.isDirectory())
                .map(Path::getFileName)
                .map(Path::toString)
                .filter(this::hasTableDefinition)
                .forEach(name -> {
                    try {
                        createTable(name);
                    } catch (IOException | DataFormatException | JSONException e) {
                        throw new RuntimeException(e);
                    }
                });
        }

        // after creating tables, upload CSV and table level assets
        for (String existingTable : getTableList()) {
            uploadTableAsset(existingTable);
            success &= uploadCsv(existingTable);
        }

        return success;
    }

    private boolean uploadCsv(String tableName) {
        Path pathToCsv = FileUtil.getPathToCSV(getBasePath(), tableName);

        if (!Files.exists(pathToCsv)) {
            return true;
        }

        String path = pathToCsv.toAbsolutePath().toString();
        String error = FieldsValidatorUtils.checkUpdateFields(tableName, ServerConsts.SYNC_PROTOCOL_VERSION, path);

        if (error != null) {
            DialogUtils.showError(error, true);
            return false;
        } else {
            new LoginTask(cloudEndpointInfo, false).blockingExecute();
            UpdateTask updateTask = new UpdateTask(getCloudEndpointInfo(), path, null, tableName,
                getBasePath().getParent().toAbsolutePath().toString(), true);
            int returnCode = updateTask.blockingExecute();
            return returnCode == okCode;
        }


        // TODO: do something with the return code?
    }

    private void uploadTableAsset(String tableName) throws IOException {
        Path tablePath = OdkPathUtil.getTablePath(getBasePath(), tableName);

        if (Files.notExists(tablePath)) {
            return;
        }

        // Find all files to upload
        ArrayList<String> tableFiles = recurseDir(tablePath.toAbsolutePath().toFile());
        int relativePathSubstringIdx = getBasePath().toAbsolutePath().toString().length() + 1;

        for (String filePath : tableFiles) {
            String relativePathOnServer = filePath.substring(relativePathSubstringIdx);
            uploadFile(filePath, relativePathOnServer);
        }
    }

    public void downloadConfigFiles() throws IOException {
        String outputPath =  getBasePath().resolve(FileConsts.RC2_CONFIG_JSON).toAbsolutePath().toString();
        downloadFile(outputPath, ASSETS_CONFIG_JSON_PATH_ON_SERVER);
    }

    public void downloadAllTables(boolean withAttachment, boolean withTableDef) throws IOException, JSONException {
        Files.deleteIfExists(Paths.get(getBasePath().toAbsolutePath().toString(),
                cloudEndpointInfo.getAppId()));

        List<String> tableList = getTableList();

        tableList
                .forEach(name -> {
                    try {
                        downloadTable(name, withAttachment, withTableDef);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
    }

    public void downloadTable(String tableName, boolean withAttachment, boolean withTableDef)
        throws IOException, IllegalArgumentException {

        String appId = cloudEndpointInfo.getAppId();
        String basePathString = basePath.toAbsolutePath().toString();

        String[] cliOpts = new SuitcaseCliArgsBuilder()
            .configureEndpoint(cloudEndpointInfo)
            .configureTableId(tableName)
            .configurePath(basePathString)
            .configureAttachmentDownload(withAttachment)
            .configureMetadataOption(true)
            .configureOperation(SuitcaseCliArgsBuilder.Operation.DOWNLOAD)
            .build();

        int retCode = -1;
        try {
            retCode = new SuitcaseCLI(cliOpts).startCLI();
        } catch (IllegalArgumentException e) {
            System.err.println(LogStr.LOG_ILLEGAL_ARGUMENT_EXCEPTION_ON + tableName + LogStr.LOG_TABLE);
            System.err.println(LogStr.LOG_RETCODE + retCode);
            return;
        } finally {
            if (retCode != 0) {
                System.err.println(LogStr.LOG_FAILED_TO_DOWNLOAD + tableName + LogStr.LOG_TABLE);
                throw new SuitcaseException(LogStr.LOG_FAILED_TO_DOWNLOAD + tableName + LogStr.LOG_TABLE);
            }
        }

        String suitcaseCsvFilename =
            withAttachment ? SuitcaseConsts.DOWNLOAD_CSV_NAME : SuitcaseConsts.DOWNLOAD_NO_ATTACHMENT_CSV_NAME;

        Path pathToAppId = getBasePath().resolve(appId);
        Path pathToTable = pathToAppId.resolve(tableName);
        Path pathToDownloadFile = pathToTable.resolve(suitcaseCsvFilename);
        Path pathToCopyFile = FileUtil.getPathToCSV(basePath, tableName);

        Files.move(pathToDownloadFile, pathToCopyFile, REPLACE_EXISTING);
        Files.deleteIfExists(pathToDownloadFile);

        if (withAttachment) {
            // move attachments
            Path pathToInstances = pathToTable.resolve(SyncClient.INSTANCES_DIR);
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(pathToInstances)) {
                if (dirStream.iterator().hasNext()) {
                    Files.walkFileTree(pathToInstances, new AttachmentMovingFileVisitor(basePath, pathToAppId));
                } else {
                    Files.deleteIfExists(pathToInstances);
                }
            }
        }

        if (withTableDef) {
            Path tableDirectory = getBasePath().toAbsolutePath().resolve(tableName);
            Files.createDirectory(tableDirectory);

            downloadFile(
                tableDirectory.resolve(FileConsts.DEFINITION_CSV).toString(),
                "tables/" + tableName + "/" + FileConsts.DEFINITION_CSV
            );

            downloadFile(
                tableDirectory.resolve(FileConsts.PROPERTIES_CSV).toString(),
                "tables/" + tableName + "/" + FileConsts.PROPERTIES_CSV
            );
        }

        Files.deleteIfExists(pathToTable);
        Files.deleteIfExists(pathToAppId);
    }

    private void createTable(String tableName) throws IOException, DataFormatException, JSONException {
        String tableDefPath = FileUtils.getTableDefinitionFilePath(getBasePath().toAbsolutePath().toString(), tableName);
        JSONObject tableResult = syncClient.createTableWithCSV(cloudEndpointInfo.getServerUrl(), cloudEndpointInfo.getAppId(), tableName, GenConsts.EMPTY_STRING, tableDefPath);
    }

    private void uploadFile(String filePath, String relativePathOnServer) throws IOException {
        getSyncClient().uploadFile(getCloudEndpointInfo().getServerUrl(), getCloudEndpointInfo().getAppId(),
                filePath, relativePathOnServer, ServerConsts.SYNC_PROTOCOL_VERSION);

    }

    private void downloadFile(String pathToSaveFile, String relativePathOnServer) throws IOException {
        getSyncClient().downloadFile(
            getCloudEndpointInfo().getServerUrl(),
            getCloudEndpointInfo().getAppId(),
            pathToSaveFile,
            relativePathOnServer,
            ServerConsts.SYNC_PROTOCOL_VERSION
        );
    }

    private List<String> getTableList() throws IOException, JSONException {
        String tablesJson = getSyncClient()
            .getTables(getCloudEndpointInfo().getServerUrl(), getCloudEndpointInfo().getAppId())
            .toString();

        TableResourceList tableResourceList = new ObjectMapper()
            .readerFor(TableResourceList.class)
            .readValue(tablesJson);

        return tableResourceList
            .getTables()
            .stream()
            .map(TableResource::getTableId)
            .collect(Collectors.toList());
    }

    private boolean hasTableDefinition(String tableName) {
        String tableDefFilePath = FileUtils.getTableDefinitionFilePath(getBasePath().toAbsolutePath().toString(), tableName);

        File file = new File(tableDefFilePath);
        return file.exists() && file.length() > 0;
    }

    private static CloudEndpointInfo attemptHttpUpgrade(SyncClient syncClient, CloudEndpointInfo unmodifiedEndpoint) {
        // CloudEndpointInfo ensures the url starts with http(s)://
        if (unmodifiedEndpoint.getHostUrl().startsWith(HTTPS_SCHEME)) {
            return unmodifiedEndpoint;
        }

        try {
            CloudEndpointInfo httpsEndpoint = new CloudEndpointInfo(
                unmodifiedEndpoint.getHostUrl().replaceFirst(HTTP_SCHEME, HTTPS_SCHEME),
                unmodifiedEndpoint.getAppId(),
                unmodifiedEndpoint.getUserName(),
                unmodifiedEndpoint.getPassword()
            );

            PrivilegesInfo privilegesInfo =
                syncClient.getPrivilegesInfo(httpsEndpoint.getServerUrl(), httpsEndpoint.getAppId());
            if (privilegesInfo != null) {
                return httpsEndpoint;
            } else {
                return unmodifiedEndpoint;
            }
        } catch (IOException | JSONException e) {
            // request failed, https doesn't work, fallback
            return unmodifiedEndpoint;
        }
    }

    private static ArrayList<String> recurseDir(File dir) {
        ArrayList<String> filePaths = new ArrayList<>();
        File listFile[] = dir.listFiles();
        if (listFile != null) {
            for (File aListFile : listFile) {
                if (aListFile.isDirectory()) {
                    ArrayList<String> interimFilePaths = recurseDir(aListFile);
                    filePaths.addAll(interimFilePaths);
                } else {
                    filePaths.add(aListFile.getPath());
                }
            }
        }
        return filePaths;
    }

    private static class AttachmentMovingFileVisitor extends SimpleFileVisitor<Path> {
        private final Path pathToOutput;
        private final Path pathToAppId;

        public AttachmentMovingFileVisitor(Path pathToOutput, Path pathToAppId) {
            this.pathToOutput = pathToOutput;
            this.pathToAppId = pathToAppId;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Files.createDirectories(pathToOutput.resolve(pathToAppId.relativize(dir)));
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.move(file, pathToOutput.resolve(pathToAppId.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.deleteIfExists(dir);
            return FileVisitResult.CONTINUE;
        }
    }
}
