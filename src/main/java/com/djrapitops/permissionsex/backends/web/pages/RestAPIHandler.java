package com.djrapitops.permissionsex.backends.web.pages;

import com.djrapitops.permissionsex.backends.web.http.Request;
import com.djrapitops.permissionsex.backends.web.http.Response;
import com.djrapitops.permissionsex.backends.web.http.auth.Authentication;
import com.djrapitops.permissionsex.backends.web.http.responses.JsonErrorResponse;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Abstract TreePageHandler for all RestAPI providers.
 * <p>
 * Contains utility methods.
 *
 * @author Rsl1122
 */
public abstract class RestAPIHandler extends TreePageHandler {

    protected Response checkAuthValidity(Request request) {
        if (!request.hasAuth()) {
            return new JsonErrorResponse("Authorization not provided", 401);
        }
        Authentication auth = request.getAuth();
        if (!auth.isValid()) {
            return new JsonErrorResponse("Expired user token, please log-in again.", 400);
        }
        return null;
    }

    protected String getStringFromRequestBody(Request request) throws IOException {
        InputStream requestBody = request.getRequestBody();
        return readInputStream(requestBody);
    }

    private String readInputStream(InputStream inputStream) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    builder.append(charBuffer, 0, bytesRead);
                }
            } else {
                builder.append("");
            }
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }

        return builder.toString();
    }

    /**
     * Parses JSON String into a JsonElement.
     * <p>
     * https://stackoverflow.com/a/15116323
     *
     * @param json String format of JSON.
     * @return parsed JsonElement
     */
    protected JsonElement parseJSONFromString(String json) {
        return new GsonBuilder().setPrettyPrinting().create().fromJson(json, JsonElement.class);
    }

}