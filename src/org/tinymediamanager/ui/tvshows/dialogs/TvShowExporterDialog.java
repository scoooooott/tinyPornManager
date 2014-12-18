/*
 * Copyright 2012 - 2014 Manuel Laggner
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
package org.tinymediamanager.ui.tvshows.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.ExportTemplate;
import org.tinymediamanager.core.MediaEntityExporter.TemplateType;
import org.tinymediamanager.core.tvshow.TvShowExporter;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.ui.EqualsLayout;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.dialogs.TmmDialog;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class TvShowExporter.
 * 
 * @author Manuel Laggner
 */
public class TvShowExporterDialog extends TmmDialog {
  private static final long           serialVersionUID = -2197076428245222349L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private static final Logger         LOGGER           = LoggerFactory.getLogger(TvShowExporterDialog.class);

  private List<TvShow>                tvShows;
  private List<ExportTemplate>        templatesFound;

  private JTextField                  tfExportDir;
  private JList                       list;
  private JLabel                      lblTemplateName;
  private JLabel                      lblUrl;
  private JTextPane                   tpDescription;
  private JCheckBox                   chckbxTemplateWithDetail;

  /**
   * Create the dialog.
   * 
   * @param tvShowsToExport
   *          the movies to export
   */
  public TvShowExporterDialog(List<TvShow> tvShowsToExport) {
    super(BUNDLE.getString("tvshow.export"), "tvShowExporter"); //$NON-NLS-1$
    setBounds(5, 5, 600, 300);

    getContentPane().setLayout(
        new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC,
            FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
            RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.UNRELATED_GAP_ROWSPEC,
            FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, }));

    JSplitPane splitPane = new JSplitPane();
    splitPane.setResizeWeight(0.7);
    getContentPane().add(splitPane, "2, 2, 3, 1, fill, fill");

    JScrollPane scrollPane = new JScrollPane();
    splitPane.setLeftComponent(scrollPane);

    list = new JList();
    scrollPane.setViewportView(list);

    JPanel panelExporterDetails = new JPanel();
    splitPane.setRightComponent(panelExporterDetails);
    panelExporterDetails.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));

    lblTemplateName = new JLabel("");
    panelExporterDetails.add(lblTemplateName, "2, 2, 3, 1");

    lblUrl = new JLabel("");
    panelExporterDetails.add(lblUrl, "2, 4, 3, 1");

    chckbxTemplateWithDetail = new JCheckBox("");
    chckbxTemplateWithDetail.setEnabled(false);
    panelExporterDetails.add(chckbxTemplateWithDetail, "2, 6");

    JLabel lblDetails = new JLabel(BUNDLE.getString("export.detail")); //$NON-NLS-1$
    panelExporterDetails.add(lblDetails, "4, 6");

    JScrollPane scrollPaneDescription = new JScrollPane();
    panelExporterDetails.add(scrollPaneDescription, "2, 8, 3, 1, fill, fill");

    tpDescription = new JTextPane();
    scrollPaneDescription.setViewportView(tpDescription);
    splitPane.setDividerLocation(300);

    tfExportDir = new JTextField();
    getContentPane().add(tfExportDir, "2, 4, fill, default");
    tfExportDir.setColumns(10);

    JButton btnSetDestination = new JButton(BUNDLE.getString("export.setdestination")); //$NON-NLS-1$
    btnSetDestination.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        File file = TmmUIHelper.selectDirectory(BUNDLE.getString("export.selectdirectory")); //$NON-NLS-1$
        if (file != null) {
          tfExportDir.setText(file.getAbsolutePath());
        }
      }
    });
    getContentPane().add(btnSetDestination, "4, 4");

    JPanel panelButtons = new JPanel();
    panelButtons.setLayout(new EqualsLayout(5));
    getContentPane().add(panelButtons, "2, 6, 3, 1, fill, fill");

    JButton btnExport = new JButton("Export");
    btnExport.setIcon(IconManager.EXPORT);
    btnExport.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
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
          try {
            TvShowExporter exporter = new TvShowExporter(selectedTemplate.getPath());
            exporter.export(tvShows, tfExportDir.getText());
          }
          catch (Exception e) {
            LOGGER.error("Error exporting tv shows: " + e.getMessage());
          }
          setVisible(false);
        }
      }
    });
    panelButtons.add(btnExport);

    JButton btnCancel = new JButton(BUNDLE.getString("Button.cancel")); //$NON-NLS-1$
    btnCancel.setIcon(IconManager.CANCEL);
    btnCancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        setVisible(false);
      }
    });
    panelButtons.add(btnCancel);

    tvShows = tvShowsToExport;
    templatesFound = TvShowExporter.findTemplates(TemplateType.TV_SHOW);
    initDataBindings();
  }

  /**
   * Inits the data bindings.
   */
  protected void initDataBindings() {
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
    AutoBinding<JList, String, JLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, list, jListBeanProperty,
        lblTemplateName, jLabelBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<JList, String> jListBeanProperty_1 = BeanProperty.create("selectedElement.url");
    AutoBinding<JList, String, JLabel, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, list, jListBeanProperty_1, lblUrl,
        jLabelBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<JList, String> jListBeanProperty_2 = BeanProperty.create("selectedElement.description");
    BeanProperty<JTextPane, String> jTextPaneBeanProperty = BeanProperty.create("text");
    AutoBinding<JList, String, JTextPane, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, list, jListBeanProperty_2,
        tpDescription, jTextPaneBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<JList, Boolean> jListBeanProperty_3 = BeanProperty.create("selectedElement.detail");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<JList, Boolean, JCheckBox, Boolean> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, list, jListBeanProperty_3,
        chckbxTemplateWithDetail, jCheckBoxBeanProperty);
    autoBinding_3.bind();
  }
}
