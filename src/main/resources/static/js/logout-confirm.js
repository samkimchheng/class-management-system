/**
 * ប្រព័ន្ធគ្រប់គ្រងថ្នាក់រៀន ២០២៧
 * ផ្ទាំងបញ្ជាក់ការចាកចេញពីប្រព័ន្ធ (Logout Confirmation Modal)
 * រចនាពិសេសជាមួយអក្សរធំ ដិតច្បាស់ (High Contrast & Bold) ងាយស្រួលមើលសម្រាប់លោកគ្រូ-អ្នកគ្រូរៀមច្បង
 */
(function() {
    // 1. បង្កើតស្ទីលតុបតែងផ្ទាំង Modal ឱ្យមានអក្សរធំ ដិតច្បាស់ និងពណ៌ដិតខ្មៅងាយស្រួលអាន
    var style = document.createElement('style');
    style.id = 'logout-confirm-style';
    style.textContent = `
        .swal2-popup.swal-logout-popup {
            font-family: 'Kantumruy Pro', 'Battambang', sans-serif !important;
            border-radius: 20px !important;
            padding: 28px 22px !important;
            box-shadow: 0 25px 50px rgba(15, 23, 42, 0.35) !important;
            border: 2px solid #cbd5e1 !important;
            max-width: 500px !important;
        }
        .swal-logout-title {
            font-size: 1.6rem !important;
            font-weight: 800 !important;
            color: #0f172a !important;
            margin-top: 12px !important;
            letter-spacing: 0.3px !important;
        }
        .swal-logout-text {
            font-size: 1.15rem !important;
            font-weight: 700 !important;
            color: #0f172a !important;
            line-height: 1.75 !important;
            margin-top: 8px !important;
        }
        .swal-logout-text b {
            color: #b91c1c !important;
            font-weight: 800 !important;
            text-decoration: underline !important;
            text-underline-offset: 4px !important;
        }
        .swal-logout-note {
            font-size: 1rem !important;
            font-weight: 700 !important;
            color: #b45309 !important;
            background: #fef3c7 !important;
            border: 1.5px solid #fde68a !important;
            border-radius: 10px !important;
            padding: 8px 14px !important;
            margin-top: 12px !important;
            display: inline-block !important;
            line-height: 1.5 !important;
        }
        .swal-logout-confirm {
            background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%) !important;
            color: white !important;
            font-weight: 800 !important;
            font-size: 1.05rem !important;
            padding: 12px 26px !important;
            border-radius: 12px !important;
            box-shadow: 0 4px 14px rgba(239, 68, 68, 0.4) !important;
            border: none !important;
            cursor: pointer !important;
            transition: all 0.2s ease !important;
        }
        .swal-logout-confirm:hover {
            transform: translateY(-2px) !important;
            box-shadow: 0 6px 20px rgba(239, 68, 68, 0.5) !important;
        }
        .swal-logout-cancel {
            background: #f8fafc !important;
            color: #0f172a !important;
            font-weight: 800 !important;
            font-size: 1.05rem !important;
            padding: 12px 24px !important;
            border-radius: 12px !important;
            border: 2px solid #94a3b8 !important;
            cursor: pointer !important;
            transition: all 0.2s ease !important;
        }
        .swal-logout-cancel:hover {
            background: #e2e8f0 !important;
            border-color: #64748b !important;
            color: #000000 !important;
        }
        .swal2-icon.swal2-warning {
            border-color: #f59e0b !important;
            color: #d97706 !important;
        }
    `;
    if (!document.getElementById('logout-confirm-style')) {
        document.head.appendChild(style);
    }

    // 2. ពិនិត្យ និងផ្ទុក SweetAlert2 ប្រសិនបើទំព័រនោះមិនទាន់មាន
    function ensureSweetAlert(callback) {
        if (typeof Swal !== 'undefined') {
            callback();
            return;
        }
        var existingScript = document.querySelector('script[src*="sweetalert2"]');
        if (existingScript) {
            existingScript.addEventListener('load', callback);
            return;
        }
        var script = document.createElement('script');
        script.src = 'https://cdn.jsdelivr.net/npm/sweetalert2@11';
        script.onload = callback;
        document.head.appendChild(script);
    }

    // 2b. ធានាថា mobile-nav.js ត្រូវបានផ្ទុកដំណើរការលើគ្រប់ទំព័រ (Auto-Fit Android & iOS)
    if (!document.getElementById('mobileNavScript') && !document.querySelector('script[src*="mobile-nav.js"]')) {
        var navScript = document.createElement('script');
        navScript.id = 'mobileNavScript';
        navScript.src = '/js/mobile-nav.js';
        document.head.appendChild(navScript);
    }

    // 3. អនុវត្តការបង្ហាញផ្ទាំងបញ្ជាក់នៅពេលចុច Logout
    function triggerLogoutConfirm(logoutUrl) {
        ensureSweetAlert(function() {
            var isStudent = logoutUrl.indexOf('/test/logout') !== -1;
            var title = isStudent ? 'ចាកចេញពីគណនីសិស្ស?' : 'ចាកចេញពីប្រព័ន្ធ?';
            var html = isStudent
                ? '<div class="swal-logout-text">តើប្អូនពិតជាចង់ <b>ចាកចេញពីប្រព័ន្ធប្រឡង</b> មែនទេ?</div>'
                : '<div class="swal-logout-text">' +
                  'តើលោកគ្រូ/អ្នកគ្រូពិតជាចង់ <b>ចាកចេញពីប្រព័ន្ធ</b> មែនទេ?<br>' +
                  '<div class="swal-logout-note">⚠️ សូមរក្សាទុកទិន្នន័យដែលកំពុងធ្វើ មុនពេលចាកចេញ!</div>' +
                  '</div>';

            Swal.fire({
                title: title,
                html: html,
                icon: 'warning',
                iconColor: '#f59e0b',
                showCancelButton: true,
                confirmButtonText: '🚪 បាទ/ចាស, ចាកចេញ',
                cancelButtonText: '↩️ បោះបង់ (នៅបន្ត)',
                reverseButtons: true,
                focusCancel: true,
                customClass: {
                    popup: 'swal-logout-popup',
                    title: 'swal-logout-title',
                    confirmButton: 'swal-logout-confirm',
                    cancelButton: 'swal-logout-cancel'
                }
            }).then(function(result) {
                if (result.isConfirmed) {
                    window.location.href = logoutUrl;
                }
            });
        });
    }

    // 4. ចាប់យកព្រឹត្តិការណ៍ចុច (Click Delegation) លើគ្រប់តំណភ្ជាប់ href="/logout" ឬ href="/test/logout"
    document.addEventListener('click', function(e) {
        var target = e.target.closest('a[href="/logout"], a[href="/test/logout"], a[href$="/logout"]');
        if (target) {
            e.preventDefault();
            var url = target.getAttribute('href');
            triggerLogoutConfirm(url);
        }
    });

    // 5. ផ្តល់ជា function សកលផងដែរសម្រាប់ហៅប្រើតាម onclick="confirmLogout(event)"
    window.confirmLogout = function(event) {
        if (event && event.preventDefault) event.preventDefault();
        var target = event ? (event.currentTarget || event.target) : null;
        var url = (target && target.getAttribute && target.getAttribute('href')) ? target.getAttribute('href') : '/logout';
        triggerLogoutConfirm(url);
        return false;
    };
})();
