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

package edu.uw.cse.ifrcdemo.setup.logic;

import com.github.mustachejava.MustacheFactory;
import edu.uw.cse.ifrcdemo.mustachetopdf.PdfGenerator;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.sharedlib.generator.BarcodeGeneratorConst;
import edu.uw.cse.ifrcdemo.sharedlib.generator.BarcodeGeneratorSingleton;
import edu.uw.cse.ifrcdemo.sharedlib.generator.BarcodeVoucher;
import edu.uw.cse.ifrcdemo.sharedlib.util.DialogUtil;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;

import javax.swing.SwingWorker;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BarcodeGenWorker extends SwingWorker<Void, Void> {

  private final Path outputPath;
  private final int startIndex;
  private final int endIndex;
  private final String text;
  private final MustacheFactory mustacheFactory;

  public BarcodeGenWorker(Path outputPath, int startIndex, int endIndex, String text, MustacheFactory mustacheFactory) {
    super();

    this.outputPath = outputPath;
    this.startIndex = startIndex;
    this.endIndex = endIndex;
    this.text = text;
    this.mustacheFactory = mustacheFactory;
  }

  @Override
  protected Void doInBackground() throws Exception {
    Map<String, List<BarcodeVoucher>> scopes = IntStream
        .rangeClosed(startIndex, endIndex)
        .mapToObj(String::valueOf)
        .parallel()
        .map(i -> new BarcodeVoucher(i, text, BarcodeGeneratorConst.DATA_URI_PREFIX + BarcodeGeneratorSingleton.generateBase64Barcode(i)))
        .collect(Collectors.collectingAndThen(Collectors.toList(), list -> Collections.singletonMap(
            BarcodeGeneratorConst.VOUCHERS, list)));

    PdfGenerator.generatePdf(mustacheFactory.compile(BarcodeGeneratorConst.TEMPLATE_PATH), scopes, resolveOutputPath());

    return null;
  }

  @Override
  protected void done() {
    try {
      get();
    } catch (InterruptedException | ExecutionException e) {
      e.getCause().printStackTrace();
      String msg = TranslationUtil.getTranslations().getString(TranslationConsts.FAILED_TO_GENERATE_ERROR) + GenConsts.SPACE;
      DialogUtil.showErrorDialog(msg + e.getCause().toString());
    }
  }

  private Path resolveOutputPath() {
    String fileName = String.join(GenConsts.UNDERSCORE, new String[] {
        BarcodeGeneratorConst.OUTPUT_FILE_PREFIX,
        text,
        String.valueOf(startIndex),
        String.valueOf(endIndex)
    }) + GenConsts.PDF_FILE_EXTENSION;

    return outputPath.resolve(fileName);
  }
}
