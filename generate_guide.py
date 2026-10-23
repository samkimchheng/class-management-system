import os
import docx
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import parse_xml
from docx.oxml.ns import nsdecls

def create_document():
    doc = Document()

    # Set page margins (Normal: 1 inch / 2.54 cm)
    for section in doc.sections:
        section.top_margin = Inches(1)
        section.bottom_margin = Inches(1)
        section.left_margin = Inches(1)
        section.right_margin = Inches(1)

    def set_cell_background(cell, hex_color):
        tcPr = cell._tc.get_or_add_tcPr()
        shd = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{hex_color}"/>')
        tcPr.append(shd)

    def set_cell_margins(cell, top=120, bottom=120, left=150, right=150):
        tcPr = cell._tc.get_or_add_tcPr()
        tcMar = parse_xml(f'''
            <w:tcMar {nsdecls("w")}>
                <w:top w:w="{top}" w:type="dxa"/>
                <w:bottom w:w="{bottom}" w:type="dxa"/>
                <w:left w:w="{left}" w:type="dxa"/>
                <w:right w:w="{right}" w:type="dxa"/>
            </w:tcMar>
        ''')
        tcPr.append(tcMar)

    def format_run(run, font_name="Khmer OS Battambang", size_pt=11, bold=False, italic=False, color_rgb=(30, 41, 59)):
        run.font.name = font_name
        run.font.size = Pt(size_pt)
        run.bold = bold
        run.italic = italic
        run.font.color.rgb = RGBColor(*color_rgb)
        rPr = run._r.get_or_add_rPr()
        rFonts = parse_xml(f'<w:rFonts {nsdecls("w")} w:ascii="{font_name}" w:hAnsi="{font_name}" w:cs="{font_name}"/>')
        rPr.append(rFonts)

    def add_callout(title, text, bg_color="F0F9FF", border_color="0284C7"):
        tbl = doc.add_table(rows=1, cols=1)
        tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
        tbl.autofit = False
        tbl.columns[0].width = Inches(6.5)
        
        cell = tbl.cell(0, 0)
        set_cell_background(cell, bg_color)
        set_cell_margins(cell, top=140, bottom=140, left=200, right=200)
        
        tcPr = cell._tc.get_or_add_tcPr()
        borders = parse_xml(f'''
            <w:tcBorders {nsdecls("w")}>
                <w:left w:val="single" w:sz="36" w:space="0" w:color="{border_color}"/>
                <w:top w:val="none"/>
                <w:right w:val="none"/>
                <w:bottom w:val="none"/>
            </w:tcBorders>
        ''')
        tcPr.append(borders)
        
        p = cell.paragraphs[0]
        p.paragraph_format.space_before = Pt(2)
        p.paragraph_format.space_after = Pt(2)
        p.paragraph_format.line_spacing = 1.25
        run_title = p.add_run(f"📌 {title}: ")
        format_run(run_title, font_name="Khmer OS Battambang", size_pt=11, bold=True, color_rgb=(2, 132, 199))
        
        run_text = p.add_run(text)
        format_run(run_text, font_name="Khmer OS Battambang", size_pt=10.5, color_rgb=(51, 65, 85))
        
        p_after = doc.add_paragraph()
        p_after.paragraph_format.space_before = Pt(0)
        p_after.paragraph_format.space_after = Pt(4)

    def add_heading_1(text):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(20)
        p.paragraph_format.space_after = Pt(6)
        p.paragraph_format.keep_with_next = True
        run = p.add_run(text)
        format_run(run, font_name="Khmer OS Muol Light", size_pt=13.5, bold=True, color_rgb=(15, 23, 42))
        
        pPr = p._p.get_or_add_pPr()
        pBdr = parse_xml(f'''
            <w:pBdr {nsdecls("w")}>
                <w:bottom w:val="single" w:sz="16" w:space="4" w:color="0284C7"/>
            </w:pBdr>
        ''')
        pPr.append(pBdr)

    def add_heading_2(text):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(14)
        p.paragraph_format.space_after = Pt(4)
        p.paragraph_format.keep_with_next = True
        run = p.add_run(text)
        format_run(run, font_name="Khmer OS Battambang", size_pt=12, bold=True, color_rgb=(2, 132, 199))

    def add_heading_3(text):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(9)
        p.paragraph_format.space_after = Pt(2)
        p.paragraph_format.keep_with_next = True
        run = p.add_run(text)
        format_run(run, font_name="Khmer OS Battambang", size_pt=11, bold=True, color_rgb=(71, 85, 105))

    def add_body(text, bold_prefix="", space_after=4):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(0)
        p.paragraph_format.space_after = Pt(space_after)
        p.paragraph_format.line_spacing = 1.25
        if bold_prefix:
            r_pre = p.add_run(bold_prefix)
            format_run(r_pre, font_name="Khmer OS Battambang", size_pt=11, bold=True, color_rgb=(30, 41, 59))
        r_body = p.add_run(text)
        format_run(r_body, font_name="Khmer OS Battambang", size_pt=11, color_rgb=(51, 65, 85))

    def add_bullet(text, bold_prefix=""):
        p = doc.add_paragraph(style='List Bullet')
        p.paragraph_format.space_before = Pt(1)
        p.paragraph_format.space_after = Pt(3)
        p.paragraph_format.line_spacing = 1.25
        if bold_prefix:
            r_pre = p.add_run(bold_prefix)
            format_run(r_pre, font_name="Khmer OS Battambang", size_pt=10.5, bold=True, color_rgb=(15, 23, 42))
        r_body = p.add_run(text)
        format_run(r_body, font_name="Khmer OS Battambang", size_pt=10.5, color_rgb=(51, 65, 85))

    # ================= COVER / TITLE SECTION =================
    p_top = doc.add_paragraph()
    p_top.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_top.paragraph_format.space_after = Pt(2)
    r1 = p_top.add_run("ព្រះរាជាណាចក្រកម្ពុជា\nជាតិ សាសនា ព្រះមហាក្សត្រ\n***")
    format_run(r1, font_name="Khmer OS Muol Light", size_pt=13, bold=True, color_rgb=(15, 23, 42))

    p_sch = doc.add_paragraph()
    p_sch.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_sch.paragraph_format.space_before = Pt(10)
    p_sch.paragraph_format.space_after = Pt(14)
    r2 = p_sch.add_run("សាកលវិទ្យាល័យជាតិបាត់ដំបង (NUBB)\nប្រព័ន្ធគ្រប់គ្រងថ្នាក់រៀនអនឡាញ (Class Management System 2027)")
    format_run(r2, font_name="Khmer OS Muol Light", size_pt=13, bold=True, color_rgb=(2, 132, 199))

    # Big Banner Box
    tbl_cover = doc.add_table(rows=1, cols=1)
    tbl_cover.alignment = WD_TABLE_ALIGNMENT.CENTER
    tbl_cover.autofit = False
    tbl_cover.columns[0].width = Inches(6.5)
    c_cover = tbl_cover.cell(0, 0)
    set_cell_background(c_cover, "0F172A")
    set_cell_margins(c_cover, top=260, bottom=260, left=250, right=250)

    p_cover = c_cover.paragraphs[0]
    p_cover.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_cover.paragraph_format.line_spacing = 1.3
    r_cov_title = p_cover.add_run("សៀវភៅមគ្គទេសក៍ និងមេរៀនលម្អិត\nស្តីពីការចូលប្រើប្រាស់ និងគ្រប់គ្រងប្រព័ន្ធ\n")
    format_run(r_cov_title, font_name="Khmer OS Muol Light", size_pt=16, bold=True, color_rgb=(255, 255, 255))
    r_cov_sub = p_cover.add_run("សម្រាប់លោកគ្រូ-អ្នកគ្រូបង្រៀន និងគណៈគ្រប់គ្រង")
    format_run(r_cov_sub, font_name="Khmer OS Battambang", size_pt=12, bold=False, color_rgb=(56, 189, 248))

    p_meta = doc.add_paragraph()
    p_meta.paragraph_format.space_before = Pt(14)
    p_meta.paragraph_format.space_after = Pt(18)
    p_meta.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r_meta = p_meta.add_run("🌐 អាសយដ្ឋានផ្លូវការ៖ https://class.mitpukae.com\n"
                           "✍️ រៀបចំ និងចងក្រងដោយ៖ លោកគ្រូ សំ គឹមឆេង (Sam Kimchheng)\n"
                           "✈️ Telegram ជំនួយបច្ចេកទេសផ្ទាល់៖ https://t.me/Kimchheng12 (ID: @Kimchheng12)\n"
                           "📅 ឆ្នាំសិក្សា៖ ២០២៦ - ២០២៧")
    format_run(r_meta, font_name="Khmer OS Battambang", size_pt=10.5, color_rgb=(71, 85, 105))

    doc.add_page_break()

    # ================= TABLE OF CONTENTS =================
    add_heading_1("មាតិកានៃសៀវភៅណែនាំ (TABLE OF CONTENTS)")
    
    toc_items = [
        ("ជំពូកទី ១", "សេចក្តីផ្តើម និងព័ត៌មានទូទៅអំពីប្រព័ន្ធ"),
        ("ជំពូកទី ២", "របៀបចូលប្រើប្រាស់ប្រព័ន្ធ (Login) និងការគ្រប់គ្រងគណនី"),
        ("ជំពូកទី ៣", "ការស្វែងយល់ពីផ្ទាំងទំព័រដើម (Dashboard Overview) និងម៉ឺនុយ"),
        ("ជំពូកទី ៤", "ការគ្រប់គ្រងបញ្ជីថ្នាក់រៀនផ្ទាល់ខ្លួន (Classroom Management)"),
        ("ជំពូកទី ៥", "ការគ្រប់គ្រងបញ្ជីឈ្មោះសិស្ស (Students Management)"),
        ("ជំពូកទី ៦", "ការកត់ត្រាវត្តមានសិស្សប្រចាំថ្ងៃ (Daily Attendance Tracking)"),
        ("ជំពូកទី ៧", "ការបញ្ចូល និងគ្រប់គ្រងពិន្ទុសិស្ស (Scores & Grading Management)"),
        ("ជំពូកទី ៨", "ស្ថិតិ និងការវិភាគទិន្នន័យសិក្សា (Student Statistics & Analytics)"),
        ("ជំពូកទី ៩", "ការគ្រប់គ្រងវិញ្ញាសា និងការប្រឡងអនឡាញ (Online Exams)"),
        ("ជំពូកទី ១០", "ការគ្រប់គ្រង និងចែករំលែកមេរៀន (Lesson Management)"),
        ("ជំពូកទី ១១", "ការរៀបចំកាលវិភាគបង្រៀន (Teaching Schedule)"),
        ("ជំពូកទី ១២", "ការទាញយក និងបោះពុម្ពរបាយការណ៍សរុប (Reports & Printing)"),
        ("ជំពូកទី ១៣", "ផ្នែកទំនាក់ទំនង និងជំនួយបច្ចេកទេសតាម Telegram"),
        ("ជំពូកទី ១៤", "សុវត្ថិភាពគណនី និងគន្លឹះដោះស្រាយបញ្ហាបច្ចេកទេស")
    ]

    tbl_toc = doc.add_table(rows=1, cols=2)
    tbl_toc.alignment = WD_TABLE_ALIGNMENT.CENTER
    tbl_toc.autofit = False
    tbl_toc.columns[0].width = Inches(1.8)
    tbl_toc.columns[1].width = Inches(4.7)
    
    # Header row
    hdr_cells = tbl_toc.rows[0].cells
    hdr_cells[0].text = "ជំពូក / លេខរៀង"
    hdr_cells[1].text = "ខ្លឹមសារមេរៀនណែនាំ"
    for i, c in enumerate(hdr_cells):
        set_cell_background(c, "0284C7")
        set_cell_margins(c, top=100, bottom=100, left=120, right=120)
        p = c.paragraphs[0]
        for r in p.runs:
            format_run(r, font_name="Khmer OS Battambang", size_pt=11, bold=True, color_rgb=(255, 255, 255))
    
    for ch, desc in toc_items:
        row_cells = tbl_toc.add_row().cells
        row_cells[0].text = ch
        row_cells[1].text = desc
        for i, c in enumerate(row_cells):
            set_cell_background(c, "F8FAFC" if toc_items.index((ch, desc)) % 2 == 0 else "FFFFFF")
            set_cell_margins(c, top=80, bottom=80, left=120, right=120)
            p = c.paragraphs[0]
            for r in p.runs:
                format_run(r, font_name="Khmer OS Battambang", size_pt=10.5, bold=(i == 0), color_rgb=(30, 41, 59))

    # ================= CHAPTER 1 =================
    add_heading_1("ជំពូកទី ១៖ សេចក្តីផ្តើម និងព័ត៌មានទូទៅអំពីប្រព័ន្ធ")
    add_body("ប្រព័ន្ធគ្រប់គ្រងថ្នាក់រៀនអនឡាញ (Class Management System 2027) ត្រូវបានបង្កើតឡើងក្នុងគោលបំណងធ្វើទំនើបកម្ម និងសម្រួលដល់ការងារប្រចាំថ្ងៃរបស់លោកគ្រូ-អ្នកគ្រូ ក្នុងការគ្រប់គ្រងសិស្ស វត្តមាន ពិន្ទុ វិញ្ញាសាប្រឡង មេរៀន កាលវិភាគ និងរបាយការណ៍សរុបប្រចាំខែ ដោយរហ័ស ត្រឹមត្រូវ និងមានសុវត្ថិភាពខ្ពស់។")
    
    add_heading_2("១.១. អាសយដ្ឋានតំណភ្ជាប់ផ្លូវការ (Official URL)")
    add_body("លោកគ្រូ-អ្នកគ្រូអាចចូលប្រើប្រាស់ប្រព័ន្ធពីគ្រប់ទីកន្លែង និងគ្រប់ពេលវេលា តាមរយៈអាសយដ្ឋានគេហទំព័រផ្ទាល់៖")
    add_bullet("https://class.mitpukae.com (សម្រាប់ទំព័រទូទៅ និងការ Login)", "👉 Link ផ្លូវការ៖ ")
    add_bullet("https://class.mitpukae.com/login (សម្រាប់ចូលផ្ទាំង Login ផ្ទាល់)", "👉 Link Login គ្រូ៖ ")
    add_bullet("https://class.mitpukae.com/test (សម្រាប់សិស្សចូលប្រឡងអនឡាញ)", "👉 Link សិស្សប្រឡង៖ ")
    
    add_heading_2("១.២. តម្រូវការបច្ចេកទេស និងឧបករណ៍ប្រើប្រាស់")
    add_bullet("អាចដំណើរការបានលើគ្រប់ឧបករណ៍៖ កុំព្យូទ័រលើតុ (Desktop), កុំព្យូទ័រយួរដៃ (Laptop), ថេប្លេត (iPad/Tablet), និងទូរស័ព្ទដៃឆ្លាតវៃ (Smartphone Android / iOS)។")
    add_bullet("កម្មវិធីរុករក (Web Browser) ដែលណែនាំ៖ Google Chrome, Microsoft Edge, Safari, ឬ Mozilla Firefox (កំណែទម្រង់ចុងក្រោយ)។")
    add_bullet("តម្រូវឱ្យមានការតភ្ជាប់បណ្តាញអ៊ីនធឺណិត (Wi-Fi ឬ 4G/5G) ដើម្បីរក្សាទុកទិន្នន័យលើពពក (Cloud Server) ភ្លាមៗ។")

    add_callout("ចំណុចពិសេសនៃប្រព័ន្ធ", "ទិន្នន័យទាំងអស់របស់លោកគ្រូ-អ្នកគ្រូត្រូវបានការពារដោយប្រព័ន្ធសុវត្ថិភាព Cloud Database ដោយមានការបម្រុងទុក (Backup) ស្វ័យប្រវត្តិ។ គ្រូនីមួយៗមានគណនីផ្ទាល់ខ្លួនដាច់ដោយឡែក មិនប៉ះពាល់ដល់ថ្នាក់រៀនរបស់គ្រូដទៃឡើយ។")

    # ================= CHAPTER 2 =================
    add_heading_1("ជំពូកទី ២៖ របៀបចូលប្រើប្រាស់ប្រព័ន្ធ (Login) និងការគ្រប់គ្រងគណនី")
    add_body("ដើម្បីធានាសុវត្ថិភាពទិន្នន័យពិន្ទុ និងវត្តមានរបស់សិស្ស មានតែលោកគ្រូ-អ្នកគ្រូដែលមានគណនីដែលត្រូវបានផ្តល់ជូនដោយសាលាប៉ុណ្ណោះ ទើបអាចចូលប្រើប្រាស់មុខងារបង្រៀនបាន។")

    add_heading_2("២.១. ជំហានក្នុងការ Login ចូលប្រព័ន្ធ")
    add_bullet("បើកកម្មវិធី Browser (Chrome, Edge ឬ Safari) រួចវាយបញ្ចូលអាសយដ្ឋាន៖ https://class.mitpukae.com/login", "ជំហានទី ១៖ ")
    add_bullet("ប្រព័ន្ធនឹងបង្ហាញផ្ទាំង «ចូលប្រើប្រព័ន្ធគ្រប់គ្រង» ពណ៌សស្អាតនៅចំកណ្តាលអេក្រង់។", "ជំហានទី ២៖ ")
    add_bullet("បញ្ចូល ឈ្មោះអ្នកប្រើប្រាស់ (Username) របស់អ្នក (ឧទាហរណ៍៖ samkimchheng ឬ teacher1001)។", "ជំហានទី ៣៖ ")
    add_bullet("បញ្ចូល លេខសម្ងាត់ (Password) របស់អ្នក។", "ជំហានទី ៤៖ ")
    add_bullet("អ្នកអាចចុចលើរូបសញ្ញា ភ្នែក 👁️ នៅខាងស្តាំប្រអប់លេខសម្ងាត់ ដើម្បីផ្ទៀងផ្ទាត់លេខកូដដែលបានវាយបញ្ចូលឱ្យបានត្រឹមត្រូវ។", "ជំហានទី ៥៖ ")
    add_bullet("ចុចលើប៊ូតុងពណ៌ខៀវ «ចូលប្រព័ន្ធ (Login)» ដើម្បីបញ្ចប់។", "ជំហានទី ៦៖ ")

    add_heading_2("២.២. មុខងារចងចាំគណនីស្វ័យប្រវត្តិ (Remember Session)")
    add_body("ប្រព័ន្ធមានមុខងារចងចាំការចូលប្រើប្រាស់ (Persistent Auto-login Cookie) រយៈពេលរហូតដល់ ៣០ ថ្ងៃ។ នៅពេលលោកគ្រូ-អ្នកគ្រូបាន Login ម្តងរួចហើយ លើកក្រោយបើកកម្មវិធី Browser ឡើងវិញ លោកគ្រូ-អ្នកគ្រូអាចចូលទៅកាន់ផ្ទាំងគ្រប់គ្រងបានភ្លាមៗ ដោយមិនបាច់វាយលេខសម្ងាត់ម្តងទៀតឡើយ។")

    add_heading_2("២.៣. ការដោះស្រាយបញ្ហាក្នុងការ Login")
    add_bullet("សារ «ឈ្មោះអ្នកប្រើប្រាស់ ឬលេខសម្ងាត់មិនត្រឹមត្រូវ!»៖ សូមពិនិត្យមើលអក្សរតូច-ធំ (Caps Lock) ឬដកឃ្លាដែលច្រឡំវាយលើក្តារចុច។", "ករណីទី ១៖ ")
    add_bullet("សារ «គណនីនេះត្រូវបានផ្អាក ឬបិទសិទ្ធិជាបណ្តោះអាសន្ន! សូមទាក់ទង Admin»៖ មានន័យថាគណនីត្រូវបានផ្អាកដោយ Admin។ សូមទាក់ទង Admin ដើម្បីបើកសិទ្ធិឡើងវិញ។", "ករណីទី ២៖ ")
    add_bullet("ភ្លេចលេខសម្ងាត់៖ សូមចុចលើតំណភ្ជាប់ Telegram ជំនួយបច្ចេកទេសនៅផ្នែកខាងក្រោមនៃផ្ទាំង Login ដើម្បីស្នើសុំកំណត់លេខសម្ងាត់ថ្មី។", "ករណីទី ៣៖ ")

    # ================= CHAPTER 3 =================
    add_heading_1("ជំពូកទី ៣៖ ការស្វែងយល់ពីផ្ទាំងទំព័រដើម (Dashboard Overview) និងម៉ឺនុយ")
    add_body("បន្ទាប់ពី Login បានជោគជ័យ ប្រព័ន្ធនឹងនាំលោកគ្រូ-អ្នកគ្រូមកកាន់ទំព័រដើម (Dashboard) ភ្លាមៗ។")

    add_heading_2("៣.១. ផ្ទាំងស្ថិតិសង្ខេប (Summary Cards)")
    add_body("នៅផ្នែកខាងលើនៃទំព័រដើម មានប្រអប់ព័ត៌មានសំខាន់ៗចំនួន ៤៖")
    add_bullet("បង្ហាញចំនួនសិស្សទាំងអស់ដែលស្ថិតក្រោមការគ្រប់គ្រងរបស់គ្រូ។", "១. ចំនួនសិស្សសរុប (Total Students)៖ ")
    add_bullet("បង្ហាញចំនួនសិស្សជានារី ដើម្បីងាយស្រួលតាមដានសមភាពយេនឌ័រ។", "២. សិស្សស្រីសរុប (Total Female)៖ ")
    add_bullet("បង្ហាញចំនួនសិស្សដែលបានចូលរៀនជាក់ស្តែងក្នុងថ្ងៃនេះ។", "៣. វត្តមានថ្ងៃនេះ (Present Today)៖ ")
    add_bullet("បង្ហាញចំនួនសិស្សស្រីដែលមានវត្តមានរៀនថ្ងៃនេះ។", "៤. សិស្សស្រីមានវត្តមាន (Female Present)៖ ")

    add_heading_2("៣.២. ការស្វែងយល់ពីរចនាសម្ព័ន្ធម៉ឺនុយចំហៀង (Sidebar Navigation Menu)")
    add_body("នៅផ្នែកខាងឆ្វេងដៃនៃអេក្រង់ មានម៉ឺនុយរៀបចំតាមលំដាប់លំដោយយ៉ាងងាយស្រួល៖")
    add_bullet("ទំព័រដើម (Dashboard) - មើលស្ថិតិសង្ខេបទូទៅ។")
    add_bullet("បញ្ជីថ្នាក់រៀនខ្ញុំ (Classrooms) - គ្រប់គ្រងថ្នាក់រៀនផ្ទាល់ខ្លួន។")
    add_bullet("បញ្ជីឈ្មោះសិស្ស (Students) - មើល និងបន្ថែមឈ្មោះសិស្ស។")
    add_bullet("បញ្ជីវត្តមាន (Attendance) - កត់ត្រាវត្តមានសិស្សប្រចាំថ្ងៃ។")
    add_bullet("បញ្ជីពិន្ទុ (Scores) - បញ្ចូល និងគណនាពិន្ទុសិស្ស។")
    add_bullet("ស្ថិតិសិស្ស (Statistics) - មើលក្រាហ្វិក និងការវិភាគនិទ្ទេស។")
    add_bullet("វិញ្ញាសាប្រឡង (Exams) - បង្កើត និងគ្រប់គ្រងវិញ្ញាសាប្រឡង។")
    add_bullet("មើលមេរៀន (Lessons) - បង្ហោះ និងអានមេរៀន។")
    add_bullet("កាលវិភាគ (Schedule) - មើល និងរៀបចំកាលវិភាគបង្រៀន។")
    add_bullet("របាយការណ៍ (Report) - បោះពុម្ពតារាងចំណាត់ថ្នាក់ និងលទ្ធផលសរុប។")
    add_bullet("✈️ ទាក់ទងខ្ញុំ (Telegram) - ចុចឆាតផ្ទាល់ទៅកាន់អ្នកបច្ចេកទេស។")
    add_bullet("ចាកចេញ (Logout 🚪) - ចាកចេញពីគណនីប្រកបដោយសុវត្ថិភាព។")

    # ================= CHAPTER 4 =================
    add_heading_1("ជំពូកទី ៤៖ ការគ្រប់គ្រងបញ្ជីថ្នាក់រៀនផ្ទាល់ខ្លួន (Classroom Management)")
    add_body("លោកគ្រូ-អ្នកគ្រូអាចបង្រៀនច្រើនថ្នាក់ក្នុងពេលតែមួយ (ឧទាហរណ៍៖ ថ្នាក់ ១០A, ១០B, ១១A, ១២A...)។ មុខងារនេះជួយឱ្យគ្រូបែងចែកសិស្សតាមថ្នាក់នីមួយៗបានយ៉ាងច្បាស់លាស់។")

    add_heading_2("៤.១. ការបន្ថែមថ្នាក់រៀនថ្មី")
    add_bullet("ចុចលើម៉ឺនុយ «បញ្ជីថ្នាក់រៀនខ្ញុំ» នៅលើ Sidebar ខាងឆ្វេង។", "ជំហានទី ១៖ ")
    add_bullet("ចុចលើប៊ូតុងពណ៌ខៀវ «+ បន្ថែមថ្នាក់រៀនថ្មី»។", "ជំហានទី ២៖ ")
    add_bullet("បញ្ចូល ឈ្មោះថ្នាក់រៀន (ឧទាហរណ៍៖ ថ្នាក់ទី១០ A ឬ Grade 10-A)។", "ជំហានទី ៣៖ ")
    add_bullet("បញ្ចូល បន្ទប់សិក្សា ឬវេនសិក្សា (បើមាន)។", "ជំហានទី ៤៖ ")
    add_bullet("ចុចលើប៊ូតុង «រក្សាទុក (Save)»។ ថ្នាក់រៀនថ្មីនឹងបង្ហាញភ្លាមៗក្នុងបញ្ជី។", "ជំហានទី ៥៖ ")

    add_heading_2("៤.២. ការកែសម្រួល ឬលុបថ្នាក់រៀន")
    add_bullet("ការកែប្រែ (Edit)៖ ចុចលើប៊ូតុង «កែប្រែ ✏️» នៅជួរថ្នាក់រៀនដែលចង់ប្តូរឈ្មោះ រួចចុច Save។")
    add_bullet("ការលុប (Delete)៖ ចុចលើប៊ូតុង «លុប 🗑️» ក្នុងករណីបញ្ចប់ការបង្រៀនថ្នាក់នោះ។ (ប្រព័ន្ធនឹងបញ្ជាក់ការយល់ព្រមមុននឹងលុប)។")

    # ================= CHAPTER 5 =================
    add_heading_1("ជំពូកទី ៥៖ ការគ្រប់គ្រងបញ្ជីឈ្មោះសិស្ស (Students Management)")
    add_body("ការគ្រប់គ្រងទិន្នន័យសិស្សគឺជាមូលដ្ឋានគ្រឹះក្នុងការស្រង់វត្តមាន និងបញ្ចូលពិន្ទុ។")

    add_heading_2("៥.១. របៀបបន្ថែមសិស្សថ្មីចូលក្នុងថ្នាក់")
    add_bullet("ចុចលើម៉ឺនុយ «បញ្ជីឈ្មោះសិស្ស» -> រួចចុចប៊ូតុង «+ បន្ថែមសិស្សថ្មី»។", "ជំហានទី ១៖ ")
    add_bullet("លេខសម្គាល់សិស្ស (Student ID / Code)៖ ឧទាហរណ៍ STU-001 (ជាលេខកូដដែលសិស្សប្រើសម្រាប់ចូលប្រឡងអនឡាញ)។", "ជំហានទី ២ (ព័ត៌មានសិស្ស)៖ ")
    add_bullet("គោត្តនាម និងនាម (FullName)៖ វាយបញ្ចូលឈ្មោះជាភាសាខ្មែរ (ឧទាហរណ៍៖ សុខ សុភា)។")
    add_bullet("ភេទ (Gender)៖ ជ្រើសរើស ប្រុស ឬ ស្រី។")
    add_bullet("ថ្ងៃខែឆ្នាំកំណើត (Date of Birth)៖ ជ្រើសរើសតាមប្រតិទិន។")
    add_bullet("ថ្នាក់រៀន (Classroom)៖ ជ្រើសរើសថ្នាក់ដែលសិស្សនោះកំពុងរៀន។")
    add_bullet("លេខទូរស័ព្ទអាណាព្យាបាល (Parent Contact)៖ សម្រាប់ទាក់ទងករណីចាំបាច់។")
    add_bullet("ចុចប៊ូតុង «រក្សាទុកសិស្ស (Save Student)»។", "ជំហានទី ៣៖ ")

    add_heading_2("៥.២. ការស្វែងរក និងច្រោះមើលឈ្មោះសិស្ស")
    add_body("លោកគ្រូ-អ្នកគ្រូអាចប្រើប្រអប់ជ្រើសរើសថ្នាក់រៀន (Filter by Class) ដើម្បីបង្ហាញតែឈ្មោះសិស្សក្នុងថ្នាក់ដែលចង់មើលបានយ៉ាងរហ័ស ដោយមិនបាច់រកមើលក្នុងចំណោមសិស្សទាំងអស់ឡើយ។")

    # ================= CHAPTER 6 =================
    add_heading_1("ជំពូកទី ៦៖ ការកត់ត្រាវត្តមានសិស្សប្រចាំថ្ងៃ (Daily Attendance Tracking)")
    add_body("ការកត់ត្រាវត្តមានត្រូវបានរៀបចំឡើងជាទម្រង់ឌីជីថល ដោយគ្រាន់តែចុច Check លើស្ថានភាពវត្តមានរបស់សិស្សម្នាក់ៗ។")

    add_heading_2("៦.១. និមិត្តសញ្ញាវត្តមាន")
    add_bullet("វត្តមាន (Present - ពណ៌បៃតង)៖ សិស្សបានចូលរៀនពេញលេញ។", "✔️ P (Present)៖ ")
    add_bullet("អវត្តមានឥតច្បាប់ (Absent - ពណ៌ក្រហម)៖ សិស្សឈប់រៀនដោយគ្មានការសុំច្បាប់។", "❌ A (Absent)៖ ")
    add_bullet("អវត្តមានមានច្បាប់ (Permission/Leave - ពណ៌លឿង)៖ សិស្សមានលិខិតសុំច្បាប់ ឬអាណាព្យាបាលបានជូនដំណឹង។", "⚠️ L (Permission)៖ ")

    add_heading_2("៦.២. ជំហានកត់ត្រាវត្តមាន")
    add_bullet("ចូលទៅកាន់ម៉ឺនុយ «បញ្ជីវត្តមាន (Attendance)»។", "ជំហានទី ១៖ ")
    add_bullet("ជ្រើសរើស ថ្នាក់រៀន (ឧទាហរណ៍៖ ថ្នាក់ទី១០ A)។", "ជំហានទី ២៖ ")
    add_bullet("ជ្រើសរើស កាលបរិច្ឆេទ (ថ្ងៃ ខែ ឆ្នាំ ដែលត្រូវកត់ត្រា)។", "ជំហានទី ៣៖ ")
    add_bullet("ប្រព័ន្ធនឹងរាយនាមសិស្សទាំងអស់ក្នុងថ្នាក់នោះដោយស្វ័យប្រវត្តិ។ លោកគ្រូ-អ្នកគ្រូគ្រាន់តែចុចជ្រើសរើសលើជម្រើស P, A ឬ L សម្រាប់សិស្សម្នាក់ៗ។", "ជំហានទី ៤៖ ")
    add_bullet("ចុចលើប៊ូតុង «រក្សាទុកវត្តមាន (Save Attendance)»។", "ជំហានទី ៥៖ ")

    add_callout("គន្លឹះរហ័សក្នុងការកត់វត្តមាន", "ប្រព័ន្ធបានកំណត់លំនាំដើម (Default) ឱ្យសិស្សទាំងអស់មាន «វត្តមាន (P)» ជាស្រេច។ ដូច្នេះ លោកគ្រូ-អ្នកគ្រូគ្រាន់តែចុចប្តូរតែលើសិស្សណាដែលអវត្តមាន (A) ឬមានច្បាប់ (L) ប៉ុណ្ណោះ ដែលចំណាយពេលមិនដល់ ១ នាទីឡើយក្នុងមួយថ្នាក់!")

    # ================= CHAPTER 7 =================
    add_heading_1("ជំពូកទី ៧៖ ការបញ្ចូល និងគ្រប់គ្រងពិន្ទុសិស្ស (Scores & Grading Management)")
    add_body("ប្រព័ន្ធគាំទ្រការគណនាពិន្ទុចម្រុះប្រកបដោយស្តង់ដារគរុកោសល្យខ្ពស់ ដោយរួមបញ្ចូលគ្នារវាងពិន្ទុតេស្តប្រចាំខែ ពិន្ទុវត្តមាន និងពិន្ទុកិច្ចការផ្ទះ។")

    add_heading_2("៧.១. ការកំណត់លក្ខខណ្ឌពិន្ទុ (Score Rules)")
    add_body("មុនពេលបញ្ចូលពិន្ទុ លោកគ្រូ-អ្នកគ្រូអាចកំណត់ទម្ងន់ពិន្ទុតាមមុខវិជ្ជានីមួយៗបាន៖")
    add_bullet("ពិន្ទុតេស្តប្រចាំខែ (Monthly Test Max Score)៖ ឧទាហរណ៍ ៧០ ពិន្ទុ។")
    add_bullet("ពិន្ទុវត្តមាន (Attendance Max Score)៖ ឧទាហរណ៍ ១៥ ពិន្ទុ។")
    add_bullet("ពិន្ទុកិច្ចការផ្ទះ/ស្វ័យសិក្សា (Assignment Max Score)៖ ឧទាហរណ៍ ១៥ ពិន្ទុ។")
    add_bullet("ពិន្ទុសរុបអតិបរមា = ១០០ ពិន្ទុ។")

    add_heading_2("៧.២. តារាងកម្រិតនិទ្ទេសស្វ័យប្រវត្តិ (Grade Scale)")
    add_body("ប្រព័ន្ធនឹងធ្វើការបូកសរុបពិន្ទុ និងគណនានិទ្ទេសជូនសិស្សដោយស្វ័យប្រវត្តិតាមរូបមន្តស្តង់ដារជាតិ៖")

    tbl_grade = doc.add_table(rows=1, cols=4)
    tbl_grade.alignment = WD_TABLE_ALIGNMENT.CENTER
    tbl_grade.columns[0].width = Inches(1.3)
    tbl_grade.columns[1].width = Inches(1.8)
    tbl_grade.columns[2].width = Inches(1.8)
    tbl_grade.columns[3].width = Inches(1.6)

    g_hdrs = ["និទ្ទេស (Grade)", "ភាគរយពិន្ទុ (%)", "ពិន្ទុជាក់ស្តែង (លើ១០០)", "ការវាយតម្លៃ"]
    for i, h in enumerate(g_hdrs):
        c = tbl_grade.rows[0].cells[i]
        c.text = h
        set_cell_background(c, "0284C7")
        set_cell_margins(c, top=80, bottom=80, left=100, right=100)
        for r in c.paragraphs[0].runs:
            format_run(r, font_name="Khmer OS Battambang", size_pt=10, bold=True, color_rgb=(255, 255, 255))

    grades_data = [
        ("A", "៩០% - ១០០%", "៩០ - ១០០", "ល្អប្រសើរណាស់ (Excellent)"),
        ("B", "៨០% - ៨៩%", "៨០ - ៨៩", "ល្អណាស់ (Very Good)"),
        ("C", "៧០% - ៧៩%", "៧០ - ៧៩", "ល្អ (Good)"),
        ("D", "៦០% - ៦៩%", "៦០ - ៦៩", "ល្អបង្គួរ (Fair)"),
        ("E", "៥០% - ៥៩%", "៥០ - ៥៩", "មធ្យម / ជាប់ (Pass)"),
        ("F", "ក្រោម ៥០%", "ក្រោម ៥០", "ធ្លាក់ (Fail)")
    ]

    for row in grades_data:
        r_cells = tbl_grade.add_row().cells
        for i in range(4):
            r_cells[i].text = row[i]
            set_cell_background(r_cells[i], "F1F5F9" if row[0] in ["A", "B"] else ("FEF2F2" if row[0] == "F" else "FFFFFF"))
            set_cell_margins(r_cells[i], top=60, bottom=60, left=100, right=100)
            for r in r_cells[i].paragraphs[0].runs:
                format_run(r, font_name="Khmer OS Battambang", size_pt=10, bold=(i == 0), color_rgb=(239, 68, 68) if row[0] == "F" else (30, 41, 59))

    # ================= CHAPTER 8 =================
    add_heading_1("ជំពូកទី ៨៖ ស្ថិតិ និងការវិភាគទិន្នន័យសិក្សា (Student Statistics & Analytics)")
    add_body("ទំព័រ «ស្ថិតិសិស្ស (Statistics)» ជួយឱ្យលោកគ្រូ-អ្នកគ្រូ និងគណៈគ្រប់គ្រងអាចវាយតម្លៃគុណភាពនៃការបង្រៀន និងការរៀនបានយ៉ាងស៊ីជម្រៅតាមរយៈរូបភាព និងភាគរយជាក់ស្តែង។")
    add_bullet("ភាគរយសិស្សជាប់ និងធ្លាក់៖ បង្ហាញជាក្រាហ្វពណ៌បៃតង (ជាប់) និងពណ៌ក្រហម (ធ្លាក់)។")
    add_bullet("ការបែងចែកតាមនិទ្ទេស A, B, C, D, E, F៖ ជួយឱ្យដឹងថាតើសិស្សពូកែមានចំនួនប៉ុន្មាននាក់ និងសិស្សខ្សោយដែលត្រូវជួយបំប៉នបន្ថែមមានចំនួនប៉ុន្មាននាក់។")
    add_bullet("ស្ថិតិយេនឌ័រ៖ បង្ហាញពីលទ្ធផលសិក្សារបស់សិស្សស្រីប្រៀបធៀបជាមួយសិស្សប្រុស។")

    # ================= CHAPTER 9 =================
    add_heading_1("ជំពូកទី ៩៖ ការគ្រប់គ្រងវិញ្ញាសា និងការប្រឡងអនឡាញ (Online Exams)")
    add_body("ប្រព័ន្ធបានបំពាក់ម៉ូឌុលប្រឡងអនឡាញ ដែលអនុញ្ញាតឱ្យលោកគ្រូ-អ្នកគ្រូអាចបង្កើតវិញ្ញាសា និងឱ្យសិស្សធ្វើតេស្តតាមទូរស័ព្ទ ឬកុំព្យូទ័របានដោយស្វ័យប្រវត្តិ។")

    add_heading_2("៩.១. របៀបដែលគ្រូបង្កើតវិញ្ញាសា")
    add_bullet("ចូលទៅកាន់ម៉ឺនុយ «វិញ្ញាសាប្រឡង (Exams)»។", "ជំហានទី ១៖ ")
    add_bullet("ជ្រើសរើស ខែប្រឡង (ឧទាហរណ៍៖ ខែវិច្ឆិកា ២០២៦) និង ថ្នាក់រៀន។", "ជំហានទី ២៖ ")
    add_bullet("បញ្ចូលសំណួរ និងជម្រើសចម្លើយ (A, B, C, D) រួមទាំងកំណត់ចម្លើយដែលត្រឹមត្រូវ។", "ជំហានទី ៣៖ ")
    add_bullet("ចុច «រក្សាទុកវិញ្ញាសា»។ វិញ្ញាសានឹងបើកឱ្យសិស្សក្នុងថ្នាក់នោះចូលប្រឡងបានភ្លាមៗ។", "ជំហានទី ៤៖ ")

    add_heading_2("៩.២. របៀបដែលសិស្សចូលប្រឡង (Student Exam Portal)")
    add_bullet("សិស្សបើកទូរស័ព្ទដៃ ឬកុំព្យូទ័រ ហើយចូលទៅកាន់ Link៖ https://class.mitpukae.com/test", "ជំហានទី ១៖ ")
    add_bullet("សិស្សបញ្ចូល លេខកូដសិស្ស របស់ខ្លួន (ឧទាហរណ៍៖ STU-001) រួចចុច «ចូលប្រឡង»។", "ជំហានទី ២៖ ")
    add_bullet("សិស្សអានសំណួរ និងចុចជ្រើសរើសចម្លើយរហូតដល់ចប់ រួចចុច «បញ្ជូនចម្លើយ (Submit)»។", "ជំហានទី ៣៖ ")
    add_bullet("ប្រព័ន្ធនឹងកែពិន្ទុ និងបង្ហាញលទ្ធផលជូនសិស្សភ្លាមៗ ហើយបញ្ជូនទិន្នន័យពិន្ទុនេះចូលទៅកាន់បញ្ជីពិន្ទុរបស់គ្រូដោយស្វ័យប្រវត្តិ។", "ជំហានទី ៤៖ ")

    # ================= CHAPTER 10 =================
    add_heading_1("ជំពូកទី ១០៖ ការគ្រប់គ្រង និងចែករំលែកមេរៀន (Lesson Management)")
    add_body("លោកគ្រូ-អ្នកគ្រូអាចបង្ហោះមេរៀន ស្លាយបង្រៀន (PowerPoint), ឯកសារ PDF ឬតំណភ្ជាប់វីដេអូបង្រៀន ដើម្បីឱ្យសិស្សអាចចូលមើល និងទាញយកទៅស្វ័យសិក្សានៅផ្ទះបាន។")
    add_bullet("ការបង្ហោះមេរៀន៖ ចុច «មើលមេរៀន» -> បញ្ចូលចំណងជើងមេរៀន មាតិកា និង Upload ឯកសារភ្ជាប់។")
    add_bullet("ការកំណត់សិទ្ធិ៖ អាចជ្រើសរើសចែករំលែកសម្រាប់ «គ្រប់ថ្នាក់» ឬសម្រាប់តែ «ថ្នាក់ជាក់លាក់ណាមួយ»។")

    # ================= CHAPTER 11 =================
    add_heading_1("ជំពូកទី ១១៖ ការរៀបចំកាលវិភាគបង្រៀន (Teaching Schedule)")
    add_body("កាលវិភាគត្រូវបានរៀបចំឡើងជាតារាងប្រចាំសប្តាហ៍ (ពីថ្ងៃចន្ទ ដល់ ថ្ងៃសៅរ៍/អាទិត្យ)៖")
    add_bullet("បង្ហាញម៉ោងបង្រៀនច្បាស់លាស់ (ម៉ោងពេលព្រឹក ពេលរសៀល ឬពេលល្ងាច)។")
    add_bullet("បង្ហាញមុខវិជ្ជា និងបន្ទប់សិក្សានីមួយៗ ដើម្បីកុំឱ្យមានការជាន់ម៉ោងគ្នារវាងគ្រូនិងគ្រូ។")

    # ================= CHAPTER 12 =================
    add_heading_1("ជំពូកទី ១២៖ ការទាញយក និងបោះពុម្ពរបាយការណ៍សរុប (Reports & Printing)")
    add_body("នៅដំណាច់ខែនីមួយៗ លោកគ្រូ-អ្នកគ្រូត្រូវចេញរបាយការណ៍សរុបជូនគណៈគ្រប់គ្រង និងអាណាព្យាបាលសិស្ស។ ទំព័រ «របាយការណ៍ (Report)» ត្រូវបានរចនាឡើងយ៉ាងពិសេសសម្រាប់ការងារនេះ។")

    add_heading_2("១២.១. មាតិកាក្នុងតារាងរបាយការណ៍សរុប")
    add_bullet("ចំណាត់ថ្នាក់សិស្ស (Rank)៖ ចំណាត់ថ្នាក់លេខ ១, ២, ៣... គណនាដោយស្វ័យប្រវត្តិតាមពិន្ទុសរុបខ្ពស់ទៅទាប។")
    add_bullet("ព័ត៌មានសិស្ស៖ លេខសម្គាល់, គោត្តនាម-នាម, ភេទ។")
    add_bullet("លទ្ធផលសិក្សា៖ ពិន្ទុសរុប និងនិទ្ទេសរួម (Grade A-F)។")
    add_bullet("កំណត់ត្រាវត្តមាន៖ ចំនួនថ្ងៃមានវត្តមាន និងចំនួនថ្ងៃអវត្តមានសរុបក្នុងខែនោះ។")

    add_heading_2("១២.២. ការបោះពុម្ពរបាយការណ៍ (Print Report) ជាក្រដាស A4 ឬ PDF")
    add_bullet("ជ្រើសរើស ថ្នាក់រៀន និង ខែ ដែលចង់ចេញរបាយការណ៍។", "ជំហានទី ១៖ ")
    add_bullet("ពិនិត្យមើលតារាងទិន្នន័យឱ្យបានរួចរាល់។", "ជំហានទី ២៖ ")
    add_bullet("ចុចលើប៊ូតុងពណ៌ខៀវ «បោះពុម្ពរបាយការណ៍ (Print 🖨️)» នៅផ្នែកខាងលើ។", "ជំហានទី ៣៖ ")
    add_bullet("ផ្ទាំង Print របស់កុំព្យូទ័រនឹងលោតឡើង៖", "ជំហានទី ៤៖ ")
    add_bullet("ប្រសិនបើចង់បោះពុម្ពជាក្រដាស៖ ជ្រើសរើសម៉ាស៊ីនព្រីន (Printer) របស់អ្នក រួចចុច Print។")
    add_bullet("ប្រសិនបើចង់រក្សាទុកជាឯកសារ PDF៖ នៅកន្លែង Destination សូមជ្រើសរើស «Save as PDF» រួចចុច Save។")

    add_callout("ភាពស្អាតនៃទម្រង់បោះពុម្ព (Clean Print Layout)", "ប្រព័ន្ធបានកំណត់កូដពិសេស (.no-print)។ នៅពេលលោកគ្រូ-អ្នកគ្រូចុចបោះពុម្ព គឺមានតែតារាងរបាយការណ៍ផ្លូវការប៉ុណ្ណោះដែលបង្ហាញនៅលើក្រដាស A4 ចំណែកឯម៉ឺនុយ Sidebar, ប៊ូតុងចុចផ្សេងៗ និងផ្ទាំង Telegram នឹងត្រូវលាក់ស្វ័យប្រវត្តិ ធានាបាននូវទម្រង់របាយការណ៍ផ្លូវការដ៏ស្រស់ស្អាត ១០០%!")

    # ================= CHAPTER 13 =================
    add_heading_1("ជំពូកទី ១៣៖ ផ្នែកទំនាក់ទំនង និងជំនួយបច្ចេកទេសតាម Telegram")
    add_body("ដើម្បីផ្តល់ភាពកក់ក្តៅ និងភាពងាយស្រួលបំផុតដល់លោកគ្រូ-អ្នកគ្រូ ប្រព័ន្ធបានភ្ជាប់ទំនាក់ទំនងផ្ទាល់ជាមួយអ្នកគ្រប់គ្រង និងអ្នកអភិវឌ្ឍន៍ប្រព័ន្ធតាមរយៈ Telegram។")

    add_heading_2("១៣.១. ព័ត៌មានទំនាក់ទំនងផ្ទាល់")
    add_bullet("លោកគ្រូ សំ គឹមឆេង (Sam Kimchheng)", "👤 អ្នកគ្រប់គ្រងបច្ចេកទេស៖ ")
    add_bullet("https://t.me/Kimchheng12", "✈️ តំណភ្ជាប់ Telegram ផ្ទាល់៖ ")
    add_bullet("@Kimchheng12", "💬 ឈ្មោះគណនី Telegram (Username)៖ ")

    add_heading_2("១៣.២. ទីតាំងប៊ូតុង Telegram លើប្រព័ន្ធ")
    add_bullet("នៅលើម៉ឺនុយចំហៀង (Sidebar)៖ មានប៊ូតុង «✈️ ទាក់ទងខ្ញុំ (Telegram)» នៅខាងក្រោម «របាយការណ៍» លើគ្រប់ទំព័រទាំងអស់។")
    add_bullet("នៅខាងក្រោមតារាងរបាយការណ៍ (Report Page)៖ មានផ្ទាំងពណ៌ស គែមពណ៌ខៀវ «ផ្នែកទំនាក់ទំនង និងជំនួយផ្ទាល់ (Telegram Support)» ជាមួយប៊ូតុង «✈️ ផ្ញើសារតាម Telegram (@Kimchheng12)»។")
    add_bullet("នៅលើផ្ទាំង Login និង Admin៖ មានតំណភ្ជាប់ទាក់ទងផ្ទាល់ផងដែរ។")

    # ================= CHAPTER 14 =================
    add_heading_1("ជំពូកទី ១៤៖ សុវត្ថិភាពគណនី និងគន្លឹះដោះស្រាយបញ្ហាបច្ចេកទេស")
    add_heading_2("១៤.១. គន្លឹះសុវត្ថិភាពទិន្នន័យ")
    add_bullet("កុំចែករំលែកលេខសម្ងាត់គណនីគ្រូរបស់អ្នកទៅកាន់អ្នកដទៃ ឬសិស្ស។")
    add_bullet("ប្រសិនបើលោកគ្រូ-អ្នកគ្រូប្រើប្រាស់កុំព្យូទ័ររួម (Computer Lab សាលា ឬហាងអ៊ីនធឺណិត) សូមចុច «ចាកចេញ (Logout 🚪)» រាល់ពេលប្រើប្រាស់រួច។")

    add_heading_2("១៤.២. របៀបដោះស្រាយនៅពេលប្រព័ន្ធមិនទាន់បង្ហាញទិន្នន័យថ្មី")
    add_bullet("ចុចបញ្ជា «Ctrl + F5» លើកុំព្យូទ័រ (ឬចុច Reload លើទូរស័ព្ទដៃ) ដើម្បីឱ្យ Browser ទាញយកទិន្នន័យថ្មីស្រឡាងពី Server។")
    add_bullet("ប្រសិនបើមានបញ្ហាបច្ចេកទេសណាមួយ សូម Screenshot អេក្រង់រួចផ្ញើមកកាន់ Telegram @Kimchheng12 ដើម្បីទទួលបានការជួយសម្រួលភ្លាមៗ។")

    # Save to documents
    paths = [
        r"D:\NUBB DOCUMENTARY\Class Management System2027\សៀវភៅណែនាំ_ការប្រើប្រាស់ប្រព័ន្ធគ្រប់គ្រងថ្នាក់រៀន_២០២៧.docx",
        r"D:\NUBB DOCUMENTARY\Class Management System2027\សៀវភៅណែនាំ_ការប្រើប្រាស់ប្រព័ន្ធគ្រប់គ្រងថ្នាក់រៀន_២០២៧.doc",
        r"D:\NUBB DOCUMENTARY\សៀវភៅណែនាំ_ការប្រើប្រាស់ប្រព័ន្ធគ្រប់គ្រងថ្នាក់រៀន_២០២៧.docx",
        r"D:\NUBB DOCUMENTARY\សៀវភៅណែនាំ_ការប្រើប្រាស់ប្រព័ន្ធគ្រប់គ្រងថ្នាក់រៀន_២០២៧.doc",
        r"D:\NUBB DOCUMENTARY\Class Management System2027\Class_Management_System_Teacher_Guide_2027.docx",
        r"D:\NUBB DOCUMENTARY\Class_Management_System_Teacher_Guide_2027.docx",
        r"D:\NUBB DOCUMENTARY\Class Management System2027\src\main\resources\static\docs\teacher-guide.docx",
        r"D:\NUBB DOCUMENTARY\Class Management System2027\src\main\resources\static\docs\teacher-guide.doc",
    ]
    
    os.makedirs(r"D:\NUBB DOCUMENTARY\Class Management System2027\src\main\resources\static\docs", exist_ok=True)

    for p in paths:
        doc.save(p)
        print("Saved to:", p.encode('ascii', 'replace').decode('ascii'))

if __name__ == "__main__":
    create_document()
