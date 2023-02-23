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

package edu.uw.cse.ifrcdemo.distreport.util;

import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import org.opendatakit.sync.client.SyncClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class AttachmentUtil {

  public static String toDataUri(Path dataPath, String tableId, String rowId, String uriFragment, String mimeType) throws IOException {
    Path attachmentPath = dataPath.resolve(resolveAttachmentPath(tableId, rowId, uriFragment));
    return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(Files.readAllBytes(attachmentPath));
  }

  public static Path resolveAttachmentPath(String tableId, String rowId, String uriFragment) {
    return Paths
        .get(tableId)
        .resolve(SyncClient.INSTANCES_DIR)
        .resolve(convertRowIdForInstances(rowId))
        .resolve(uriFragment);
  }

  public static String convertRowIdForInstances(String rowId) {
    String rowIdToUse = null;
    if (rowId != null && rowId.length() != 0) {
      rowIdToUse = rowId.replaceAll("[\\p{Punct}\\p{Space}]", GenConsts.UNDERSCORE);
      return rowIdToUse;
    } else {
      String msg = TranslationUtil.getTranslations().getString(TranslationConsts.ROW_ID_NULL_OR_EMPTY_ERROR);
      throw new IllegalArgumentException(msg);
    }
  }
}
