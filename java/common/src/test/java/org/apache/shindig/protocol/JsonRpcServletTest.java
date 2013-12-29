/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.shindig.protocol;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.apache.shindig.api.auth.SecurityToken;
import org.apache.shindig.common.DefaultJsonSerializer;
import org.apache.shindig.common.JsonAssert;
import org.apache.shindig.config.ContainerConfig;
import org.apache.shindig.protocol.conversion.BeanJsonConverter;
import org.apache.shindig.protocol.multipart.FormDataItem;
import org.apache.shindig.protocol.multipart.MultipartFormParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonRpcServletTest extends JsonAssert {

  private static final String IMAGE_FIELDNAME = "profile-photo";
  private static final String IMAGE_DATA = "image data";
  private static final byte[] IMAGE_DATA_BYTES = IMAGE_DATA.getBytes();
  private static final String IMAGE_TYPE = "image/jpeg";

  @Mock
  private HttpServletRequest req;

  @Mock
  private HttpServletResponse res;

  @Mock
  private MultipartFormParser multipartFormParser;

  @Mock
  private ContainerConfig containerConfig;

  @Mock
  private SecurityToken token;

  private JsonRpcServlet servlet;

  private final ByteArrayOutputStream stream = new ByteArrayOutputStream();
  private final PrintWriter writer = new PrintWriter(stream);
  private final TestHandler handler = new TestHandler();

  public JsonRpcServletTest() {
    super(new DefaultJsonSerializer());
  }

  @Before
  public void setUp() throws Exception {

  //  = new FakeGadgetToken()
//        .setOwnerId("john.doe").setViewerId("john.doe");
    servlet = new JsonRpcServlet();
    when(multipartFormParser.isMultipartContent(req)).thenReturn(false);
    servlet.setMultipartFormParser(multipartFormParser);

    BeanJsonConverter converter = new BeanJsonConverter();

    HandlerRegistry registry = new DefaultHandlerRegistry(null, null,
        new HandlerExecutionListener.NoOpHandler());
    registry.addHandlers(Collections.<Object>singleton(handler));

    servlet.setHandlerRegistry(registry);
    servlet.setBeanConverters(converter, null, null);
    servlet.setContainerConfig(containerConfig);
    servlet.setJSONPAllowed(true);

    handler.setMock(new TestHandler() {
      @Override
      public Object get(RequestItem req) {
        return ImmutableMap.of("foo", "bar");
      }
    });
  }

  private String getOutput() throws IOException {
    writer.close();
    return stream.toString("UTF-8");
  }

  @Test
  public void testMethodRecognition() throws Exception {
    setupRequest("{method:test.get,id:id,params:{userId:5,groupId:@self}}");

    when(res.getWriter()).thenReturn(writer);

    servlet.service(req, res);
    verify(res).getWriter();

    assertJsonEquals("{id: 'id', result: {foo:'bar'}}", getOutput());
  }

  @Test
  public void testPostMultipartFormData() throws Exception {
    reset(multipartFormParser);

    handler.setMock(new TestHandler() {
      @Override
      public Object get(RequestItem req) {
        FormDataItem item = req.getFormMimePart(IMAGE_FIELDNAME);
        return ImmutableMap.of("image-data", new String(item.get()),
            "image-type", item.getContentType(),
            "image-ref", req.getParameter("image-ref"));
      }
    });
    when(req.getMethod()).thenReturn("POST");
    when(req.getAttribute(isA(String.class))).thenReturn(token);
    when(req.getCharacterEncoding()).thenReturn("UTF-8");
    when(req.getContentType()).thenReturn(ContentTypes.MULTIPART_FORM_CONTENT_TYPE);
    res.setCharacterEncoding("UTF-8");
    res.setContentType(ContentTypes.OUTPUT_JSON_CONTENT_TYPE);

    List<FormDataItem> formItems = Lists.newArrayList();
    String request = "{method:'test.get',id:'id',params:" +
        "{userId:5,groupId:'@self',image-ref:'@" + IMAGE_FIELDNAME + "'}}";
    formItems.add(mockFormDataItem(JsonRpcServlet.REQUEST_PARAM,
        ContentTypes.OUTPUT_JSON_CONTENT_TYPE, request.getBytes(), true));
    formItems.add(mockFormDataItem(IMAGE_FIELDNAME, IMAGE_TYPE, IMAGE_DATA_BYTES, false));
    when(multipartFormParser.isMultipartContent(req)).thenReturn(true);
    when(multipartFormParser.parse(req)).thenReturn(formItems);
    when(res.getWriter()).thenReturn(writer);

    servlet.service(req, res);

    assertJsonEquals("{id: 'id', result: {image-data:'" + IMAGE_DATA +
        "', image-type:'" + IMAGE_TYPE + "', image-ref:'@" + IMAGE_FIELDNAME + "'}}", getOutput());
  }

  /**
   * Test that it passes even when content-type is not set for "request" parameter. This would
   * be the case where the request is published via webform.
   */
  @Test
  public void testPostMultipartFormDataWithRequestFieldHavingNoContentType() throws Exception {
    reset(multipartFormParser);

    handler.setMock(new TestHandler() {
      @Override
      public Object get(RequestItem req) {
        FormDataItem item = req.getFormMimePart(IMAGE_FIELDNAME);
        return ImmutableMap.of("image-data", new String(item.get()),
            "image-type", item.getContentType(),
            "image-ref", req.getParameter("image-ref"));
      }
    });
    when(req.getMethod()).thenReturn("POST");
    when(req.getAttribute(isA(String.class))).thenReturn(token);
    when(req.getCharacterEncoding()).thenReturn("UTF-8");
    when(req.getContentType()).thenReturn(ContentTypes.MULTIPART_FORM_CONTENT_TYPE);
    res.setCharacterEncoding("UTF-8");
    res.setContentType(ContentTypes.OUTPUT_JSON_CONTENT_TYPE);

    List<FormDataItem> formItems = Lists.newArrayList();
    String request = "{method:'test.get',id:'id',params:" +
        "{userId:5,groupId:'@self',image-ref:'@" + IMAGE_FIELDNAME + "'}}";
    formItems.add(mockFormDataItem(IMAGE_FIELDNAME, IMAGE_TYPE, IMAGE_DATA_BYTES, false));
    formItems.add(mockFormDataItem("request", null, request.getBytes(), true));
    when(multipartFormParser.isMultipartContent(req)).thenReturn(true);
    when(multipartFormParser.parse(req)).thenReturn(formItems);
    when(res.getWriter()).thenReturn(writer);

    servlet.service(req, res);

    assertJsonEquals("{id: 'id', result: {image-data:'" + IMAGE_DATA +
        "', image-type:'" + IMAGE_TYPE + "', image-ref:'@" + IMAGE_FIELDNAME + "'}}", getOutput());
  }


  /**
   * Test that any form-data other than "request" does not undergo any content type check.
   */
  @Test
  public void testPostMultipartFormDataOnlyRequestFieldHasContentTypeChecked()
      throws Exception {
    reset(multipartFormParser);

    handler.setMock(new TestHandler() {
      @Override
      public Object get(RequestItem req) {
        FormDataItem item = req.getFormMimePart(IMAGE_FIELDNAME);
        return ImmutableMap.of("image-data", new String(item.get()),
            "image-type", item.getContentType(),
            "image-ref", req.getParameter("image-ref"));
      }
    });
    when(req.getMethod()).thenReturn("POST");
    when(req.getAttribute(isA(String.class))).thenReturn(token);
    when(req.getCharacterEncoding()).thenReturn("UTF-8");
    when(req.getContentType()).thenReturn(ContentTypes.MULTIPART_FORM_CONTENT_TYPE);
    res.setCharacterEncoding("UTF-8");
    res.setContentType(ContentTypes.OUTPUT_JSON_CONTENT_TYPE);

    List<FormDataItem> formItems = Lists.newArrayList();
    String request = "{method:'test.get',id:'id',params:" +
        "{userId:5,groupId:'@self',image-ref:'@" + IMAGE_FIELDNAME + "'}}";
    formItems.add(mockFormDataItem(IMAGE_FIELDNAME, IMAGE_TYPE, IMAGE_DATA_BYTES, false));
    formItems.add(mockFormDataItem("oauth_hash", "application/octet-stream",
        "oauth-hash".getBytes(), true));
    formItems.add(mockFormDataItem("request", null, request.getBytes(), true));
    formItems.add(mockFormDataItem("oauth_signature", "application/octet-stream",
        "oauth_signature".getBytes(), true));
    when(multipartFormParser.isMultipartContent(req)).thenReturn(true);
    when(multipartFormParser.parse(req)).thenReturn(formItems);
    when(res.getWriter()).thenReturn(writer);

    servlet.service(req, res);

    assertJsonEquals("{id: 'id', result: {image-data:'" + IMAGE_DATA +
        "', image-type:'" + IMAGE_TYPE + "', image-ref:'@" + IMAGE_FIELDNAME + "'}}", getOutput());
  }

  /**
   * Test that "request" field undergoes contentType check, and error is thrown if wrong content
   * type is present.
   */
  @Test
  public void testPostMultipartFormDataRequestFieldIsSubjectedToContentTypeCheck()
      throws Exception {
    reset(multipartFormParser);

    handler.setMock(new TestHandler() {
      @Override
      public Object get(RequestItem req) {
        FormDataItem item = req.getFormMimePart(IMAGE_FIELDNAME);
        return ImmutableMap.of("image-data", item.get(),
            "image-type", item.getContentType(),
            "image-ref", req.getParameter("image-ref"));
      }
    });
    when(req.getMethod()).thenReturn("POST");
    when(req.getAttribute(isA(String.class))).thenReturn(token);
    when(req.getCharacterEncoding()).thenReturn("UTF-8");
    when(req.getContentType()).thenReturn(ContentTypes.MULTIPART_FORM_CONTENT_TYPE);
    res.setCharacterEncoding("UTF-8");
    res.setContentType(ContentTypes.OUTPUT_JSON_CONTENT_TYPE);

    List<FormDataItem> formItems = Lists.newArrayList();
    String request = "{method:'test.get',id:'id',params:" +
        "{userId:5,groupId:'@self',image-ref:'@" + IMAGE_FIELDNAME + "'}}";
    formItems.add(mockFormDataItem(IMAGE_FIELDNAME, IMAGE_TYPE, IMAGE_DATA_BYTES, false));
    formItems.add(mockFormDataItem("request", "application/octet-stream", request.getBytes(),
        true));
    when(multipartFormParser.isMultipartContent(req)).thenReturn(true);
    when(multipartFormParser.parse(req)).thenReturn(formItems);
    when(res.getWriter()).thenReturn(writer);

    servlet.service(req, res);

    String output = getOutput();
    assertTrue(output.contains("Unsupported Content-Type application/octet-stream"));
  }

  @Test
  public void testInvalidService() throws Exception {
    setupRequest("{method:junk.get,id:id,params:{userId:5,groupId:@self}}");

    when(res.getWriter()).thenReturn(writer);

    servlet.service(req, res);

    assertJsonEquals(
        "{id:id,error:{message:'notImplemented: The method junk.get is not implemented',code:501}}",
        getOutput());
  }


  /**
   * Tests a data handler that returns a failed Future.
   * @throws Exception on failure
   */
  @Test
  public void testFailedRequest() throws Exception {
    setupRequest("{id:id,method:test.futureException}");

    when(res.getWriter()).thenReturn(writer);

    servlet.service(req, res);

    assertJsonEquals(
        "{id:id,error:{message:'badRequest: FAILURE_MESSAGE',code:400}}", getOutput());
  }

  @Test
  public void testBasicBatch() throws Exception {
    setupRequest("[{method:test.get,id:'1'},{method:test.get,id:'2'}]");

    when(res.getWriter()).thenReturn(writer);

    servlet.service(req, res);

    assertJsonEquals("[{id:'1',result:{foo:'bar'}},{id:'2',result:{foo:'bar'}}]",
        getOutput());
  }

  @Test
  public void testDisallowJSONP() throws Exception {
    servlet.setJSONPAllowed(false);
    setupRequest("[{method:test.get,id:'1'},{method:test.get,id:'2'}]");

    when(res.getWriter()).thenReturn(writer);

    servlet.service(req, res);

    assertJsonEquals("[{id:'1',result:{foo:'bar'}},{id:'2',result:{foo:'bar'}}]",
        getOutput());
    servlet.setJSONPAllowed(true);
  }

  @Test
  public void testGetExecution() throws Exception {
    when(req.getParameterMap()).thenReturn(
        ImmutableMap.of("method", new String[]{"test.get"}, "id", new String[]{"1"}));
    when(req.getMethod()).thenReturn("GET");
    when(req.getAttribute(isA(String.class))).thenReturn(token);
    when(req.getCharacterEncoding()).thenReturn("UTF-8");
    res.setCharacterEncoding("UTF-8");

    when(res.getWriter()).thenReturn(writer);

    servlet.service(req, res);

    assertJsonEquals("{id:'1',result:{foo:'bar'}}", getOutput());
  }

  @Test
  public void testGetJsonResponseWithKey() throws Exception {
    ResponseItem responseItem = new ResponseItem("Name");
    Object result = servlet.getJSONResponse("my-key", responseItem);
    assertObjectEquals("{id: 'my-key', result: 'Name'}", result);
  }

  @Test
  public void testGetJsonResponseWithoutKey() throws Exception {
    ResponseItem responseItem = new ResponseItem("Name");
    Object result = servlet.getJSONResponse(null, responseItem);
    assertObjectEquals("{result: 'Name'}", result);
  }

  @Test
  public void testGetJsonResponseErrorWithData() throws Exception {
    ResponseItem responseItem = new ResponseItem(401, "Error Message", "Optional Data");
    Object result = servlet.getJSONResponse(null, responseItem);
    assertObjectEquals(
        "{error: {message: 'unauthorized: Error Message', data: 'Optional Data', code: 401}}",
        result);
  }

  @Test
  public void testGetJsonResponseErrorWithoutData() throws Exception {
    ResponseItem responseItem = new ResponseItem(401, "Error Message");
    Object result = servlet.getJSONResponse(null, responseItem);
    assertObjectEquals(
        "{error: {message:'unauthorized: Error Message', code:401}}",
        result);
  }

  private void setupRequest(String json) throws IOException {
    final InputStream in = new ByteArrayInputStream(json.getBytes());
    ServletInputStream stream = new ServletInputStream() {
      @Override
      public int read() throws IOException {
        return in.read();
      }
    };

    when(req.getInputStream()).thenReturn(stream);
    when(req.getMethod()).thenReturn("POST");
    when(req.getAttribute(isA(String.class))).thenReturn(token);
    when(req.getCharacterEncoding()).thenReturn("UTF-8");
    when(req.getContentType()).thenReturn(ContentTypes.OUTPUT_JSON_CONTENT_TYPE);
    res.setCharacterEncoding("UTF-8");
    res.setContentType(ContentTypes.OUTPUT_JSON_CONTENT_TYPE);
  }

  private FormDataItem mockFormDataItem(String fieldName, String contentType, byte content[],
      boolean isFormField) throws IOException {
    InputStream in = new ByteArrayInputStream(content);

    FormDataItem formDataItem = mock(FormDataItem.class);
    when(formDataItem.getContentType()).thenReturn(contentType);
    when(formDataItem.getSize()).thenReturn((long) content.length);
    when(formDataItem.get()).thenReturn(content);
    when(formDataItem.getAsString()).thenReturn(new String(content));
    when(formDataItem.getFieldName()).thenReturn(fieldName);
    when(formDataItem.isFormField()).thenReturn(isFormField);
    when(formDataItem.getInputStream()).thenReturn(in);
    return formDataItem;
  }
}
