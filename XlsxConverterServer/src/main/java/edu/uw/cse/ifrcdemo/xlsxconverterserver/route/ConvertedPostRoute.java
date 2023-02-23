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
import edu.uw.cse.ifrcdemo.xlsxconverterserver.consts.FileConst;
import edu.uw.cse.ifrcdemo.xlsxconverterserver.consts.RequestConst;
import edu.uw.cse.ifrcdemo.xlsxconverterserver.handler.FormUploadConsumer;
import edu.uw.cse.ifrcdemo.xlsxconverterserver.model.ConvertedXlsx;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class ConvertedPostRoute implements Route {
  private static final Set<String> CONSUMABLE_PARTS;

  static {
    Set<String> parts = new HashSet<>();

    parts.add(FileConst.FORM_DEF_JSON);
    parts.add(FileConst.DEFINITION_CSV);
    parts.add(FileConst.PROPERTIES_CSV);
    parts.add(FileConst.TABLE_SPECIFIC_DEF_JS);

    CONSUMABLE_PARTS = Collections.unmodifiableSet(parts);
  }

  private final FormUploadConsumer formUploadConsumer;
  private final Map<String, Map<String, CompletableFuture<ConvertedXlsx>>> requestFutures;
  private final Predicate<Request> requestPredicate;

  private final ObjectReader warningJsonReader;

  public ConvertedPostRoute(FormUploadConsumer formUploadConsumer,
                            Map<String, Map<String, CompletableFuture<ConvertedXlsx>>> requestFutures,
                            Predicate<Request> requestPredicate) {
    this.formUploadConsumer = formUploadConsumer;
    this.requestFutures = requestFutures;
    this.requestPredicate = requestPredicate;

    this.warningJsonReader = new ObjectMapper().readerFor(String.class);
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement(GenConsts.EMPTY_STRING));

    if (!requestPredicate.test(request)) {
      response.status(GenConsts.HTML_CLIENT_BAD_REQUEST_CODE);
      return GenConsts.EMPTY_STRING;
    }

    List<String> formDefWarnings = Collections.emptyList();

    for (Part part : request.raw().getParts()) {
      if (part.getSize() == 0) {
        continue;
      }

      try(InputStream stream = part.getInputStream()) {
        if (CONSUMABLE_PARTS.contains(part.getName())) {
          formUploadConsumer.accept(request::params, part.getName(), stream);
        } else {
          if (part.getName().equals(FileConst.FORM_WARNINGS_JSON)) {
            formDefWarnings = warningJsonReader
                .<String>readValues(stream)
                .readAll();
          }
        }
      }
    }

    requestFutures
        .getOrDefault(request.params(RequestConst.REQUEST_ID_PARAM), Collections.emptyMap())
        .getOrDefault(request.params(RequestConst.POST_FORM_FILENAME_PARAM), new CompletableFuture<>())
        .complete(new ConvertedXlsx(
            request.params(RequestConst.POST_FORM_TABLE_ID_PARAM),
            request.params(RequestConst.POST_FORM_FORM_ID_PARAM),
            formDefWarnings,
            request.params(RequestConst.POST_FORM_FILENAME_PARAM)
        ));

    return GenConsts.EMPTY_STRING;
  }
}
