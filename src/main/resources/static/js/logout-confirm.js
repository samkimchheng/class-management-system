/**
 * ប្រព័ន្ធគ្រប់គ្រងថ្នាក់រៀន ២០២៧
 * ផ្ទាំងបញ្ជាក់ការចាកចេញពីប្រព័ន្ធ (Logout Confirmation Modal)
 * រចនាពិសេសជាមួយអក្សរធំ ដិតច្បាស់ (High Contrast & Bold) ងាយស្រួលមើលសម្រាប់លោកគ្រូ-អ្នកគ្រូរៀមច្បង
 */
(function() {
    // 1. បង្កើតស្ទីលតុបតែងផ្ទាំង Modal ឱ្យមានអក្សរធំ ដិតច្បាស់ និង Fit យ៉ាងស្អាតលើអេក្រង់ទូរស័ព្ទដៃគ្រប់ប្រភេទ
    var style = document.createElement('style');
    style.id = 'logout-confirm-style';
    style.textContent = `
        /* ===== SweetAlert2 Base & Desktop Styling ===== */
        .swal2-container {
            font-family: 'Kantumruy Pro', 'Battambang', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif !important;
            -webkit-overflow-scrolling: touch !important;
        }
        .swal2-popup.swal-logout-popup {
            font-family: 'Kantumruy Pro', 'Battambang', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif !important;
            border-radius: 20px !important;
            padding: 26px 22px !important;
            box-shadow: 0 25px 50px rgba(15, 23, 42, 0.35) !important;
            border: 2px solid #cbd5e1 !important;
            width: min(92vw, 460px) !important;
            max-width: min(92vw, 460px) !important;
            box-sizing: border-box !important;
        }
        .swal-logout-title {
            font-size: 1.55rem !important;
            font-weight: 800 !important;
            color: #0f172a !important;
            margin-top: 10px !important;
            margin-bottom: 8px !important;
            letter-spacing: 0.2px !important;
            line-height: 1.4 !important;
            word-break: break-word !important;
        }
        .swal-logout-text {
            font-size: 1.12rem !important;
            font-weight: 700 !important;
            color: #1e293b !important;
            line-height: 1.7 !important;
            margin-top: 6px !important;
            word-break: break-word !important;
        }
        .swal-logout-text b {
            color: #b91c1c !important;
            font-weight: 800 !important;
            text-decoration: underline !important;
            text-underline-offset: 4px !important;
        }
        .swal-logout-note {
            font-size: 0.95rem !important;
            font-weight: 700 !important;
            color: #b45309 !important;
            background: #fef3c7 !important;
            border: 1.5px solid #fde68a !important;
            border-radius: 10px !important;
            padding: 8px 14px !important;
            margin-top: 10px !important;
            display: inline-block !important;
            line-height: 1.5 !important;
            word-break: break-word !important;
        }
        .swal-logout-confirm {
            background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%) !important;
            color: white !important;
            font-weight: 800 !important;
            font-size: 1.02rem !important;
            padding: 12px 22px !important;
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
            font-size: 1.02rem !important;
            padding: 12px 20px !important;
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

        /* Desktop button alignment */
        @media (min-width: 641px) {
            .swal2-actions {
                display: flex !important;
                flex-direction: row !important;
                gap: 12px !important;
                justify-content: center !important;
                margin-top: 20px !important;
            }
            .swal-logout-confirm,
            .swal-logout-cancel {
                min-width: 140px !important;
            }
        }

        /* ===== Universal SweetAlert2 Mobile Optimization (Fit All Phones) ===== */
        @media (max-width: 640px) {
            .swal2-container {
                padding: 12px 10px !important;
                box-sizing: border-box !important;
            }
            .swal2-popup,
            .swal2-popup.swal-logout-popup {
                width: calc(100vw - 24px) !important;
                max-width: calc(100vw - 24px) !important;
                margin: auto !important;
                padding: 22px 16px 18px 16px !important;
                border-radius: 18px !important;
                box-sizing: border-box !important;
                box-shadow: 0 16px 36px rgba(15, 23, 42, 0.25) !important;
            }
            .swal2-icon {
                transform: scale(0.85) !important;
                margin: 6px auto 10px auto !important;
            }
            .swal2-title,
            .swal-logout-title {
                font-size: clamp(1.15rem, 4.4vw, 1.45rem) !important;
                line-height: 1.45 !important;
                margin-top: 6px !important;
                margin-bottom: 8px !important;
                padding: 0 4px !important;
                word-break: break-word !important;
                overflow-wrap: break-word !important;
            }
            .swal2-html-container,
            .swal-logout-text {
                font-size: clamp(0.92rem, 3.6vw, 1.05rem) !important;
                line-height: 1.6 !important;
                margin: 6px auto 0 auto !important;
                padding: 0 4px !important;
                word-break: break-word !important;
                overflow-wrap: break-word !important;
            }
            .swal-logout-note {
                font-size: 0.88rem !important;
                padding: 6px 10px !important;
                margin-top: 8px !important;
                border-radius: 8px !important;
                display: block !important;
                word-break: break-word !important;
            }
            .swal2-actions {
                display: flex !important;
                flex-direction: column !important;
                width: 100% !important;
                gap: 10px !important;
                margin: 18px auto 0 auto !important;
                padding: 0 !important;
            }
            .swal-logout-confirm,
            .swal2-confirm {
                order: 1 !important;
                width: 100% !important;
                min-height: 48px !important;
                margin: 0 !important;
                padding: 12px 14px !important;
                font-size: 1rem !important;
                font-weight: 800 !important;
                border-radius: 12px !important;
                box-sizing: border-box !important;
                display: flex !important;
                align-items: center !important;
                justify-content: center !important;
                text-align: center !important;
                white-space: normal !important;
                line-height: 1.35 !important;
            }
            .swal-logout-cancel,
            .swal2-cancel {
                order: 2 !important;
                width: 100% !important;
                min-height: 48px !important;
                margin: 0 !important;
                padding: 12px 14px !important;
                font-size: 1rem !important;
                font-weight: 800 !important;
                border-radius: 12px !important;
                box-sizing: border-box !important;
                display: flex !important;
                align-items: center !important;
                justify-content: center !important;
                text-align: center !important;
                white-space: normal !important;
                line-height: 1.35 !important;
            }
            .swal2-deny {
                order: 3 !important;
                width: 100% !important;
                min-height: 48px !important;
                margin: 0 !important;
            }
            .swal2-input,
            .swal2-textarea,
            .swal2-select {
                width: 100% !important;
                box-sizing: border-box !important;
                margin: 8px 0 !important;
                font-size: 0.98rem !important;
                border-radius: 10px !important;
            }
        }

        /* Extra narrow screens (<= 360px: iPhone SE, older Android) */
        @media (max-width: 360px) {
            .swal2-popup,
            .swal2-popup.swal-logout-popup {
                width: calc(100vw - 16px) !important;
                max-width: calc(100vw - 16px) !important;
                padding: 16px 10px 14px 10px !important;
            }
            .swal2-title,
            .swal-logout-title {
                font-size: 1.15rem !important;
            }
            .swal2-html-container,
            .swal-logout-text {
                font-size: 0.88rem !important;
            }
            .swal-logout-confirm,
            .swal-logout-cancel,
            .swal2-confirm,
            .swal2-cancel {
                min-height: 44px !important;
                font-size: 0.92rem !important;
                padding: 10px 8px !important;
            }
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
