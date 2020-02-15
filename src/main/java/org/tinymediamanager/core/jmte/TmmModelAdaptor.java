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

package org.tinymediamanager.core.jmte;

import static com.floreysoft.jmte.message.ErrorMessage.INDEX_OUT_OF_BOUNDS;
import static com.floreysoft.jmte.message.ErrorMessage.INVALID_INDEX;
import static com.floreysoft.jmte.message.ErrorMessage.NOT_ARRAY;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tinymediamanager.scraper.DynaEnum;

import com.floreysoft.jmte.DefaultModelAdaptor;
import com.floreysoft.jmte.ErrorHandler;
import com.floreysoft.jmte.ModelBuilder;
import com.floreysoft.jmte.token.Token;
import com.floreysoft.jmte.util.Util;

/**
 * the class TmmModelAdaptor is used as a custom ModelAdaptor for JMTE
 *
 * @author Manuel Laggner
 */
public class TmmModelAdaptor extends DefaultModelAdaptor {
  private static final String ERROR_STRING = "";

  @Override
  protected Object getIndexFromArray(Object array, String arrayIndex, ErrorHandler errorHandler, Token token) {
    if (array == null) {
      errorHandler.error(NOT_ARRAY, token, new ModelBuilder("array", "[null]").build());
      return ERROR_STRING;
    }
    List<Object> arrayAsList = Util.arrayAsList(array);
    try {
      if (arrayAsList != null) {
        try {
          final int index;
          if (arrayIndex.equalsIgnoreCase("last")) {
            if (!arrayAsList.isEmpty()) {
              index = arrayAsList.size() - 1;
              return arrayAsList.get(index);
            }
            else {
              if (array != ERROR_STRING) {
                errorHandler.error(INDEX_OUT_OF_BOUNDS, token, new ModelBuilder("arrayIndex", arrayIndex, "array", array.toString()).build());
              }
              return ERROR_STRING;
            }
          }
          else if (arrayIndex.contains(",")) {
            // try to get an interval
            String[] interval = arrayIndex.split(",");
            int start = Integer.parseInt(interval[0]);
            int end = start + Integer.parseInt(interval[1]);
            List<Object> returnArray = new ArrayList<>();
            for (int i = start; i < end && i < arrayAsList.size(); i++) {
              returnArray.add(arrayAsList.get(i));
            }
            return returnArray;
          }
          else {
            index = Integer.parseInt(arrayIndex);
            return arrayAsList.get(index);
          }
        }
        catch (NumberFormatException nfe) {
          if (array != ERROR_STRING) {
            errorHandler.error(INVALID_INDEX, token, new ModelBuilder("arrayIndex", arrayIndex, "array", array.toString()).build());
          }
          return ERROR_STRING;
        }
      }
      else {
        if (array instanceof String && array != ERROR_STRING) {
          if (arrayIndex.contains(",")) {
            // try to get an interval
            String[] interval = arrayIndex.split(",");
            int start = Integer.parseInt(interval[0]);
            int end = start + Integer.parseInt(interval[1]);
            return array.toString().substring(start, end);
          }
          else {
            int index = Integer.parseInt(arrayIndex);
            return array.toString().substring(index, index + 1);
          }
        }
        if (array != ERROR_STRING) {
          errorHandler.error(NOT_ARRAY, token, new ModelBuilder("array", array.toString()).build());
        }
        return array;
      }
    }
    catch (IndexOutOfBoundsException e) {
      return ERROR_STRING;
    }
  }

  @SuppressWarnings("rawtypes")
  protected Object getPropertyValue(Object o, String propertyName) {
    try {
      // XXX this is so strange, can not call invoke on key and value for
      // Map.Entry, so we have to get this done like this:
      if (o instanceof Map.Entry) {
        final Map.Entry entry = (Map.Entry) o;
        if (propertyName.equals("key")) {
          return entry.getKey();
        }
        else if (propertyName.equals("value")) {
          return entry.getValue();
        }

      }
      boolean valueSet = false;
      Object value = null;
      Member member = null;
      final Class<?> clazz = o.getClass();
      Map<String, Member> members = cache.get(clazz);
      if (members == null) {
        members = new HashMap<>();
        cache.put(clazz, members);
      }
      else {
        member = members.get(propertyName);
        if (member != null) {
          if (member.getClass() == Method.class)
            return ((Method) member).invoke(o);
          if (member.getClass() == Field.class)
            return ((Field) member).get(o);
        }
      }

      final String suffix = Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
      final Method[] declaredMethods = clazz.getMethods();
      for (Method method : declaredMethods) {
        if (Modifier.isPublic(method.getModifiers())
            && (method.getName().equals("get" + suffix) || method.getName().equals("is" + suffix)
                || ((o instanceof Enum || o instanceof DynaEnum) && method.getName().equals(propertyName)))
            && method.getParameterTypes().length == 0) {
          value = method.invoke(o, (Object[]) null);
          valueSet = true;
          member = method;
          break;
        }
      }
      if (!valueSet) {
        final Field field = clazz.getField(propertyName);
        if (Modifier.isPublic(field.getModifiers())) {
          value = field.get(o);
          member = field;
          valueSet = true;
        }
      }
      if (valueSet) {
        members.put(propertyName, member);
      }
      return value;
    }
    catch (NoSuchFieldException e) {
      // swallow NoSuchFieldExceptions
      return null;
    }
    catch (Exception e) {
      // swallow NoSuchFieldExceptions
      throw new RuntimeException(e);
    }
  }
}
