// CMS App (PWA) Installer v3 — Mobile Standalone Screen-Fit + Smart Installer (Android, iOS, Desktop)
(function () {
    // 1. Ensure clean mobile viewport & standalone fit CSS on EVERY page of CMS App
    (function applyMobileAppFit() {
        let vp = document.querySelector('meta[name="viewport"]');
        if (!vp) {
            vp = document.createElement('meta');
            vp.name = 'viewport';
            document.head.appendChild(vp);
        }
        vp.content = 'width=device-width, initial-scale=1.0, viewport-fit=cover';

        const isStandaloneNow = window.matchMedia('(display-mode: standalone)').matches
            || window.navigator.standalone === true;

        if (!document.getElementById('cmsPwaMobileFit')) {
            const st = document.createElement('style');
            st.id = 'cmsPwaMobileFit';
            st.textContent = `
                html { -webkit-text-size-adjust: 100%; max-width: 100vw; overflow-x: hidden; }
                body { max-width: 100vw; overflow-x: hidden; }
                @media all and (display-mode: standalone) {
                    html, body {
                        width: 100% !important;
                        max-width: 100vw !important;
                        overflow-x: hidden !important;
                        min-height: 100dvh;
                    }
                    body {
                        padding-bottom: env(safe-area-inset-bottom, 0px);
                    }
                    .topbar, header.topbar {
                        padding-top: max(10px, env(safe-area-inset-top, 10px)) !important;
                    }
                    .main-content, .content, .container, .panel, .card, .login-card {
                        max-width: 100% !important;
                        box-sizing: border-box !important;
                    }
                }
                @media screen and (max-width: 768px) {
                    .login-card {
                        width: calc(100% - 28px) !important;
                        max-width: 400px !important;
                        padding: 26px 20px !important;
                        margin: 14px auto !important;
                        box-sizing: border-box !important;
                    }
                    input[type="text"], input[type="password"], input[type="number"], input[type="date"], select, textarea {
                        font-size: 16px;
                    }
                }
            `;
            document.head.appendChild(st);
        }

        if (isStandaloneNow) {
            document.documentElement.classList.add('cms-standalone');
            document.addEventListener('DOMContentLoaded', () => {
                if (document.body) document.body.classList.add('cms-standalone');
            });
        }
    })();

    // Register Service Worker immediately (don't wait for window.load so Android Chrome fires beforeinstallprompt faster!)
    if ('serviceWorker' in navigator) {
        navigator.serviceWorker.register('/sw.js', { scope: '/' }).catch(() => {});
    }

    const isStandalone = window.matchMedia('(display-mode: standalone)').matches
        || window.navigator.standalone === true;
    if (isStandalone) return; // Already running inside the installed CMS App!

    let deferredPrompt = null;
    const ua = navigator.userAgent || '';
    const isIos = /iphone|ipad|ipod/i.test(ua);
    const isAndroid = /android/i.test(ua);
    const isInAppBrowser = /FBAN|FBAV|Instagram|Telegram|Line\/|MicroMessenger|Messenger|TikTok/i.test(ua)
        || (isAndroid && /; wv\)/i.test(ua));
    const isHttpsOrLocal = (location.protocol === 'https:' || location.hostname === 'localhost' || location.hostname === '127.0.0.1');

    function createInstallPill(isNativeReady) {
        if (sessionStorage.getItem('cmsPwaDismissed') === '1' && !isNativeReady) return;
        let wrap = document.getElementById('cmsPwaInstallWrap');
        if (!wrap) {
            wrap = document.createElement('div');
            wrap.id = 'cmsPwaInstallWrap';
            wrap.className = 'no-print';
            wrap.style.cssText = [
                'position:fixed',
                'right:14px',
                'bottom:calc(14px + env(safe-area-inset-bottom, 0px))',
                'z-index:9999',
                'display:inline-flex',
                'align-items:center',
                'gap:6px',
                'background:linear-gradient(135deg,#1e3a8a 0%,#2563eb 100%)',
                'color:#fff',
                'padding:9px 12px 9px 14px',
                'border-radius:999px',
                'box-shadow:0 8px 24px rgba(30,58,138,0.42)',
                'border:1.5px solid rgba(255,255,255,0.4)',
                'font-family:"Kantumruy Pro","Battambang",sans-serif',
                'font-size:0.88rem',
                'font-weight:700',
                'cursor:pointer',
                'user-select:none',
                '-webkit-tap-highlight-color:transparent'
            ].join(';');

            const btn = document.createElement('span');
            btn.id = 'cmsPwaInstallBtn';
            btn.style.cssText = 'display:inline-flex;align-items:center;gap:7px;';
            btn.addEventListener('click', handleInstallClick);

            const closeBtn = document.createElement('span');
            closeBtn.textContent = '×';
            closeBtn.title = 'បិទ';
            closeBtn.style.cssText = 'margin-left:4px;padding:0 5px;font-size:1.15rem;line-height:1;opacity:0.85;border-left:1px solid rgba(255,255,255,0.3);';
            closeBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                sessionStorage.setItem('cmsPwaDismissed', '1');
                wrap.remove();
            });

            wrap.appendChild(btn);
            wrap.appendChild(closeBtn);
            document.body.appendChild(wrap);
        }

        const btn = document.getElementById('cmsPwaInstallBtn');
        if (btn) {
            if (isNativeReady) {
                wrap.style.background = 'linear-gradient(135deg,#059669 0%,#10b981 100%)';
                btn.innerHTML = '<img src="/icons/icon-192.png" alt="CMS" style="width:22px;height:22px;border-radius:5px;"> <span>📲 តម្លើង App CMS ឥឡូវនេះ</span>';
            } else {
                btn.innerHTML = '<img src="/icons/icon-192.png" alt="CMS" style="width:22px;height:22px;border-radius:5px;"> <span>📲 តម្លើង App (CMS)</span>';
            }
        }
    }

    function copyCurrentUrl() {
        const url = window.location.origin + '/login';
        if (navigator.clipboard && navigator.clipboard.writeText) {
            navigator.clipboard.writeText(url).then(() => {
                if (typeof Swal !== 'undefined') Swal.fire({ icon: 'success', title: 'បាន Copy Link រួចរាល់!', text: 'សូមបើកកម្មវិធី Chrome ឬ Safari រួច Paste Link នេះដើម្បីតម្លើង App CMS', timer: 2200, showConfirmButton: false });
            }).catch(() => {});
        }
    }
    window.copyCmsAppUrl = copyCurrentUrl;

    function showManualInstallGuide() {
        let html = '';
        if (isInAppBrowser) {
            html = `<div style="text-align:left;line-height:1.8;font-size:0.95rem;">
                <div style="background:#fef2f2;border:1px solid #fecaca;color:#991b1b;padding:10px 12px;border-radius:8px;margin-bottom:10px;font-weight:700;">
                    ⚠️ អ្នកកំពុងបើក Link នៅក្នុង Telegram / Facebook — មិនអាចតម្លើង App ផ្ទាល់បានទេ!
                </div>
                <div>១. ចុចម៉ឺនុយ <b>⋮</b> ឬ <b>🧭</b> នៅជ្រុងខាងស្តាំលើ (ឬខាងក្រោម)</div>
                <div>២. ជ្រើសរើស <b>«Open in Chrome»</b> (Android) ឬ <b>«Open in Safari»</b> (iPhone)</div>
                <div>៣. បន្ទាប់មកចុចប៊ូតុង <b>«📲 តម្លើង App CMS»</b> ម្តងទៀត នឹងតម្លើងបានភ្លាមៗ!</div>
                <div style="margin-top:12px;text-align:center;">
                    <button type="button" onclick="window.copyCmsAppUrl()" style="background:#2563eb;color:#fff;border:none;padding:9px 16px;border-radius:8px;font-weight:700;cursor:pointer;font-family:inherit;">📋 Copy Link ទៅបើកក្នុង Chrome/Safari</button>
                </div>
            </div>`;
        } else if (isIos) {
            html = `<div style="text-align:left;line-height:1.85;font-size:0.95rem;">
                <div style="margin-bottom:8px;font-weight:700;color:#1e3a8a;">🍎 របៀបតម្លើង App <b>CMS</b> លើ iPhone / iPad (iOS)៖</div>
                <div style="background:#eff6ff;border:1px solid #bfdbfe;padding:10px 12px;border-radius:8px;">
                    <div><b>ជំហានទី ១៖</b> បើកគេហទំព័រនេះក្នុង <b>Safari</b> (មិនមែនក្នុង Telegram ទេ)</div>
                    <div><b>ជំហានទី ២៖</b> ចុចប៊ូតុង <b>Share (⬆️)</b> នៅផ្នែកខាងក្រោមអេក្រង់</div>
                    <div><b>ជំហានទី ៣៖</b> អូសចុះក្រោម ចុច <b>«Add to Home Screen» (➕)</b></div>
                    <div><b>ជំហានទី ៤៖</b> ចុច <b>«Add»</b> ជាការស្រេច — រូប <b>CMS</b> នឹងបង្ហាញលើអេក្រង់!</div>
                </div>
            </div>`;
        } else if (isAndroid) {
            html = `<div style="text-align:left;line-height:1.85;font-size:0.95rem;">
                <div style="margin-bottom:8px;font-weight:700;color:#1e3a8a;">🤖 របៀបតម្លើង App <b>CMS</b> លើទូរស័ព្ទ Android៖</div>
                ${!isHttpsOrLocal ? '<div style="background:#fef2f2;border:1px solid #fecaca;color:#991b1b;padding:8px 10px;border-radius:8px;margin-bottom:8px;font-size:0.86rem;">⚠️ សូមបើកតាម Link <b>https://</b> (ឧ. Render / Domain ផ្លូវការ) ទើប Android អនុញ្ញាតឱ្យតម្លើង App!</div>' : ''}
                <div style="background:#f0fdf4;border:1px solid #bbf7d0;padding:10px 12px;border-radius:8px;">
                    <div><b>ជំហានទី ១៖</b> បើក Link នេះក្នុងកម្មវិធី <b>Google Chrome</b></div>
                    <div><b>ជំហានទី ២៖</b> ចុចម៉ឺនុយ <b>⋮ (ចំណុច ៣)</b> នៅជ្រុងខាងស្តាំលើ</div>
                    <div><b>ជំហានទី ៣៖</b> ចុច <b>«Add to Home screen»</b> ឬ <b>«Install app»</b></div>
                    <div><b>ជំហានទី ៤៖</b> ចុច <b>«Install»</b> ជាការស្រេច!</div>
                </div>
            </div>`;
        } else {
            html = `<div style="text-align:left;line-height:1.8;font-size:0.95rem;">
                <div style="margin-bottom:8px;font-weight:700;color:#1e3a8a;">💻 របៀបតម្លើង App <b>CMS</b> លើកុំព្យូទ័រ៖</div>
                <div>• ចុចរូបតំណាង <b>⊕ / 💻</b> នៅខាងស្តាំរបារ Address bar ខាងលើ (Chrome / Edge) → ចុច <b>«Install»</b></div>
            </div>`;
        }

        if (typeof Swal !== 'undefined') {
            Swal.fire({
                title: '📲 តម្លើងកម្មវិធី CMS',
                html: html,
                confirmButtonText: 'យល់ព្រម',
                confirmButtonColor: '#2563eb'
            });
        } else {
            alert('ដើម្បីតម្លើង App CMS៖\n- Android (Chrome): ចុច ⋮ ខាងស្តាំលើ -> Install app / Add to Home screen\n- iPhone/iPad (Safari): ចុច Share (⬆️) -> Add to Home Screen -> Add');
        }
    }

    async function handleInstallClick() {
        if (deferredPrompt) {
            deferredPrompt.prompt();
            try {
                const choice = await deferredPrompt.userChoice;
                if (choice && choice.outcome === 'accepted') {
                    const wrap = document.getElementById('cmsPwaInstallWrap');
                    if (wrap) wrap.remove();
                }
            } catch (e) {}
            deferredPrompt = null;
        } else {
            showManualInstallGuide();
        }
    }

    window.addEventListener('beforeinstallprompt', (e) => {
        e.preventDefault();
        deferredPrompt = e;
        createInstallPill(true);
    });

    window.addEventListener('appinstalled', () => {
        deferredPrompt = null;
        const wrap = document.getElementById('cmsPwaInstallWrap');
        if (wrap) wrap.remove();
    });

    document.addEventListener('DOMContentLoaded', () => {
        setTimeout(() => {
            if (!isStandalone) createInstallPill(!!deferredPrompt);
        }, 1200);
    });

    window.triggerCmsAppInstall = handleInstallClick;
})();
