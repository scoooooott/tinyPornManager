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
package org.tinymediamanager.scraper.opensubtitles;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.tinymediamanager.scraper.http.TmmHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.timroes.axmlrpc.Call;
import de.timroes.axmlrpc.XMLRPCClient;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;
import de.timroes.axmlrpc.XMLUtil;
import de.timroes.axmlrpc.serializer.SerializerHandler;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * This is the tinyMediaManager implementation of the XMLRPCClient to use our http client
 *
 * @author Manuel Laggner
 */
public class TmmXmlRpcClient {
  private static OkHttpClient client;

  private Map<String, Object> callCache = new HashMap<>();

  private URL                 url;
  private String              userAgent;
  private ResponseParser      responseParser;

  public TmmXmlRpcClient(URL url, String userAgent) {
    this.url = url;
    this.userAgent = userAgent;
    SerializerHandler.initialize(XMLRPCClient.FLAGS_8BYTE_INT);
    responseParser = new ResponseParser();

    if (client == null) {
      client = TmmHttpClient.getHttpClient();
    }
  }

  /**
   * Call a remote procedure on the server. The method must be described by a method name. If the method requires parameters, this must be set. The
   * type of the return object depends on the server. You should consult the server documentation and then cast the return value according to that.
   * This method will block until the server returned a result (or an error occurred). Read the README file delivered with the source code of this
   * library for more information.
   *
   * @param method
   *          A method name to call.
   * @param params
   *          An array of parameters for the method.
   * @return The result of the server.
   * @throws TmmXmlRpcException
   *           Will be thrown if an error occurred during the call.
   */
  public Object call(String method, Object... params) throws TmmXmlRpcException {
    return new Caller().call(method, params);
  }

  /**
   * The Caller class is used to make asynchronous calls to the server. For synchronous calls the Thread function of this class isn't used.
   */
  private class Caller {
    private static final String HTTP_POST  = "POST";
    private static final String USER_AGENT = "User-Agent";

    private final MediaType     XML        = MediaType.parse("text/xml");

    /**
     * Create a new Caller for synchronous use. If the caller has been created with this constructor you cannot use the start method to start it as a
     * thread. But you can call the call method on it for synchronous use.
     */
    public Caller() {
    }

    /**
     * Call a remote procedure on the server. The method must be described by a method name. If the method requires parameters, this must be set. The
     * type of the return object depends on the server. You should consult the server documentation and then cast the return value according to that.
     * This method will block until the server returned a result (or an error occurred). Read the README file delivered with the source code of this
     * library for more information.
     *
     * @param methodName
     *          A method name to call.
     * @param params
     *          An array of parameters for the method.
     * @return The result of the server.
     * @throws TmmXmlRpcException
     *           Will be thrown if an error occurred during the call.
     */
    public Object call(String methodName, Object[] params) throws TmmXmlRpcException {
      try {
        Call c = new Call(methodName, params);
        String callXml = c.getXML();

        // look in the cache if there is a cached call
        Object cachedResponse = callCache.get(callXml);
        if (cachedResponse != null) {
          return cachedResponse;
        }

        RequestBody body = RequestBody.create(XML, callXml);
        Request request = new Request.Builder().url(url).header(USER_AGENT, userAgent).addHeader("Connection", "close").post(body).build();
        Response response = client.newCall(request).execute();

        // Try to get the status code from the connection
        int statusCode = response.code();

        // if the response was not successful, throw an exception
        if (statusCode != HttpURLConnection.HTTP_OK) {
          throw new TmmXmlRpcException(statusCode, url.toString());
        }

        // cache the response
        cachedResponse = responseParser.parse(response.body().byteStream());
        callCache.put(callXml, cachedResponse);

        return cachedResponse;
      }
      catch (Exception ex) {
        throw new TmmXmlRpcException(ex, url.toString());
      }
    }
  }

  private class ResponseParser {

    private static final String FAULT_CODE      = "faultCode";
    private static final String FAULT_STRING    = "faultString";
    private static final String METHOD_RESPONSE = "methodResponse";
    private static final String PARAMS          = "params";
    private static final String PARAM           = "param";
    private static final String FAULT           = "fault";

    /**
     * The given InputStream must contain the xml response from an xmlrpc server. This method extract the content of it as an object.
     *
     * @param response
     *          The InputStream of the server response.
     * @return The returned object.
     * @throws XMLRPCException
     *           Will be thrown whenever something fails.
     * @throws XMLRPCServerException
     *           Will be thrown, if the server returns an error.
     */
    public Object parse(InputStream response) throws XMLRPCException {

      try {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document dom = builder.parse(response);
        Element e = dom.getDocumentElement();

        // Check for root tag
        if (!e.getNodeName().equals(METHOD_RESPONSE)) {
          throw new XMLRPCException("MethodResponse root tag is missing.");
        }

        e = XMLUtil.getOnlyChildElement(e.getChildNodes());

        if (e.getNodeName().equals(PARAMS)) {
          e = XMLUtil.getOnlyChildElement(e.getChildNodes());

          if (!e.getNodeName().equals(PARAM)) {
            throw new XMLRPCException("The params tag must contain a param tag.");
          }

          return getReturnValueFromElement(e);
        }
        else if (e.getNodeName().equals(FAULT)) {
          @SuppressWarnings("unchecked")
          Map<String, Object> o = (Map<String, Object>) getReturnValueFromElement(e);

          throw new XMLRPCServerException((String) o.get(FAULT_STRING), (Integer) o.get(FAULT_CODE));
        }

        throw new XMLRPCException("The methodResponse tag must contain a fault or params tag.");
      }
      catch (Exception ex) {
        if (ex instanceof XMLRPCServerException) {
          throw (XMLRPCServerException) ex;
        }
        else {
          throw new XMLRPCException(ex.getMessage());
        }
      }
    }

    /**
     * This method takes an element (must be a param or fault element) and returns the deserialized object of this param tag.
     *
     * @param element
     *          An param element.
     * @return The deserialized object within the given param element.
     * @throws XMLRPCException
     *           Will be thrown when the structure of the document doesn't match the XML-RPC specification.
     */
    private Object getReturnValueFromElement(Element element) throws XMLRPCException {
      element = XMLUtil.getOnlyChildElement(element.getChildNodes());
      return SerializerHandler.getDefault().deserialize(element);
    }
  }
}
