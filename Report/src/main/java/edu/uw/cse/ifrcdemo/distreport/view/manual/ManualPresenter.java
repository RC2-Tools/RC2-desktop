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

package edu.uw.cse.ifrcdemo.distreport.view.manual;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import edu.uw.cse.ifrcdemo.distreport.logic.DataPathChecker;
import edu.uw.cse.ifrcdemo.distreport.logic.ReportGenerator;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.sharedlib.util.DialogUtil;
import edu.uw.cse.ifrcdemo.sharedlib.view.SingleDirectoryPanel;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;

import javax.swing.JFileChooser;
import javax.swing.JTextField;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ManualPresenter {

  private final SingleDirectoryPanel manualView;
  private final MustacheFactory mustacheFactory;
  private Path dataPath;

  public ManualPresenter(SingleDirectoryPanel manualView) {
    this.manualView = manualView;
    this.dataPath = null;
    this.mustacheFactory = new DefaultMustacheFactory();

    setupDirChoosers();
    setupGenerateBtn();
  }

  public SingleDirectoryPanel getManualView() {
    return manualView;
  }

  public Path getDataPath() {
    return dataPath;
  }

  public void setDataPath(Path dataPath) {
    this.dataPath = dataPath;
  }

  public MustacheFactory getMustacheFactory() {
    return mustacheFactory;
  }

  private void setupDirChoosers() {
    getManualView()
        .getDataDirTextField()
        .addMouseListener(new DirTextFieldListener());
  }

  private void setupGenerateBtn() {
    getManualView().getBottomBtn().addActionListener(evt -> {
      if (!DataPathChecker.checkPath(getDataPath())) {
        String msg = TranslationUtil.getTranslations().getString(TranslationConsts.INVALID_DIR_ERROR);
        DialogUtil.showErrorDialog(getManualView(), msg);
        return;
      }

      JFileChooser chooser = new JFileChooser(Paths.get(GenConsts.CUR_DIR).toFile());
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      chooser.setAcceptAllFileFilterUsed(false);

      chooser.addActionListener(chooserEvt -> {
        if (!chooserEvt.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
          return;
        }

        Path outputPath = ((JFileChooser) chooserEvt.getSource()).getSelectedFile().toPath();
        ReportGenerator.generateReport(getDataPath(), outputPath, getManualView());
      });

      chooser.showOpenDialog(getManualView());
    });
  }


  private class DirTextFieldListener extends MouseAdapter {
    @Override
    public void mouseClicked(MouseEvent e) {
      super.mouseClicked(e);

      JTextField source = (JTextField) e.getSource();

      if (!source.isEnabled()) {
        return;
      }

      JFileChooser chooser = new JFileChooser(Paths.get(GenConsts.CUR_DIR).toFile());
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      chooser.setAcceptAllFileFilterUsed(false);

      chooser.addActionListener(chooserEvt -> {
        if (chooserEvt.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
          JFileChooser fileChooser = (JFileChooser) chooserEvt.getSource();

          source.setText(fileChooser.getSelectedFile().getName());
          setDataPath(fileChooser.getSelectedFile().toPath());
        }
      });

      chooser.showOpenDialog(getManualView());
    }
  }
}