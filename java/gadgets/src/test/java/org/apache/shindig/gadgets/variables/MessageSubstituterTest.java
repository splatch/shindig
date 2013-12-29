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
package org.apache.shindig.gadgets.variables;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.apache.shindig.gadgets.variables.Substitutions.Type;

import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.gadgets.GadgetContext;
import org.apache.shindig.gadgets.MessageBundleFactory;
import org.apache.shindig.gadgets.spec.GadgetSpec;
import org.apache.shindig.gadgets.spec.MessageBundle;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class MessageSubstituterTest {

  private static final String FOO_PROP = "foo";
  private static final String FOO_VALUE = "bar";
  private static final String BAR_PROP = "bar";
  private static final String BAR_VALUE = "baz";

  private final GadgetContext context = new GadgetContext();

  @Mock
  private MessageBundleFactory messageBundleFactory;

  @Mock
  private MessageBundle bundle;

  @Test
  public void testMessageReplacements() throws Exception {
    String xml =
        "<Module>" +
        " <ModulePrefs title=''>" +
        "  <Locale>" +
        "    <msg name='foo'>bar</msg>" +
        "    <msg name='bar'>baz</msg>" +
        "  </Locale>" +
        " </ModulePrefs>" +
        " <Content />" +
        "</Module>";

    Substitutions substitutions = new Substitutions();
    GadgetSpec spec = new GadgetSpec(Uri.parse("#"), xml);
    when(messageBundleFactory.getBundle(spec, context.getLocale(), context.getIgnoreCache(), context.getContainer(), context.getView())).thenReturn(bundle);
    when(bundle.getMessages()).thenReturn(new ImmutableMap.Builder<String, String>()
      .put(FOO_PROP, FOO_VALUE)
      .put(BAR_PROP, BAR_VALUE)
      .build()
    );

    MessageSubstituter substituter = new MessageSubstituter(messageBundleFactory);
    substituter.addSubstitutions(substitutions, context, spec);


    assertEquals(FOO_VALUE, substitutions.getSubstitution(Type.MESSAGE, FOO_PROP));
    assertEquals(BAR_VALUE, substitutions.getSubstitution(Type.MESSAGE, BAR_PROP));
  }
}
