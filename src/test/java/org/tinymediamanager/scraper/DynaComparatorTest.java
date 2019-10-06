package org.tinymediamanager.scraper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.BeforeClass;
import org.junit.Test;

public class DynaComparatorTest {

  private static List<Person> p = new ArrayList<Person>();

  @BeforeClass
  public static void init() throws ParseException {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
    p.add(new Person("Myron", 1, 2.0f, true, df.parse("2013-05-17")));
    p.add(new Person("Manuel", 2, 1.0f, true, df.parse("2012-04-13")));
    p.add(new Person("An user", 3, 3.0f, false, new Date()));
  }

  @Test
  public void test() {
    Collections.sort(p, new DynaComparator("getName", false));
    displayPersonList(p);
    Collections.sort(p, new DynaComparator("getNumber", false));
    displayPersonList(p);
    Collections.sort(p, new DynaComparator("isActive"));
    displayPersonList(p);
    Collections.sort(p, new DynaComparator("getEntry", false));
    displayPersonList(p);
  }

  private static void displayPersonList(List<Person> persons) {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    for (Person p : persons) {
      System.out.println(String.format("%s\t%s\t%s\t%s\t%s", p.getName(), p.getNumber(), p.getScore(), p.isActive(), df.format(p.getEntry())));
    }
    System.out.println();
  }

  public static class Person {
    String  name   = "";
    int     number = 0;
    Float   score  = 0.0f;
    boolean active = false;
    Date    entry  = null;

    public Person(String name, int number, Float score, boolean active, Date entry) {
      this.name = name;
      this.number = number;
      this.score = score;
      this.active = active;
      this.entry = entry;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public int getNumber() {
      return number;
    }

    public void setNumber(int number) {
      this.number = number;
    }

    public Float getScore() {
      return score;
    }

    public void setScore(Float score) {
      this.score = score;
    }

    public boolean isActive() {
      return active;
    }

    public void setActive(boolean active) {
      this.active = active;
    }

    public Date getEntry() {
      return entry;
    }

    public void setEntry(Date entry) {
      this.entry = entry;
    }
  }
}
