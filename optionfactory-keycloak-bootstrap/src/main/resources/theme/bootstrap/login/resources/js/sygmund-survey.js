/* global ftl, ful */

$(async function () {


    const ee = new ftl.ExpressionEvaluator({});
    const tne = new ftl.TextNodeExpressionEvaluator(ee);
    const ch = new ftl.TplCommandsHandler();

    const evaluation = {
        evaluator: ee,
        textNodeEvaluator: tne,
        commandsHandler: ch
    };

    const bindings = new ful.Bindings({
        extractors: {
        },
        mutators: {
        }
    });

    const wizard = new ful.Wizard(document.querySelector('[data-ref=survey]'));

    $('[data-ref=prev]').on('click', function () {
        wizard.prev();
    });

    $('[data-ref=carousel] [data-ref=next]').on('click', function () {
        wizard.next();
    });

    $('[data-ref=info] [data-ref=next]').on('click', function () {
        const section = this.closest('section');
        const form = new ful.Form(section, bindings, {});
        const values = form.getValues();
        if (!values.name || values.name.length === 0) {
            form.setErrors([{type: 'FIELD_ERROR', context: 'name', reason: 'Il campo è obbligatorio'}]);
            return;
        }
        wizard.next();
    });
    $('[data-ref=age] [data-ref=next]').on('click', function () {
        const section = this.closest('section');
        const form = new ful.Form(section, bindings, {
            globalErrorsEl: section.querySelector('.alert')
        });
        const values = form.getValues();
        if (!values.age) {
            form.setErrors([{type: 'GENERIC_ERROR', context: 'age', reason: 'Il campo è obbligatorio'}]);
            return;
        }
        if (values.age === 'AGE_0_18') {
            form.setErrors([{type: 'GENERIC_ERROR', context: 'age', reason: 'Ci spiace! Non possiamo proseguire con il nostro questionario in caso di minorenni in quanto per legge abbiamo bisogno del consenso di entrambi i genitori. Ti invitiamo a scriverci una mail all\'indirizzo <a href="mailto:support@sygmund.it">support@sygmund.it</a>. A tua disposizione!'}]);
            return;
        }
        wizard.next();
    });
    $('[data-ref=topics] [data-ref=next]').on('click', function () {
        const section = this.closest('section');
        const form = new ful.Form(section, bindings, {
            globalErrorsEl: section.querySelector('.alert')
        });
        const values = form.getValues();
        if (Object.values(values.topics).filter(v => v).length === 0) {
            form.setErrors([{type: 'GENERIC_ERROR', context: 'topics', reason: 'Il campo è obbligatorio'}]);
            return;
        }
        wizard.next();

    });
    $('[data-ref=mood] [data-ref=next]').on('click', function () {
        const section = this.closest('section');
        wizard.next();
    });
    $('[data-ref=region] [data-ref=next]').on('click', function () {
        const section = this.closest('section');
        const form = new ful.Form(section, bindings, {});
        const values = form.getValues();
        if (!values.region) {
            form.setErrors([{type: 'FIELD_ERROR', context: 'region', reason: 'Il campo è obbligatorio'}]);
            return;
        }
        wizard.next();
    });
    $('[data-ref=language] [data-ref=next]').on('click', function () {
        const section = this.closest('section');
        const form = new ful.Form(section, bindings, {
            globalErrorsEl: section.querySelector('.alert')
        });
        const values = form.getValues();
        if (!values.language) {
            form.setErrors([{type: 'GENERIC_ERROR', context: 'age', reason: 'Il campo è obbligatorio'}]);
            return;
        }
        wizard.next();
    });
    $('[data-ref=sex] [data-ref=next]').on('click', function () {
        const section = this.closest('section');
        const form = new ful.Form(section, bindings, {
            globalErrorsEl: section.querySelector('.alert')
        });
        const values = form.getValues();
        if (!values.sex) {
            form.setErrors([{type: 'GENERIC_ERROR', context: 'sex', reason: 'Il campo è obbligatorio'}]);
            return;
        }
        wizard.next();
    });
    $('[data-ref=psex] [data-ref=next]').on('click', function () {
        const section = this.closest('section');
        const form = new ful.Form(section, bindings, {
            globalErrorsEl: section.querySelector('.alert')
        });
        const values = form.getValues();
        if (!values.psychologist.sex) {
            form.setErrors([{type: 'GENERIC_ERROR', context: 'psychologist.sex', reason: 'Il campo è obbligatorio'}]);
            return;
        }
        wizard.next();
    });
    $('[data-ref=page] [data-ref=next]').on('click', async function () {
        const section = this.closest('section');
        const form = new ful.Form(section, bindings, {
            globalErrorsEl: section.querySelector('.alert')
        });
        const values = form.getValues();
        if (!values.psychologist.age) {
            form.setErrors([{type: 'GENERIC_ERROR', context: 'psychologist.age', reason: 'Il campo è obbligatorio'}]);
            return;
        }
        const v = bindings.getValues(document.querySelector('[data-ref=survey]'));
        delete v.name;
        v.topics = Object.entries(v.topics).filter(([a, b]) => b).map(([a, b]) => a);
        v.tags = Object.entries(v.tags).filter(([a, b]) => b).map(([a, b]) => a);

        const s = JSON.stringify(v);
        $('form [name=survey]').val(s);
        $('form').submit();
    });


    $('[data-ref=topics] input').on('change', function () {
        console.log($(this).prop('id'));
        const checked = $(this).prop('checked');

        const next = this.closest('.input-group')?.nextElementSibling;
        if (!next || !next.matches('section')) {
            return;
        }
        next.classList[checked ? 'remove' : 'add']('d-none');
    });

    $('#topic-5').on('change', function () {
        $(this).prop('checked') && $('#topic-1,#topic-2,#topic-3,#topic-4').prop('checked', false).change();
    });
    $('#topic-1,#topic-2,#topic-3,#topic-4').on('change', function () {
        $(this).prop('checked') && $('#topic-5').prop('checked', false).change();
    });
});