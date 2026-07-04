// i18n.js - Load translations from API and apply to DOM
(function() {
    window.i18nData = {};

    function getCookie(name) {
        var match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
        return match ? decodeURIComponent(match[2]) : '';
    }

    function applyTranslations(messages) {
        if (!messages) return;
        window.i18nData = messages;
        document.querySelectorAll('[data-i18n]').forEach(function(el) {
            var key = el.getAttribute('data-i18n');
            if (messages[key]) {
                el.textContent = messages[key];
            }
        });
        document.querySelectorAll('[data-i18n-placeholder]').forEach(function(el) {
            var key = el.getAttribute('data-i18n-placeholder');
            if (messages[key]) {
                el.placeholder = messages[key];
            }
        });
        document.querySelectorAll('[data-i18n-title]').forEach(function(el) {
            var key = el.getAttribute('data-i18n-title');
            if (messages[key]) {
                el.title = messages[key];
            }
        });
    }

    window.t = function(key, fallback) {
        return window.i18nData[key] || fallback || key;
    };

    window.loadI18n = function() {
        return fetch('/api/i18n')
            .then(function(res) { return res.json(); })
            .then(function(result) {
                if (result.code === 200 && result.data) {
                    applyTranslations(result.data);
                }
            })
            .catch(function() {
                // Fallback to hardcoded defaults in HTML
            });
    };

    window.setLang = function(lang) {
        document.cookie = 'lang=' + lang + ';path=/;max-age=31536000';
        location.reload();
    };
})();
