package org.apache.shindig.common.cache;

import org.apache.shindig.api.cache.CacheProvider;

import com.google.inject.AbstractModule;

public class DefaultCacheModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(CacheProvider.class).to(LruCacheProvider.class);
  }

}
