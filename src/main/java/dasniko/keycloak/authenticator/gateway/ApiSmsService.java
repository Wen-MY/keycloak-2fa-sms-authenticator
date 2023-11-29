package dasniko.keycloak.authenticator.gateway;

import dasniko.keycloak.authenticator.SmsConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.models.KeycloakSession;

import javax.net.ssl.*;


import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import java.security.cert.CertificateException;
import java.util.Map;
import java.io.InputStream;
@Slf4j
public class ApiSmsService implements SmsService {
	private final String apiUrl;
	private final String senderId;

	ApiSmsService(Map<String, String> config){
		this.apiUrl = config.getOrDefault(SmsConstants.API_URL,"http://localhost/sent");
		this.senderId = config.getOrDefault(SmsConstants.SENDER_ID,"Keycloak");


	}
	@Override
	public void send(String phoneNumber, String message, KeycloakSession session) {
		try{
			HttpClientProvider provider = session.getProvider(HttpClientProvider.class);
			CloseableHttpClient httpClient = provider.getHttpClient();

			HttpPost request = new HttpPost(apiUrl);
			request.setHeader("Content-Type", "application/json");
			request.setEntity(new StringEntity(jsonRequest(phoneNumber, senderId, message)));
			try(CloseableHttpResponse response = httpClient.execute(request)) {
				log.info("API request: " + apiUrl);
				if (response.getStatusLine().getStatusCode() == 200) {
					log.info("Sent SMS to " + phoneNumber + " API response: " + response.getEntity().toString());
				} else {
					log.error("Failed to send message to " + phoneNumber + " with answer: " + response.getEntity().toString() + " Validate your config.");
				}
			}
		} catch (Exception e) {
			log.error("Failed to make request to "+ apiUrl, e);
		}
	}
	private String jsonRequest(String phoneNumber, String senderId, String message) {
		return "{" +
			String.format("\"%s\":\"%s\",", SmsConstants.SENDER_ATTRIBUTE, senderId) +
			String.format("\"%s\":\"%s\",", SmsConstants.RECEIVER_ATTRIBUTE, phoneNumber) +
			String.format("\"%s\":\"%s\"", SmsConstants.MSG_ATTRIBUTE, message) +
			"}";
	}
	/*
	manual importing the truststore file without using the truststore get from keycloak
	private SSLContext createSSLContext() throws Exception {
		String truststorePath = "../conf/keycloak-truststore.jks";
		String truststorePassword = "smsauth";
		KeyStore truststore = KeyStore.getInstance("JKS");
		try (InputStream truststoreStream = new FileInputStream(truststorePath)) {
			truststore.load(truststoreStream, truststorePassword.toCharArray());
		} catch (IOException | NoSuchAlgorithmException | CertificateException e) {
			log.error("Failed to load truststore from path: {}", truststorePath);
			e.printStackTrace();
			throw e;
		}
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(truststore,truststorePassword.toCharArray());
		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(truststore);

		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());

		HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
		HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
		return sslContext;
	}
	 */

}
