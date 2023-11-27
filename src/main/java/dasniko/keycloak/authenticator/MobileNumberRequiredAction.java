package dasniko.keycloak.authenticator;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.InitiatedActionSupport;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.validation.Validation;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Niko Köbler, https://www.n-k.de, @dasniko
 */
@Slf4j
public class MobileNumberRequiredAction implements RequiredActionProvider {

	public static final String PROVIDER_ID = "mobile-number-ra";

	private static final String MOBILE_NUMBER_FIELD = "mobile_number";

	@Override
	public InitiatedActionSupport initiatedActionSupport() {
		return InitiatedActionSupport.SUPPORTED;
	}

	@Override
	public void evaluateTriggers(RequiredActionContext context) {
		AuthenticatorConfigModel config = context.getRealm().getAuthenticatorConfigByAlias("sms-2fa-auth");
		if (config == null) {
			log.error("Failed to check 2FA enforcement, no config alias sms-2fa found");
			return;
		}else{
			Map<String, String> configMap = config.getConfig();
			log.info("Authenticator Config: {}", configMap);
		}


		if (config.getConfig().get(SmsConstants.REQUIRED_FOR) != null) {
			log.info("Checking User Role : ", config.getConfig().get(SmsConstants.REQUIRED_FOR));
			String[] parts = config.getConfig().get(SmsConstants.REQUIRED_FOR).split("\\.");
			RoleModel whitelistRole = context.getRealm().getClientByClientId(parts[0]).getRole(parts[1]);
			if (whitelistRole == null) {
				log.warn(
					"Failed configured whitelist role check [%s], make sure that the role exists",
					config.getConfig().get(SmsConstants.REQUIRED_FOR)
				);
			} else if (context.getUser().hasRole(whitelistRole)) {
				// skip enforcement if user is whitelisted
				if (context.getUser().getFirstAttribute(MOBILE_NUMBER_FIELD) == null) {
					context.getUser().addRequiredAction(PROVIDER_ID);
					context.getAuthenticationSession().addRequiredAction(PROVIDER_ID);
				}
			}else{
				context.getUser().removeRequiredAction(PROVIDER_ID);
				context.getAuthenticationSession().removeRequiredAction(PROVIDER_ID);
			}
		}
		else{
			log.warn(
				"No roles setting for enforce SMS 2FA"
			);
		}
	}

	@Override
	public void requiredActionChallenge(RequiredActionContext context) {
		// show initial form
		context.challenge(createForm(context, null));
	}

	@Override
	public void processAction(RequiredActionContext context) {
		// submitted form

		UserModel user = context.getUser();

		MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
		String mobileNumber = formData.getFirst(MOBILE_NUMBER_FIELD);
		user.setSingleAttribute(MOBILE_NUMBER_FIELD, mobileNumber);
		user.removeRequiredAction(PROVIDER_ID);
		context.getAuthenticationSession().removeRequiredAction(PROVIDER_ID);

		context.success();
	}

	@Override
	public void close() {
	}

	private Response createForm(RequiredActionContext context, Consumer<LoginFormsProvider> formConsumer) {
		LoginFormsProvider form = context.form();
		form.setAttribute("username", context.getUser().getUsername());

		String mobileNumber = context.getUser().getFirstAttribute(MOBILE_NUMBER_FIELD);
		form.setAttribute(MOBILE_NUMBER_FIELD, mobileNumber == null ? "" : mobileNumber);

		if (formConsumer != null) {
			formConsumer.accept(form);
		}

		return form.createForm("update-mobile-number.ftl");
	}

}
