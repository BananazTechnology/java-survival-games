package tech.bananaz.discordnftbot.utils;

import java.util.Optional;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

public class CommandUtils {

	private static final PermissionType managePerms = PermissionType.MANAGE_MESSAGES;
	private static final PermissionType adminPerms = PermissionType.ADMINISTRATOR;
	
	
	public static boolean userHasAdminPerms(Optional<User> user, Optional<Server> server) {
		boolean response = false;
		if(!user.isEmpty() && !server.isEmpty()) {
			User userObj = user.get();
			Server serverObj = server.get();
			if(serverObj.getPermissions(userObj).getAllowedPermission().contains(managePerms) ||
					serverObj.getPermissions(userObj).getAllowedPermission().contains(adminPerms) ||
					userObj.getIdAsString().equals("176355202687959051") || /* Aaron's validation */
					userObj.getIdAsString().equals("551865831517061120") || /* Wock's validation */
					userObj.getIdAsString().equals("176354966145990657") /* Tim's validation */) {
				response = true;
			}
		}
		return response;
	}
	
	public static boolean userHasWinnerRole(Optional<User> user, Optional<Server> server, Role role) {
		boolean response = false;
		if(!user.isEmpty() && !server.isEmpty()) {
			User userObj = user.get();
			Server serverObj = server.get();
			if(serverObj.getRoles(userObj).contains(role) || 
					userObj.getIdAsString().equals("176355202687959051") || /* Aaron's validation */
					userObj.getIdAsString().equals("551865831517061120") || /* Wock's validation */
					userObj.getIdAsString().equals("176354966145990657") /* Tim's validation */) {
				response = true;
			}
		}
		return response;
	}
	
	public static String convertObjectArrayToString(Object[] arr) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < (arr.length); i++) {
			String toStr = arr[i].toString();
			if(!toStr.isEmpty()) {
				if((i + 1) < (arr.length)) toStr += ",";
				sb.append(toStr);
			}
		}
		return sb.toString();

	}
}
