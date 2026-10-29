/**
 * ប្រព័ន្ធគ្រប់គ្រងថ្នាក់រៀន ២០២៧
 * ផ្ទាំងបញ្ជាក់ការចាកចេញពីប្រព័ន្ធ (Logout Confirmation Modal)
 * ដំណើរការស្វ័យប្រវត្តិលើគ្រប់ទំព័រទាំងគណនី Admin និងគណនីគ្រូ
 */
(function() {
    // 1. បង្កើតស្ទីលតុបតែងផ្ទាំង Modal ឱ្យមានសោភ័ណភាពស្រស់ស្អាត
    var style = document.createElement('style');
    style.id = 'logout-confirm-style';
    style.textContent = `
        .swal2-popup.swal-logout-popup {
            font-family: 'Kantumruy Pro', 'Battambang', sans-serif !important;
            border-radius: 20px !important;
            padding: 24px 20px !important;
            box-shadow: 0 20px 40px rgba(15, 23, 42, 0.25) !important;
            border: 1px solid #e2e8f0 !important;
            max-width: 440px !important;
        }
        .swal-logout-title {
            font-size: 1.35rem !important;
            font-weight: 800 !important;
            color: #0f172a !important;
            margin-top: 10px !important;
            letter-spacing: 0.2px !important;
        }
        .swal-logout-text {
            font-size: 0.96rem !important;
            color: #475569 !important;
            line-height: 1.6 !important;
        }
        .swal-logout-confirm {
            background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%) !important;
            color: white !important;
            font-weight: 700 !important;
            font-size: 0.95rem !important;
            padding: 10px 22px !important;
            border-radius: 10px !important;
            box-shadow: 0 4px 12px rgba(239, 68, 68, 0.35) !important;
            border: none !important;
            cursor: pointer !important;
            transition: all 0.2s ease !important;
        }
        .swal-logout-confirm:hover {
            transform: translateY(-1px) !important;
            box-shadow: 0 6px 16px rgba(239, 68, 68, 0.45) !important;
        }
        .swal-logout-cancel {
            background: #f1f5f9 !important;
            color: #334155 !important;
            font-weight: 700 !important;
            font-size: 0.95rem !important;
            padding: 10px 20px !important;
            border-radius: 10px !important;
            border: 1.5px solid #cbd5e1 !important;
            cursor: pointer !important;
            transition: all 0.2s ease !important;
        }
        .swal-logout-cancel:hover {
            background: #e2e8f0 !important;
            color: #0f172a !important;
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

    // 3. អនុវត្តការបង្ហាញផ្ទាំងបញ្ជាក់នៅពេលចុច Logout
    function triggerLogoutConfirm(logoutUrl) {
        ensureSweetAlert(function() {
            var isStudent = logoutUrl.indexOf('/test/logout') !== -1;
            var title = isStudent ? 'ចាកចេញពីគណនីសិស្ស?' : 'ចាកចេញពីប្រព័ន្ធ?';
            var html = isStudent
                ? '<div class="swal-logout-text">តើប្អូនពិតជាចង់<b>ចាកចេញពីប្រព័ន្ធប្រឡង</b>មែនទេ?</div>'
                : '<div class="swal-logout-text">តើលោកគ្រូ/អ្នកគ្រូពិតជាចង់<b>ចាកចេញពីប្រព័ន្ធ</b>មែនទេ?<br><span style="font-size: 0.85rem; color: #94a3b8; display: inline-block; margin-top: 4px;">(សូមរក្សាទុកទិន្នន័យដែលកំពុងធ្វើ មុនពេលចាកចេញ)</span></div>';

            Swal.fire({
                title: title,
                html: html,
                icon: 'warning',
                iconColor: '#f59e0b',
                showCancelButton: true,
                confirmButtonText: '🚪 បាទ/ចាស, ចាកចេញ',
                cancelButtonText: 'បោះបង់ (នៅបន្ត)',
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
