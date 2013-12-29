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
package org.apache.shindig.gadgets.oauth;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;

import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.common.xml.XmlUtil;
import org.apache.shindig.gadgets.AuthType;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.oauth.OAuthArguments.UseToken;
import org.apache.shindig.gadgets.spec.Preload;

import org.junit.Test;
import org.junit.Assert;

import javax.servlet.http.HttpServletRequest;

/**
 * Tests parameter parsing
 */
public class OAuthArgumentsTest {

  @Test
  public void testInitFromPreload() throws Exception {
    String xml = "<Preload href='http://www.example.com' " +
        "oauth_service_name='service' " +
        "OAUTH_TOKEN_NAME='token' " +
        "OAUTH_REQuest_token='requesttoken' " +
        "oauth_request_token_secret='tokensecret' " +
        "OAUTH_USE_TOKEN='never' " +
        "random='stuff'" +
        "/>";

    Preload preload = new Preload(XmlUtil.parse(xml), Uri.parse(""));
    OAuthArguments params = new OAuthArguments(preload);
    assertEquals("service", params.getServiceName());
    assertEquals("token", params.getTokenName());
    assertEquals("requesttoken", params.getRequestToken());
    assertEquals("tokensecret", params.getRequestTokenSecret());
    assertEquals(UseToken.NEVER, params.getUseToken());
    assertNull(params.getOrigClientState());
    assertFalse(params.getBypassSpecCache());
    assertEquals("stuff", params.getRequestOption("random"));
  }

  private HttpServletRequest makeDummyRequest() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getParameter("OAUTH_USE_TOKEN")).thenReturn("never");
    when(req.getParameter("OAUTH_SERVICE_NAME")).thenReturn("service");
    when(req.getParameter("OAUTH_TOKEN_NAME")).thenReturn("token");
    when(req.getParameter("OAUTH_REQUEST_TOKEN")).thenReturn("reqtoken");
    when(req.getParameter("OAUTH_REQUEST_TOKEN_SECRET")).thenReturn("secret");
    when(req.getParameter("oauthState")).thenReturn("state");
    when(req.getParameter("bypassSpecCache")).thenReturn("1");
    when(req.getParameter("signOwner")).thenReturn("false");
    when(req.getParameter("signViewer")).thenReturn("false");
    when(req.getParameter("random")).thenReturn("stuff");
    when(req.getParameterNames()).thenReturn(Collections.enumeration(Arrays.asList(
      "OAUTH_USE_TOKEN","OAUTH_SERVICE_NAME","OAUTH_TOKEN_NAME","OAUTH_REQUEST_TOKEN",
      "OAUTH_REQUEST_TOKEN_SECRET","oauthState","bypassSpecCache","signOwner","signViewer","random")));
    return req;
  }

  @Test
  public void testInitFromRequest() throws Exception {
    HttpServletRequest req = makeDummyRequest();

    OAuthArguments args = new OAuthArguments(AuthType.SIGNED, req);
    assertEquals(UseToken.NEVER, args.getUseToken());
    assertEquals("service", args.getServiceName());
    assertEquals("token", args.getTokenName());
    assertEquals("reqtoken", args.getRequestToken());
    assertEquals("secret", args.getRequestTokenSecret());
    assertEquals("state", args.getOrigClientState());
    assertTrue(args.getBypassSpecCache());
    assertFalse(args.getSignOwner());
    assertFalse(args.getSignViewer());
    assertEquals("stuff", args.getRequestOption("random"));
    assertEquals("stuff", args.getRequestOption("rAnDoM"));
  }

  @Test
  public void testInitFromRequest_defaults() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    OAuthArguments args = new OAuthArguments(AuthType.SIGNED, req);
    assertEquals(UseToken.NEVER, args.getUseToken());
    assertEquals("", args.getServiceName());
    assertEquals("", args.getTokenName());
    assertNull(args.getRequestToken());
    assertNull(args.getRequestTokenSecret());
    assertNull(args.getOrigClientState());
    assertFalse(args.getBypassSpecCache());
    assertTrue(args.getSignOwner());
    assertTrue(args.getSignViewer());
    assertNull(args.getRequestOption("random"));
  }

  @Test
  public void testInitFromRequest_oauthDefaults() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    OAuthArguments args = new OAuthArguments(AuthType.OAUTH, req);
    assertEquals(UseToken.ALWAYS, args.getUseToken());
  }

  @Test
  public void testNoArgConstructorDefaults() throws Exception {
    OAuthArguments args = new OAuthArguments();
    assertEquals(UseToken.ALWAYS, args.getUseToken());
    assertEquals("", args.getServiceName());
    assertEquals("", args.getTokenName());
    Assert.assertNull(args.getRequestToken());
    Assert.assertNull(args.getRequestTokenSecret());
    Assert.assertNull(args.getOrigClientState());
    Assert.assertFalse(args.getBypassSpecCache());
    Assert.assertFalse(args.getSignOwner());
    Assert.assertFalse(args.getSignViewer());
  }

  @Test
  public void testGetAndSet() throws Exception {
    OAuthArguments args = new OAuthArguments();
    args.setBypassSpecCache(true);
    Assert.assertTrue(args.getBypassSpecCache());

    args.setOrigClientState("thestate");
    assertEquals("thestate", args.getOrigClientState());

    args.setRequestToken("rt");
    assertEquals("rt", args.getRequestToken());

    args.setRequestTokenSecret("rts");
    assertEquals("rts", args.getRequestTokenSecret());

    args.setServiceName("s");
    assertEquals("s", args.getServiceName());

    args.setSignOwner(true);
    Assert.assertTrue(args.getSignOwner());

    args.setSignViewer(true);
    Assert.assertTrue(args.getSignViewer());

    args.setUseToken(UseToken.IF_AVAILABLE);
    assertEquals(UseToken.IF_AVAILABLE, args.getUseToken());

    args.setRequestOption("foo", "bar");
    assertEquals("bar", args.getRequestOption("foo"));
    args.removeRequestOption("foo");
    assertNull(args.getRequestOption("foo"));
  }

  @Test
  public void testCopyConstructor() throws Exception {
    HttpServletRequest req = makeDummyRequest();
    OAuthArguments args = new OAuthArguments(AuthType.OAUTH, req);
    args = new OAuthArguments(args);
    assertEquals(UseToken.NEVER, args.getUseToken());
    assertEquals("service", args.getServiceName());
    assertEquals("token", args.getTokenName());
    assertEquals("reqtoken", args.getRequestToken());
    assertEquals("secret", args.getRequestTokenSecret());
    assertEquals("state", args.getOrigClientState());
    Assert.assertTrue(args.getBypassSpecCache());
    Assert.assertFalse(args.getSignOwner());
    Assert.assertFalse(args.getSignViewer());
  }

  @Test
  public void testCopyConstructor_options() throws Exception {
    HttpServletRequest req = makeDummyRequest();
    OAuthArguments args = new OAuthArguments(AuthType.OAUTH, req);
    args = new OAuthArguments(args);

    args.setRequestOption("foo", "bar");
    args.setRequestOption("quux", "baz");
    assertEquals("bar", args.getRequestOption("foo"));
    assertEquals("baz", args.getRequestOption("quux"));
  }

  @Test
  public void testParseUseToken() throws Exception {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getParameter("OAUTH_USE_TOKEN")).thenReturn("ALWAYS");
    OAuthArguments args = new OAuthArguments(AuthType.SIGNED, req);
    assertEquals(UseToken.ALWAYS, args.getUseToken());

    when(req.getParameter("OAUTH_USE_TOKEN")).thenReturn("if_available");
    args = new OAuthArguments(AuthType.SIGNED, req);
    assertEquals(UseToken.IF_AVAILABLE, args.getUseToken());

    when(req.getParameter("OAUTH_USE_TOKEN")).thenReturn("never");
    args = new OAuthArguments(AuthType.SIGNED, req);
    assertEquals(UseToken.NEVER, args.getUseToken());

    when(req.getParameter("OAUTH_USE_TOKEN")).thenReturn("");
    args = new OAuthArguments(AuthType.SIGNED, req);
    assertEquals(UseToken.NEVER, args.getUseToken());

    when(req.getParameter("OAUTH_USE_TOKEN")).thenReturn("");
    args = new OAuthArguments(AuthType.OAUTH, req);
    assertEquals(UseToken.ALWAYS, args.getUseToken());

    try {
      when(req.getParameter("OAUTH_USE_TOKEN")).thenReturn("stuff");
      new OAuthArguments(AuthType.OAUTH, req);
      fail("Should have thrown");
    } catch (GadgetException e) {
      // good.
    }
  }
}
