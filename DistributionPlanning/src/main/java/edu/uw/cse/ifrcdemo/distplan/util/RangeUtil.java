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

package edu.uw.cse.ifrcdemo.distplan.util;

import edu.uw.cse.ifrcdemo.distplan.model.authorization.Range;
import edu.uw.cse.ifrcdemo.distplan.model.authorization.RangeDescriptor;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.Set;
import java.util.stream.LongStream;

public class RangeUtil {
  public static LongStream toLongStream(List<Range> ranges) {
    if (ranges != null) {
      return ranges.stream().map(Range::toLongStream).reduce(LongStream::concat).orElse(LongStream.empty());
    } else {
      return LongStream.empty();
    }
  }

  public static long getTotalCount(List<Range> ranges) {
    if (ranges.isEmpty()) {
      return Long.MAX_VALUE;
    }
    long total = 0;
    for (Range range : ranges) {
      total += range.getMax() - range.getMin() + 1;
    }

    return total;
  }

  /**
   *
   * @param ofInt
   * @param usedInts
   * @return
   * @throws NoSuchElementException if the iteration has no more elements
   */
  public static int getNextAvailable(PrimitiveIterator.OfInt ofInt, Set<Integer> usedInts) {
    Integer nextCandidate = ofInt.next();

    while (usedInts.contains(nextCandidate)) {
      nextCandidate = ofInt.next();
    }

    return nextCandidate;
  }

  // given a list of ranges and a new range, mutate list to include range and remove overlaps
  public static <T extends RangeDescriptor> T checkOverlap(T range, List<T> ranges) {
    boolean found = false;
    T overlapped = null;
    int i = 0;
    while (!found && i < ranges.size()) {
      T r = ranges.get(i);
      if (((r.getMin() <= range.getMax() + 1) && (r.getMax() >= range.getMin() - 1)) || (r.equals(range))) {
        found = true;
        overlapped = r;
      } else {
        i++;
      }
    }
    if (!found) {
      return range;
    } else {
      overlapped.setMin(Math.min(overlapped.getMin(), range.getMin()));
      overlapped.setMax(Math.max(overlapped.getMax(), range.getMax()));
      ranges.remove(range);
      return checkOverlap(overlapped, ranges);
    }
  }

  public static String toDisplayString(Range r) {
    return r.getMin() + GenConsts.DASH + r.getMax();
  }

  public static ArrayList<Range> calculateRangesFromInts(List<Integer> values) {
    ArrayList<Range> ranges = new ArrayList<Range>();
    // May need to remove duplicates here
    int len = values.size();
    int idx = 0, idx2 = 0;
    while (idx < len) {
      while (++idx2 < len && values.get(idx2) - values.get(idx2 - 1) == 1);
      if (idx2 - idx > 2) {
        ranges.add(new Range(values.get(idx), values.get(idx2 - 1)));
        idx = idx2;
      } else {
        for (; idx < idx2; idx++)
          ranges.add(new Range(values.get(idx), values.get(idx)));
      }
    }
    return ranges;
  }
}
