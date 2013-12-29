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

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;

import org.apache.shindig.api.auth.SecurityToken;
import org.apache.shindig.auth.AuthInfoUtil;
import org.apache.shindig.config.ContainerConfig;
import org.apache.shindig.protocol.conversion.BeanConverter;
import org.apache.shindig.protocol.conversion.BeanJsonConverter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

@RunWith(MockitoJUnitRunner.class)
public class DataServiceServletTest {

  @Mock
  private SecurityToken FAKE_GADGET_TOKEN;

  @Mock
  private HttpServletRequest req;

  @Mock
  private HttpServletResponse res;

  @Mock
  private BeanJsonConverter jsonConverter;

  @Mock
  private BeanConverter xmlConverter;

  @Mock
  private BeanConverter atomConverter;

  @Mock
  private ContainerConfig containerConfig;

  private DataServiceServlet servlet;

  @Before
  public void setUp() throws Exception {
    when(FAKE_GADGET_TOKEN.getOwnerId()).thenReturn("john.doe");
    when(FAKE_GADGET_TOKEN.getViewerId()).thenReturn("john.doe");

    servlet = new DataServiceServlet();

    when(jsonConverter.getContentType()).thenReturn(
        ContentTypes.OUTPUT_JSON_CONTENT_TYPE);
    when(xmlConverter.getContentType()).thenReturn(
        ContentTypes.OUTPUT_XML_CONTENT_TYPE);
    when(atomConverter.getContentType()).thenReturn(
        ContentTypes.OUTPUT_ATOM_CONTENT_TYPE);

    HandlerRegistry registry = new DefaultHandlerRegistry(null, jsonConverter,
        new HandlerExecutionListener.NoOpHandler());
    registry.addHandlers(Sets.<Object>newHashSet(new TestHandler()));

    servlet.setHandlerRegistry(registry);
    servlet.setContainerConfig(containerConfig);
    servlet.setJSONPAllowed(true);

    servlet.setBeanConverters(jsonConverter, xmlConverter, atomConverter);
  }

  @Test
  public void testUriRecognition() throws Exception {
    verifyHandlerWasFoundForPathInfo("/test/5/@self");
  }

  private void verifyHandlerWasFoundForPathInfo(String peoplePathInfo)
      throws Exception {
    String post = "POST";
    verifyHandlerWasFoundForPathInfo(peoplePathInfo, post, post);
  }

  private void verifyHandlerWasFoundForPathInfo(String pathInfo,
    String actualMethod, String overrideMethod) throws Exception {
    setupRequest(pathInfo, actualMethod, overrideMethod);

    String method = Strings.isNullOrEmpty(overrideMethod) ? actualMethod : overrideMethod;

    String response = "{ 'entry' : " + TestHandler.REST_RESULTS.get(method) + " }";
    when(jsonConverter.convertToString(
        ImmutableMap.of("entry", TestHandler.REST_RESULTS.get(method))))
        .thenReturn(response);

    PrintWriter writerMock = mock(PrintWriter.class);
    when(res.getWriter()).thenReturn(writerMock);

    res.setCharacterEncoding("UTF-8");
    res.setContentType(ContentTypes.OUTPUT_JSON_CONTENT_TYPE);

    servlet.service(req, res);
    verify(writerMock).write(response);
  }

  @Test
  public void testDisallowJSONP() throws Exception {
    servlet.setJSONPAllowed(false);
    String route = "/test";
    verifyHandlerWasFoundForPathInfo(route, "POST", "GET");
    servlet.setJSONPAllowed(true);
  }

  @Test
  public void testOverridePostWithGet() throws Exception {
    String route = "/test";
    verifyHandlerWasFoundForPathInfo(route, "POST", "GET");
  }

  @Test
  public void  testOverrideGetWithPost() throws Exception {
    String route = "/test";
    verifyHandlerWasFoundForPathInfo(route, "GET", "POST");
  }

  /**
   * Tests a data handler that returns a failed Future
   */
  @Test
  public void testFailedRequest() throws Exception {
    String route = "/test";
    setupRequest(route, "DELETE", null);

    // Shouldnt these be expectations
    res.sendError(HttpServletResponse.SC_BAD_REQUEST, TestHandler.FAILURE_MESSAGE);
    res.setCharacterEncoding("UTF-8");
    res.setContentType(ContentTypes.OUTPUT_JSON_CONTENT_TYPE);

    servlet.service(req, res);
  }

  private void setupRequest(String pathInfo, String actualMethod, String overrideMethod)
      throws IOException {
    when(req.getRequestURL()).thenReturn(new StringBuffer("/social/rest"));
    when(req.getPathInfo()).thenReturn(pathInfo);
    when(req.getMethod()).thenReturn(actualMethod);
    when(req.getParameter(DataServiceServlet.X_HTTP_METHOD_OVERRIDE)).thenReturn(overrideMethod);
    when(req.getCharacterEncoding()).thenReturn("UTF-8");
    when(req.getAttribute(AuthInfoUtil.Attribute.SECURITY_TOKEN.getId())).thenReturn(FAKE_GADGET_TOKEN);
    when(req.getContentType()).thenReturn(ContentTypes.OUTPUT_JSON_CONTENT_TYPE);
  }

  @Test
  public void testGetConverterForFormat() throws Exception {
    assertConverterForFormat(atomConverter, "atom");
    assertConverterForFormat(xmlConverter, "xml");
    assertConverterForFormat(jsonConverter, "");
    assertConverterForFormat(jsonConverter, null);
    assertConverterForFormat(jsonConverter, "ahhhh!");
  }

  @Test
  public void testGetConverterForContentType() throws Exception {
    assertConverterForContentType(atomConverter, ContentTypes.OUTPUT_ATOM_CONTENT_TYPE);
    assertConverterForContentType(xmlConverter, ContentTypes.OUTPUT_XML_CONTENT_TYPE);
    assertConverterForContentType(xmlConverter, "text/xml");
    assertConverterForContentType(jsonConverter, ContentTypes.OUTPUT_JSON_CONTENT_TYPE);
    assertConverterForContentType(jsonConverter, "application/json");
    assertConverterForContentType(jsonConverter, "");
    assertConverterForContentType(jsonConverter, null);
    assertConverterForContentType(jsonConverter, "abcd!");
  }

  private void assertConverterForFormat(BeanConverter converter, String format) {
    assertEquals(converter, servlet.getConverterForFormat(format));
  }

  private void assertConverterForContentType(BeanConverter converter, String contentType) {
    assertEquals(converter, servlet.getConverterForContentType(contentType));
  }
}
