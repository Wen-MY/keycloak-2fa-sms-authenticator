<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
	<#if section = "header">
		${msg("smsAuthTitle")}
	<#elseif section = "show-username">
		<h1>${msg("smsAuthCodeTitle")}</h1>
	<#elseif section = "form">
		<form id="kc-sms-code-login-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
			<div class="${properties.kcFormGroupClass!}">
				<div class="${properties.kcLabelWrapperClass!}">
					<label for="code" class="${properties.kcLabelClass!}">${msg("smsAuthLabel")}</label>
				</div>
				<div class="${properties.kcInputWrapperClass!}" style = "display: flex">
					<input type="text" id="code" name="code" class="${properties.kcInputClass!}" autofocus/>
					<span class="${properties.kcLabelClass!}" id="countdownNumber" style="margin-left: 12px;display: flex;flex-wrap: wrap;align-content: space-around;"></span>
				</div>
			</div>
			<div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingClass!}">
				<div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
					<input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" type="submit" value="${msg("doSubmit")}"/>
					<span style="margin: 8px;"></span>
					<button id="regenerateButton" style="display: none" class="${properties.kcButtonClass!} ${properties.kcButtonDefaultClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" type="submit" name="regenerate" value="true">${msg("Resend OTP")}</button>
				</div>
			</div>
		</form>
	<#elseif section = "info" >
		${msg("smsAuthInstruction")}
	</#if>
</@layout.registrationLayout>
<script>
	const ttl = ${ttl!300}
	const ttr = ${ttr!30}
	function enableRegenerateButton(generationTime) {
            var regenerateButton = document.getElementById('regenerateButton');
            var currentTime = new Date().getTime();
			if (generationTime === 0 || isNaN(generationTime)) {
                // If lastRegenerationTime is null or not a valid number, set it to the current time
                regenerationTime = currentTime;
            }
            if (currentTime >= generationTime + (ttr*1000)) {
                // Enable the button
                regenerateButton.disabled = false;
                regenerateButton.style.display = 'block';
            } else {
                // Calculate the remaining time until regeneration and update the button text
                let remainingTimeInSeconds = Math.ceil(((generationTime + (ttr*1000)) - currentTime) / 1000);
				regenerateButton.disabled = true;
				regenerateButton.style.display = 'none';
                // Schedule a timeout to enable the button when the regeneration time is reached
                setTimeout(function () {
                    enableRegenerateButton(generationTime);
                    console.log("Time left to regenerate :" + remainingTimeInSeconds);
                }, 1000);
            }
        }
	function updateCountdown(lastGenerationTime) {
			var currentTime = new Date().getTime();
			let remainingTimeInSeconds = Math.ceil(((ttl*1000) - (currentTime - lastGenerationTime))/1000)//check is the code still valid
            var countdownNumber = document.getElementById('countdownNumber');
            countdownNumber.innerHTML = remainingTimeInSeconds + 's';

            if (remainingTimeInSeconds > 0) {
                setTimeout(function () {
					updateCountdown(lastGenerationTime); // Update every second
                	console.log("Time left to live :" + remainingTimeInSeconds);
			},1000);
            } else {
                countdownNumber.textContent = '0s'; // Optionally, clear the countdown text when it reaches 0
            }
        }
        // Call the function to check and enable/disable the button on page load
    const lastGenerationTime = ${lastGenerationTime!0};
    const regenerateEnable = ${regenerateEnable!1};
    if(regenerateEnable){
    	enableRegenerateButton(lastGenerationTime);
    }
    updateCountdown(lastGenerationTime);

</script>
