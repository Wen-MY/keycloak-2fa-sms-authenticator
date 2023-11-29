package dasniko.keycloak.authenticator;

import lombok.experimental.UtilityClass;
import org.keycloak.models.RoleModel;

import javax.management.relation.Role;

@UtilityClass
public class SmsConstants {

	public static String CODE = "code";
	public static String CODE_LENGTH = "length";
	public static String CODE_TTL = "ttl";
	public static String SENDER_ID = "senderId";
	public static String SIMULATION_MODE = "simulation";
	public static String CODE_TTR = "ttr";
	public static String API_URL = "apiUrl";
	public static String REQUIRED_FOR = "requiredFor";
	public static final String RECEIVER_ATTRIBUTE = "destinationAddress";
	public static final String SENDER_ATTRIBUTE = "sourceAddress";
	public static final String MSG_ATTRIBUTE = "message";
}
