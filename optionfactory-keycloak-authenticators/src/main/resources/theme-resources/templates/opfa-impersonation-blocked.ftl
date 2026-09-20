<#-- opfa:modeled on base error.ftl -->
<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        ${msg("opfaImpersonationBlockedTitle")}
    <#elseif section = "form">
        <div id="kc-error-message">
            <p class="instruction">${kcSanitize(msg("opfaImpersonationBlocked", impersonationTarget!''))?no_esc}</p>
            <p>
                <#if backUrl?has_content>
                    <a id="backToApplication" href="${backUrl}">${msg("opfaImpersonationBack")}</a>
                <#elseif client?? && client.baseUrl?has_content>
                    <a id="backToApplication" href="${client.baseUrl}">${msg("backToApplication")}</a>
                </#if>
            </p>
        </div>
    </#if>
</@layout.registrationLayout>
