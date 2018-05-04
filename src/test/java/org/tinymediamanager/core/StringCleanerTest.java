package org.tinymediamanager.core;

import java.util.Iterator;

import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.scraper.util.StrgUtils;

/**
 * StringCleaner provides a method for normalizing a string to generally ASCII-compatible form.
 */
public class StringCleanerTest extends BasicTest {

  /***********************************/
  @Test
  public void testCleaner() {
    /* lower case test */
    TestDataIterator it = new TestDataIterator(false);
    while (it.hasNext()) {
      Object[] data = it.next();
      String result = StrgUtils.convertToAscii((String) data[0], false);
      assertEqual(result, data[1]);
    }
  }

  /**
   * This array of String Arrays holds the data for all tests.
   * 
   * <p>
   * The data contained in each array has the following semantics:
   * </p>
   * <code>{ "EXP(lc)", "EXP(uc)", "TV_1", ... , "TV-n" }</code>
   * <p>
   * Legend:
   * </p>
   * <ul>
   * <li>EXP(lc): Reference value for lowercase tests (expectation)</li>
   * <li>EXP(uc): Reference value for uppercase tests (expectation)</li>
   * <li>TV_1...TV_n: Test String/Character 1...n</li>
   * </ul>
   */
  private static final String[][] testDataSource = {
      //
      /* Sanity checks first */
      { "ABCDEFGHIJKLMNOPQRSTUVWXYZ", "ABCDEFGHIJKLMNOPQRSTUVWXYZ", "ABCDEFGHIJKLMNOPQRSTUVWXYZ" }, //
      { "abcdefghijklmnopqrstuvwxyz", "abcdefghijklmnopqrstuvwxyz", "abcdefghijklmnopqrstuvwxyz" }, //
      { "1234567890", "1234567890", "1234567890" }, //
      /* Symbols */
      { "!\"§$%&/()=?'\\", "!\"§$%&/()=?'\\", "!\"§$%&/()=?'\\" }, //
      { "÷×¡¢£¤·", "÷×¡¢£¤·", "÷×¡¢£¤·" }, //
      { " \t\r\n", " \t\r\n", " \t\r\n" }, //
      /* Fun starts here */
      { "A", "A", "Ā", "Ă", "Å", "À", "Â" }, //
      { "a", "a", "ā", "ă", "å", "à", "â" }, //
      { "Ae", "AE", "Æ", "Ǽ", "Ä" }, //
      { "ae", "ae", "æ", "ǣ", "ä", "ǟ" }, //
      { "C", "C", "Ċ", "Ç", "Č" }, //
      { "c", "c", "ċ", "ç", "č" }, //
      { "D", "D", "Ď", "Ð" }, //
      { "d", "d", "ď", "đ" }, //
      { "E", "E", "Ê", "Ë", "È", "É", "Ê" }, //
      { "e", "e", "ê", "ë", "è", "é" }, //
      { "G", "G", "Ĝ", "Ğ", "Ġ", "Ģ" }, //
      { "g", "g", "ĝ", "ğ", "ġ", "ģ" }, //
      { "I", "I", "Ì", "Í", "Î", "Ï", "Ĩ" }, //
      { "i", "i", "ĩ", "ì", "í", "î", "ï" }, //
      { "l", "l", "ł" }, { "L", "L", "Ł" },
      { "N", "N", "Ñ" }, //
      { "n", "n", "ñ", }, //
      { "O", "O", "Ø", "Ò", "Ó", "Ô", "Õ", "Ő", "Ǿ" }, //
      { "o", "o", "ø", "ő", "ò", "ó", "ô", "õ", "ǿ" }, //
      { "Oe", "OE", "Ö", "Œ" }, //
      { "oe", "oe", "ö", "œ" }, //
      { "ss", "ss", "ß" }, //
      { "Aeffin", "AEffin", "Ä\uFB03n" }, //
      { "IJ", "IJ", "Ĳ" }, //
      { "ij", "ij", "ĳ" }, //
      { "U", "U", "Û", "Ù", "Ú", "Ů" }, //
      { "u", "u", "û", "ù", "ú", "ů" }, //
      { "Ue", "UE", "Ü" }, //
      { "ue", "ue", "ü" }, //
      { "T", "T", "Ţ", "Ŧ" }, //
      { "t", "t", "ţ", "ŧ" }, //
      { "Y", "Y", "Ý" }, //
      { "y", "y", "ý", "ÿ" } //
  };

  /**
   * Implementation of an iterator that knows how to iterate over the source test data array for upper- and lowercase mode.
   * 
   * @author JHAUSHER
   */
  private static final class TestDataIterator implements Iterator<Object[]> {
    int           dataIndex    = 0;
    int           currentIndex = 2;

    final boolean uppercase;

    public TestDataIterator(boolean uppercase) {
      this.uppercase = uppercase;
    }

    public boolean hasNext() {
      return !(dataIndex == testDataSource.length && currentIndex != testDataSource[testDataSource.length - 1].length - 1);
    }

    public Object[] next() {
      Object[] result = new Object[2];
      int idx = currentIndex++;
      result[0] = testDataSource[dataIndex][idx];
      result[1] = (uppercase ? testDataSource[dataIndex][1] : testDataSource[dataIndex][0]);

      if (currentIndex == testDataSource[dataIndex].length) {
        currentIndex = 2;
        dataIndex += 1;
      }

      return result;
    }

    public void remove() {
      // ignore
    }
  }
}