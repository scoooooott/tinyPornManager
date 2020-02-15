/*
 * Copyright 2012 - 2020 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinymediamanager.ui.movies.dialogs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.ExportTemplate;
import org.tinymediamanager.core.MediaEntityExporter;
import org.tinymediamanager.core.MediaEntityExporter.TemplateType;
import org.tinymediamanager.core.TmmProperties;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.movie.MovieExporter;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.tasks.ExportTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.components.ReadOnlyTextArea;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.TmmSplitPane;
import org.tinymediamanager.ui.dialogs.TmmDialog;

import net.miginfocom.swing.MigLayout;

/**
 * The Class MovieExporter.
 * 
 * @author Manuel Laggner
 */
public class MovieExporterDialog extends TmmDialog {
  private static final long    serialVersionUID = 4085262825778794266L;
  private static final Logger  LOGGER           = LoggerFactory.getLogger(MovieExporterDialog.class);

  private static final String  DIALOG_ID        = "movieExporter";

  private List<Movie>          movies;
  private List<ExportTemplate> templatesFound;

  private JTextField           tfExportDir;
  private JList                list;
  private JLabel               lblTemplateName;
  private JLabel               lblUrl;
  private JTextArea            taDescription;
  private JCheckBox            chckbxTemplateWithDetail;

  /**
   * Create the dialog.
   * 
   * @param moviesToExport
   *          the movies to export
   */
  public MovieExporterDialog(List<Movie> moviesToExport) {
    super(BUNDLE.getString("movie.export"), DIALOG_ID);
    {
      JPanel panelContent = new JPanel();
      getContentPane().add(panelContent);
      panelContent.setLayout(new MigLayout("", "[600lp,grow]", "[300lp,grow][]"));

      JSplitPane splitPane = new TmmSplitPane();
      splitPane.setResizeWeight(0.7);
      panelContent.add(splitPane, "cell 0 0,grow");

      JScrollPane scrollPane = new JScrollPane();
      splitPane.setLeftComponent(scrollPane);

      list = new JList();
      scrollPane.setViewportView(list);

      JPanel panelExporterDetails = new JPanel();
      splitPane.setRightComponent(panelExporterDetails);
      panelExporterDetails.setLayout(new MigLayout("", "[100lp,grow]", "[][][][200lp,grow]"));

      lblTemplateName = new JLabel("");
      panelExporterDetails.add(lblTemplateName, "cell 0 0,growx");

      lblUrl = new JLabel("");
      panelExporterDetails.add(lblUrl, "cell 0 1,growx");

      chckbxTemplateWithDetail = new JCheckBox("");
      chckbxTemplateWithDetail.setEnabled(false);
      panelExporterDetails.add(chckbxTemplateWithDetail, "flowx,cell 0 2");

      JScrollPane scrollPaneDescription = new JScrollPane();
      panelExporterDetails.add(scrollPaneDescription, "cell 0 3,grow");

      taDescription = new ReadOnlyTextArea();
      scrollPaneDescription.setViewportView(taDescription);

      JLabel lblDetails = new TmmLabel(BUNDLE.getString("export.detail"));
      panelExporterDetails.add(lblDetails, "cell 0 2,growx,aligny center");
      splitPane.setDividerLocation(300);

      tfExportDir = new JTextField(TmmProperties.getInstance().getProperty(DIALOG_ID + ".path"));
      panelContent.add(tfExportDir, "flowx,cell 0 1,growx");
      tfExportDir.setColumns(10);

      JButton btnSetDestination = new JButton(BUNDLE.getString("export.setdestination"));
      panelContent.add(btnSetDestination, "cell 0 1");
      btnSetDestination.addActionListener(e -> {
        Path file = TmmUIHelper.selectDirectory(BUNDLE.getString("export.selectdirectory"), tfExportDir.getText());
        if (file != null) {
          tfExportDir.setText(file.toAbsolutePath().toString());
          TmmProperties.getInstance().putProperty(DIALOG_ID + ".path", tfExportDir.getText());
        }
      });

    }

    {
      JButton btnCancel = new JButton(BUNDLE.getString("Button.cancel"));
      btnCancel.setIcon(IconManager.CANCEL_INV);
      btnCancel.addActionListener(arg0 -> setVisible(false));
      addButton(btnCancel);

      JButton btnExport = new JButton("Export");
      btnExport.setIcon(IconManager.EXPORT);
      btnExport.addActionListener(arg0 -> {
        if (StringUtils.isBlank(tfExportDir.getText())) {
          return;
        }
        // check selected template
        int index = list.getSelectedIndex();
        if (index < 0) {
          return;
        }

        ExportTemplate selectedTemplate = templatesFound.get(index);
        if (selectedTemplate != null) {
          // check whether the chosen export path exists/is empty or not
          Path exportPath = Paths.get(tfExportDir.getText());
          if (!Files.exists(exportPath)) {
            // export dir does not exist
            JOptionPane.showMessageDialog(MovieExporterDialog.this, BUNDLE.getString("export.foldernotfound"));
            return;
          }

          try {
            if (!Utils.isFolderEmpty(exportPath)) {
              Object[] options = { BUNDLE.getString("Button.yes"), BUNDLE.getString("Button.no") };
              int decision = JOptionPane.showOptionDialog(MovieExporterDialog.this, BUNDLE.getString("export.foldernotempty"), "",
                  JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);// $NON-NLS-1$
              if (decision == JOptionPane.NO_OPTION) {
                return;
              }
            }
          }
          catch (IOException e) {
            LOGGER.warn("could not open folder: {}", e.getMessage());
            return;
          }

          try {
            TmmProperties.getInstance().putProperty(DIALOG_ID + ".template", selectedTemplate.getName());
            MovieExporter exporter = new MovieExporter(Paths.get(selectedTemplate.getPath()));
            TmmTaskManager.getInstance().addMainTask(new ExportTask(BUNDLE.getString("movie.export"), exporter, movies, exportPath));
          }
          catch (Exception e) {
            LOGGER.error("Error exporting movies: ", e);
          }
          setVisible(false);
        }
      });
      addDefaultButton(btnExport);
    }

    movies = moviesToExport;
    templatesFound = MediaEntityExporter.findTemplates(TemplateType.MOVIE);
    bindingGroup = initDataBindings();

    // set the last used template as default
    String lastTemplateName = TmmProperties.getInstance().getProperty(DIALOG_ID + ".template");
    if (StringUtils.isNotBlank(lastTemplateName)) {
      list.setSelectedValue(lastTemplateName, true);
    }
  }

  protected BindingGroup initDataBindings() {
    JListBinding<ExportTemplate, List<ExportTemplate>, JList> jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ, templatesFound,
        list);
    //
    BeanProperty<ExportTemplate, String> exportTemplateBeanProperty = BeanProperty.create("name");
    jListBinding.setDetailBinding(exportTemplateBeanProperty);
    //
    jListBinding.bind();
    //
    BeanProperty<JList, String> jListBeanProperty = BeanProperty.create("selectedElement.name");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<JList, String, JLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, list, jListBeanProperty, lblTemplateName,
        jLabelBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<JList, String> jListBeanProperty_1 = BeanProperty.create("selectedElement.url");
    AutoBinding<JList, String, JLabel, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, list, jListBeanProperty_1, lblUrl,
        jLabelBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<JList, String> jListBeanProperty_2 = BeanProperty.create("selectedElement.description");
    BeanProperty<JTextArea, String> JTextAreaBeanProperty = BeanProperty.create("text");
    AutoBinding<JList, String, JTextArea, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, list, jListBeanProperty_2,
        taDescription, JTextAreaBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<JList, Boolean> jListBeanProperty_3 = BeanProperty.create("selectedElement.detail");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<JList, Boolean, JCheckBox, Boolean> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, list, jListBeanProperty_3,
        chckbxTemplateWithDetail, jCheckBoxBeanProperty);
    autoBinding_3.bind();
    //
    BindingGroup bindingGroup = new BindingGroup();
    //
    bindingGroup.addBinding(jListBinding);
    bindingGroup.addBinding(autoBinding);
    bindingGroup.addBinding(autoBinding_1);
    bindingGroup.addBinding(autoBinding_2);
    bindingGroup.addBinding(autoBinding_3);
    return bindingGroup;
  }
}
