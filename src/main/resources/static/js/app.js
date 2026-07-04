// app.js - Shared utilities
(function() {
    window.getCsrfToken = function() {
        var match = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
        if (match) return decodeURIComponent(match[1]);
        var meta = document.querySelector('meta[name="csrf-token"]');
        if (meta) return meta.getAttribute('content');
        return window._csrfToken || '';
    };

    window.loadCsrfToken = function() {
        return fetch('/csrf').then(function(r) { return r.json(); }).then(function(data) {
            if (data && data.token) {
                var meta = document.createElement('meta');
                meta.name = 'csrf-token';
                meta.content = data.token;
                document.head.appendChild(meta);
            }
        }).catch(function() {});
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

    window.initPage = function() {
        // Add footer
        var footer = document.createElement('footer');
        footer.style.cssText = 'text-align:center;padding:1.5rem 0;color:#999;font-size:0.8rem;border-top:1px solid #eee;margin-top:2rem;clear:both;';
        footer.innerHTML = '&copy; 2026 <a href="/" style="color:#888;text-decoration:none;">ContactHub</a> &mdash; Made with &#10084; by <a href="#" style="color:#888;text-decoration:none;">yabo</a>';
        document.body.appendChild(footer);

        loadCsrfToken().then(function() {
            ajaxSetup();
            loadI18n().then(function() {
                fetch('/api/v1/user/current')
                    .then(function(r) { return r.json(); })
                    .then(function(r) {
                        if (r.code === 200 && r.data && r.data.isAdmin) {
                            var el = document.getElementById('navAdmin');
                            if (el) el.style.display = '';
                        }
                    })
                    .catch(function() {});
            });
        });
    };
})();
