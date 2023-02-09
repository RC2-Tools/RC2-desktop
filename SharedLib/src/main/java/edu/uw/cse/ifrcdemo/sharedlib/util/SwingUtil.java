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

package edu.uw.cse.ifrcdemo.sharedlib.util;

import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Frame;
import java.net.URI;
import java.util.Arrays;
import java.util.stream.Stream;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class SwingUtil {
  public static void setSystemLookAndFeel() {
    try {
      // Set System L&F
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch (Exception e) {
      // Silently do nothing when this exception occurs. We failed to set the look and feel to the system default.
      // This should fall back to the Java cross platform "metal" default, which looks worse but should still
      // work fine.
    }
  }

  public static void invokeOnEdt(Runnable r) {
    if (SwingUtilities.isEventDispatchThread()) {
      r.run();
    } else {
      SwingUtilities.invokeLater(r);
    }
  }

  public static Frame getActiveFrame() {
    return Arrays.stream(JFrame.getFrames())
        .filter(Component::isVisible)
        .findFirst()
        .orElse(null); // for lack of a better default value
  }

  public static String wrapStringWithCenter(String input) {
    return GenConsts.OPEN_HTML + GenConsts.OPEN_HTML_CENTER + input + GenConsts.CLOSE_HTML_CENTER + GenConsts.CLOSE_HTML;
  }

  public static <E> DefaultComboBoxModel<E> buildComboBoxModel(Stream<E> items) {
    DefaultComboBoxModel<E> newModel = new DefaultComboBoxModel<>();

    items.forEach(newModel::addElement);
    newModel.setSelectedItem(newModel.getElementAt(0));

    return newModel;
  }

  public static void launchBrowser(URI uri) {
    try {
      Desktop.getDesktop().browse(uri);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
