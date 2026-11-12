/**
 * Auto-Fit Mobile Navigation & Real-Time Student Leave Alert Helper
 * ប្រព័ន្ធគ្រប់គ្រងថ្នាក់រៀន ២០២៧
 */
(function() {
    function initMobileNav() {
        var sidebar = document.querySelector('.sidebar');
        var topbar = document.querySelector('.topbar');
        if (!sidebar || !topbar) return;

        // Prevent duplicate initialization
        if (document.getElementById('mobileNavInitialized')) return;
        var marker = document.createElement('div');
        marker.id = 'mobileNavInitialized';
        marker.style.display = 'none';
        document.body.appendChild(marker);

        // 1. Ensure .topbar-left exists and contains [menuBtn] + [titleEl] tightly docked together on the left
        var leftWrapper = topbar.querySelector('.topbar-left');
        var titleEl = topbar.querySelector('h3, h2');

        if (!leftWrapper) {
            leftWrapper = document.createElement('div');
            leftWrapper.className = 'topbar-left';
            topbar.insertBefore(leftWrapper, topbar.firstChild);
        }

        var menuBtn = topbar.querySelector('.mobile-menu-btn');
        if (!menuBtn) {
            menuBtn = document.createElement('button');
            menuBtn.type = 'button';
            menuBtn.className = 'mobile-menu-btn';
            menuBtn.setAttribute('aria-label', 'បើកម៉ឺនុយ');
            menuBtn.innerHTML = '☰';
            menuBtn.title = 'បើកម៉ឺនុយ';

            menuBtn.addEventListener('click', function(e) {
                e.stopPropagation();
                var isOpen = sidebar.classList.contains('mobile-open');
                toggleSidebar(!isOpen);
            });
        }

        // Always ensure menuBtn is the FIRST child inside leftWrapper
        if (menuBtn.parentElement !== leftWrapper || leftWrapper.firstChild !== menuBtn) {
            leftWrapper.insertBefore(menuBtn, leftWrapper.firstChild);
        }

        // Always ensure titleEl is inside leftWrapper right next to menuBtn
        if (titleEl && titleEl.parentElement !== leftWrapper) {
            var oldParent = titleEl.parentElement;
            leftWrapper.appendChild(titleEl);
            if (oldParent && oldParent !== topbar && oldParent.children.length === 0) {
                oldParent.remove();
            }
        }

        // 2. Create Backdrop overlay
        var backdrop = document.querySelector('.mobile-sidebar-backdrop');
        if (!backdrop) {
            backdrop = document.createElement('div');
            backdrop.className = 'mobile-sidebar-backdrop';
            document.body.appendChild(backdrop);
            backdrop.addEventListener('click', function() {
                toggleSidebar(false);
            });
        }

        // 3. Remove any Close button inside sidebar-header to keep header clean and prevent [x] mark
        var existingCloseButtons = sidebar.querySelectorAll('.mobile-sidebar-close');
        existingCloseButtons.forEach(function(btn) {
            btn.remove();
        });

        function toggleSidebar(open) {
            if (open) {
                sidebar.classList.add('mobile-open');
                backdrop.classList.add('active');
                document.body.style.overflow = 'hidden';
            } else {
                sidebar.classList.remove('mobile-open');
                backdrop.classList.remove('active');
                document.body.style.overflow = '';
            }
        }

        // Close sidebar when clicking any navigation link on mobile
        var navLinks = sidebar.querySelectorAll('.nav-link, a');
        navLinks.forEach(function(link) {
            link.addEventListener('click', function() {
                if (window.innerWidth <= 992) {
                    toggleSidebar(false);
                }
            });
        });

        // Close sidebar on ESC key
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape' && sidebar.classList.contains('mobile-open')) {
                toggleSidebar(false);
            }
        });

        // Swipe-to-close gesture support for touchscreen (Android & iOS)
        var touchStartX = 0;
        var touchCurrentX = 0;
        sidebar.addEventListener('touchstart', function(e) {
            touchStartX = e.touches[0].clientX;
        }, { passive: true });

        sidebar.addEventListener('touchmove', function(e) {
            touchCurrentX = e.touches[0].clientX;
        }, { passive: true });

        sidebar.addEventListener('touchend', function() {
            if (touchStartX - touchCurrentX > 70 && touchCurrentX !== 0) {
                toggleSidebar(false);
            }
            touchStartX = 0;
            touchCurrentX = 0;
        });

        // 4. Initialize Live Leave Request Alert System for Admin & Teachers
        if (!document.body.classList.contains('is-student')) {
            initLeaveRequestAlertSystem(sidebar, topbar);
        }
    }

    function initLeaveRequestAlertSystem(sidebar, topbar) {
        if (document.getElementById('leaveNotifSystemInitialized')) return;
        var flag = document.createElement('div');
        flag.id = 'leaveNotifSystemInitialized';
        flag.style.display = 'none';
        document.body.appendChild(flag);

        // Inject CSS for notification bell and alert modal
        var style = document.createElement('style');
        style.textContent =
            '@keyframes leavePulse { 0% { transform: scale(1); box-shadow: 0 0 0 0 rgba(239,68,68,0.6); } 70% { transform: scale(1.04); box-shadow: 0 0 0 8px rgba(239,68,68,0); } 100% { transform: scale(1); box-shadow: 0 0 0 0 rgba(239,68,68,0); } }' +
            '.leave-notif-bell-btn { display: inline-flex; align-items: center; gap: 6px; padding: 6px 13px; border-radius: 20px; border: 1px solid #cbd5e1; background: #f8fafc; color: #334155; font-family: inherit; font-size: 0.9rem; font-weight: 700; cursor: pointer; transition: all 0.2s; }' +
            '.leave-notif-bell-btn.has-pending { background: #fef2f2; color: #dc2626; border-color: #ef4444; animation: leavePulse 2s infinite; }' +
            '.leave-alert-overlay { position: fixed; inset: 0; background: rgba(15, 23, 42, 0.6); backdrop-filter: blur(3px); z-index: 99999; display: none; align-items: center; justify-content: center; padding: 15px; }' +
            '.leave-alert-modal { background: #ffffff; border-radius: 16px; width: 100%; max-width: 650px; max-height: 88vh; display: flex; flex-direction: column; box-shadow: 0 20px 45px rgba(0,0,0,0.3); border-top: 6px solid #f59e0b; overflow: hidden; font-family: "Kantumruy Pro", "Battambang", sans-serif; }' +
            '.leave-alert-header { padding: 18px 22px; background: #fffbeb; border-bottom: 1px solid #fde68a; display: flex; justify-content: space-between; align-items: center; gap: 10px; }' +
            '.leave-alert-body { padding: 18px 22px; overflow-y: auto; flex: 1; }' +
            '.leave-alert-item { background: #f8fafc; border: 1px solid #e2e8f0; border-left: 4px solid #f59e0b; border-radius: 10px; padding: 14px 16px; margin-bottom: 12px; }' +
            '.leave-alert-footer { padding: 14px 22px; background: #f8fafc; border-top: 1px solid #e2e8f0; display: flex; justify-content: flex-end; gap: 10px; flex-wrap: wrap; }' +
            '@media (max-width: 640px) {' +
            '  .leave-alert-overlay { padding: 10px !important; }' +
            '  .leave-alert-modal { width: calc(100vw - 20px) !important; max-height: 90vh !important; border-radius: 14px !important; }' +
            '  .leave-alert-header { padding: 14px 16px !important; }' +
            '  .leave-alert-body { padding: 14px 16px !important; }' +
            '  .leave-alert-footer { padding: 12px 16px !important; flex-direction: column !important; }' +
            '  .leave-alert-footer a, .leave-alert-footer button { width: 100% !important; text-align: center !important; justify-content: center !important; }' +
            '}';
        document.head.appendChild(style);

        // Create Bell Button in Topbar
        var bellBtn = document.createElement('button');
        bellBtn.type = 'button';
        bellBtn.id = 'leaveNotifBellBtn';
        bellBtn.className = 'leave-notif-bell-btn';
        bellBtn.title = 'មើលសារជូនដំណឹងសិស្សសុំច្បាប់';
        bellBtn.innerHTML = '<span>🔔</span> <span id="leaveNotifBellText">សុំច្បាប់ (0)</span>';
        bellBtn.style.display = 'none';

        var rightContainer = topbar.lastElementChild;
        if (rightContainer && rightContainer !== topbar.querySelector('.topbar-left')) {
            rightContainer.insertBefore(bellBtn, rightContainer.firstChild);
        } else {
            topbar.appendChild(bellBtn);
        }

        // Create Modal Overlay
        var overlay = document.createElement('div');
        overlay.id = 'leaveAlertOverlay';
        overlay.className = 'leave-alert-overlay';
        overlay.innerHTML =
            '<div class="leave-alert-modal">' +
                '<div class="leave-alert-header">' +
                    '<div style="display:flex;align-items:center;gap:10px;">' +
                        '<span style="font-size:1.6rem;">🔔</span>' +
                        '<div>' +
                            '<h3 style="margin:0;font-size:1.15rem;color:#92400e;">សារជូនដំណឹង៖ សិស្សដាក់ពាក្យសុំច្បាប់ឈប់សម្រាក!</h3>' +
                            '<div id="leaveAlertSubtitle" style="font-size:0.85rem;color:#b45309;font-weight:600;margin-top:2px;">មានសំណើសុំច្បាប់កំពុងរង់ចាំការពិនិត្យ និងអនុម័ត</div>' +
                        '</div>' +
                    '</div>' +
                    '<button type="button" id="leaveAlertCloseTop" style="background:none;border:none;font-size:1.4rem;cursor:pointer;color:#64748b;">&times;</button>' +
                '</div>' +
                '<div class="leave-alert-body" id="leaveAlertListContainer"></div>' +
                '<div class="leave-alert-footer">' +
                    '<a href="/attendance" style="background:#4361ee;color:white;text-decoration:none;padding:9px 16px;border-radius:8px;font-weight:700;font-size:0.9rem;">📋 ចូលមើលក្នុងបញ្ជីអវត្តមាន</a>' +
                    '<button type="button" id="leaveAlertCloseBtn" style="background:#e2e8f0;color:#1e293b;border:none;padding:9px 16px;border-radius:8px;font-family:inherit;font-weight:700;font-size:0.9rem;cursor:pointer;">បិទសារជូនដំណឹង</button>' +
                '</div>' +
            '</div>';
        document.body.appendChild(overlay);

        var currentRequests = [];

        function closeAlertModal() {
            overlay.style.display = 'none';
        }

        document.getElementById('leaveAlertCloseTop').addEventListener('click', closeAlertModal);
        document.getElementById('leaveAlertCloseBtn').addEventListener('click', closeAlertModal);
        overlay.addEventListener('click', function(e) {
            if (e.target === overlay) closeAlertModal();
        });

        bellBtn.addEventListener('click', function() {
            renderAlertModal(currentRequests);
            overlay.style.display = 'flex';
        });

        function playNotificationChime() {
            try {
                var AudioCtx = window.AudioContext || window.webkitAudioContext;
                if (!AudioCtx) return;
                var ctx = new AudioCtx();
                var osc = ctx.createOscillator();
                var gain = ctx.createGain();
                osc.type = 'sine';
                osc.frequency.setValueAtTime(587.33, ctx.currentTime); // D5
                osc.frequency.setValueAtTime(880, ctx.currentTime + 0.14); // A5
                gain.gain.setValueAtTime(0.15, ctx.currentTime);
                gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.45);
                osc.connect(gain);
                gain.connect(ctx.destination);
                osc.start();
                osc.stop(ctx.currentTime + 0.45);
            } catch (e) {}
        }

        function escapeHtml(str) {
            var d = document.createElement('div');
            d.textContent = str || '';
            return d.innerHTML;
        }

        function renderAlertModal(requests) {
            var container = document.getElementById('leaveAlertListContainer');
            var subtitle = document.getElementById('leaveAlertSubtitle');
            if (!requests || requests.length === 0) {
                if (subtitle) subtitle.textContent = 'មិនមានសំណើសុំច្បាប់ថ្មីដែលរង់ចាំការអនុម័តទេ';
                container.innerHTML = '<div style="text-align:center;padding:25px;color:#64748b;">មិនមានសំណើសុំច្បាប់ដែលរង់ចាំការអនុម័តទេ។</div>';
                return;
            }
            if (subtitle) {
                subtitle.textContent = 'មានសំណើសុំច្បាប់ចំនួន ' + requests.length + ' កំពុងរង់ចាំការអនុម័ត';
            }
            var html = '';
            requests.forEach(function(r) {
                html +=
                    '<div class="leave-alert-item" id="leave-alert-row-' + r.id + '">' +
                        '<div style="display:flex;justify-content:space-between;align-items:flex-start;flex-wrap:wrap;gap:8px;">' +
                            '<div>' +
                                '<div style="font-size:1.05rem;font-weight:800;color:#0f172a;">👤 សិស្ស៖ <span style="color:#4361ee;">' + escapeHtml(r.studentName) + '</span> ' +
                                    '<span style="font-size:0.85rem;color:#0284c7;background:#e0f2fe;padding:2px 8px;border-radius:6px;">ថ្នាក់ ' + escapeHtml(r.className) + '</span>' +
                                '</div>' +
                                '<div style="font-size:0.84rem;color:#64748b;margin-top:3px;">' +
                                    'កូដសិស្ស៖ <b>' + escapeHtml(r.studentCode) + '</b> • គ្រូបង្រៀន៖ <b style="color:#059669;">' + escapeHtml(r.teacherName) + '</b>' +
                                '</div>' +
                            '</div>' +
                            '<div style="display:flex;gap:6px;">' +
                                '<button type="button" onclick="window.__handleLeaveAction(' + r.id + ', \'approve\')" style="background:#10b981;color:white;border:none;padding:7px 12px;border-radius:6px;font-family:inherit;font-size:0.84rem;font-weight:700;cursor:pointer;">✅ អនុម័តភ្លាមៗ</button>' +
                                '<button type="button" onclick="window.__handleLeaveAction(' + r.id + ', \'reject\')" style="background:#ef4444;color:white;border:none;padding:7px 12px;border-radius:6px;font-family:inherit;font-size:0.84rem;font-weight:700;cursor:pointer;">❌ បដិសេធ</button>' +
                            '</div>' +
                        '</div>' +
                        '<div style="margin-top:10px;padding-top:8px;border-top:1px dashed #cbd5e1;font-size:0.9rem;color:#1e293b;line-height:1.5;">' +
                            '<div>📅 <b>កាលបរិច្ឆេទសុំឈប់៖</b> <span style="color:#b45309;font-weight:700;">' + escapeHtml(r.dateRange) + '</span> (' + r.totalDays + ' ថ្ងៃ • ' + escapeHtml(r.leaveSession) + ')</div>' +
                            '<div style="margin-top:4px;">📝 <b>មូលហេតុ៖</b> <span style="background:#fef3c7;color:#92400e;padding:1px 7px;border-radius:5px;font-weight:700;font-size:0.82rem;">' + escapeHtml(r.leaveType) + '</span> ' + escapeHtml(r.reason) +
                                (r.parentPhone ? ' <span style="color:#0369a1;font-weight:600;">(📞 ' + escapeHtml(r.parentPhone) + ')</span>' : '') +
                            '</div>' +
                        '</div>' +
                    '</div>';
            });
            container.innerHTML = html;
        }

        window.__handleLeaveAction = function(id, action) {
            fetch('/api/leave-requests/' + id + '/' + action, { method: 'POST' })
                .then(function(res) { return res.json(); })
                .then(function(data) {
                    if (data && data.success) {
                        currentRequests = currentRequests.filter(function(item) { return item.id !== id; });
                        updateBadges(currentRequests.length);
                        renderAlertModal(currentRequests);
                        if (currentRequests.length === 0) {
                            setTimeout(function() {
                                closeAlertModal();
                                if (window.location.pathname === '/attendance' || window.location.pathname === '/' || window.location.pathname === '/admin') {
                                    window.location.reload();
                                }
                            }, 400);
                        }
                    }
                })
                .catch(function() {});
        };

        function updateBadges(pendingCount) {
            var bellText = document.getElementById('leaveNotifBellText');
            if (bellText) {
                bellText.textContent = 'សុំច្បាប់ (' + pendingCount + ')';
            }
            if (pendingCount > 0) {
                bellBtn.style.display = 'inline-flex';
                bellBtn.classList.add('has-pending');
            } else {
                bellBtn.classList.remove('has-pending');
                bellBtn.style.display = 'none';
            }

            // Also update sidebar badge next to /attendance
            var attLink = sidebar.querySelector('a[href="/attendance"]');
            if (attLink) {
                var existingBadge = attLink.querySelector('.sidebar-leave-badge');
                if (pendingCount > 0) {
                    if (!existingBadge) {
                        existingBadge = document.createElement('span');
                        existingBadge.className = 'sidebar-leave-badge';
                        existingBadge.style.cssText = 'margin-left:auto;background:#ef4444;color:white;font-size:0.75rem;padding:2px 8px;border-radius:12px;font-weight:800;';
                        attLink.style.display = 'flex';
                        attLink.style.alignItems = 'center';
                        attLink.appendChild(existingBadge);
                    }
                    existingBadge.textContent = pendingCount;
                } else if (existingBadge) {
                    existingBadge.remove();
                }
            }
        }

        function checkLeaveNotifications() {
            fetch('/api/leave-requests/notifications', { headers: { 'Cache-Control': 'no-cache' } })
                .then(function(res) {
                    if (!res.ok) return null;
                    return res.json();
                })
                .then(function(data) {
                    if (!data) return;
                    var pendingCount = data.pendingCount || 0;
                    var requests = data.requests || [];
                    currentRequests = requests;
                    updateBadges(pendingCount);

                    if (pendingCount > 0 && requests.length > 0) {
                        var seenIdsRaw = sessionStorage.getItem('ALERTED_LEAVE_REQ_IDS') || '[]';
                        var seenIds = [];
                        try { seenIds = JSON.parse(seenIdsRaw); } catch (e) { seenIds = []; }

                        var hasNewUnalerted = false;
                        requests.forEach(function(r) {
                            if (seenIds.indexOf(r.id) === -1) {
                                hasNewUnalerted = true;
                                seenIds.push(r.id);
                            }
                        });

                        if (hasNewUnalerted) {
                            sessionStorage.setItem('ALERTED_LEAVE_REQ_IDS', JSON.stringify(seenIds));
                            renderAlertModal(requests);
                            overlay.style.display = 'flex';
                            playNotificationChime();
                        }
                    }
                })
                .catch(function() {});
        }

        checkLeaveNotifications();
        setInterval(checkLeaveNotifications, 8000);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initMobileNav);
    } else {
        initMobileNav();
    }
})();
