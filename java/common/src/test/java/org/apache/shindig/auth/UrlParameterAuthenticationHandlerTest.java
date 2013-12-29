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
package org.apache.shindig.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.oauth.OAuth;

import org.apache.shindig.api.auth.AuthenticationMode;
import org.apache.shindig.api.auth.SecurityToken;
import org.apache.shindig.api.auth.SecurityTokenCodec;
import org.apache.shindig.api.auth.SecurityTokenException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UrlParameterAuthenticationHandlerTest {
  SecurityToken expectedToken;
  UrlParameterAuthenticationHandler authHandler;
  SecurityTokenCodec codec;
  @Mock
  HttpServletRequest req;

  @Before
  public void setup() throws Exception {
    expectedToken = new BasicSecurityToken(
        "owner", "viewer", "app",
        "domain", "appUrl", "0", "container", "activeUrl", 1000L);
    // Mock token codec
    codec = new SecurityTokenCodec() {
      public SecurityToken createToken(Map<String, String> tokenParameters) throws SecurityTokenException {
        return tokenParameters == null ? null :
               "1234".equals(tokenParameters.get(SecurityTokenCodec.SECURITY_TOKEN_NAME)) ? expectedToken : null;
      }

      public String encodeToken(SecurityToken token) throws SecurityTokenException {
        return null;
      }

      public int getTokenTimeToLive() {
        return 0; // Not used.
      }

      public int getTokenTimeToLive(String container) {
        return 0; // Not used.
      }
    };

    authHandler = new UrlParameterAuthenticationHandler(codec, true);
  }

  @Test
  public void testGetSecurityTokenFromRequest() throws Exception {
    assertEquals(authHandler.getName(), AuthenticationMode.SECURITY_TOKEN_URL_PARAMETER.name());
  }

  @Test
  public void testInvalidRequests() throws Exception {
    when(req.getRequestURL()).thenReturn(new StringBuffer("/"));
    assertNull(authHandler.getSecurityTokenFromRequest(req));

    // Old behavior, no longer supported
    when(req.getHeader("Authorization")).thenReturn("Token token=\"1234\"");
    assertNull(authHandler.getSecurityTokenFromRequest(req));

    when(req.getHeader("Authorization")).thenReturn("OAuth 1234");
    assertNull(authHandler.getSecurityTokenFromRequest(req));
  }

  @Test
  public void testSecurityToken() throws Exception {
    // security token in request
    when(req.getRequestURL()).thenReturn(new StringBuffer("http://example.org/rpc?st=1234"));
    when(req.getParameter(UrlParameterAuthenticationHandler.SECURITY_TOKEN_PARAM)).thenReturn("1234");
    assertEquals(expectedToken, authHandler.getSecurityTokenFromRequest(req));
  }

  @Test
  public void testOAuth1() throws Exception {
    // An OAuth 1.0 request, we should not process this.
    when(req.getRequestURL()).thenReturn(new StringBuffer("/"));
    when(req.getParameter("Authorization")).thenReturn("OAuth oauth_signature_method=\"RSA-SHA1\"");
    SecurityToken token = authHandler.getSecurityTokenFromRequest(req);
    assertNull(token);
  }

  @Test
  public void testOAuth2Header() throws Exception {
    when(req.getRequestURL()).thenReturn(new StringBuffer("https://www.example.org/"));
    when(req.isSecure()).thenReturn(true);
    when(req.getHeaders("Authorization")).thenReturn(Collections.enumeration(Arrays.asList("OAuth2  1234")));
    assertEquals(expectedToken, authHandler.getSecurityTokenFromRequest(req));

    when(req.getHeaders("Authorization")).thenReturn(Collections.enumeration(Arrays.asList("   OAuth2    1234 ")));
    assertEquals(expectedToken, authHandler.getSecurityTokenFromRequest(req));

    when(req.getHeaders("Authorization")).thenReturn(Collections.enumeration(Arrays.asList("OAuth2 1234 x=1,y=\"2 2 2\"")));
    assertEquals(expectedToken, authHandler.getSecurityTokenFromRequest(req));
  }

  @Test
  public void testBadOAuth2Header() throws Exception {
    when(req.getRequestURL()).thenReturn(new StringBuffer("http://www.example.org/"));
    when(req.getHeaders("Authorization")).thenReturn(Collections.enumeration(Arrays.asList("OAuth2 1234")));
    assertNull(authHandler.getSecurityTokenFromRequest(req));
  }

  @Test
  public void testOAuth2Param() throws Exception
  {
    when(req.getRequestURL()).thenReturn(new StringBuffer("https://www.example.com?oauth_token=1234"));
    when(req.isSecure()).thenReturn(true);
    when(req.getParameter("oauth_token")).thenReturn("1234");

    assertEquals(expectedToken, authHandler.getSecurityTokenFromRequest(req));

    when(req.getParameter(OAuth.OAUTH_SIGNATURE_METHOD)).thenReturn("RSA-SHA1");
    assertNull(authHandler.getSecurityTokenFromRequest(req));
  }
}
