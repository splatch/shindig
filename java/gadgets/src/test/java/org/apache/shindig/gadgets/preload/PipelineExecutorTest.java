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
package org.apache.shindig.gadgets.preload;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.apache.shindig.common.DefaultJsonSerializer;
import org.apache.shindig.common.JsonAssert;
import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.common.xml.XmlUtil;
import org.apache.shindig.expressions.Expressions;
import org.apache.shindig.gadgets.GadgetContext;
import org.apache.shindig.gadgets.spec.PipelinedData;
import org.apache.shindig.gadgets.spec.PipelinedData.Batch;
import org.apache.shindig.gadgets.spec.PipelinedData.BatchItem;
import org.apache.shindig.gadgets.spec.PipelinedData.BatchType;
import org.apache.shindig.gadgets.spec.RequestAuthenticationInfo;
import org.apache.shindig.gadgets.spec.SpecParserException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

@RunWith(MockitoJUnitRunner.class)
public class PipelineExecutorTest extends JsonAssert {

  @Mock
  private PipelinedDataPreloader preloader;
  private PreloaderService preloaderService;
  private GadgetContext context;
  private PipelineExecutor executor;

  private static final Uri GADGET_URI = Uri.parse("http://example.org/gadget.php");

  private static final String CONTENT =
    "<Content xmlns:os=\"http://ns.opensocial.org/2008/markup\">"
      + "  <os:PeopleRequest key=\"me\" userId=\"canonical\"/>"
      + "  <os:HttpRequest key=\"json\" href=\"test.json\"/>"
      + "</Content>";

  // Two requests, one depends on the other
  private static final String TWO_BATCH_CONTENT =
    "<Content xmlns:os=\"http://ns.opensocial.org/2008/markup\">"
    + "  <os:PeopleRequest key=\"me\" userId=\"${json.user}\"/>"
    + "  <os:HttpRequest key=\"json\" href=\"${ViewParams.file}\"/>"
    + "</Content>";

  // One request, but it requires data that isn\"t present
  private static final String BLOCKED_FIRST_BATCH_CONTENT =
    "<Content xmlns:os=\"http://ns.opensocial.org/2008/markup\">"
    + "  <os:PeopleRequest key=\"me\" userId=\"${json.user}\"/>"
    + "</Content>";

  public PipelineExecutorTest() {
    super(new DefaultJsonSerializer());
  }

  @Before
  public void setUp() throws Exception {
    preloaderService = new ConcurrentPreloaderService(Executors.newSingleThreadExecutor(), null);
    executor = new PipelineExecutor(preloader, preloaderService, Expressions.forTesting());

    context = new GadgetContext(){};
  }

  private PipelinedData getPipelinedData(String pipelineXml) throws SpecParserException {
    Element element = XmlUtil.parseSilent(pipelineXml);
    return new PipelinedData(element, GADGET_URI);
  }

  @Test
  public void execute() throws Exception {
    PipelinedData pipeline = getPipelinedData(CONTENT);

    ArgumentCaptor<Batch> batchCapture = ArgumentCaptor.forClass(Batch.class);

    JSONObject expectedData = new JSONObject("{result: {foo: 'bar'}}");

    // Dummy return results (the "real" return would have two values)
    Callable<PreloadedData> callable = createPreloadTask("key", expectedData.toString());

    // One batch with 1 each HTTP and Social preload
    when(preloader.createPreloadTasks(same(context), batchCapture.capture()))
            .thenReturn(ImmutableList.of(callable));

    PipelineExecutor.Results results = executor.execute(context,
        ImmutableList.of(pipeline));

    // Verify the data set is injected, and the os-data was deleted
    Batch value = batchCapture.getValue();
    assertThat(value, new BatchMatcher(1, 1));

    assertTrue(value.getPreloads().containsKey("me"));
    assertTrue(batchCapture.getValue().getPreloads().containsKey("json"));

    assertJsonEquals("[{id: 'key', result: {foo: 'bar'}}]",
        new DefaultJsonSerializer().serialize(results.results));
    assertJsonEquals("{foo: 'bar'}",
        new DefaultJsonSerializer().serialize(results.keyedResults.get("key")));
    assertTrue(results.remainingPipelines.isEmpty());
  }

  @Test
  public void executeWithTwoBatches() throws Exception {
    PipelinedData pipeline = getPipelinedData(TWO_BATCH_CONTENT);

    context = new GadgetContext() {
      @Override
      public String getParameter(String property) {
        // Provide the filename to be requested in the first batch
        if ("view-params".equals(property)) {
          return "{'file': 'test.json'}";
        }
        return null;
      }
    };

    // First batch, the HTTP fetch
    ArgumentCaptor<Batch> firstBatch = ArgumentCaptor.forClass(Batch.class);
    Callable<PreloadedData> firstTask = createPreloadTask("json",
        "{result: {user: 'canonical'}}");

    // Second batch, the user fetch
    ArgumentCaptor<Batch> secondBatch = ArgumentCaptor.forClass(Batch.class);
    Callable<PreloadedData> secondTask = createPreloadTask("me",
        "{result: {'id':'canonical'}}");

    // First, a batch with an HTTP request
    when(preloader.createPreloadTasks(same(context), firstBatch.capture()))
        .thenReturn(ImmutableList.of(firstTask));
    // Second, a batch with a social request
    when(preloader.createPreloadTasks(same(context), secondBatch.capture()))
        .thenReturn(ImmutableList.of(secondTask));


    PipelineExecutor.Results results = executor.execute(context,
        ImmutableList.of(pipeline));

    assertJsonEquals("[{id: 'json', result: {user: 'canonical'}}," +
        "{id: 'me', result: {id: 'canonical'}}]",
        new DefaultJsonSerializer().serialize(results.results));
    assertEquals(ImmutableSet.of("json", "me"), results.keyedResults.keySet());
    assertTrue(results.remainingPipelines.isEmpty());

    // Verify the data set is injected, and the os-data was deleted

    Batch firstValue = firstBatch.getValue();
    assertThat(firstValue, new BatchMatcher(0, 1));
    // Check the evaluated HTTP request
    RequestAuthenticationInfo request = (RequestAuthenticationInfo)
        firstValue.getPreloads().get("json").getData();
    assertEquals("http://example.org/test.json", request.getHref().toString());

    // Check the evaluated person request
    Batch secondValue = secondBatch.getValue();
    JSONObject personRequest = (JSONObject) secondValue.getPreloads().get("me").getData();
    assertEquals("canonical", personRequest.getJSONObject("params").getJSONArray("userId").get(0));
    assertThat(secondValue, new BatchMatcher(1, 0));
  }

  @Test
  public void executeWithBlockedBatch() throws Exception {
    PipelinedData pipeline = getPipelinedData(BLOCKED_FIRST_BATCH_CONTENT);

    // Expect a batch with no content
    when(preloader.createPreloadTasks(same(context), argThat(new BatchMatcher(0, 0))))
      .thenReturn(ImmutableList.<Callable<PreloadedData>>of());


    PipelineExecutor.Results results = executor.execute(context,
        ImmutableList.of(pipeline));
    assertEquals(0, results.results.size());
    assertTrue(results.keyedResults.isEmpty());
    assertEquals(1, results.remainingPipelines.size());
    assertSame(pipeline, results.remainingPipelines.iterator().next());
  }

  @Test
  public void executeError() throws Exception {
    PipelinedData pipeline = getPipelinedData(CONTENT);

    ArgumentCaptor<Batch> batchCapture = ArgumentCaptor.forClass(Batch.class);

    JSONObject expectedData = new JSONObject("{error: {message: 'NO!', code: 500}}");

    // Dummy return results (the "real" return would have two values)
    Callable<PreloadedData> callable = createPreloadTask("key", expectedData.toString());

    // One batch with 1 each HTTP and Social preload
    when(preloader.createPreloadTasks(same(context), batchCapture.capture()))
      .thenReturn(ImmutableList.of(callable));

    PipelineExecutor.Results results = executor.execute(context,
        ImmutableList.of(pipeline));

    // Verify the data set is injected, and the os-data was deleted
    Batch batch = batchCapture.getValue();
    assertThat(batch, new BatchMatcher(1, 1));
    assertTrue(batch.getPreloads().containsKey("me"));
    assertTrue(batchCapture.getValue().getPreloads().containsKey("json"));

    assertJsonEquals("[{id: 'key', error: {message: 'NO!', code: 500}}]",
        new DefaultJsonSerializer().serialize(results.results));
    assertJsonEquals("{message: 'NO!', code: 500}",
        new DefaultJsonSerializer().serialize(results.keyedResults.get("key")));
    assertTrue(results.remainingPipelines.isEmpty());
  }

  @Test
  public void executePreloadException() throws Exception {
    PipelinedData pipeline = getPipelinedData(CONTENT);
    final PreloadedData willThrow = mock(PreloadedData.class);

    Callable<PreloadedData> callable = new Callable<PreloadedData>() {
      public PreloadedData call() throws Exception {
        return willThrow;
      }
    };

    // One batch
    when(preloader.createPreloadTasks(same(context),
        isA(Batch.class))).thenReturn(ImmutableList.of(callable));
    // And PreloadedData that throws an exception
    when(willThrow.toJson()).thenThrow(new PreloadException("Failed"));

    PipelineExecutor.Results results = executor.execute(context,
        ImmutableList.of(pipeline));

    // The exception is fully handled, and leads to empty results
    assertEquals(0, results.results.size());
    assertTrue(results.keyedResults.isEmpty());
    assertTrue(results.remainingPipelines.isEmpty());
  }

  private static class BatchMatcher extends ArgumentMatcher<Batch> {
    private final int socialCount;
    private final int httpCount;

    public BatchMatcher(int socialCount, int httpCount) {
      this.socialCount = socialCount;
      this.httpCount = httpCount;
    }

    public boolean matches(Object obj) {
      if (!(obj instanceof Batch)) {
        return false;
      }

      Batch batch = (Batch) obj;
      int actualSocialCount = 0;
      int actualHttpCount = 0;
      for (BatchItem item : batch.getPreloads().values()) {
        if (item.getType() == BatchType.HTTP) {
          actualHttpCount++;
        } else if (item.getType() == BatchType.SOCIAL) {
          actualSocialCount++;
        }
      }

      return socialCount == actualSocialCount && httpCount == actualHttpCount;
    }

  }
  /** Create a mock Callable for a single preload task */
  private Callable<PreloadedData> createPreloadTask(final String key, String jsonResult)
      throws JSONException {
    final JSONObject value = new JSONObject(jsonResult);
    value.put("id", key);
    final PreloadedData preloadResult = new PreloadedData() {
      public Collection<Object> toJson() throws PreloadException {
        return ImmutableList.<Object>of(value);
      }
    };

    Callable<PreloadedData> callable = new Callable<PreloadedData>() {
      public PreloadedData call() throws Exception {
        return preloadResult;
      }
    };
    return callable;
  }
}
