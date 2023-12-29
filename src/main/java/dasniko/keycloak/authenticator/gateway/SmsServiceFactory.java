package dasniko.keycloak.authenticator.gateway;

import dasniko.keycloak.authenticator.SmsConstants;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.KeycloakSession;

import java.util.Map;

/**
 * @author Niko Köbler, https://www.n-k.de, @dasniko
 */
@Slf4j
public class SmsServiceFactory {

	public SmsServiceFactory(){
		log.info("A new SMS Service Factory is created");
	}

	public static SmsService get(Map<String, String> config) {
		//log.info(String.format("A new SMS Service Factory is created"));
		try {
			if (Boolean.parseBoolean(config.getOrDefault(SmsConstants.SIMULATION_MODE, "false"))) {
				return (phoneNumber, message, session) ->
					log.warn(String.format("***** SIMULATION MODE ***** Would send SMS to %s with text: %s", phoneNumber, message));
			} else {
				return new ApiSmsService(config);
			}
		}catch (SmsServiceException e){
			return (phoneNumber, message, session) -> {
				throw new SmsServiceException(e.getMessage());
			};
		}
	}

}
