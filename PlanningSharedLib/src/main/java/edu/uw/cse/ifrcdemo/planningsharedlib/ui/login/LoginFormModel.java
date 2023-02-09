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

package edu.uw.cse.ifrcdemo.planningsharedlib.ui.login;

import edu.uw.cse.ifrcdemo.planningsharedlib.model.profile.Rc2Profile;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.Validation.Conditional;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.common.CloudEndpointAuthFormModel;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Conditional(
    selected = "type",
    values = {"RC2_SERVER"},
    required = {"serverUrl", "username", "password"}
)
@Conditional(
    selected = "type",
    values = {"LOCAL_DATA_DIRECTORY"},
    required = {"inputDataDirectory"}
)
public class LoginFormModel implements CloudEndpointAuthFormModel {

  private String serverUrl;
  private String username;
  private String password;
  private String inputDataDirectory;
  @NotNull
  private String type;
  private List<Rc2Profile> profileList;

  @NotEmpty
  private String profile;

  @Override
  public String getServerUrl() {
    return serverUrl;
  }

  @Override
  public void setServerUrl(String serverUrl) {
    if (serverUrl != null) { this.serverUrl = serverUrl.trim(); }
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public void setUsername(String username) {
    if (username != null) { this.username = username.trim(); }
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public void setPassword(String password) {
    if (password != null) { this.password = password.trim(); }
  }

  public String getInputDataDirectory() {
    return inputDataDirectory;
  }

  public void setInputDataDirectory(String inputDataDirectory) {
    this.inputDataDirectory = inputDataDirectory;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public List<Rc2Profile> getProfileList() {
    return profileList;
  }

  public void setProfileList(List<Rc2Profile> profileList) {
    this.profileList = profileList;
  }

  public String getProfile() {
    return profile;
  }

  public void setProfile(String profile) {
    this.profile = profile;
  }

  @Override
  public String toString() {
    return "LoginFormModel{" +
        "serverUrl='" + serverUrl + '\'' +
        ", username='" + username + '\'' +
        ", password='" + password + '\'' +
        ", inputDataDirectory='" + inputDataDirectory + '\'' +
        ", type='" + type + '\'' +
        ", profileList=" + profileList +
        ", profile='" + profile + '\'' +
        '}';
  }
}
