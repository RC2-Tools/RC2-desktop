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

package edu.uw.cse.ifrcdemo.setup.view.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import edu.uw.cse.ifrcdemo.setup.consts.SetupConsts;
import edu.uw.cse.ifrcdemo.setup.consts.ViewConsts;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.sharedlib.consts.ModuleConsts;
import edu.uw.cse.ifrcdemo.sharedlib.model.config.AuxiliaryProperty;
import edu.uw.cse.ifrcdemo.sharedlib.model.config.SaveDirectory;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationType;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.RegistrationMode;
import edu.uw.cse.ifrcdemo.sharedlib.util.DialogUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.FileUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.OdkPathUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.StringUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.SwingUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.XlsxUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.ZipUtil;
import edu.uw.cse.ifrcdemo.sharedlib.view.MainPanel;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import edu.uw.cse.ifrcdemo.xlsxconverterserver.XlsxConverterServer;
import edu.uw.cse.ifrcdemo.xlsxconverterserver.handler.MappingFormUploadConsumer;
import edu.uw.cse.ifrcdemo.xlsxconverterserver.model.ConvertedXlsx;
import io.sentry.Sentry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ConfigPresenter {

  private final MainPanel mainPanel;
  private final ConfigView configView;
  private final SaveDirectory saveDirectory;
  private final MappingFormUploadConsumer formUploadConsumer;

  private final Logger logger;

  private String beneficiaryEntityFormPath;
  private String individualFormPath;

  public MainPanel getMainPanel() {
    return this.mainPanel;
  }

  public String getBeneficiaryEntityFormPath() {
    return beneficiaryEntityFormPath;
  }

  public void setBeneficiaryEntityFormPath(String beneficiaryEntityFormPath) {
    this.beneficiaryEntityFormPath = beneficiaryEntityFormPath;
  }

  public String getIndividualFormPath() {
    return individualFormPath;
  }

  public void setIndividualFormPath(String individualFormPath) {
    this.individualFormPath = individualFormPath;
  }

  public ConfigView getConfigView() {
    return configView;
  }

  public MappingFormUploadConsumer getFormUploadConsumer() {
    return formUploadConsumer;
  }

  public ConfigPresenter(MainPanel mainPanel, ConfigView configView, SaveDirectory saveDirectory, MappingFormUploadConsumer formUploadConsumer) {
    this.configView = configView;
    this.saveDirectory = saveDirectory;
    this.formUploadConsumer = formUploadConsumer;

    this.beneficiaryEntityFormPath = null;
    this.individualFormPath = null;

    this.mainPanel = mainPanel;

    this.logger = LogManager.getLogger(ConfigPresenter.class);

    setupWorkflowModeComboBox();
    setupRegModeComboBox();
    setupFormChoosers();
    setupForeignKeyField();
    setupSave();
    setupServerBtn();
  }

  private void setupWorkflowModeComboBox() {
    JComboBox<AuthorizationType> workflowModeComboBox = getConfigView().getWorkflowModeJComboBox();

    workflowModeComboBox.setModel(new DefaultComboBoxModel<>(AuthorizationType.values()));
    workflowModeComboBox.addItemListener(new WorkflowModeSelectionListener());
    workflowModeComboBox.setSelectedIndex(-1);
  }

  private void setupRegModeComboBox() {
    JComboBox<RegistrationMode> regModeComboBox = getConfigView().getRegModeJComboBox();

    regModeComboBox.setModel(new DefaultComboBoxModel<>(RegistrationMode.values()));
    regModeComboBox.addItemListener(new RegModeSelectionListener());
    regModeComboBox.setSelectedIndex(-1);
  }

  private void setupFormChoosers() {
    JTextField beneficiaryEntityForm = getConfigView().getBeneficiaryEntityFormChooser();
    JTextField individualFormChooser = getConfigView().getIndividualFormChooser();

    FileChooserClickListener fileChooserClickListener = new FileChooserClickListener();
    beneficiaryEntityForm.addMouseListener(fileChooserClickListener);
    individualFormChooser.addMouseListener(fileChooserClickListener);
  }

  private void setupForeignKeyField() {
    getConfigView().getBeneficiaryEntityIdColumnTextField().getDocument().addDocumentListener(new TextFieldDocumentListener());
  }

  private void setupSave() {
    getConfigView().getSaveBtn().addActionListener(e -> {
      JFileChooser chooser = new JFileChooser(Paths.get(GenConsts.CUR_DIR).toFile());
      ResourceBundle translations = TranslationUtil.getTranslations();
      chooser.setDialogTitle(translations.getString(TranslationConsts.CHOOSE_OUTPUT_DIR_TITLE));
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

      chooser.addActionListener(chooserEvt -> {
        if (!chooserEvt.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
          return;
        }

        Path savePath = FileUtil.addDateWithPrefixToPath(((JFileChooser) chooserEvt.getSource()).getSelectedFile().toPath(), ModuleConsts.SETUP);
        CompletableFuture convertFuture = createFutureToProcessXLSX(translations, savePath);

        if(convertFuture == null) {
          return;
        }

        CompletableFuture.allOf(convertFuture).thenRunAsync(() -> DialogUtil.showConfirmDialog(getConfigView(), translations.getString(TranslationConsts.DONE_LABEL), translations.getString(TranslationConsts.DONE_MSG)), SwingUtil::invokeOnEdt)
            .exceptionally(throwable -> {
              logger.catching(throwable);
              String msg = translations.getString(TranslationConsts.FAILED_TO_CONVERT_FILES_ERROR) + GenConsts.NEW_LINE + translations.getString(TranslationConsts.XLSX_PROBLEM_ERROR) + GenConsts.NEW_LINE + throwable.getMessage();
              // TODO: better exception handling
              DialogUtil.showScrollingErrorDialog(getConfigView(), msg);
              Sentry.capture(new Throwable(msg, throwable));
              return null;
            });
      });

      chooser.showOpenDialog(getConfigView());
    });
  }


  private void setupServerBtn() {

    //TODO: generate directory into an implicit directory and set name
    getConfigView().getServerBtn().addActionListener(e -> {

      try {
        this.saveDirectory.setCurrentSaveDirectory(ModuleConsts.SETUP);
      } catch (IOException e1) {
        throw new RuntimeException(e1);
      }

      ResourceBundle translations = TranslationUtil.getTranslations();
      CompletableFuture convertFuture = createFutureToProcessXLSX(translations, saveDirectory.getCurrentSaveDirectory());

      if(convertFuture == null) {
        return;
      }

      CompletableFuture.allOf(convertFuture).thenRunAsync(() -> {
                String msg = translations.getString(TranslationConsts.CONFIG_FILES_GENERATED_CONTINUE_TO_SERVER_MSG);
                DialogUtil.showConfirmDialog(GenConsts.EMPTY_STRING, msg);
                getMainPanel().getLayoutMngr().show(getMainPanel(), ViewConsts.CARD_UPLOAD_CONFIG);
              }, SwingUtil::invokeOnEdt)
              .exceptionally(throwable -> {
                logger.catching(throwable);
                String msg = translations.getString(TranslationConsts.FAILED_TO_CONVERT_FILES_ERROR) + GenConsts.NEW_LINE + translations.getString(TranslationConsts.XLSX_PROBLEM_ERROR) + GenConsts.NEW_LINE + throwable.getMessage();
                DialogUtil.showScrollingErrorDialog(getConfigView(), msg);
                Sentry.capture(new Throwable(msg, throwable));
                return null;
              });
    });
  }

  private CompletableFuture createFutureToProcessXLSX(ResourceBundle translations, Path savePath) {
    Path tablesPath = OdkPathUtil.getTablesPath(savePath);
    Path configPath = OdkPathUtil.getAssetsPath(savePath);

    try {
      Files.createDirectories(tablesPath);
      Files.createDirectories(configPath);
    } catch (IOException e1) {
      DialogUtil.showErrorDialog(getConfigView(), e1.getMessage());
      e1.printStackTrace();
      return null;
    }

    try {
      ZipUtil.extractZipFromResource(SetupConsts.BASE_FILES_RESOURCE_NAME, savePath);
    } catch (IOException e1) {
      DialogUtil.showErrorDialog(getConfigView(), translations.getString(TranslationConsts.FAILED_TO_EXTRACT_APP_LEVEL_FILES_ERROR));
      e1.printStackTrace();
      return null;
    }

    Set<String> filesToConvert;
    try {
      filesToConvert = getAllXlsx(tablesPath);
    } catch (IOException e1) {
      e1.printStackTrace();
      return null;
    }

    if (getConfigView().getBeneficiaryEntityFormChooser().isEnabled()) {
      filesToConvert.add(getBeneficiaryEntityFormPath());
    }
    if (getConfigView().getIndividualFormChooser().isEnabled()) {
      filesToConvert.add(getIndividualFormPath());
    }

    CompletableFuture<?>[] futures = new CompletableFuture[]{CompletableFuture.completedFuture(null)};
    Map<String, Path> pathTranslationMap = new ConcurrentHashMap<>();

    if (!filesToConvert.isEmpty()) {
      futures = convertXlsxs(filesToConvert, savePath)
              .entrySet()
              .stream()
              .map(entry -> entry.getValue().thenAcceptAsync(converted -> {
                try {
                  Path newPath = XlsxUtil.organizeFormLevelFiles(
                          Paths.get(entry.getKey()),
                          converted.getTableId(),
                          converted.getFormId(),
                          savePath
                  );

                  pathTranslationMap.put(entry.getKey(), newPath);

                  if (!converted.getWarnings().isEmpty()) {
                    DialogUtil.showScrollingWarningMsgDialog(
                            getConfigView(),
                            XlsxUtil.formatFormDefWarnings(entry.getKey(), converted.getWarnings())
                    );
                  }
                } catch (IOException e1) {
                  throw new RuntimeException(e1);
                }
              }))
              .toArray(CompletableFuture[]::new);
    }

   return CompletableFuture.allOf(futures)
            .thenRunAsync(() -> {
              AuxiliaryProperty auxProp = buildAuxiliaryProperty(pathTranslationMap);
              try (BufferedWriter writer = Files.newBufferedWriter(configPath.resolve(FileUtil.getConfigFileName()), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                   SequenceWriter sequenceWriter = new ObjectMapper().writerFor(AuxiliaryProperty.class).writeValues(writer)) {

                sequenceWriter.write(auxProp);
              } catch (IOException e1) {
                logger.catching(e1);
                String msg = TranslationUtil.getTranslations().getString(TranslationConsts.FAILED_TO_EXPORT_ERROR) + GenConsts.SPACE + FileUtil.getConfigFileName();
                DialogUtil.showErrorDialog(getConfigView(), msg);
              }
            }).exceptionally(throwable -> {
              throw new RuntimeException(throwable);
            });
  }


  private Map<String, CompletableFuture<ConvertedXlsx>> convertXlsxs(Set<String> files, Path outputPath) {
    XlsxConverterServer instance = XlsxConverterServer.getInstance();

    String request = instance.submitConvertRequest(files);
    getFormUploadConsumer().getPathMap().put(request, outputPath);
    Map<String, CompletableFuture<ConvertedXlsx>> completableFuture = instance.getCompletableFuture(request);

    try {
      SwingUtil.launchBrowser(instance.getUriForRequest(request));
    } catch (URISyntaxException e1) {
      e1.printStackTrace();
    }

    return completableFuture;
  }

  private AuxiliaryProperty buildAuxiliaryProperty(Map<String, Path> pathTranslationMap) {
    AuxiliaryProperty auxProp = new AuxiliaryProperty();

    auxProp.setWorkflowMode((AuthorizationType) getConfigView()
        .getWorkflowModeJComboBox()
        .getSelectedItem()
    );

    auxProp.setRegistrationMode((RegistrationMode) getConfigView()
        .getRegModeJComboBox()
        .getSelectedItem()
    );

    auxProp.setBeneficiaryEntityCustomFormId(getBeneficiaryEntityFormPath() != null ?
        pathTranslationMap
            .get(getBeneficiaryEntityFormPath())
            .getFileName()
            .toString() :
        null
    );

    auxProp.setMemberCustomFormId(getIndividualFormPath() != null ?
        pathTranslationMap.get(getIndividualFormPath()).getFileName().toString() :
        null
    );

    auxProp.setCustomBeneficiaryRowIdColumn(getConfigView()
        .getBeneficiaryEntityIdColumnTextField()
        .getText()
    );

    return auxProp;
  }


  private static Set<String> getAllXlsx(Path tablesPath) throws IOException {
    return Files
        .walk(tablesPath, 1)
        .map(OdkPathUtil::getFormsPath)
        .filter(Files::exists)
        .flatMap(path -> { // get list of forms for each table
          try {
            return Files.walk(path, 1);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        })
        .map(path -> { // get name of xlsx file for each form
          try {
            return Files
                .find(path, 1, (file, __) -> file.getFileName().toString().endsWith(GenConsts.XLSX_FILE_EXTENSION))
                .findFirst(); // assume there will be at most be 1 xlsx file
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        })
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(path -> path.toAbsolutePath().toString())
        .collect(Collectors.toSet());
  }

  private void setToRegMode(RegistrationMode mode) {
    if (mode == RegistrationMode.HOUSEHOLD) {
      Arrays
              .asList(
                      getConfigView().getBeneficiaryEntityFormChooser(),
                      getConfigView().getBeneficiaryEntityIdColumnTextField(),
                      getConfigView().getIndividualFormChooser()
              )
              .forEach(comp -> comp.setEnabled(true));

      boolean valid = getBeneficiaryEntityFormPath() != null &&
          getIndividualFormPath() != null &&
          StringUtil.isNotNullAndNotEmpty(getConfigView().getBeneficiaryEntityIdColumnTextField().getText());
      getConfigView().getSaveBtn().setEnabled(valid);
      getConfigView().getServerBtn().setEnabled(valid);

    } else if (mode == RegistrationMode.INDIVIDUAL) {
      getConfigView().getBeneficiaryEntityFormChooser().setEnabled(true);
      getConfigView().getBeneficiaryEntityIdColumnTextField().setEnabled(false);
      getConfigView().getIndividualFormChooser().setEnabled(false);

      boolean valid = getBeneficiaryEntityFormPath() != null;
      getConfigView().getSaveBtn().setEnabled(valid);
      getConfigView().getServerBtn().setEnabled(valid);
    } else {
      getConfigView().getSaveBtn().setEnabled(false);
      getConfigView().getServerBtn().setEnabled(false);
    }
  }

  private void refreshClickableComponents() {
    Object selectedWorkflowMode = getConfigView().getWorkflowModeJComboBox().getSelectedItem();

    if (selectedWorkflowMode == AuthorizationType.NO_REGISTRATION) {
      getConfigView().getRegModeJComboBox().setEnabled(false);
      getConfigView().getBeneficiaryEntityFormChooser().setEnabled(false);
      getConfigView().getBeneficiaryEntityIdColumnTextField().setEnabled(false);
      getConfigView().getIndividualFormChooser().setEnabled(false);

      getConfigView().getSaveBtn().setEnabled(true);
      getConfigView().getServerBtn().setEnabled(true);
    } else if (selectedWorkflowMode == AuthorizationType.ID_ONLY_REGISTRATION) {
      getConfigView().getRegModeJComboBox().setEnabled(false);
      getConfigView().getBeneficiaryEntityFormChooser().setEnabled(false);
      getConfigView().getBeneficiaryEntityIdColumnTextField().setEnabled(false);
      getConfigView().getIndividualFormChooser().setEnabled(false);

      getConfigView().getSaveBtn().setEnabled(true);
      getConfigView().getServerBtn().setEnabled(true);

      getConfigView()
          .getRegModeJComboBox()
          .setSelectedItem(RegistrationMode.INDIVIDUAL);

      setToRegMode(RegistrationMode.INDIVIDUAL);
    } else {
      JComboBox<RegistrationMode> regComboBox = getConfigView().getRegModeJComboBox();
      regComboBox.setEnabled(true);

      setToRegMode((RegistrationMode) regComboBox.getSelectedItem());
    }
  }

  private class WorkflowModeSelectionListener implements ItemListener {
    @Override
    public void itemStateChanged(ItemEvent e) {
      if (e.getStateChange() != ItemEvent.SELECTED) {
        return;
      }

      refreshClickableComponents();
    }
  }

  private class RegModeSelectionListener implements ItemListener {
    @Override
    public void itemStateChanged(ItemEvent e) {
      if (e.getStateChange() != ItemEvent.SELECTED) {
        return;
      }

      refreshClickableComponents();
    }
  }

  private class TextFieldDocumentListener implements DocumentListener {

    @Override
    public void changedUpdate(DocumentEvent e) {}

    @Override
    public void removeUpdate(DocumentEvent e) {
      if (getConfigView().getBeneficiaryEntityIdColumnTextField().getText().equals(GenConsts.EMPTY_STRING)) {
        refreshClickableComponents();
      }
    }
    @Override
    public void insertUpdate(DocumentEvent e) {
      if (!getConfigView().getBeneficiaryEntityIdColumnTextField().getText().equals(GenConsts.EMPTY_STRING)) {
        refreshClickableComponents();
      }
    }
  }

  private class FileChooserClickListener extends MouseAdapter {
    @Override
    public void mouseClicked(MouseEvent e) {
      super.mouseClicked(e);
      ResourceBundle translations = TranslationUtil.getTranslations();
      JTextField source = (JTextField) e.getSource();

      if (!source.isEnabled()) {
        return;
      }

      JFileChooser chooser = new JFileChooser(Paths.get(GenConsts.CUR_DIR).toFile());

      chooser.setDialogTitle(translations.getString(TranslationConsts.CHOOSE_XLSX_LABEL));
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      chooser.setFileFilter(new FileNameExtensionFilter(GenConsts.XLSX, GenConsts.XLSX));

      chooser.addActionListener(chooserEvt -> {
        if (chooserEvt.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
          JFileChooser fileChooser = (JFileChooser) chooserEvt.getSource();

          source.setText(fileChooser.getSelectedFile().getName());

          if (source == getConfigView().getBeneficiaryEntityFormChooser()) {

            setBeneficiaryEntityFormPath(fileChooser.getSelectedFile().getAbsolutePath());
          } else if (source == getConfigView().getIndividualFormChooser()) {

            setIndividualFormPath(fileChooser.getSelectedFile().getAbsolutePath());
          }
          refreshClickableComponents();
        }
      });

      chooser.showOpenDialog(getConfigView());
    }
  }
}
