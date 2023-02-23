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

package edu.uw.cse.ifrcdemo.distplan.model.csv;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.BaseSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.UntypedSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.util.CsvMapperUtil;
import edu.uw.cse.ifrcdemo.translations.LogStr;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FileCsvRepository implements CsvRepository {


  private final Map<String, List<UntypedSyncRow>> untypedCsv;
  private final Map<Class<? extends BaseSyncRow>, List<? extends BaseSyncRow>> typedCsv;
  private final Map<String, Map<String, ? extends BaseSyncRow>> indexedCsv; // TODO: not efficient

  private final CsvMapper mapper;
  private final Map<Class<? extends BaseSyncRow>, ObjectReader> objectReaderCache;

  private final Logger logger;

  private Map<String, List<UntypedSyncRow>> getUntypedCsv() {
    return untypedCsv;
  }

  private Map<Class<? extends BaseSyncRow>, List<? extends BaseSyncRow>> getTypedCsv() {
    return typedCsv;
  }

  private Map<String, Map<String, ? extends BaseSyncRow>> getIndexedCsv() {
    return indexedCsv;
  }

  private CsvMapper getMapper() {
    return mapper;
  }

  private Map<Class<? extends BaseSyncRow>, ObjectReader> getObjectReaderCache() {
    return objectReaderCache;
  }

  public FileCsvRepository() {
    this.untypedCsv = new ConcurrentHashMap<>();
    this.typedCsv = new ConcurrentHashMap<>();
    this.indexedCsv = new ConcurrentHashMap<>();

    this.mapper = CsvMapperUtil.getCsvMapper(true);
    this.objectReaderCache = new ConcurrentHashMap<>();

    this.logger = LogManager.getLogger(FileCsvRepository.class);
  }

  /**
   * Reads in an untyped csv and stores in cache by path
   *
   * @param filename
   * @param input
   * @param purgeCache
   * @return
   */
  @Override
  public CompletableFuture<List<UntypedSyncRow>> readUntypedCsv(String filename, InputStream input, boolean purgeCache) {
    return CompletableFuture.supplyAsync(() -> getUntypedCsv().compute(filename, (__, curr) -> {
      try {
        if (curr == null || purgeCache) {
          logger.debug(LogStr.LOG_READING_TABLE_FROM, filename::toString, input::toString);

          try (MappingIterator<UntypedSyncRow> iterator = getObjectReader(UntypedSyncRow.class).readValues(input)) {
            return Collections.unmodifiableList(iterator.readAll());
          }
        } else {
          logger.debug(LogStr.LOG_TABLE_ALREADY_EXISTS, filename);
          return Collections.unmodifiableList(curr);
        }
      } catch (IOException e) {
        String tmp = TranslationUtil.getTranslations().getString(TranslationConsts.FAILED_READ_ERROR) + GenConsts.SPACE;
        throw new CompletionException(tmp + filename, e);
      } finally {
        try {
          input.close();
        } catch (IOException e) {
          logger.catching(e);
        }
      }
    })).thenApply(list -> {
      logger.debug(LogStr.LOG_READ_ROWS_FOR, list.size(), filename);
      logger.debug(LogStr.LOG_COMPUTING_INDEX_FOR, filename);

      getIndexedCsv().put(filename, indexRowList(list));
      return list;
    });
  }

  /**
   * Reads in a typed csv and stores in cache by path
   *
   * @param clazz
   * @param input
   * @param purgeCache
   * @return
   */
  @Override
  public <T extends BaseSyncRow> CompletableFuture<List<? extends BaseSyncRow>> readTypedCsv(Class<T> clazz,
                                                                                             InputStream input,
                                                                                             boolean purgeCache) {
    return CompletableFuture.supplyAsync(() -> getTypedCsv().compute(clazz, (__, curr) -> {
      try {
        if (curr == null || purgeCache) {
          logger.debug(LogStr.LOG_READING_TABLE_FROM, clazz::getSimpleName, input::toString);

          try (MappingIterator<T> iterator = getObjectReader(clazz).readValues(input)) {
            return Collections.unmodifiableList(iterator.readAll());
          }
        } else {
          logger.debug(LogStr.LOG_TABLE_ALREADY_EXISTS, clazz::getSimpleName);
          return Collections.unmodifiableList(curr);
        }
      } catch (IOException e) {
        String tmp = TranslationUtil.getTranslations().getString(TranslationConsts.FAILED_READ_ERROR) + GenConsts.SPACE;
        throw new CompletionException(tmp + clazz.getSimpleName(), e);
      } finally {
        try {
          input.close();
        } catch (IOException e) {
          logger.catching(e);
        }
      }
    })).thenApply(list -> {
      logger.debug(LogStr.LOG_READ_ROWS_FOR, list::size, clazz::getSimpleName);
      logger.debug(LogStr.LOG_COMPUTING_INDEX_FOR, clazz::getSimpleName);

      getIndexedCsv().put(clazz.getCanonicalName(), indexRowList(list));
      return list;
    });
  }

  /**
   * Reads from cache
   *
   * @param filename
   * @return
   */
  @Override
  public Optional<List<UntypedSyncRow>> readUntypedCsv(String filename) {
    logger.trace(LogStr.LOG_READING_FROM_CACHE, filename);

    return Optional
        .ofNullable(getUntypedCsv().get(filename))
        .map(Collections::unmodifiableList);
  }

  /**
   * Reads from cache
   *
   * @param clazz
   * @return
   */
  @Override
  public <T extends BaseSyncRow> Optional<List<T>> readTypedCsv(Class<T> clazz) {
    logger.trace(LogStr.LOG_READING_FROM_CACHE, clazz::getSimpleName);

    return Optional
        .ofNullable(getTypedCsv().get(clazz))
        .map(list -> (List<T>) list)
        .map(Collections::unmodifiableList);
  }

  @Override
  public Optional<Map<String, UntypedSyncRow>> readIndexedUntypedCsv(String filename) {
    logger.trace(LogStr.LOG_READING_INDEXED_FROM_CACHE, filename);

    return Optional
        .ofNullable(getIndexedCsv().get(filename))
        .map(map -> (Map<String, UntypedSyncRow>) map)
        .map(Collections::unmodifiableMap);
  }

  @Override
  public <T extends BaseSyncRow> Optional<Map<String, T>> readIndexedTypedCsv(Class<T> clazz) {
    logger.trace(LogStr.LOG_READING_INDEXED_FROM_CACHE, clazz::getSimpleName);

    return Optional
        .ofNullable(getIndexedCsv().get(clazz.getCanonicalName()))
        .map(map -> (Map<String, T>) map)
        .map(Collections::unmodifiableMap);
  }

  private ObjectReader getObjectReader(Class<? extends BaseSyncRow> clazz) {
    return getObjectReaderCache()
        .computeIfAbsent(clazz, aClass -> CsvMapperUtil.getReader(aClass, getMapper(), true));
  }

  private <T extends BaseSyncRow> Map<String, T> indexRowList(List<T> list) {
    return indexRowList(list, BaseSyncRow::getRowId);
  }

  private <T extends BaseSyncRow> Map<String, T> indexRowList(List<T> list, Function<T, ? extends String> keyFunc) {
    return list.stream().collect(Collectors.toMap(keyFunc, Function.identity()));
  }
}
