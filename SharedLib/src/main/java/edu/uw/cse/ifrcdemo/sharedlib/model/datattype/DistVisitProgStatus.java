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

package edu.uw.cse.ifrcdemo.sharedlib.model.datattype;

import edu.uw.cse.ifrcdemo.translations.TranslationConsts;

public enum DistVisitProgStatus {
  /**
   * The distribution is active, entitlements can be generated from it, and deliveries can be made against it
   */
  ACTIVE(TranslationConsts.AUTH_ACTIVE),
  /**
   * Existing entitlements can generate deliveries, but no new entitlements can be made from it
   */
  INACTIVE(TranslationConsts.AUTH_INACTIVE),
  /**
   * No deliveries should be made from this distribution, including existing entitlements
   */
  DISABLED(TranslationConsts.AUTH_DISABLED),
  /**
   * The distribution should be removed from the mobile db
   */
  REMOVED(TranslationConsts.AUTH_REMOVED);

  private final String displayNameKey;

  public String getDisplayNameKey() {
    return displayNameKey;
  }

  DistVisitProgStatus(String displayNameKey) {
    this.displayNameKey = displayNameKey;
  }
}
