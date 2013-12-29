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
package org.apache.shindig.gadgets.http;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.apache.shindig.api.auth.AuthenticationMode;
import org.apache.shindig.api.auth.SecurityToken;
import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.config.ContainerConfig;
import org.apache.shindig.protocol.DefaultHandlerRegistry;
import org.apache.shindig.protocol.HandlerExecutionListener;
import org.apache.shindig.protocol.HandlerRegistry;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RestHandler;
import org.apache.shindig.protocol.conversion.BeanJsonConverter;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Basic test of invalidation handler
 */
@RunWith(MockitoJUnitRunner.class)
public class InvalidationHandlerTest {

  @Mock
  private BeanJsonConverter converter;

  @Mock
  private InvalidationService invalidationService;

  @Mock
  private SecurityToken token;

  private InvalidationHandler handler;
  private Map<String, String[]> params;

  protected HandlerRegistry registry;
  protected ContainerConfig containerConfig;

  @Before
  public void setUp() throws Exception {
    when(token.getAppId()).thenReturn("appId");
    when(token.getViewerId()).thenReturn("userX");


    handler = new InvalidationHandler(invalidationService);
    registry = new DefaultHandlerRegistry(null, converter,
        new HandlerExecutionListener.NoOpHandler());
    registry.addHandlers(Sets.<Object>newHashSet(handler));

    params = Maps.newHashMap();
  }

  @Test
  public void testHandleSimpleGetInvalidateViewer() throws Exception {
    String path = "/cache/invalidate";
    RestHandler operation = registry.getRestHandler(path, "GET");

    operation.execute(params, null, token, converter).get();

    verify(invalidationService).invalidateUserResources(
        eq(ImmutableSet.of("userX")),
        eq(token));
  }

  @Test
  public void testAllowConsumerAuthInvalidateAppResource() throws Exception {
    String path = "/cache/invalidate";
    RestHandler operation = registry.getRestHandler(path, "POST");
    params.put(InvalidationHandler.KEYS_PARAM, new String[]{"http://www.example.org/gadget.xml"});
    token.setAuthenticationMode(AuthenticationMode.OAUTH_CONSUMER_REQUEST);

    operation.execute(params, null, token, converter).get();

    verify(invalidationService).invalidateApplicationResources(
        eq(ImmutableSet.of(Uri.parse("http://www.example.org/gadget.xml"))),
        eq(token));
  }

  @Test
  public void testFailTokenAuthInvalidateAppResource() throws Exception {
    String path = "/cache/invalidate";
    RestHandler operation = registry.getRestHandler(path, "POST");
    params.put(InvalidationHandler.KEYS_PARAM, new String[]{"http://www.example.org/gadget.xml"});

    try {
      operation.execute(params, null, token, converter).get();
      fail("Expected error");
    } catch (ExecutionException ee) {
      assertTrue(ee.getCause() instanceof ProtocolException);
    }
  }

  @Test
  public void testFailInvalidateNoApp() throws Exception {
    String path = "/cache/invalidate";
    RestHandler operation = registry.getRestHandler(path, "POST");
    params.put(InvalidationHandler.KEYS_PARAM, new String[]{"http://www.example.org/gadget.xml"});

    try {
      token.setAppId("");
      token.setAppUrl("");
      operation.execute(params, null, token, converter).get();
      fail("Expected error");
    } catch (ExecutionException ee) {
      assertTrue(ee.getCause() instanceof ProtocolException);
    }
  }
}
