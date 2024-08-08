<#-- opfa:imported from 25.0.2 -->
<#macro registrationLayout bodyClass="" displayInfo=false displayMessage=true displayRequiredFields=false showTabs=false showFooterCard=false>
<!DOCTYPE html>
<html class="${properties.kcHtmlClass!}"<#if realm.internationalizationEnabled> lang="${locale.currentLanguageTag}"</#if>>

<head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

    <#-- opfa:modification start -->
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <meta name="format-detection" content="telephone=no">
    <meta name="format-detection" content="email=no">
    <#-- opfa:modification end -->
    <meta name="robots" content="noindex, nofollow">

    <#if properties.meta?has_content>
        <#list properties.meta?split(' ') as meta>
            <meta name="${meta?split('==')[0]}" content="${meta?split('==')[1]}"/>
        </#list>
    </#if>
    <title>${msg("loginTitle",(realm.displayName!''))}</title>
    <link rel="icon" href="${url.resourcesPath}/img/favicon.ico" />

    <#-- opfa:modification start (google fonts, baseHeaders, themeHeaders)-->

    <#if properties.googleFonts?has_content>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?${properties.googleFonts}" rel="stylesheet">
    </#if>

    <#list 0..10 as index>
        <#if properties['baseHeaders.' + index]?has_content>
            ${properties['baseHeaders.' + index]?replace("{resources}", "${url.resourcesPath}")?replace("{commonResources}", "${url.resourcesCommonPath}")?no_esc}
        </#if>
    </#list>
    <#list 0..50 as index>
        <#if properties['themeHeaders.' + index]?has_content>
            ${properties['themeHeaders.' + index]?replace("{resources}", "${url.resourcesPath}")?replace("{commonResources}", "${url.resourcesCommonPath}")?no_esc}
        </#if>
    </#list>

    <#-- opfa:modification end -->

    <#if properties.stylesCommon?has_content>
        <#list properties.stylesCommon?split(' ') as style>
            <link href="${url.resourcesCommonPath}/${style}" rel="stylesheet" />
        </#list>
    </#if>
    <#if properties.styles?has_content>
        <#list properties.styles?split(' ') as style>
            <link href="${url.resourcesPath}/${style}" rel="stylesheet" />
        </#list>
    </#if>
    <#if properties.scripts?has_content>
        <#list properties.scripts?split(' ') as script>
            <script src="${url.resourcesPath}/${script}" type="text/javascript"></script>
        </#list>
    </#if>
    <script type="importmap">
        {
            "imports": {
                "rfc4648": "${url.resourcesCommonPath}/node_modules/rfc4648/lib/rfc4648.js"
            }
        }
    </script>
    <script src="${url.resourcesPath}/js/menu-button-links.js" type="module"></script>
    <#if scripts??>
        <#list scripts as script>
            <script src="${script}" type="text/javascript"></script>
        </#list>
    </#if>
    <script type="module">
        import { checkCookiesAndSetTimer } from "${url.resourcesPath}/js/authChecker.js";

        checkCookiesAndSetTimer(
          "${url.ssoLoginInOtherTabsUrl?no_esc}"
        );
    </script>
</head>

<body class="${properties.kcBodyClass!} ${bodyClass}">
    <#-- opfa:modification start -->
    <#if (properties.cards!"false") == 'true'>
        <div class="left">
            <#if msg('leftCardTitle')?has_content || msg('leftCardText')?has_content>
            <div class="card p-4">
                <#if msg('leftCardTitle')?has_content>
                    <div class="title">${msg("leftCardTitle")}</div>
                </#if>
                <#if msg('leftCardText')?has_content>
                    <p>${msg("leftCardText")}</p>
                </#if>
            </div>
            </#if>
        </div>

        <div class="right">
    </#if>
    <#-- opfa:modification end -->
    <div class="${properties.kcLoginClass!}">
    <div id="kc-header" class="${properties.kcHeaderClass!}">
        <div id="kc-header-wrapper"
             class="${properties.kcHeaderWrapperClass!}">${kcSanitize(msg("loginTitleHtml",(realm.displayNameHtml!'')))?no_esc}</div>
    </div>
    <div class="${properties.kcFormCardClass!}">
        <header class="${properties.kcFormHeaderClass!}">
            <#if realm.internationalizationEnabled  && locale.supported?size gt 1>
                <div class="${properties.kcLocaleMainClass!}" id="kc-locale">
                    <div id="kc-locale-wrapper" class="${properties.kcLocaleWrapperClass!}">
                        <div id="kc-locale-dropdown" class="menu-button-links ${properties.kcLocaleDropDownClass!}">
                            <button tabindex="1" id="kc-current-locale-link" aria-label="${msg("languages")}" aria-haspopup="true" aria-expanded="false" aria-controls="language-switch1">${locale.current}</button>
                            <ul role="menu" tabindex="-1" aria-labelledby="kc-current-locale-link" aria-activedescendant="" id="language-switch1" class="${properties.kcLocaleListClass!}">
                                <#assign i = 1>
                                <#list locale.supported as l>
                                    <li class="${properties.kcLocaleListItemClass!}" role="none">
                                        <a role="menuitem" id="language-${i}" class="${properties.kcLocaleItemClass!}" href="${l.url}">${l.label}</a>
                                    </li>
                                    <#assign i++>
                                </#list>
                            </ul>
                        </div>
                    </div>
                </div>
            </#if>
        <#if !(auth?has_content && auth.showUsername() && !auth.showResetCredentials())>
            <#if displayRequiredFields>
                <div class="${properties.kcContentWrapperClass!}">
                    <#-- opfa:modification start: added header2 -->
                    <h1 id="kc-page-title"><#nested "header"></h1>
                    <p id="kc-page-subtitle"><#nested "header2"></p>
                    <div class="${properties.kcLabelWrapperClass!} subtitle">
                        <span class="subtitle"><span class="required">*</span> ${msg("requiredFields")}</span>
                    </div>
                    <#-- opfa:modification end -->
                </div>
            <#else>
                <#-- opfa:modification start: added header2 -->
                <h1 id="kc-page-title"><#nested "header"></h1>
                <p id="kc-page-subtitle"><#nested "header2"></p>
                <#-- opfa:modification end -->
            </#if>
        <#else>
            <#if displayRequiredFields>
                <div class="${properties.kcContentWrapperClass!}">
                    <div class="${properties.kcLabelWrapperClass!} subtitle">
                        <span class="subtitle"><span class="required">*</span> ${msg("requiredFields")}</span>
                    </div>
                    <div class="col-md-10">
                        <#nested "show-username">
                        <div id="kc-username" class="${properties.kcFormGroupClass!}">
                            <label id="kc-attempted-username">${auth.attemptedUsername}</label>
                            <a id="reset-login" href="${url.loginRestartFlowUrl}" aria-label="${msg("restartLoginTooltip")}">
                                <div class="kc-login-tooltip">
                                    <i class="${properties.kcResetFlowIcon!}"></i>
                                    <span class="kc-tooltip-text">${msg("restartLoginTooltip")}</span>
                                </div>
                            </a>
                        </div>
                    </div>
                </div>
            <#else>
                <#nested "show-username">
                <div id="kc-username" class="${properties.kcFormGroupClass!}">
                    <label id="kc-attempted-username">${auth.attemptedUsername}</label>
                    <a id="reset-login" href="${url.loginRestartFlowUrl}" aria-label="${msg("restartLoginTooltip")}">
                        <div class="kc-login-tooltip">
                            <i class="${properties.kcResetFlowIcon!}"></i>
                            <span class="kc-tooltip-text">${msg("restartLoginTooltip")}</span>
                        </div>
                    </a>
                </div>
            </#if>
        </#if>
      </header>
      <div id="kc-content">
        <div id="kc-content-wrapper">
          <#-- opfa:modification start -->
          <#if showTabs>
            <div class="d-flex mb-3">
              <a class="btn btn-info flex-grow-1 m-2 disabled">${msg("doLogIn")}</a>
              <a class="btn btn-info flex-grow-1 m-2" href="${url.registrationUrl}">${msg("doRegister")}</a>
            </div>
          </#if>
          <#-- opfa:modification end -->

          <#-- App-initiated actions should not see warning messages about the need to complete the action -->
          <#-- during login.                                                                               -->
          <#if displayMessage && message?has_content && (message.type != 'warning' || !isAppInitiatedAction??)>
              <div class="alert-${message.type} ${properties.kcAlertClass!} pf-m-<#if message.type = 'error'>danger<#else>${message.type}</#if>">
                  <div class="pf-c-alert__icon">
                      <#if message.type = 'success'><span class="${properties.kcFeedbackSuccessIcon!}"></span></#if>
                      <#if message.type = 'warning'><span class="${properties.kcFeedbackWarningIcon!}"></span></#if>
                      <#if message.type = 'error'><span class="${properties.kcFeedbackErrorIcon!}"></span></#if>
                      <#if message.type = 'info'><span class="${properties.kcFeedbackInfoIcon!}"></span></#if>
                  </div>
                      <span class="${properties.kcAlertTitleClass!}">${kcSanitize(message.summary)?no_esc}</span>
              </div>
          </#if>

          <div class="legend-mandatory-fields" aria-hidden="true">I campi contrassegnati con (*) sono obbligatori</div>

          <#nested "form">

          <#if auth?has_content && auth.showTryAnotherWayLink()>
              <form id="kc-select-try-another-way-form" action="${url.loginAction}" method="post">
                  <div class="${properties.kcFormGroupClass!}">
                      <input type="hidden" name="tryAnotherWay" value="on"/>
                      <a href="#" id="try-another-way"
                         onclick="document.forms['kc-select-try-another-way-form'].submit();return false;">${msg("doTryAnotherWay")}</a>
                  </div>
              </form>
          </#if>

          <#nested "socialProviders">

          <#if displayInfo>
              <div id="kc-info" class="${properties.kcSignUpClass!}">
                  <div id="kc-info-wrapper" class="${properties.kcInfoAreaWrapperClass!}">
                      <#nested "info">
                  </div>
              </div>
          </#if>
        </div>
      </div>
    </div>
  <#-- opfa:modification start -->
  <#if (properties.cards!"false") == 'true'>
        <#if showFooterCard>
            <div class="footer-card">
                <div class="card p-4">
                    <#nested "footerCard">
                </div>
            </div>
        </#if>
  </div>
  </#if>
  <#-- opfa:modification end -->

  </div>
</body>
</html>
</#macro>
