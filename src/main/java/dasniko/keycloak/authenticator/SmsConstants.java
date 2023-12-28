package dasniko.keycloak.authenticator;

import lombok.experimental.UtilityClass;

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
	public static final String LAST_GENERATION_TIME = "lastGenerationTime";
	public static final String AUTH_CODE_SUBMIT_ATTEMPT = "submitAttempt";
	public static final String REGENERATE_ATTEMPT = "regenerateAttempt";
	public static final String CURRENT_SUBMIT_ATTEMPT = "currentSubmitAttempt";
	public static final String CURRENT_REGENERATE_ATTEMPT = "currentRegenerateAttempt";
}
