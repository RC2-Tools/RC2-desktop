package edu.uw.cse.ifrcdemo.distplan.model.csv;

import edu.uw.cse.ifrcdemo.sharedlib.model.row.BaseSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.UntypedSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.SyncRow;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MockCsvRepository implements CsvRepository {
  private final Map<String, Map<String, ? extends BaseSyncRow>> csv;

  public MockCsvRepository(Map<String, List<UntypedSyncRow>> untypedCsv,
                           Map<Class<? extends BaseSyncRow>, List<? extends BaseSyncRow>> typedCsv) {
    Objects.requireNonNull(untypedCsv);
    Objects.requireNonNull(typedCsv);

    this.csv = new ConcurrentHashMap<>(untypedCsv.size() + typedCsv.size());

    untypedCsv
        .entrySet()
        .stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> entry.getValue().stream().collect(Collectors.toMap(SyncRow::getRowId, Function.identity()))
        ))
        .forEach(csv::put);

    typedCsv
        .entrySet()
        .stream()
        .collect(Collectors.toMap(
            entry -> classToString(entry.getKey()),
            entry -> entry.getValue().stream().collect(Collectors.toMap(SyncRow::getRowId, Function.identity()))
        ))
        .forEach(csv::put);
  }

  private static String classToString(Class<?> clazz) {
    return clazz.getCanonicalName();
  }

  @Override
  public CompletableFuture<List<UntypedSyncRow>> readUntypedCsv(String filename, InputStream input, boolean purgeCache) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T extends BaseSyncRow> CompletableFuture<List<? extends BaseSyncRow>> readTypedCsv(Class<T> clazz, InputStream input, boolean purgeCache) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<List<UntypedSyncRow>> readUntypedCsv(String filename) {
    return Optional
        .ofNullable(csv.get(filename))
        .map(Map::values)
        .map(x -> ((Collection<UntypedSyncRow>) x))
        .map(ArrayList::new)
        .map(Collections::unmodifiableList);
  }

  @Override
  public <T extends BaseSyncRow> Optional<List<T>> readTypedCsv(Class<T> clazz) {
    return Optional
        .ofNullable(csv.get(classToString(clazz)))
        .map(Map::values)
        .map(x -> ((Collection<T>) x))
        .map(ArrayList::new)
        .map(Collections::unmodifiableList);
  }

  @Override
  public Optional<Map<String, UntypedSyncRow>> readIndexedUntypedCsv(String filename) {
    return Optional
        .ofNullable(csv.get(filename))
        .map(x -> ((Map<String, UntypedSyncRow>) x))
        .map(Collections::unmodifiableMap);
  }

  @Override
  public <T extends BaseSyncRow> Optional<Map<String, T>> readIndexedTypedCsv(Class<T> clazz) {
    return Optional
        .ofNullable(csv.get(classToString(clazz)))
        .map(x -> ((Map<String, T>) x))
        .map(Collections::unmodifiableMap);
  }
}
