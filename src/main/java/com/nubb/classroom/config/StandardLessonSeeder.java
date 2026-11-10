package com.nubb.classroom.config;

import com.nubb.classroom.model.Account;
import com.nubb.classroom.model.Lesson;
import com.nubb.classroom.model.LessonTranscript;
import com.nubb.classroom.repository.AccountRepository;
import com.nubb.classroom.repository.LessonRepository;
import com.nubb.classroom.repository.LessonTranscriptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class StandardLessonSeeder {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private LessonTranscriptRepository lessonTranscriptRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Transactional
    public void seedLessonsForAllTeachers() {
        Set<Long> teacherIds = new LinkedHashSet<>();
        try {
            List<Account> teachers = accountRepository.findByRole("TEACHER");
            if (teachers != null) {
                for (Account t : teachers) {
                    if (t.getId() != null) teacherIds.add(t.getId());
                }
            }
        } catch (Exception ignored) {}

        teacherIds.add(97L); // samkimchheng
        teacherIds.add(33L); // Teacher1001 / seungsomuny
        teacherIds.add(98L); // demo

        for (Long accId : teacherIds) {
            seedLessonsForTeacher(accId);
        }
    }

    @Transactional
    public void seedLessonsForTeacher(Long accountId) {
        if (accountId == null) return;
        List<Lesson> existing = lessonRepository.findByAccountIdOrderByIdAsc(accountId);

        // ----------------------------------------------------
        // GRADE 10 LESSONS (Units 1 to 5 with full Audio and Transcripts)
        // ----------------------------------------------------
        seedOrUpdateGrade10Lessons(accountId, existing);

        // ----------------------------------------------------
        // GRADE 11 LESSONS (Units 1 to 5 for 11F, 11G)
        // ----------------------------------------------------
        seedOrUpdateGrade11Lessons(accountId, existing);
    }

    private void seedOrUpdateGrade10Lessons(Long accountId, List<Lesson> existing) {
        String classG10 = "១០E, ១០F, ១០G";

        // Unit 1
        Optional<Lesson> optU1 = existing.stream().filter(l -> l.getTitle() != null && l.getTitle().contains("Unit1") && !l.getTitle().contains("Grade 11")).findFirst();
        Lesson u1;
        String u1Audio = "/uploads/audio/1790611893123_a27f091e_EG10_UNIT_1.A_3.A__High_quality_.mp3";
        if (optU1.isPresent()) {
            u1 = optU1.get();
            boolean changed = false;
            if (u1.getAudioUrl() == null || u1.getAudioUrl().trim().isEmpty()) {
                u1.setAudioUrl(u1Audio);
                changed = true;
            }
            if (u1.getClassName() == null || !u1.getClassName().contains("១០")) {
                u1.setClassName(classG10);
                changed = true;
            }
            if (changed) u1 = lessonRepository.save(u1);
        } else {
            u1 = new Lesson(accountId, "Unit1 : MY PERSONAL INFORMATION", classG10, "ភាសាអង់គ្លេស",
                    "Unit1 Aims :  Hold a conversation where people greet  each other ,exchange information about name (with spelling) age, and telephone number",
                    "Unit1 គោលបំណង៖ ធ្វើការសន្ទនាដែលមនុស្សស្វាគមន៍គ្នាទៅវិញទៅមកផ្លាស់ប្តូរព័ត៌មានអំពីឈ្មោះ (ជាមួយនឹងអក្ខរាវិរុទ្ធ) អាយុ និងលេខទូរស័ព្ទ",
                    u1Audio);
            u1 = lessonRepository.save(u1);
        }
        if (lessonTranscriptRepository.findByLessonIdOrderByOrderIndexAsc(u1.getId()).isEmpty()) {
            seedTranscriptsUnit1(u1.getId());
        }

        // Unit 2
        Optional<Lesson> optU2 = existing.stream().filter(l -> l.getTitle() != null && l.getTitle().contains("Unit2") && !l.getTitle().contains("Grade 11")).findFirst();
        Lesson u2;
        String u2Audio = "/uploads/audio/1790694853083_61632581_EG10_UNIT_2.A_2.B__High_quality_.mp3";
        if (optU2.isPresent()) {
            u2 = optU2.get();
            boolean changed = false;
            if (u2.getAudioUrl() == null || u2.getAudioUrl().trim().isEmpty()) {
                u2.setAudioUrl(u2Audio);
                changed = true;
            }
            if (u2.getClassName() == null || !u2.getClassName().contains("១០")) {
                u2.setClassName(classG10);
                changed = true;
            }
            if (changed) u2 = lessonRepository.save(u2);
        } else {
            u2 = new Lesson(accountId, "Unit2:  COUNTRIES AND NATIONALITIES", classG10, "ភាសាអង់គ្លេស",
                    "Aims\r\nIdentify some countries and tell and ask other people where they are from and their nationalities\r\nForms\r\nWhere are you from?\r\nAre you from China? \r\nShe isn't Cambodian. \r\nThey're from Thailand.",
                    "គោលបំណង\r\nកំណត់អត្តសញ្ញាណប្រទេសមួយចំនួន ហើយប្រាប់ និងសួរមនុស្សផ្សេងទៀតថាតើពួកគេមកពីណា និងសញ្ជាតិរបស់ពួកគេ។\r\nទម្រង់\r\nតើអ្នកមកពីណា?\r\nតើអ្នកមកពីប្រទេសចិនទេ? \r\nនាងមិនមែនជាជនជាតិខ្មែរទេ។ \r\nពួកគេមកពីប្រទេសថៃ។",
                    u2Audio);
            u2 = lessonRepository.save(u2);
        }
        if (lessonTranscriptRepository.findByLessonIdOrderByOrderIndexAsc(u2.getId()).isEmpty()) {
            seedTranscriptsUnit2(u2.getId());
        }

        // Unit 3
        Optional<Lesson> optU3 = existing.stream().filter(l -> l.getTitle() != null && l.getTitle().contains("Unit3") && !l.getTitle().contains("Grade 11")).findFirst();
        Lesson u3;
        String u3Audio = "/uploads/audio/1790758175148_93f5dc21_EG10_UNIT_3.A_1.B__High_quality_.mp3";
        if (optU3.isPresent()) {
            u3 = optU3.get();
            boolean changed = false;
            if (u3.getAudioUrl() == null || u3.getAudioUrl().trim().isEmpty()) {
                u3.setAudioUrl(u3Audio);
                changed = true;
            }
            if (u3.getClassName() == null || !u3.getClassName().contains("១០")) {
                u3.setClassName(classG10);
                changed = true;
            }
            if (changed) u3 = lessonRepository.save(u3);
        } else {
            u3 = new Lesson(accountId, "Unit3: MY HOUSE", classG10, "ភាសាអង់គ្លេស",
                    "Aims: Describe the position of things around them and what they look like\r\nForms: There is a bag on the TV in the living room. There aren't any cupboards in the kitchen.\r\nVocabulary: sofa, closet, cupboard, desk, mirror, drawers, bookshelf, bed, chair, refrigerator",
                    "គោលបំណង៖ ពិពណ៌នាអំពីទីតាំងរបស់វត្ថុជុំវិញខ្លួន និងរូបរាងរបស់វា។\r\nទម្រង់៖ មានកាបូបដាក់ទូរទស្សន៍នៅក្នុងបន្ទប់ទទួលភ្ញៀវ។ មិនមានទូដាក់ចាននៅក្នុងផ្ទះបាយទេ។\r\nវាក្យសព្ទ៖ សាឡុង ទូខោអាវ ទូដាក់ចាន តុ កញ្ចក់ ថតឯកសារ ធ្នើរសៀវភៅ គ្រែ កៅអី ទូទឹកកក",
                    u3Audio);
            u3 = lessonRepository.save(u3);
        }
        if (lessonTranscriptRepository.findByLessonIdOrderByOrderIndexAsc(u3.getId()).isEmpty()) {
            seedTranscriptsUnit3(u3.getId());
        }

        // Unit 4
        Optional<Lesson> optU4 = existing.stream().filter(l -> l.getTitle() != null && l.getTitle().contains("Unit4") && !l.getTitle().contains("Grade 11")).findFirst();
        Lesson u4;
        String u4Audio = "/uploads/audio/1790759024420_5a7d2e9f_EG10_UNIT_4.A_1.B__High_quality_.mp3";
        if (optU4.isPresent()) {
            u4 = optU4.get();
            boolean changed = false;
            if (u4.getAudioUrl() == null || u4.getAudioUrl().trim().isEmpty()) {
                u4.setAudioUrl(u4Audio);
                changed = true;
            }
            if (u4.getClassName() == null || !u4.getClassName().contains("១០")) {
                u4.setClassName(classG10);
                changed = true;
            }
            if (changed) u4 = lessonRepository.save(u4);
        } else {
            u4 = new Lesson(accountId, "Unit4: MY FAMILY", classG10, "ភាសាអង់គ្លេស",
                    "Aims: Describe and compare family members and people around them\r\nForms: My grandma isn't tall. She's short.\r\nPisey is pretty. My aunt is older than my dad.\r\nVocabulary: aunt, uncle, father, mother, sister, brother, grandma, grandpa, cousin\r\nyoung, tall, fat, short, old, long",
                    "គោលបំណង៖ ពិពណ៌នា និងប្រៀបធៀបសមាជិកគ្រួសារ និងមនុស្សជុំវិញពួកគេ។\r\nទម្រង់៖ យាយរបស់ខ្ញុំមិនខ្ពស់ទេ។ នាងទាប។\r\nពិសី ស្អាត. មីងខ្ញុំចាស់ជាងប៉ាខ្ញុំទៅទៀត។\r\nវាក្យសព្ទ៖ មីង ពូ ឪពុក ម្តាយ បងស្រី បងប្អូនប្រុស យាយ ជីតា បងប្អូនជីដូនមួយ\r\nក្មេង ខ្ពស់ ធាត់ ទាប ចាស់ វែង",
                    u4Audio);
            u4 = lessonRepository.save(u4);
        }
        if (lessonTranscriptRepository.findByLessonIdOrderByOrderIndexAsc(u4.getId()).isEmpty()) {
            seedTranscriptsUnit4(u4.getId());
        }

        // Unit 5
        Optional<Lesson> optU5 = existing.stream().filter(l -> l.getTitle() != null && l.getTitle().contains("Unit5") && !l.getTitle().contains("Grade 11")).findFirst();
        Lesson u5;
        String u5Audio = "/uploads/audio/1790761164924_5e1e7391_EG10_UNIT_5.A_5.B_Replace_OK__High_quality_.mp3";
        if (optU5.isPresent()) {
            u5 = optU5.get();
            boolean changed = false;
            if (u5.getAudioUrl() == null || u5.getAudioUrl().trim().isEmpty()) {
                u5.setAudioUrl(u5Audio);
                changed = true;
            }
            if (u5.getClassName() == null || !u5.getClassName().contains("១០")) {
                u5.setClassName(classG10);
                changed = true;
            }
            if (changed) u5 = lessonRepository.save(u5);
        } else {
            u5 = new Lesson(accountId, "Unit5: WHAT HAVE YOU GOT?", classG10, "ភាសាអង់គ្លេស",
                    "Aims: Talk about personal possessions\r\nForms: They haven't got shoes in the drawer.\r\nShe's got a purse in her bag.\r\nThis motorbike is mine. His sister's name is Amanda.\r\nVocabulary: water bottle, umbrella, camera, glasses, pen, medicine, keys, purse, comb, pencil, notebook, mobile phone",
                    "គោលបំណង៖ និយាយអំពីទ្រព្យសម្បត្តិផ្ទាល់ខ្លួន\r\nទម្រង់៖ ពួកគេមិនមានស្បែកជើងនៅក្នុងថតទេ។\r\nនាងមានកាបូបមួយនៅក្នុងកាបូបរបស់នាង។\r\nម៉ូតូនេះជារបស់ខ្ញុំ។ ប្អូនស្រីរបស់គាត់ឈ្មោះ អាម៉ាន់ដា។\r\nវាក្យសព្ទ៖ ដបទឹក ឆ័ត្រ កាមេរ៉ា វ៉ែនតា ប៊ិច ថ្នាំ កូនសោ កាបូប សិតសក់ ខ្មៅដៃ សៀវភៅកត់ត្រា ទូរស័ព្ទដៃ",
                    u5Audio);
            u5 = lessonRepository.save(u5);
        }
        if (lessonTranscriptRepository.findByLessonIdOrderByOrderIndexAsc(u5.getId()).isEmpty()) {
            seedTranscriptsUnit5(u5.getId());
        }
    }

    private void seedOrUpdateGrade11Lessons(Long accountId, List<Lesson> existing) {
        String classG11 = "១១F, ១១G";

        // Grade 11 Unit 1
        Optional<Lesson> optU1 = existing.stream().filter(l -> l.getTitle() != null && l.getTitle().contains("Grade 11 - Unit 1")).findFirst();
        Lesson u1;
        String u1Audio = "/uploads/audio/1790607688743_EG10_UNIT_1.A_3.A__High_quality_.mp3";
        if (optU1.isPresent()) {
            u1 = optU1.get();
            boolean changed = false;
            if (u1.getAudioUrl() == null || u1.getAudioUrl().trim().isEmpty()) {
                u1.setAudioUrl(u1Audio);
                changed = true;
            }
            if (u1.getClassName() == null || !u1.getClassName().contains("១១")) {
                u1.setClassName(classG11);
                changed = true;
            }
            if (changed) u1 = lessonRepository.save(u1);
        } else {
            u1 = new Lesson(accountId, "Grade 11 - Unit 1: MEETING NEW FRIENDS", classG11, "ភាសាអង់គ្លេស",
                    "Aims: Hold a conversation where people meet new friends, greet each other, exchange personal information, interests, and contact details.\r\nForms: Nice to meet you. Where do you come from? What do you like doing in your free time?\r\nVocabulary: greeting, introduce, classmate, hobby, interest, hometown, contact number",
                    "គោលបំណង៖ ជួប និងបង្កើតមិត្តភក្តិថ្មីៗ ការសន្ទនាស្វាគមន៍គ្នាទៅវិញទៅមក និងការផ្លាស់ប្តូរព័ត៌មានផ្ទាល់ខ្លួនព្រមទាំងចំណង់ចំណូលចិត្ត។\r\nទម្រង់៖ រីករាយដែលបានស្គាល់អ្នក។ តើអ្នកមកពីណា? តើអ្នកចូលចិត្តធ្វើអ្វីនៅពេលទំនេរ?\r\nវាក្យសព្ទ៖ ការស្វាគមន៍, ការណែនាំ, មិត្តរួមថ្នាក់, ចំណង់ចំណូលចិត្ត, ស្រុកកំណើត, លេខទំនាក់ទំនង",
                    u1Audio);
            u1 = lessonRepository.save(u1);
        }
        if (lessonTranscriptRepository.findByLessonIdOrderByOrderIndexAsc(u1.getId()).isEmpty()) {
            seedTranscriptsG11Unit1(u1.getId());
        }

        // Grade 11 Unit 2
        Optional<Lesson> optU2 = existing.stream().filter(l -> l.getTitle() != null && l.getTitle().contains("Grade 11 - Unit 2")).findFirst();
        Lesson u2;
        String u2Audio = "/uploads/audio/1790694853083_61632581_EG10_UNIT_2.A_2.B__High_quality_.mp3";
        if (optU2.isPresent()) {
            u2 = optU2.get();
            boolean changed = false;
            if (u2.getAudioUrl() == null || u2.getAudioUrl().trim().isEmpty()) {
                u2.setAudioUrl(u2Audio);
                changed = true;
            }
            if (u2.getClassName() == null || !u2.getClassName().contains("១១")) {
                u2.setClassName(classG11);
                changed = true;
            }
            if (changed) u2 = lessonRepository.save(u2);
        } else {
            u2 = new Lesson(accountId, "Grade 11 - Unit 2: CATCHING UP", classG11, "ភាសាអង់គ្លេស",
                    "Aims: Catch up with friends after vacation, discuss past events and holiday experiences.\r\nForms: How have you been? It's been a long time! What did you do during the break?\r\nVocabulary: catch up, vacation, holiday, experience, memorable, update",
                    "គោលបំណង៖ ការសន្ទនាជួបជុំគ្នាឡើងវិញ ការសួរសុខទុក្ខ និងការចែករំលែកបទពិសោធន៍អំឡុងពេលវិស្សមកាល។\r\nទម្រង់៖ តើអ្នកសុខសប្បាយជាទេមួយរយៈនេះ? ខានជួបគ្នាយូរហើយ! តើអ្នកបានធ្វើអ្វីខ្លះកាលពីពេលឈប់សម្រាក?\r\nវាក្យសព្ទ៖ ការជួបជុំឡើងវិញ, វិស្សមកាល, ថ្ងៃឈប់សម្រាក, បទពិសោធន៍, ការចងចាំ",
                    u2Audio);
            u2 = lessonRepository.save(u2);
        }
        if (lessonTranscriptRepository.findByLessonIdOrderByOrderIndexAsc(u2.getId()).isEmpty()) {
            seedTranscriptsG11Unit2(u2.getId());
        }

        // Grade 11 Unit 3
        Optional<Lesson> optU3 = existing.stream().filter(l -> l.getTitle() != null && l.getTitle().contains("Grade 11 - Unit 3")).findFirst();
        Lesson u3;
        String u3Audio = "/uploads/audio/1790758175148_93f5dc21_EG10_UNIT_3.A_1.B__High_quality_.mp3";
        if (optU3.isPresent()) {
            u3 = optU3.get();
            boolean changed = false;
            if (u3.getAudioUrl() == null || u3.getAudioUrl().trim().isEmpty()) {
                u3.setAudioUrl(u3Audio);
                changed = true;
            }
            if (u3.getClassName() == null || !u3.getClassName().contains("១១")) {
                u3.setClassName(classG11);
                changed = true;
            }
            if (changed) u3 = lessonRepository.save(u3);
        } else {
            u3 = new Lesson(accountId, "Grade 11 - Unit 3: AROUND TOWN", classG11, "ភាសាអង់គ្លេស",
                    "Aims: Describe places around town, ask for and give directions, and discuss local public transportation.\r\nForms: Excuse me, how do I get to the bank? Turn right at the traffic lights.\r\nVocabulary: museum, post office, supermarket, pharmacy, roundabout, intersection, opposite",
                    "គោលបំណង៖ ការពិពណ៌នាអំពីទីតាំងផ្សេងៗក្នុងទីក្រុង ការសួរ និងការប្រាប់ផ្លូវ ព្រមទាំងមធ្យោបាយធ្វើដំណើរ។\r\nទម្រង់៖ អធ្យាស្រ័យ តើខ្ញុំអាចទៅធនាគារដោយរបៀបណា? សូមបត់ស្តាំត្រង់ស្តុបចរាចរណ៍។\r\nវាក្យសព្ទ៖ សារមន្ទីរ, ប្រៃសណីយ៍, ផ្សារទំនើប, ឱសថស្ថាន, រង្វង់មូល, ផ្លូវបំបែក, ទល់មុខ",
                    u3Audio);
            u3 = lessonRepository.save(u3);
        }
        if (lessonTranscriptRepository.findByLessonIdOrderByOrderIndexAsc(u3.getId()).isEmpty()) {
            seedTranscriptsG11Unit3(u3.getId());
        }

        // Grade 11 Unit 4
        Optional<Lesson> optU4 = existing.stream().filter(l -> l.getTitle() != null && l.getTitle().contains("Grade 11 - Unit 4")).findFirst();
        Lesson u4;
        String u4Audio = "/uploads/audio/1790759024420_5a7d2e9f_EG10_UNIT_4.A_1.B__High_quality_.mp3";
        if (optU4.isPresent()) {
            u4 = optU4.get();
            boolean changed = false;
            if (u4.getAudioUrl() == null || u4.getAudioUrl().trim().isEmpty()) {
                u4.setAudioUrl(u4Audio);
                changed = true;
            }
            if (u4.getClassName() == null || !u4.getClassName().contains("១១")) {
                u4.setClassName(classG11);
                changed = true;
            }
            if (changed) u4 = lessonRepository.save(u4);
        } else {
            u4 = new Lesson(accountId, "Grade 11 - Unit 4: YOU MUSTN'T BREAK THE RULES", classG11, "ភាសាអង់គ្លេស",
                    "Aims: Understand obligation and prohibition using modal verbs (must, mustn't, have to, shouldn't).\r\nForms: You must wear a helmet. Students mustn't cheat on exams.\r\nVocabulary: rules, regulations, obligation, prohibition, helmet, uniform, discipline",
                    "គោលបំណង៖ ការយល់ដឹងអំពីបទបញ្ជា និងច្បាប់ចរាចរណ៍ ការប្រើប្រាស់កិរិយាសព្ទជំនួយ (must, mustn't, have to)។\r\nទម្រង់៖ អ្នកត្រូវតែពាក់មួកសុវត្ថិភាព។ សិស្សមិនត្រូវលួចចម្លងក្នុងពេលប្រឡងឡើយ។\r\nវាក្យសព្ទ៖ បទបញ្ជា, ច្បាប់ទម្លាប់, កាតព្វកិច្ច, ការហាមឃាត់, មួកសុវត្ថិភាព, ឯកសណ្ឋាន, វិន័យ",
                    u4Audio);
            u4 = lessonRepository.save(u4);
        }
        if (lessonTranscriptRepository.findByLessonIdOrderByOrderIndexAsc(u4.getId()).isEmpty()) {
            seedTranscriptsG11Unit4(u4.getId());
        }

        // Grade 11 Unit 5
        Optional<Lesson> optU5 = existing.stream().filter(l -> l.getTitle() != null && l.getTitle().contains("Grade 11 - Unit 5")).findFirst();
        Lesson u5;
        String u5Audio = "/uploads/audio/1790761164924_5e1e7391_EG10_UNIT_5.A_5.B_Replace_OK__High_quality_.mp3";
        if (optU5.isPresent()) {
            u5 = optU5.get();
            boolean changed = false;
            if (u5.getAudioUrl() == null || u5.getAudioUrl().trim().isEmpty()) {
                u5.setAudioUrl(u5Audio);
                changed = true;
            }
            if (u5.getClassName() == null || !u5.getClassName().contains("១១")) {
                u5.setClassName(classG11);
                changed = true;
            }
            if (changed) u5 = lessonRepository.save(u5);
        } else {
            u5 = new Lesson(accountId, "Grade 11 - Unit 5: CUSTOMS AND TRADITIONS", classG11, "ភាសាអង់គ្លេស",
                    "Aims: Explain Cambodian customs, festivals, and respectful social etiquette.\r\nForms: In Cambodia, people greet with a Sampeah. It is respectful to take off shoes before entering a home.\r\nVocabulary: custom, tradition, festival, Pchum Ben, Khmer New Year, Sampeah, respect",
                    "គោលបំណង៖ ការពន្យល់អំពីទំនៀមទម្លាប់ ប្រពៃណី ពិធីបុណ្យជាតិខ្មែរ និងសុជីវធម៌នៃការរស់នៅ។\r\nទម្រង់៖ នៅប្រទេសកម្ពុជា ប្រជាជនស្វាគមន៍គ្នាដោយការសំពះ។ វាជាសុជីវធម៌ក្នុងការដោះស្បែកជើងមុនពេលចូលផ្ទះ។\r\nវាក្យសព្ទ៖ ទំនៀមទម្លាប់, ប្រពៃណី, ពិធីបុណ្យ, ភ្ជុំបិណ្ឌ, ចូលឆ្នាំខ្មែរ, សំពះ, ការគោរព",
                    u5Audio);
            u5 = lessonRepository.save(u5);
        }
        if (lessonTranscriptRepository.findByLessonIdOrderByOrderIndexAsc(u5.getId()).isEmpty()) {
            seedTranscriptsG11Unit5(u5.getId());
        }
    }

    private void seedTranscriptsUnit1(Long lessonId) {
        List<LessonTranscript> list = List.of(
                new LessonTranscript(lessonId, "អត្ថបទស្តាប់ទី១",
                        "1.A_3A Listen and write the number you hear\r\n1. Fifteen  2. ninety   3. sixty  4. eighteen 5. I am fourteen  6. My dad is fifty\r\n7. Meng and Sothea are thirteen.    9. It's Oudom's birthday. He's seventy. \r\n10. No, she's sixteen",
                        "1.A_3A ស្តាប់ហើយសរសេរលេខដែលអ្នកឮ\r\n1. ដប់ប្រាំបួននាក់  2. ដប់ប្រាំបីនាក់ 4. ដប់ប្រាំបីនាក់ 5. ខ្ញុំ មាន អាយុ ១៤ ឆ្នាំ ។ ឪពុកខ្ញុំមានអាយុ១៥ឆ្នាំ\r\n7. ម៉េង និងសុធា មានដប់បីនាក់ ។    9. វាជាថ្ងៃខួបកំណើតរបស់ Oudom ។ គាត់មានអាយុ 70 ឆ្នាំ ។ \r\n10. ទេ នាងអាយុដប់ប្រាំមួយឆ្នាំ",
                        "/uploads/audio/1790611893123_a27f091e_EG10_UNIT_1.A_3.A__High_quality_.mp3", 1),
                new LessonTranscript(lessonId, "អត្ថបទស្តាប់ទី២",
                        "Listen and check your answer",
                        "ស្តាប់និងពិនិត្យមើលចម្លើយរបស់អ្នក",
                        "/uploads/audio/1790611893149_8ed108e6_EG10_UNIT_1.A_3.B__High_quality_.mp3", 2),
                new LessonTranscript(lessonId, "អត្ថបទស្តាប់ទី៣",
                        "Now listen again and decide which number end with \"-teen\" and which number end with \"-ty\"",
                        "ឥឡូវនេះ សូមស្តាប់ឡើងវិញ ហើយសម្រេចចិត្តថា តើលេខណាដែលបញ្ចប់ដោយ \"-ដប់ពីរ\" និងលេខណាដែលបញ្ចប់ដោយ \"-ty\"",
                        "/uploads/audio/1790611893445_ba89082d_EG10_UNIT_1.A_3.C__High_quality_.mp3", 3),
                new LessonTranscript(lessonId, "អត្ថបទស្តាប់ទី4",
                        "You will hear four short conversations . Write the names and phone numbers you hear in your notebooks",
                        "អ្នកនឹងឮការសន្ទនាខ្លីៗចំនួនបួន ។ សរសេរឈ្មោះនិងលេខទូរស័ព្ទដែលអ្នកឮក្នុងសៀវភៅកត់ត្រារបស់អ្នក",
                        "/uploads/audio/1790611893460_7d9e22c9_EG10_UNIT_1.A_4.A___High_quality_.mp3", 4),
                new LessonTranscript(lessonId, "អត្ថបទស្តាប់ទី៥",
                        "Listen again and check your answers.",
                        "ស្តាប់ម្តងទៀត ហើយពិនិត្យមើលចម្លើយរបស់អ្នក។",
                        "/uploads/audio/1790611893477_784a0bf3_EG10_UNIT_1.A_4.B__High_quality_.mp3", 5),
                new LessonTranscript(lessonId, "អត្ថបទស្តាប់ទី៦",
                        "Listen, check and repeat",
                        "ស្តាប់ ពិនិត្យ និងនិយាយឡើងវិញ",
                        "/uploads/audio/1790611893493_d3a27716_EG10_UNIT_1.B_4.C__High_quality_.mp3", 6)
        );
        lessonTranscriptRepository.saveAll(list);
    }

    private void seedTranscriptsUnit2(Long lessonId) {
        List<LessonTranscript> list = List.of(
                new LessonTranscript(lessonId, "អត្ថបទស្តាប់ទី១",
                        "2A\r\n2B. Listen, check and repeat",
                        "ស្តាប់ ពិនិត្យមើល និងនិយាយឡើងវិញ",
                        "/uploads/audio/1790694853083_61632581_EG10_UNIT_2.A_2.B__High_quality_.mp3", 1),
                new LessonTranscript(lessonId, "អត្ថបទស្តាប់ទី២",
                        "2.A 4B. Listen, check and repeat",
                        "ស្តាប់ ពិនិត្យមើល និងនិយាយឡើងវិញ",
                        "/uploads/audio/1790694853111_453a38dd_EG10_UNIT_2.A_4.B__High_quality_.mp3", 2),
                new LessonTranscript(lessonId, "អត្ថបទស្តាប់ទី៣",
                        "2B, 5B.Listen and check your answer",
                        "ស្តាប់ និងពិនិត្យចម្លើយរបស់អ្នក",
                        "/uploads/audio/1790694853122_43bc6b89_EG10_UNIT_2.B_5.B__High_quality_.mp3", 3)
        );
        lessonTranscriptRepository.saveAll(list);
    }

    private void seedTranscriptsUnit3(Long lessonId) {
        List<LessonTranscript> list = List.of(
                new LessonTranscript(lessonId, "អត្ថបទស្តាប់ទី១",
                        "bed, bookshelf, chair, closet, cupboard, desk, drawer, mirror, refrigerator, sofa, table",
                        "គ្រែ, ធ្នើសៀវភៅ, កៅអី, ទូខោអាវ, ទូដាក់ចាន, តុធ្វើការ, ថតតុ, កញ្ចក់, ទូទឹកកក, សាឡុង, តុ",
                        "/uploads/audio/1790758175148_93f5dc21_EG10_UNIT_3.A_1.B__High_quality_.mp3", 1),
                new LessonTranscript(lessonId, "អត្ថបទស្តាប់ទី២",
                        "Kosal: Hi, Tim! Welcome to my house.\r\nTim: Wow, Kosal! This room is big! Is it the living room?\r\nKosal: Yes, it is. There is a sofa and a TV.\r\nTim: That TV is huge! Where's your room, Kosal?\r\nKosal: It's upstairs. There's a bed and a desk in my room.\r\nTim: Cool! Let's go see it.",
                        "កុសល៖ សួស្តីធីម! សូមស្វាគមន៍មកកាន់ផ្ទះរបស់ខ្ញុំ។\r\nធីម៖ អីយ៉ា កុសល! បន្ទប់នេះធំណាស់! តើវាជាបន្ទប់ទទួលភ្ញៀវមែនទេ?\r\nកុសល៖ បាទ ពិតមែនហើយ។ មានសាឡុង និងទូរទស្សន៍មួយ។\r\nធីម៖ ទូរទស្សន៍នោះធំណាស់! តើបន្ទប់របស់អ្នកនៅឯណា កុសល?\r\nកុសល៖ នៅជាន់លើ។ មានគ្រែ និងតុមួយនៅក្នុងបន្ទប់របស់ខ្ញុំ។\r\nធីម៖ អស្ចារ្យណាស់! តោះទៅមើលទាំងអស់គ្នា។",
                        "/uploads/audio/1790758175191_68ca2ec6_EG10_UNIT_3.A_2.A__High_quality_.mp3", 2),
                new LessonTranscript(lessonId, "អត្ថបទស្តាប់ទី៣",
                        "1. There are two spoons in the cupboard.\r\n2. There is a cat under the chair.\r\n3. There aren't any glasses on the table.\r\n4. Is there a mirror in the bathroom?\r\n5. There is a big refrigerator in the kitchen.",
                        "1. មានស្លាបព្រាពីរនៅក្នុងទូដាក់ចាន។\r\n2. មានឆ្មាមួយនៅក្រោមកៅអី។\r\n3. គ្មានកែវនៅលើតុទេ។\r\n4. តើមានកញ្ចក់នៅក្នុងបន្ទប់ទឹកទេ?\r\n5. មានទូទឹកកកធំមួយនៅក្នុងផ្ទះបាយ។",
                        "/uploads/audio/1790758175196_37f5d27c_EG10_UNIT_3.B_3.B__High_quality_.mp3", 3)
        );
        lessonTranscriptRepository.saveAll(list);
    }

    private void seedTranscriptsUnit4(Long lessonId) {
        List<LessonTranscript> list = List.of(
                new LessonTranscript(lessonId, "អត្ថបទស្តាប់ទី១",
                        "Tim. Oh, I miss home! I miss my family!\r\nKosal: Tell me about your family, Tim.\r\nTim: Well, my dad is tall and strong. He's a doctor.\r\nKosal: What about your mum?\r\nTim: My mum is very kind and pretty. She has long brown hair.\r\nKosal: Have you got any brothers or sisters?\r\nTim: Yes, I've got one older brother. He's very clever.",
                        "ធីម៖ អូ ខ្ញុំនឹកផ្ទះណាស់! ខ្ញុំនឹកគ្រួសាររបស់ខ្ញុំ!\r\nកុសល៖ ប្រាប់ខ្ញុំអំពីគ្រួសាររបស់អ្នកមក ធីម។\r\nធីម៖ ឪពុកខ្ញុំខ្ពស់ និងរឹងមាំ។ គាត់ជាវេជ្ជបណ្ឌិត។\r\nកុសល៖ ចុះម្តាយរបស់អ្នកវិញ?\r\nធីម៖ ម្តាយខ្ញុំចិត្តល្អ និងស្អាតណាស់។ គាត់មានសក់វែងពណ៌ត្នោត។\r\nកុសល៖ តើអ្នកមានបងប្អូនប្រុសស្រីទេ?\r\nធីម៖ បាទ ខ្ញុំមានបងប្រុសម្នាក់។ គាត់ឆ្លាតណាស់។",
                        "/uploads/audio/1790759024420_5a7d2e9f_EG10_UNIT_4.A_1.B__High_quality_.mp3", 1),
                new LessonTranscript(lessonId, "អត្ថបទស្តាប់ទី២",
                        "Listening: Listen again. Match the people with what they look like.",
                        "ការស្តាប់៖ ស្តាប់ម្តងទៀត។ ផ្គូផ្គងមនុស្សជាមួយនឹងរូបរាងរបស់ពួកគេ។",
                        "/uploads/audio/1790759024450_6dec8ad4_EG10_UNIT_4.A_2__High_quality_.mp3", 2)
        );
        lessonTranscriptRepository.saveAll(list);
    }

    private void seedTranscriptsUnit5(Long lessonId) {
        List<LessonTranscript> list = List.of(
                new LessonTranscript(lessonId, "អត្ថបទស្តាប់ទី១",
                        "1. an uncle, 2. an aunt, 3. a cousin, 4. a camera, 5. an umbrella, 6. a notebook",
                        "1. ពូម្នាក់, 2. មីងម្នាក់, 3. បងប្អូនជីដូនមួយម្នាក់, 4. កាមេរ៉ាមួយ, 5. ឆ័ត្រមួយ, 6. សៀវភៅកត់ត្រាមួយ",
                        "/uploads/audio/1790761164924_5e1e7391_EG10_UNIT_5.A_5.B_Replace_OK__High_quality_.mp3", 1),
                new LessonTranscript(lessonId, "អត្ថបទស្តាប់ទី២",
                        "Staff: Hello, ma'am. How can I help you?\r\nTourist: I think I lost my backpack on the bus.\r\nStaff: What is inside the backpack?\r\nTourist: There is a camera, my sunglasses, and a water bottle.\r\nStaff: Is this blue backpack yours?\r\nTourist: Yes! That's mine! Thank you so much!",
                        "បុគ្គលិក៖ ជំរាបសួរលោកស្រី។ តើខ្ញុំអាចជួយលោកស្រីដោយរបៀបណា?\r\nភ្ញៀវទេសចរ៖ ខ្ញុំគិតថាខ្ញុំភ្លេចកាតាបស្ពាយនៅលើឡានក្រុង។\r\nបុគ្គលិក៖ តើមានអ្វីនៅក្នុងកាតាបនោះខ្លះ?\r\nភ្ញៀវទេសចរ៖ មានកាមេរ៉ាមួយ វ៉ែនតាការពារកម្តៅថ្ងៃរបស់ខ្ញុំ និងដបទឹកមួយ។\r\nបុគ្គលិក៖ តើកាតាបពណ៌ខៀវនេះជារបស់លោកស្រីមែនទេ?\r\nភ្ញៀវទេសចរ៖ បាទ! នោះជារបស់ខ្ញុំហើយ! អរគុណច្រើនណាស់!",
                        "/uploads/audio/1790761164940_0c170128_EG10_UNIT_5.B_1.A__High_quality_.mp3", 2),
                new LessonTranscript(lessonId, "អត្ថបទស្តាប់ទី៣",
                        "Conversation 1:\r\nLeakhena: Have you got a pen?\r\nDara: Yes, I have. Here you are.\r\nLeakhena: Thanks!\r\n\r\nConversation 2:\r\nSophea: Has he got a motorcycle?\r\nBona: No, he hasn't. He has got a bicycle.",
                        "ការសន្ទនាទី ១៖\r\nលក្ខិណា៖ តើអ្នកមានប៊ិចទេ?\r\nតារា៖ បាទ ខ្ញុំមាន។ នេះនែ៎។\r\nលក្ខិណា៖ អរគុណ!\r\n\r\nការសន្ទនាទី ២៖\r\nសុភា៖ តើគាត់មានម៉ូតូទេ?\r\nបូណា៖ អត់ទេ គាត់គ្មានទេ។ គាត់មានកង់មួយ។",
                        "/uploads/audio/1790761164952_b9a1c82c_EG10_UNIT_5.B_4__High_quality_.mp3", 3)
        );
        lessonTranscriptRepository.saveAll(list);
    }

    private void seedTranscriptsG11Unit1(Long lessonId) {
        List<LessonTranscript> list = List.of(
                new LessonTranscript(lessonId, "អត្ថបទស្តាប់ទី១: Meeting a new student",
                        "Dara: Hello, my name is Dara. Are you new here?\r\nSreypov: Hi Dara, yes, I just transferred from Battambang. Nice to meet you!\r\nDara: Welcome to our school! What class are you in?\r\nSreypov: I am in Class 11F. And you?\r\nDara: Me too! Let me show you around the classroom.",
                        "តារា៖ សួស្តី ខ្ញុំឈ្មោះតារា។ តើអ្នកជាសិស្សថ្មីនៅទីនេះមែនទេ?\r\nស្រីពៅ៖ សួស្តីតារា បាទ/ចាស ខ្ញុំទើបតែផ្ទេរមកពីបាត់ដំបង។ រីករាយណាស់ដែលបានស្គាល់អ្នក!\r\nតារា៖ សូមស្វាគមន៍មកកាន់សាលារបស់យើង! តើអ្នករៀននៅថ្នាក់ណាដែរ?\r\nស្រីពៅ៖ ខ្ញុំរៀននៅថ្នាក់ ១១F។ ចុះអ្នកវិញ?\r\nតារា៖ ខ្ញុំក៏នៅថ្នាក់ហ្នឹងដែរ! តោះខ្ញុំនាំអ្នកទៅមើលបន្ទប់រៀន។",
                        "/uploads/audio/1790607688743_EG10_UNIT_1.A_3.A__High_quality_.mp3", 1),
                new LessonTranscript(lessonId, "អត្ថបទស្តាប់ទី២: Talking about hobbies and interests",
                        "Sreypov: What do you like to do in your free time, Dara?\r\nDara: I enjoy playing football and practicing English speaking. What about you?\r\nSreypov: I love reading novels and learning traditional Khmer music.\r\nDara: That sounds wonderful! Can I have your phone number so we can study together?\r\nSreypov: Sure! It's 012 345 678.",
                        "ស្រីពៅ៖ តើអ្នកចូលចិត្តធ្វើអ្វីនៅពេលទំនេរ តារា?\r\nតារា៖ ខ្ញុំចូលចិត្តលេងបាល់ទាត់ និងហាត់និយាយភាសាអង់គ្លេស។ ចុះអ្នកវិញ?\r\nស្រីពៅ៖ ខ្ញុំចូលចិត្តអានសៀវភៅប្រលោមលោក និងរៀនតន្ត្រីបុរាណខ្មែរ។\r\nតារា៖ ពិសេសណាស់! តើខ្ញុំអាចសុំលេខទូរស័ព្ទរបស់អ្នកបានទេ ដើម្បីយើងអាចរៀនជាមួយគ្នា?\r\nស្រីពៅ៖ ចាសបាន! គឺលេខ 012 345 678។",
                        "/uploads/audio/1790611893149_8ed108e6_EG10_UNIT_1.A_3.B__High_quality_.mp3", 2)
        );
        lessonTranscriptRepository.saveAll(list);
    }

    private void seedTranscriptsG11Unit2(Long lessonId) {
        List<LessonTranscript> list = List.of(
                new LessonTranscript(lessonId, "អត្ថបទស្តាប់ទី១: Greeting after vacation",
                        "Bona: Hey Piseth! Long time no see! How was your vacation?\r\nPiseth: Hi Bona! It was fantastic. I visited my grandparents in Siem Reap and went to Angkor Wat.\r\nBona: That sounds exciting! Did you take many photos?\r\nPiseth: Yes, plenty! How about you? What have you been up to?",
                        "បូណា៖ សួស្តីពិសិដ្ឋ! ខានជួបគ្នាយូរហើយ! តើវិស្សមកាលរបស់អ្នកយ៉ាងម៉េចដែរ?\r\nពិសិដ្ឋ៖ សួស្តីបូណា! វាពិតជាអស្ចារ្យណាស់។ ខ្ញុំបានទៅលេងជីដូនជីតានៅសៀមរាប និងបានទៅទស្សនាប្រាសាទអង្គរវត្ត។\r\nបូណា៖ គួរឱ្យរំភើបណាស់! តើអ្នកបានថតរូបច្រើនទេ?\r\nពិសិដ្ឋ៖ បាទ ច្រើនណាស់! ចុះអ្នកវិញ? តើអ្នកបានធ្វើអ្វីខ្លះដែរ?",
                        "/uploads/audio/1790694853083_61632581_EG10_UNIT_2.A_2.B__High_quality_.mp3", 1),
                new LessonTranscript(lessonId, "អត្ថបទស្តាប់ទី២: Sharing recent updates",
                        "Bona: I stayed home helping my parents at their shop and took an online computer course.\r\nPiseth: Good job! Are you ready for the new semester?\r\nBona: Absolutely, let's work hard together this year!",
                        "បូណា៖ ខ្ញុំនៅផ្ទះជួយលក់ដូរឪពុកម្តាយ និងបានរៀនវគ្គកុំព្យូទ័រតាមអ៊ីនធឺណិតខ្លះៗ។\r\nពិសិដ្ឋ៖ ពូកែណាស់! តើអ្នកត្រៀមខ្លួនសម្រាប់ឆមាសថ្មីនេះហើយឬនៅ?\r\nបូណា៖ រួចរាល់ហើយ តោះយើងខិតខំរៀនសូត្រទាំងអស់គ្នាក្នុងឆ្នាំនេះ!",
                        "/uploads/audio/1790694853111_453a38dd_EG10_UNIT_2.A_4.B__High_quality_.mp3", 2)
        );
        lessonTranscriptRepository.saveAll(list);
    }

    private void seedTranscriptsG11Unit3(Long lessonId) {
        List<LessonTranscript> list = List.of(
                new LessonTranscript(lessonId, "អត្ថបទស្តាប់ទី១: Directions around town",
                        "Tourist: Excuse me, could you tell me how to get to the central market?\r\nLocal: Certainly. Walk straight down this road for two blocks, then turn right at the traffic lights. The market is opposite the big bank.\r\nTourist: Is it far from here?\r\nLocal: No, it is just about a five-minute walk.",
                        "ភ្ញៀវទេសចរ៖ អធ្យាស្រ័យ តើអ្នកអាចប្រាប់ខ្ញុំពីផ្លូវទៅកាន់ផ្សារធំបានទេ?\r\nអ្នកស្រុក៖ បាទ/ចាស បាន។ សូមដើរត្រង់តាមផ្លូវនេះប្រហែលពីរផ្លូវកាត់ រួចបត់ស្តាំត្រង់ភ្លើងស្តុប។ ផ្សារនៅទល់មុខធនាគារធំ។\r\nភ្ញៀវទេសចរ៖ តើវាឆ្ងាយពីទីនេះទេ?\r\nអ្នកស្រុក៖ ទេ ដើរតែប្រហែល ៥ នាទីប៉ុណ្ណោះ។",
                        "/uploads/audio/1790758175148_93f5dc21_EG10_UNIT_3.A_1.B__High_quality_.mp3", 1)
        );
        lessonTranscriptRepository.saveAll(list);
    }

    private void seedTranscriptsG11Unit4(Long lessonId) {
        List<LessonTranscript> list = List.of(
                new LessonTranscript(lessonId, "អត្ថបទស្តាប់ទី១: School and traffic rules",
                        "Teacher: Remember everyone, students must arrive at school before 7:00 AM.\r\nStudent: Teacher, are we allowed to use mobile phones during study hours?\r\nTeacher: No, you mustn't use mobile phones in class unless the teacher gives permission. And when riding home, you must wear a helmet at all times.",
                        "លោកគ្រូ៖ សូមអ្នកទាំងអស់គ្នាចងចាំ សិស្សទាំងអស់ត្រូវមកដល់សាលាមុនម៉ោង ៧:០០ ព្រឹក។\r\nសិស្ស៖ លោកគ្រូ តើយើងត្រូវបានអនុញ្ញាតឱ្យប្រើទូរស័ព្ទក្នុងម៉ោងសិក្សាទេ?\r\nលោកគ្រូ៖ ទេ អ្នកមិនត្រូវប្រើទូរស័ព្ទក្នុងថ្នាក់រៀនទេ លើកលែងតែមានការអនុញ្ញាតពីគ្រូ។ ហើយពេលជិះម៉ូតូត្រឡប់ទៅផ្ទះវិញ អ្នកត្រូវតែពាក់មួកសុវត្ថិភាពគ្រប់ពេលវេលា។",
                        "/uploads/audio/1790759024420_5a7d2e9f_EG10_UNIT_4.A_1.B__High_quality_.mp3", 1)
        );
        lessonTranscriptRepository.saveAll(list);
    }

    private void seedTranscriptsG11Unit5(Long lessonId) {
        List<LessonTranscript> list = List.of(
                new LessonTranscript(lessonId, "អត្ថបទស្តាប់ទី១: Cambodian customs and greeting",
                        "Guide: In Cambodian culture, greeting elders and teachers with a respectful Sampeah is an important tradition.\r\nVisitor: How should we hold our hands during a Sampeah?\r\nGuide: Place your palms together in front of your chest or nose depending on the level of respect, and bow your head slightly.",
                        "មគ្គុទ្ទេសក៍៖ នៅក្នុងវប្បធម៌ខ្មែរ ការស្វាគមន៍ចាស់ព្រឹទ្ធាចារ្យ និងលោកគ្រូអ្នកគ្រូដោយការសំពះ គឺជាប្រពៃណីដ៏សំខាន់មួយ។\r\nភ្ញៀវ៖ តើយើងគួរដាក់បាតដៃបែបណាពេលសំពះ?\r\nមគ្គុទ្ទេសក៍៖ សូមផ្គុំបាតដៃទាំងពីរនៅត្រឹមទ្រូង ឬច្រមុះ អាស្រ័យលើកម្រិតនៃការគោរព រួចឱនក្បាលបន្តិច។",
                        "/uploads/audio/1790761164924_5e1e7391_EG10_UNIT_5.A_5.B_Replace_OK__High_quality_.mp3", 1)
        );
        lessonTranscriptRepository.saveAll(list);
    }
}
