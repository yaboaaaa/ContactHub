// app.js - Shared utilities (no navbar rendering - navbar is inline in each page)
(function() {
    window.getCsrfToken = function() {
        var match = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
        return match ? decodeURIComponent(match[1]) : '';
    };

    window.ajaxSetup = function() {
        if (typeof $ === 'undefined') return;
        $.ajaxSetup({
            beforeSend: function(xhr) {
                var token = getCsrfToken();
                if (token) xhr.setRequestHeader('X-XSRF-TOKEN', token);
            }
        });
    };

    window.escapeHtml = function(text) {
        if (!text) return '';
        var div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    };

    window.showAlert = function(message, type) {
        var container = document.createElement('div');
        container.style.cssText = 'position:fixed;top:1rem;right:1rem;z-index:9999;max-width:360px;';
        var alert = document.createElement('div');
        alert.className = 'alert alert-' + type + ' alert-dismissible fade show';
        alert.style.cssText = 'border-radius:8px;font-size:0.875rem;box-shadow:0 4px 12px rgba(0,0,0,0.15);';
        alert.innerHTML = message + '<button type="button" class="btn-close" data-bs-dismiss="alert"></button>';
        container.appendChild(alert);
        document.body.appendChild(container);
        setTimeout(function() { if (container.parentNode) container.parentNode.removeChild(container); }, 3000);
    };

    // Called by each page to: load i18n, show admin menu, setup ajax
    window.initPage = function() {
        ajaxSetup();
        loadI18n().then(function() {
            fetch('/api/user/current')
                .then(function(r) { return r.json(); })
                .then(function(r) {
                    if (r.code === 200 && r.data && r.data.isAdmin) {
                        var el = document.getElementById('navAdmin');
                        if (el) el.style.display = '';
                    }
                })
                .catch(function() {});
        });
    };
})();
