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

package edu.uw.cse.ifrcdemo.setup.view.barcode;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import edu.uw.cse.ifrcdemo.setup.consts.SwingWorkerConsts;
import edu.uw.cse.ifrcdemo.setup.logic.BarcodeGenWorker;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.sharedlib.util.DialogUtil;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

public class BarcodePanel extends JPanel implements PropertyChangeListener {

  private JLabel textLabel;
  private JLabel startLabel;
  private JLabel endLabel;
  private JTextField textTextField;
  private JTextField startTextField;
  private JTextField endTextField;
  private JButton generateBtn;
  private JButton resetBtn;

  private final MustacheFactory mustacheFactory;

  public JLabel getTextLabel() {
    return textLabel;
  }

  public JLabel getStartLabel() {
    return startLabel;
  }

  public JLabel getEndLabel() {
    return endLabel;
  }

  public JTextField getTextTextField() {
    return textTextField;
  }

  public JTextField getStartTextField() {
    return startTextField;
  }

  public JTextField getEndTextField() {
    return endTextField;
  }

  public JButton getGenerateBtn() {
    return generateBtn;
  }

  public JButton getResetBtn() {
    return resetBtn;
  }

  public MustacheFactory getMustacheFactory() {
    return mustacheFactory;
  }

  public BarcodePanel() {
    super(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    ResourceBundle translations = TranslationUtil.getTranslations();
    gbc.insets = new Insets(10, 0, 10, 10);
    gbc.gridx = 0;

    this.mustacheFactory = new DefaultMustacheFactory();

    this.add(buildFormPanel(translations), gbc);
    this.add(buildBtnPanel(translations), gbc);
  }

  private JPanel buildFormPanel(ResourceBundle translations) {
    JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));

    this.textLabel = new JLabel(translations.getString(TranslationConsts.TEXT_LABEL));
    formPanel.add(this.textLabel);

    this.textTextField = new JTextField();
    formPanel.add(this.textTextField);

    this.startLabel = new JLabel(translations.getString(TranslationConsts.START_BARCODE_RANGE_LABEL));
    formPanel.add(this.startLabel);

    this.startTextField = new JTextField();
    formPanel.add(this.startTextField);

    this.endLabel = new JLabel(translations.getString(TranslationConsts.END_BARCODE_RANGE_LABEL));
    formPanel.add(this.endLabel);

    this.endTextField = new JTextField();
    formPanel.add(this.endTextField);

    return formPanel;
  }

  private JPanel buildBtnPanel(ResourceBundle translations) {
    JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));

    this.generateBtn = buildGenerateBtn(translations);
    btnPanel.add(this.generateBtn);

    this.resetBtn = buildResetBtn(translations);
    btnPanel.add(this.resetBtn);

    return btnPanel;
  }

  private JButton buildGenerateBtn(ResourceBundle translations) {
    JButton btn = new JButton(translations.getString(TranslationConsts.GEN_BTN_LABEL));

    btn.addActionListener(e -> {
      final JButton sourceBtn = (JButton) e.getSource();

      sourceBtn.setEnabled(false);
      sourceBtn.setText(translations.getString(TranslationConsts.PLEASE_WAIT_MSG));

      JFileChooser chooser = new JFileChooser();
      chooser.setDialogTitle(translations.getString(TranslationConsts.CHOOSE_OUTPUT_DIR_TITLE));
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      chooser.setAcceptAllFileFilterUsed(false);

      if (chooser.showOpenDialog(BarcodePanel.this) == JFileChooser.APPROVE_OPTION) {
        BarcodeGenWorker worker = new BarcodeGenWorker(
            chooser.getSelectedFile().toPath(),
            Integer.parseInt(getStartTextField().getText()),
            Integer.parseInt(getEndTextField().getText()),
            getTextTextField().getText(),
            getMustacheFactory()
        );

        worker.addPropertyChangeListener(BarcodePanel.this);
        worker.execute();
      } else {
        sourceBtn.setText(translations.getString(TranslationConsts.GEN_BTN_LABEL));
        sourceBtn.setEnabled(true);
      }
    });

    return btn;
  }

  private JButton buildResetBtn(ResourceBundle translations) {
    JButton btn = new JButton(translations.getString(TranslationConsts.RESET_BTN_LABEL));

    btn.addActionListener(e -> {
      getTextTextField().setText(GenConsts.EMPTY_STRING);
      getStartTextField().setText(GenConsts.EMPTY_STRING);
      getEndTextField().setText(GenConsts.EMPTY_STRING);
    });

    return btn;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getNewValue() != null &&
        evt.getPropertyName().equals(SwingWorkerConsts.SWING_WORKER_PROP_NAME) &&
        evt.getNewValue().equals(SwingWorker.StateValue.DONE)) {

      ResourceBundle translations = TranslationUtil.getTranslations();
      getGenerateBtn().setText(translations.getString(TranslationConsts.GEN_BTN_LABEL));
      getGenerateBtn().setEnabled(true);

      try {
        ((BarcodeGenWorker) evt.getSource()).get();
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();

        DialogUtil.showErrorDialog(this, translations.getString(TranslationConsts.FAILED_TO_GENERATE_ERROR) + e.getCause().toString());
        return;
      }

      DialogUtil.showConfirmDialog(this, translations.getString(TranslationConsts.BARCODE_GENERATOR_TITLE), translations.getString(TranslationConsts.SUCCESS_LABEL));
    }
  }
}
