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

package edu.uw.cse.ifrcdemo.sharedlib.model.jacksonmixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.opendatakit.aggregate.odktables.rest.entity.RowFilterScope;
import org.opendatakit.sync.client.SyncClient;

public abstract class RowFilterScopeCsvMixin extends RowFilterScope {
  public RowFilterScopeCsvMixin(Access access, String rowOwner, String groupReadOnly, String groupModify, String groupPrivileged) {
    super(access, rowOwner, groupReadOnly, groupModify, groupPrivileged);
  }

  @Override
  @JsonProperty(SyncClient.DEFAULT_ACCESS_ROW_DEF)
  public abstract Access getDefaultAccess();

  @Override
  @JsonProperty(SyncClient.ROW_OWNER_ROW_DEF)
  public abstract String getRowOwner();

  @Override
  @JsonProperty(SyncClient.GROUP_READ_ONLY_ROW_DEF)
  public abstract String getGroupReadOnly();

  @Override
  @JsonProperty(SyncClient.GROUP_MODIFY_ROW_DEF)
  public abstract String getGroupModify();

  @Override
  @JsonProperty(SyncClient.GROUP_PRIVILEGED_ROW_DEF)
  public abstract String getGroupPrivileged();
}
