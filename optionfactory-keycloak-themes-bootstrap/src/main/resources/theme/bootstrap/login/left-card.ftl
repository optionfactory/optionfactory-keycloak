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
