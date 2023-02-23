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

package edu.uw.cse.ifrcdemo.sharedlib.util;

import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.SuitcaseUploadOperation;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvSuitcaseSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.SyncRow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvDiffGenerator {
  private static final Logger logger = LogManager.getLogger(CsvDiffGenerator.class);

  public static <T extends SyncRow> List<CsvSuitcaseSyncRow<T>> generate(Collection<T> origColl, Collection<T> newColl) {
    Set<RowWrapper<T>> origIdSet = origColl.stream().map(RowWrapper::new).collect(Collectors.toSet());
    Set<RowWrapper<T>> newIdSet = newColl.stream().map(RowWrapper::new).collect(Collectors.toSet());

    origIdSet.removeAll(newIdSet);

    HashSet<T> updateSet = new HashSet<>(newColl);
    updateSet.removeAll(new HashSet<>(origColl));

    logger.info("Removing rows [{}]", () ->
        origIdSet.stream().map(RowWrapper::getRowId).collect(Collectors.joining(", ")));
    logger.info("Updating rows [{}]", () ->
        updateSet.stream().map(SyncRow::getRowId).collect(Collectors.joining(", ")));

    return Stream
        .concat(
            origIdSet
                .stream()
                .map(RowWrapper::getRow)
                .map(row -> new CsvSuitcaseSyncRow<>(SuitcaseUploadOperation.DELETE, row)),
            updateSet
                .stream()
                .map(row -> new CsvSuitcaseSyncRow<>(SuitcaseUploadOperation.FORCE_UPDATE, row))
        )
        .collect(Collectors.toList());
  }

  private static class RowWrapper<T extends SyncRow> {
    private final T row;

    public T getRow() {
      return row;
    }

    public String getRowId() {
      return getRow().getRowId();
    }

    public RowWrapper(T row) {
      this.row = row;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      RowWrapper that = (RowWrapper) o;

      return getRowId() != null ? getRowId().equals(that.getRowId()) : that.getRowId() == null;
    }

    @Override
    public int hashCode() {
      return 31 * (getRowId() != null ? getRowId().hashCode() : 0);
    }

    @Override
    public String toString() {
      return "RowWrapper{" +
          "row=" + row +
          '}';
    }
  }
}
