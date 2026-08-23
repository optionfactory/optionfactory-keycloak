<#macro googleFonts>
    <#if properties.googleFonts?has_content>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?${properties.googleFonts}" rel="stylesheet">
    </#if>
</#macro>

<#macro baseHeaders>
    <#list 0..10 as index>
        <#if properties['baseHeaders.' + index]?has_content>
            ${properties['baseHeaders.' + index]?replace("{resources}", "${url.resourcesPath}")?replace("{commonResources}", "${url.resourcesCommonPath}")?no_esc}
        </#if>
    </#list>
</#macro>

<#macro themeHeaders>
    <#list 0..50 as index>
        <#if properties['themeHeaders.' + index]?has_content>
            ${properties['themeHeaders.' + index]?replace("{resources}", "${url.resourcesPath}")?replace("{commonResources}", "${url.resourcesCommonPath}")?no_esc}
        </#if>
    </#list>
</#macro>
