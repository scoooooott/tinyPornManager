package org.tinymediamanager.scraper.xbmc;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class RegExp implements RegExpContainer {
  private String       input, output;
  private int          dest;
  private String       conditional;
  private boolean      appendBuffer = false;
  private List<RegExp> children     = new ArrayList<RegExp>();
  private Expression   expression;

  public RegExp() {
  }

  public void addRegExp(RegExp regexp) {
    children.add(regexp);
  }

  public RegExp[] getRegExps() {
    return children.toArray(new RegExp[children.size()]);
  }

  public boolean hasRegExps() {
    return children != null && children.size() > 0;
  }

  public String getInput() {
    return input;
  }

  public String getOutput() {
    return output;
  }

  public int getDest() {
    return dest;
  }

  public void setInput(String input) {
    if (StringUtils.isEmpty(input))
      input = "$$1";
    this.input = input;
  }

  public void setOutput(String output) {
    this.output = output;
  }

  public void setDest(int dest) {
    this.dest = dest;
  }

  public Expression getExpression() {
    return expression;
  }

  public void setExpression(Expression expression) {
    this.expression = expression;
  }

  public boolean isAppendBuffer() {
    return appendBuffer;
  }

  public void setAppendBuffer(boolean appendBuffer) {
    this.appendBuffer = appendBuffer;
  }

  public String getConditional() {
    return conditional;
  }

  public void setConditional(String conditional) {
    this.conditional = conditional;
  }

  /**
   * <p>
   * Uses <code>ReflectionToStringBuilder</code> to generate a <code>toString</code> for the specified object.
   * </p>
   * 
   * @return the String result
   * @see ReflectionToStringBuilder#toString(Object)
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
