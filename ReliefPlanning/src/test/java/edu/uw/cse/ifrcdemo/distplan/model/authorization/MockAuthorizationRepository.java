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

package edu.uw.cse.ifrcdemo.distplan.model.authorization;

import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Authorization;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Item;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.authorization.AuthorizationRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.form.XlsxForm;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationType;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MockAuthorizationRepository implements AuthorizationRepository {
  public MockAuthorizationRepository() {
    System.out.println("[" + getClass().getSimpleName() + "] Using Mock Implementation");
  }

  @Override
  public CompletableFuture<Authorization> getAuthorization(Long id) {
    return CompletableFuture.supplyAsync(Authorization::new);
  }

  @Override
  public CompletableFuture<List<Authorization>> getAuthorizations(AuthorizationStatus status) {
    return null;
  }

  @Override
  public CompletableFuture<List<Authorization>> getAuthorizations(AuthorizationType type) {
    return null;
  }

  @Override
  public CompletableFuture<List<Authorization>> getAuthorizations(AuthorizationStatus status,
      AuthorizationType type) {
    return null;
  }

  @Override
  public CompletableFuture<List<Authorization>> getAuthorizations(Item item) {
    return null;
  }

  @Override
  public CompletableFuture<Authorization> saveAuthorization(Authorization authorization) {
    return new CompletableFuture<>();
  }

  @Override
  public CompletableFuture<Authorization> updateAuthorizationStatus(Long id, AuthorizationStatus status) {
    return new CompletableFuture<>();
  }

  @Override
  public List<XlsxForm> getAllForms() {
    return Collections.emptyList();
  }
}
