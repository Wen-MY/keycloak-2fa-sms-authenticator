<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('mobile_number'); section>
    <#if section = "header">
        ${msg("updateMobileTitle",realm.displayName)}
    <#elseif section = "form">
			<h2>${msg("updateMobileHello",(username!''))}</h2>
			<p>${msg("updateMobileText")}</p>
			<p>Please contact your IT administrator to setting up your phone number</p>
    </#if>
</@layout.registrationLayout>
