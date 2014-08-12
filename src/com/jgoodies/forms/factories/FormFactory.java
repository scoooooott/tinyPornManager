package com.jgoodies.forms.factories;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

public class FormFactory {
  private FormFactory() {
    // Suppresses default constructor, prevents instantiation.
  }

  /* Cols */
  public static final ColumnSpec MIN_COLSPEC                 = FormSpecs.MIN_COLSPEC;
  public static final ColumnSpec PREF_COLSPEC                = FormSpecs.PREF_COLSPEC;
  public static final ColumnSpec DEFAULT_COLSPEC             = FormSpecs.DEFAULT_COLSPEC;
  public static final ColumnSpec GLUE_COLSPEC                = FormSpecs.GLUE_COLSPEC;
  public static final ColumnSpec LABEL_COMPONENT_GAP_COLSPEC = FormSpecs.LABEL_COMPONENT_GAP_COLSPEC;
  public static final ColumnSpec RELATED_GAP_COLSPEC         = FormSpecs.RELATED_GAP_COLSPEC;
  public static final ColumnSpec UNRELATED_GAP_COLSPEC       = FormSpecs.UNRELATED_GAP_COLSPEC;
  public static final ColumnSpec BUTTON_COLSPEC              = FormSpecs.BUTTON_COLSPEC;
  public static final ColumnSpec GROWING_BUTTON_COLSPEC      = FormSpecs.GROWING_BUTTON_COLSPEC;

  /* Rows */
  public static final RowSpec    MIN_ROWSPEC                 = FormSpecs.MIN_ROWSPEC;
  public static final RowSpec    PREF_ROWSPEC                = FormSpecs.PREF_ROWSPEC;
  public static final RowSpec    DEFAULT_ROWSPEC             = FormSpecs.DEFAULT_ROWSPEC;
  public static final RowSpec    GLUE_ROWSPEC                = FormSpecs.GLUE_ROWSPEC;
  public static final RowSpec    LABEL_COMPONENT_GAP_ROWSPEC = FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC;
  public static final RowSpec    RELATED_GAP_ROWSPEC         = FormSpecs.RELATED_GAP_ROWSPEC;
  public static final RowSpec    UNRELATED_GAP_ROWSPEC       = FormSpecs.UNRELATED_GAP_ROWSPEC;
  public static final RowSpec    NARROW_LINE_GAP_ROWSPEC     = FormSpecs.NARROW_LINE_GAP_ROWSPEC;
  public static final RowSpec    LINE_GAP_ROWSPEC            = FormSpecs.LINE_GAP_ROWSPEC;
  public static final RowSpec    PARAGRAPH_GAP_ROWSPEC       = FormSpecs.PARAGRAPH_GAP_ROWSPEC;
  public static final RowSpec    BUTTON_ROWSPEC              = FormSpecs.BUTTON_ROWSPEC;
}
