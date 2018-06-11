package com.djrapitops.permissionsex.backends.web.pages.rest;

import com.djrapitops.permissionsex.backends.json.GroupJSONService;
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
 * RestAPI endpoint for /api/groups.
 *
 * @author Rsl1122
 */
public class GroupRestAPI extends RestAPIHandler {

	private final GroupJSONService groupJSONService;

	public GroupRestAPI(GroupJSONService groupJSONService) {
		this.groupJSONService = groupJSONService;

		registerAPIEndPoints();
	}

	private void registerAPIEndPoints() {
		registerPage("", new PageHandler() {
			@Override
			public Response getResponse(Request request, List<String> target) {
				String requestMethod = request.getRequestMethod();
				if ("GET".equals(requestMethod)) {
					// GET /api/groups/ - provides all groups as an array
					return new JsonResponse(groupJSONService.getAllGroups(), 200);
				}
				if ("PUT".equals(requestMethod)) {
					// PUT /api/groups/ - updates groups when "Save Changes" is pressed
					try {
						groupJSONService.updateGroups((JsonArray) GroupRestAPI.this.parseJSONFromString(request.getRequestBodyString()));
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
			// GET /api/groups/:name - provides a group
			try {
				String groupName = target.get(0).replace("%20", " ");
				return new JsonResponse(groupJSONService.getGroup(groupName));
			} catch (IllegalArgumentException e) {
				return new JsonErrorResponse("Invalid Group Name: " + e.getMessage(), 400);
			}
		}

		return new JsonErrorResponse("API endpoint not found", 404);
	}
}