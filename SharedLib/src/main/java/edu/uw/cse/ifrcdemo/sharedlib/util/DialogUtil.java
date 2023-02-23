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

package edu.uw.cse.ifrcdemo.sharedlib.util;

import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.sharedlib.consts.LayoutViewConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;

/**
 * Utilities that wrap JOptionPane dialog method
 *
 * These utils ensure that the dialogs are ran on the Event Dispatch Thread
 */
public class DialogUtil {

  public static void showErrorDialog(Object msg) {
    showErrorDialog(SwingUtil.getActiveFrame(), msg);
  }

  public static void showErrorDialog(Component parent, Object msg) {
    SwingUtil.invokeOnEdt(() -> JOptionPane.showMessageDialog(
        parent,
        msg, TranslationUtil.getTranslations().getString(TranslationConsts.ERROR_LABEL),
        JOptionPane.ERROR_MESSAGE
    ));
  }

  public static void showScrollingErrorDialog(String msg) {
    showScrollingErrorDialog(SwingUtil.getActiveFrame(), msg);
  }

  public static void showScrollingErrorDialog(Component parent, String msg) {
    JTextArea textArea = new JTextArea(msg);
    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setPreferredSize(new Dimension(
        LayoutViewConsts.SCROLLING_DIALOG_WIDTH,
        LayoutViewConsts.SCROLLING_DIALOG_HEIGHT
    ));

    showErrorDialog(parent, scrollPane);
  }

  public static void showConfirmDialog(final String title, final String msg) {
    showConfirmDialog(SwingUtil.getActiveFrame(), title, msg);
  }

  public static void showConfirmDialog(final Component parent, final String title, final String msg) {
    SwingUtil.invokeOnEdt(() -> JOptionPane.showConfirmDialog(
        parent,
        msg,
        title,
        JOptionPane.DEFAULT_OPTION,
        JOptionPane.INFORMATION_MESSAGE
    ));
  }

  public static int showWarningDialog(final String msg) {
    return showWarningDialog(SwingUtil.getActiveFrame(), msg);
  }

  public static int showWarningDialog(final Component parent, final String msg) {
    return JOptionPane.showConfirmDialog(
        parent,
        msg, TranslationUtil.getTranslations().getString(TranslationConsts.WARNING_DIALOG_TITLE),
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE
    );
  }

  public static void showScrollingWarningMsgDialog(String msg) {
    showScrollingWarningMsgDialog(SwingUtil.getActiveFrame(), msg);
  }

  public static void showScrollingWarningMsgDialog(Component parent, String msg) {
    JTextArea textArea = new JTextArea(msg);
    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setPreferredSize(new Dimension(
        LayoutViewConsts.SCROLLING_DIALOG_WIDTH,
        LayoutViewConsts.SCROLLING_DIALOG_HEIGHT
    ));

    SwingUtil.invokeOnEdt(() -> JOptionPane.showMessageDialog(
        parent,
        scrollPane,
            TranslationUtil.getTranslations().getString(TranslationConsts.WARNING_DIALOG_TITLE),
        JOptionPane.WARNING_MESSAGE
    ));
  }

  public static void showMissingFileDialog(final Component parent, final List<String> files) {
    StringBuilder errorMsgBuilder = new StringBuilder();
    files.forEach(f -> errorMsgBuilder.append(TranslationUtil.getTranslations().getString(TranslationConsts.MISSING_LABEL)).append(GenConsts.SPACE).append(f).append(GenConsts.EXCLAMATION_POINT).append(GenConsts.NEW_LINE));

    showErrorDialog(parent, errorMsgBuilder.toString());
  }
}
