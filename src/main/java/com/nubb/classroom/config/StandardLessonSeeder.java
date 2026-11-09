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
        teacherIds.add(33L); // Teacher1001
        teacherIds.add(98L); // demo

        for (Long accId : teacherIds) {
            seedLessonsForTeacher(accId);
        }
    }

    @Transactional
    public void seedLessonsForTeacher(Long accountId) {
        if (accountId == null) return;
        List<Lesson> existing = lessonRepository.findByAccountIdOrderByIdAsc(accountId);
        boolean hasUnit1 = existing.stream().anyMatch(l -> l.getTitle() != null && l.getTitle().startsWith("Unit1"));
        if (hasUnit1) {
            // Verify all units have transcripts
            for (Lesson l : existing) {
                if (l.getTitle() != null && l.getTitle().startsWith("Unit")) {
                    List<LessonTranscript> trs = lessonTranscriptRepository.findByLessonIdOrderByOrderIndexAsc(l.getId());
                    if (trs.isEmpty()) {
                        seedTranscriptsForLesson(l);
                    }
                }
            }
            return;
        }

        System.out.println(">>> [StandardLessonSeeder] Seeding Unit 1 - Unit 5 audio lessons for teacher account ID: " + accountId);

        // Unit 1
        Lesson u1 = new Lesson(accountId, "Unit1 : MY PERSONAL INFORMATION", "គ្រប់ថ្នាក់", "ភាសាអង់គ្លេស",
                "Unit1 Aims :  Hold a conversation where people greet  each other ,exchange information about name (with spelling) age, and telephone number",
                "Unit1 គោលបំណង៖ ធ្វើការសន្ទនាដែលមនុស្សស្វាគមន៍គ្នាទៅវិញទៅមកផ្លាស់ប្តូរព័ត៌មានអំពីឈ្មោះ (ជាមួយនឹងអក្ខរាវិរុទ្ធ) អាយុ និងលេខទូរស័ព្ទ",
                null);
        u1 = lessonRepository.save(u1);
        seedTranscriptsUnit1(u1.getId());

        // Unit 2
        Lesson u2 = new Lesson(accountId, "Unit2:  COUNTRIES AND NATIONALITIES", "គ្រប់ថ្នាក់", "ភាសាអង់គ្លេស",
                "Aims\r\nIdentify some countries and tell and ask other people where they are from and their nationalities\r\nForms\r\nWhere are you from?\r\nAre you from China? \r\nShe isn't Cambodian. \r\nThey're from Thailand.",
                "គោលបំណង\r\nកំណត់អត្តសញ្ញាណប្រទេសមួយចំនួន ហើយប្រាប់ និងសួរមនុស្សផ្សេងទៀតថាតើពួកគេមកពីណា និងសញ្ជាតិរបស់ពួកគេ។\r\nទម្រង់\r\nតើអ្នកមកពីណា?\r\nតើអ្នកមកពីប្រទេសចិនទេ? \r\nនាងមិនមែនជាជនជាតិខ្មែរទេ។ \r\nពួកគេមកពីប្រទេសថៃ។",
                null);
        u2 = lessonRepository.save(u2);
        seedTranscriptsUnit2(u2.getId());

        // Unit 3
        Lesson u3 = new Lesson(accountId, "Unit3: MY HOUSE", "គ្រប់ថ្នាក់", "ភាសាអង់គ្លេស",
                "Aims: Describe the position of things around them and what they look like\r\nForms: There is a bag on the TV in the living room. There aren't any cupboards in the kitchen.\r\nVocabulary: sofa, closet, cupboard, desk, mirror, drawers, bookshelf, bed, chair, refrigerator\r\n",
                "គោលបំណង៖ ពិពណ៌នាអំពីទីតាំងរបស់វត្ថុជុំវិញខ្លួន និងរូបរាងរបស់វា។\r\nទម្រង់៖ មានកាបូបដាក់ទូរទស្សន៍នៅក្នុងបន្ទប់ទទួលភ្ញៀវ។ មិនមានទូដាក់ចាននៅក្នុងផ្ទះបាយទេ។\r\nវាក្យសព្ទ៖ សាឡុង ទូខោអាវ ទូដាក់ចាន តុ កញ្ចក់ ថតឯកសារ ធ្នើរសៀវភៅ គ្រែ កៅអី ទូទឹកកក",
                null);
        u3 = lessonRepository.save(u3);
        seedTranscriptsUnit3(u3.getId());

        // Unit 4
        Lesson u4 = new Lesson(accountId, "Unit4: MY FAMILY", "គ្រប់ថ្នាក់", "ភាសាអង់គ្លេស",
                "Aims: Describe and compare family members and people around them\r\nForms: My grandma isn't tall. She's short.\r\nPisey is pretty. My aunt is older than my dad.\r\nVocabulary: aunt, uncle, father, mother, sister, brother, grandma, grandpa, cousin\r\nyoung, tall, fat, short, old, long",
                "គោលបំណង៖ ពិពណ៌នា និងប្រៀបធៀបសមាជិកគ្រួសារ និងមនុស្សជុំវិញពួកគេ។\r\nទម្រង់៖ យាយរបស់ខ្ញុំមិនខ្ពស់ទេ។ នាងទាប។\r\nពិសី ស្អាត. មីងខ្ញុំចាស់ជាងប៉ាខ្ញុំទៅទៀត។\r\nវាក្យសព្ទ៖ មីង ពូ ឪពុក ម្តាយ បងស្រី បងប្អូនប្រុស យាយ ជីតា បងប្អូនជីដូនមួយ\r\nក្មេង ខ្ពស់ ធាត់ ទាប ចាស់ វែង",
                null);
        u4 = lessonRepository.save(u4);
        seedTranscriptsUnit4(u4.getId());

        // Unit 5
        Lesson u5 = new Lesson(accountId, "Unit5: WHAT HAVE YOU GOT?", "គ្រប់ថ្នាក់", "ភាសាអង់គ្លេស",
                "Aims: Talk about personal possessions\r\nForms: They haven't got shoes in the drawer.\r\nShe's got a purse in her bag.\r\nThis motorbike is mine. His sister's name is Amanda.\r\nVocabulary: water bottle, umbrella, camera, glasses, pen, medicine, keys, purse, comb, pencil, notebook, mobile phone",
                "គោលបំណង៖ និយាយអំពីទ្រព្យសម្បត្តិផ្ទាល់ខ្លួន\r\nទម្រង់៖ ពួកគេមិនមានស្បែកជើងនៅក្នុងថតទេ។\r\nនាងមានកាបូបមួយនៅក្នុងកាបូបរបស់នាង។\r\nម៉ូតូនេះជារបស់ខ្ញុំ។ ប្អូនស្រីរបស់គាត់ឈ្មោះ អាម៉ាន់ដា។\r\nវាក្យសព្ទ៖ ដបទឹក ឆ័ត្រ កាមេរ៉ា វ៉ែនតា ប៊ិច ថ្នាំ កូនសោ កាបូប សិតសក់ ខ្មៅដៃ សៀវភៅកត់ត្រា ទូរស័ព្ទដៃ",
                null);
        u5 = lessonRepository.save(u5);
        seedTranscriptsUnit5(u5.getId());
    }

    private void seedTranscriptsForLesson(Lesson l) {
        if (l == null || l.getTitle() == null) return;
        String t = l.getTitle().trim();
        if (t.startsWith("Unit1")) seedTranscriptsUnit1(l.getId());
        else if (t.startsWith("Unit2")) seedTranscriptsUnit2(l.getId());
        else if (t.startsWith("Unit3")) seedTranscriptsUnit3(l.getId());
        else if (t.startsWith("Unit4")) seedTranscriptsUnit4(l.getId());
        else if (t.startsWith("Unit5")) seedTranscriptsUnit5(l.getId());
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
}
