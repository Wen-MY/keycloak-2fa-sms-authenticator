package dasniko.keycloak.authenticator.gateway;

import dasniko.keycloak.authenticator.SmsConstants;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.util.Map;
@Slf4j
public class ApiSmsService implements SmsService {
	private final String apiUrl;
	private final String senderId;

	ApiSmsService(Map<String, String> config){
		this.apiUrl = config.get(SmsConstants.API_GATEWAY);
		this.senderId = config.getOrDefault(SmsConstants.SENDER_ID,"Keycloak");
	}
	@Override
	public void send(String phoneNumber, String message) {
		try{
			HttpClient httpClient = HttpClient.newHttpClient();
			Builder requestBuilder=json_request(phoneNumber,senderId,message);
			HttpRequest request = requestBuilder.build();
			HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());
			log.info("API request: %s", response.toString());
			if (response.statusCode() == 200) {
				log.info("Sent SMS to " + phoneNumber +  " API response: " + response.body());
			} else {
				log.error("Failed to send message to %s with answer: %s. Validate your config.", phoneNumber, response.body());
			}
		} catch (Exception e) {
			log.error("Failed to make request to "+ apiUrl);
			e.printStackTrace();
			return;
        }
    }
	public Builder json_request(String phoneNumber,String senderId, String message) throws URISyntaxException {
		String sendJson = "{"
			.concat(String.format("\"%s\":\"%s\",", SmsConstants.SENDER_ATTRIBUTE, senderId))
			.concat(String.format("\"%s\":\"%s\",", SmsConstants.RECEIVER_ATTRIBUTE, phoneNumber))
			.concat(String.format("\"%s\":\"%s\"", SmsConstants.MSG_ATTRIBUTE, message))
			.concat("}");

		return HttpRequest.newBuilder()
			.uri(new URI(apiUrl))
			.header("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString(sendJson));
	}

}
