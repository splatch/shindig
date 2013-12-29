package org.apache.shindig.common;

import org.apache.shindig.api.json.JsonSerializer;
import org.apache.shindig.common.DefaultJsonSerializer;

import com.google.inject.AbstractModule;

public class DefaultJsonModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(JsonSerializer.class).to(DefaultJsonSerializer.class);
    }

}
