package io.smallrye.reactive.messaging.jms;

import javax.enterprise.context.ApplicationScoped;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import io.smallrye.reactive.messaging.json.JsonMapping;

@ApplicationScoped
public class TestMapping implements JsonMapping {

    private Jsonb jsonb = JsonbBuilder.create();

    @Override
    public String toJson(Object object) {
        return jsonb.toJson(object);
    }

    @Override
    public <T> T fromJson(String str, Class<T> type) {
        return jsonb.fromJson(str, type);
    }
}
