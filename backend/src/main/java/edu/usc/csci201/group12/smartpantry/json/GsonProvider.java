package edu.usc.csci201.group12.smartpantry.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Single shared Gson instance for consistent date/number serialization across servlets.
 */
public final class GsonProvider {
    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    private GsonProvider() {
    }

    public static Gson get() {
        return GSON;
    }
}
