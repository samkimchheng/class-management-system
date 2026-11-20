// CMS App (PWA) Installer for Android, iOS (iPhone/iPad), and Desktop (Windows/Mac)
(function () {
    if ('serviceWorker' in navigator) {
        window.addEventListener('load', () => {
            navigator.serviceWorker.register('/sw.js', { scope: '/' }).catch(() => {});
        });
    }

    const isStandalone = window.matchMedia('(display-mode: standalone)').matches
        || window.navigator.standalone === true;
    if (isStandalone) return; // Already running inside the installed CMS App!

    let deferredPrompt = null;
    const isIos = /iphone|ipad|ipod/i.test(navigator.userAgent);
    const isSafari = /^((?!chrome|android).)*safari/i.test(navigator.userAgent);

    function createInstallPill() {
        if (document.getElementById('cmsPwaInstallWrap')) return;
        if (sessionStorage.getItem('cmsPwaDismissed') === '1') return;

        const wrap = document.createElement('div');
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
            'padding:8px 12px 8px 14px',
            'border-radius:999px',
            'box-shadow:0 8px 24px rgba(30,58,138,0.38)',
            'border:1.5px solid rgba(255,255,255,0.35)',
            'font-family:"Kantumruy Pro","Battambang",sans-serif',
            'font-size:0.86rem',
            'font-weight:700',
            'cursor:pointer',
            'user-select:none',
            '-webkit-tap-highlight-color:transparent'
        ].join(';');

        const btn = document.createElement('span');
        btn.id = 'cmsPwaInstallBtn';
        btn.style.cssText = 'display:inline-flex;align-items:center;gap:6px;';
        btn.innerHTML = '<img src="/icons/icon-192.png" alt="CMS" style="width:20px;height:20px;border-radius:5px;"> <span>📲 តម្លើង App (CMS)</span>';
        btn.addEventListener('click', handleInstallClick);

        const closeBtn = document.createElement('span');
        closeBtn.textContent = '×';
        closeBtn.title = 'បិទ';
        closeBtn.style.cssText = 'margin-left:4px;padding:0 5px;font-size:1.1rem;line-height:1;opacity:0.85;border-left:1px solid rgba(255,255,255,0.3);';
        closeBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            sessionStorage.setItem('cmsPwaDismissed', '1');
            wrap.remove();
        });

        wrap.appendChild(btn);
        wrap.appendChild(closeBtn);
        document.body.appendChild(wrap);
    }

    function showManualInstallGuide() {
        const html = isIos
            ? `<div style="text-align:left;line-height:1.8;font-size:0.95rem;">
                 <div style="margin-bottom:8px;font-weight:700;color:#1e3a8a;">របៀបតម្លើងកម្មវិធី <b>CMS</b> លើ iPhone / iPad (iOS)៖</div>
                 <div>១. ចុចប៊ូតុង <b>Share (⬆️)</b> នៅផ្នែកខាងក្រោមអេក្រង់ Safari</div>
                 <div>២. អូសចុះក្រោម រួចចុច <b>«Add to Home Screen» (➕)</b></div>
                 <div>៣. ចុច <b>«Add»</b> ជាការស្រេច — រូបតំណាង <b>CMS</b> នឹងបង្ហាញលើអេក្រង់ទូរស័ព្ទ!</div>
               </div>`
            : `<div style="text-align:left;line-height:1.8;font-size:0.95rem;">
                 <div style="margin-bottom:8px;font-weight:700;color:#1e3a8a;">របៀបតម្លើងកម្មវិធី <b>CMS</b> លើ Android ឬ កុំព្យូទ័រ៖</div>
                 <div>• <b>លើទូរស័ព្ទ Android (Chrome)៖</b> ចុចម៉ឺនុយ <b>⋮</b> ខាងស្តាំលើ → <b>«Install app»</b> ឬ <b>«Add to Home screen»</b></div>
                 <div>• <b>លើកុំព្យូទ័រ (Chrome / Edge)៖</b> ចុចរូបតំណាង <b>⊕ / 💻</b> នៅខាងស្តាំរបារ Address bar ខាងលើ → ចុច <b>«Install»</b></div>
               </div>`;

        if (typeof Swal !== 'undefined') {
            Swal.fire({
                title: '📲 តម្លើងកម្មវិធី CMS',
                html: html,
                confirmButtonText: 'យល់ព្រម',
                confirmButtonColor: '#2563eb'
            });
        } else {
            alert('ដើម្បីតម្លើង App CMS៖\n- Android/PC (Chrome/Edge): ចុចម៉ឺនុយ ⋮ -> Install app (CMS)\n- iPhone/iPad (Safari): ចុច Share (⬆️) -> Add to Home Screen -> Add');
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
        createInstallPill();
    });

    window.addEventListener('appinstalled', () => {
        deferredPrompt = null;
        const wrap = document.getElementById('cmsPwaInstallWrap');
        if (wrap) wrap.remove();
    });

    // Show the install pill on iOS Safari (which doesn't fire beforeinstallprompt) and on Desktop/Android after load
    document.addEventListener('DOMContentLoaded', () => {
        if (isIos && isSafari) {
            createInstallPill();
        } else {
            // If browser hasn't fired beforeinstallprompt after 1.5s, still show the pill with smart fallback guide
            setTimeout(() => {
                if (!isStandalone) createInstallPill();
            }, 1500);
        }
    });

    // Expose global trigger so any page button can also invoke CMS installation
    window.triggerCmsAppInstall = handleInstallClick;
})();
