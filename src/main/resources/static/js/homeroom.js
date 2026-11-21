// ============================================================
// HOMEROOM TEACHER MANAGEMENT SUITE (ស្តង់ដារក្រសួងអប់រំ យុវជន និងកីឡា)
// 100% isolated data layer - zero modification to existing teacher data
// ============================================================

const PERIOD_LABELS = {
    SEC: 'តេស្តដើមឆ្នាំ (SEC)',
    M1: 'ខែវិច្ឆិកា', M2: 'ខែធ្នូ', M3: 'ខែមករា', M4: 'ខែកុម្ភៈ', M5: 'ខែមីនា',
    EXAM_S1: 'ប្រឡងឆមាសលើកទី១',
    M6: 'ខែមេសា', M7: 'ខែឧសភា', M8: 'ខែមិថុនា', M9: 'ខែកក្កដា', M10: 'ខែសីហា',
    EXAM_S2: 'ប្រឡងឆមាសលើកទី២',
    S1: 'ប្រចាំឆមាសទី១', S2: 'ប្រចាំឆមាសទី២', YEAR: 'ប្រចាំឆ្នាំសិក្សា'
};

const S1_MONTHS = ['M1', 'M2', 'M3', 'M4', 'M5'];
const S2_MONTHS = ['M6', 'M7', 'M8', 'M9', 'M10'];

let HR = {
    className: '',
    academicYear: '',
    teacher: {},
    students: [],
    subjects: [],
    scores: [],      // raw list from DB
    scoreMap: {},    // key: `${period}_${studentId}_${subjectId}` -> number
    dailyAbsences: [],
    admin: {
        cleaning: null,
        seating: null,
        youth: null,
        inventory: null,
        books: null,
        geip: null,
        meetings: null,
        monthlyAtt: {},
        config: {}
    }
};

function esc(str) {
    if (str === null || str === undefined) return '';
    return String(str)
        .replace(/&/g, '&amp;').replace(/</g, '&lt;')
        .replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#39;');
}

function toggleSidebar(open) {
    document.getElementById('sidebar').classList.toggle('open', open);
    document.getElementById('sidebarOverlay').classList.toggle('show', open);
}

function switchTab(tabId, btn) {
    document.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));
    document.querySelectorAll('.hr-tab').forEach(b => b.classList.remove('active'));
    const pane = document.getElementById(tabId);
    if (pane) pane.classList.add('active');
    if (btn) {
        btn.classList.add('active');
        try { btn.scrollIntoView({ behavior: 'smooth', inline: 'center', block: 'nearest' }); } catch (e) {}
    }

    if (tabId === 'tab-scores') renderScoreEntryTable();
    else if (tabId === 'tab-monthly-att') renderMonthlyAttendance();
    else if (tabId === 'tab-ranks') renderRankingsView();
    else if (tabId === 'tab-honor') renderHonorAndCert();
    else if (tabId === 'tab-reports') renderReportDocs();
    else if (tabId === 'tab-cards') renderCardsTab();
    else if (tabId === 'tab-admin') renderAdminSubTab();
    else if (tabId === 'tab-assets') renderAssetSubTab();
    else if (tabId === 'tab-geip') renderGeipSubTab();
}

function parseJsonSafe(raw, fallback) {
    if (!raw) return fallback;
    try { return JSON.parse(raw); } catch (e) { return fallback; }
}

async function loadHomeroomData() {
    const clsSel = document.getElementById('hrClassSelect');
    if (!clsSel || !clsSel.value) {
        document.getElementById('hrKpiGrid').innerHTML =
            '<div class="panel" style="grid-column:1/-1;text-align:center;">សូមបង្កើតថ្នាក់រៀននៅក្នុងម៉ឺនុយ «បញ្ជីថ្នាក់រៀនខ្ញុំ» ជាមុនសិន។</div>';
        return;
    }
    HR.className = clsSel.value;
    HR.academicYear = document.getElementById('hrAcademicYear').value || '២០២៦-២០២៧';

    try {
        const res = await fetch(`/api/homeroom/bootstrap?className=${encodeURIComponent(HR.className)}&academicYear=${encodeURIComponent(HR.academicYear)}`);
        if (!res.ok) throw new Error('Failed to load homeroom data');
        const data = await res.json();
        HR.teacher = data.teacher || {};
        HR.students = data.students || [];
        HR.subjects = data.subjects || [];
        HR.scores = data.scores || [];
        HR.dailyAbsences = data.dailyAbsences || [];
        rebuildScoreMap();

        const adm = data.admin || {};
        HR.admin.config = parseJsonSafe(adm.configJson, {});
        HR.admin.monthlyAtt = parseJsonSafe(adm.monthlyAttendanceJson, {});
        HR.admin.monitorStudentId = adm.monitorStudentId || null;
        HR.admin.cleaning = parseJsonSafe(adm.cleaningScheduleJson, null);
        HR.admin.seating = parseJsonSafe(adm.seatingLayoutJson, null);
        HR.admin.youth = parseJsonSafe(adm.youthCouncilJson, null);
        HR.admin.inventory = parseJsonSafe(adm.inventoryJson, null);
        HR.admin.books = parseJsonSafe(adm.textbookLoansJson, null);
        HR.admin.geip = parseJsonSafe(adm.geipPlanJson, null);
        HR.admin.meetings = parseJsonSafe(adm.meetingLogsJson, null);

        populateConfigInputs();
        populateStudentSelectors();
        renderDashboardAndSubjects();

        const activeTab = document.querySelector('.tab-pane.active');
        if (activeTab && activeTab.id !== 'tab-setup') {
            switchTab(activeTab.id, document.querySelector(`.hr-tab[data-tab="${activeTab.id}"]`));
        }
    } catch (err) {
        console.error(err);
        Swal.fire('កំហុស', 'មិនអាចទាញយកទិន្នន័យគ្រូបន្ទុកថ្នាក់បានទេ', 'error');
    }
}

function rebuildScoreMap() {
    HR.scoreMap = {};
    for (const sc of HR.scores) {
        if (sc.score !== null && sc.score !== undefined) {
            HR.scoreMap[`${sc.period}_${sc.studentId}_${sc.subjectId}`] = Number(sc.score);
        }
    }
}

function populateConfigInputs() {
    const cfg = HR.admin.config || {};
    document.getElementById('cfgMinistry').value = cfg.ministry || (HR.teacher.province ? `មន្ទីរអប់រំ យុវជន និងកីឡា ${HR.teacher.province}` : 'មន្ទីរអប់រំ យុវជន និងកីឡា');
    document.getElementById('cfgSchool').value = cfg.school || HR.teacher.schoolName || 'វិទ្យាល័យ';
    document.getElementById('cfgPrincipal').value = cfg.principal || HR.teacher.principalName || '';
    document.getElementById('cfgLunarDate').value = cfg.lunarDate || 'ថ្ងៃ............ ទី...... ខែ............ ឆ្នាំ............ ព.ស. ២៥......';
    document.getElementById('cfgSolarDate').value = cfg.solarDate || 'ថ្ងៃទី...... ខែ............ ឆ្នាំ ២០២៦';
}

function populateStudentSelectors() {
    const opts = HR.students.map((s, i) => `<option value="${s.id}">${i + 1}. ${esc(s.name)} (${esc(s.gender)})</option>`).join('');
    ['certStudentSelect', 'reportStudentSelect', 'salakabatStudentSelect'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.innerHTML = opts || '<option value="">គ្មានសិស្ស</option>';
    });
    populateMonitorSelector();
}

function populateMonitorSelector() {
    const sel = document.getElementById('hrMonitorSelect');
    const statusEl = document.getElementById('hrMonitorStatus');
    if (!sel) return;
    const curId = HR.admin.monitorStudentId ? String(HR.admin.monitorStudentId) : '';
    sel.innerHTML = `<option value="">-- មិនទាន់ផ្តល់សិទ្ធិ (មានតែគ្រូបន្ទុក) --</option>` +
        HR.students.map((s, i) => `<option value="${s.id}" ${String(s.id) === curId ? 'selected' : ''}>${i + 1}. ${esc(s.name)} (${esc(s.studentCode)})</option>`).join('');
    if (statusEl) {
        const mon = HR.students.find(s => String(s.id) === curId);
        statusEl.innerHTML = mon
            ? `✅ កំពុងផ្តល់សិទ្ធិជូន៖ <b>${esc(mon.name)}</b> (លេខកូដចូលសិស្ស៖ <b>${esc(mon.studentCode)}</b>)`
            : 'ℹ️ មិនទាន់មានប្រធានថ្នាក់ត្រូវបានផ្តល់សិទ្ធិនៅឡើយទេ';
    }
}

async function saveClassMonitorPermission() {
    const sel = document.getElementById('hrMonitorSelect');
    if (!sel) return;
    const val = sel.value ? Number(sel.value) : null;
    const res = await fetch('/api/homeroom/admin/save', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ className: HR.className, academicYear: HR.academicYear, monitorStudentId: val })
    });
    const data = await res.json();
    if (data.success) {
        HR.admin.monitorStudentId = val;
        populateMonitorSelector();
        const mon = HR.students.find(s => s.id === val);
        Swal.fire({
            icon: 'success',
            title: val ? 'បានផ្តល់សិទ្ធិប្រធានថ្នាក់ជោគជ័យ!' : 'បានដកសិទ្ធិប្រធានថ្នាក់រួចរាល់!',
            text: mon ? `សិស្ស ${mon.name} អាចចូលគណនីសិស្ស (កូដ ${mon.studentCode}) ហើយចុចប៊ូតុង «👑 ស្រង់អវត្តមានតាមខែ» ដើម្បីជួយស្រង់អវត្តមានថ្នាក់ ${HR.className} បានភ្លាមៗ!` : '',
            confirmButtonText: 'យល់ព្រម'
        });
    } else {
        Swal.fire('បរាជ័យ', data.error || 'មិនអាចរក្សាទុកបានទេ', 'error');
    }
}

function getOfficialHeader(docTitle, subtitle = '') {
    const cfg = HR.admin.config || {};
    const ministry = cfg.ministry || (HR.teacher.province ? `មន្ទីរអប់រំ យុវជន និងកីឡា ${HR.teacher.province}` : 'មន្ទីរអប់រំ យុវជន និងកីឡា');
    const school = cfg.school || HR.teacher.schoolName || 'វិទ្យាល័យ';
    return `
        <div class="moeys-header">
            <div class="moeys-ministry">
                <div>${esc(ministry)}</div>
                <div>${esc(school)}</div>
                <div>ថ្នាក់ទី៖ ${esc(HR.className)} | ឆ្នាំសិក្សា៖ ${esc(HR.academicYear)}</div>
            </div>
            <div class="moeys-kingdom">
                <div class="moul">ព្រះរាជាណាចក្រកម្ពុជា</div>
                <div class="moul" style="font-size:0.9rem;margin-top:2px;">ជាតិ សាសនា ព្រះមហាក្សត្រ</div>
                <div style="margin-top:2px;letter-spacing:3px;">***</div>
            </div>
        </div>
        <div style="text-align:center;margin:8px 0 14px;">
            <div class="moul" style="font-size:1.05rem;color:#0f172a;">${esc(docTitle)}</div>
            ${subtitle ? `<div style="font-weight:700;color:#334155;margin-top:4px;font-size:0.9rem;">${esc(subtitle)}</div>` : ''}
        </div>
    `;
}

function getOfficialFooter() {
    const cfg = HR.admin.config || {};
    const lunar = cfg.lunarDate || 'ថ្ងៃ............ ទី...... ខែ............ ឆ្នាំ............ ព.ស. ២៥......';
    const solar = cfg.solarDate || 'ថ្ងៃទី...... ខែ............ ឆ្នាំ ២០២៦';
    const principal = cfg.principal || HR.teacher.principalName || '.........................';
    const teacherName = HR.teacher.fullName || '.........................';
    const stampHtml = HR.teacher.schoolStamp ? `<img src="${esc(HR.teacher.schoolStamp)}" style="max-height:64px;opacity:0.85;margin-top:4px;">` : '';
    const pSigHtml = HR.teacher.principalSignature ? `<img src="${esc(HR.teacher.principalSignature)}" style="max-height:54px;margin-top:4px;">` : '';
    const tSigHtml = HR.teacher.signature ? `<img src="${esc(HR.teacher.signature)}" style="max-height:54px;margin-top:4px;">` : '';

    return `
        <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-top:22px;font-size:0.86rem;gap:20px;flex-wrap:wrap;">
            <div style="text-align:center;min-width:230px;">
                <div>បានឃើញ និងឯកភាព</div>
                <div class="moul" style="font-size:0.84rem;margin-top:2px;">នាយកសាលា</div>
                <div style="display:flex;justify-content:center;align-items:center;gap:6px;min-height:66px;">${stampHtml}${pSigHtml}</div>
                <div class="moul" style="padding-left:52px;font-size:0.86rem;">${esc(principal)}</div>
            </div>
            <div style="text-align:center;min-width:250px;">
                <div>${esc(lunar)}</div>
                <div>${esc(solar)}</div>
                <div class="moul" style="font-size:0.84rem;margin-top:4px;">គ្រូបន្ទុកថ្នាក់</div>
                <div style="min-height:62px;display:flex;justify-content:center;align-items:center;">${tSigHtml}</div>
                <div class="moul" style="padding-left:52px;font-size:0.86rem;">${esc(teacherName)}</div>
            </div>
        </div>
    `;
}

// ==================== GRADING & RANKING ENGINE ====================
function getTotalMaxCoeff() {
    let totalMax = 0, totalCoeff = 0;
    for (const s of HR.subjects) {
        totalMax += Number(s.maxScore || 50);
        totalCoeff += Number(s.coefficient || 1);
    }
    return { totalMax, totalCoeff: totalCoeff > 0 ? totalCoeff : 1 };
}

function getMentionByAvg50(avg) {
    // Standard Cambodian Secondary School 50-point average scale:
    // >=40 (80%): A ល្អប្រសើរ | >=35 (70%): B ល្អ | >=30 (60%): C ល្អបង្គួរ | >=25 (50%): D មធ្យម | >=20 (40%): E ខ្សោយ | <20: F ធ្លាក់
    if (avg === null || avg === undefined || isNaN(avg)) return { code: '-', kh: '-', pass: false };
    if (avg >= 40) return { code: 'A', kh: 'ល្អប្រសើរ', pass: true };
    if (avg >= 35) return { code: 'B', kh: 'ល្អ', pass: true };
    if (avg >= 30) return { code: 'C', kh: 'ល្អបង្គួរ', pass: true };
    if (avg >= 25) return { code: 'D', kh: 'មធ្យម', pass: true };
    if (avg >= 20) return { code: 'E', kh: 'ខ្សោយ', pass: false };
    return { code: 'F', kh: 'ធ្លាក់', pass: false };
}

function getSubjectMention(score, maxScore) {
    if (score === null || score === undefined || !maxScore) return '-';
    const pct = (score / maxScore) * 100;
    if (pct >= 80) return 'A';
    if (pct >= 70) return 'B';
    if (pct >= 60) return 'C';
    if (pct >= 50) return 'D';
    if (pct >= 40) return 'E';
    return 'F';
}

function computeSinglePeriod(period) {
    const { totalCoeff } = getTotalMaxCoeff();
    const rows = HR.students.map(st => {
        let total = 0;
        let hasAny = false;
        const subScores = {};
        for (const sub of HR.subjects) {
            const val = HR.scoreMap[`${period}_${st.id}_${sub.id}`];
            if (val !== undefined) {
                subScores[sub.id] = val;
                total += val;
                hasAny = true;
            } else {
                subScores[sub.id] = null;
            }
        }
        const avg = hasAny ? (total / totalCoeff) : null;
        const mention = getMentionByAvg50(avg);
        return { student: st, subScores, total: hasAny ? total : null, avg, mention, rank: null };
    });
    assignRanks(rows);
    return rows;
}

function computeSemester(semesterNum) {
    const months = semesterNum === 1 ? S1_MONTHS : S2_MONTHS;
    const examPeriod = semesterNum === 1 ? 'EXAM_S1' : 'EXAM_S2';
    const monthResults = months.map(m => computeSinglePeriod(m));
    const examResults = computeSinglePeriod(examPeriod);
    const examMap = {};
    examResults.forEach(r => { examMap[r.student.id] = r; });

    const rows = HR.students.map((st, idx) => {
        let mSum = 0, mCnt = 0;
        monthResults.forEach(mr => {
            if (mr[idx] && mr[idx].avg !== null) {
                mSum += mr[idx].avg;
                mCnt++;
            }
        });
        const avgMonths = mCnt > 0 ? (mSum / mCnt) : null;
        const examAvg = examMap[st.id] ? examMap[st.id].avg : null;
        let semAvg = null;
        if (avgMonths !== null && examAvg !== null) semAvg = (avgMonths + examAvg) / 2.0;
        else if (avgMonths !== null) semAvg = avgMonths;
        else if (examAvg !== null) semAvg = examAvg;

        const mention = getMentionByAvg50(semAvg);
        return {
            student: st,
            avgMonths,
            examAvg,
            avg: semAvg,
            total: examMap[st.id] ? examMap[st.id].total : null,
            subScores: examMap[st.id] ? examMap[st.id].subScores : {},
            mention,
            rank: null
        };
    });
    assignRanks(rows);
    return rows;
}

function computeAnnual() {
    const s1 = computeSemester(1);
    const s2 = computeSemester(2);
    const s1Map = {}, s2Map = {};
    s1.forEach(r => { s1Map[r.student.id] = r; });
    s2.forEach(r => { s2Map[r.student.id] = r; });

    const rows = HR.students.map(st => {
        const a1 = s1Map[st.id] ? s1Map[st.id].avg : null;
        const a2 = s2Map[st.id] ? s2Map[st.id].avg : null;
        let yrAvg = null;
        if (a1 !== null && a2 !== null) yrAvg = (a1 + a2) / 2.0;
        else if (a1 !== null) yrAvg = a1;
        else if (a2 !== null) yrAvg = a2;
        const mention = getMentionByAvg50(yrAvg);
        return {
            student: st, s1Avg: a1, s2Avg: a2, avg: yrAvg,
            mention, promoted: yrAvg !== null ? yrAvg >= 25 : null, rank: null
        };
    });
    assignRanks(rows);
    return rows;
}

function computeResultsForPeriod(period) {
    if (period === 'S1') return computeSemester(1);
    if (period === 'S2') return computeSemester(2);
    if (period === 'YEAR') return computeAnnual();
    return computeSinglePeriod(period);
}

function assignRanks(rows) {
    const valid = rows.filter(r => r.avg !== null).sort((a, b) => b.avg - a.avg);
    let lastAvg = null, lastRank = 0;
    valid.forEach((r, idx) => {
        const rounded = Math.round(r.avg * 1000);
        if (lastAvg !== null && rounded === lastAvg) {
            r.rank = lastRank;
        } else {
            r.rank = idx + 1;
            lastRank = r.rank;
            lastAvg = rounded;
        }
    });
}

function fmtNum(n) {
    if (n === null || n === undefined || isNaN(n)) return '-';
    return Number(n).toFixed(2);
}

// ==================== TAB 1: DASHBOARD & SUBJECTS ====================
function renderDashboardAndSubjects() {
    const total = HR.students.length;
    const female = HR.students.filter(s => s.gender === 'ស្រី' || s.gender === 'Female' || s.gender === 'F').length;
    const totalAbsent = HR.students.reduce((acc, s) => acc + (s.absentTotal || 0), 0);
    const s1 = computeSemester(1);
    const validS1 = s1.filter(r => r.avg !== null);
    const passS1 = validS1.filter(r => r.avg >= 25).length;
    const passPct = validS1.length > 0 ? ((passS1 / validS1.length) * 100).toFixed(1) + '%' : '-';

    document.getElementById('hrKpiGrid').innerHTML = `
        <div class="kpi-card">
            <div class="kpi-icon" style="background:#dbeafe;color:#1d4ed8;">👥</div>
            <div><div class="kpi-val">${total} <span style="font-size:0.85rem;color:#64748b;">(ស្រី ${female})</span></div><div class="kpi-lbl">សិស្សសរុបក្នុងថ្នាក់</div></div>
        </div>
        <div class="kpi-card">
            <div class="kpi-icon" style="background:#e0e7ff;color:#4338ca;">📚</div>
            <div><div class="kpi-val">${HR.subjects.length}</div><div class="kpi-lbl">មុខវិជ្ជាសរុប</div></div>
        </div>
        <div class="kpi-card">
            <div class="kpi-icon" style="background:#dcfce7;color:#15803d;">🏆</div>
            <div><div class="kpi-val">${passPct}</div><div class="kpi-lbl">អត្រាជាប់ (ឆមាសទី១)</div></div>
        </div>
        <div class="kpi-card">
            <div class="kpi-icon" style="background:#fee2e2;color:#b91c1c;">📅</div>
            <div><div class="kpi-val">${totalAbsent}</div><div class="kpi-lbl">អវត្តមានសរុប (ទាញស្វ័យប្រវត្តិ)</div></div>
        </div>
    `;

    const tbody = document.getElementById('subjectsTableBody');
    if (HR.subjects.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="padding:20px;color:#64748b;">មិនទាន់មានមុខវិជ្ជាទេ — សូមចុចប៊ូតុង <b>⚡ ថ្នាក់ទី៧-៩ / ១០ / ១១-១២</b> ខាងលើដើម្បីបង្កើតមុខវិជ្ជាស្តង់ដារក្រសួងអប់រំក្នុង ១ ចុច!</td></tr>`;
        return;
    }
    tbody.innerHTML = HR.subjects.map((sub, i) => `
        <tr>
            <td>${i + 1}</td>
            <td class="text-left" style="font-weight:700;">${esc(sub.subjectName)}</td>
            <td class="text-left">${esc(sub.teacherName || '-')}</td>
            <td>${sub.maxScore}</td>
            <td>${sub.coefficient}</td>
            <td class="no-print">
                <button class="btn btn-outline btn-sm" onclick="openEditSubjectModal(${sub.id})">✏️ កែ</button>
                <button class="btn btn-danger btn-sm" onclick="deleteSubject(${sub.id})">🗑️</button>
            </td>
        </tr>
    `).join('');
}

async function applyPreset(preset) {
    const res = await fetch('/api/homeroom/subjects/preset', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ className: HR.className, academicYear: HR.academicYear, preset })
    });
    const data = await res.json();
    if (data.success) {
        HR.subjects = data.subjects || [];
        renderDashboardAndSubjects();
        Swal.fire({ icon: 'success', title: 'បានបង្កើតមុខវិជ្ជាស្តង់ដារជោគជ័យ!', timer: 1400, showConfirmButton: false });
    }
}

async function openAddSubjectModal(existing = null) {
    const { value: formValues } = await Swal.fire({
        title: existing ? 'កែប្រែមុខវិជ្ជា' : 'បន្ថែមមុខវិជ្ជាថ្មី',
        html: `
            <div style="text-align:left;display:flex;flex-direction:column;gap:8px;">
                <label style="font-weight:700;font-size:0.85rem;">ឈ្មោះមុខវិជ្ជា៖</label>
                <input id="swSubName" class="swal2-input" style="margin:0;width:100%;" value="${esc(existing ? existing.subjectName : '')}" placeholder="ឧ. គណិតវិទ្យា">
                <label style="font-weight:700;font-size:0.85rem;">ឈ្មោះគ្រូបង្រៀនតាមមុខវិជ្ជា៖</label>
                <input id="swSubTeacher" class="swal2-input" style="margin:0;width:100%;" value="${esc(existing ? existing.teacherName : '')}" placeholder="ឧ. លោកគ្រូ សុខ ដារ៉ា">
                <div style="display:flex;gap:10px;">
                    <div style="flex:1;">
                        <label style="font-weight:700;font-size:0.85rem;">ពិន្ទុពេញ៖</label>
                        <input id="swSubMax" type="number" class="swal2-input" style="margin:0;width:100%;" value="${existing ? existing.maxScore : 100}">
                    </div>
                    <div style="flex:1;">
                        <label style="font-weight:700;font-size:0.85rem;">មេគុណ៖</label>
                        <input id="swSubCoeff" type="number" step="0.5" class="swal2-input" style="margin:0;width:100%;" value="${existing ? existing.coefficient : 2}">
                    </div>
                </div>
            </div>
        `,
        showCancelButton: true, confirmButtonText: 'រក្សាទុក', cancelButtonText: 'បោះបង់',
        preConfirm: () => ({
            id: existing ? existing.id : null,
            subjectName: document.getElementById('swSubName').value.trim(),
            teacherName: document.getElementById('swSubTeacher').value.trim(),
            maxScore: parseFloat(document.getElementById('swSubMax').value) || 50,
            coefficient: parseFloat(document.getElementById('swSubCoeff').value) || 1
        })
    });
    if (!formValues || !formValues.subjectName) return;

    const res = await fetch('/api/homeroom/subjects/save', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ...formValues, className: HR.className, academicYear: HR.academicYear })
    });
    const data = await res.json();
    if (data.success) {
        HR.subjects = data.subjects || [];
        renderDashboardAndSubjects();
        Swal.fire({ icon: 'success', title: 'រក្សាទុកជោគជ័យ!', timer: 1200, showConfirmButton: false });
    }
}

function openEditSubjectModal(id) {
    const sub = HR.subjects.find(s => s.id === id);
    if (sub) openAddSubjectModal(sub);
}

async function deleteSubject(id) {
    const c = await Swal.fire({ title: 'លុបមុខវិជ្ជានេះ?', text: 'ពិន្ទុរបស់មុខវិជ្ជានេះក្នុងផ្នែកគ្រូបន្ទុកនឹងត្រូវលុប។', icon: 'warning', showCancelButton: true, confirmButtonText: 'លុប', cancelButtonText: 'ទេ' });
    if (!c.isConfirmed) return;
    const res = await fetch(`/api/homeroom/subjects/${id}`, { method: 'DELETE' });
    if (res.ok) {
        HR.subjects = HR.subjects.filter(s => s.id !== id);
        renderDashboardAndSubjects();
    }
}

async function saveClassConfig() {
    HR.admin.config = {
        ministry: document.getElementById('cfgMinistry').value.trim(),
        school: document.getElementById('cfgSchool').value.trim(),
        principal: document.getElementById('cfgPrincipal').value.trim(),
        lunarDate: document.getElementById('cfgLunarDate').value.trim(),
        solarDate: document.getElementById('cfgSolarDate').value.trim()
    };
    await saveAdminField('configJson', JSON.stringify(HR.admin.config));
}

// ==================== TAB 2: SCORE ENTRY (EXACT MOEYS SCHOOL PDF FORMAT) ====================
function getScorePeriodKhmerLabel(period) {
    const map = {
        SEC: 'តេស្តដើមឆ្នាំ',
        M1: 'ខែវិច្ឆិកា', M2: 'ខែធ្នូ', M3: 'ខែមករា', M4: 'ខែកុម្ភៈ', M5: 'ខែមីនា',
        EXAM_S1: 'ប្រឡងឆមាសលើកទី១', S1: 'ឆមាសទី១',
        M6: 'ខែមេសា', M7: 'ខែឧសភា', M8: 'ខែមិថុនា', M9: 'ខែកក្កដា', M10: 'ខែសីហា',
        EXAM_S2: 'ប្រឡងឆមាសលើកទី២', S2: 'ឆមាសទី២',
        YEAR: 'ប្រចាំឆ្នាំសិក្សា'
    };
    return map[period] || period;
}

function buildMoeysGradeSummaryHtml(results) {
    const total = results.length;
    const female = results.filter(r => getShortGender(r.student.gender) === 'ស').length;
    const counts = {
        A: { t: 0, f: 0 }, B: { t: 0, f: 0 }, C: { t: 0, f: 0 },
        D: { t: 0, f: 0 }, E: { t: 0, f: 0 }, F: { t: 0, f: 0 },
        NONE: { t: 0, f: 0 }
    };
    results.forEach(r => {
        const isF = getShortGender(r.student.gender) === 'ស';
        const code = (r.avg !== null && r.avg !== undefined && r.mention && r.mention.code && r.mention.code !== '-') ? r.mention.code : 'NONE';
        if (counts[code]) {
            counts[code].t++;
            if (isF) counts[code].f++;
        }
    });
    const pct = n => total > 0 ? Number(((n / total) * 100).toFixed(2)) : 0;
    const cfg = HR.admin.config || {};
    const lunarDate = cfg.lunarDate || 'ថ្ងៃ............ខែ............ឆ្នាំ............ ព.ស. ២៥......';
    const solarDate = cfg.solarDate || '............, ថ្ងៃទី...... ខែ............ ឆ្នាំ២០២......';
    const principalName = cfg.principalName || HR.teacher.principalName || '';
    const teacherName = HR.teacher.fullName || '';

    return `
        <div class="moeys-summary-grid">
            <div>
                <div>-បញ្ឈប់បញ្ជីត្រឹមចំនួនសិស្ស <b>${total}នាក់</b> ស្រី <b>${female}នាក់</b></div>
                <div>-សិស្សនិទ្ទេស A &nbsp;&nbsp; សរុប៖ ${counts.A.t}នាក់ ស្រី ${counts.A.f}នាក់ ស្មើនឹង ${pct(counts.A.t)}%</div>
                <div>-សិស្សនិទ្ទេស B &nbsp;&nbsp; សរុប៖ ${counts.B.t}នាក់ ស្រី ${counts.B.f}នាក់ ស្មើនឹង ${pct(counts.B.t)}%</div>
                <div>-សិស្សនិទ្ទេស C &nbsp;&nbsp; សរុប៖ ${counts.C.t}នាក់ ស្រី ${counts.C.f}នាក់ ស្មើនឹង ${pct(counts.C.t)}%</div>
            </div>
            <div>
                <div>-សិស្សនិទ្ទេស D &nbsp;&nbsp; សរុប៖ ${counts.D.t}នាក់ ស្រី ${counts.D.f}នាក់ ស្មើនឹង ${pct(counts.D.t)}%</div>
                <div>-សិស្សនិទ្ទេស E &nbsp;&nbsp; សរុប៖ ${counts.E.t}នាក់ ស្រី ${counts.E.f}នាក់ ស្មើនឹង ${pct(counts.E.t)}%</div>
                <div>-សិស្សនិទ្ទេស F &nbsp;&nbsp; សរុប៖ ${counts.F.t}នាក់ ស្រី ${counts.F.f}នាក់ ស្មើនឹង ${pct(counts.F.t)}%</div>
                <div>-គ្មានចំណាត់ថ្នាក់ &nbsp; សរុប៖ ${counts.NONE.t}នាក់ ស្រី ${counts.NONE.f}នាក់ ស្មើនឹង ${pct(counts.NONE.t)}%</div>
            </div>
        </div>
        <div class="moeys-sig-row">
            <div style="text-align:center;min-width:220px;padding-top:24px;">
                <div>បានឃើញ និងឯកភាព</div>
                <div style="margin-top:4px;font-weight:700;">នាយកសាលា</div>
                <div class="moul" style="margin-top:48px;font-size:0.92rem;">${esc(principalName)}</div>
            </div>
            <div style="text-align:center;min-width:260px;">
                <div>${esc(lunarDate)}</div>
                <div style="margin-top:2px;">${esc(solarDate)}</div>
                <div style="margin-top:4px;font-weight:700;">គ្រូបន្ទុកថ្នាក់</div>
                <div class="moul" style="margin-top:44px;font-size:0.92rem;">${esc(teacherName)}</div>
            </div>
        </div>`;
}

function renderScoreEntryTable() {
    const period = document.getElementById('scorePeriodSelect').value;
    const periodKh = getScorePeriodKhmerLabel(period);
    const banner = document.getElementById('scoreSheetTopBanner');
    const thead = document.getElementById('scoreEntryHead');
    const tbody = document.getElementById('scoreEntryBody');
    const footer = document.getElementById('scoreSheetFooter');

    if (HR.subjects.length === 0) {
        if (banner) banner.innerHTML = '';
        thead.innerHTML = '';
        tbody.innerHTML = `<tr><td style="padding:24px;color:#64748b;">សូមចូល Tab <b>«១. រៀបចំថ្នាក់ &amp; មុខវិជ្ជា»</b> ដើម្បីបង្កើតមុខវិជ្ជាជាមុនសិន។</td></tr>`;
        if (footer) footer.innerHTML = '';
        return;
    }

    const results = computeSinglePeriod(period);
    const totalCount = results.length;
    const femaleCount = results.filter(r => getShortGender(r.student.gender) === 'ស').length;

    if (banner) {
        banner.innerHTML = `
            <div class="moeys-top-banner">
                <span>តារាងស្រង់ពិន្ទុសម្រាប់${esc(periodKh)}</span>
                <span>ថ្នាក់ទី ${esc(HR.className)} ឆ្នាំសិក្សា ${esc(HR.academicYear)}</span>
                <span>-បញ្ឈប់បញ្ជីត្រឹមចំនួនសិស្ស ${totalCount}នាក់ ស្រី ${femaleCount}នាក់</span>
            </div>`;
    }

    thead.innerHTML = `
        <tr>
            <th rowspan="3" class="ms-shade ms-sticky-1">ល.រ</th>
            <th rowspan="3" class="ms-shade ms-sticky-2" style="min-width:165px;">គោត្តនាម-នាម</th>
            <th rowspan="3" class="ms-shade" style="width:42px;">ភេទ</th>
            <th colspan="${HR.subjects.length}" style="font-weight:700;font-size:0.95rem;background:#fff;padding:6px;">តារាងស្រង់ពិន្ទុសម្រាប់${esc(periodKh)}</th>
            <th rowspan="3" class="ms-shade" style="width:46px;"><div class="ms-vsum">ពិន្ទុសរុប</div></th>
            <th rowspan="3" class="ms-shade" style="width:52px;"><div class="ms-vsum">មធ្យមភាគ</div></th>
            <th rowspan="3" class="ms-shade" style="width:46px;"><div class="ms-vsum">ចំណាត់ថ្នាក់</div></th>
            <th rowspan="3" class="ms-shade" style="width:46px;"><div class="ms-vsum">និទ្ទេស</div></th>
        </tr>
        <tr>
            ${HR.subjects.map(s => `<th class="ms-sub-col" style="font-size:0.86rem;background:#fff;padding:3px 2px;">${s.maxScore != null ? s.maxScore : 0}</th>`).join('')}
        </tr>
        <tr>
            ${HR.subjects.map(s => `<th class="ms-sub-col" style="background:#fff;padding:2px;"><div class="ms-vsub">${esc(s.subjectName)}</div></th>`).join('')}
        </tr>
    `;

    tbody.innerHTML = results.map((r, i) => {
        const hasScore = (r.total !== null && r.total !== undefined);
        const totalTxt = hasScore ? (Number.isInteger(r.total) ? String(r.total) : r.total.toFixed(2)) : '0';
        const avgTxt = (r.avg !== null && r.avg !== undefined) ? r.avg.toFixed(2) : '0.00';
        const rankTxt = r.rank ? String(r.rank) : '-';
        const mentionTxt = (r.avg !== null && r.avg !== undefined && r.mention) ? r.mention.code : '-';
        return `
        <tr data-student-id="${r.student.id}">
            <td class="ms-sticky-1">${i + 1}</td>
            <td class="ms-name ms-sticky-2">${esc(r.student.name)}</td>
            <td>${getShortGender(r.student.gender)}</td>
            ${HR.subjects.map(sub => {
                const v = r.subScores[sub.id];
                return `<td class="ms-sub-col"><input type="number" step="0.25" min="0" max="${sub.maxScore}" class="score-input moeys-score-inp" data-sid="${r.student.id}" data-subid="${sub.id}" value="${v !== null && v !== undefined ? v : ''}" oninput="onScoreInputLive(this)"></td>`;
            }).join('')}
            <td class="cell-total">${totalTxt}</td>
            <td class="cell-avg">${avgTxt}</td>
            <td class="cell-rank ms-rank">${rankTxt}</td>
            <td class="cell-mention ms-mention">${mentionTxt}</td>
        </tr>`;
    }).join('');

    if (footer) {
        footer.innerHTML = buildMoeysGradeSummaryHtml(results);
    }
}

function onScoreInputLive(input) {
    const period = document.getElementById('scorePeriodSelect').value;
    const sid = input.getAttribute('data-sid');
    const subid = input.getAttribute('data-subid');
    const maxVal = parseFloat(input.getAttribute('max')) || 100;
    let val = input.value === '' ? null : parseFloat(input.value);
    if (val !== null && val > maxVal) {
        val = maxVal;
        input.value = maxVal;
    }
    const key = `${period}_${sid}_${subid}`;
    if (val === null || isNaN(val)) delete HR.scoreMap[key];
    else HR.scoreMap[key] = val;

    // Live recalculate totals, averages, ranks, mentions, and bottom summary
    const results = computeSinglePeriod(period);
    const byId = {};
    results.forEach(r => { byId[String(r.student.id)] = r; });
    document.querySelectorAll('#scoreEntryBody tr[data-student-id]').forEach(tr => {
        const r = byId[tr.getAttribute('data-student-id')];
        if (!r) return;
        const hasScore = (r.total !== null && r.total !== undefined);
        tr.querySelector('.cell-total').textContent = hasScore ? (Number.isInteger(r.total) ? String(r.total) : r.total.toFixed(2)) : '0';
        tr.querySelector('.cell-avg').textContent = (r.avg !== null && r.avg !== undefined) ? r.avg.toFixed(2) : '0.00';
        tr.querySelector('.cell-rank').textContent = r.rank ? String(r.rank) : '-';
        tr.querySelector('.cell-mention').textContent = (r.avg !== null && r.avg !== undefined && r.mention) ? r.mention.code : '-';
    });
    const footer = document.getElementById('scoreSheetFooter');
    if (footer) footer.innerHTML = buildMoeysGradeSummaryHtml(results);
}

function makeVerticalKhmerHeaderImg(text, isBold = false, heightPx = 118, widthPx = 30, fontSizePx = 14) {
    const scale = 3; // 3x HiDPI for razor-sharp Khmer text in A3 PDF
    const c = document.createElement('canvas');
    c.width = widthPx * scale;
    c.height = heightPx * scale;
    const ctx = c.getContext('2d');
    ctx.scale(scale, scale);
    ctx.fillStyle = '#000000';
    ctx.font = `${isBold ? '600' : '400'} ${fontSizePx}px "Kantumruy Pro", "Battambang", sans-serif`;
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.translate(widthPx / 2, heightPx / 2);
    ctx.rotate(-Math.PI / 2);
    ctx.fillText(String(text || ''), 0, 0, heightPx - 6);
    return `<img src="${c.toDataURL('image/png')}" alt="${esc(text)}" style="width:${widthPx}px;height:${heightPx}px;display:block;margin:0 auto;">`;
}

async function downloadMoeysScoreSheetPdf() {
    if (HR.subjects.length === 0) return;
    const JsPdfCtor = (window.jspdf && window.jspdf.jsPDF) ? window.jspdf.jsPDF : window.jsPDF;
    if (typeof html2canvas === 'undefined' || !JsPdfCtor) { window.print(); return; }

    // Ensure Khmer web fonts are fully loaded before drawing vertical canvas headers
    if (document.fonts && document.fonts.ready) {
        try { await document.fonts.ready; } catch (e) {}
    }

    const period = document.getElementById('scorePeriodSelect').value;
    const periodKh = getScorePeriodKhmerLabel(period);
    const results = computeSinglePeriod(period);
    const totalCount = results.length;
    const femaleCount = results.filter(r => getShortGender(r.student.gender) === 'ស').length;

    // Put ALL students (e.g. #1 to #47) on Page 1 of A3 Landscape (420mm x 297mm) filling 100% edge-to-edge left & right with larger text
    const stageW = 1920;
    const includeFooterOnPage1 = (totalCount <= 32);
    const availRowsH = includeFooterOnPage1 ? 820 : 1140;
    const rowH = Math.max(21, Math.min(32, Math.floor(availRowsH / Math.max(totalCount, 1))));
    const rowFont = rowH <= 22 ? 15 : 16;
    const cellPad = '1px 3px';

    // Use 3x HiDPI canvas-rendered vertical Khmer labels (larger, razor-sharp)
    const vTotalImg = makeVerticalKhmerHeaderImg('ពិន្ទុសរុប', true, 102, 34, 16);
    const vAvgImg = makeVerticalKhmerHeaderImg('មធ្យមភាគ', true, 102, 34, 16);
    const vRankImg = makeVerticalKhmerHeaderImg('ចំណាត់ថ្នាក់', true, 102, 34, 16);
    const vMentionImg = makeVerticalKhmerHeaderImg('និទ្ទេស', true, 102, 34, 16);
    const subImgs = HR.subjects.map(s => makeVerticalKhmerHeaderImg(s.subjectName, false, 98, 32, 15.5));

    const theadHtml = `
        <thead>
            <tr>
                <th rowspan="3" class="ms-shade" style="width:46px;font-size:16px;">ល.រ</th>
                <th rowspan="3" class="ms-shade" style="width:225px;font-size:16.5px;">គោត្តនាម-នាម</th>
                <th rowspan="3" class="ms-shade" style="width:48px;font-size:16px;">ភេទ</th>
                <th colspan="${HR.subjects.length}" style="font-weight:700;font-size:17px;background:#fff;padding:5px;">តារាងស្រង់ពិន្ទុសម្រាប់${esc(periodKh)}</th>
                <th rowspan="3" class="ms-shade" style="width:66px;padding:2px;">${vTotalImg}</th>
                <th rowspan="3" class="ms-shade" style="width:74px;padding:2px;">${vAvgImg}</th>
                <th rowspan="3" class="ms-shade" style="width:64px;padding:2px;">${vRankImg}</th>
                <th rowspan="3" class="ms-shade" style="width:64px;padding:2px;">${vMentionImg}</th>
            </tr>
            <tr>
                ${HR.subjects.map(s => `<th style="font-size:15px;background:#fff;padding:3px 2px;font-weight:400;">${s.maxScore != null ? s.maxScore : 0}</th>`).join('')}
            </tr>
            <tr>
                ${subImgs.map(img => `<th style="background:#fff;padding:2px 1px;">${img}</th>`).join('')}
            </tr>
        </thead>`;

    const bannerHtml = `
        <div class="moeys-top-banner" style="font-size:18px;padding:6px 14px;margin-bottom:4px;">
            <span>តារាងស្រង់ពិន្ទុសម្រាប់${esc(periodKh)}</span>
            <span>ថ្នាក់ទី ${esc(HR.className)} ឆ្នាំសិក្សា ${esc(HR.academicYear)}</span>
            <span>-បញ្ឈប់បញ្ជីត្រឹមចំនួនសិស្ស ${totalCount}នាក់ ស្រី ${femaleCount}នាក់</span>
        </div>`;

    const colgroupHtml = `<colgroup>
        <col style="width:46px;">
        <col style="width:225px;">
        <col style="width:48px;">
        ${HR.subjects.map(() => `<col>`).join('')}
        <col style="width:66px;">
        <col style="width:74px;">
        <col style="width:64px;">
        <col style="width:64px;">
    </colgroup>`;

    const allBodyRows = results.map((r, ri) => {
        const hasScore = (r.total !== null && r.total !== undefined);
        const totalTxt = hasScore ? (Number.isInteger(r.total) ? String(r.total) : r.total.toFixed(2)) : '0';
        const avgTxt = (r.avg !== null && r.avg !== undefined) ? r.avg.toFixed(2) : '0.00';
        const rankTxt = r.rank ? String(r.rank) : '-';
        const mentionTxt = (r.avg !== null && r.avg !== undefined && r.mention) ? r.mention.code : '-';
        return `
            <tr style="height:${rowH}px;">
                <td style="padding:${cellPad};line-height:1.12;">${ri + 1}</td>
                <td class="ms-name" style="position:static;box-shadow:none;font-size:${rowFont + 0.5}px;padding:${cellPad} 10px;line-height:1.12;">${esc(r.student.name)}</td>
                <td style="padding:${cellPad};line-height:1.12;">${getShortGender(r.student.gender)}</td>
                ${HR.subjects.map(sub => {
                    const v = r.subScores[sub.id];
                    return `<td style="padding:${cellPad};line-height:1.12;">${v !== null && v !== undefined ? v : ''}</td>`;
                }).join('')}
                <td style="padding:${cellPad};line-height:1.12;">${totalTxt}</td>
                <td style="padding:${cellPad};line-height:1.12;">${avgTxt}</td>
                <td class="ms-rank" style="padding:${cellPad};line-height:1.12;">${rankTxt}</td>
                <td class="ms-mention" style="padding:${cellPad};line-height:1.12;">${mentionTxt}</td>
            </tr>`;
    }).join('');

    const stage = document.getElementById('moeysPdfStage');
    stage.style.width = stageW + 'px';
    let stageHtml = `
        <div class="moeys-pdf-page" style="width:${stageW}px;background:#fff;padding:2px 2px 4px 2px;box-sizing:border-box;">
            ${bannerHtml}
            <table class="moeys-score-table" style="width:100%;min-width:100%;border-collapse:collapse;table-layout:fixed;font-size:${rowFont}px;">
                ${colgroupHtml}
                ${theadHtml}
                <tbody>${allBodyRows}</tbody>
            </table>
            ${includeFooterOnPage1 ? buildMoeysGradeSummaryHtml(results) : ''}
        </div>`;
    if (!includeFooterOnPage1) {
        stageHtml += `
            <div class="moeys-pdf-page" style="width:${stageW}px;background:#fff;padding:10px 18px;box-sizing:border-box;">
                ${buildMoeysGradeSummaryHtml(results)}
            </div>`;
    }
    stage.innerHTML = stageHtml;

    const pages = Array.from(stage.querySelectorAll('.moeys-pdf-page'));
    Swal.fire({
        title: 'កំពុងបង្កើតឯកសារ PDF (ក្រដាស A3 ផ្តេក)...',
        html: `<div id="moeysPdfProg">ទំព័រទី <b>1</b> / <b>${pages.length}</b></div>`,
        allowOutsideClick: false,
        didOpen: () => Swal.showLoading()
    });

    try {
        const pdf = new JsPdfCtor({ orientation: 'landscape', unit: 'mm', format: 'a3' });
        const pageW = pdf.internal.pageSize.getWidth();  // 420mm
        const pageH = pdf.internal.pageSize.getHeight(); // 297mm
        const marginX = 2.5;
        const maxW = pageW - marginX * 2; // 415mm full edge-to-edge width!
        const maxH = pageH - 5;

        const canvases = [];
        for (let i = 0; i < pages.length; i++) {
            const prog = document.getElementById('moeysPdfProg');
            if (prog) prog.innerHTML = `ទំព័រទី <b>${i + 1}</b> / <b>${pages.length}</b>`;
            const canvas = await html2canvas(pages[i], {
                scale: 2, useCORS: true, backgroundColor: '#ffffff', logging: false, width: stageW, windowWidth: stageW + 40
            });
            canvases.push(canvas);
            await new Promise(r => setTimeout(r, 15));
        }

        for (let i = 0; i < canvases.length; i++) {
            const cv = canvases[i];
            const imgData = cv.toDataURL('image/jpeg', 0.96);
            // Always span 100% edge-to-edge left & right (415mm) while clamping height to maxH cleanly
            const drawW = maxW;
            const drawH = Math.min(maxH, cv.height * (maxW / cv.width));
            const topY = (i === 0) ? 2.5 : 5;

            if (i > 0) pdf.addPage('a3', 'landscape');
            pdf.addImage(imgData, 'JPEG', marginX, topY, drawW, drawH, undefined, 'FAST');
        }

        pdf.save(`ScoreSheet_A3_Landscape_${HR.className}_${periodKh}_${HR.academicYear}.pdf`);
        Swal.close();
    } catch (e) {
        console.error(e);
        Swal.close();
        window.print();
    } finally {
        stage.innerHTML = '';
    }
}

async function saveCurrentPeriodScores() {
    const period = document.getElementById('scorePeriodSelect').value;
    const entries = [];
    document.querySelectorAll('#scoreEntryBody .score-input').forEach(inp => {
        entries.push({
            studentId: Number(inp.getAttribute('data-sid')),
            subjectId: Number(inp.getAttribute('data-subid')),
            score: inp.value === '' ? null : parseFloat(inp.value)
        });
    });

    const res = await fetch('/api/homeroom/scores/save', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ className: HR.className, academicYear: HR.academicYear, period, entries })
    });
    const data = await res.json();
    if (data.success) {
        HR.scores = data.scores || [];
        rebuildScoreMap();
        renderScoreEntryTable();
        renderDashboardAndSubjects();
        Swal.fire({ icon: 'success', title: 'រក្សាទុកពិន្ទុជោគជ័យ!', text: `បានរក្សាទុក ${data.savedCount} ប្រឡោះពិន្ទុ`, timer: 1400, showConfirmButton: false });
    } else {
        Swal.fire('បរាជ័យ', data.error || 'មានបញ្ហាក្នុងការរក្សាទុក', 'error');
    }
}

function exportScoresExcel() {
    const period = document.getElementById('scorePeriodSelect').value;
    const results = computeSinglePeriod(period);
    const rows = results.map((r, i) => {
        const obj = { 'ល.រ': i + 1, 'គោត្តនាម និងនាម': r.student.name, 'ភេទ': r.student.gender };
        HR.subjects.forEach(s => { obj[s.subjectName] = r.subScores[s.id] !== null ? r.subScores[s.id] : ''; });
        obj['ពិន្ទុសរុប'] = r.total !== null ? Number(r.total.toFixed(2)) : '';
        obj['មធ្យមភាគ'] = r.avg !== null ? Number(r.avg.toFixed(2)) : '';
        obj['និទ្ទេស'] = r.mention.code;
        obj['ចំណាត់ថ្នាក់'] = r.rank || '';
        return obj;
    });
    const ws = XLSX.utils.json_to_sheet(rows);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, period);
    XLSX.writeFile(wb, `Scores_${HR.className}_${period}.xlsx`);
}

function getMonthlyAttCountsForPeriod(view, student) {
    const mMap = { M1: 11, M2: 12, M3: 1, M4: 2, M5: 3, M6: 4, M7: 5, M8: 6, M9: 7, M10: 8 };
    const monthNum = mMap[view];
    if (monthNum && HR.admin && HR.admin.monthlyAtt) {
        // Parse academic year start/end to find matching YYYY-MM key in monthlyAtt
        const ayAscii = String(HR.academicYear || '').replace(/[០-៩]/g, c => '០១២៣៤៥៦៧៨៩'.indexOf(c));
        const yrs = ayAscii.match(/\d{4}/g);
        const startY = yrs && yrs.length >= 1 ? parseInt(yrs[0], 10) : new Date().getFullYear();
        const endY = yrs && yrs.length >= 2 ? parseInt(yrs[1], 10) : startY + 1;
        const targetY = (monthNum >= 11) ? startY : endY;
        const key = `${targetY}-${String(monthNum).padStart(2, '0')}`;
        const mData = HR.admin.monthlyAtt[key];
        if (mData && mData.marks) {
            let perm = 0, unex = 0;
            const prefix = `${student.id}_`;
            Object.keys(mData.marks).forEach(k => {
                if (k.startsWith(prefix)) {
                    const mk = mData.marks[k];
                    if (mk === 'ច') perm++;
                    else if (mk === 'អ') unex++;
                }
            });
            return { perm, unex, tot: perm + unex };
        }
    }
    const perm = student.absentPermission || 0;
    const unex = student.absentUnexcused || 0;
    return { perm, unex, tot: student.absentTotal != null ? student.absentTotal : (perm + unex) };
}

async function downloadRankingA4PortraitPdf() {
    const el = document.getElementById('rankingA4Sheet') || document.querySelector('#rankingsPrintArea .print-sheet');
    if (!el) return;
    const JsPdfCtor = (window.jspdf && window.jspdf.jsPDF) ? window.jspdf.jsPDF : window.jsPDF;
    if (typeof html2canvas === 'undefined' || !JsPdfCtor) { window.print(); return; }

    if (document.fonts && document.fonts.ready) {
        try { await document.fonts.ready; } catch (e) {}
    }

    const view = document.getElementById('rankViewSelect').value;
    const viewLabel = getScorePeriodKhmerLabel(view);

    Swal.fire({
        title: 'កំពុងបង្កើតឯកសារ PDF (A4 បញ្ឈរ ១សន្លឹក)...',
        allowOutsideClick: false,
        didOpen: () => Swal.showLoading()
    });

    try {
        const prevBorder = el.style.border, prevRadius = el.style.borderRadius, prevMaxW = el.style.maxWidth, prevW = el.style.width;
        el.style.border = 'none'; el.style.borderRadius = '0'; el.style.maxWidth = '960px'; el.style.width = '960px';

        const canvas = await html2canvas(el, {
            scale: 2, useCORS: true, backgroundColor: '#ffffff', logging: false, width: 960, windowWidth: 1000
        });

        el.style.border = prevBorder; el.style.borderRadius = prevRadius; el.style.maxWidth = prevMaxW; el.style.width = prevW;

        const pdf = new JsPdfCtor({ orientation: 'portrait', unit: 'mm', format: 'a4' });
        const pageW = pdf.internal.pageSize.getWidth();  // 210mm
        const pageH = pdf.internal.pageSize.getHeight(); // 297mm
        const margin = 4;
        const maxW = pageW - margin * 2;
        const maxH = pageH - margin * 2;

        const imgData = canvas.toDataURL('image/jpeg', 0.96);
        const ratio = Math.min(maxW / canvas.width, maxH / canvas.height);
        const drawW = canvas.width * ratio;
        const drawH = canvas.height * ratio;
        const offsetX = margin + (maxW - drawW) / 2;

        pdf.addImage(imgData, 'JPEG', offsetX, margin, drawW, drawH, undefined, 'FAST');
        pdf.save(`Ranking_A4_${HR.className}_${viewLabel}_${HR.academicYear}.pdf`);
        Swal.close();
    } catch (e) {
        console.error(e);
        Swal.close();
        window.print();
    }
}

// ==================== TAB 3: RANKINGS, SEMESTERS, PROMOTION & TEACHER RANKS ====================
function renderRankingsView() {
    const view = document.getElementById('rankViewSelect').value;
    const colsMode = document.getElementById('rankColsMode').value;
    const area = document.getElementById('rankingsPrintArea');

    if (view === 'PROMOTION') {
        const ann = computeAnnual();
        const up = ann.filter(r => r.promoted === true).sort((a, b) => (a.rank || 999) - (b.rank || 999));
        const down = ann.filter(r => r.promoted === false);
        area.innerHTML = `
            <div class="print-sheet">
                ${getOfficialHeader('បញ្ជីឈ្មោះសិស្សឡើងថ្នាក់ និងត្រួតថ្នាក់ប្រចាំឆ្នាំសិក្សា', `សិស្សឡើងថ្នាក់៖ ${up.length} នាក់ | សិស្សត្រួតថ្នាក់៖ ${down.length} នាក់`)}
                <h4 style="color:#15803d;margin:10px 0 6px;">✅ ១. បញ្ជីឈ្មោះសិស្សត្រូវឡើងថ្នាក់ (${up.length} នាក់)</h4>
                <table class="hr-table">
                    <thead><tr><th>ល.រ</th><th>អត្តលេខ</th><th>គោត្តនាម និងនាម</th><th>ភេទ</th><th>មធ្យម.ឆ១</th><th>មធ្យម.ឆ២</th><th>មធ្យម.ប្រចាំឆ្នាំ</th><th>និទ្ទេស</th><th>ចំណាត់ថ្នាក់</th></tr></thead>
                    <tbody>
                        ${up.length ? up.map((r, i) => `<tr><td>${i + 1}</td><td>${esc(r.student.studentCode)}</td><td class="text-left" style="font-weight:700;">${esc(r.student.name)}</td><td>${esc(r.student.gender)}</td><td>${fmtNum(r.s1Avg)}</td><td>${fmtNum(r.s2Avg)}</td><td style="font-weight:800;color:#15803d;">${fmtNum(r.avg)}</td><td><span class="badge badge-${r.mention.code}">${r.mention.code} (${r.mention.kh})</span></td><td style="font-weight:800;">${r.rank}</td></tr>`).join('') : '<tr><td colspan="9">មិនទាន់មានទិន្នន័យ</td></tr>'}
                    </tbody>
                </table>
                <h4 style="color:#b91c1c;margin:18px 0 6px;">⚠️ ២. បញ្ជីឈ្មោះសិស្សត្រួតថ្នាក់ (${down.length} នាក់)</h4>
                <table class="hr-table">
                    <thead><tr><th>ល.រ</th><th>អត្តលេខ</th><th>គោត្តនាម និងនាម</th><th>ភេទ</th><th>មធ្យម.ឆ១</th><th>មធ្យម.ឆ២</th><th>មធ្យម.ប្រចាំឆ្នាំ</th><th>និទ្ទេស</th><th>ផ្សេងៗ</th></tr></thead>
                    <tbody>
                        ${down.length ? down.map((r, i) => `<tr><td>${i + 1}</td><td>${esc(r.student.studentCode)}</td><td class="text-left" style="font-weight:700;">${esc(r.student.name)}</td><td>${esc(r.student.gender)}</td><td>${fmtNum(r.s1Avg)}</td><td>${fmtNum(r.s2Avg)}</td><td style="font-weight:800;color:#b91c1c;">${fmtNum(r.avg)}</td><td><span class="badge badge-${r.mention.code}">${r.mention.code}</span></td><td>ត្រួតថ្នាក់</td></tr>`).join('') : '<tr><td colspan="9">គ្មានសិស្សត្រួតថ្នាក់</td></tr>'}
                    </tbody>
                </table>
                ${getOfficialFooter()}
            </div>`;
        return;
    }

    if (view === 'TEACHER_RANKS') {
        const period = document.getElementById('scorePeriodSelect').value || 'M1';
        const stats = HR.subjects.map(sub => {
            const counts = { A: 0, B: 0, C: 0, D: 0, E: 0, F: 0 };
            let sumPct = 0, cnt = 0, pass = 0;
            HR.students.forEach(st => {
                const sc = HR.scoreMap[`${period}_${st.id}_${sub.id}`];
                if (sc !== undefined) {
                    const m = getSubjectMention(sc, sub.maxScore);
                    if (counts[m] !== undefined) counts[m]++;
                    const pct = (sc / sub.maxScore) * 100;
                    sumPct += pct; cnt++;
                    if (pct >= 50) pass++;
                }
            });
            return { sub, counts, cnt, pass, passPct: cnt ? (pass / cnt) * 100 : 0, avgPct: cnt ? (sumPct / cnt) : 0 };
        }).sort((a, b) => b.passPct - a.passPct);

        area.innerHTML = `
            <div class="print-sheet">
                ${getOfficialHeader('តារាងរាប់និទ្ទេសតាមមុខវិជ្ជា និងចំណាត់ថ្នាក់គ្រូមុខវិជ្ជា', `ប្រចាំ ${PERIOD_LABELS[period] || period}`)}
                <table class="hr-table">
                    <thead>
                        <tr><th>ចំណាត់ថ្នាក់</th><th>មុខវិជ្ជា</th><th>គ្រូបង្រៀន</th><th>A</th><th>B</th><th>C</th><th>D</th><th>E</th><th>F</th><th>សិស្សជាប់</th><th>ភាគរយជាប់ (%)</th></tr>
                    </thead>
                    <tbody>
                        ${stats.map((st, i) => `
                            <tr>
                                <td style="font-weight:800;">${i + 1}</td>
                                <td class="text-left" style="font-weight:700;">${esc(st.sub.subjectName)}</td>
                                <td class="text-left">${esc(st.sub.teacherName || '-')}</td>
                                <td>${st.counts.A}</td><td>${st.counts.B}</td><td>${st.counts.C}</td><td>${st.counts.D}</td><td>${st.counts.E}</td><td>${st.counts.F}</td>
                                <td>${st.pass}/${st.cnt}</td>
                                <td style="font-weight:800;color:#1d4ed8;">${st.passPct.toFixed(1)}%</td>
                            </tr>`).join('')}
                    </tbody>
                </table>
                ${getOfficialFooter()}
            </div>`;
        return;
    }

    const rawResults = computeResultsForPeriod(view);
    const results = rawResults.slice().sort((a, b) => (a.rank || 9999) - (b.rank || 9999));
    const periodKh = getScorePeriodKhmerLabel(view);
    const title = `តារាងចំណាត់ថ្នាក់សិស្ស ${PERIOD_LABELS[view] || view}`;
    const valid = results.filter(r => r.avg !== null);
    const passCount = valid.filter(r => r.avg >= 25).length;
    const subtitle = `សិស្សសរុប៖ ${results.length} នាក់ | ជាប់មធ្យមភាគ៖ ${passCount} នាក់ (${valid.length ? ((passCount / valid.length) * 100).toFixed(1) : 0}%)`;

    if (colsMode === '2') {
        const isSem1 = (view === 'S1');
        const isSem2 = (view === 'S2');
        const isYear = (view === 'YEAR');
        const isSemOrYear = (isSem1 || isSem2 || isYear);

        // Always sort by Rank ascending (1, 2, 3...) for Monthly, Semester 1/2, and Annual rankings
        const list = results;
        const half = Math.ceil(list.length / 2);
        const leftList = list.slice(0, half);
        const rightList = list.slice(half);
        const cfg = HR.admin.config || {};
        const ministry = cfg.ministry || 'មន្ទីរអប់រំ យុវជន និងកីឡា';
        const school = cfg.school || HR.teacher.schoolName || 'វិទ្យាល័យ';
        const rkRowH = Math.max(24, Math.min(36, Math.floor(805 / Math.max(half, 1))));

        const headingPeriod = isSem1 ? 'ប្រចាំឆមាសទី១'
            : isSem2 ? 'ប្រចាំឆមាសទី២'
            : isYear ? 'ប្រចាំឆ្នាំសិក្សា'
            : periodKh;

        if (isSemOrYear) {
            const col4Label = isYear ? 'ម.ឆ១' : 'ម.ខែ';
            const col5Label = isYear ? 'ម.ឆ២' : (isSem1 ? 'ម.ឆ១' : 'ម.ឆ២');
            const col6Label = isYear ? 'ម.ប្រឆ្នាំ' : (isSem1 ? 'ម.ប្រឆ១' : 'ម.ប្រឆ២');

            const renderSemCells = (r, no, isRightStart) => {
                const midCls = isRightStart ? 'rk-mid-border' : '';
                if (!r) {
                    return `<td class="${midCls}"></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td>`;
                }
                const c4 = isYear ? r.s1Avg : r.avgMonths;
                const c5 = isYear ? r.s2Avg : r.examAvg;
                const c6 = r.avg;
                const fmt2 = v => (v !== null && v !== undefined) ? Number(v).toFixed(2) : '0.00';
                const rankTxt = r.rank ? String(r.rank) : '-';
                const mentionTxt = (c6 !== null && c6 !== undefined && r.mention) ? r.mention.code : '-';
                return `
                    <td class="${midCls}">${no}</td>
                    <td class="rk-name">${esc(r.student.name)}</td>
                    <td>${getShortGender(r.student.gender)}</td>
                    <td>${fmt2(c4)}</td>
                    <td>${fmt2(c5)}</td>
                    <td>${fmt2(c6)}</td>
                    <td class="rk-rank">${rankTxt}</td>
                    <td style="color:#1d4ed8;">${mentionTxt}</td>`;
            };

            let rowsHtml = '';
            for (let i = 0; i < half; i++) {
                rowsHtml += `<tr style="height:${rkRowH}px;">${renderSemCells(leftList[i], i + 1, false)}${renderSemCells(rightList[i] || null, half + i + 1, true)}</tr>`;
            }

            const semColgroup = `<colgroup>
                <col style="width:30px;"><col style="width:146px;"><col style="width:30px;"><col style="width:52px;"><col style="width:52px;"><col style="width:56px;"><col style="width:46px;"><col style="width:46px;">
                <col style="width:30px;"><col style="width:146px;"><col style="width:30px;"><col style="width:52px;"><col style="width:52px;"><col style="width:56px;"><col style="width:46px;"><col style="width:46px;">
            </colgroup>`;

            area.innerHTML = `
                <div id="rankingA4Sheet" class="rank-a4-sheet">
                    <div style="text-align:center;color:#000;line-height:1.6;">
                        <div class="moul" style="font-size:18px;">ព្រះរាជាណាចក្រកម្ពុជា</div>
                        <div class="moul" style="font-size:16px;">ជាតិ សាសនា ព្រះមហាក្សត្រ</div>
                        <div style="font-size:12.5px;letter-spacing:3px;margin-top:-2px;">~❖~</div>
                    </div>
                    <div style="text-align:left;color:#000;margin-top:-18px;margin-bottom:6px;line-height:1.65;">
                        <div class="moul" style="font-size:15px;">${esc(ministry)}</div>
                        <div class="moul" style="font-size:15px;">${esc(school)}</div>
                    </div>
                    <div style="text-align:center;color:#000;margin:4px 0 10px;line-height:1.65;">
                        <div class="moul" style="font-size:16px;">ចំណាត់ថ្នាក់សម្រាប់ ${esc(headingPeriod)}</div>
                        <div class="moul" style="font-size:15.5px;">ថ្នាក់ទី ${esc(HR.className)} ឆ្នាំសិក្សា ${esc(HR.academicYear)}</div>
                    </div>
                    <table class="rank-a4-table">
                        ${semColgroup}
                        <thead>
                            <tr style="height:34px;">
                                <th>ល.រ</th>
                                <th>គោត្តនាម-នាម</th>
                                <th>ភេទ</th>
                                <th>${col4Label}</th>
                                <th>${col5Label}</th>
                                <th>${col6Label}</th>
                                <th>ចំ.ថ្នាក់</th>
                                <th>និទ្ទេស</th>
                                <th class="rk-mid-border">ល.រ</th>
                                <th>គោត្តនាម-នាម</th>
                                <th>ភេទ</th>
                                <th>${col4Label}</th>
                                <th>${col5Label}</th>
                                <th>${col6Label}</th>
                                <th>ចំ.ថ្នាក់</th>
                                <th>និទ្ទេស</th>
                            </tr>
                        </thead>
                        <tbody>${rowsHtml}</tbody>
                    </table>
                    ${buildMoeysGradeSummaryHtml(list)}
                </div>`;
            return;
        }

        // Official MoEYS 1-Page A4 Portrait 2-Column Side-by-Side Monthly Ranking Sheet (Exact PDF Format)
        const renderCells = (r, no, isRightStart) => {
            const midCls = isRightStart ? 'rk-mid-border' : '';
            if (!r) {
                return `<td class="${midCls}"></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td>`;
            }
            const hasScore = (r.total !== null && r.total !== undefined);
            const totalTxt = hasScore ? (Number.isInteger(r.total) ? String(r.total) : r.total.toFixed(1)) : '0';
            const avgTxt = (r.avg !== null && r.avg !== undefined) ? r.avg.toFixed(2) : '0.00';
            const rankTxt = r.rank ? String(r.rank) : '-';
            const att = getMonthlyAttCountsForPeriod(view, r.student);
            return `
                <td class="${midCls}">${no}</td>
                <td class="rk-name">${esc(r.student.name)}</td>
                <td>${getShortGender(r.student.gender)}</td>
                <td>${totalTxt}</td>
                <td>${avgTxt}</td>
                <td class="rk-rank">${rankTxt}</td>
                <td>${att.perm}</td>
                <td>${att.unex}</td>
                <td>${att.tot}</td>`;
        };

        let rowsHtml = '';
        for (let i = 0; i < half; i++) {
            const lItem = leftList[i];
            const rItem = rightList[i] || null;
            rowsHtml += `<tr style="height:${rkRowH}px;">${renderCells(lItem, i + 1, false)}${renderCells(rItem, half + i + 1, true)}</tr>`;
        }

        const colgroup = `<colgroup>
            <col style="width:28px;"><col style="width:136px;"><col style="width:26px;"><col style="width:44px;"><col style="width:46px;"><col style="width:44px;"><col style="width:24px;"><col style="width:28px;"><col style="width:30px;">
            <col style="width:28px;"><col style="width:136px;"><col style="width:26px;"><col style="width:44px;"><col style="width:46px;"><col style="width:44px;"><col style="width:24px;"><col style="width:28px;"><col style="width:30px;">
        </colgroup>`;

        area.innerHTML = `
            <div id="rankingA4Sheet" class="rank-a4-sheet">
                <div style="text-align:center;color:#000;line-height:1.6;">
                    <div class="moul" style="font-size:18px;">ព្រះរាជាណាចក្រកម្ពុជា</div>
                    <div class="moul" style="font-size:16px;">ជាតិ សាសនា ព្រះមហាក្សត្រ</div>
                    <div style="font-size:12.5px;letter-spacing:3px;margin-top:-2px;">~❖~</div>
                </div>
                <div style="text-align:left;color:#000;margin-top:-18px;margin-bottom:6px;line-height:1.65;">
                    <div class="moul" style="font-size:15px;">${esc(ministry)}</div>
                    <div class="moul" style="font-size:15px;">${esc(school)}</div>
                </div>
                <div style="text-align:center;color:#000;margin:4px 0 10px;line-height:1.65;">
                    <div class="moul" style="font-size:16px;">ចំណាត់ថ្នាក់សម្រាប់ ${esc(periodKh)}</div>
                    <div class="moul" style="font-size:15.5px;">ថ្នាក់ទី ${esc(HR.className)} ឆ្នាំសិក្សា ${esc(HR.academicYear)}</div>
                </div>
                <table class="rank-a4-table">
                    ${colgroup}
                    <thead>
                        <tr>
                            <th rowspan="2">ល.រ</th>
                            <th rowspan="2">គោត្តនាម-នាម</th>
                            <th rowspan="2">ភេទ</th>
                            <th rowspan="2">ពិន្ទុសរុប</th>
                            <th rowspan="2">ម.ភាគ</th>
                            <th rowspan="2">ចំ.ថ្នាក់</th>
                            <th colspan="3">អវត្តមាន</th>
                            <th rowspan="2" class="rk-mid-border">ល.រ</th>
                            <th rowspan="2">គោត្តនាម-នាម</th>
                            <th rowspan="2">ភេទ</th>
                            <th rowspan="2">ពិន្ទុសរុប</th>
                            <th rowspan="2">ម.ភាគ</th>
                            <th rowspan="2">ចំ.ថ្នាក់</th>
                            <th colspan="3">អវត្តមាន</th>
                        </tr>
                        <tr>
                            <th>ច្ប</th><th>អច្ប</th><th>សរុប</th>
                            <th>ច្ប</th><th>អច្ប</th><th>សរុប</th>
                        </tr>
                    </thead>
                    <tbody>${rowsHtml}</tbody>
                </table>
                ${buildMoeysGradeSummaryHtml(list)}
            </div>`;
        return;
    }

    const extraColsHead = view === 'S1' || view === 'S2'
        ? '<th>មធ្យម.ខែ</th><th>ប្រឡងឆមាស</th>'
        : view === 'YEAR' ? '<th>មធ្យម.ឆ១</th><th>មធ្យម.ឆ២</th>' : '<th>ពិន្ទុសរុប</th>';

    area.innerHTML = `
        <div class="print-sheet">
            ${getOfficialHeader(title, subtitle)}
            <table class="hr-table">
                <thead>
                    <tr><th>ចំណាត់ថ្នាក់</th><th>គោត្តនាម និងនាម</th><th>ភេទ</th>${extraColsHead}<th>មធ្យមភាគ</th><th>និទ្ទេស</th><th>ច្បាប់</th><th>អត់ច្បាប់</th><th>អវត្ត.សរុប</th></tr>
                </thead>
                <tbody>
                    ${results.map(r => {
                        const extraCols = view === 'S1' || view === 'S2'
                            ? `<td>${fmtNum(r.avgMonths)}</td><td>${fmtNum(r.examAvg)}</td>`
                            : view === 'YEAR' ? `<td>${fmtNum(r.s1Avg)}</td><td>${fmtNum(r.s2Avg)}</td>`
                            : `<td>${fmtNum(r.total)}</td>`;
                        return `<tr>
                            <td style="font-weight:800;">${r.rank || '-'}</td>
                            <td class="text-left" style="font-weight:700;">${esc(r.student.name)}</td>
                            <td>${esc(r.student.gender)}</td>
                            ${extraCols}
                            <td style="font-weight:800;color:#1d4ed8;">${fmtNum(r.avg)}</td>
                            <td><span class="badge badge-${r.mention.code}">${r.mention.code} (${r.mention.kh})</span></td>
                            <td>${r.student.absentPermission || 0}</td>
                            <td>${r.student.absentUnexcused || 0}</td>
                            <td style="font-weight:700;">${r.student.absentTotal || 0}</td>
                        </tr>`;
                    }).join('')}
                </tbody>
            </table>
            ${getOfficialFooter()}
        </div>`;
}

function exportRankingsExcel() {
    const view = document.getElementById('rankViewSelect').value;
    const results = computeResultsForPeriod(view === 'PROMOTION' || view === 'TEACHER_RANKS' ? 'YEAR' : view)
        .sort((a, b) => (a.rank || 9999) - (b.rank || 9999));
    const rows = results.map(r => ({
        'ចំណាត់ថ្នាក់': r.rank || '',
        'អត្តលេខ': r.student.studentCode,
        'គោត្តនាម និងនាម': r.student.name,
        'ភេទ': r.student.gender,
        'មធ្យមភាគ': r.avg !== null ? Number(r.avg.toFixed(2)) : '',
        'និទ្ទេស': r.mention.code,
        'ផ្សេងៗ': r.mention.kh,
        'អវត្តមានមានច្បាប់': r.student.absentPermission || 0,
        'អវត្តមានគ្មានច្បាប់': r.student.absentUnexcused || 0,
        'អវត្តមានសរុប': r.student.absentTotal || 0
    }));
    const ws = XLSX.utils.json_to_sheet(rows);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Rankings');
    XLSX.writeFile(wb, `Rankings_${HR.className}_${view}.xlsx`);
}

// ==================== TAB 4: HONOR ROLL (2 OFFICIAL PDF TEMPLATES) & CERTIFICATES ====================
function buildHonorRibbonSlot(item, rankNum, withPhoto) {
    const khRank = toKhmerNum(rankNum);
    const nameText = item && item.student ? item.student.name : '..........................';
    const photoUrl = item && item.student && item.student.photoUrl ? item.student.photoUrl : '';

    // Exact Khmer ribbon banner SVG (soft pastel green #e2edd8, dark navy stroke #1e3a5f, folded tails + stars)
    const ribbonSvg = `
        <svg viewBox="0 0 360 86" width="356" height="84" style="position:absolute;inset:0;z-index:1;">
            <!-- Left ribbon tail -->
            <polygon points="8,24 58,24 58,78 8,78 30,51" fill="#e2edd8" stroke="#1e3a5f" stroke-width="1.4" stroke-linejoin="round"/>
            <polygon points="44,68 58,68 58,78" fill="#bdd3ab" stroke="#1e3a5f" stroke-width="1.2" stroke-linejoin="round"/>
            <!-- Right ribbon tail -->
            <polygon points="352,24 302,24 302,78 352,78 330,51" fill="#e2edd8" stroke="#1e3a5f" stroke-width="1.4" stroke-linejoin="round"/>
            <polygon points="316,68 302,68 302,78" fill="#bdd3ab" stroke="#1e3a5f" stroke-width="1.2" stroke-linejoin="round"/>
            <!-- Main center ribbon -->
            <rect x="44" y="12" width="272" height="56" fill="#e2edd8" stroke="#1e3a5f" stroke-width="1.5"/>
            <!-- Left star -->
            <polygon points="44,1 47,9 55,9 49,14 51,22 44,17 37,22 39,14 33,9 41,9" fill="#ffffff" stroke="#1e3a5f" stroke-width="1.3" stroke-linejoin="round"/>
            ${!withPhoto ? '<polygon points="316,1 319,9 327,9 321,14 323,22 316,17 309,22 311,14 305,9 313,9" fill="#ffffff" stroke="#1e3a5f" stroke-width="1.3" stroke-linejoin="round"/>' : ''}
        </svg>`;

    if (withPhoto) {
        return `
            <div class="honor-slot">
                <div class="honor-photo-box">
                    ${photoUrl ? `<img src="${esc(photoUrl)}" alt="Photo">` : ''}
                </div>
                <div class="honor-ribbon-wrap">
                    ${ribbonSvg}
                    <div class="honor-rank-circle" style="position:absolute;top:-22px;right:14px;">${khRank}</div>
                    <div class="honor-ribbon-name">${esc(nameText)}</div>
                </div>
            </div>`;
    } else {
        return `
            <div class="honor-slot">
                <div class="honor-rank-circle" style="margin-bottom:4px;">${khRank}</div>
                <div class="honor-ribbon-wrap">
                    ${ribbonSvg}
                    <div class="honor-ribbon-name">${esc(nameText)}</div>
                </div>
            </div>`;
    }
}

async function downloadHonorA4PortraitPdf() {
    const el = document.getElementById('honorA4Sheet') || document.querySelector('#honorCertArea .cert-frame');
    if (!el) return;
    const JsPdfCtor = (window.jspdf && window.jspdf.jsPDF) ? window.jspdf.jsPDF : window.jsPDF;
    if (typeof html2canvas === 'undefined' || !JsPdfCtor) { window.print(); return; }
    if (document.fonts && document.fonts.ready) { try { await document.fonts.ready; } catch (e) {} }

    const period = document.getElementById('honorPeriodSelect').value;
    const mode = document.getElementById('honorModeSelect').value;
    const periodLabel = getScorePeriodKhmerLabel(period);

    Swal.fire({
        title: 'កំពុងបង្កើត PDF តារាងកិត្តិយស (A4 បញ្ឈរ)...',
        allowOutsideClick: false,
        didOpen: () => Swal.showLoading()
    });

    try {
        const targets = document.getElementById('honorA4Sheet')
            ? [document.getElementById('honorA4Sheet')]
            : Array.from(document.querySelectorAll('#honorCertArea .cert-frame'));

        const pdf = new JsPdfCtor({ orientation: 'portrait', unit: 'mm', format: 'a4' });
        const pageW = pdf.internal.pageSize.getWidth();  // 210mm
        const pageH = pdf.internal.pageSize.getHeight(); // 297mm
        const margin = 4;
        const maxW = pageW - margin * 2;
        const maxH = pageH - margin * 2;

        for (let i = 0; i < targets.length; i++) {
            const node = targets[i];
            const prevB = node.style.border, prevR = node.style.borderRadius;
            node.style.border = 'none'; node.style.borderRadius = '0';

            const canvas = await html2canvas(node, {
                scale: 2, useCORS: true, backgroundColor: '#ffffff', logging: false, width: 840, windowWidth: 880
            });

            node.style.border = prevB; node.style.borderRadius = prevR;

            const imgData = canvas.toDataURL('image/jpeg', 0.96);
            const ratio = Math.min(maxW / canvas.width, maxH / canvas.height);
            const drawW = canvas.width * ratio;
            const drawH = canvas.height * ratio;
            const offsetX = margin + (maxW - drawW) / 2;

            if (i > 0) pdf.addPage('a4', 'portrait');
            pdf.addImage(imgData, 'JPEG', offsetX, margin, drawW, drawH, undefined, 'FAST');
            await new Promise(r => setTimeout(r, 15));
        }

        const tag = mode === 'HONOR_WITH_PHOTO' ? 'WithPhoto' : (mode === 'HONOR_NO_PHOTO' ? 'NoPhoto' : 'Cert');
        pdf.save(`HonorRoll_${tag}_A4_${HR.className}_${periodLabel}_${HR.academicYear}.pdf`);
        Swal.close();
    } catch (e) {
        console.error(e);
        Swal.close();
        window.print();
    }
}

function renderHonorAndCert() {
    const period = document.getElementById('honorPeriodSelect').value;
    const mode = document.getElementById('honorModeSelect').value;
    const theme = document.getElementById('certThemeSelect').value;
    const stuSel = document.getElementById('certStudentSelect');
    stuSel.style.display = mode === 'CERT_ONE' ? 'inline-block' : 'none';
    document.getElementById('certThemeSelect').style.display = (mode === 'CERT_TOP5' || mode === 'CERT_ONE') ? 'inline-block' : 'none';

    const results = computeResultsForPeriod(period).filter(r => r.rank !== null).sort((a, b) => a.rank - b.rank);
    const top5 = results.slice(0, 5);
    const area = document.getElementById('honorCertArea');

    if (mode === 'HONOR_NO_PHOTO' || mode === 'HONOR_WITH_PHOTO') {
        const withPhoto = (mode === 'HONOR_WITH_PHOTO');
        const cfg = HR.admin.config || {};
        const ministry = cfg.ministry || 'មន្ទីរអប់រំ យុវជន និងកីឡា';
        const school = cfg.school || HR.teacher.schoolName || 'វិទ្យាល័យ';
        const lunarDate = cfg.lunarDate || 'ថ្ងៃ............ខែ............ឆ្នាំ............ ព.ស. ២៥......';
        const solarDate = cfg.solarDate || '............, ថ្ងៃទី...... ខែ............ ឆ្នាំ២០២......';
        const principalName = cfg.principalName || HR.teacher.principalName || '';
        const teacherName = HR.teacher.fullName || '';
        const periodTitle = period === 'S1' ? 'ប្រចាំឆមាសទី១'
            : period === 'S2' ? 'ប្រចាំឆមាសទី២'
            : period === 'YEAR' ? 'ប្រចាំឆ្នាំសិក្សា'
            : getScorePeriodKhmerLabel(period);

        area.innerHTML = `
            <div style="overflow-x:auto;">
                <div id="honorA4Sheet" class="honor-a4-sheet">
                    <div>
                        <div style="text-align:center;color:#000;line-height:1.65;">
                            <div class="moul" style="font-size:18px;">ព្រះរាជាណាចក្រកម្ពុជា</div>
                            <div class="moul" style="font-size:16px;">ជាតិ សាសនា ព្រះមហាក្សត្រ</div>
                            <div style="font-size:13px;letter-spacing:3px;margin-top:-2px;">~❖~</div>
                        </div>
                        <div style="text-align:left;color:#000;margin-top:-12px;margin-bottom:8px;line-height:1.7;">
                            <div class="moul" style="font-size:14.5px;">${esc(ministry)}</div>
                            <div class="moul" style="font-size:14.5px;">${esc(school)}</div>
                        </div>
                        <div style="text-align:center;color:#0f2a5c;margin:4px 0 ${withPhoto ? '8px' : '20px'};line-height:1.7;">
                            <div class="moul" style="font-size:21px;">តារាងកិត្តិយសសម្រាប់ ${esc(periodTitle)}</div>
                            <div class="moul" style="font-size:16px;margin-top:4px;">ថ្នាក់ទី ${esc(HR.className)} ឆ្នាំសិក្សា ${esc(HR.academicYear)}</div>
                        </div>
                    </div>

                    <div class="honor-pyramid ${withPhoto ? 'with-photo' : ''}">
                        ${buildHonorRibbonSlot(top5[0], 1, withPhoto)}
                        <div class="honor-row-2">
                            ${buildHonorRibbonSlot(top5[1], 2, withPhoto)}
                            ${buildHonorRibbonSlot(top5[2], 3, withPhoto)}
                        </div>
                        <div class="honor-row-2">
                            ${buildHonorRibbonSlot(top5[3], 4, withPhoto)}
                            ${buildHonorRibbonSlot(top5[4], 5, withPhoto)}
                        </div>
                    </div>

                    <div style="display:flex;justify-content:space-between;align-items:flex-start;padding:0 28px;margin-top:${withPhoto ? '10px' : '24px'};font-size:15.5px;color:#000;line-height:1.8;">
                        <div style="text-align:center;min-width:220px;padding-top:34px;">
                            <div>បានឃើញ និងឯកភាព</div>
                            <div style="font-weight:700;margin-top:4px;">នាយកសាលា</div>
                            <div class="moul" style="margin-top:56px;font-size:15px;">${esc(principalName)}</div>
                        </div>
                        <div style="text-align:center;min-width:280px;">
                            <div>${esc(lunarDate)}</div>
                            <div>${esc(solarDate)}</div>
                            <div style="font-weight:700;margin-top:4px;">គ្រូបន្ទុកថ្នាក់</div>
                            <div class="moul" style="margin-top:52px;font-size:15.5px;">${esc(teacherName)}</div>
                        </div>
                    </div>
                </div>
            </div>`;
        return;
    }

    const listToPrint = mode === 'CERT_TOP5'
        ? top5
        : results.filter(r => String(r.student.id) === String(stuSel.value));

    if (listToPrint.length === 0) {
        area.innerHTML = '<div style="text-align:center;color:#64748b;padding:30px;">មិនទាន់មានពិន្ទុសម្រាប់សិស្សនេះទេ — សូមបញ្ចូលពិន្ទុជាមុនសិន។</div>';
        return;
    }

    const themeClass = theme === 'red' ? 'theme-red' : theme === 'gold' ? 'theme-gold' : '';
    const accentColor = theme === 'red' ? '#b91c1c' : theme === 'gold' ? '#b45309' : '#1d4ed8';

    area.innerHTML = listToPrint.map((r, idx) => `
        <div class="cert-frame ${themeClass} ${idx < listToPrint.length - 1 ? 'page-break' : ''}">
            ${getOfficialHeader('ប័ណ្ណសរសើរ', '')}
            <div style="text-align:center;line-height:2;font-size:0.98rem;margin:10px 0 18px;">
                <div>សូមសរសើរចំពោះសិស្សឈ្មោះ <strong class="moul" style="font-size:1.12rem;color:${accentColor};">${esc(r.student.name)}</strong> &nbsp; ភេទ៖ <strong>${esc(r.student.gender)}</strong></div>
                <div>កើតនៅថ្ងៃទី៖ <strong>${esc(r.student.dob || '............................')}</strong> &nbsp; ជាសិស្សថ្នាក់ទី៖ <strong>${esc(HR.className)}</strong></div>
                <div>ដែលបានខិតខំប្រឹងប្រែងរៀនសូត្ររហូតទទួលបាន <strong style="color:${accentColor};font-size:1.1rem;">ចំណាត់ថ្នាក់លេខ ${r.rank}</strong> ក្នុងចំណោមសិស្សសរុប <strong>${HR.students.length} នាក់</strong></div>
                <div>មធ្យមភាគ៖ <strong>${fmtNum(r.avg)}</strong> &nbsp; និទ្ទេស៖ <strong style="color:${accentColor};">${r.mention.code} (${r.mention.kh})</strong> &nbsp; សម្រាប់លទ្ធផលសិក្សា <strong>${PERIOD_LABELS[period] || period}</strong> ឆ្នាំសិក្សា <strong>${esc(HR.academicYear)}</strong></div>
                <div style="font-size:0.86rem;color:#475569;margin-top:6px;">ប័ណ្ណសរសើរនេះចេញជូនសាមីខ្លួនយកទៅប្រើប្រាស់តាមការដែលអាចប្រើប្រាស់បាន។</div>
            </div>
            ${getOfficialFooter()}
        </div>
    `).join('');
}

// ==================== TAB 5: REPORT CARDS, TAMDAN & KEARIK ====================
function toKhmerNum(n) {
    const kh = ['០', '១', '២', '៣', '៤', '៥', '៦', '៧', '៨', '៩'];
    return String(n).replace(/\d/g, d => kh[Number(d)]);
}

const STANDARD_KEARIK_SUBJECTS = [
    { label: 'ភាសាខ្មែរ', keys: ['ភាសាខ្មែរ', 'ខ្មែរ', 'khmer'] },
    { label: 'គណិតវិទ្យា', keys: ['គណិតវិទ្យា', 'គណិត', 'math'] },
    { label: 'រូបវិទ្យា', keys: ['រូបវិទ្យា', 'រូប', 'physics'] },
    { label: 'គីមីវិទ្យា', keys: ['គីមីវិទ្យា', 'គីមី', 'chem'] },
    { label: 'ជីវវិទ្យា', keys: ['ជីវវិទ្យា', 'ជីវ', 'bio'] },
    { label: 'ប្រវត្តិវិទ្យា', keys: ['ប្រវត្តិវិទ្យា', 'ប្រវត្តិ', 'history'] },
    { label: 'ព័ត៌មានវិទ្យា', keys: ['ព័ត៌មានវិទ្យា', 'ict', 'computer'] },
    { label: 'សីល-ពលរដ្ឋ', keys: ['សីល', 'ពលរដ្ឋ', 'civic', 'moral'] },
    { label: 'ផែនដីវិទ្យា', keys: ['ផែនដី', 'បរិស្ថាន', 'earth'] },
    { label: 'ភូមិវិទ្យា', keys: ['ភូមិវិទ្យា', 'ភូមិ', 'geo'] },
    { label: 'គេហវិទ្យា', keys: ['គេហវិទ្យា', 'គេហ', 'home'] },
    { label: 'អប់រំកាយ', keys: ['អប់រំកាយ', 'កីឡា', 'sport', 'pe'] },
    { label: 'បំណិនជីវិត', keys: ['បំណិនជីវិត', 'បំណិន', 'life'] },
    { label: 'សេដ្ឋកិច្ច', keys: ['សេដ្ឋកិច្ច', 'econ'] },
    { label: 'សិល្បៈ', keys: ['សិល្បៈ', 'art'] },
    { label: 'កសិកម្ម', keys: ['កសិកម្ម', 'agri'] },
    { label: 'ភាសាបរទេស', keys: ['ភាសាអង់គ្លេស', 'អង់គ្លេស', 'បារាំង', 'ភាសាបរទេស', 'english', 'french'] }
];

function findMatchingSubject(stdDef) {
    for (const sub of HR.subjects) {
        const lower = (sub.subjectName || '').toLowerCase();
        if (stdDef.keys.some(k => lower.includes(k))) return sub;
    }
    return null;
}

function computeSubjectRanksForPeriod(period, subjectId) {
    const arr = HR.students.map(st => ({
        sid: st.id,
        val: HR.scoreMap[`${period}_${st.id}_${subjectId}`]
    })).filter(x => x.val !== undefined).sort((a, b) => b.val - a.val);
    const rankMap = {};
    let lastV = null, lastR = 0;
    arr.forEach((x, idx) => {
        if (lastV !== null && Math.round(x.val * 100) === Math.round(lastV * 100)) {
            rankMap[x.sid] = lastR;
        } else {
            rankMap[x.sid] = idx + 1;
            lastR = idx + 1;
            lastV = x.val;
        }
    });
    return rankMap;
}

function getStudentSemesterAttendance(student) {
    // Combine monthlyAttendanceJson (Nov-Mar = S1, Apr-Oct = S2) or fallback to existing Attendance
    let s1P = 0, s1U = 0, s2P = 0, s2U = 0, hasMonthly = false;
    const mAtt = HR.admin.monthlyAtt || {};
    Object.keys(mAtt).forEach(k => {
        const parts = k.split('-');
        const mNum = parts.length === 2 ? parseInt(parts[1], 10) : 0;
        const marks = (mAtt[k] && mAtt[k].marks) ? mAtt[k].marks : {};
        Object.keys(marks).forEach(mkKey => {
            if (mkKey.startsWith(`${student.id}_`)) {
                hasMonthly = true;
                const val = marks[mkKey];
                const isS1 = (mNum === 11 || mNum === 12 || mNum === 1 || mNum === 2 || mNum === 3);
                if (val === 'ច') { if (isS1) s1P++; else s2P++; }
                else if (val === 'អ') { if (isS1) s1U++; else s2U++; }
            }
        });
    });
    if (!hasMonthly && student.absentByMonth) {
        Object.keys(student.absentByMonth).forEach(mStr => {
            const mNum = parseInt(mStr, 10);
            const arr = student.absentByMonth[mStr] || [0, 0, 0];
            const isS1 = (mNum === 11 || mNum === 12 || mNum === 1 || mNum === 2 || mNum === 3);
            if (isS1) { s1P += arr[0]; s1U += arr[1]; }
            else { s2P += arr[0]; s2U += arr[1]; }
        });
    }
    return { s1P, s1U, s2P, s2U, yrP: s1P + s2P, yrU: s1U + s2U };
}

function getStudentPeriodAttendance(student, period) {
    const periodToMonthNum = {
        M1: 11, M2: 12, M3: 1, M4: 2, M5: 3,
        M6: 4, M7: 5, M8: 6, M9: 7, M10: 8
    };
    if (period === 'S1' || period === 'S2' || period === 'YEAR') {
        const sem = getStudentSemesterAttendance(student);
        if (period === 'S1') return { p: sem.s1P, u: sem.s1U, t: sem.s1P + sem.s1U };
        if (period === 'S2') return { p: sem.s2P, u: sem.s2U, t: sem.s2P + sem.s2U };
        return { p: sem.yrP, u: sem.yrU, t: sem.yrP + sem.yrU };
    }
    const targetM = periodToMonthNum[period];
    if (targetM) {
        let p = 0, u = 0, found = false;
        const mAtt = HR.admin.monthlyAtt || {};
        Object.keys(mAtt).forEach(k => {
            const parts = k.split('-');
            const mNum = parts.length === 2 ? parseInt(parts[1], 10) : 0;
            if (mNum === targetM) {
                const marks = (mAtt[k] && mAtt[k].marks) ? mAtt[k].marks : {};
                Object.keys(marks).forEach(mkKey => {
                    if (mkKey.startsWith(`${student.id}_`)) {
                        found = true;
                        const val = marks[mkKey];
                        if (val === 'ច') p++;
                        else if (val === 'អ') u++;
                    }
                });
            }
        });
        if (!found && student.absentByMonth && student.absentByMonth[String(targetM)]) {
            const arr = student.absentByMonth[String(targetM)] || [0, 0, 0];
            p = arr[0] || 0;
            u = arr[1] || 0;
        }
        return { p, u, t: p + u };
    }
    const p = student.absentPermission || 0;
    const u = student.absentUnexcused || 0;
    return { p, u, t: p + u };
}

function getAutoTeacherComment(mentionCode, customComment) {
    if (customComment && customComment.trim()) return customComment.trim();
    switch (mentionCode) {
        case 'A': return 'សិស្សល្អប្រសើរ សូមបន្តខិតខំប្រឹងប្រែងរក្សាលទ្ធផលដ៏ល្អនេះជារៀងរហូត។';
        case 'B': return 'សិស្សល្អណាស់ សូមបន្តខិតខំប្រឹងប្រែងរៀនសូត្របន្ថែមទៀតដើម្បីទទួលបាននិទ្ទេស A។';
        case 'C': return 'សិស្សល្អ សូមបន្តខិតខំប្រឹងប្រែងរៀនសូត្រ និងយកចិត្តទុកដាក់បន្ថែមទៀត។';
        case 'D': return 'សិស្សល្អបង្គួរ សូមខិតខំប្រឹងប្រែងរៀនសូត្រ និងរំលឹកមេរៀនឱ្យបានទៀងទាត់។';
        case 'E': return 'សិស្សមធ្យម សូមខិតខំប្រឹងប្រែងរៀនសូត្របន្ថែមទៀត និងកាត់បន្ថយអវត្តមាន។';
        case 'F': return 'សិស្សខ្សោយ សូមអាណាព្យាបាលជួយជំរុញ និងតាមដានការសិក្សារបស់ប្អូនបន្ថែមទៀត។';
        default: return 'សូមបន្តខិតខំប្រឹងប្រែងរៀនសូត្រ និងគោរពវិន័យសាលាឱ្យបានល្អ។';
    }
}

function renderReportDocs() {
    const docType = document.getElementById('reportDocType').value;
    const period = document.getElementById('reportPeriodSelect').value;
    const stuId = document.getElementById('reportStudentSelect').value;

    const isSingleStu = (
        docType === 'TAMDAN_ONE' ||
        docType === 'PRITIBAT_MONTH_ONE' ||
        docType === 'PRITIBAT_YEAR_ONE' ||
        docType === 'KEARIK_ONE'
    );
    const needsPeriod = (
        docType === 'TAMDAN_ONE' || docType === 'TAMDAN_ALL' ||
        docType === 'PRITIBAT_MONTH_ONE' || docType === 'PRITIBAT_MONTH_ALL'
    );

    document.getElementById('reportStudentSelect').style.display = isSingleStu ? 'inline-block' : 'none';
    document.getElementById('reportPeriodSelect').style.display = needsPeriod ? 'inline-block' : 'none';

    if (docType === 'TAMDAN_ONE' || docType === 'TAMDAN_ALL') {
        renderTamdanPages(period, docType === 'TAMDAN_ONE' ? stuId : null);
        return;
    }

    if (docType === 'PRITIBAT_MONTH_ONE' || docType === 'PRITIBAT_MONTH_ALL') {
        renderPritibatMonthPages(period, docType === 'PRITIBAT_MONTH_ONE' ? stuId : null);
        return;
    }

    if (docType === 'PRITIBAT_YEAR_ONE' || docType === 'PRITIBAT_YEAR_ALL') {
        renderPritibatYearPages(docType === 'PRITIBAT_YEAR_ONE' ? stuId : null);
        return;
    }

    if (docType === 'KEARIK_ONE' || docType === 'KEARIK_ALL') {
        renderKearikPages(docType === 'KEARIK_ONE' ? stuId : null);
        return;
    }
}

// ==================== OFFICIAL STUDY TRACKING BOOK (សៀវភៅតាមដានការសិក្សា - A4 PORTRAIT PDF) ====================
function getMentionKhFromCode(code) {
    switch (code) {
        case 'A': return 'ល្អប្រសើរ';
        case 'B': return 'ល្អណាស់';
        case 'C': return 'ល្អ';
        case 'D': return 'ល្អបង្គួរ';
        case 'E': return 'មធ្យម';
        case 'F': return 'ខ្សោយ';
        default: return '-';
    }
}

function getReportDisplaySubjects() {
    if (HR.subjects && HR.subjects.length > 0) return HR.subjects;
    const defaultMax = {
        'ភាសាខ្មែរ': 100, 'គណិតវិទ្យា': 100, 'ភាសាបរទេស': 100
    };
    return STANDARD_KEARIK_SUBJECTS.map((s, idx) => ({
        id: `STD_${idx}`,
        subjectName: s.label,
        maxScore: defaultMax[s.label] || 50,
        teacherName: ''
    }));
}

function formatMentionDisplay(mention) {
    if (!mention || !mention.code || mention.code === '-') return '-';
    return (mention.kh && mention.kh !== '-')
        ? `${esc(mention.code)} (${esc(mention.kh)})`
        : esc(mention.code);
}

function renderTamdanPages(period, singleStudentId) {
    const area = document.getElementById('reportDocsArea');
    const results = computeResultsForPeriod(period);
    const list = singleStudentId
        ? results.filter(r => String(r.student.id) === String(singleStudentId))
        : results;

    if (list.length === 0) {
        area.innerHTML = '<div style="text-align:center;color:#64748b;padding:24px;">គ្មានទិន្នន័យសិស្ស</div>';
        return;
    }

    const displaySubjects = getReportDisplaySubjects();
    const cfg = HR.admin.config || {};
    const school = cfg.school || HR.teacher.schoolName || 'វិទ្យាល័យ';
    const principal = cfg.principal || HR.teacher.principalName || '';
    const teacherName = HR.teacher.fullName || '';
    const lunarDate = cfg.lunarDate || 'ថ្ងៃ............ ....កើត/រោច ខែ............ ឆ្នាំ............ ព.ស. ២៥៦៩';
    const solarDate = cfg.solarDate || `${school.replace(/^វិទ្យាល័យ\s*/, '')}, ថ្ងៃទី ...... ខែ ............ ឆ្នាំ ២០២៦`;

    const totalStuKh = toKhmerNum(HR.students.length);
    const femaleStuKh = toKhmerNum(HR.students.filter(s => s.gender === 'ស្រី' || s.gender === 'F').length);
    const rawPeriodLabel = PERIOD_LABELS[period] || period;
    const periodShort = rawPeriodLabel.replace(/^ប្រចាំ/, '');

    // Determine score source & compute per-subject ranks
    const scorePeriodKey = (period === 'S1') ? 'EXAM_S1' : (period === 'S2' ? 'EXAM_S2' : period);
    const subRankMap = {};
    const subValMap = {};
    displaySubjects.forEach(sub => {
        if (String(sub.id).startsWith('STD_')) {
            subRankMap[sub.id] = {};
            subValMap[sub.id] = {};
            return;
        }
        if (period === 'YEAR') {
            const arr = [];
            subValMap[sub.id] = {};
            HR.students.forEach(st => {
                const v1 = HR.scoreMap[`EXAM_S1_${st.id}_${sub.id}`];
                const v2 = HR.scoreMap[`EXAM_S2_${st.id}_${sub.id}`];
                if (v1 !== undefined && v2 !== undefined) {
                    const av = (v1 + v2) / 2;
                    subValMap[sub.id][st.id] = av;
                    arr.push({ sid: st.id, val: av });
                } else if (v1 !== undefined) {
                    subValMap[sub.id][st.id] = v1;
                    arr.push({ sid: st.id, val: v1 });
                } else if (v2 !== undefined) {
                    subValMap[sub.id][st.id] = v2;
                    arr.push({ sid: st.id, val: v2 });
                }
            });
            arr.sort((a, b) => b.val - a.val);
            const rMap = {};
            let lastV = null, lastR = 0;
            arr.forEach((x, idx) => {
                if (lastV !== null && Math.round(x.val * 100) === Math.round(lastV * 100)) rMap[x.sid] = lastR;
                else { rMap[x.sid] = idx + 1; lastR = idx + 1; lastV = x.val; }
            });
            subRankMap[sub.id] = rMap;
        } else {
            subRankMap[sub.id] = computeSubjectRanksForPeriod(scorePeriodKey, sub.id);
        }
    });

    const subCount = displaySubjects.length || 1;
    const rowH = Math.min(46, Math.max(25, Math.floor(485 / subCount)));
    const tableFont = subCount >= 18 ? '14px' : '15px';
    const rowStyle = `height:${rowH}px;padding:2px 5px;`;

    area.innerHTML = list.map((r, idx) => {
        const st = r.student;
        const att = getStudentPeriodAttendance(st, period);

        let totalScoreSum = 0, hasAnyScore = false;
        const subjectRowsHtml = displaySubjects.map((sub, si) => {
            const val = String(sub.id).startsWith('STD_')
                ? undefined
                : ((period === 'YEAR')
                    ? (subValMap[sub.id] ? subValMap[sub.id][st.id] : undefined)
                    : HR.scoreMap[`${scorePeriodKey}_${st.id}_${sub.id}`]);
            if (val !== undefined) { totalScoreSum += val; hasAnyScore = true; }
            const rk = subRankMap[sub.id] ? subRankMap[sub.id][st.id] : null;
            const mCode = (val !== undefined && sub.maxScore > 0) ? getSubjectMention(val, sub.maxScore) : '-';
            let remarkHtml = '';
            if (val === undefined) {
                remarkHtml = '<span class="td-red">មិនបានប្រឡង</span>';
            } else {
                const khM = getMentionKhFromCode(mCode);
                remarkHtml = (mCode === 'F') ? `<span class="td-red">${khM}</span>` : `<span>${khM}</span>`;
            }
            return `
                <tr>
                    <td style="${rowStyle}">${si + 1}</td>
                    <td style="${rowStyle}text-align:center;">${esc(sub.subjectName)}</td>
                    <td style="${rowStyle}">${sub.maxScore}</td>
                    <td style="${rowStyle}font-weight:700;">${val !== undefined ? fmtNum(val) : '0'}</td>
                    <td class="td-red" style="${rowStyle}">${rk || '-'}</td>
                    <td class="td-red" style="${rowStyle}">${mCode}</td>
                    <td style="${rowStyle}">${remarkHtml}</td>
                </tr>
            `;
        }).join('');

        const totalScoreDisplay = (r.total !== undefined && r.total !== null)
            ? fmtNum(r.total)
            : (hasAnyScore ? fmtNum(totalScoreSum) : '-');
        const commentText = getAutoTeacherComment(r.mention?.code, st.teacherComment);

        return `
            <div class="pritibat-a4-page pritibat-year-page ${idx < list.length - 1 ? 'page-break' : ''}">
                <!-- TOP HEADER & MAIN TABLE -->
                <div>
                    <div style="text-align:center;line-height:1.55;">
                        <div class="moul" style="font-size:18px;color:#0f172a;">ព្រះរាជាណាចក្រកម្ពុជា</div>
                        <div class="moul" style="font-size:16px;color:#0f172a;">ជាតិ សាសនា ព្រះមហាក្សត្រ</div>
                        <div style="font-size:12.5px;letter-spacing:3px;color:#0f172a;">~❖~</div>
                    </div>
                    <div style="margin-top:-12px;line-height:1.55;">
                        <div class="moul" style="font-size:15px;color:#0f172a;">${esc(school)}</div>
                    </div>

                    <div style="text-align:center;margin-top:4px;line-height:1.55;">
                        <div class="moul" style="font-size:21px;color:#0f172a;">សៀវភៅតាមដានការសិក្សា</div>
                        <div class="moul" style="font-size:15.5px;color:#0f172a;margin-top:4px;">
                            លទ្ធផលសិក្សាសម្រាប់
                            <span class="moul" style="color:#dc2626;margin:0 14px;">${esc(periodShort)}</span>
                            ឆ្នាំសិក្សា
                            <span class="moul" style="color:#dc2626;margin-left:10px;">${esc(HR.academicYear)}</span>
                        </div>
                        <div style="font-size:15px;color:#000;margin-top:4px;display:flex;justify-content:center;gap:34px;">
                            <span>ថ្នាក់ទី <strong>"${esc(HR.className)}"</strong></span>
                            <span>ចំនួនសិស្សសរុប <strong>${totalStuKh} នាក់</strong></span>
                            <span>ស្រី <strong>${femaleStuKh} នាក់</strong></span>
                        </div>
                    </div>

                    <!-- 7-COLUMN BLACK TABLE MATCHING PDF -->
                    <table class="tamdan-a4-table" style="margin-top:8px;font-size:${tableFont};">
                        <thead>
                            <tr>
                                <th colspan="5" style="padding:7px 8px;border-right:none;text-align:right;padding-right:16px;font-size:15px;" class="moul">លទ្ធផលនៃការសិក្សា របស់សិស្សឈ្មោះ</th>
                                <th colspan="2" style="padding:7px 10px;border-left:none;text-align:left;color:#dc2626;font-size:16px;" class="moul">${esc(st.name)}</th>
                            </tr>
                            <tr style="font-size:14.5px;">
                                <th style="width:6%;padding:6px 4px;">ល.រ</th>
                                <th style="width:22%;padding:6px 5px;">មុខវិជ្ជា</th>
                                <th style="width:13%;padding:6px 4px;">ពិន្ទុអតិបរមា</th>
                                <th style="width:13%;padding:6px 4px;">ពិន្ទុទទួលបាន</th>
                                <th style="width:15%;padding:6px 4px;">ចំណាត់ថ្នាក់ពិន្ទុ</th>
                                <th style="width:11%;padding:6px 4px;">និទ្ទេស</th>
                                <th style="width:20%;padding:6px 4px;">ផ្សេងៗ</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${subjectRowsHtml}
                            <!-- INTEGRATED SUMMARY ROW 1 -->
                            <tr>
                                <td colspan="7" style="padding:7px 12px;border-top:1.5px solid #000;font-size:15px;">
                                    <div style="display:flex;justify-content:space-between;align-items:center;font-weight:700;">
                                        <span>ពិន្ទុសរុប <span class="td-red" style="margin-left:10px;">${totalScoreDisplay}</span></span>
                                        <span>មធ្យមភាគ <span class="td-red" style="margin-left:10px;">${fmtNum(r.avg)}</span></span>
                                        <span>ចំណាត់ថ្នាក់ <span class="td-red" style="margin-left:10px;">${r.rank || '-'}</span></span>
                                        <span>និទ្ទេសរួម <span class="td-red" style="margin-left:10px;">${formatMentionDisplay(r.mention)}</span></span>
                                    </div>
                                </td>
                            </tr>
                            <!-- INTEGRATED SUMMARY ROW 2 -->
                            <tr>
                                <td colspan="7" style="padding:7px 12px;font-size:15px;">
                                    <div style="display:flex;justify-content:space-between;align-items:center;font-weight:700;">
                                        <span>អវត្តមានមានច្បាប់ <span class="td-red" style="margin-left:14px;">${att.p}</span></span>
                                        <span>អវត្តមានឥតច្បាប់ <span class="td-red" style="margin-left:14px;">${att.u}</span></span>
                                        <span>សរុប <span class="td-red" style="margin-left:14px;">${att.t}</span></span>
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>

                    <!-- TEACHER COMMENT -->
                    <div style="margin-top:10px;display:flex;align-items:flex-start;gap:14px;font-size:15px;line-height:1.7;">
                        <div style="white-space:nowrap;font-weight:700;color:#000;">មូលវិចារគ្រូបន្ទុកថ្នាក់៖</div>
                        <div style="color:#0369a1;flex:1;">${esc(commentText)}</div>
                    </div>
                </div>

                <!-- SIGNATURES & PARENT COMMENT -->
                <div>
                    <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-top:8px;padding:0 22px;">
                        <div style="text-align:center;width:40%;padding-top:34px;">
                            <div style="font-size:15px;color:#000;">បានឃើញ និងឯកភាព</div>
                            <div class="moul" style="font-size:15px;color:#000;margin-top:4px;">នាយកសាលា</div>
                            <div style="height:40px;"></div>
                            <div class="moul" style="font-size:15px;color:#000;">${esc(principal)}</div>
                        </div>
                        <div style="text-align:center;width:56%;">
                            <div style="font-size:14.5px;color:#000;line-height:1.55;">${esc(lunarDate)}</div>
                            <div style="font-size:14.5px;color:#000;line-height:1.55;">${esc(solarDate)}</div>
                            <div style="font-size:15px;color:#000;margin-top:4px;font-weight:700;">គ្រូបន្ទុកថ្នាក់</div>
                            <div style="height:40px;"></div>
                            <div class="moul" style="font-size:15.5px;color:#000;">${esc(teacherName)}</div>
                        </div>
                    </div>

                    <div style="margin-top:8px;">
                        <div style="text-align:center;font-size:15.5px;font-weight:700;color:#000;">មតិរបស់អាណាព្យាបាល</div>
                        <div class="tamdan-dotted-line"></div>
                        <div class="tamdan-dotted-line"></div>
                        <div class="tamdan-dotted-line"></div>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

// ==================== OFFICIAL MONTHLY / SEMESTER REPORT CARD (A4 PORTRAIT - BLUE/RED PDF) ====================
function renderPritibatMonthPages(period, singleStudentId) {
    const area = document.getElementById('reportDocsArea');
    const results = computeResultsForPeriod(period);
    const list = singleStudentId
        ? results.filter(r => String(r.student.id) === String(singleStudentId))
        : results;

    if (list.length === 0) {
        area.innerHTML = '<div style="text-align:center;color:#64748b;padding:24px;">គ្មានទិន្នន័យសិស្ស</div>';
        return;
    }

    const cfg = HR.admin.config || {};
    const ministry = cfg.ministry || 'មន្ទីរអប់រំយុវជន និងកីឡា';
    const school = cfg.school || HR.teacher.schoolName || 'វិទ្យាល័យ';
    const principal = cfg.principal || HR.teacher.principalName || '';
    const teacherName = HR.teacher.fullName || '';
    const lunarDate = cfg.lunarDate || 'ថ្ងៃ............ ....កើត/រោច ខែ............ ឆ្នាំ............ ព.ស. ២៥៦៩';
    const solarDate = cfg.solarDate || `${school.replace(/^វិទ្យាល័យ\s*/, '')}, ថ្ងៃទី ...... ខែ ............ ឆ្នាំ ២០២៦`;

    const totalStu = HR.students.length;
    const femaleStu = HR.students.filter(s => s.gender === 'ស្រី' || s.gender === 'F').length;
    const rawPeriodLabel = PERIOD_LABELS[period] || period;
    const periodTitle = rawPeriodLabel.startsWith('ប្រចាំ') ? rawPeriodLabel : `ប្រចាំ${rawPeriodLabel}`;
    const attTitle = (period === 'S1' || period === 'S2') ? 'អវត្តមានប្រចាំឆមាស' : 'អវត្តមានប្រចាំខែ';

    // For S1/S2 display exam scores per subject; for monthly periods display that month's scores
    const scorePeriodKey = (period === 'S1') ? 'EXAM_S1' : (period === 'S2' ? 'EXAM_S2' : period);
    const displaySubjects = getReportDisplaySubjects();

    // Dynamically calculate exact row height so table fills A4 Portrait cleanly with zero empty gap below
    const subCount = displaySubjects.length || 1;
    const rowH = Math.min(44, Math.max(24, Math.floor(470 / subCount)));
    const tableFont = subCount >= 18 ? '14px' : '15px';
    const rowStyle = `height:${rowH}px;padding:2px 5px;`;

    area.innerHTML = list.map((r, idx) => {
        const st = r.student;
        const att = getStudentPeriodAttendance(st, period);
        const totalScoreVal = (r.total !== undefined && r.total !== null)
            ? fmtNum(r.total)
            : (() => {
                let sum = 0, hasAny = false;
                displaySubjects.forEach(sub => {
                    if (String(sub.id).startsWith('STD_')) return;
                    const v = HR.scoreMap[`${scorePeriodKey}_${st.id}_${sub.id}`];
                    if (v !== undefined) { sum += v; hasAny = true; }
                });
                return hasAny ? fmtNum(sum) : '-';
            })();

        const subjectRowsHtml = displaySubjects.map(sub => {
            const val = String(sub.id).startsWith('STD_') ? undefined : HR.scoreMap[`${scorePeriodKey}_${st.id}_${sub.id}`];
            const m = getSubjectMention(val, sub.maxScore);
            const scoreDisplay = val !== undefined ? fmtNum(val) : '';
            return `
                <tr>
                    <td class="pm-sub" style="${rowStyle}">${esc(sub.subjectName)}</td>
                    <td style="${rowStyle}">${sub.maxScore}</td>
                    <td style="${rowStyle}font-weight:700;">${scoreDisplay}</td>
                    <td style="${rowStyle}font-weight:700;">${m === 'A' ? 'A' : ''}</td>
                    <td style="${rowStyle}font-weight:700;">${m === 'B' ? 'B' : ''}</td>
                    <td style="${rowStyle}font-weight:700;">${m === 'C' ? 'C' : ''}</td>
                    <td style="${rowStyle}font-weight:700;">${m === 'D' ? 'D' : ''}</td>
                    <td style="${rowStyle}font-weight:700;">${m === 'E' ? 'E' : ''}</td>
                    <td class="pm-red" style="${rowStyle}">${m === 'F' ? 'F' : ''}</td>
                </tr>
            `;
        }).join('');

        const commentText = getAutoTeacherComment(r.mention?.code, st.teacherComment);

        return `
            <div class="pritibat-a4-page pritibat-month-page ${idx < list.length - 1 ? 'page-break' : ''}">
                <!-- TOP HEADER -->
                <div>
                    <div style="text-align:center;line-height:1.55;">
                        <div class="moul" style="font-size:18px;color:#1d4ed8;">ព្រះរាជាណាចក្រកម្ពុជា</div>
                        <div class="moul" style="font-size:16px;color:#1d4ed8;">ជាតិ សាសនា ព្រះមហាក្សត្រ</div>
                    </div>
                    <div style="margin-top:2px;line-height:1.55;">
                        <div class="moul" style="font-size:15px;color:#1d4ed8;">${esc(ministry)}</div>
                        <div class="moul" style="font-size:15px;color:#1d4ed8;">${esc(school)}</div>
                    </div>
                    <div style="text-align:center;margin-top:4px;line-height:1.45;">
                        <div class="moul" style="font-size:21px;color:#dc2626;">ព្រឹត្តិបត្រពិន្ទុ ${esc(periodTitle)}</div>
                        <div class="moul" style="font-size:15.5px;color:#1d4ed8;margin-top:2px;">ថ្នាក់ទី ${esc(HR.className)} ឆ្នាំសិក្សា ${esc(HR.academicYear)}</div>
                    </div>

                    <!-- STUDENT INFO -->
                    <div style="margin-top:8px;font-size:15px;color:#1d4ed8;line-height:1.75;">
                        <div style="display:flex;justify-content:space-between;align-items:baseline;gap:10px;">
                            <div>ព្រឹត្តិបត្រពិន្ទុសិស្សឈ្មោះ <span class="moul" style="font-size:15.5px;margin-left:8px;">${esc(st.name)}</span></div>
                            <div>ភេទ <strong style="margin-left:6px;">${esc(st.gender || '-')}</strong></div>
                            <div>ថ្ងៃខែឆ្នាំកំណើត៖ <strong style="margin-left:4px;">${esc(st.dob || '-')}</strong></div>
                        </div>
                        <div style="display:flex;justify-content:space-between;align-items:baseline;gap:10px;">
                            <div><strong class="moul" style="font-size:14.5px;">ថ្នាក់ទី "${esc(HR.className)}"</strong> នៃ <span>${esc(school)}</span></div>
                            <div>សិស្សសរុប <strong>${totalStu}</strong> នាក់</div>
                            <div>ស្រី <strong>${femaleStu}</strong> នាក់</div>
                        </div>
                    </div>

                    <!-- SUBJECT SCORE TABLE -->
                    <table class="pritibat-month-table" style="margin-top:8px;font-size:${tableFont};">
                        <thead>
                            <tr style="font-size:14px;">
                                <th style="width:24%;padding:6px 5px;">មុខវិជ្ជា</th>
                                <th style="width:11%;padding:6px 4px;">ពិន្ទុអតិបរមា</th>
                                <th style="width:12%;padding:6px 4px;">ពិន្ទុទទួលបាន</th>
                                <th style="width:8.8%;padding:6px 3px;">និទ្ទេសA</th>
                                <th style="width:8.8%;padding:6px 3px;">និទ្ទេសB</th>
                                <th style="width:8.8%;padding:6px 3px;">និទ្ទេសC</th>
                                <th style="width:8.8%;padding:6px 3px;">និទ្ទេសD</th>
                                <th style="width:8.8%;padding:6px 3px;">និទ្ទេសE</th>
                                <th class="pm-red" style="width:9%;padding:6px 3px;">និទ្ទេសF</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${subjectRowsHtml}
                        </tbody>
                    </table>

                    <!-- SUMMARY ROWS -->
                    <div style="margin-top:10px;padding:0 10px;font-size:15px;color:#1d4ed8;line-height:1.85;">
                        <div style="display:grid;grid-template-columns:1.05fr 1.05fr 1.05fr 1.15fr;gap:8px;">
                            <div>ពិន្ទុសរុប <strong style="margin-left:6px;">${totalScoreVal}</strong></div>
                            <div>មធ្យមភាគ <strong style="margin-left:6px;">${fmtNum(r.avg)}</strong></div>
                            <div>ចំណាត់ថ្នាក់ <strong style="margin-left:6px;color:#dc2626;font-size:16px;">${r.rank || '-'}</strong></div>
                            <div>និទ្ទេស <strong style="margin-left:6px;">${formatMentionDisplay(r.mention)}</strong></div>
                        </div>
                        <div style="display:grid;grid-template-columns:1.05fr 1.05fr 1.05fr 1.15fr;gap:8px;">
                            <div>${attTitle}</div>
                            <div>មានច្បាប់៖ <strong style="margin-left:6px;">${att.p}</strong></div>
                            <div>អត់ច្បាប់៖ <strong style="margin-left:6px;">${att.u}</strong></div>
                            <div>សរុប ៖ <strong style="margin-left:6px;">${att.t}</strong></div>
                        </div>
                        <div style="display:grid;grid-template-columns:1.05fr 3.25fr;gap:8px;">
                            <div>សីលធម៌ រស់នៅ</div>
                            <div><strong>ល្អ</strong></div>
                        </div>
                    </div>
                </div>

                <!-- MIDDLE & BOTTOM: COMMENT, SIGNATURES, PARENT COMMENT -->
                <div>
                    <div style="text-align:center;margin-top:6px;">
                        <div class="moul" style="font-size:15px;color:#1d4ed8;">មូលវិចាររបស់គ្រូទទួលបន្ទុកថ្នាក់</div>
                        <div style="margin-top:4px;font-size:15px;color:#1d4ed8;min-height:22px;">${esc(commentText)}</div>
                    </div>

                    <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-top:8px;padding:0 18px;">
                        <div style="text-align:center;width:42%;padding-top:16px;">
                            <div class="moul" style="font-size:15px;color:#1d4ed8;">បានឃើញ និងឯកភាព</div>
                            <div class="moul" style="font-size:15px;color:#1d4ed8;margin-top:3px;">នាយកសាលា</div>
                            <div style="height:38px;"></div>
                            <div class="moul" style="font-size:15px;color:#1d4ed8;">${esc(principal)}</div>
                        </div>
                        <div style="text-align:center;width:54%;">
                            <div style="font-size:14.5px;color:#1d4ed8;line-height:1.55;">${esc(lunarDate)}</div>
                            <div style="font-size:14.5px;color:#1d4ed8;line-height:1.55;">${esc(solarDate)}</div>
                            <div class="moul" style="font-size:15px;color:#1d4ed8;margin-top:3px;">គ្រូទទួលបន្ទុកថ្នាក់</div>
                            <div style="height:34px;"></div>
                            <div class="moul" style="font-size:15.5px;color:#1d4ed8;">${esc(teacherName)}</div>
                        </div>
                    </div>

                    <div style="margin-top:6px;">
                        <div class="moul" style="text-align:center;font-size:15px;color:#1d4ed8;">មតិរបស់អាណាព្យាបាលសិស្ស</div>
                        <div class="pritibat-dotted-line"></div>
                        <div class="pritibat-dotted-line"></div>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

// ==================== OFFICIAL ANNUAL REPORT CARD (A4 PORTRAIT - CLASSIC BLACK PDF) ====================
function renderPritibatYearPages(singleStudentId) {
    const area = document.getElementById('reportDocsArea');
    const s1List = computeSemester(1), s2List = computeSemester(2), yrList = computeAnnual();
    const ex1List = computeSinglePeriod('EXAM_S1'), ex2List = computeSinglePeriod('EXAM_S2');

    // Monthly average ranks for S1 & S2
    const s1MthRankRows = s1List.map(r => ({ student: r.student, avg: r.avgMonths, rank: null }));
    assignRanks(s1MthRankRows);
    const s2MthRankRows = s2List.map(r => ({ student: r.student, avg: r.avgMonths, rank: null }));
    assignRanks(s2MthRankRows);

    const mapBy = (arr) => { const m = {}; arr.forEach(x => m[x.student.id] = x); return m; };
    const s1M = mapBy(s1List), s2M = mapBy(s2List), yrM = mapBy(yrList);
    const ex1M = mapBy(ex1List), ex2M = mapBy(ex2List);
    const s1MthM = mapBy(s1MthRankRows), s2MthM = mapBy(s2MthRankRows);

    // Precompute per-subject ranks for S1, S2, and Annual
    const displaySubjects = getReportDisplaySubjects();
    const subRankS1 = {}, subRankS2 = {}, subRankYr = {}, subValYr = {};
    displaySubjects.forEach(sub => {
        if (String(sub.id).startsWith('STD_')) {
            subRankS1[sub.id] = {}; subRankS2[sub.id] = {}; subRankYr[sub.id] = {}; subValYr[sub.id] = {};
            return;
        }
        subRankS1[sub.id] = computeSubjectRanksForPeriod('EXAM_S1', sub.id);
        subRankS2[sub.id] = computeSubjectRanksForPeriod('EXAM_S2', sub.id);
        const yrArr = [];
        subValYr[sub.id] = {};
        HR.students.forEach(st => {
            const v1 = HR.scoreMap[`EXAM_S1_${st.id}_${sub.id}`];
            const v2 = HR.scoreMap[`EXAM_S2_${st.id}_${sub.id}`];
            if (v1 !== undefined && v2 !== undefined) {
                const av = (v1 + v2) / 2;
                subValYr[sub.id][st.id] = av;
                yrArr.push({ sid: st.id, val: av });
            } else if (v1 !== undefined) {
                subValYr[sub.id][st.id] = v1;
                yrArr.push({ sid: st.id, val: v1 });
            } else if (v2 !== undefined) {
                subValYr[sub.id][st.id] = v2;
                yrArr.push({ sid: st.id, val: v2 });
            }
        });
        yrArr.sort((a, b) => b.val - a.val);
        const rMap = {};
        let lastV = null, lastR = 0;
        yrArr.forEach((x, idx) => {
            if (lastV !== null && Math.round(x.val * 100) === Math.round(lastV * 100)) {
                rMap[x.sid] = lastR;
            } else {
                rMap[x.sid] = idx + 1;
                lastR = idx + 1;
                lastV = x.val;
            }
        });
        subRankYr[sub.id] = rMap;
    });

    const cfg = HR.admin.config || {};
    const ministry = cfg.ministry || 'មន្ទីរអប់រំយុវជន និងកីឡា';
    const school = cfg.school || HR.teacher.schoolName || 'វិទ្យាល័យ';
    const principal = cfg.principal || HR.teacher.principalName || '';
    const teacherName = HR.teacher.fullName || '';
    const lunarDate = cfg.lunarDate || 'ថ្ងៃ............ ....កើត/រោច ខែ............ ឆ្នាំ............ ព.ស. ២៥៦៩';
    const solarDate = cfg.solarDate || `${school.replace(/^វិទ្យាល័យ\s*/, '')}, ថ្ងៃទី ...... ខែ ............ ឆ្នាំ ២០២៦`;
    const totalStu = HR.students.length;

    const targetStudents = singleStudentId
        ? HR.students.filter(s => String(s.id) === String(singleStudentId))
        : HR.students;

    if (targetStudents.length === 0) {
        area.innerHTML = '<div style="text-align:center;color:#64748b;padding:24px;">គ្មានទិន្នន័យសិស្ស</div>';
        return;
    }

    const subCount = displaySubjects.length || 1;
    const rowH = Math.min(40, Math.max(22, Math.floor(435 / subCount)));
    const cellPad = subCount >= 18 ? '2.5px 4px' : (subCount >= 14 ? '3.5px 4px' : '4.5px 4px');
    const rowStyle = `height:${rowH}px;padding:2px 4px;`;
    const tableFont = subCount >= 18 ? '13.5px' : '14.8px';

    area.innerHTML = targetStudents.map((st, idx) => {
        const r1 = s1M[st.id] || {}, r2 = s2M[st.id] || {}, ry = yrM[st.id] || {};
        const e1 = ex1M[st.id] || {}, e2 = ex2M[st.id] || {};
        const m1 = s1MthM[st.id] || {}, m2 = s2MthM[st.id] || {};
        const att = getStudentSemesterAttendance(st);

        const yrTotalExam = (e1.total !== undefined && e2.total !== undefined)
            ? fmtNum((e1.total + e2.total) / 2)
            : (e1.total !== undefined ? fmtNum(e1.total) : (e2.total !== undefined ? fmtNum(e2.total) : '-'));

        const subjectRowsHtml = displaySubjects.map((sub, si) => {
            const v1 = String(sub.id).startsWith('STD_') ? undefined : HR.scoreMap[`EXAM_S1_${st.id}_${sub.id}`];
            const v2 = String(sub.id).startsWith('STD_') ? undefined : HR.scoreMap[`EXAM_S2_${st.id}_${sub.id}`];
            const vy = subValYr[sub.id] ? subValYr[sub.id][st.id] : undefined;
            const rk1 = subRankS1[sub.id] ? subRankS1[sub.id][st.id] : null;
            const rk2 = subRankS2[sub.id] ? subRankS2[sub.id][st.id] : null;
            const rky = subRankYr[sub.id] ? subRankYr[sub.id][st.id] : null;
            return `
                <tr>
                    <td style="${rowStyle}">${si + 1}</td>
                    <td class="py-left" style="${rowStyle}">${esc(sub.subjectName)}</td>
                    <td style="${rowStyle}">${v1 !== undefined ? fmtNum(v1) : '-'}</td>
                    <td class="py-rank" style="${rowStyle}">${rk1 || '-'}</td>
                    <td style="${rowStyle}">${v2 !== undefined ? fmtNum(v2) : '-'}</td>
                    <td class="py-rank" style="${rowStyle}">${rk2 || '-'}</td>
                    <td style="${rowStyle}">${vy !== undefined ? fmtNum(vy) : '-'}</td>
                    <td class="py-rank" style="${rowStyle}">${rky || '-'}</td>
                    <td class="py-left" style="${rowStyle}font-size:12px;color:#334155;">${esc(sub.teacherName || '')}</td>
                </tr>
            `;
        }).join('');

        return `
            <div class="pritibat-a4-page pritibat-year-page ${idx < targetStudents.length - 1 ? 'page-break' : ''}">
                <!-- TOP HEADER & TABLE -->
                <div>
                    <div style="text-align:center;line-height:1.5;">
                        <div class="moul" style="font-size:18px;color:#000;">ព្រះរាជាណាចក្រកម្ពុជា</div>
                        <div class="moul" style="font-size:16px;color:#000;">ជាតិ សាសនា ព្រះមហាក្សត្រ</div>
                        <div style="font-size:12.5px;letter-spacing:3px;color:#000;">~❖~</div>
                    </div>
                    <div style="margin-top:-14px;line-height:1.5;">
                        <div class="moul" style="font-size:15px;color:#000;">${esc(ministry)}</div>
                        <div class="moul" style="font-size:15px;color:#000;">${esc(school)}</div>
                    </div>
                    <div style="text-align:center;margin-top:4px;">
                        <div class="moul" style="font-size:18.5px;color:#000;">ព្រឹត្តិបត្រពិន្ទុ ប្រចាំឆ្នាំសិក្សា</div>
                    </div>

                    <!-- STUDENT INFO -->
                    <div style="margin-top:6px;font-size:15px;color:#000;line-height:1.7;">
                        <div style="display:flex;justify-content:space-between;align-items:baseline;flex-wrap:wrap;gap:6px;">
                            <div>ឈ្មោះសិស្ស <span class="moul" style="font-size:15.5px;margin-left:8px;">${esc(st.name)}</span></div>
                            <div>ភេទ <strong>${esc(st.gender || '-')}</strong></div>
                            <div>ថ្នាក់ទី <strong>${esc(HR.className)}</strong></div>
                            <div>ឆ្នាំសិក្សា <strong>${esc(HR.academicYear)}</strong></div>
                            <div>ចំនួនសិស្សសរុប <strong>${totalStu}</strong>នាក់</div>
                        </div>
                        <div style="text-align:center;">ជាសិស្សនៃ${esc(school)}</div>
                    </div>

                    <!-- ANNUAL 9-COLUMN BLACK TABLE -->
                    <table class="pritibat-year-table" style="margin-top:6px;font-size:${tableFont};">
                        <thead>
                            <tr>
                                <th rowspan="2" style="width:5%;">ល.រ</th>
                                <th rowspan="2" style="width:21%;">មុខវិជ្ជា</th>
                                <th colspan="2" style="width:15%;">ឆមាសទី១</th>
                                <th colspan="2" style="width:15%;">ឆមាសទី២</th>
                                <th colspan="2" style="width:15%;">ប្រចាំឆ្នាំ</th>
                                <th rowspan="2" style="width:29%;">មូលវិចារ ហត្ថលេខា និងឈ្មោះគ្រូ</th>
                            </tr>
                            <tr>
                                <th style="width:7.5%;">ពិន្ទុ</th>
                                <th style="width:7.5%;">ចំ.ថ្នាក់</th>
                                <th style="width:7.5%;">ពិន្ទុ</th>
                                <th style="width:7.5%;">ចំ.ថ្នាក់</th>
                                <th style="width:7.5%;">ពិន្ទុ</th>
                                <th style="width:7.5%;">ចំ.ថ្នាក់</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${subjectRowsHtml}
                            <!-- SUMMARY ROWS -->
                            <tr>
                                <td style="padding:${cellPad};"></td>
                                <td class="py-left" style="padding:${cellPad};font-weight:700;">ពិន្ទុប្រឡងឆមាស</td>
                                <td style="padding:${cellPad};font-weight:700;">${fmtNum(e1.total)}</td>
                                <td style="padding:${cellPad};"></td>
                                <td style="padding:${cellPad};font-weight:700;">${fmtNum(e2.total)}</td>
                                <td style="padding:${cellPad};"></td>
                                <td style="padding:${cellPad};font-weight:700;">${yrTotalExam}</td>
                                <td style="padding:${cellPad};"></td>
                                <td style="padding:${cellPad};"></td>
                            </tr>
                            <tr>
                                <td style="padding:${cellPad};"></td>
                                <td class="py-left" style="padding:${cellPad};font-weight:700;">ម.ពិន្ទុប្រឡងឆមាស</td>
                                <td style="padding:${cellPad};font-weight:700;">${fmtNum(e1.avg)}</td>
                                <td class="py-rank" style="padding:${cellPad};">${e1.rank || '-'}</td>
                                <td style="padding:${cellPad};font-weight:700;">${fmtNum(e2.avg)}</td>
                                <td class="py-rank" style="padding:${cellPad};">${e2.rank || '-'}</td>
                                <td style="padding:${cellPad};"></td>
                                <td style="padding:${cellPad};"></td>
                                <td style="padding:${cellPad};"></td>
                            </tr>
                            <tr>
                                <td style="padding:${cellPad};"></td>
                                <td class="py-left" style="padding:${cellPad};font-weight:700;">ម.ពិន្ទុខែប្រចាំឆមាស</td>
                                <td style="padding:${cellPad};font-weight:700;">${fmtNum(r1.avgMonths)}</td>
                                <td class="py-rank" style="padding:${cellPad};">${m1.rank || '-'}</td>
                                <td style="padding:${cellPad};font-weight:700;">${fmtNum(r2.avgMonths)}</td>
                                <td class="py-rank" style="padding:${cellPad};">${m2.rank || '-'}</td>
                                <td style="padding:${cellPad};"></td>
                                <td style="padding:${cellPad};"></td>
                                <td style="padding:${cellPad};"></td>
                            </tr>
                            <tr>
                                <td style="padding:${cellPad};"></td>
                                <td class="py-left" style="padding:${cellPad};font-weight:700;">ម.ពិន្ទុប្រចាំឆមាស</td>
                                <td style="padding:${cellPad};font-weight:700;">${fmtNum(r1.avg)}</td>
                                <td class="py-rank" style="padding:${cellPad};">${r1.rank || '-'}</td>
                                <td style="padding:${cellPad};font-weight:700;">${fmtNum(r2.avg)}</td>
                                <td class="py-rank" style="padding:${cellPad};">${r2.rank || '-'}</td>
                                <td style="padding:${cellPad};font-weight:700;">${fmtNum(ry.avg)}</td>
                                <td class="py-rank" style="padding:${cellPad};">${ry.rank || '-'}</td>
                                <td style="padding:${cellPad};font-weight:700;">${(ry.mention?.code && ry.mention.code !== '-') ? `និទ្ទេស ${formatMentionDisplay(ry.mention)}` : ''}</td>
                            </tr>
                            <tr>
                                <td style="padding:${cellPad};"></td>
                                <td class="py-left" style="padding:${cellPad};">សីលធម៌រស់នៅ</td>
                                <td colspan="7" class="py-left" style="padding:${cellPad};padding-left:14px;">ល្អ</td>
                            </tr>
                            <tr>
                                <td style="padding:${cellPad};"></td>
                                <td class="py-left" style="padding:${cellPad};">ពលកម្ម-បង្កបង្កើនផល</td>
                                <td colspan="7" class="py-left" style="padding:${cellPad};padding-left:14px;">ល្អ</td>
                            </tr>
                            <tr>
                                <td style="padding:${cellPad};"></td>
                                <td class="py-left" style="padding:${cellPad};">សុខភាព-អនាម័យ</td>
                                <td colspan="7" class="py-left" style="padding:${cellPad};padding-left:14px;">ល្អ</td>
                            </tr>
                            <!-- ABSENCE SUB-TABLE -->
                            <tr>
                                <td colspan="2" style="padding:${cellPad};font-weight:700;">ចំនួនអវត្តមាន</td>
                                <td colspan="2" style="padding:${cellPad};font-weight:700;">ឆមាសទី១</td>
                                <td colspan="2" style="padding:${cellPad};font-weight:700;">ឆមាសទី២</td>
                                <td colspan="2" style="padding:${cellPad};font-weight:700;">ប្រចាំឆ្នាំ</td>
                                <td style="padding:${cellPad};"></td>
                            </tr>
                            <tr>
                                <td colspan="2" style="padding:${cellPad};">មានច្បាប់</td>
                                <td colspan="2" style="padding:${cellPad};">${att.s1P}</td>
                                <td colspan="2" style="padding:${cellPad};">${att.s2P}</td>
                                <td colspan="2" style="padding:${cellPad};font-weight:700;">${att.yrP}</td>
                                <td style="padding:${cellPad};"></td>
                            </tr>
                            <tr>
                                <td colspan="2" style="padding:${cellPad};">ឥតច្បាប់</td>
                                <td colspan="2" style="padding:${cellPad};">${att.s1U}</td>
                                <td colspan="2" style="padding:${cellPad};">${att.s2U}</td>
                                <td colspan="2" style="padding:${cellPad};font-weight:700;">${att.yrU}</td>
                                <td style="padding:${cellPad};"></td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <!-- SIGNATURES (KEPT CLEANLY ON PAGE 1) -->
                <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-top:10px;padding:0 18px;">
                    <div style="text-align:center;width:40%;padding-top:24px;">
                        <div style="font-size:15px;color:#000;">បានឃើញ និងឯកភាព</div>
                        <div class="moul" style="font-size:15px;color:#000;margin-top:3px;">នាយកសាលា</div>
                        <div style="height:40px;"></div>
                        <div class="moul" style="font-size:15px;color:#000;">${esc(principal)}</div>
                    </div>
                    <div style="text-align:center;width:56%;">
                        <div style="font-size:14.5px;color:#000;line-height:1.55;">${esc(lunarDate)}</div>
                        <div style="font-size:14.5px;color:#000;line-height:1.55;">${esc(solarDate)}</div>
                        <div style="font-size:15px;color:#000;margin-top:3px;font-weight:700;">គ្រូបន្ទុកថ្នាក់</div>
                        <div style="height:36px;"></div>
                        <div class="moul" style="font-size:15.5px;color:#000;">${esc(teacherName)}</div>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

function renderKearikPages(singleStudentId) {
    const area = document.getElementById('reportDocsArea');
    const s1List = computeSemester(1), s2List = computeSemester(2), yrList = computeAnnual();
    const ex1List = computeSinglePeriod('EXAM_S1'), ex2List = computeSinglePeriod('EXAM_S2');

    // Compute ranks for monthly averages in S1 & S2
    const s1MthRankRows = s1List.map(r => ({ student: r.student, avg: r.avgMonths, rank: null }));
    assignRanks(s1MthRankRows);
    const s2MthRankRows = s2List.map(r => ({ student: r.student, avg: r.avgMonths, rank: null }));
    assignRanks(s2MthRankRows);

    const mapBy = (arr) => { const m = {}; arr.forEach(x => m[x.student.id] = x); return m; };
    const s1M = mapBy(s1List), s2M = mapBy(s2List), yrM = mapBy(yrList);
    const ex1M = mapBy(ex1List), ex2M = mapBy(ex2List);
    const s1MthM = mapBy(s1MthRankRows), s2MthM = mapBy(s2MthRankRows);

    // Precompute subject rank maps for EXAM_S1 & EXAM_S2
    const subRankS1 = {}, subRankS2 = {};
    HR.subjects.forEach(sub => {
        subRankS1[sub.id] = computeSubjectRanksForPeriod('EXAM_S1', sub.id);
        subRankS2[sub.id] = computeSubjectRanksForPeriod('EXAM_S2', sub.id);
    });

    const cfg = HR.admin.config || {};
    const school = cfg.school || HR.teacher.schoolName || 'វិទ្យាល័យ';
    const principal = cfg.principal || HR.teacher.principalName || '';
    const teacherName = HR.teacher.fullName || '';
    const totalStuKh = toKhmerNum(HR.students.length);

    const targetStudents = singleStudentId
        ? HR.students.map((s, idx) => ({ st: s, num: idx + 1 })).filter(x => String(x.st.id) === String(singleStudentId))
        : HR.students.map((s, idx) => ({ st: s, num: idx + 1 }));

    if (targetStudents.length === 0) {
        area.innerHTML = '<div style="text-align:center;color:#64748b;padding:24px;">គ្មានទិន្នន័យសិស្ស</div>';
        return;
    }

    area.innerHTML = targetStudents.map((item, pageIdx) => {
        const st = item.st;
        const r1 = s1M[st.id] || {}, r2 = s2M[st.id] || {}, ry = yrM[st.id] || {};
        const e1 = ex1M[st.id] || {}, e2 = ex2M[st.id] || {};
        const m1 = s1MthM[st.id] || {}, m2 = s2MthM[st.id] || {};
        const att = getStudentSemesterAttendance(st);

        const m1Mention = getMentionByAvg50(r1.avgMonths);
        const m2Mention = getMentionByAvg50(r2.avgMonths);
        const e1Mention = getMentionByAvg50(e1.avg);
        const e2Mention = getMentionByAvg50(e2.avg);

        const subjectRowsHtml = STANDARD_KEARIK_SUBJECTS.map((std, i) => {
            const matched = findMatchingSubject(std);
            let sc1 = '-', rk1 = '-', mn1 = '-';
            let sc2 = '-', rk2 = '-', mn2 = '-';
            let scY = '-', rkY = '-', mnY = '-';
            let tName = '';
            if (matched) {
                tName = matched.teacherName || '';
                const v1 = HR.scoreMap[`EXAM_S1_${st.id}_${matched.id}`];
                const v2 = HR.scoreMap[`EXAM_S2_${st.id}_${matched.id}`];
                if (v1 !== undefined) {
                    sc1 = fmtNum(v1); rk1 = subRankS1[matched.id][st.id] || '-'; mn1 = getSubjectMention(v1, matched.maxScore);
                }
                if (v2 !== undefined) {
                    sc2 = fmtNum(v2); rk2 = subRankS2[matched.id][st.id] || '-'; mn2 = getSubjectMention(v2, matched.maxScore);
                }
                if (v1 !== undefined && v2 !== undefined) {
                    const vy = (v1 + v2) / 2;
                    scY = fmtNum(vy); mnY = getSubjectMention(vy, matched.maxScore);
                } else if (v1 !== undefined) {
                    scY = fmtNum(v1); mnY = getSubjectMention(v1, matched.maxScore);
                } else if (v2 !== undefined) {
                    scY = fmtNum(v2); mnY = getSubjectMention(v2, matched.maxScore);
                }
            }
            return `
                <tr>
                    <td>${toKhmerNum(i + 1)}</td>
                    <td class="k-left">${esc(std.label)}</td>
                    <td>${sc1}</td><td>${rk1}</td><td class="k-blue">${mn1}</td>
                    <td>${sc2}</td><td>${rk2}</td><td class="k-blue">${mn2}</td>
                    <td>${scY}</td><td>${rkY}</td><td class="k-blue">${mnY}</td>
                    <td class="k-left" style="font-size:0.66rem;">${esc(tName)}</td>
                </tr>`;
        }).join('');

        const certS1 = (r1.rank && r1.rank <= 5) ? '១' : '';
        const certS2 = (r2.rank && r2.rank <= 5) ? '១' : '';
        const certYr = (ry.rank && ry.rank <= 5) ? '១' : '';

        return `
            <div class="kearik-page ${pageIdx < targetStudents.length - 1 ? 'page-break' : ''}">
                <div class="kearik-grid">
                    <!-- LEFT COLUMN -->
                    <div class="kearik-left-box">
                        <table class="kearik-table">
                            <thead>
                                <tr>
                                    <th style="width:28px;">ល.រ</th>
                                    <th style="width:74px;">អត្តលេខ</th>
                                    <th>គោត្តនាម និងនាម</th>
                                    <th style="width:38px;">ភេទ</th>
                                    <th style="width:95px;">ថ្ងៃខែឆ្នាំកំណើត</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td style="font-weight:800;">${toKhmerNum(item.num)}</td>
                                    <td>${esc(st.studentCode || '-')}</td>
                                    <td class="k-teal moul">${esc(st.name)}</td>
                                    <td>${esc(st.gender)}</td>
                                    <td>${esc(st.dob || '-')}</td>
                                </tr>
                            </tbody>
                        </table>

                        <div class="kearik-title-bar moul">លទ្ធផលនៃការសិក្សា</div>

                        <table class="kearik-table kearik-study-table" style="border-top:none;">
                            <thead>
                                <tr>
                                    <th rowspan="2" style="width:24px;">ល.រ</th>
                                    <th rowspan="2" style="min-width:86px;">មុខវិជ្ជា</th>
                                    <th colspan="3">ឆមាសទី ១</th>
                                    <th colspan="3">ឆមាសទី ២</th>
                                    <th colspan="3">ប្រចាំឆ្នាំ</th>
                                    <th rowspan="2" style="min-width:74px;font-size:0.65rem;">មូលវិចារ ហត្ថលេខា<br>និងឈ្មោះគ្រូ</th>
                                </tr>
                                <tr>
                                    <th>ពិន្ទុ</th><th>ចំ.</th><th>និ.</th>
                                    <th>ពិន្ទុ</th><th>ចំ.</th><th>និ.</th>
                                    <th>ពិន្ទុ</th><th>ចំ.</th><th>និ.</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${subjectRowsHtml}
                                <tr>
                                    <td>១៨</td><td class="k-left" style="font-weight:700;">ពិន្ទុប្រឡងឆមាស</td>
                                    <td style="font-weight:700;">${fmtNum(e1.total)}</td><td></td><td></td>
                                    <td style="font-weight:700;">${fmtNum(e2.total)}</td><td></td><td></td>
                                    <td></td><td></td><td></td><td></td>
                                </tr>
                                <tr>
                                    <td>១៩</td><td class="k-left" style="font-weight:700;">ម.ពិន្ទុប្រឡងឆមាស</td>
                                    <td style="font-weight:700;">${fmtNum(e1.avg)}</td><td class="k-red">${e1.rank || '-'}</td><td class="k-blue">${e1Mention.code}</td>
                                    <td style="font-weight:700;">${fmtNum(e2.avg)}</td><td class="k-red">${e2.rank || '-'}</td><td class="k-blue">${e2Mention.code}</td>
                                    <td></td><td></td><td></td><td></td>
                                </tr>
                                <tr>
                                    <td>២០</td><td class="k-left" style="font-weight:700;">ម.ពិន្ទុខែប្រចាំឆមាស</td>
                                    <td style="font-weight:700;">${fmtNum(r1.avgMonths)}</td><td class="k-red">${m1.rank || '-'}</td><td class="k-blue">${m1Mention.code}</td>
                                    <td style="font-weight:700;">${fmtNum(r2.avgMonths)}</td><td class="k-red">${m2.rank || '-'}</td><td class="k-blue">${m2Mention.code}</td>
                                    <td></td><td></td><td></td><td></td>
                                </tr>
                                <tr>
                                    <td>២១</td><td class="k-left" style="font-weight:800;">ម.ពិន្ទុប្រចាំឆមាស</td>
                                    <td style="font-weight:800;">${fmtNum(r1.avg)}</td><td class="k-red">${r1.rank || '-'}</td><td class="k-blue">${r1.mention?.code || '-'}</td>
                                    <td style="font-weight:800;">${fmtNum(r2.avg)}</td><td class="k-red">${r2.rank || '-'}</td><td class="k-blue">${r2.mention?.code || '-'}</td>
                                    <td style="font-weight:800;">${fmtNum(ry.avg)}</td><td class="k-red">${ry.rank || '-'}</td><td class="k-blue">${ry.mention?.code || '-'}</td>
                                    <td></td>
                                </tr>
                            </tbody>
                        </table>

                        <div class="kearik-title-bar moul" style="font-size:0.78rem;">ចំនួនអវត្តមានក្នុងឆ្នាំសិក្សា</div>
                        <table class="kearik-table" style="border-top:none;">
                            <thead>
                                <tr>
                                    <th style="width:28%;">អវត្តមាន</th>
                                    <th>ឆមាសទី ១</th>
                                    <th>ឆមាសទី ២</th>
                                    <th>ប្រចាំឆ្នាំ</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr><td class="k-left">- មានច្បាប់</td><td>${att.s1P}</td><td>${att.s2P}</td><td style="font-weight:700;">${att.yrP}</td></tr>
                                <tr><td class="k-left">- អត់ច្បាប់</td><td>${att.s1U}</td><td>${att.s2U}</td><td style="font-weight:700;">${att.yrU}</td></tr>
                            </tbody>
                        </table>
                    </div>

                    <!-- RIGHT COLUMN -->
                    <div class="kearik-right-box">
                        <div>
                            <table class="kearik-table" style="border:none;">
                                <tbody>
                                    <tr>
                                        <td class="moul" style="border-top:none;border-left:none;font-size:0.74rem;padding:4px 5px;white-space:nowrap;">${esc(school)}</td>
                                        <td style="border-top:none;border-right:none;font-weight:800;width:30%;font-size:0.78rem;white-space:nowrap;">សិស្សសរុប ${totalStuKh} នាក់</td>
                                    </tr>
                                    <tr>
                                        <td colspan="2" class="moul" style="border-left:none;border-right:none;font-size:0.8rem;padding:4px;">
                                            ថ្នាក់ទី ${esc(HR.className)} ឆ្នាំសិក្សា ${esc(HR.academicYear)}
                                        </td>
                                    </tr>
                                    <tr>
                                        <td colspan="2" class="moul" style="border-left:none;border-right:none;font-size:0.78rem;padding:3px;">ការវាយតម្លៃ</td>
                                    </tr>
                                </tbody>
                            </table>

                            <table class="kearik-table" style="border-left:none;border-right:none;border-top:none;">
                                <thead>
                                    <tr>
                                        <th style="border-left:none;width:44%;">ផ្នែកទាំង ៤</th>
                                        <th>ឆមាសទី១</th>
                                        <th>ឆមាសទី២</th>
                                        <th style="border-right:none;">ប្រចាំឆ្នាំ</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr><td class="k-left" style="border-left:none;">១- ការសិក្សា</td><td>${esc(r1.mention?.kh || '-')}</td><td>${esc(r2.mention?.kh || '-')}</td><td style="border-right:none;">${esc(ry.mention?.kh || '-')}</td></tr>
                                    <tr><td class="k-left" style="border-left:none;">២- សីលធម៌រស់នៅ</td><td>ល្អ</td><td>ល្អ</td><td style="border-right:none;">ល្អ</td></tr>
                                    <tr><td class="k-left" style="border-left:none;">៣- ពលកម្ម-បង្កបង្កើនផល</td><td>ល្អបង្គួរ</td><td>ល្អបង្គួរ</td><td style="border-right:none;">ល្អបង្គួរ</td></tr>
                                    <tr><td class="k-left" style="border-left:none;">៤- សុខភាព-អនាម័យ</td><td>ល្អ</td><td>ល្អ</td><td style="border-right:none;">ល្អ</td></tr>
                                </tbody>
                            </table>

                            <div style="text-align:center;font-weight:800;padding:4px;font-size:0.8rem;border-bottom:1px solid #000;" class="moul">ការសរសើរ</div>
                            <table class="kearik-table" style="border-left:none;border-right:none;border-top:none;">
                                <tbody>
                                    <tr><td class="k-left" style="border-left:none;width:44%;font-weight:700;">បានទទួលការសរសើរ</td><td></td><td></td><td style="border-right:none;"></td></tr>
                                    <tr><td class="k-left" style="border-left:none;">- ប័ណ្ណសរសើរចំនួន៖.........</td><td>${certS1}</td><td>${certS2}</td><td style="border-right:none;">${certYr}</td></tr>
                                    <tr><td class="k-left" style="border-left:none;">- លិខិតសរសើរចំនួន៖.......</td><td></td><td></td><td style="border-right:none;"></td></tr>
                                    <tr><td class="k-left" style="border-left:none;">- សក្ខីប័ណ្ណលើកទឹកចិត្ត៖......</td><td></td><td></td><td style="border-right:none;"></td></tr>
                                </tbody>
                            </table>

                            <div style="padding:6px 8px;border-bottom:1px solid #000;font-size:13.5px;color:#000;font-weight:400;">
                                <div class="moul" style="text-align:center;margin-bottom:6px;font-size:13.5px;color:#000;">លទ្ធផលនៃការប្រឡងបញ្ចប់ភូមិសិក្សា ( ជាប់ ឬ ធ្លាក់ )</div>
                                <div style="white-space:nowrap;">មណ្ឌលប្រឡង..........................................ខេត្ត.............បន្ទប់.......លេខតុ.......</div>
                            </div>
                        </div>

                        <div style="padding:8px 10px 10px;display:flex;flex-direction:column;justify-content:space-between;flex:1;font-size:14px;color:#000;font-weight:400;">
                            <div style="text-align:center;">
                                <div class="moul" style="font-size:13.5px;color:#000;">មូលវិចារ គ្រូទទួលបន្ទុកថ្នាក់</div>
                                <div style="min-height:82px;padding:6px 4px;font-style:italic;color:#000;">${esc(st.teacherComment || '')}</div>
                                <div>ថ្ងៃទី........ខែ.............ឆ្នាំ........</div>
                                <div class="moul" style="margin-top:2px;font-size:13.5px;color:#000;">គ្រូបន្ទុកថ្នាក់</div>
                                <div style="min-height:36px;"></div>
                                <div class="moul" style="padding-left:56px;font-size:13.5px;color:#000;">${esc(teacherName)}</div>
                            </div>

                            <div style="text-align:center;margin-top:12px;">
                                <div class="moul" style="font-size:13.5px;color:#000;">មូលវិចារ នាយកសាលា</div>
                                <div style="min-height:46px;"></div>
                                <div>ថ្ងៃទី........ខែ.............ឆ្នាំ........</div>
                                <div class="moul" style="margin-top:2px;font-size:13.5px;color:#000;">នាយកសាលា</div>
                                <div style="min-height:40px;"></div>
                                <div class="moul" style="padding-left:56px;font-size:13.5px;color:#000;">${esc(principal)}</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

async function downloadAllStudentsReportPdf() {
    const docSel = document.getElementById('reportDocType');
    if (!docSel) return;
    const cur = docSel.value;
    if (cur === 'TAMDAN_ONE') {
        docSel.value = 'TAMDAN_ALL';
        renderReportDocs();
        await new Promise(r => setTimeout(r, 120));
    } else if (cur === 'PRITIBAT_MONTH_ONE') {
        docSel.value = 'PRITIBAT_MONTH_ALL';
        renderReportDocs();
        await new Promise(r => setTimeout(r, 120));
    } else if (cur === 'PRITIBAT_YEAR_ONE') {
        docSel.value = 'PRITIBAT_YEAR_ALL';
        renderReportDocs();
        await new Promise(r => setTimeout(r, 120));
    } else if (cur === 'KEARIK_ONE') {
        docSel.value = 'KEARIK_ALL';
        renderReportDocs();
        await new Promise(r => setTimeout(r, 120));
    }
    await downloadReportDocsPdf();
}

async function downloadReportDocsPdf() {
    const area = document.getElementById('reportDocsArea');
    if (!area || !area.innerHTML.trim()) return;
    const docType = document.getElementById('reportDocType').value;

    const JsPdfCtor = (window.jspdf && window.jspdf.jsPDF) ? window.jspdf.jsPDF : window.jsPDF;
    if (typeof html2canvas === 'undefined' || !JsPdfCtor) {
        window.print();
        return;
    }

    const isPortraitReport = (
        docType === 'TAMDAN_ONE' || docType === 'TAMDAN_ALL' ||
        docType === 'PRITIBAT_MONTH_ONE' || docType === 'PRITIBAT_MONTH_ALL' ||
        docType === 'PRITIBAT_YEAR_ONE' || docType === 'PRITIBAT_YEAR_ALL'
    );

    // Select individual pages to render ONE-BY-ONE
    // Avoids browser max-canvas-height blank page bug AND guarantees 0 trailing blank pages!
    let pages = Array.from(area.querySelectorAll('.pritibat-a4-page, .kearik-page, .print-sheet'));
    if (pages.length === 0) pages = [area];

    Swal.fire({
        title: 'កំពុងបង្កើតឯកសារ PDF...',
        html: `<div id="pdfProgText">កំពុងដំណើរការទំព័រទី <b>1</b> / <b>${pages.length}</b> ...</div>`,
        allowOutsideClick: false,
        didOpen: () => Swal.showLoading()
    });

    try {
        const orientation = isPortraitReport ? 'portrait' : 'landscape';
        const pdf = new JsPdfCtor({ orientation, unit: 'mm', format: 'a4' });
        const pageW = pdf.internal.pageSize.getWidth();   // 210mm (portrait) or 297mm (landscape)
        const pageH = pdf.internal.pageSize.getHeight();  // 297mm (portrait) or 210mm (landscape)
        const margin = isPortraitReport ? 4 : 3;
        const maxW = pageW - margin * 2;
        const maxH = pageH - margin * 2;

        for (let i = 0; i < pages.length; i++) {
            const progEl = document.getElementById('pdfProgText');
            if (progEl) {
                progEl.innerHTML = `កំពុងដំណើរការទំព័រទី <b>${i + 1}</b> / <b>${pages.length}</b> ...`;
            }

            const el = pages[i];
            const prevBorder = el.style.border;
            const prevRadius = el.style.borderRadius;
            const prevShadow = el.style.boxShadow;
            const prevMargin = el.style.marginBottom;
            const prevPadding = el.style.padding;
            const prevWidth = el.style.width;

            el.style.border = 'none';
            el.style.borderRadius = '0';
            el.style.boxShadow = 'none';
            el.style.marginBottom = '0';

            const renderW = isPortraitReport ? 840 : 1220;
            if (!isPortraitReport) {
                el.style.padding = '2px';
            }
            el.style.width = `${renderW}px`;

            const canvas = await html2canvas(el, {
                scale: pages.length > 20 ? 1.5 : 2,
                useCORS: true,
                backgroundColor: '#ffffff',
                logging: false,
                width: renderW,
                windowWidth: renderW + 60
            });

            el.style.border = prevBorder;
            el.style.borderRadius = prevRadius;
            el.style.boxShadow = prevShadow;
            el.style.marginBottom = prevMargin;
            el.style.padding = prevPadding;
            el.style.width = prevWidth;

            const imgData = canvas.toDataURL('image/jpeg', 0.95);
            const imgW = canvas.width;
            const imgH = canvas.height;
            const ratio = Math.min(maxW / imgW, maxH / imgH);
            const drawW = imgW * ratio;
            const drawH = imgH * ratio;
            const offsetX = margin + (maxW - drawW) / 2;
            const offsetY = isPortraitReport ? margin : (margin + (maxH - drawH) / 2);

            if (i > 0) pdf.addPage('a4', orientation);
            pdf.addImage(imgData, 'JPEG', offsetX, offsetY, drawW, drawH, undefined, 'FAST');

            // Yield briefly so UI progress stays smooth on large classes
            await new Promise(r => setTimeout(r, 10));
        }

        const periodSuffix = (
            docType === 'TAMDAN_ONE' || docType === 'TAMDAN_ALL' ||
            docType === 'PRITIBAT_MONTH_ONE' || docType === 'PRITIBAT_MONTH_ALL'
        ) ? `_${document.getElementById('reportPeriodSelect').value}` : '';
        pdf.save(`${docType}${periodSuffix}_${HR.className}_${HR.academicYear}.pdf`);
        Swal.close();
    } catch (e) {
        console.error('PDF generation error:', e);
        Swal.close();
        Swal.fire({
            icon: 'warning',
            title: 'សូមប្រើប៊ូតុងព្រីន (Save as PDF)',
            text: 'មានបញ្ហាក្នុងការបង្កើត PDF ដោយផ្ទាល់។ សូមចុច «🖨️ ព្រីន» រួចជ្រើស Save as PDF។'
        });
    }
}

// ==================== TAB 6: STUDENT EXTRA, ID CARDS & SALAKABAT ====================
function renderCardsTab() {
    const mode = document.getElementById('cardModeSelect').value;
    const salaSel = document.getElementById('salakabatStudentSelect');
    salaSel.style.display = mode === 'SALAKABAT' ? 'inline-block' : 'none';
    const area = document.getElementById('cardsTabArea');

    if (mode === 'EDIT_EXTRA') {
        area.innerHTML = `
            <div class="table-wrap">
                <table class="hr-table" style="font-size:0.82rem;">
                    <thead>
                        <tr><th>ល.រ</th><th>រូបថត</th><th>គោត្តនាម-នាម</th><th>ថ្ងៃខែឆ្នាំកំណើត</th><th>ទីកន្លែងកំណើត</th><th>លេខទូរស័ព្ទ</th><th>មតិគ្រូបន្ទុកថ្នាក់</th><th>រក្សាទុក</th></tr>
                    </thead>
                    <tbody>
                        ${HR.students.map((s, i) => `
                            <tr id="extra-row-${s.id}">
                                <td>${i + 1}</td>
                                <td>
                                    <img id="extra-img-${s.id}" src="${esc(s.photoUrl || 'https://ui-avatars.com/api/?background=e2e8f0&color=334155&name=' + encodeURIComponent(s.name))}" style="width:42px;height:52px;object-fit:cover;border-radius:6px;display:block;margin:0 auto 4px;">
                                    <input type="file" accept="image/*" style="width:85px;font-size:0.7rem;" onchange="uploadStuPhoto(${s.id}, this)">
                                </td>
                                <td class="text-left" style="font-weight:700;">${esc(s.name)}<br><small style="color:#64748b;">${esc(s.studentCode)}</small></td>
                                <td><input type="text" class="form-control ex-dob" value="${esc(s.dob)}" placeholder="ឧ. ១៧ មករា ២០១០"></td>
                                <td><input type="text" class="form-control ex-pob" value="${esc(s.pob)}" placeholder="ភូមិ-ឃុំ-ស្រុក-ខេត្ត"></td>
                                <td><input type="text" class="form-control ex-phone" value="${esc(s.phone)}" placeholder="012 345 678"></td>
                                <td><input type="text" class="form-control ex-comment" value="${esc(s.teacherComment)}" placeholder="មតិលើព្រឹត្តិបត្រ"></td>
                                <td><button class="btn btn-success btn-sm" onclick="saveStudentExtraRow(${s.id})">💾</button></td>
                            </tr>`).join('')}
                    </tbody>
                </table>
            </div>`;
        return;
    }

    const cfg = HR.admin.config || {};
    const school = cfg.school || HR.teacher.schoolName || 'វិទ្យាល័យ';
    const principal = cfg.principal || HR.teacher.principalName || '';

    if (mode === 'ID_CARDS') {
        area.innerHTML = `
            <div class="idcard-grid">
                ${HR.students.map(s => `
                    <div class="idcard">
                        <div class="idcard-head">
                            <div style="font-size:0.78rem;font-weight:700;">${esc(school)}</div>
                            <div class="moul" style="font-size:0.86rem;">ប័ណ្ណសម្គាល់ខ្លួនសិស្ស</div>
                            <div style="font-size:0.72rem;opacity:0.9;">ឆ្នាំសិក្សា៖ ${esc(HR.academicYear)}</div>
                        </div>
                        <div class="idcard-body">
                            <img class="idcard-photo" src="${esc(s.photoUrl || 'https://ui-avatars.com/api/?background=e0e7ff&color=1e3a8a&name=' + encodeURIComponent(s.name))}" alt="Photo">
                            <div style="flex:1;">
                                <div><strong>អត្តលេខ៖</strong> ${esc(s.studentCode)}</div>
                                <div><strong>ឈ្មោះ៖</strong> <strong class="moul" style="color:#1d4ed8;">${esc(s.name)}</strong></div>
                                <div><strong>ភេទ៖</strong> ${esc(s.gender)} &nbsp; <strong>ថ្នាក់៖</strong> ${esc(HR.className)}</div>
                                <div><strong>ថ្ងៃកំណើត៖</strong> ${esc(s.dob || '-')}</div>
                                <div><strong>ទូរស័ព្ទ៖</strong> ${esc(s.phone || '-')}</div>
                                <div style="margin-top:6px;text-align:right;font-size:0.72rem;font-weight:700;">នាយកសាលា៖ ${esc(principal)}</div>
                            </div>
                        </div>
                    </div>`).join('')}
            </div>`;
        return;
    }

    // SALAKABAT (សលាកបត្រឯកត្តជន)
    const s = HR.students.find(x => String(x.id) === String(salaSel.value)) || HR.students[0];
    if (!s) { area.innerHTML = 'គ្មានសិស្ស'; return; }
    area.innerHTML = `
        <div class="print-sheet">
            ${getOfficialHeader('សលាកបត្រឯកត្តជនសិស្ស', `ឆ្នាំសិក្សា ${HR.academicYear}`)}
            <div style="display:flex;gap:20px;align-items:flex-start;margin-bottom:16px;">
                <img src="${esc(s.photoUrl || 'https://ui-avatars.com/api/?background=e2e8f0&color=334155&name=' + encodeURIComponent(s.name))}" style="width:110px;height:135px;object-fit:cover;border:2px solid #94a3b8;border-radius:8px;">
                <table class="hr-table" style="flex:1;">
                    <tbody>
                        <tr><th class="text-left" style="width:30%;">គោត្តនាម និងនាម</th><td class="text-left moul" style="color:#0f766e;">${esc(s.name)}</td><th style="width:15%;">ភេទ</th><td>${esc(s.gender)}</td></tr>
                        <tr><th class="text-left">អត្តលេខសិស្ស</th><td class="text-left">${esc(s.studentCode)}</td><th>ថ្នាក់ទី</th><td>${esc(HR.className)}</td></tr>
                        <tr><th class="text-left">ថ្ងៃខែឆ្នាំកំណើត</th><td class="text-left" colspan="3">${esc(s.dob || '........................................')}</td></tr>
                        <tr><th class="text-left">ទីកន្លែងកំណើត</th><td class="text-left" colspan="3">${esc(s.pob || '........................................')}</td></tr>
                        <tr><th class="text-left">អាសយដ្ឋានបច្ចុប្បន្ន</th><td class="text-left" colspan="3">${esc([s.village, s.commune, s.district, s.province].filter(Boolean).join(', ') || '........................................')}</td></tr>
                        <tr><th class="text-left">ឈ្មោះឪពុក / មុខរបរ</th><td class="text-left" colspan="3">${esc(s.fatherName || '-')} / ${esc(s.fatherOccupation || '-')}</td></tr>
                        <tr><th class="text-left">ឈ្មោះម្ដាយ / មុខរបរ</th><td class="text-left" colspan="3">${esc(s.motherName || '-')} / ${esc(s.motherOccupation || '-')}</td></tr>
                        <tr><th class="text-left">លេខទូរស័ព្ទទំនាក់ទំនង</th><td class="text-left" colspan="3">${esc(s.phone || '-')}</td></tr>
                    </tbody>
                </table>
            </div>
            ${getOfficialFooter()}
        </div>`;
}

async function saveStudentExtraRow(studentId) {
    const tr = document.getElementById(`extra-row-${studentId}`);
    const payload = {
        studentId, className: HR.className,
        dob: tr.querySelector('.ex-dob').value.trim(),
        pob: tr.querySelector('.ex-pob').value.trim(),
        phone: tr.querySelector('.ex-phone').value.trim(),
        teacherComment: tr.querySelector('.ex-comment').value.trim()
    };
    const res = await fetch('/api/homeroom/student-extra/save', {
        method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload)
    });
    if (res.ok) {
        const s = HR.students.find(x => x.id === studentId);
        if (s) Object.assign(s, payload);
        Swal.fire({ icon: 'success', title: 'រក្សាទុកជោគជ័យ', timer: 900, showConfirmButton: false });
    }
}

async function uploadStuPhoto(studentId, input) {
    if (!input.files || !input.files[0]) return;
    const fd = new FormData();
    fd.append('studentId', studentId);
    fd.append('className', HR.className);
    fd.append('file', input.files[0]);
    const res = await fetch('/api/homeroom/student-extra/upload-photo', { method: 'POST', body: fd });
    const data = await res.json();
    if (data.success) {
        document.getElementById(`extra-img-${studentId}`).src = data.photoUrl;
        const s = HR.students.find(x => x.id === studentId);
        if (s) s.photoUrl = data.photoUrl;
        Swal.fire({ icon: 'success', title: 'បាន Upload រូបថតជោគជ័យ', timer: 900, showConfirmButton: false });
    }
}

// ==================== TAB 7: CLEANING, SEATING & YOUTH COUNCIL ====================
function renderAdminSubTab() {
    const sub = document.getElementById('adminSubTab').value;
    const area = document.getElementById('adminSubTabArea');

    if (sub === 'CLEANING') {
        const days = ['ថ្ងៃចន្ទ', 'ថ្ងៃអង្គារ', 'ថ្ងៃពុធ', 'ថ្ងៃព្រហស្បតិ៍', 'ថ្ងៃសុក្រ', 'ថ្ងៃសៅរ៍'];
        if (!HR.admin.cleaning) {
            HR.admin.cleaning = days.map(d => ({ day: d, leader: '', members: '' }));
        }
        area.innerHTML = `
            <div class="print-sheet">
                ${getOfficialHeader('តារាងវេនសម្អាតថ្នាក់រៀនប្រចាំសប្តាហ៍', `ថ្នាក់ទី ${HR.className}`)}
                <div class="no-print" style="margin-bottom:10px;">
                    <button class="btn btn-outline btn-sm" onclick="autoDistributeCleaning()">⚡ ចែកវេនសម្អាតស្វ័យប្រវត្តិគ្រប់សិស្ស</button>
                </div>
                <table class="hr-table">
                    <thead><tr><th style="width:16%;">ថ្ងៃ</th><th style="width:24%;">ប្រធានក្រុមវេន</th><th>សមាជិកក្រុមសម្អាត</th></tr></thead>
                    <tbody>
                        ${HR.admin.cleaning.map((c, i) => `
                            <tr>
                                <td style="font-weight:800;">${esc(c.day)}</td>
                                <td><input type="text" class="form-control" value="${esc(c.leader)}" oninput="HR.admin.cleaning[${i}].leader=this.value"></td>
                                <td><textarea class="form-control" rows="2" oninput="HR.admin.cleaning[${i}].members=this.value">${esc(c.members)}</textarea></td>
                            </tr>`).join('')}
                    </tbody>
                </table>
                ${getOfficialFooter()}
            </div>`;
        return;
    }

    if (sub === 'SEATING') {
        if (!HR.admin.seating) {
            HR.admin.seating = { rows: 6, cols: 4, seats: {} };
        }
        const { rows, cols, seats } = HR.admin.seating;
        let cells = '';
        for (let r = 1; r <= rows; r++) {
            for (let c = 1; c <= cols; c++) {
                const key = `${r}_${c}`;
                const val = seats[key] || '';
                cells += `
                    <div class="seat-box">
                        <div style="font-size:0.72rem;color:#64748b;font-weight:700;">ជួរ ${r} - តុ ${c}</div>
                        <select onchange="HR.admin.seating.seats['${key}']=this.value">
                            <option value="">-- ទំនេរ --</option>
                            ${HR.students.map(s => `<option value="${esc(s.name)}" ${val === s.name ? 'selected' : ''}>${esc(s.name)}</option>`).join('')}
                        </select>
                    </div>`;
            }
        }
        area.innerHTML = `
            <div class="print-sheet">
                ${getOfficialHeader('ប្លង់តុអង្គុយរបស់សិស្សប្រចាំថ្នាក់', `ថ្នាក់ទី ${HR.className}`)}
                <div class="no-print" style="display:flex;gap:10px;align-items:center;margin-bottom:12px;flex-wrap:wrap;">
                    <label>ចំនួនជួរ៖ <input type="number" min="2" max="12" value="${rows}" style="width:60px;" onchange="HR.admin.seating.rows=Number(this.value);renderAdminSubTab()"></label>
                    <label>ចំនួនតុ/ជួរ៖ <input type="number" min="2" max="8" value="${cols}" style="width:60px;" onchange="HR.admin.seating.cols=Number(this.value);renderAdminSubTab()"></label>
                    <button class="btn btn-outline btn-sm" onclick="autoFillSeating()">⚡ រៀបតុសិស្សស្វ័យប្រវត្តិ</button>
                </div>
                <div class="seating-board">ក្តារខៀន (WHITEBOARD / តុគ្រូ)</div>
                <div class="seating-grid" style="grid-template-columns:repeat(${cols}, 1fr);">${cells}</div>
                ${getOfficialFooter()}
            </div>`;
        return;
    }

    // YOUTH COUNCIL (ក្រុមប្រឹក្សាយុវជន)
    if (!HR.admin.youth) {
        HR.admin.youth = [
            { role: 'ប្រធានថ្នាក់', studentName: '', phone: '', note: 'ដឹកនាំរួមក្នុងថ្នាក់' },
            { role: 'អនុប្រធានថ្នាក់ទី១', studentName: '', phone: '', note: 'ទទួលបន្ទុកការសិក្សា និងវត្តមាន' },
            { role: 'អនុប្រធានថ្នាក់ទី២', studentName: '', phone: '', note: 'ទទួលបន្ទុកអនាម័យ និងបរិស្ថាន' },
            { role: 'ប្រធានក្រុមទី១', studentName: '', phone: '', note: 'គ្រប់គ្រងក្រុមទី១' },
            { role: 'ប្រធានក្រុមទី២', studentName: '', phone: '', note: 'គ្រប់គ្រងក្រុមទី២' },
            { role: 'ប្រធានក្រុមទី៣', studentName: '', phone: '', note: 'គ្រប់គ្រងក្រុមទី៣' },
            { role: 'ប្រធានក្រុមទី៤', studentName: '', phone: '', note: 'គ្រប់គ្រងក្រុមទី៤' },
            { role: 'ទទួលបន្ទុកកីឡា និងសិល្បៈ', studentName: '', phone: '', note: '' },
            { role: 'ទទួលបន្ទុកបណ្ណាល័យ និងសៀវភៅ', studentName: '', phone: '', note: '' }
        ];
    }
    area.innerHTML = `
        <div class="print-sheet">
            ${getOfficialHeader('បញ្ជីរាយនាមក្រុមប្រឹក្សាយុវជន និងគណៈកម្មការប្រចាំថ្នាក់', `ថ្នាក់ទី ${HR.className}`)}
            <table class="hr-table">
                <thead><tr><th>ល.រ</th><th>តួនាទី / ភារកិច្ច</th><th>គោត្តនាម និងនាមសិស្ស</th><th>លេខទូរស័ព្ទ</th><th>ផ្សេងៗ</th></tr></thead>
                <tbody>
                    ${HR.admin.youth.map((y, i) => `
                        <tr>
                            <td>${i + 1}</td>
                            <td class="text-left" style="font-weight:700;">${esc(y.role)}</td>
                            <td>
                                <select class="form-control" onchange="HR.admin.youth[${i}].studentName=this.value">
                                    <option value="">-- ជ្រើសរើសសិស្ស --</option>
                                    ${HR.students.map(s => `<option value="${esc(s.name)}" ${y.studentName === s.name ? 'selected' : ''}>${esc(s.name)}</option>`).join('')}
                                </select>
                            </td>
                            <td><input type="text" class="form-control" value="${esc(y.phone)}" oninput="HR.admin.youth[${i}].phone=this.value"></td>
                            <td><input type="text" class="form-control" value="${esc(y.note)}" oninput="HR.admin.youth[${i}].note=this.value"></td>
                        </tr>`).join('')}
                </tbody>
            </table>
            ${getOfficialFooter()}
        </div>`;
}

function autoDistributeCleaning() {
    const days = ['ថ្ងៃចន្ទ', 'ថ្ងៃអង្គារ', 'ថ្ងៃពុធ', 'ថ្ងៃព្រហស្បតិ៍', 'ថ្ងៃសុក្រ', 'ថ្ងៃសៅរ៍'];
    const groups = days.map(d => ({ day: d, leader: '', membersList: [] }));
    HR.students.forEach((s, idx) => {
        const g = groups[idx % 6];
        if (!g.leader) g.leader = s.name;
        else g.membersList.push(s.name);
    });
    HR.admin.cleaning = groups.map(g => ({ day: g.day, leader: g.leader, members: g.membersList.join(', ') }));
    renderAdminSubTab();
}

function autoFillSeating() {
    const { rows, cols } = HR.admin.seating;
    const seats = {};
    let idx = 0;
    for (let r = 1; r <= rows; r++) {
        for (let c = 1; c <= cols; c++) {
            if (idx < HR.students.length) {
                seats[`${r}_${c}`] = HR.students[idx++].name;
            }
        }
    }
    HR.admin.seating.seats = seats;
    renderAdminSubTab();
}

async function saveCurrentAdminSubTab() {
    const sub = document.getElementById('adminSubTab').value;
    if (sub === 'CLEANING') await saveAdminField('cleaningScheduleJson', JSON.stringify(HR.admin.cleaning));
    else if (sub === 'SEATING') await saveAdminField('seatingLayoutJson', JSON.stringify(HR.admin.seating));
    else if (sub === 'YOUTH') await saveAdminField('youthCouncilJson', JSON.stringify(HR.admin.youth));
}

// ==================== TAB 8: INVENTORY & TEXTBOOK LOANS ====================
function renderAssetSubTab() {
    const sub = document.getElementById('assetSubTab').value;
    const area = document.getElementById('assetSubTabArea');

    if (sub === 'INVENTORY') {
        if (!HR.admin.inventory) {
            HR.admin.inventory = [
                { item: 'តុសិស្ស', total: 22, good: 22, broken: 0, note: '' },
                { item: 'កៅអីសិស្ស', total: 44, good: 44, broken: 0, note: '' },
                { item: 'តុ និងកៅអីគ្រូ', total: 1, good: 1, broken: 0, note: '' },
                { item: 'ក្តារខៀន / Whiteboard', total: 1, good: 1, broken: 0, note: '' },
                { item: 'កង្ហារពិដាន / ជញ្ជាំង', total: 4, good: 4, broken: 0, note: '' },
                { item: 'អំពូលភ្លើង', total: 6, good: 6, broken: 0, note: '' },
                { item: 'កន្ត្រកសម្រាម និងអំបោស', total: 2, good: 2, broken: 0, note: '' }
            ];
        }
        area.innerHTML = `
            <div class="print-sheet">
                ${getOfficialHeader('បញ្ជីសារពើភ័ណ្ឌសម្ភារៈក្នុងថ្នាក់រៀន', `ថ្នាក់ទី ${HR.className}`)}
                <table class="hr-table">
                    <thead><tr><th>ល.រ</th><th>ឈ្មោះសម្ភារៈ</th><th>ចំនួនសរុប</th><th>ល្អ</th><th>ខូច</th><th>កំណត់ចំណាំ</th><th class="no-print">លុប</th></tr></thead>
                    <tbody>
                        ${HR.admin.inventory.map((it, i) => `
                            <tr>
                                <td>${i + 1}</td>
                                <td><input type="text" class="form-control" value="${esc(it.item)}" oninput="HR.admin.inventory[${i}].item=this.value"></td>
                                <td><input type="number" class="form-control" value="${it.total}" oninput="HR.admin.inventory[${i}].total=Number(this.value)"></td>
                                <td><input type="number" class="form-control" value="${it.good}" oninput="HR.admin.inventory[${i}].good=Number(this.value)"></td>
                                <td><input type="number" class="form-control" value="${it.broken}" oninput="HR.admin.inventory[${i}].broken=Number(this.value)"></td>
                                <td><input type="text" class="form-control" value="${esc(it.note)}" oninput="HR.admin.inventory[${i}].note=this.value"></td>
                                <td class="no-print"><button class="btn btn-danger btn-sm" onclick="HR.admin.inventory.splice(${i},1);renderAssetSubTab()">✕</button></td>
                            </tr>`).join('')}
                    </tbody>
                </table>
                ${getOfficialFooter()}
            </div>`;
        return;
    }

    // BOOKS (បញ្ជីខ្ចី-សងសៀវភៅពុម្ព)
    if (!HR.admin.books) {
        HR.admin.books = HR.students.map(s => ({ studentName: s.name, bookCount: 0, bookList: '', borrowDate: '', status: 'កំពុងខ្ចី' }));
    }
    area.innerHTML = `
        <div class="print-sheet">
            ${getOfficialHeader('បញ្ជីខ្ចី-សងសៀវភៅពុម្ពសិស្ស', `ថ្នាក់ទី ${HR.className}`)}
            <table class="hr-table">
                <thead><tr><th>ល.រ</th><th>គោត្តនាម-នាមសិស្ស</th><th>ចំនួនក្បាល</th><th>មុខវិជ្ជា / ឈ្មោះសៀវភៅ</th><th>ថ្ងៃខ្ចី</th><th>ស្ថានភាព</th></tr></thead>
                <tbody>
                    ${HR.admin.books.map((b, i) => `
                        <tr>
                            <td>${i + 1}</td>
                            <td><input type="text" class="form-control" value="${esc(b.studentName)}" oninput="HR.admin.books[${i}].studentName=this.value"></td>
                            <td><input type="number" class="form-control" style="width:72px;" value="${b.bookCount}" oninput="HR.admin.books[${i}].bookCount=Number(this.value)"></td>
                            <td><input type="text" class="form-control" value="${esc(b.bookList)}" placeholder="ឧ. ខ្មែរ, គណិត, អង់គ្លេស..." oninput="HR.admin.books[${i}].bookList=this.value"></td>
                            <td><input type="text" class="form-control" value="${esc(b.borrowDate)}" placeholder="dd/mm/yyyy" oninput="HR.admin.books[${i}].borrowDate=this.value"></td>
                            <td>
                                <select class="form-control" onchange="HR.admin.books[${i}].status=this.value">
                                    <option value="កំពុងខ្ចី" ${b.status === 'កំពុងខ្ចី' ? 'selected' : ''}>កំពុងខ្ចី</option>
                                    <option value="សងរួចរាល់" ${b.status === 'សងរួចរាល់' ? 'selected' : ''}>សងរួចរាល់</option>
                                    <option value="បាត់/ខូច" ${b.status === 'បាត់/ខូច' ? 'selected' : ''}>បាត់/ខូច</option>
                                </select>
                            </td>
                        </tr>`).join('')}
                </tbody>
            </table>
            ${getOfficialFooter()}
        </div>`;
}

function addAssetRow() {
    const sub = document.getElementById('assetSubTab').value;
    if (sub === 'INVENTORY') {
        HR.admin.inventory.push({ item: '', total: 1, good: 1, broken: 0, note: '' });
    } else {
        HR.admin.books.push({ studentName: '', bookCount: 1, bookList: '', borrowDate: '', status: 'កំពុងខ្ចី' });
    }
    renderAssetSubTab();
}

async function saveCurrentAssetSubTab() {
    const sub = document.getElementById('assetSubTab').value;
    if (sub === 'INVENTORY') await saveAdminField('inventoryJson', JSON.stringify(HR.admin.inventory));
    else await saveAdminField('textbookLoansJson', JSON.stringify(HR.admin.books));
}

// ==================== TAB 9: GEIP PLANS & MEETING LOGS ====================
function renderGeipSubTab() {
    const sub = document.getElementById('geipSubTab').value;
    const area = document.getElementById('geipSubTabArea');

    if (sub === 'GEIP') {
        if (!HR.admin.geip) {
            HR.admin.geip = [
                { goal: 'កាត់បន្ថយអវត្តមានគ្មានច្បាប់ឱ្យនៅក្រោម ២%', horizon: '១ឆ្នាំ', t1: 'កំពុងអនុវត្ត', t2: '', t3: '', t4: '', responsible: 'គ្រូបន្ទុក + អាណាព្យាបាល' },
                { goal: 'បង្កើនអត្រាសិស្សជាប់មធ្យមភាគឱ្យបាន ៩៥% ឡើងទៅ', horizon: '១ឆ្នាំ', t1: 'កំពុងអនុវត្ត', t2: '', t3: '', t4: '', responsible: 'គ្រូបន្ទុក + គ្រូមុខវិជ្ជា' },
                { goal: 'បំប៉នសិស្សរៀនយឺតមុខវិជ្ជាគណិតវិទ្យា និងភាសាអង់គ្លេស', horizon: '៣ខែ', t1: 'កំពុងអនុវត្ត', t2: '', t3: '', t4: '', responsible: 'ក្រុមសិស្សពូកែជួយសិស្សខ្សោយ' }
            ];
        }
        area.innerHTML = `
            <div class="print-sheet">
                ${getOfficialHeader('ផែនការកែលម្អការសិក្សា GEIP (៣ឆ្នាំ / ១ឆ្នាំ / ត្រីមាស ១-៤) និងកិច្ចព្រមព្រៀង', `ថ្នាក់ទី ${HR.className}`)}
                <table class="hr-table" style="font-size:0.82rem;">
                    <thead><tr><th>ល.រ</th><th>គោលដៅ / សកម្មភាពផែនការ (GEIP)</th><th>រយៈពេល</th><th>ត្រីមាស១</th><th>ត្រីមាស២</th><th>ត្រីមាស៣</th><th>ត្រីមាស៤</th><th>អ្នកទទួលខុសត្រូវ</th><th class="no-print">លុប</th></tr></thead>
                    <tbody>
                        ${HR.admin.geip.map((g, i) => `
                            <tr>
                                <td>${i + 1}</td>
                                <td><input type="text" class="form-control" value="${esc(g.goal)}" oninput="HR.admin.geip[${i}].goal=this.value"></td>
                                <td><input type="text" class="form-control" style="width:76px;" value="${esc(g.horizon)}" oninput="HR.admin.geip[${i}].horizon=this.value"></td>
                                <td><input type="text" class="form-control" value="${esc(g.t1)}" oninput="HR.admin.geip[${i}].t1=this.value"></td>
                                <td><input type="text" class="form-control" value="${esc(g.t2)}" oninput="HR.admin.geip[${i}].t2=this.value"></td>
                                <td><input type="text" class="form-control" value="${esc(g.t3)}" oninput="HR.admin.geip[${i}].t3=this.value"></td>
                                <td><input type="text" class="form-control" value="${esc(g.t4)}" oninput="HR.admin.geip[${i}].t4=this.value"></td>
                                <td><input type="text" class="form-control" value="${esc(g.responsible)}" oninput="HR.admin.geip[${i}].responsible=this.value"></td>
                                <td class="no-print"><button class="btn btn-danger btn-sm" onclick="HR.admin.geip.splice(${i},1);renderGeipSubTab()">✕</button></td>
                            </tr>`).join('')}
                    </tbody>
                </table>
                ${getOfficialFooter()}
            </div>`;
        return;
    }

    // MEETING LOGS (កំណត់ហេតុប្រជុំជីវភាពថ្នាក់)
    if (!HR.admin.meetings) {
        HR.admin.meetings = [
            { date: '', topic: 'ប្រជុំជីវភាពថ្នាក់ប្រចាំខែ (វិន័យ អនាម័យ និងលទ្ធផលសិក្សា)', summary: '', attendees: `${HR.students.length} នាក់` }
        ];
    }
    area.innerHTML = `
        <div class="print-sheet">
            ${getOfficialHeader('សៀវភៅកំណត់ហេតុប្រជុំជីវភាពថ្នាក់រៀន', `ថ្នាក់ទី ${HR.className}`)}
            <table class="hr-table">
                <thead><tr><th>ល.រ</th><th style="width:14%;">កាលបរិច្ឆេទ</th><th style="width:26%;">ប្រធានបទប្រជុំ</th><th>ខ្លឹមសារសង្ខេប និងដំណោះស្រាយ</th><th style="width:12%;">អ្នកចូលរួម</th><th class="no-print">លុប</th></tr></thead>
                <tbody>
                    ${HR.admin.meetings.map((m, i) => `
                        <tr>
                            <td>${i + 1}</td>
                            <td><input type="text" class="form-control" value="${esc(m.date)}" placeholder="dd/mm/yyyy" oninput="HR.admin.meetings[${i}].date=this.value"></td>
                            <td><input type="text" class="form-control" value="${esc(m.topic)}" oninput="HR.admin.meetings[${i}].topic=this.value"></td>
                            <td><textarea class="form-control" rows="2" oninput="HR.admin.meetings[${i}].summary=this.value">${esc(m.summary)}</textarea></td>
                            <td><input type="text" class="form-control" value="${esc(m.attendees)}" oninput="HR.admin.meetings[${i}].attendees=this.value"></td>
                            <td class="no-print"><button class="btn btn-danger btn-sm" onclick="HR.admin.meetings.splice(${i},1);renderGeipSubTab()">✕</button></td>
                        </tr>`).join('')}
                </tbody>
            </table>
            ${getOfficialFooter()}
        </div>`;
}

function addGeipOrMeetingRow() {
    const sub = document.getElementById('geipSubTab').value;
    if (sub === 'GEIP') HR.admin.geip.push({ goal: '', horizon: '១ឆ្នាំ', t1: '', t2: '', t3: '', t4: '', responsible: '' });
    else HR.admin.meetings.push({ date: '', topic: '', summary: '', attendees: `${HR.students.length} នាក់` });
    renderGeipSubTab();
}

async function saveCurrentGeipSubTab() {
    const sub = document.getElementById('geipSubTab').value;
    if (sub === 'GEIP') await saveAdminField('geipPlanJson', JSON.stringify(HR.admin.geip));
    else await saveAdminField('meetingLogsJson', JSON.stringify(HR.admin.meetings));
}

async function saveAdminField(fieldName, jsonStr) {
    const payload = { className: HR.className, academicYear: HR.academicYear };
    payload[fieldName] = jsonStr;
    const res = await fetch('/api/homeroom/admin/save', {
        method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload)
    });
    const data = await res.json();
    if (data.success) {
        Swal.fire({ icon: 'success', title: 'រក្សាទុកជោគជ័យ!', timer: 1200, showConfirmButton: false });
    } else {
        Swal.fire('បរាជ័យ', data.error || 'មិនអាចរក្សាទុកបានទេ', 'error');
    }
}

function downloadJsonBackup() {
    const blob = new Blob([JSON.stringify(HR, null, 2)], { type: 'application/json' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = `Homeroom_Backup_${HR.className}_${HR.academicYear}.json`;
    a.click();
}

// ==================== TAB: MONTHLY ATTENDANCE (EXACT MOEYS PDF LAYOUT) ====================
const KHMER_MONTHS = {
    1: 'មករា', 2: 'កុម្ភៈ', 3: 'មីនា', 4: 'មេសា', 5: 'ឧសភា', 6: 'មិថុនា',
    7: 'កក្កដា', 8: 'សីហា', 9: 'កញ្ញា', 10: 'តុលា', 11: 'វិច្ឆិកា', 12: 'ធ្នូ'
};
const KHMER_WEEKDAYS = ['អាទិ.', 'ច.', 'អ.', 'ពុ.', 'ព្រហ.', 'សុ.', 'ស.'];

function getMonthlyAttKey() {
    const m = Number(document.getElementById('attMonthSelect').value || 11);
    const y = Number(document.getElementById('attYearSelect').value || 2026);
    return { month: m, year: y, key: `${y}-${String(m).padStart(2, '0')}` };
}

function getOrCreateMonthAtt(key) {
    if (!HR.admin.monthlyAtt || typeof HR.admin.monthlyAtt !== 'object') {
        HR.admin.monthlyAtt = {};
    }
    if (!HR.admin.monthlyAtt[key]) {
        HR.admin.monthlyAtt[key] = { holidays: [], marks: {} };
    }
    if (!Array.isArray(HR.admin.monthlyAtt[key].holidays)) HR.admin.monthlyAtt[key].holidays = [];
    if (!HR.admin.monthlyAtt[key].marks) HR.admin.monthlyAtt[key].marks = {};
    return HR.admin.monthlyAtt[key];
}

function getShortGender(g) {
    if (!g) return '';
    const s = String(g).trim().toLowerCase();
    if (s === 'ស្រី' || s === 'ស' || s === 'female' || s === 'f') return 'ស';
    return 'ប';
}

function renderMonthlyAttendance(preserveScroll = false) {
    const area = document.getElementById('monthlyAttendanceArea');
    if (!area) return;
    const prevWrap = document.getElementById('attScrollWrap');
    const prevScrollLeft = (preserveScroll && prevWrap) ? prevWrap.scrollLeft : 0;
    const prevScrollTop = (preserveScroll && prevWrap) ? prevWrap.scrollTop : 0;

    const { month, year, key } = getMonthlyAttKey();
    const daysInMonth = new Date(year, month, 0).getDate();
    const mData = getOrCreateMonthAtt(key);
    const holidaySet = new Set(mData.holidays.map(Number));

    const now = new Date();
    const nowY = now.getFullYear();
    const nowM = now.getMonth() + 1;
    const nowD = now.getDate();

    // Build day metadata (Sunday = auto-holiday; highlight today in bright yellow)
    const days = [];
    let studyDays = 0;
    for (let d = 1; d <= daysInMonth; d++) {
        const dow = new Date(year, month - 1, d).getDay(); // 0 = Sun
        const isSunday = dow === 0;
        const isHoliday = isSunday || holidaySet.has(d);
        const isToday = (year === nowY && month === nowM && d === nowD);
        if (!isHoliday) studyDays++;
        days.push({
            day: d,
            label: String(d).padStart(2, '0'),
            weekday: KHMER_WEEKDAYS[dow],
            isSunday,
            isHoliday,
            isToday
        });
    }

    let grandTotalAbsent = 0;
    const studentRowsHtml = HR.students.map((st, idx) => {
        let perm = 0, unex = 0;
        const cells = days.map(d => {
            if (d.isHoliday) {
                return `<td class="att-day-col att-holiday"></td>`;
            }
            const mk = mData.marks[`${st.id}_${d.day}`] || '';
            if (mk === 'ច') perm++;
            else if (mk === 'អ') unex++;
            const cls = (mk === 'ច' ? 'att-cell-p' : (mk === 'អ' ? 'att-cell-a' : '')) + (d.isToday ? ' att-today-cell' : '');
            return `<td class="att-day-col att-cell ${cls}" onclick="cycleAttMark(${st.id}, ${d.day})" title="${esc(st.name)} - ថ្ងៃទី ${d.label}${d.isToday ? ' (ថ្ងៃនេះ)' : ''}">${esc(mk)}</td>`;
        }).join('');
        const tot = perm + unex;
        grandTotalAbsent += tot;
        return `
            <tr>
                <td class="att-sticky-1">${idx + 1}</td>
                <td class="att-name att-sticky-2">${esc(st.name)}</td>
                <td>${getShortGender(st.gender)}</td>
                ${cells}
                <td>${perm}</td>
                <td>${unex}</td>
                <td style="font-weight:800;">${tot}</td>
            </tr>`;
    }).join('');

    const totalStudents = HR.students.length;
    const totalRequired = totalStudents * studyDays;
    const totalActualPresent = Math.max(0, totalRequired - grandTotalAbsent);
    const absentPercent = totalRequired > 0 ? ((grandTotalAbsent / totalRequired) * 100).toFixed(2) : '0.00';

    const cfg = HR.admin.config || {};
    const school = cfg.school || HR.teacher.schoolName || 'វិទ្យាល័យ';
    const monthKh = KHMER_MONTHS[month] || month;

    area.innerHTML = `
        <div class="print-sheet" style="padding:14px 16px;">
            <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:6px;">
                <div style="text-align:left;font-weight:800;font-size:0.9rem;min-width:220px;">
                    <div class="moul" style="font-size:0.9rem;">${esc(school)}</div>
                </div>
                <div style="text-align:center;flex:1;">
                    <div class="moul" style="font-size:0.98rem;">ព្រះរាជាណាចក្រកម្ពុជា</div>
                    <div class="moul" style="font-size:0.88rem;margin-top:2px;">ជាតិ សាសនា ព្រះមហាក្សត្រ</div>
                    <div style="letter-spacing:3px;font-size:0.8rem;">~❖~</div>
                </div>
                <div style="min-width:220px;"></div>
            </div>
            <div style="text-align:center;margin:4px 0 10px;">
                <div class="moul" style="font-size:0.95rem;">
                    បញ្ជីស្រង់អវត្តមានសម្រាប់ខែ${esc(monthKh)} ថ្នាក់ទី ${esc(HR.className)} ឆ្នាំសិក្សា ${esc(HR.academicYear)}
                </div>
            </div>

            <div id="attScrollWrap" class="att-scroll-wrap">
                <div class="att-scroll-inner">
                <table class="att-pdf-table">
                    <thead>
                        <tr>
                            <th colspan="2" class="att-sticky-12" style="text-align:left;padding:4px 8px;font-weight:800;font-size:0.82rem;">ជ្រើសរើសថ្ងៃឈប់សម្រាក</th>
                            <th></th>
                            ${days.map(d => `
                                <th class="att-day-col ${d.isToday && !d.isHoliday ? 'att-today-head' : ''}" style="padding:2px;">
                                    <input type="checkbox" class="att-holiday-toggle no-print" ${d.isHoliday ? 'checked' : ''} ${d.isSunday ? 'disabled title="ថ្ងៃអាទិត្យ"' : `onchange="toggleAttHoliday(${d.day})" title="ដាក់/ដកថ្ងៃឈប់សម្រាក ថ្ងៃទី ${d.label}"`}>
                                </th>`).join('')}
                            <th colspan="3"></th>
                        </tr>
                        <tr>
                            <th rowspan="2" class="att-col-fit att-sticky-1" style="padding:4px 6px;">ល.រ</th>
                            <th rowspan="2" class="att-col-fit att-sticky-2" style="padding:4px 10px;">គោត្តនាម និងនាម</th>
                            <th rowspan="2" class="att-col-fit" style="padding:4px 6px;">ភេទ</th>
                            ${days.map(d => `<th ${d.isToday ? 'id="attTodayCol"' : ''} class="att-day-col ${d.isHoliday ? 'att-holiday' : (d.isToday ? 'att-today-head' : '')}">${d.label}${d.isToday && !d.isHoliday ? '<span class="att-today-badge">ថ្ងៃនេះ</span>' : ''}</th>`).join('')}
                            <th rowspan="2" class="att-col-fit"><div class="att-vhead">មានច្បាប់</div></th>
                            <th rowspan="2" class="att-col-fit"><div class="att-vhead">ឥតច្បាប់</div></th>
                            <th rowspan="2" class="att-col-fit"><div class="att-vhead">សរុប</div></th>
                        </tr>
                        <tr>
                            ${days.map(d => `<th class="att-day-col att-day-wd ${d.isHoliday ? 'att-holiday' : (d.isToday ? 'att-today-head' : '')}">${d.weekday}</th>`).join('')}
                        </tr>
                    </thead>
                    <tbody>
                        ${studentRowsHtml || `<tr><td colspan="${daysInMonth + 6}" style="padding:18px;color:#64748b;">គ្មានសិស្សក្នុងថ្នាក់នេះ</td></tr>`}
                    </tbody>
                </table>
                <div class="att-scroll-spacer no-print"></div>
                </div>
            </div>

            <!-- Exact Bottom Summary Statistics matching PDF Page 2 -->
            <div class="att-summary-bar">
                <div>
                    <div class="att-summary-item">
                        <span>ចំនួនពេលសរុបដែលសិស្សត្រូវមករៀន</span>
                        <span style="font-style:normal;font-weight:800;padding:0 10px;">${totalRequired}</span>
                    </div>
                    <div class="att-summary-item" style="margin-top:4px;padding-left:20px;">
                        <span>ពេលដែលសិស្សត្រូវរៀនក្នុង១ខែ</span>
                        <span style="font-style:normal;font-weight:800;padding:0 14px;">${studyDays}</span>
                    </div>
                </div>
                <div class="att-summary-item">
                    <span>ចំនួនពេលអវត្តមាន</span>
                    <span class="att-summary-box">${grandTotalAbsent}</span>
                </div>
                <div class="att-summary-item">
                    <span>ចំនួនពេលដែលសិស្សបានមករៀនពិតប្រាកដ</span>
                    <span class="att-summary-box">${totalActualPresent}</span>
                </div>
                <div class="att-summary-item">
                    <span>ភាគរយនៃអវត្តមាន</span>
                    <span class="att-summary-box">${absentPercent}%</span>
                </div>
            </div>

            ${getOfficialFooter()}
        </div>
    `;

    // Restore scroll position on cell clicks, or smoothly center Today's yellow column on load/month switch
    const wrap = document.getElementById('attScrollWrap');
    if (wrap) {
        if (preserveScroll) {
            wrap.scrollLeft = prevScrollLeft;
            wrap.scrollTop = prevScrollTop;
        } else {
            scrollAttWrapToToday(wrap);
        }
    }
}

function scrollAttWrapToToday(wrap) {
    const todayEl = document.getElementById('attTodayCol');
    if (!wrap || !todayEl) return;
    // Align today's yellow column right next to the sticky No + Student Name columns
    const sticky1 = wrap.querySelector('thead .att-sticky-1');
    const sticky2 = wrap.querySelector('thead .att-sticky-2');
    const stickyW = (sticky1 ? sticky1.offsetWidth : 44) + (sticky2 ? sticky2.offsetWidth : 170) + 6;
    const targetLeft = Math.max(0, todayEl.offsetLeft - stickyW);
    wrap.scrollTo({ left: targetLeft, behavior: 'smooth' });
}

function jumpToAttToday() {
    const now = new Date();
    const mSel = document.getElementById('attMonthSelect');
    const ySel = document.getElementById('attYearSelect');
    if (mSel) mSel.value = String(now.getMonth() + 1);
    if (ySel) {
        const yVal = String(now.getFullYear());
        if (!Array.from(ySel.options).some(o => o.value === yVal)) {
            const opt = document.createElement('option');
            opt.value = yVal;
            opt.textContent = yVal;
            ySel.appendChild(opt);
        }
        ySel.value = yVal;
    }
    renderMonthlyAttendance(false);
}

function toggleAttHoliday(day) {
    const { key } = getMonthlyAttKey();
    const mData = getOrCreateMonthAtt(key);
    const idx = mData.holidays.indexOf(day);
    if (idx >= 0) mData.holidays.splice(idx, 1);
    else mData.holidays.push(day);
    renderMonthlyAttendance(true);
}

function cycleAttMark(studentId, day) {
    const { key } = getMonthlyAttKey();
    const mData = getOrCreateMonthAtt(key);
    const k = `${studentId}_${day}`;
    const cur = mData.marks[k] || '';
    if (cur === '') mData.marks[k] = 'ច';
    else if (cur === 'ច') mData.marks[k] = 'អ';
    else delete mData.marks[k];
    renderMonthlyAttendance(true);
}

function pullExistingAttendanceIntoMonth() {
    const { month, year, key } = getMonthlyAttKey();
    const prefix = `${year}-${String(month).padStart(2, '0')}-`;
    const mData = getOrCreateMonthAtt(key);
    let pulled = 0;
    (HR.dailyAbsences || []).forEach(a => {
        if (a.date && a.date.startsWith(prefix)) {
            const d = parseInt(a.date.substring(8, 10), 10);
            if (!isNaN(d)) {
                mData.marks[`${a.studentId}_${d}`] = a.permission ? 'ច' : 'អ';
                pulled++;
            }
        }
    });
    renderMonthlyAttendance();
    Swal.fire({
        icon: 'info',
        title: pulled > 0 ? `ទាញបាន ${pulled} កំណត់ត្រាអវត្តមាន!` : 'មិនមានអវត្តមានក្នុងខែនេះទេ',
        text: pulled > 0 ? 'សូមចុច «រក្សាទុកអវត្តមានខែនេះ» ដើម្បីរក្សាទុក។' : 'មិនទាន់មានទិន្នន័យអវត្តមានសម្រាប់ខែ-ឆ្នាំដែលបានជ្រើសរើសនៅក្នុងបញ្ជីអវត្តមានចាស់ទេ។',
        timer: 2200,
        showConfirmButton: false
    });
}

async function saveMonthlyAttendance() {
    await saveAdminField('monthlyAttendanceJson', JSON.stringify(HR.admin.monthlyAtt || {}));
}

function exportMonthlyAttendanceExcel() {
    const { month, year, key } = getMonthlyAttKey();
    const daysInMonth = new Date(year, month, 0).getDate();
    const mData = getOrCreateMonthAtt(key);
    const holidaySet = new Set(mData.holidays.map(Number));

    const rows = HR.students.map((st, i) => {
        const obj = { 'ល.រ': i + 1, 'គោត្តនាម-នាម': st.name, 'ភេទ': getShortGender(st.gender) };
        let perm = 0, unex = 0;
        for (let d = 1; d <= daysInMonth; d++) {
            const dow = new Date(year, month - 1, d).getDay();
            const isHol = dow === 0 || holidaySet.has(d);
            const mk = isHol ? 'ឈប់' : (mData.marks[`${st.id}_${d}`] || '');
            if (mk === 'ច') perm++;
            else if (mk === 'អ') unex++;
            obj[String(d).padStart(2, '0')] = mk;
        }
        obj['មានច្បាប់'] = perm;
        obj['ឥតច្បាប់'] = unex;
        obj['សរុប'] = perm + unex;
        return obj;
    });
    const ws = XLSX.utils.json_to_sheet(rows);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, `Attendance_${key}`);
    XLSX.writeFile(wb, `Attendance_${HR.className}_${key}.xlsx`);
}

async function downloadMonthlyAttendanceA3Pdf() {
    const JsPdfCtor = (window.jspdf && window.jspdf.jsPDF) ? window.jspdf.jsPDF : window.jsPDF;
    if (typeof html2canvas === 'undefined' || !JsPdfCtor) { window.print(); return; }

    if (document.fonts && document.fonts.ready) {
        try { await document.fonts.ready; } catch (e) {}
    }

    const { month, year, key } = getMonthlyAttKey();
    const daysInMonth = new Date(year, month, 0).getDate();
    const mData = getOrCreateMonthAtt(key);
    const holidaySet = new Set(mData.holidays.map(Number));

    const days = [];
    let studyDays = 0;
    for (let d = 1; d <= daysInMonth; d++) {
        const dow = new Date(year, month - 1, d).getDay();
        const isSunday = dow === 0;
        const isHoliday = isSunday || holidaySet.has(d);
        if (!isHoliday) studyDays++;
        days.push({ day: d, label: String(d).padStart(2, '0'), weekday: KHMER_WEEKDAYS[dow], isSunday, isHoliday });
    }

    const totalStudents = HR.students.length;
    const stageW = 1920;
    const includeFooterOnPage1 = (totalStudents <= 34);
    const availRowsH = includeFooterOnPage1 ? 820 : 1140;
    const rowH = Math.max(21, Math.min(32, Math.floor(availRowsH / Math.max(totalStudents, 1))));
    const rowFont = rowH <= 22 ? 15 : 16;
    const cellPad = '1px 2px';

    // Crisp 3x HiDPI vertical Khmer headers for summary columns
    const vPermImg = makeVerticalKhmerHeaderImg('មានច្បាប់', true, 78, 30, 15);
    const vUnexImg = makeVerticalKhmerHeaderImg('ឥតច្បាប់', true, 78, 30, 15);
    const vTotImg = makeVerticalKhmerHeaderImg('សរុប', true, 78, 30, 15);

    let grandTotalAbsent = 0;
    const rowsHtml = HR.students.map((st, idx) => {
        let perm = 0, unex = 0;
        const cells = days.map(d => {
            if (d.isHoliday) {
                return `<td style="background:#ff0000;border:1px solid #000;padding:${cellPad};"></td>`;
            }
            const mk = mData.marks[`${st.id}_${d.day}`] || '';
            if (mk === 'ច') perm++;
            else if (mk === 'អ') unex++;
            const color = mk === 'ច' ? '#1d4ed8' : (mk === 'អ' ? '#dc2626' : '#000');
            const bg = mk === 'ច' ? '#eff6ff' : (mk === 'អ' ? '#fef2f2' : '#fff');
            return `<td style="border:1px solid #000;padding:${cellPad};text-align:center;font-weight:700;color:${color};background:${bg};line-height:1.12;">${esc(mk)}</td>`;
        }).join('');
        const tot = perm + unex;
        grandTotalAbsent += tot;
        return `
            <tr style="height:${rowH}px;">
                <td style="border:1px solid #000;text-align:center;padding:${cellPad};line-height:1.12;">${idx + 1}</td>
                <td style="border:1px solid #000;text-align:left;padding:${cellPad} 10px;white-space:nowrap;font-size:${rowFont + 0.5}px;line-height:1.12;">${esc(st.name)}</td>
                <td style="border:1px solid #000;text-align:center;padding:${cellPad};line-height:1.12;">${getShortGender(st.gender)}</td>
                ${cells}
                <td style="border:1px solid #000;text-align:center;padding:${cellPad};line-height:1.12;">${perm}</td>
                <td style="border:1px solid #000;text-align:center;padding:${cellPad};line-height:1.12;">${unex}</td>
                <td style="border:1px solid #000;text-align:center;padding:${cellPad};font-weight:700;line-height:1.12;">${tot}</td>
            </tr>`;
    }).join('');

    const totalRequired = totalStudents * studyDays;
    const totalActualPresent = Math.max(0, totalRequired - grandTotalAbsent);
    const absentPercent = totalRequired > 0 ? ((grandTotalAbsent / totalRequired) * 100).toFixed(2) : '0.00';

    const cfg = HR.admin.config || {};
    const school = cfg.school || HR.teacher.schoolName || 'វិទ្យាល័យ';
    const monthKh = KHMER_MONTHS[month] || month;

    const colgroupHtml = `<colgroup>
        <col style="width:46px;">
        <col style="width:225px;">
        <col style="width:46px;">
        ${days.map(() => `<col>`).join('')}
        <col style="width:48px;">
        <col style="width:48px;">
        <col style="width:48px;">
    </colgroup>`;

    const summaryFooterHtml = `
        <div style="display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:14px;margin-top:12px;font-size:15.5px;color:#000;">
            <div>
                <div style="font-style:italic;font-weight:600;">ចំនួនពេលសរុបដែលសិស្សត្រូវមករៀន <span style="font-style:normal;font-weight:800;padding:0 12px;">${totalRequired}</span></div>
                <div style="font-style:italic;font-weight:600;margin-top:4px;padding-left:24px;">ពេលដែលសិស្សត្រូវរៀនក្នុង១ខែ <span style="font-style:normal;font-weight:800;padding:0 16px;">${studyDays}</span></div>
            </div>
            <div style="display:inline-flex;align-items:center;gap:8px;font-style:italic;font-weight:600;">
                <span>ចំនួនពេលអវត្តមាន</span>
                <span style="border:1px solid #000;padding:3px 16px;min-width:64px;text-align:center;font-style:normal;font-weight:800;">${grandTotalAbsent}</span>
            </div>
            <div style="display:inline-flex;align-items:center;gap:8px;font-style:italic;font-weight:600;">
                <span>ចំនួនពេលដែលសិស្សបានមករៀនពិតប្រាកដ</span>
                <span style="border:1px solid #000;padding:3px 16px;min-width:64px;text-align:center;font-style:normal;font-weight:800;">${totalActualPresent}</span>
            </div>
            <div style="display:inline-flex;align-items:center;gap:8px;font-style:italic;font-weight:600;">
                <span>ភាគរយនៃអវត្តមាន</span>
                <span style="border:1px solid #000;padding:3px 16px;min-width:70px;text-align:center;font-style:normal;font-weight:800;">${absentPercent}%</span>
            </div>
        </div>
        ${getOfficialFooter()}`;

    const stage = document.getElementById('attA3PdfStage');
    stage.style.width = stageW + 'px';
    let stageHtml = `
        <div class="att-a3-page" style="width:${stageW}px;background:#fff;padding:4px 4px 6px 4px;box-sizing:border-box;color:#000;">
            <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:4px;">
                <div style="text-align:left;min-width:280px;padding-top:6px;">
                    <div class="moul" style="font-size:16px;">${esc(school)}</div>
                </div>
                <div style="text-align:center;flex:1;">
                    <div class="moul" style="font-size:17px;">ព្រះរាជាណាចក្រកម្ពុជា</div>
                    <div class="moul" style="font-size:15.5px;margin-top:2px;">ជាតិ សាសនា ព្រះមហាក្សត្រ</div>
                    <div class="moul" style="font-size:16px;margin-top:4px;">
                        បញ្ជីស្រង់អវត្តមានសម្រាប់ខែ${esc(monthKh)} ថ្នាក់ទី ${esc(HR.className)} ឆ្នាំសិក្សា ${esc(HR.academicYear)}
                    </div>
                </div>
                <div style="min-width:280px;"></div>
            </div>
            <table style="width:100%;border-collapse:collapse;table-layout:fixed;font-size:${rowFont}px;color:#000;background:#fff;border:1.4px solid #000;">
                ${colgroupHtml}
                <thead>
                    <tr>
                        <th rowspan="2" style="border:1px solid #000;background:#f1f5f9;font-size:16px;font-weight:700;padding:4px;">ល.រ</th>
                        <th rowspan="2" style="border:1px solid #000;background:#f1f5f9;font-size:16.5px;font-weight:700;padding:4px;">គោត្តនាម និងនាម</th>
                        <th rowspan="2" style="border:1px solid #000;background:#f1f5f9;font-size:16px;font-weight:700;padding:4px;">ភេទ</th>
                        ${days.map(d => `<th style="border:1px solid #000;padding:3px 1px;font-size:14.5px;font-weight:700;background:${d.isHoliday ? '#ff0000' : '#fff'};color:${d.isHoliday ? '#fff' : '#000'};">${d.label}</th>`).join('')}
                        <th rowspan="2" style="border:1px solid #000;background:#f1f5f9;padding:2px;">${vPermImg}</th>
                        <th rowspan="2" style="border:1px solid #000;background:#f1f5f9;padding:2px;">${vUnexImg}</th>
                        <th rowspan="2" style="border:1px solid #000;background:#f1f5f9;padding:2px;">${vTotImg}</th>
                    </tr>
                    <tr>
                        ${days.map(d => `<th style="border:1px solid #000;padding:2px 1px;font-size:12.5px;font-weight:600;background:${d.isHoliday ? '#ff0000' : '#fff'};color:${d.isHoliday ? '#fff' : '#000'};">${d.weekday}</th>`).join('')}
                    </tr>
                </thead>
                <tbody>${rowsHtml}</tbody>
            </table>
            ${includeFooterOnPage1 ? summaryFooterHtml : ''}
        </div>`;

    if (!includeFooterOnPage1) {
        stageHtml += `
            <div class="att-a3-page" style="width:${stageW}px;background:#fff;padding:14px 22px;box-sizing:border-box;color:#000;">
                ${summaryFooterHtml}
            </div>`;
    }
    stage.innerHTML = stageHtml;

    const pages = Array.from(stage.querySelectorAll('.att-a3-page'));
    Swal.fire({
        title: 'កំពុងបង្កើត PDF អវត្តមាន (ក្រដាស A3 ផ្តេក)...',
        html: `<div id="attA3Prog">ទំព័រទី <b>1</b> / <b>${pages.length}</b></div>`,
        allowOutsideClick: false,
        didOpen: () => Swal.showLoading()
    });

    try {
        const pdf = new JsPdfCtor({ orientation: 'landscape', unit: 'mm', format: 'a3' });
        const pageW = pdf.internal.pageSize.getWidth();  // 420mm
        const pageH = pdf.internal.pageSize.getHeight(); // 297mm
        const marginX = 2.5;
        const maxW = pageW - marginX * 2; // 415mm full edge-to-edge width!
        const maxH = pageH - 5;

        for (let i = 0; i < pages.length; i++) {
            const prog = document.getElementById('attA3Prog');
            if (prog) prog.innerHTML = `ទំព័រទី <b>${i + 1}</b> / <b>${pages.length}</b>`;
            const canvas = await html2canvas(pages[i], {
                scale: 2, useCORS: true, backgroundColor: '#ffffff', logging: false, width: stageW, windowWidth: stageW + 40
            });
            const imgData = canvas.toDataURL('image/jpeg', 0.96);
            const drawW = maxW;
            const drawH = Math.min(maxH, canvas.height * (maxW / canvas.width));
            const topY = (i === 0) ? 2.5 : 5;

            if (i > 0) pdf.addPage('a3', 'landscape');
            pdf.addImage(imgData, 'JPEG', marginX, topY, drawW, drawH, undefined, 'FAST');
            await new Promise(r => setTimeout(r, 15));
        }

        pdf.save(`Attendance_A3_Landscape_${HR.className}_${monthKh}_${year}.pdf`);
        Swal.close();
    } catch (e) {
        console.error(e);
        Swal.close();
        window.print();
    } finally {
        stage.innerHTML = '';
    }
}

document.addEventListener('DOMContentLoaded', () => {
    loadHomeroomData();
});
