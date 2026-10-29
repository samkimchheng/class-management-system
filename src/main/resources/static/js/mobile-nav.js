/**
 * Auto-Fit Mobile Navigation & Responsive Helper for Android & iOS
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
                toggleSidebar(true);
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

        // 3. Create Close button inside sidebar-header for mobile
        var header = sidebar.querySelector('.sidebar-header');
        if (header && !header.querySelector('.mobile-sidebar-close')) {
            header.style.position = 'relative';
            var closeBtn = document.createElement('button');
            closeBtn.type = 'button';
            closeBtn.className = 'mobile-sidebar-close';
            closeBtn.innerHTML = '&times;';
            closeBtn.setAttribute('aria-label', 'បិទម៉ឺនុយ');
            closeBtn.addEventListener('click', function(e) {
                e.stopPropagation();
                toggleSidebar(false);
            });
            header.appendChild(closeBtn);
        }

        function toggleSidebar(open) {
            if (open) {
                sidebar.classList.add('mobile-open');
                backdrop.classList.add('active');
                document.body.style.overflow = 'hidden'; // Prevent background scrolling on iOS & Android
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
                // Swiped left to close
                toggleSidebar(false);
            }
            touchStartX = 0;
            touchCurrentX = 0;
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initMobileNav);
    } else {
        initMobileNav();
    }
})();
