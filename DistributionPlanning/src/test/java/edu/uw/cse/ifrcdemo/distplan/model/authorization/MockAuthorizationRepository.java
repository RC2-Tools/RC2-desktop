package edu.uw.cse.ifrcdemo.distplan.model.authorization;

import edu.uw.cse.ifrcdemo.distplan.entity.Authorization;
import edu.uw.cse.ifrcdemo.distplan.entity.ItemPack;
import edu.uw.cse.ifrcdemo.distplan.model.form.XlsxForm;
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
  public CompletableFuture<List<Authorization>> getAuthorizations(ItemPack itemPack) {
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
