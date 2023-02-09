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

package edu.uw.cse.ifrcdemo.sharedlib.util;

import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.SuitcaseUploadOperation;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.BaseSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvSuitcaseSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.SyncRow;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvDiffGeneratorTest {
  @Test
  void generate() {
    SyncRow oldRow1 = new TestSyncRow("id-1", "A", "B");
    SyncRow oldRow2 = new TestSyncRow("id-2", "AA", "BB");
    SyncRow oldRow3 = new TestSyncRow("id-3", "AAA", "BBB");

    SyncRow newRow1 = new TestSyncRow("id-1", "A", "B");
    SyncRow newRow2 = new TestSyncRow("id-2", "A_NEW", "B_NEW");
    SyncRow newRow3 = new TestSyncRow("id-10", "A-10", "B-10");

    List<CsvSuitcaseSyncRow<SyncRow>> diffList = CsvDiffGenerator.generate(
        Arrays.asList(oldRow1, oldRow2, oldRow3),
        Arrays.asList(newRow1, newRow2, newRow3)
    );

    assertFalse(diffList.contains(new CsvSuitcaseSyncRow<>(SuitcaseUploadOperation.FORCE_UPDATE, oldRow1)));
    assertFalse(diffList.contains(new CsvSuitcaseSyncRow<>(SuitcaseUploadOperation.FORCE_UPDATE, oldRow2)));
    assertTrue(diffList.contains(new CsvSuitcaseSyncRow<>(SuitcaseUploadOperation.DELETE, oldRow3)));

    assertFalse(diffList.contains(new CsvSuitcaseSyncRow<>(SuitcaseUploadOperation.FORCE_UPDATE, newRow1)));
    assertTrue(diffList.contains(new CsvSuitcaseSyncRow<>(SuitcaseUploadOperation.FORCE_UPDATE, newRow2)));
    assertTrue(diffList.contains(new CsvSuitcaseSyncRow<>(SuitcaseUploadOperation.FORCE_UPDATE, newRow3)));

    assertEquals(3, diffList.size());
  }

  private class TestSyncRow extends BaseSyncRow {
    String fieldA;
    String fieldB;

    public TestSyncRow() {
    }

    public TestSyncRow(String rowId, String fieldA, String fieldB) {
      this.fieldA = fieldA;
      this.fieldB = fieldB;

      setRowId(rowId);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      if (!super.equals(o)) return false;
      TestSyncRow that = (TestSyncRow) o;
      return Objects.equals(fieldA, that.fieldA) && Objects.equals(fieldB, that.fieldB);
    }

    @Override
    public int hashCode() {
      return Objects.hash(super.hashCode(), fieldA, fieldB);
    }

    @Override
    public String toString() {
      return "TestSyncRow{" + "fieldA='" + fieldA + '\'' + ", fieldB='" + fieldB + '\'' + "} " + super.toString();
    }
  }
}