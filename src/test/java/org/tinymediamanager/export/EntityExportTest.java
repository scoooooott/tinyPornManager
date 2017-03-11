/*
 * Copyright 2012 - 2017 Manuel Laggner
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
package org.tinymediamanager.export;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.core.tvshow.entities.TvShow;

public class EntityExportTest extends BasicTest {

  @Test
  public void exportMovieProperties() {
    List<String> entities = new ArrayList<>();

    printPropertyEntries(Movie.class, entities);

    int i = 0;
    while (i < entities.size()) {
      String entity = entities.get(i++);
      try {
        Class clazz = Class.forName(entity);
        printPropertyEntries(clazz, entities);
      }
      catch (Exception e) {
        e.printStackTrace();
        Assertions.fail(e.getMessage());
      }
    }
  }

  @Test
  public void exportMovieSetProperties() {
    List<String> entities = new ArrayList<>();

    printPropertyEntries(MovieSet.class, entities);

    int i = 0;
    while (i < entities.size()) {
      String entity = entities.get(i++);
      try {
        Class clazz = Class.forName(entity);
        printPropertyEntries(clazz, entities);
      }
      catch (Exception e) {
        e.printStackTrace();
        Assertions.fail(e.getMessage());
      }
    }
  }

  @Test
  public void exportTvShowProperties() {
    List<String> entities = new ArrayList<>();

    printPropertyEntries(TvShow.class, entities);

    int i = 0;
    while (i < entities.size()) {
      String entity = entities.get(i++);
      try {
        Class clazz = Class.forName(entity);
        printPropertyEntries(clazz, entities);
      }
      catch (Exception e) {
        e.printStackTrace();
        Assertions.fail(e.getMessage());
      }
    }
  }

  private void printPropertyEntries(Class clazz, List<String> entities) {
    // deactivated since that won't compile on java7
    // try {
    // System.out.println("|" + getCleanedClassname(clazz.getName()) + "||");
    // System.out.println("|---|---|");
    // for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(clazz, Object.class).getPropertyDescriptors()) {
    // String line = "|";
    // // propertyEditor.getReadMethod() exposes the getter
    // // btw, this may be null if you have a write-only property
    // Method method = propertyDescriptor.getReadMethod();
    // if (method == null) {
    // continue;
    // }
    //
    // if (method.getGenericReturnType() instanceof ParameterizedType) {
    // String returnType = getCleanedClassname(method.getReturnType().getTypeName().toString()) + "\\<";
    //
    // ParameterizedType genericReturnType = (ParameterizedType) method.getGenericReturnType();
    // List<String> typeNames = new ArrayList<>();
    // for (Type type : genericReturnType.getActualTypeArguments()) {
    // typeNames.add(getCleanedClassname(type.getTypeName()));
    // if (type.getTypeName().startsWith("org.tinymediamanager") && !entities.contains(type.getTypeName())) {
    // entities.add(type.getTypeName());
    // }
    // }
    //
    // line += returnType + StringUtils.join(typeNames, ", ") + "\\>" + "|";
    // }
    // else {
    // line += getCleanedClassname(method.getReturnType().getTypeName().toString()) + "|";
    // }
    //
    // line += propertyDescriptor.getDisplayName() + "|";
    // System.out.println(line);
    //
    // }
    // System.out.println("");
    // }
    // catch (Exception e) {
    // e.printStackTrace();
    // Assertions.fail(e.getMessage());
    // }
  }

  private String getCleanedClassname(String fullClassname) {
    Pattern pattern = Pattern.compile(".*\\.(.*?)$");
    Matcher matcher = pattern.matcher(fullClassname);
    if (matcher.matches() && matcher.groupCount() > 0) {
      return matcher.group(1);
    }
    return fullClassname;
  }
}
