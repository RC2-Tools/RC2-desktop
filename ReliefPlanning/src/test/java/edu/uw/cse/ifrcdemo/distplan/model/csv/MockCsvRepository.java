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

package edu.uw.cse.ifrcdemo.distplan.model.csv;

import edu.uw.cse.ifrcdemo.planningsharedlib.model.csv.CsvRepository;
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
