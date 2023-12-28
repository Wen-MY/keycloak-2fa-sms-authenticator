package dasniko.keycloak.authenticator;

import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.*;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Niko Köbler, https://www.n-k.de, @dasniko
 */
@AutoService(AuthenticatorFactory.class)
public class SmsAuthenticatorFactory implements AuthenticatorFactory {

	public static final String PROVIDER_ID = "sms-authenticator";

	private static final SmsAuthenticator SINGLETON = new SmsAuthenticator();
	//private List<String> ClientRoles = new ArrayList<String>();

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

	@Override
	public String getDisplayType() {
		return "SMS Authentication";
	}

	@Override
	public String getHelpText() {
		return "Validates an OTP sent via SMS to the users mobile phone.";
	}

	@Override
	public String getReferenceCategory() {
		return "otp";
	}

	@Override
	public boolean isConfigurable() {
		return true;
	}

	@Override
	public boolean isUserSetupAllowed() {
		return true;
	}

	@Override
	public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
		return REQUIREMENT_CHOICES;
	}

	@Override
	public List<ProviderConfigProperty> getConfigProperties() {
		return List.of(
			new ProviderConfigProperty(SmsConstants.CODE_LENGTH, "Code length", "The number of digits of the generated code.", ProviderConfigProperty.STRING_TYPE, 6),
			new ProviderConfigProperty(SmsConstants.CODE_TTL, "Time-to-live", "The time to live in seconds for the code to be valid.", ProviderConfigProperty.STRING_TYPE, "300"),
			new ProviderConfigProperty(SmsConstants.SENDER_ID, "SenderId", "The sender ID is displayed as the message sender on the receiving device.", ProviderConfigProperty.STRING_TYPE, "Keycloak"),
			new ProviderConfigProperty(SmsConstants.SIMULATION_MODE, "Simulation mode", "In simulation mode, the SMS won't be sent, but printed to the server logs", ProviderConfigProperty.BOOLEAN_TYPE, true),
			new ProviderConfigProperty(SmsConstants.CODE_TTR,"Time-to-regenerate","The time to regenerate in seconds for the code can be sent to user device",ProviderConfigProperty.STRING_TYPE,"30"),
			new ProviderConfigProperty(SmsConstants.AUTH_CODE_SUBMIT_ATTEMPT,"OTP code Submit Attempt","The attempts that user can submit the invalid code on each code generation, user is not allow use submit the OTP code if have exceed this attempt in 1 login session.",ProviderConfigProperty.STRING_TYPE,"3"),
			new ProviderConfigProperty(SmsConstants.REGENERATE_ATTEMPT,"OTP code Regenerate Attempt","The attempts that user regenerate a new OTP code in 1 login session",ProviderConfigProperty.STRING_TYPE,"3"),
			new ProviderConfigProperty(SmsConstants.API_URL,"URL of API gateway", "The URL connected to your API server hosted on",ProviderConfigProperty.STRING_TYPE,"https://127.0.0.1"),
			new ProviderConfigProperty(SmsConstants.REQUIRED_FOR,"Required for enforced 2FA","All users with the here selected role are forced to use 2FA.", ProviderConfigProperty.ROLE_TYPE, null)
		);
	}

	@Override
	public Authenticator create(KeycloakSession session) {
		//getAllClientRoles(session);
		return SINGLETON;}

	@Override
	public void init(Config.Scope config) {
	}

	@Override
	public void postInit(KeycloakSessionFactory factory) {
	}

	@Override
	public void close() {
	}
	/*
	private void getAllClientRoles(KeycloakSession session){

		List<ClientModel> clientModels = session.getContext().getRealm().getClientsStream().collect(Collectors.toList());

		for(ClientModel clientModel : clientModels){
			List<RoleModel> roleModels = clientModel.getRolesStream().collect(Collectors.toList());
			for(RoleModel roleModel : roleModels){
				ClientRoles.add(roleModel.getName());
			}
		}

		List <RoleModel> realmRoles = session.getContext().getRealm().getRolesStream().collect(Collectors.toList());
		ClientRoles.addAll(realmRoles.stream().map(RoleModel::getName).collect(Collectors.toList()));
	}
	*/

}
