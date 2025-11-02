package ru.practicum.util;

import com.google.gson.*;

import java.time.Duration;
import java.time.LocalDateTime;

public class GsonUtil {
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Duration.class, (JsonSerializer<Duration>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(src.toMinutes()))
            .registerTypeAdapter(Duration.class, (JsonDeserializer<Duration>) (json, typeOfT, context) ->
                    Duration.ofMinutes(json.getAsLong()))
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
}

