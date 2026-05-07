package edu.usc.csci201.group12.smartpantry.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Single shared Gson instance for consistent date/number serialization across servlets.
 */
public final class GsonProvider {
    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (src, type, ctx) ->
                            new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .registerTypeAdapter(LocalDate.class,
                    (JsonSerializer<LocalDate>) (src, type, ctx) ->
                            new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE)))
            .create();

    private GsonProvider() {
    }

    public static Gson get() {
        return GSON;
    }
}
