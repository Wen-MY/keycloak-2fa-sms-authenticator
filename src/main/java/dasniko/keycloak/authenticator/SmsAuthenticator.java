package dasniko.keycloak.authenticator;

import dasniko.keycloak.authenticator.gateway.SmsServiceFactory;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.models.*;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.theme.Theme;
import java.util.Locale;

/**
 * @author Niko Köbler, https://www.n-k.de, @dasniko
 */
public class SmsAuthenticator implements Authenticator {

	private static final String MOBILE_NUMBER_FIELD = "mobile_number";
	private static final String TPL_CODE = "login-sms.ftl";

	@Override
	public void authenticate(AuthenticationFlowContext context) {

		AuthenticatorConfigModel config = context.getAuthenticatorConfig();
		KeycloakSession session = context.getSession();
		UserModel user = context.getUser();
		String mobileNumber = user.getFirstAttribute(MOBILE_NUMBER_FIELD);

		AuthenticationSessionModel authSession = context.getAuthenticationSession();



		// mobileNumber of course has to be further validated on proper format, country code, ...

		int length = Integer.parseInt(config.getConfig().get(SmsConstants.CODE_LENGTH));
		int ttl = Integer.parseInt(config.getConfig().get(SmsConstants.CODE_TTL));
		int ttr = Integer.parseInt(config.getConfig().get(SmsConstants.CODE_TTR));
		int cooldownInterval = Integer.parseInt(config.getConfig().get(SmsConstants.CODE_TTR));
		long currentTime = System.currentTimeMillis();



		try {
			//check for code generation history , avoid refresh to regenerate code
			if(authSession.getAuthNote(SmsConstants.CODE) == null) {
				String code = SecretGenerator.getInstance().randomString(length, SecretGenerator.DIGITS);
				authSession.setAuthNote(SmsConstants.CODE, code);
				authSession.setAuthNote(SmsConstants.CODE_TTL, Long.toString(currentTime + (ttl * 1000)));
				authSession.setAuthNote(SmsConstants.CODE_TTR, Long.toString(currentTime + (ttr * 1000)));
				authSession.setAuthNote(SmsConstants.LAST_GENERATION_TIME, Long.toString(currentTime));
				Theme theme = session.theme().getTheme(Theme.Type.LOGIN);
				Locale locale = session.getContext().resolveLocale(user);
				String smsAuthText = theme.getMessages(locale).getProperty("smsAuthText");
				String smsText = String.format(smsAuthText, code, Math.floorDiv(ttl, 60));
				SmsServiceFactory ssf = new SmsServiceFactory();
				ssf.get(config.getConfig()).send(mobileNumber, smsText,session);
			}
			if(authSession.getAuthNote(SmsConstants.CURRENT_SUBMIT_ATTEMPT) == null){
				authSession.setAuthNote(SmsConstants.CURRENT_SUBMIT_ATTEMPT,"1");
			}
			//check for submit attempt return if error exceed limit
			if(Integer.parseInt(authSession.getAuthNote(SmsConstants.CURRENT_SUBMIT_ATTEMPT)) > Integer.parseInt(config.getConfig().get(SmsConstants.AUTH_CODE_SUBMIT_ATTEMPT))){
				context.failureChallenge(AuthenticationFlowError.INVALID_CLIENT_SESSION,
					context.form().setError("smsAuthCodeExceedSubmitLimit")
						.createErrorPage(Response.Status.BAD_REQUEST));
				return;
			}
			context.challenge(context
				.form()
				.setAttribute("ttr",cooldownInterval)
				.setAttribute("ttl",ttl)
				.setAttribute("lastGenerationTime",authSession.getAuthNote(SmsConstants.LAST_GENERATION_TIME))
				.setAttribute("regenerateEnable","1")
				.createForm(TPL_CODE));
		} catch (Exception e) {
			context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
				context.form().setError("smsAuthSmsNotSent", e.getMessage())
					.createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
		}
	}

	@Override
	public void action(AuthenticationFlowContext context) {
		AuthenticatorConfigModel config = context.getAuthenticatorConfig();
		if (context.getHttpRequest().getDecodedFormParameters().containsKey("regenerate")) {
			regenerateCode(context);
			return;
		}
		String enteredCode = context.getHttpRequest().getDecodedFormParameters().getFirst(SmsConstants.CODE);

		AuthenticationSessionModel authSession = context.getAuthenticationSession();
		String code = authSession.getAuthNote(SmsConstants.CODE);
		String ttl = authSession.getAuthNote(SmsConstants.CODE_TTL);
		//check for expired
		if (Long.parseLong(ttl) < System.currentTimeMillis()) {
			// expired
			context.failureChallenge(AuthenticationFlowError.EXPIRED_CODE,
				context.form().setError("smsAuthCodeExpired").createErrorPage(Response.Status.BAD_REQUEST));
			return;
		}
		//check for submit attempt return if error exceed limit
		if(Integer.parseInt(authSession.getAuthNote(SmsConstants.CURRENT_SUBMIT_ATTEMPT)) > Integer.parseInt(config.getConfig().get(SmsConstants.AUTH_CODE_SUBMIT_ATTEMPT))){
			context.failureChallenge(AuthenticationFlowError.INVALID_CLIENT_SESSION,
				context.form().setError("smsAuthCodeExceedSubmitLimit")
					.createErrorPage(Response.Status.BAD_REQUEST));
			return;
		}
		if (code == null) {
			context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
				context.form().setError("smsAuthCodeNotFound")
					.createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
			return;
		}

		boolean isValid = enteredCode.equals(code);
		if (isValid) {
			// valid
			context.success();
		} else {
			// invalid

			AuthenticationExecutionModel execution = context.getExecution();
			if (execution.isRequired()) {
				int remainingAttempt = Integer.parseInt(config.getConfig().get(SmsConstants.AUTH_CODE_SUBMIT_ATTEMPT)) - Integer.parseInt(authSession.getAuthNote(SmsConstants.CURRENT_SUBMIT_ATTEMPT));
				context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS,
					context.form()
						.setAttribute("ttl" ,config.getConfig().get(SmsConstants.CODE_TTL))
						.setAttribute("ttr",config.getConfig().get(SmsConstants.CODE_TTR))
						.setAttribute("lastGenerationTime",authSession.getAuthNote(SmsConstants.LAST_GENERATION_TIME))
						.setAttribute("regenerateEnable","1")
						.setError("smsAuthCodeInvalid", remainingAttempt).createForm(TPL_CODE));
				authSession.setAuthNote(SmsConstants.CURRENT_SUBMIT_ATTEMPT,String.valueOf(Integer.parseInt(authSession.getAuthNote(SmsConstants.CURRENT_SUBMIT_ATTEMPT))+1));
			} else if (execution.isConditional() || execution.isAlternative()) {
				context.attempted();
			}
		}
	}

	@Override
	public boolean requiresUser() {
		return true;
	}

	@Override
	public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
		return user.getFirstAttribute(MOBILE_NUMBER_FIELD) != null;
	}

	@Override
	public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
		// this will only work if you have the required action from here configured:
		// https://github.com/dasniko/keycloak-extensions-demo/tree/main/requiredaction
		/*
		AuthenticatorConfigModel config = realm.getAuthenticatorConfigByAlias("sms-2fa");
		RoleModel whitelistRole = realm.getRole(config.getConfig().get("requiredFor"));
		if(user.hasRole(whitelistRole)){
			user.addRequiredAction(MobileNumberRequiredAction.PROVIDER_ID);
		}
		*/
		user.addRequiredAction(MobileNumberRequiredAction.PROVIDER_ID);
	}

	@Override
	public void close() {
	}
	public void regenerateCode(AuthenticationFlowContext context) {
		AuthenticatorConfigModel config = context.getAuthenticatorConfig();
		KeycloakSession session = context.getSession();
		UserModel user = context.getUser();

		String mobileNumber = user.getFirstAttribute(MOBILE_NUMBER_FIELD);

		int length = Integer.parseInt(config.getConfig().get(SmsConstants.CODE_LENGTH));
		int ttl = Integer.parseInt(config.getConfig().get(SmsConstants.CODE_TTL));
		int cooldownInterval = Integer.parseInt(config.getConfig().get(SmsConstants.CODE_TTR));
		AuthenticationSessionModel authSession = context.getAuthenticationSession();

		long currentTime = System.currentTimeMillis();
		String lastRegenerationTime = authSession.getAuthNote(SmsConstants.LAST_GENERATION_TIME);
		String regenerateEnable = "1"; //for checking is this sesssion still able to regenerate code or not
		if (lastRegenerationTime != null && currentTime - Long.parseLong(lastRegenerationTime) < cooldownInterval * 1000) {
			// Code regeneration is too frequent, return an error
			context.failureChallenge(AuthenticationFlowError.INVALID_CLIENT_SESSION,
				context.form().setError("smsAuthCodeRegenerationCooldown")
					.createErrorPage(Response.Status.BAD_REQUEST));
			return;
		}
		String code = SecretGenerator.getInstance().randomString(length, SecretGenerator.DIGITS);
		authSession.setAuthNote(SmsConstants.CODE, code);
		authSession.setAuthNote(SmsConstants.CODE_TTL,Long.toString(currentTime + (ttl * 1000)));
		authSession.setAuthNote(SmsConstants.CODE_TTR,Long.toString(currentTime + (cooldownInterval * 1000)));
		authSession.setAuthNote(SmsConstants.LAST_GENERATION_TIME, Long.toString(currentTime));
		if(authSession.getAuthNote(SmsConstants.CURRENT_SUBMIT_ATTEMPT) == null){
			authSession.setAuthNote(SmsConstants.CURRENT_SUBMIT_ATTEMPT,"1");
		}
		if(authSession.getAuthNote(SmsConstants.CURRENT_REGENERATE_ATTEMPT) == null){
			authSession.setAuthNote(SmsConstants.CURRENT_REGENERATE_ATTEMPT,"1");
		}else {
			//check for regenerate limit
			if(Integer.parseInt(authSession.getAuthNote(SmsConstants.CURRENT_REGENERATE_ATTEMPT))>= Integer.parseInt(config.getConfig().get(SmsConstants.REGENERATE_ATTEMPT))){
				context.failureChallenge(AuthenticationFlowError.INVALID_CLIENT_SESSION,
					context.form().setError("smsAuthCodeExceedRegenerateLimit")
						.createErrorPage(Response.Status.BAD_REQUEST));
				return;
			}
			int currentRegenerateAttempt = Integer.parseInt(authSession.getAuthNote(SmsConstants.CURRENT_REGENERATE_ATTEMPT));
			currentRegenerateAttempt += 1;
			if(currentRegenerateAttempt >= Integer.parseInt(config.getConfig().get(SmsConstants.REGENERATE_ATTEMPT))){
				regenerateEnable = "0";
			}
			authSession.setAuthNote(SmsConstants.CURRENT_REGENERATE_ATTEMPT, String.valueOf(currentRegenerateAttempt));
		}
		try {
			Theme theme = session.theme().getTheme(Theme.Type.LOGIN);
			Locale locale = session.getContext().resolveLocale(user);
			String smsAuthText = theme.getMessages(locale).getProperty("smsAuthTextRegenerate");
			String smsText = String.format(smsAuthText, code, Math.floorDiv(ttl, 60));
			SmsServiceFactory ssf = new SmsServiceFactory();
			ssf.get(config.getConfig()).send(mobileNumber, smsText,session);
			context.challenge(context.
				form()
				.setAttribute("ttr",cooldownInterval)
				.setAttribute("ttl",ttl)
				.setAttribute("lastGenerationTime",authSession.getAuthNote(SmsConstants.LAST_GENERATION_TIME))
				.setAttribute("regenerateEnable",regenerateEnable)
				.createForm(TPL_CODE));
		} catch (Exception e) {
			context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
				context.form().setError("smsAuthSmsNotSent", e.getMessage())
					.createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
		}
	}
}
