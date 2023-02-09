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

package edu.uw.cse.ifrcdemo.sharedlib.suitcase;

import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.sharedlib.consts.ServerConsts;
import edu.uw.cse.ifrcdemo.sharedlib.consts.SuitcaseConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import java.util.ArrayList;
import java.util.List;
import org.opendatakit.suitcase.model.CloudEndpointInfo;

public class SuitcaseCliArgsBuilder {

  public enum Operation {
    DOWNLOAD, UPDATE, UPLOAD
  }

  private List<String> arguments;

  private List<String> getArguments() {
    return arguments;
  }

  public SuitcaseCliArgsBuilder() {
    this.arguments = new ArrayList<>();
  }

  public SuitcaseCliArgsBuilder configureEndpoint(String url, String appId, String dataVersion) {
    getArguments().add(SuitcaseConsts.SUITCASE_CLOUD_ENDPOINT_OPTION);
    getArguments().add(url);

    getArguments().add(SuitcaseConsts.SUITCASE_APP_ID_OPTION);
    getArguments().add(appId);

    getArguments().add(SuitcaseConsts.SUITCASE_DATA_VERSION_OPTION);
    getArguments().add(dataVersion);

    return this;
  }

  public SuitcaseCliArgsBuilder configureEndpoint(CloudEndpointInfo cloudEndpointInfo) {
    // assume that the version is 2
    configureEndpoint(cloudEndpointInfo.getHostUrl(), cloudEndpointInfo.getAppId(), ServerConsts.SYNC_PROTOCOL_VERSION);
    configureCredential(cloudEndpointInfo.getUserName(), cloudEndpointInfo.getPassword());

    return this;
  }

  public SuitcaseCliArgsBuilder configureCredential(String username, String password) {
    getArguments().add(SuitcaseConsts.SUITCASE_USERNAME_OPTION);
    getArguments().add(username);

    getArguments().add(SuitcaseConsts.SUITCASE_PASSWORD_OPTION);
    getArguments().add(password);

    return this;
  }

  public SuitcaseCliArgsBuilder configureOperation(Operation operation) {
    switch (operation) {
      case DOWNLOAD:
        getArguments().add(SuitcaseConsts.SUITCASE_DOWNLOAD_OPTION);
        break;
      case UPDATE:
        getArguments().add(SuitcaseConsts.SUITCASE_UPDATE_OPTION);
        break;
      case UPLOAD:
        getArguments().add(SuitcaseConsts.SUITCASE_UPLOAD_OPTION);
        break;
      default:
        String errMsg = TranslationUtil.getTranslations().getString(TranslationConsts.UNSUPPORTED_OPERATION_ERROR) + GenConsts.SPACE + operation;
        throw new IllegalArgumentException(errMsg);
    }

    return this;
  }

  public SuitcaseCliArgsBuilder configureTableId(String tableId) {
    getArguments().add(SuitcaseConsts.SUITCASE_TABLE_ID_OPTION);
    getArguments().add(tableId);

    return this;
  }

  public SuitcaseCliArgsBuilder configurePath(String path) {
    getArguments().add(SuitcaseConsts.SUITCASE_PATH_OPTION);
    getArguments().add(path);

    return this;
  }

  public SuitcaseCliArgsBuilder configureAttachmentDownload(boolean downloadAttachment) {
    if (downloadAttachment) {
      getArguments().add(SuitcaseConsts.SUITCASE_ATTACHMENT_OPTION);
    }

    return this;
  }

  public SuitcaseCliArgsBuilder configureMetadataOption(boolean withExtraMetadata) {
    if (withExtraMetadata) {
      getArguments().add(SuitcaseConsts.SUITCASE_METADATA_OPTION);
    }

    return this;
  }

  public SuitcaseCliArgsBuilder configureOtherArgs(String arg, String value) {
    getArguments().add(arg);
    getArguments().add(value);

    return this;
  }

  public String[] build() {
    return getArguments().toArray(new String[] {});
  }
}
