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
package org.apache.shindig.social.core.config;

import static org.junit.Assert.*;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

import org.apache.shindig.auth.AuthenticationHandler;
import org.apache.shindig.common.PropertiesModule;
import org.apache.shindig.common.cache.DefaultCacheModule;
import org.apache.shindig.social.core.oauth.AuthenticationHandlerProvider;
import org.apache.shindig.social.core.oauth2.OAuth2Service;
import org.apache.shindig.social.opensocial.oauth.OAuthDataStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class SocialApiGuiceModuleTest {
  private Injector injector;

  @Mock
  private OAuthDataStore store;
  @Mock
  private OAuth2Service service;

  @Before
  public void setUp() throws Exception {
    injector = Guice.createInjector(new SocialApiGuiceModule(), new DefaultCacheModule(), new PropertiesModule(),
        new AbstractModule() {
          @Override
          protected void configure() {
            bind(OAuthDataStore.class).toInstance(store);
            bind(OAuth2Service.class).toInstance(service);
          }
    });
  }

  /**
   * Test default auth handler injection
   */
  @Test
  public void testAuthHandler() {
    injector.getInstance(AuthenticationHandlerProvider.class).get();

    AuthenticationHandlerProvider provider =
        injector.getInstance(AuthenticationHandlerProvider.class);
    assertEquals(4, provider.get().size());

    List<AuthenticationHandler> handlers = injector.getInstance(
        Key.get(new TypeLiteral<List<AuthenticationHandler>>(){}));

    assertEquals(4, handlers.size());
  }
}
