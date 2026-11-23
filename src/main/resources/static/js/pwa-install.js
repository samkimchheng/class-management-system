// CMS App (PWA) Installer v5 — Universal Mobile Screen-Fit (Browser + Installed App on Android, iOS, Desktop)
(function () {
    // 1. Ensure clean mobile viewport & universal screen-fit CSS on EVERY page of CMS
    (function applyMobileAppFit() {
        let vp = document.querySelector('meta[name="viewport"]');
        if (!vp) {
            vp = document.createElement('meta');
            vp.name = 'viewport';
            document.head.appendChild(vp);
        }
        vp.content = 'width=device-width, initial-scale=1.0, maximum-scale=5.0, viewport-fit=cover';

        const isStandaloneNow = window.matchMedia('(display-mode: standalone)').matches
            || window.navigator.standalone === true;

        if (!document.getElementById('cmsPwaMobileFit')) {
            const st = document.createElement('style');
            st.id = 'cmsPwaMobileFit';
            st.textContent = `
                html {
                    -webkit-text-size-adjust: 100%;
                    width: 100%;
                    max-width: 100vw;
                    overflow-x: clip;
                }
                body {
                    width: 100%;
                    max-width: 100vw;
                    overflow-x: clip;
                }
                .cms-mob-table-scroll {
                    width: 100% !important;
                    max-width: 100% !important;
                    overflow-x: auto !important;
                    -webkit-overflow-scrolling: touch !important;
                }
                @media all and (display-mode: standalone) {
                    html, body {
                        width: 100% !important;
                        max-width: 100vw !important;
                        overflow-x: clip !important;
                        min-height: 100dvh;
                    }
                    body {
                        padding-bottom: env(safe-area-inset-bottom, 0px);
                    }
                    .topbar, header.topbar {
                        padding-top: max(10px, env(safe-area-inset-top, 10px)) !important;
                    }
                }
                @media screen and (max-width: 992px) {
                    html, body {
                        width: 100% !important;
                        max-width: 100vw !important;
                        overflow-x: clip !important;
                    }
                    .sidebar {
                        position: fixed !important;
                        top: 0 !important;
                        left: 0 !important;
                        bottom: 0 !important;
                        width: 275px !important;
                        max-width: 84vw !important;
                        height: 100dvh !important;
                        transform: translateX(-100%) !important;
                        transition: transform 0.28s ease !important;
                        z-index: 1050 !important;
                    }
                    .sidebar.mobile-open,
                    .sidebar.open {
                        transform: translateX(0) !important;
                    }
                    .main-content {
                        margin-left: 0 !important;
                        width: 100% !important;
                        max-width: 100vw !important;
                        min-width: 0 !important;
                        overflow-x: clip !important;
                        box-sizing: border-box !important;
                    }
                    .content, .container, .panel, .card, .table-container, .table-wrap {
                        max-width: 100% !important;
                        min-width: 0 !important;
                        box-sizing: border-box !important;
                    }
                    .topbar, header.topbar {
                        width: 100% !important;
                        max-width: 100vw !important;
                        box-sizing: border-box !important;
                        padding-left: 10px !important;
                        padding-right: 10px !important;
                    }
                    .topbar-left {
                        flex: 1 1 0% !important;
                        min-width: 0 !important;
                        overflow: hidden !important;
                    }
                    .topbar-left h1, .topbar-left h2, .topbar-left h3 {
                        overflow: hidden !important;
                        text-overflow: ellipsis !important;
                        white-space: nowrap !important;
                        max-width: 100% !important;
                        font-size: 0.94rem !important;
                    }
                    .mobile-menu-btn, .btn-hamburger {
                        display: inline-flex !important;
                        align-items: center !important;
                        justify-content: center !important;
                        width: 36px !important;
                        height: 36px !important;
                        min-width: 36px !important;
                        flex-shrink: 0 !important;
                        background: #f1f5f9 !important;
                        border: 1px solid #cbd5e1 !important;
                        border-radius: 8px !important;
                        color: #0f172a !important;
                        font-size: 1.2rem !important;
                        cursor: pointer !important;
                    }
                    .mobile-sidebar-backdrop {
                        position: fixed; inset: 0;
                        background: rgba(15, 23, 42, 0.58);
                        z-index: 1040; display: none;
                    }
                    .mobile-sidebar-backdrop.active { display: block !important; opacity: 1 !important; visibility: visible !important; }
                }
                @media screen and (max-width: 768px) {
                    .content {
                        padding: 12px 10px calc(24px + env(safe-area-inset-bottom, 0px)) !important;
                    }
                    .card-grid, .kpi-grid, .form-grid {
                        grid-template-columns: 1fr !important;
                    }
                    .login-card {
                        width: calc(100% - 28px) !important;
                        max-width: 400px !important;
                        padding: 26px 20px !important;
                        margin: 14px auto !important;
                        box-sizing: border-box !important;
                    }
                    input[type="text"], input[type="password"], input[type="number"], input[type="date"], select, textarea {
                        font-size: 16px;
                        max-width: 100%;
                    }
                    #cmsPwaInstallWrap {
                        max-width: calc(100vw - 24px) !important;
                        right: 12px !important;
                        font-size: 0.82rem !important;
                        padding: 8px 10px 8px 12px !important;
                    }
                }
                @media screen and (max-width: 576px) {
                    .btn-logout-header span:last-child {
                        display: none !important;
                    }
                    .btn-logout-header {
                        padding: 5px 8px !important;
                    }
                }
                /* Native-Style Mobile Bottom Navigation Bar (Matches Mitpukae App UI) */
                #cmsMobileBottomNav {
                    display: none;
                }
                @media screen and (max-width: 992px) {
                    body.cms-has-bottom-nav .content {
                        padding-bottom: calc(82px + env(safe-area-inset-bottom, 0px)) !important;
                    }
                    body.cms-has-bottom-nav #cmsPwaInstallWrap {
                        bottom: calc(74px + env(safe-area-inset-bottom, 0px)) !important;
                    }
                    #cmsMobileBottomNav {
                        display: flex !important;
                        position: fixed;
                        left: 0;
                        right: 0;
                        bottom: 0;
                        width: 100%;
                        max-width: 100vw;
                        background: #ffffff;
                        border-top: 1px solid #e2e8f0;
                        box-shadow: 0 -4px 18px rgba(15, 23, 42, 0.08);
                        padding: 6px 4px calc(6px + env(safe-area-inset-bottom, 0px)) 4px;
                        z-index: 1030;
                        align-items: stretch;
                        justify-content: space-around;
                        box-sizing: border-box;
                    }
                    .cms-bnav-item {
                        flex: 1 1 0%;
                        min-width: 0;
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                        justify-content: center;
                        gap: 3px;
                        padding: 4px 2px 3px;
                        text-decoration: none !important;
                        color: #64748b;
                        background: transparent;
                        border: none;
                        font-family: 'Kantumruy Pro', 'Battambang', sans-serif;
                        cursor: pointer;
                        position: relative;
                        -webkit-tap-highlight-color: transparent;
                        transition: color 0.15s ease, transform 0.12s ease;
                    }
                    .cms-bnav-item:active {
                        transform: scale(0.94);
                    }
                    .cms-bnav-item.active {
                        color: #1d4ed8 !important;
                        font-weight: 800;
                    }
                    .cms-bnav-item.active::before {
                        content: '';
                        position: absolute;
                        top: -6px;
                        left: 22%;
                        right: 22%;
                        height: 3px;
                        border-radius: 0 0 4px 4px;
                        background: #2563eb;
                    }
                    .cms-bnav-icon {
                        font-size: 1.32rem;
                        line-height: 1.15;
                        display: inline-flex;
                        align-items: center;
                        justify-content: center;
                        height: 26px;
                    }
                    .cms-bnav-label {
                        font-size: 0.72rem;
                        font-weight: 700;
                        line-height: 1.2;
                        white-space: nowrap;
                        overflow: hidden;
                        text-overflow: ellipsis;
                        max-width: 100%;
                    }
                }
                @media print {
                    #cmsMobileBottomNav { display: none !important; }
                }
            `;
            document.head.appendChild(st);
        }

        // Auto-wire mobile sidebar toggle, bottom navigation bar, and table scroll wrappers
        document.addEventListener('DOMContentLoaded', () => {
            try {
                const sidebar = document.querySelector('.sidebar');
                const topbar = document.querySelector('.topbar');

                const openCloseSidebar = (open) => {
                    if (!sidebar) return;
                    if (typeof window.toggleSidebar === 'function') {
                        window.toggleSidebar(open);
                        return;
                    }
                    let backdrop = document.querySelector('.mobile-sidebar-backdrop, .sidebar-overlay');
                    if (!backdrop) {
                        backdrop = document.createElement('div');
                        backdrop.className = 'mobile-sidebar-backdrop';
                        document.body.appendChild(backdrop);
                        backdrop.addEventListener('click', () => openCloseSidebar(false));
                    }
                    sidebar.classList.toggle('mobile-open', open);
                    sidebar.classList.toggle('open', open);
                    backdrop.classList.toggle('active', open);
                    backdrop.classList.toggle('show', open);
                };

                if (sidebar && topbar && !topbar.querySelector('.mobile-menu-btn, .btn-hamburger, .btn-menu-mob')) {
                    const btn = document.createElement('button');
                    btn.type = 'button';
                    btn.className = 'mobile-menu-btn';
                    btn.setAttribute('aria-label', 'បើកម៉ឺនុយ');
                    btn.textContent = '☰';
                    btn.addEventListener('click', (e) => {
                        e.stopPropagation();
                        const isOpen = sidebar.classList.contains('mobile-open') || sidebar.classList.contains('open');
                        openCloseSidebar(!isOpen);
                    });
                    const left = topbar.querySelector('.topbar-left') || topbar;
                    left.insertBefore(btn, left.firstChild);
                }

                // Inject Mobile Bottom Navigation Bar on all dashboard pages that have a .sidebar
                if (sidebar && !document.getElementById('cmsMobileBottomNav') && !document.body.classList.contains('is-student')) {
                    const p = window.location.pathname || '/';
                    const isAdminPage = p.startsWith('/admin');
                    const hasHomeroom = !!sidebar.querySelector('a[href="/homeroom"]');

                    let navItems;
                    if (isAdminPage) {
                        navItems = [
                            { href: '/admin', icon: '📊', label: 'ទំព័រដើម', match: (path) => path === '/admin' || path === '/admin/' },
                            { href: '/admin/teacher-activity', icon: '📈', label: 'តាមដានគ្រូ', match: (path) => path.startsWith('/admin/teacher-activity') },
                            { href: '/admin/accounts', icon: '👥', label: 'គណនីគ្រូ', match: (path) => path.startsWith('/admin/accounts') },
                            { href: '/admin/score-sheets', icon: '📋', label: 'ស្រង់ពិន្ទុ', match: (path) => path.startsWith('/admin/score-sheets') },
                            { action: 'menu', icon: '☰', label: 'ម៉ឺនុយ', match: () => false }
                        ];
                    } else if (hasHomeroom) {
                        navItems = [
                            { href: '/', icon: '📊', label: 'ទំព័រដើម', match: (path) => path === '/' },
                            { href: '/students', icon: '🎓', label: 'សិស្ស', match: (path) => path.startsWith('/students') },
                            { href: '/attendance', icon: '✅', label: 'វត្តមាន', match: (path) => path.startsWith('/attendance') },
                            { href: '/homeroom', icon: '📋', label: 'គ្រូបន្ទុក', match: (path) => path.startsWith('/homeroom') },
                            { action: 'menu', icon: '☰', label: 'ម៉ឺនុយ', match: () => false }
                        ];
                    } else {
                        navItems = [
                            { href: '/', icon: '📊', label: 'ទំព័រដើម', match: (path) => path === '/' },
                            { href: '/students', icon: '🎓', label: 'សិស្ស', match: (path) => path.startsWith('/students') },
                            { href: '/attendance', icon: '✅', label: 'វត្តមាន', match: (path) => path.startsWith('/attendance') },
                            { href: '/report', icon: '📋', label: 'របាយការណ៍', match: (path) => path.startsWith('/report') },
                            { action: 'menu', icon: '☰', label: 'ម៉ឺនុយ', match: () => false }
                        ];
                    }

                    const nav = document.createElement('nav');
                    nav.id = 'cmsMobileBottomNav';
                    nav.className = 'no-print';
                    nav.setAttribute('aria-label', 'ម៉ឺនុយខាងក្រោម');

                    navItems.forEach((item) => {
                        const isActive = item.match(p);
                        if (item.action === 'menu') {
                            const btn = document.createElement('button');
                            btn.type = 'button';
                            btn.className = 'cms-bnav-item';
                            btn.innerHTML = `<span class="cms-bnav-icon">${item.icon}</span><span class="cms-bnav-label">${item.label}</span>`;
                            btn.addEventListener('click', (e) => {
                                e.preventDefault();
                                e.stopPropagation();
                                const isOpen = sidebar.classList.contains('mobile-open') || sidebar.classList.contains('open');
                                openCloseSidebar(!isOpen);
                            });
                            nav.appendChild(btn);
                        } else {
                            const a = document.createElement('a');
                            a.href = item.href;
                            a.className = 'cms-bnav-item' + (isActive ? ' active' : '');
                            a.innerHTML = `<span class="cms-bnav-icon">${item.icon}</span><span class="cms-bnav-label">${item.label}</span>`;
                            nav.appendChild(a);
                        }
                    });

                    document.body.appendChild(nav);
                    document.body.classList.add('cms-has-bottom-nav');
                }

                if (window.innerWidth <= 992) {
                    document.querySelectorAll('table').forEach((tbl) => {
                        if (tbl.closest('.table-container, .table-wrap, .table-responsive, .att-scroll-wrap, .cms-mob-table-scroll, .pritibat-a4-page, .kearik-page, .rank-a4-sheet, .honor-a4-sheet, .salakabat-a4-page')) {
                            return;
                        }
                        const parent = tbl.parentElement;
                        if (!parent) return;
                        const cs = window.getComputedStyle(parent);
                        if (cs.overflowX === 'auto' || cs.overflowX === 'scroll') return;
                        const wrap = document.createElement('div');
                        wrap.className = 'cms-mob-table-scroll';
                        parent.insertBefore(wrap, tbl);
                        wrap.appendChild(tbl);
                    });
                }
            } catch (e) {}
        });

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
