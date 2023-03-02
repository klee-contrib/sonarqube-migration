package io.github.kbuntrock.http;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.http.HttpResponse;
import java.util.function.Supplier;

/**
 * @author Kévin Buntrock
 */
public class JsonBodyHandler<T> implements HttpResponse.BodyHandler<Supplier<T>> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Class<T> targetType;

    public JsonBodyHandler(Class<T> targetType) {
        this.targetType = targetType;
    }


    public <T> HttpResponse.BodySubscriber<Supplier<T>> asJSON(Class<T> target) {
        HttpResponse.BodySubscriber<InputStream> upstream = HttpResponse.BodySubscribers.ofInputStream();
        HttpResponse.BodySubscriber<Supplier<T>> downstream =
                HttpResponse.BodySubscribers.mapping(upstream, (InputStream is) -> () -> {
                    try (InputStream stream = is) {
                        return OBJECT_MAPPER.readValue(stream, target);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
        return downstream;
    }

    @Override
    public HttpResponse.BodySubscriber<Supplier<T>> apply(HttpResponse.ResponseInfo responseInfo) {
        return asJSON(targetType);
    }

    public static <W> HttpResponse.BodyHandler<Supplier<W>> forType(Class<W> target) {
        return new JsonBodyHandler<>(target);
    }
}
