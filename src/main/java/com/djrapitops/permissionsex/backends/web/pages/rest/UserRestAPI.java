package com.djrapitops.permissionsex.backends.web.pages.rest;

import com.djrapitops.permissionsex.backends.json.UserJSONService;
import com.djrapitops.permissionsex.backends.web.http.Request;
import com.djrapitops.permissionsex.backends.web.http.Response;
import com.djrapitops.permissionsex.backends.web.http.responses.JsonErrorResponse;
import com.djrapitops.permissionsex.backends.web.http.responses.JsonResponse;
import com.djrapitops.permissionsex.backends.web.pages.PageHandler;
import com.djrapitops.permissionsex.backends.web.pages.RestAPIHandler;
import com.djrapitops.permissionsex.exceptions.ParseException;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;

import java.util.List;

/**
 * RestAPI endpoint for /api/users.
 *
 * @author Rsl1122
 */
public class UserRestAPI extends RestAPIHandler {

	private final UserJSONService userJSONService;

	public UserRestAPI(UserJSONService userJSONService) {
		this.userJSONService = userJSONService;

		registerAPIEndPoints();
	}

	private void registerAPIEndPoints() {
		registerPage("", new PageHandler() {
			@Override
			public Response getResponse(Request request, List<String> target) {
				String requestMethod = request.getRequestMethod();
				if ("GET".equals(requestMethod)) {
					// GET /api/users/ - provides all users as an array
					return new JsonResponse(userJSONService.getAllUsers(), 200);
				}
				if ("PUT".equals(requestMethod)) {
					// PUT /api/users/ - updates users when "Save Changes" is pressed
					try {
						userJSONService.updateUsers((JsonArray) UserRestAPI.this.parseJSONFromString(request.getRequestBodyString()));
					} catch (ClassCastException e) {
						return new JsonErrorResponse("Sent JSON was not an Array", 400);
					} catch (JsonSyntaxException | ParseException e) {
						return e.getCause() == null ?
								new JsonErrorResponse(e.getMessage(), 500) :
								new JsonErrorResponse(e.getMessage() + " " + e.getCause().toString(), 500);
					}
					return new JsonResponse("", 200);
				}
				return null;
			}
		});
	}

	@Override
	public Response getResponse(Request request, List<String> target) {
		Response errorResponse = checkAuthValidity(request);
		if (errorResponse != null) {
			return errorResponse;
		}

		PageHandler pageHandler = getPageHandler(target);
		if (pageHandler != null) {
			Response response = pageHandler.getResponse(request, target);
			if (response != null) {
				return response;
			}
		}

		if ("GET".equals(request.getRequestMethod())) {
			// GET /api/users/:name - provides a user
			try {
				String name = target.get(0).replace("%20", " ");
				return new JsonResponse(userJSONService.getUser(name));
			} catch (IllegalArgumentException e) {
				return new JsonErrorResponse("Invalid Name: " + e.getMessage(), 400);
			}
		}

		return new JsonErrorResponse("API endpoint not found", 404);
	}
}