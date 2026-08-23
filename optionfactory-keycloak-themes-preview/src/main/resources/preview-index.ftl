<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>${title}</title>
    <style>
        body{font-family:sans-serif;margin:2rem}
        iframe{width:420px;height:640px;border:1px solid #ccc;background:#fff}
        .cell{display:inline-block;margin:1rem;text-align:center;vertical-align:top}
        .hint{background:#fff3cd;border:1px solid #ffe69c;padding:.5rem 1rem;display:inline-block}
    </style>
</head>
<body>
    <h1>${title}</h1>
    <p>Rendered by ${renderer} (mvn test). Links open standalone pages.</p>
    <p class="hint">serve over http (module scripts are blocked from file:// origins):
        <code>python3 -m http.server -d target/${outputDirectory}</code></p>
    <div>
        <#list pages as page>
            <div class="cell">
                <div><a href="${page}.html" target="_blank">${page}</a></div>
                <iframe src="${page}.html" loading="lazy"></iframe>
            </div>
        </#list>
    </div>
</body>
</html>
