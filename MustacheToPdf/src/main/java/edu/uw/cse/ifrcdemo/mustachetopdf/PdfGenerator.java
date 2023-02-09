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

package edu.uw.cse.ifrcdemo.mustachetopdf;

import com.github.mustachejava.Mustache;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.slf4j.Slf4jLogger;
import com.openhtmltopdf.util.XRLog;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.w3c.dom.Document;

import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.openhtmltopdf.pdfboxout.PdfRendererBuilder.PAGE_SIZE_LETTER_HEIGHT;
import static com.openhtmltopdf.pdfboxout.PdfRendererBuilder.PAGE_SIZE_LETTER_UNITS;
import static com.openhtmltopdf.pdfboxout.PdfRendererBuilder.PAGE_SIZE_LETTER_WIDTH;

public class PdfGenerator {
	private static final String PDF_RES_DIR = "/static";
	private static final String PDF_TEMP_RES_DIR = "/static";

	public static void generatePdf(Mustache mustache, Object scopes, Path output) throws Exception {
		Writer sw = new StringWriter();
		mustache.execute(sw, scopes).flush();
		String mustacheString = sw.toString();
		generatePdf(mustacheString, output);
	}

	public static void generatePdf(String html5, Path output) throws Exception {
		W3CDom w3cDom = new W3CDom();
		org.jsoup.nodes.Document jSoupDoc = Jsoup.parse(html5);
		Document document = w3cDom.fromJsoup(jSoupDoc);
		generatePdf(document, output);
	}

	// TODO: Remove these once style is working
	public static void generateCustomPdf(String html5, Path output) throws Exception {
		W3CDom w3cDom = new W3CDom();
		org.jsoup.nodes.Document jSoupDoc = Jsoup.parse(html5);
		Document document = w3cDom.fromJsoup(jSoupDoc);
		generateCustomPdf(document, output);
	}

	public static void generateCustomPdf(Document document, Path output) throws Exception {
//		XRLog.setLoggingEnabled(true);
//		XRLog.setLoggerImpl(new Slf4jLogger());

		try (OutputStream stream = Files.newOutputStream(output)) {
			PdfRendererBuilder builder = new PdfRendererBuilder().useUriResolver((baseUri, uri) -> {
				if (uri == null || uri.isEmpty()) {
					return null;
				}

				try {
					URI uri1 = new URI(uri);

					if (uri1.isAbsolute()) {
						return uri1.toString();
					}

					String separator = (baseUri.endsWith(GenConsts.PATH_SEPARATOR) || uri.startsWith(GenConsts.PATH_SEPARATOR)) ? GenConsts.EMPTY_STRING : GenConsts.PATH_SEPARATOR;

					return PdfGenerator.class.getResource(baseUri + separator + uri).toString();
				} catch (URISyntaxException e) {
					e.printStackTrace();
					return null;
				}
			}).withW3cDocument(document, PDF_TEMP_RES_DIR).toStream(stream);

			// TODO: Do we ever want to use portrait mode?
			builder.useDefaultPageSize(PAGE_SIZE_LETTER_HEIGHT, PAGE_SIZE_LETTER_WIDTH,
					PAGE_SIZE_LETTER_UNITS);
			builder.run();
		}
	}

	public static void generatePdf(Document document, Path output) throws Exception {
//		XRLog.setLoggingEnabled(true);
//		XRLog.setLoggerImpl(new Slf4jLogger());

		try (OutputStream stream = Files.newOutputStream(output)) {
			PdfRendererBuilder builder = new PdfRendererBuilder().useUriResolver((baseUri, uri) -> {
				if (uri == null || uri.isEmpty()) {
					return null;
				}

				try {
					URI uri1 = new URI(uri);

					if (uri1.isAbsolute()) {
						return uri1.toString();
					}

					String separator = (baseUri.endsWith(GenConsts.PATH_SEPARATOR) || uri.startsWith(GenConsts.PATH_SEPARATOR)) ? GenConsts.EMPTY_STRING : GenConsts.PATH_SEPARATOR;

					return PdfGenerator.class.getResource(baseUri + separator + uri).toString();
				} catch (URISyntaxException e) {
					e.printStackTrace();
					return null;
				}
			}).withW3cDocument(document, PDF_RES_DIR).toStream(stream);

			builder.run();
		} catch (Exception e) {
			throw e;
		}
	}
}
