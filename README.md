# Keycloak 2FA SMS Authenticator

Keycloak Authentication Provider implementation to get a 2nd-factor authentication with a OTP/code/token send via SMS through self-hosted SMSC server.

# Keycloak SMS 2FA Authentication Provider

## Overview

This Keycloak plugin adds a second-factor authentication method using a One-Time Password (OTP) sent via SMS through a self-hosted SMSC (Short Message Service Center) server. This enhances the security of user authentication in your Keycloak instance.

## Prerequisites

Ensure the following prerequisites are met before using this Keycloak plugin:

- JDK 17
- Maven (latest version)
- Keycloak v22.0.5 (or compatible version)

## Build and Installation

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/your-username/keycloak-sms-2fa.git
   cd keycloak-sms-2fa
   ```

2. **Build the Keycloak Plugin:**
   ```bash
   mvn clean package
   ```

   This command compiles the code and packages the Keycloak plugin into a JAR file.

3. **Navigate to the Target Directory:**
   ```bash
   cd target
   ```

   In this directory, you will find a JAR file named `keycloak-sms-2fa.jar`.

4. **Copy the JAR file to Keycloak Provider Directory:**
   Copy the `keycloak-sms-2fa.jar` file to the Keycloak Provider directory. Replace `keycloakRootPath` with the actual path to your Keycloak installation.

   ```bash
   cp keycloak-sms-2fa.jar keycloakRootPath/providers/
   ```

5. **Build and Start Keycloak:**
   Navigate to the Keycloak bin directory and run the build script:

   ```bash
   cd keycloakRootPath/bin
   ./kc.sh build
   ```

   After the build is complete, start Keycloak in development mode:

   ```bash
   ./kc.sh start-dev
   ```

   Keycloak will start, and the SMS 2FA authentication provider will be available for configuration.

## Configuration

1. **Access Keycloak Admin Console:**
   Open your browser and go to `http://localhost:8080/` (OR replace with your Keycloak URL).

2. **Log in to the Admin Console:**
   Log in with your administrator credentials.

3. **Configure SMS 2FA Authentication:**
   - Navigate to the realm and select "Authentication" in the left sidebar.
   - Duplicate a the built-in browser flow and add the new step for the browser flow, SMS Authentication.
   - **MUST** the Step Alias Name with "sms-2fa-auth" , else it will won't work as intended.
   - Configure other necessary settings or use the simulation for testing purpose.
   - Click on the action button and bind the authentication flow to include the SMS 2FA provider.

4. **Test the SMS 2FA:**
   - Log in with a user account that is configured to use the SMS 2FA provider.
   - Follow the prompts to enter the OTP/code/token sent via SMS.
## SSL Configuration
If you want apply http request with secure socket layer (HTTPS). You need to put the java keystore file of your reverse proxy server into `keycloakRootPath/conf` and you need to config the keycloak by adding a section for keystore spi.
```bash
nano keycloakRootPath/conf/keycloak.conf
#add this section to the configuration file

# Keycloak Truststore Configuration
spi-truststore-file-file=/keycloak/keycloak-22.0.5/conf/NAME_OF_YOUR_KEYSTORE.jks <-- your keystore file
spi-truststore-file-password=password <-- your keystore file password if applicable
spi-truststore-file-hostname-verification-policy=ANY

```
After adding the section of code into the keycloak configuration file , you need to rebuild the keycloak before starting the keycloak

## Additional Information

- Customize the SMSC server settings and SMS templates in the source code as needed.
- For more details on Keycloak authentication configuration, refer to the [Keycloak Documentation](https://www.keycloak.org/documentation.html).
