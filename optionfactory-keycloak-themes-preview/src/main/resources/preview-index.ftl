<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>${title}</title>
    <style>
        :root {
            --panel: 320px
        }
        * {
            box-sizing: border-box
        }
        body {
            font-family: sans-serif;
            margin: 0;
            display: flex;
            align-items: flex-start
        }
        aside {
            width: var(--panel);
            flex: 0 0 var(--panel);
            height: 100vh;
            position: sticky;
            top: 0;
            overflow-y: auto;
            padding: 1rem;
            border-right: 1px solid #ddd;
            background: #fafafa;
            font-size: .8rem
        }
        main {
            flex: 1;
            padding: 1rem 0 1rem 1rem;
            min-width: 0
        }
        h1 {
            font-size: 1rem;
            margin: 0 0 .5rem
        }
        iframe {
            width: 420px;
            height: 640px;
            border: 1px solid #ccc;
            background: #fff
        }
        .cell {
            display: inline-block;
            margin: 0 1rem 1rem 0;
            text-align: center;
            vertical-align: top
        }
        .hint {
            background: #fff3cd;
            border: 1px solid #ffe69c;
            padding: .5rem;
            margin: 0 0 1rem
        }
        .row {
            display: flex;
            gap: .35rem;
            align-items: center;
            margin-bottom: .25rem
        }
        .row label {
            flex: 1;
            font-family: monospace;
            font-size: .72rem;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap
        }
        .row input[type=text] {
            width: 7.5rem;
            font-family: monospace;
            font-size: .72rem
        }
        .row input[type=color] {
            width: 1.6rem;
            height: 1.4rem;
            padding: 0;
            border: 1px solid #ccc;
            background: none
        }
        .row.changed label {
            font-weight: 700;
            color: #b45309
        }
        details {
            margin-bottom: .75rem
        }
        summary {
            cursor: pointer;
            font-weight: 700;
            margin-bottom: .35rem
        }
        h2 {
            font-size: .8rem;
            margin: 1rem 0 .35rem
        }
        summary.file {
            font-family: monospace;
            font-size: .68rem;
            color: #666;
            font-weight: 400
        }
        details[open] > summary.file {
            color: #111;
            font-weight: 700
        }
        .count {
            color: #999
        }
        textarea {
            width: 100%;
            height: 9rem;
            font-family: monospace;
            font-size: .72rem
        }
        button {
            font-size: .75rem;
            padding: .3rem .5rem
        }
        select {
            font-size: .75rem;
            max-width: 100%
        }
        .controls {
            display: grid;
            grid-template-columns: auto 1fr;
            gap: .35rem;
            align-items: center;
            margin-bottom: .75rem
        }
    </style>
</head>
<body>
<aside>
    <h1>${title}</h1>
    <p class="hint">Edits apply to every frame below, live. They are not written to any file:
        <b>Copy overrides</b> gives you the css to paste into a theme stylesheet.</p>

    <div class="controls">
        <span>viewport</span>
        <select id="viewport">
            <option value="420x640">gallery default &mdash; 420&times;640</option>
            <option value="320x568">iPhone SE (1st) &mdash; 320&times;568</option>
            <option value="375x667">iPhone SE (2nd/3rd) &mdash; 375&times;667</option>
            <option value="390x844">iPhone 12/13/14 &mdash; 390&times;844</option>
            <option value="393x852">iPhone 15/16 &mdash; 393&times;852</option>
            <option value="430x932">iPhone 15/16 Pro Max &mdash; 430&times;932</option>
            <option value="360x800">Galaxy S20/S23 &mdash; 360&times;800</option>
            <option value="412x915">Pixel 7/8 &mdash; 412&times;915</option>
            <option value="768x1024">iPad mini &mdash; 768&times;1024</option>
            <option value="820x1180">iPad Air &mdash; 820&times;1180</option>
            <option value="1024x1366">iPad Pro 12.9 &mdash; 1024&times;1366</option>
            <option value="1280x800">desktop &mdash; 1280&times;800</option>
            <option value="1440x900">desktop large &mdash; 1440&times;900</option>
            <option value="1920x1080">desktop full hd &mdash; 1920&times;1080</option>
        </select>
    </div>

    <h2>css variables</h2>
    <#if variableGroups?size == 0>
        <p>no <code>:root</code> declarations found in the previewed stylesheets.</p>
    </#if>
    <#-- the sheet with the last word is open; the ones it overrides start collapsed -->
    <#list variableGroups as group>
        <details<#if group?index == 0> open</#if>>
            <summary class="file">${group.file} <span class="count">${group.variables?size}</span></summary>
            <#list group.variables as variable>
                <div class="row" data-var="${variable.name}">
                    <label for="in-${variable.name}" title="${variable.name}: ${variable.value}">${variable.name}</label>
                    <#if variable.color>
                        <input type="color" id="col-${variable.name}" value="${variable.value}" data-role="color">
                    </#if>
                    <input type="text" id="in-${variable.name}" value="${variable.value}" data-role="value" spellcheck="false">
                </div>
            </#list>
        </details>
    </#list>

    <details>
        <summary>custom css</summary>
        <textarea id="custom" spellcheck="false" placeholder="#kc-form-buttons { gap: 2rem }&#10;.form-control { border-radius: 0 }"></textarea>
    </details>

    <div class="row">
        <button id="copy">Copy overrides</button>
        <button id="reset">Reset</button>
    </div>
    <p>Rendered by ${renderer} (mvn test). Titles link to the standalone page.</p>
</aside>

<main>
    <#list pages as page>
        <div class="cell">
            <div><a href="${page}.html" target="_blank">${page}</a></div>
            <iframe src="${page}.html" loading="lazy"></iframe>
        </div>
    </#list>
</main>

<script>
    const KEY = "preview-overrides:${outputDirectory}";
    const rows = [...document.querySelectorAll(".row[data-var]")];
    const custom = document.getElementById("custom");
    const viewport = document.getElementById("viewport");
    const frames = () => [...document.querySelectorAll("main iframe")];

    const defaults = new Map(rows.map(r => [r.dataset.var, r.querySelector("[data-role=value]").value]));

    const overrides = () => rows
            .filter(r => r.querySelector("[data-role=value]").value.trim() !== defaults.get(r.dataset.var))
            .map(r => [r.dataset.var, r.querySelector("[data-role=value]").value.trim()]);

    const css = () => {
        const vars = overrides();
        const root = vars.length ? ":root {\n" + vars.map(([n, v]) => `    ${'$'}{n}: ${'$'}{v};`).join("\n") + "\n}\n" : "";
        return root + custom.value;
    };

    /* the frames may be file:// documents, which a parent cannot reach into: each page
       carries a listener (injected at generation time) that takes the css as a message
       and adds it as a last stylesheet - postMessage crosses file:// origins */
    const paint = (frame) => frame.contentWindow.postMessage({type: "preview-overrides", css: css()}, "*");

    const apply = () => {
        const [w, h] = viewport.value.split("x");
        for (const frame of frames()) {
            frame.style.width = w + "px";
            frame.style.height = h + "px";
            paint(frame);
        }
        for (const row of rows) {
            const input = row.querySelector("[data-role=value]");
            const swatch = row.querySelector("[data-role=color]");
            row.classList.toggle("changed", input.value.trim() !== defaults.get(row.dataset.var));
            if (swatch && /^#[0-9a-fA-F]{6}${'$'}/.test(input.value.trim())) {
                swatch.value = input.value.trim();
            }
        }
        /* a file:// origin that refuses storage just loses the session */
        try {
            localStorage.setItem(KEY, JSON.stringify({
                vars: Object.fromEntries(overrides()),
                custom: custom.value,
                viewport: viewport.value
            }));
        } catch (e) {
        }
    };

    for (const row of rows) {
        const input = row.querySelector("[data-role=value]");
        const swatch = row.querySelector("[data-role=color]");
        input.addEventListener("input", apply);
        if (swatch) {
            swatch.addEventListener("input", () => {
                input.value = swatch.value;
                apply();
            });
        }
    }
    custom.addEventListener("input", apply);
    viewport.addEventListener("change", apply);
    /* the clipboard api is not there from every file:// origin: fall back to a
       selected textarea, which works everywhere */
    document.getElementById("copy").addEventListener("click", () => {
        const text = css();
        const select = () => {
            const field = document.createElement("textarea");
            field.value = text;
            document.body.appendChild(field);
            field.select();
            document.execCommand("copy");
            field.remove();
        };
        navigator.clipboard ? navigator.clipboard.writeText(text).catch(select) : select();
    });
    document.getElementById("reset").addEventListener("click", () => {
        for (const row of rows) {
            row.querySelector("[data-role=value]").value = defaults.get(row.dataset.var);
        }
        custom.value = "";
        apply();
    });

    /* frames are lazy: paint each as it arrives, and re-paint the whole wall on every edit.
       The pages blur their own autofocus on load (the gallery cannot reach into a file://
       frame to do it), so it stays put without help. */
    for (const frame of frames()) {
        frame.addEventListener("load", () => paint(frame));
    }

    let saved = {};
    try {
        saved = JSON.parse(localStorage.getItem(KEY) || "{}");
    } catch (e) {
    }
    for (const [name, value] of Object.entries(saved.vars || {})) {
        const input = document.getElementById("in-" + name);
        if (input) {
            input.value = value;
        }
    }
    custom.value = saved.custom || "";
    viewport.value = saved.viewport || viewport.value;
    apply();
</script>
</body>
</html>
