package edu.usc.csci201.group12.smartpantry.json;

import java.util.Objects;

/**
 * Standard envelope for JSON responses consumed by fetch/AJAX clients.
 * Teammates should reuse this shape for new endpoints: {@code success}, optional {@code data}, optional {@code error}.
 */
public final class JsonApiResponse {
    private final boolean success;
    private final Object data;
    private final String error;

    private JsonApiResponse(boolean success, Object data, String error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public static JsonApiResponse ok(Object data) {
        return new JsonApiResponse(true, data, null);
    }

    public static JsonApiResponse ok() {
        return new JsonApiResponse(true, null, null);
    }

    public static JsonApiResponse fail(String message) {
        return new JsonApiResponse(false, null, Objects.requireNonNull(message, "message"));
    }

    public boolean isSuccess() {
        return success;
    }

    public Object getData() {
        return data;
    }

    public String getError() {
        return error;
    }
}
