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

import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableList;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.ServletException;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationServletFilterTest {
  private static final String TEST_AUTH_HEADER = "Test Authentication Header";

  private AuthenticationServletFilter filter;

  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain chain;
  @Mock
  private AuthenticationHandler handler;

  @Before
  public void setup() {
    filter = new AuthenticationServletFilter();
  }

  @Test(expected = ServletException.class)
  public void testDoFilter_BadArgs() throws Exception {
    filter.doFilter(null, null, null);
  }

  @Test
  public void testNullSecurityToken() throws Exception {
    when(handler.getWWWAuthenticateHeader(anyString())).thenReturn(TEST_AUTH_HEADER);

    filter.setAuthenticationHandlers(ImmutableList.<AuthenticationHandler>of(handler));
    filter.doFilter(request, response, chain);

    verify(response).addHeader(AuthenticationServletFilter.WWW_AUTHENTICATE_HEADER, TEST_AUTH_HEADER);
  }

}
