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

package edu.uw.cse.ifrcdemo.sharedlib.generator;

import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.ResourceBundle;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.krysalis.barcode4j.BarcodeException;
import org.krysalis.barcode4j.BarcodeUtil;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.xml.sax.SAXException;

public class BarcodeGeneratorSingleton {

    private final org.krysalis.barcode4j.BarcodeGenerator generator;

    private static final BarcodeGeneratorSingleton instance = new BarcodeGeneratorSingleton();

    public static BarcodeGeneratorSingleton getInstance() {
        return instance;
    }

    private BarcodeGeneratorSingleton() {
        Configuration config;
        ResourceBundle translations = TranslationUtil.getTranslations();
        try {
            config = buildConfig();
        } catch (IOException | SAXException | ConfigurationException e) {
            e.printStackTrace();

            throw new IllegalStateException(translations.getString(TranslationConsts.FAILED_TO_PARSE_XML_CONFIGURATION_ERROR));
        }

        try {
            this.generator = BarcodeUtil.getInstance().createBarcodeGenerator(config);
        } catch (ConfigurationException | BarcodeException e) {
            e.printStackTrace();

            throw new IllegalStateException(translations.getString(TranslationConsts.FAILED_TO_CREATE_BARCODE_GENERATOR_ERROR));
        }
    }

    private Configuration buildConfig() throws SAXException, IOException, ConfigurationException {
        return new DefaultConfigurationBuilder().build(getClass().getResourceAsStream(
            BarcodeGeneratorConst.CONFIG_XML));
    }

    public static String generateBase64Barcode(String message) {
        return generateBase64Barcode(message, BarcodeGeneratorConst.DEFAULT_MIME_TYPE, BarcodeGeneratorConst.DEFAULT_DPI, false, 0);
    }

    public static String generateBase64Barcode(
            String message,
            String imgMineType,
            int resolution,
            boolean antiAlias,
            int orientation) {
        try(ByteArrayOutputStream stream = new ByteArrayOutputStream();
            OutputStream base64Stream = Base64.getEncoder().wrap(stream)) {
            BitmapCanvasProvider canvas = new BitmapCanvasProvider(
                base64Stream,
                imgMineType,
                resolution,
                BufferedImage.TYPE_BYTE_BINARY,
                antiAlias,
                orientation
            );
            getInstance().generator.generateBarcode(canvas, message);
            canvas.finish();

            return stream.toString(StandardCharsets.US_ASCII.name());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
