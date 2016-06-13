///*
// *      Copyright (C) 2005-2015 Team XBMC
// *      http://xbmc.org
// *
// *  This Program is free software; you can redistribute it and/or modify
// *  it under the terms of the GNU General Public License as published by
// *  the Free Software Foundation; either version 2, or (at your option)
// *  any later version.
// *
// *  This Program is distributed in the hope that it will be useful,
// *  but WITHOUT ANY WARRANTY; without even the implied warranty of
// *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// *  GNU General Public License for more details.
// *
// *  You should have received a copy of the GNU General Public License
// *  along with XBMC Remote; see the file license.  If not, write to
// *  the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
// *  http://www.gnu.org/copyleft/gpl.html
// *
// */
//
//package org.tinymediamanager.jsonrpc.api;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Iterator;
//
//import org.apache.commons.lang3.builder.ToStringBuilder;
//import org.apache.commons.lang3.builder.ToStringStyle;
//import org.codehaus.jackson.JsonNode;
//import org.codehaus.jackson.map.ObjectMapper;
//import org.codehaus.jackson.node.ArrayNode;
//import org.codehaus.jackson.node.ObjectNode;
//import org.tinymediamanager.core.NonEmptyToStringStyle;
//
///**
// * This is our overridden class, just to get our custom toString() method in there.<br>
// * Did not want to add all the dependencies in lib, just for this method.
// */
//public abstract class AbstractModel implements JsonSerializable {
//
//  /**
//   * Reference to Jackson's object mapper
//   */
//  protected final static ObjectMapper OM    = new ObjectMapper();
//
//  protected String                    mType;
//
//  ////////////// BEGIN CHANGE
//  private static final ToStringStyle  style = new NonEmptyToStringStyle(); // our TMM style
//
//  @Override
//  public String toString() {
//    return ToStringBuilder.reflectionToString(this, style);
//  }
//  ////////////// END CHANGE
//
//  /**
//   * Tries to read an integer from JSON object.
//   *
//   * @param node
//   *          JSON object
//   * @param key
//   *          Key
//   * @return Integer value if found, -1 otherwise.
//   */
//  public static int parseInt(JsonNode node, String key) {
//    return node.has(key) ? node.get(key).getIntValue() : -1;
//  }
//
//  /**
//   * Tries to read an integer from JSON object.
//   *
//   * @param node
//   *          JSON object
//   * @param key
//   *          Key
//   * @return String value if found, null otherwise.
//   */
//  public static String parseString(JsonNode node, String key) {
//    return node.has(key) ? node.get(key).getTextValue() : null;
//  }
//
//  /**
//   * Tries to read an boolean from JSON object.
//   *
//   * @param node
//   *          JSON object
//   * @param key
//   *          Key
//   * @return String value if found, null otherwise.
//   */
//  public static Boolean parseBoolean(JsonNode node, String key) {
//    final boolean hasKey = node.has(key);
//    if (hasKey) {
//      return node.get(key).getBooleanValue();
//    }
//    else {
//      return null;
//    }
//  }
//
//  public static Double parseDouble(JsonNode node, String key) {
//    return node.has(key) ? node.get(key).getDoubleValue() : null;
//  }
//
//  public static ArrayList<String> getStringArray(JsonNode node, String key) {
//    if (node.has(key)) {
//      final ArrayNode a = (ArrayNode) node.get(key);
//      final ArrayList<String> l = new ArrayList<>(a.size());
//      for (int i = 0; i < a.size(); i++) {
//        l.add(a.get(i).getTextValue());
//      }
//      return l;
//    }
//    return new ArrayList<>(0);
//  }
//
//  public static ArrayList<Integer> getIntegerArray(JsonNode node, String key) {
//    if (node.has(key)) {
//      final ArrayNode a = (ArrayNode) node.get(key);
//      final ArrayList<Integer> l = new ArrayList<>(a.size());
//      for (int i = 0; i < a.size(); i++) {
//        l.add(a.get(i).getIntValue());
//      }
//      return l;
//    }
//    return new ArrayList<>(0);
//  }
//
//  public static HashMap<String, String> getStringMap(JsonNode node, String key) {
//    if (node.has(key)) {
//      final ObjectNode n = (ObjectNode) node.get(key);
//      final HashMap<String, String> m = new HashMap<>();
//      final Iterator<String> it = n.getFieldNames();
//      while (it.hasNext()) {
//        final String fieldName = it.next();
//        m.put(fieldName, n.get(fieldName).getValueAsText());
//      }
//      return m;
//    }
//    return new HashMap<>();
//  }
//
//}
