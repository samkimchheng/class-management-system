function switchAcademicYear(year) {
    if (!year) return;
    if (year === '__NEW__') {
        const customYear = prompt('សូមបញ្ចូលឆ្នាំសិក្សាថ្មី (ឧទាហរណ៍៖ ២០២៧-២០២៨)៖');
        if (customYear && customYear.trim()) {
            const trimmed = customYear.trim();
            window.location.href = '/switch-academic-year?year=' + encodeURIComponent(trimmed) + '&redirect=' + encodeURIComponent(window.location.pathname + window.location.search);
        } else {
            location.reload();
        }
        return;
    }
    window.location.href = '/switch-academic-year?year=' + encodeURIComponent(year) + '&redirect=' + encodeURIComponent(window.location.pathname + window.location.search);
}
