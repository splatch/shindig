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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.apache.shindig.api.auth.SecurityToken;
import org.apache.shindig.auth.AuthInfoUtil.Attribute;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;

@RunWith(MockitoJUnitRunner.class)
public class AuthInfoUtilTest {

  private static final String FAKE_AUTH = "FakeAuth";

@Mock
  private HttpServletRequest req;

  @Mock
  private SecurityToken token;

  @Test
  public void testToken() throws Exception {
    when(req.getAttribute(Attribute.SECURITY_TOKEN.getId())).thenReturn(token);

    assertEquals(token, AuthInfoUtil.getSecurityTokenFromRequest(req));
  }

  @Test
  public void testAuthType() throws Exception {
    when(req.getAttribute(Attribute.AUTH_TYPE.getId())).thenReturn(FAKE_AUTH);

    assertEquals(FAKE_AUTH, AuthInfoUtil.getAuthTypeFromRequest(req));
  }

  @Test
  public void testWrites() {
    AuthInfoUtil.setSecurityTokenForRequest(req, token);
    AuthInfoUtil.setAuthTypeForRequest(req, FAKE_AUTH);

    verify(req).setAttribute(Attribute.SECURITY_TOKEN.getId(), token);
    verify(req).setAttribute(Attribute.AUTH_TYPE.getId(), FAKE_AUTH);
  }

}
