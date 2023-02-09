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

package edu.uw.cse.ifrcdemo.planningsharedlib.ui.other;

import edu.uw.cse.ifrcdemo.planningsharedlib.model.range.Range;
import edu.uw.cse.ifrcdemo.planningsharedlib.ui.util.ControllerPdfUtil;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.CsvFileUtil;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.RangeUtil;
import edu.uw.cse.ifrcdemo.sharedlib.generator.BarcodeGeneratorConst;
import edu.uw.cse.ifrcdemo.sharedlib.generator.BarcodeGeneratorSingleton;
import edu.uw.cse.ifrcdemo.sharedlib.generator.BarcodeVoucher;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/other")
@SessionAttributes(types = { BarcodeFormModel.class })
public class OtherController {
  private static final String OTHER_ITEMS_MENU = "other/otherItemsMenu";
  private static final String OTHER_BARCODE_GENERATOR = "other/barcodeGenerator";
  private static final String OTHER_BARCODE_OUTPUT = "other/barcodeOutput";

  private static final String FILE_LOCATION_QUERY_PARAM = "fileLocation";

  private final Logger logger;

  private final TemplateEngine templateEngine;

  public OtherController(Logger logger, TemplateEngine templateEngine) {
    this.logger = logger;
    this.templateEngine = templateEngine;
  }

  @ModelAttribute("barcodeFormModel")
  public BarcodeFormModel newBarcodeFormModel() {
    BarcodeFormModel barcodeFormModel = new BarcodeFormModel();
    return barcodeFormModel;
  }

  @GetMapping("")
  public String showOtherOptions() {
    return OTHER_ITEMS_MENU;
  }

  @GetMapping("barcodes")
  public ModelAndView showBarcodeGenerator(
      @ModelAttribute("barcodeFormModel") BarcodeFormModel barcodeFormModel) {
    return new ModelAndView(OTHER_BARCODE_GENERATOR);
  }

  @PostMapping("barcodes")
  public ModelAndView generateBarcodes(
      @Valid @ModelAttribute("barcodeFormModel") BarcodeFormModel barcodeFormModel,
      BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return new ModelAndView(OTHER_BARCODE_GENERATOR);
    }

    // Display the generated barcodes
    return new ModelAndView("redirect:barcodeOutput");
  }

  @GetMapping("barcodeOutput")
  public ModelAndView outputBarcodes(
      @Valid @ModelAttribute("barcodeFormModel") BarcodeFormModel barcodeFormModel,
      BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return new ModelAndView(OTHER_BARCODE_GENERATOR);
    }

    generateBarcodes(barcodeFormModel);

    // Display the generated barcodes
    return new ModelAndView(OTHER_BARCODE_OUTPUT);
  }

  @PostMapping("barcodeOutput")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<String> outputBarcodesDocument(
      @ModelAttribute("barcodeFormModel") BarcodeFormModel barcodeFormModel,
      HttpServletRequest request, HttpServletResponse response) {

    WebContext webContext = new WebContext(request, response, request.getServletContext());

    generateBarcodes(barcodeFormModel);

    // Write out the report
    webContext.setVariable("barcodeFormModel", barcodeFormModel);

    StringBuilder fileNameBuilder = new StringBuilder();
    fileNameBuilder.append("barcode");

    if (barcodeFormModel.getText() != null) {
      fileNameBuilder.append("_").append(barcodeFormModel.getText());
    }

    if (barcodeFormModel.getRangeStart() != null) {
      fileNameBuilder.append("_").append(barcodeFormModel.getRangeStart());
    }

    if (barcodeFormModel.getRangeEnd() != null) {
      fileNameBuilder.append("_").append(barcodeFormModel.getRangeEnd());
    }

    String filename = fileNameBuilder.toString();
    writeReport(webContext, filename, "other/barcodeOutputDocument.html");

    return new ResponseEntity<>("Success!", HttpStatus.OK);
  }

  private void generateBarcodes(
      @ModelAttribute("barcodeFormModel") BarcodeFormModel barcodeFormModel) {
    Integer start = barcodeFormModel.getRangeStart();
    Integer end = barcodeFormModel.getRangeEnd();

    if (start == null) {
      return;
    }

    if (end == null) {
      end = start;
    }

    if (end < start) {
      Integer temp = start;
      start = end;
      end = temp;
    }

    List<BarcodeVoucher> barcodes = IntStream
        .rangeClosed(start, end)
        .mapToObj(String::valueOf).parallel().map(
            i -> new BarcodeVoucher(i, barcodeFormModel.getText(),
                BarcodeGeneratorConst.DATA_URI_PREFIX + BarcodeGeneratorSingleton
                    .generateBase64Barcode(i))).collect(Collectors.toList());

    barcodeFormModel.setVoucherList(barcodes);
  }

  private void writeReport(WebContext webContext, String reportName, String templateLocation) {
    ControllerPdfUtil.writeControllerPdf(webContext, reportName, templateLocation, logger,
        templateEngine);
  }

  @GetMapping("getCsvRange")
  @ResponseBody
  public List<Range> importCsvRange(HttpServletRequest request) {

    String csvFilePath = request.getParameter(FILE_LOCATION_QUERY_PARAM);
    if (csvFilePath == null) {
      return null;
    }

    // Read in CSV file
    List<Integer> values = CsvFileUtil.readRangeCsv(new File(csvFilePath));

    if (values == null || values.size() <= 0) {
      return null;
    }

    ArrayList<Range> ranges = RangeUtil.calculateRangesFromInts(values);

    return ranges;
  }

}
