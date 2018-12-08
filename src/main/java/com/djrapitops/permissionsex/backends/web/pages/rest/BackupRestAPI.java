package com.djrapitops.permissionsex.backends.web.pages.rest;

import com.djrapitops.permissionsex.backends.json.BackupJSONService;
import com.djrapitops.permissionsex.backends.web.http.Request;
import com.djrapitops.permissionsex.backends.web.http.Response;
import com.djrapitops.permissionsex.backends.web.http.responses.JsonErrorResponse;
import com.djrapitops.permissionsex.backends.web.http.responses.JsonResponse;
import com.djrapitops.permissionsex.backends.web.pages.PageHandler;
import com.djrapitops.permissionsex.backends.web.pages.RestAPIHandler;
import com.djrapitops.permissionsex.exceptions.ParseException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.util.List;

/**
 * RestAPI endpoint for /api/backups.
 *
 * @author Rsl1122
 */
public class BackupRestAPI extends RestAPIHandler {

	private final BackupJSONService backupJSONService;

	public BackupRestAPI(BackupJSONService backupJSONService) {
		this.backupJSONService = backupJSONService;

		registerAPIEndPoints();
	}

	private void registerAPIEndPoints() {
		registerPage("clone", new CloneHandler(backupJSONService));
		registerPage("restore", new RestoreHandler(backupJSONService));
		registerPage("delete", new DeleteHandler(backupJSONService));
		registerPage("", new PageHandler() {
			@Override
			public Response getResponse(Request request, List<String> target) {
				String requestMethod = request.getRequestMethod();
				if ("GET".equals(requestMethod)) {
					// GET /api/backups/ - provides all backups as an array
					return new JsonResponse(backupJSONService.getBackupInformation(), 200);
				}
				if ("POST".equals(requestMethod)) {
					// POST /api/backups/ - Creates a new backup
					try {
						String name = BackupRestAPI.this.getBackupName(request);
						return new JsonResponse(backupJSONService.createBackup(name), 200);
					} catch (ClassCastException e) {
						return new JsonErrorResponse("Sent JSON was not an Object", 400);
					} catch (JsonSyntaxException | ParseException e) {
						return e.getCause() == null ?
								new JsonErrorResponse(e.getMessage(), 500) :
								new JsonErrorResponse(e.getMessage() + " " + e.getCause().toString(), 500);
					} catch (IllegalArgumentException e) {
						return new JsonResponse(e.getMessage(), 400);
					}
				}
				return null;
			}
		});
	}

	private String getBackupName(Request request) throws ParseException {
		JsonObject backupObj = (JsonObject) parseJSONFromString(request.getRequestBodyString());
		JsonElement nameObj = backupObj.get("name");
		if (nameObj.isJsonPrimitive()) {
			return nameObj.getAsString();
		}
		throw new ParseException("Invalid JSON format, name (String) required");
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

		return new JsonErrorResponse("API endpoint not found", 404);
	}
}

class CloneHandler implements PageHandler {

	private final BackupJSONService backupJSONService;

	public CloneHandler(BackupJSONService backupJSONService) {
		this.backupJSONService = backupJSONService;
	}

	@Override
	public Response getResponse(Request request, List<String> target) {
		if ("POST".equals(request.getRequestMethod())) {
			// POST /api/backups/clone/:name - requests a cloning of a backup
			try {
				String backupName = target.get(0).replace("%20", " ");
				return new JsonResponse(backupJSONService.duplicateBackup(backupName));
			} catch (IllegalArgumentException e) {
				return new JsonErrorResponse("Invalid Backup Name: " + e.getMessage(), 400);
			}
		}

		return new JsonErrorResponse("API endpoint not found", 404);
	}
}

class RestoreHandler implements PageHandler {

	private final BackupJSONService backupJSONService;

	public RestoreHandler(BackupJSONService backupJSONService) {
		this.backupJSONService = backupJSONService;
	}

	@Override
	public Response getResponse(Request request, List<String> target) {
		if ("POST".equals(request.getRequestMethod())) {
			// POST /api/backups/restore/:name - requests restoration of a backup
			try {
				String backupName = target.get(0).replace("%20", " ");
				backupJSONService.restoreBackup(backupName);
				return new JsonResponse("{\"success\":true}", 200);
			} catch (IllegalArgumentException e) {
				return new JsonErrorResponse("Invalid Backup Name: " + e.getMessage(), 400);
			}
		}

		return new JsonErrorResponse("API endpoint not found", 404);
	}
}

class DeleteHandler implements PageHandler {

	private final BackupJSONService backupJSONService;

	public DeleteHandler(BackupJSONService backupJSONService) {
		this.backupJSONService = backupJSONService;
	}

	@Override
	public Response getResponse(Request request, List<String> target) {
		if ("DELETE".equals(request.getRequestMethod())) {
			// DELETE /api/backups/delete/:name - requests a deleting backup
			try {
				String backupName = target.get(0).replace("%20", " ");
				backupJSONService.deleteBackup(backupName);
				return new JsonResponse("{\"success\":true}", 200);
			} catch (IllegalArgumentException e) {
				return new JsonResponse("Invalid Backup Name: " + e.getMessage(), 400);
			}
		}

		return new JsonErrorResponse("API endpoint not found", 404);
	}
}