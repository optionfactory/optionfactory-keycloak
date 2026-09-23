/*
 * Rewrites a phone field into canonical E.164 so the `phonenumber` validator, which only accepts
 * that form, refuses almost nobody. Opt in from the user profile, on the attribute itself:
 *
 *     "annotations": { "kc-phone": "+39" }
 *
 * keycloak renders every `kc*` annotation as a data attribute, so that becomes
 * `data-kc-phone="+39"` on the input. The value is the international prefix assumed when the user
 * writes a national number; leave it empty to assume none.
 *
 * This is convenience, never the rule: the field is also written by the admin api and by
 * provisioning, where no browser runs, so the server stays the one that decides.
 */
(function () {
    "use strict";

    var SEPARATORS = /[\s(). ‐-―-]/g;

    function normalize(value, prefix) {
        var digits = value.replace(SEPARATORS, "");
        if (digits === "") {
            return "";
        }
        if (digits.indexOf("00") === 0) {
            digits = "+" + digits.slice(2);
        }
        if (digits.charAt(0) !== "+" && prefix) {
            digits = prefix + digits;
        }
        return digits;
    }

    function rewrite(input) {
        var normalized = normalize(input.value, input.getAttribute("data-kc-phone"));
        if (normalized !== input.value) {
            input.value = normalized;
        }
    }

    function fields() {
        return document.querySelectorAll("input[data-kc-phone]");
    }

    document.addEventListener("DOMContentLoaded", function () {
        var inputs = fields();
        for (var i = 0; i < inputs.length; i++) {
            inputs[i].addEventListener("blur", function (event) {
                rewrite(event.target);
            });
            // submitting without leaving the field never fires blur, and that is the one case
            // where a stray space would reach the server
            if (inputs[i].form) {
                inputs[i].form.addEventListener("submit", function () {
                    var pending = fields();
                    for (var j = 0; j < pending.length; j++) {
                        rewrite(pending[j]);
                    }
                });
            }
        }
    });
})();
