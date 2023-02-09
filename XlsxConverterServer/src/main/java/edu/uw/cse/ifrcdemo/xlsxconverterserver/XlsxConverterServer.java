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

package edu.uw.cse.ifrcdemo.xlsxconverterserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.xlsxconverterserver.consts.RequestConst;
import edu.uw.cse.ifrcdemo.xlsxconverterserver.handler.FilesFormUploadConsumer;
import edu.uw.cse.ifrcdemo.xlsxconverterserver.handler.FilesXlsxSupplier;
import edu.uw.cse.ifrcdemo.xlsxconverterserver.handler.FormUploadConsumer;
import edu.uw.cse.ifrcdemo.xlsxconverterserver.handler.FormUploadValidator;
import edu.uw.cse.ifrcdemo.xlsxconverterserver.model.ConversionError;
import edu.uw.cse.ifrcdemo.xlsxconverterserver.model.ConvertedXlsx;
import edu.uw.cse.ifrcdemo.xlsxconverterserver.route.ConversionErrorRoute;
import edu.uw.cse.ifrcdemo.xlsxconverterserver.route.ConvertedPostRoute;
import edu.uw.cse.ifrcdemo.xlsxconverterserver.route.XlsxDownloadRoute;
import spark.Spark;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.initExceptionHandler;
import static spark.Spark.ipAddress;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.staticFileLocation;

public class XlsxConverterServer {
  private static final String STATIC_FILE_LOCATION = "/public";

  private final Map<String, Set<String>> convertRequests;
  private final Map<String, Map<String, CompletableFuture<ConvertedXlsx>>> requestFutures;
  private final ObjectWriter objectWriter;

  private Map<String, Set<String>> getConvertRequests() {
    return convertRequests;
  }

  public Map<String, Map<String, CompletableFuture<ConvertedXlsx>>> getRequestFutures() {
    return requestFutures;
  }

  private ObjectWriter getObjectWriter() {
    return objectWriter;
  }

  private static XlsxConverterServer instance;

  public static XlsxConverterServer getInstance() {
    if (instance == null) {
      synchronized (XlsxConverterServer.class) {
        if (instance == null) {
          instance = new XlsxConverterServer();
        }
      }
    }

    return instance;
  }

  private XlsxConverterServer() {
    this.convertRequests = new ConcurrentHashMap<>();
    this.requestFutures = new ConcurrentHashMap<>();
    this.objectWriter = new ObjectMapper().writer();

    ipAddress("localhost");
    port(0); // use any available port
    staticFileLocation(STATIC_FILE_LOCATION);

    exception(Exception.class, (exception, request, response) -> {
      String requestId = request.params(RequestConst.REQUEST_ID_PARAM);
      String filename = request.params(RequestConst.POST_FORM_FILENAME_PARAM);

      if (requestId != null && requestFutures.containsKey(requestId)) {
        Map<String, CompletableFuture<ConvertedXlsx>> futures = requestFutures.get(requestId);

        if (filename != null && futures.containsKey(filename)) {
          futures.get(filename).completeExceptionally(exception);
        } else {
          futures.forEach((__, future) -> future.completeExceptionally(exception));
        }
      }
    });
  }

  public void init(Consumer<Exception> initExceptionHandler) {
    if (initExceptionHandler != null) {
      initExceptionHandler(initExceptionHandler);
    }

    // put handler configuration here because they trigger initialization
    configureLandingPage();
    configureXlsxListGet();
  }

  public String submitConvertRequest(Set<String> xlsxFiles) {
    String requestId = UUID.randomUUID().toString();

    getConvertRequests().put(requestId, xlsxFiles);
    return requestId;
  }

  public Map<String, CompletableFuture<ConvertedXlsx>> getCompletableFuture(String requestId) {
    Map<String, CompletableFuture<ConvertedXlsx>> futures = getRequestFutures().computeIfAbsent(requestId, key -> {
      if (getConvertRequests().containsKey(key)) {
        return getConvertRequests()
            .get(key)
            .stream()
            .collect(Collectors.toConcurrentMap(
                Function.identity(),
                __ -> new CompletableFuture<>()
            ));
      } else {
        // invalid key
        throw new IllegalArgumentException("Invalid requestId");
      }
    });

    return Collections.unmodifiableMap(futures);
  }

  public void configureXlsxGet(Function<String, byte[]> fileBytesSupplier) {
    get(
        Util.buildPath(
            RequestConst.REQUEST_ID_PARAM,
            RequestConst.GET_XLSX_FILENAME_PARAM
        ),
        new XlsxDownloadRoute(fileBytesSupplier)
    );
  }

  public void configureXlsxGet() {
    configureXlsxGet(new FilesXlsxSupplier());
  }

  public void configureXlsxPost(FormUploadConsumer formUploadConsumer) {
    String path = Util.buildPath(
        RequestConst.REQUEST_ID_PARAM,
        RequestConst.POST_FORM_FILENAME_PARAM,
        RequestConst.POST_FORM_TABLE_ID_PARAM,
        RequestConst.POST_FORM_FORM_ID_PARAM
    );

    post(path, GenConsts.MULTIPART_FORM_MIME, new ConvertedPostRoute(
        formUploadConsumer,
        getRequestFutures(),
        new FormUploadValidator(getConvertRequests()))
    );
  }

  public void configureXlsxPost(Path outputPath) {
    configureXlsxPost(new FilesFormUploadConsumer(__ -> outputPath));
  }

  public void configureXlsxPostError(BiConsumer<String, ConversionError> errorConsumer) {
    String path = Util.buildPath(
        RequestConst.REQUEST_ID_PARAM,
        RequestConst.POST_FORM_FILENAME_PARAM
    );

    post(path, GenConsts.JSON_MIME, new ConversionErrorRoute(
        errorConsumer,
        getRequestFutures(),
        r -> true
    ));
  }

  public void configureXlsxPostError() {
    configureXlsxPostError((__, ___) -> {});
  }

  public CompletableFuture<Void> awaitInitialization() {
    return CompletableFuture.runAsync(Spark::awaitInitialization);
  }

  public void stop() {
    Spark.stop();
  }

  public int getPort() {
    return Spark.port();
  }

  public URI getUriForRequest(String request) throws URISyntaxException {
    return new URI("http", null, "localhost", getPort(), "/xlsx", null, request);
  }

  private void configureLandingPage() {
    get(Util.buildPath(), ((request, response) ->
        "<!DOCTYPE html>" +
            "<html lang=\"en\">" +
            "<head>" +
            "<meta charset=\"UTF-8\">" +
            "<title>XLSXConverter Embedded</title>" +
            "<script src=\"main.js\"></script>" +
            "</head>" +
            "<body>" +
            "<h1>XLSXConverter Embedded</h1>" +
            "<div>Converting XLSX files</div>" +
            "</body>" +
            "</html>")
    );
  }

  private void configureXlsxListGet() {
    get(Util.buildPath(RequestConst.REQUEST_ID_PARAM), ((request, response) -> {
      String requestId = request.params(RequestConst.REQUEST_ID_PARAM);

      if (requestId == null) {
        return null;
      }

      return getObjectWriter().writeValueAsString(getConvertRequests().get(requestId));
    }));
  }
}
