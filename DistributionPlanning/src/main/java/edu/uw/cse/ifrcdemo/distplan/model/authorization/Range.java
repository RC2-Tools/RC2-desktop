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

package edu.uw.cse.ifrcdemo.distplan.model.authorization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.stream.LongStream;

public class Range extends RangeDescriptor {

  @JsonProperty(required = true)
  private long min;

  @JsonProperty(required = true)
  private long max;

  public Range() {
    this.min = -1;
    this.max = -1;
  }

  @JsonCreator
  public Range(@JsonProperty(value = "min", required = true) long min,
               @JsonProperty(value = "max", required = true) long max) {
    this.min = min;
    this.max = max;
  }

  public long getMin() {
    return this.min;
  }

  public void setMin(long min) {
    this.min = min;
  }

  public long getMax() {
    return this.max;
  }

  public void setMax(long max) {
    this.max = max;
  }

  public LongStream toLongStream() {
    return LongStream.rangeClosed(getMin(), getMax());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Range range = (Range) o;

    if (getMin() != range.getMin()) return false;
    return getMax() == range.getMax();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getMin(), getMax());
  }

  @Override
  public String toString() {
    return "Range{" +
        "min=" + min +
        ", max=" + max +
        "}";
  }
}
