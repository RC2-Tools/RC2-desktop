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

package edu.uw.cse.ifrcdemo.distplan.ui.other;

import edu.uw.cse.ifrcdemo.sharedlib.generator.BarcodeVoucher;

import javax.validation.constraints.NotNull;
import java.util.List;

public class BarcodeFormModel {

  private String text;
  @NotNull
  private Integer rangeStart;
  private Integer rangeEnd;

  private List<BarcodeVoucher> voucherList;

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Integer getRangeStart() {
    return rangeStart;
  }

  public void setRangeStart(Integer rangeStart) {
    this.rangeStart = rangeStart;
  }

  public Integer getRangeEnd() {
    return rangeEnd;
  }

  public void setRangeEnd(Integer rangeEnd) {
    this.rangeEnd = rangeEnd;
  }

  public List<BarcodeVoucher> getVoucherList() {
    return voucherList;
  }

  public void setVoucherList(List<BarcodeVoucher> voucherList) {
    this.voucherList = voucherList;
  }

  @Override
  public String toString() {
    return "BarcodeFormModel{" + "text='" + text + '\'' + ", rangeStart=" + rangeStart
        + ", rangeEnd=" + rangeEnd + ", voucherList=" + voucherList + '}';
  }
}
