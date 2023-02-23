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

package edu.uw.cse.ifrcdemo.distplan.model.preference;

import edu.uw.cse.ifrcdemo.distplan.consts.DataSource;
import org.opendatakit.suitcase.model.CloudEndpointInfo;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class PreferencesStore {
  public static final String INPUT_PATH_KEY = "inputPath";
  public static final String OUTPUT_PATH_KEY = "outputPath";
  public static final String DATA_SOURCE_KEY = "dataSource";
  public static final String CLOUD_ENDPOINT_KEY = "cloudEndpointInfo";

  private final Map<String, Object> store;

  private Map<String, Object> getStore() {
    return store;
  }

  public PreferencesStore() {
    this.store = new HashMap<>();
  }

  public DataSource getDataSource() {
    return (DataSource) getStore().get(DATA_SOURCE_KEY);
  }

  public DataSource setDataSource(DataSource dataSource) {
    return (DataSource) getStore().put(DATA_SOURCE_KEY, dataSource);
  }

  public CloudEndpointInfo getInputCloudEndpointInfo() {
    return (CloudEndpointInfo) getStore().get(CLOUD_ENDPOINT_KEY);
  }

  public CloudEndpointInfo setInputCloudEndpointInfo(CloudEndpointInfo cloudEndpointInfo) {
    return (CloudEndpointInfo) getStore().put(CLOUD_ENDPOINT_KEY, cloudEndpointInfo);
  }

  public Path getInputPath() {
    return (Path) getStore().get(INPUT_PATH_KEY);
  }

  public Path setInputPath(Path path) {
    return (Path) getStore().put(INPUT_PATH_KEY, path);
  }

    public CloudEndpointInfo getOutputCloudEndpointInfo() {
        return (CloudEndpointInfo) getStore().get(CLOUD_ENDPOINT_KEY);
    }

    public CloudEndpointInfo setOutputCloudEndpointInfo(CloudEndpointInfo cloudEndpointInfo) {
        return (CloudEndpointInfo) getStore().put(CLOUD_ENDPOINT_KEY, cloudEndpointInfo);
    }

  public Path getOutputPath() {
    return (Path) getStore().get(OUTPUT_PATH_KEY);
  }

  public Path setOutputPath(Path path) {
    return (Path) getStore().put(OUTPUT_PATH_KEY, path);
  }

  public Object get(String key, Object defaultValue) {
    return getStore().getOrDefault(key, defaultValue);
  }

  public Object put(String key, Object value) {
    return getStore().put(key, value);
  }
}
