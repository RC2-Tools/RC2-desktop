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

package edu.uw.cse.ifrcdemo.xlsxconverterserver.route;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.xlsxconverterserver.consts.RequestConst;
import edu.uw.cse.ifrcdemo.xlsxconverterserver.exception.FormConversionException;
import edu.uw.cse.ifrcdemo.xlsxconverterserver.model.ConversionError;
import edu.uw.cse.ifrcdemo.xlsxconverterserver.model.ConvertedXlsx;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class ConversionErrorRoute implements Route {
  private final BiConsumer<String, ConversionError> formErrorConsumer;
  private final Map<String, Map<String, CompletableFuture<ConvertedXlsx>>> requestFutures;
  private final Predicate<Request> requestPredicate;

  private final ObjectReader objectReader;

  public ConversionErrorRoute(BiConsumer<String, ConversionError> formErrorConsumer,
                              Map<String, Map<String, CompletableFuture<ConvertedXlsx>>> requestFutures,
                              Predicate<Request> requestPredicate) {
    this.formErrorConsumer = formErrorConsumer;
    this.requestFutures = requestFutures;
    this.requestPredicate = requestPredicate;

    this.objectReader = new ObjectMapper().readerFor(ConversionError.class);
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    if (!requestPredicate.test(request)) {
      response.status(GenConsts.HTML_CLIENT_BAD_REQUEST_CODE);
      return GenConsts.EMPTY_STRING;
    }

    String requestId = request.params(RequestConst.REQUEST_ID_PARAM);
    String filename = request.params(RequestConst.POST_FORM_FILENAME_PARAM);

    ConversionError error = objectReader.readValue(request.raw().getReader());

    formErrorConsumer.accept(filename, error);

    requestFutures
        .getOrDefault(requestId, Collections.emptyMap())
        .getOrDefault(filename, CompletableFuture.completedFuture(null))
        .completeExceptionally(new FormConversionException(error));

    return GenConsts.EMPTY_STRING;
  }
}
