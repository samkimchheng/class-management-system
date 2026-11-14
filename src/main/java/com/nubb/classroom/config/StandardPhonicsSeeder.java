package com.nubb.classroom.config;

import com.nubb.classroom.model.PhonicsItem;
import com.nubb.classroom.repository.PhonicsItemRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class StandardPhonicsSeeder {

    @Autowired
    private PhonicsItemRepository phonicsItemRepository;

    @PostConstruct
    public void init() {
        try {
            ensureAllLevelsSeeded();
        } catch (Exception e) {
            System.err.println(">>> [StandardPhonicsSeeder] Error on startup seed: " + e.getMessage());
        }
    }

    @Transactional
    public void ensureAllLevelsSeeded() {
        seedLevel1Alphabet();
        seedLevel2ShortVowels();
        seedLevel3LongVowels();
        seedLevel4ConsonantBlends();
        seedLevel5LetterCombinations();
    }

    @Transactional
    public void seedByLevel(int level) {
        switch (level) {
            case 1 -> seedLevel1Alphabet();
            case 2 -> seedLevel2ShortVowels();
            case 3 -> seedLevel3LongVowels();
            case 4 -> seedLevel4ConsonantBlends();
            case 5 -> seedLevel5LetterCombinations();
            default -> ensureAllLevelsSeeded();
        }
    }

    private void saveLevelItems(int level, List<PhonicsItem> items) {
        List<PhonicsItem> existing = phonicsItemRepository.findByPhonicsLevelOrderByItemNumberAsc(level);
        if (existing.size() >= items.size()) {
            return;
        }
        Map<String, PhonicsItem> existingByWord = new HashMap<>();
        for (PhonicsItem ex : existing) {
            if (ex.getEnglishWord() != null) {
                existingByWord.put(ex.getEnglishWord().toLowerCase().trim(), ex);
            }
        }
        List<PhonicsItem> toSave = new ArrayList<>();
        int num = 1;
        for (PhonicsItem item : items) {
            item.setItemNumber(num++);
            item.setPhonicsLevel(level);
            PhonicsItem found = existingByWord.get(item.getEnglishWord().toLowerCase().trim());
            if (found != null) {
                found.setItemNumber(item.getItemNumber());
                found.setUnitName(item.getUnitName());
                found.setSoundPattern(item.getSoundPattern());
                found.setIpaPhonetic(item.getIpaPhonetic());
                found.setKhmerPhonetic(item.getKhmerPhonetic());
                found.setBlendingParts(item.getBlendingParts());
                found.setKhmerTranslation(item.getKhmerTranslation());
                found.setEmojiIcon(item.getEmojiIcon());
                found.setExampleSentence(item.getExampleSentence());
                found.setExampleSentenceKhmer(item.getExampleSentenceKhmer());
                toSave.add(found);
            } else {
                toSave.add(item);
            }
        }
        phonicsItemRepository.saveAll(toSave);
    }

    private PhonicsItem item(int level, String unit, String pattern, String word, String ipa,
                             String khmerPhonetic, String blend, String khmerMeaning,
                             String emoji, String sentenceEn, String sentenceKm) {
        return new PhonicsItem(0, level, unit, pattern, word, ipa, khmerPhonetic, blend, khmerMeaning, emoji, sentenceEn, sentenceKm);
    }

    // =========================================================================
    // LEVEL 1: THE ALPHABET (អក្សរក្រម និងសូរសំឡេងមូលដ្ឋាន A-Z) - 8 Units
    // =========================================================================
    @Transactional
    public void seedLevel1Alphabet() {
        int L = 1;
        List<PhonicsItem> list = List.of(
            // Unit 1: Aa, Bb, Cc
            item(L, "Unit 1: Aa - Cc", "A a /æ/", "apple", "/ˈæp.əl/", "អ៊ែ-ផល (a=អ៊ែ + pp=ផ + le=អល)", "a|pp|le", "ផ្លែប៉ោម", "🍎", "I eat a red apple.", "ខ្ញុំញ៉ាំផ្លែប៉ោមពណ៌ក្រហមមួយ។"),
            item(L, "Unit 1: Aa - Cc", "A a /æ/", "ant", "/ænt/", "អ៊ែនថ៏ (a=អ៊ែ + n=ន + t=ថ)", "a|n|t", "សត្វស្រមោច", "🐜", "A small ant is on the leaf.", "ស្រមោចតូចមួយនៅលើស្លឹកឈើ។"),
            item(L, "Unit 1: Aa - Cc", "A a /æ/", "alligator", "/ˈæl.ɪ.ɡeɪ.tər/", "អ៊ែ-លី-ហ្គេ-ធឺរ (a=អ៊ែ)", "a|ll|i|ga|tor", "សត្វក្រពើ", "🐊", "The green alligator swims fast.", "ក្រពើពណ៌បៃតងហែលទឹកលឿន។"),
            item(L, "Unit 1: Aa - Cc", "B b /b/", "bear", "/beər/", "ប៊ែរ (b=ប + ear=អ៊ែរ)", "b|ear", "សត្វខ្លាឃ្មុំ", "🐻", "The big brown bear likes honey.", "ខ្លាឃ្មុំពណ៌ត្នោតធំចូលចិត្តទឹកឃ្មុំ។"),
            item(L, "Unit 1: Aa - Cc", "B b /b/", "bird", "/bɜːd/", "បឺដ (b=ប + ir=អឺ + d=ដ)", "b|ir|d", "សត្វចាប / បក្សី", "🐦", "A blue bird sings in the tree.", "បក្សីពណ៌ខៀវមួយក្បាលច្រៀងលើដើមឈើ។"),
            item(L, "Unit 1: Aa - Cc", "C c /k/", "cat", "/kæt/", "ខែថ៏ (c=ខ + a=អ៊ែ + t=ថ)", "c|a|t", "សត្វឆ្មា", "🐱", "My cute cat sleeps on the sofa.", "ឆ្មាដ៏គួរឱ្យស្រឡាញ់របស់ខ្ញុំដេកលើសាឡុង។"),
            item(L, "Unit 1: Aa - Cc", "C c /k/", "cup", "/kʌp/", "ខាប់ (c=ខ + u=អាត់ + p=ផ)", "c|u|p", "ពែងទឹក", "☕", "She drinks tea from a white cup.", "នាងផឹកតែពីពែងពណ៌ស។"),

            // Unit 2: Dd, Ee, Ff
            item(L, "Unit 2: Dd - Ff", "D d /d/", "dog", "/dɒɡ/", "ដក់ហ្គ (d=ដ + o=អក់ + g=ហ្គ)", "d|o|g", "សត្វឆ្កែ", "🐶", "The friendly dog runs in the park.", "ឆ្កែដ៏ស្លូតបូតរត់លេងក្នុងសួនច្បារ។"),
            item(L, "Unit 2: Dd - Ff", "D d /d/", "duck", "/dʌk/", "ដាក់ខ៏ (d=ដ + u=អាត់ + ck=ខ)", "d|u|ck", "សត្វទា", "🦆", "A yellow duck swims in the pond.", "ទាពណ៌លឿងមួយក្បាលហែលក្នុងស្រះទឹក។"),
            item(L, "Unit 2: Dd - Ff", "E e /e/", "egg", "/eɡ/", "អ៊ែហ្គ (e=អ៊ែ + gg=ហ្គ)", "e|gg", "ស៊ុត / ពងមាន់", "🥚", "I have a boiled egg for breakfast.", "ខ្ញុំញ៉ាំស៊ុតស្ងោរមួយគ្រាប់សម្រាប់អាហារពេលព្រឹក។"),
            item(L, "Unit 2: Dd - Ff", "E e /e/", "elephant", "/ˈel.ɪ.fənt/", "អ៊ែ-លី-ហ្វិនថ៏ (e=អ៊ែ)", "e|le|ph|ant", "សត្វដំរី", "🐘", "The elephant has two big ears.", "សត្វដំរីមានត្រចៀកធំពីរ។"),
            item(L, "Unit 2: Dd - Ff", "F f /f/", "fish", "/fɪʃ/", "ហ្វីស (f=ហ្វ + i=អ៊ី + sh=ស៊)", "f|i|sh", "សត្វត្រី", "🐟", "Many colorful fish live in the sea.", "ត្រីចម្រុះពណ៌ជាច្រើនរស់នៅក្នុងសមុទ្រ។"),
            item(L, "Unit 2: Dd - Ff", "F f /f/", "fan", "/fæn/", "ហ្វែន (f=ហ្វ + a=អ៊ែ + n=ន)", "f|a|n", "កង្ហារ", "🪭", "Turn on the fan when it is hot.", "សូមបើកកង្ហារនៅពេលមេឃក្តៅ។"),

            // Unit 3: Gg, Hh, Ii
            item(L, "Unit 3: Gg - Ii", "G g /ɡ/", "goat", "/ɡəʊt/", "ហ្គោថ៏ (g=ហ្គ + oa=អូ + t=ថ)", "g|oa|t", "សត្វពពែ", "🐐", "The goat eats green grass on the hill.", "សត្វពពែស៊ីស្មៅបៃតងនៅលើកូនភ្នំ។"),
            item(L, "Unit 3: Gg - Ii", "G g /ɡ/", "girl", "/ɡɜːl/", "ហ្គឺល (g=ហ្គ + ir=អឺ + l=ល)", "g|ir|l", "ក្មេងស្រី", "👧", "The smart girl reads an English book.", "ក្មេងស្រីឆ្លាតអានសៀវភៅភាសាអង់គ្លេស។"),
            item(L, "Unit 3: Gg - Ii", "H h /h/", "hat", "/hæt/", "ហែថ៏ (h=ហ + a=អ៊ែ + t=ថ)", "h|a|t", "មួក", "👒", "He wears a sun hat at the beach.", "គាត់ពាក់មួកការពារកម្តៅថ្ងៃនៅឆ្នេរសមុទ្រ។"),
            item(L, "Unit 3: Gg - Ii", "H h /h/", "horse", "/hɔːs/", "ហស (h=ហ + or=អរ + se=ស)", "h|or|se", "សត្វសេះ", "🐴", "The white horse can run very fast.", "សេះពណ៌សអាចរត់បានលឿនណាស់។"),
            item(L, "Unit 3: Gg - Ii", "I i /ɪ/", "igloo", "/ˈɪɡ.luː/", "អ៊ីហ្គ-លូ (i=អ៊ី + g=ហ្គ + loo=លូ)", "i|g|loo", "ផ្ទះទឹកកក", "🛖", "An igloo is made of white snow.", "ផ្ទះទឹកកកត្រូវបានធ្វើឡើងពីព្រិលពណ៌ស។"),
            item(L, "Unit 3: Gg - Ii", "I i /ɪ/", "ink", "/ɪŋk/", "អ៊ីងខ៏ (i=អ៊ី + nk=ងខ៏)", "i|nk", "ទឹកខ្មៅប៊ិច", "🖋️", "This pen has blue ink inside.", "ប៊ិចនេះមានទឹកខ្មៅពណ៌ខៀវនៅខាងក្នុង។"),

            // Unit 4: Jj, Kk, Ll
            item(L, "Unit 4: Jj - Ll", "J j /dʒ/", "jam", "/dʒæm/", "ជែម (j=ជ + a=អ៊ែ + m=ម)", "j|a|m", "ដំណាប់ផ្លែឈើ", "🍓", "I put strawberry jam on my bread.", "ខ្ញុំលាបដំណាប់ស្ត្របឺរីលើនំប៉័ងរបស់ខ្ញុំ។"),
            item(L, "Unit 4: Jj - Ll", "J j /dʒ/", "jacket", "/ˈdʒæk.ɪt/", "ជែក-ឃីថ៏ (j=ជ + a=អ៊ែ + ck=ខ + et=អ៊ីថ)", "j|a|ck|et", "អាវក្រៅ / អាវរងា", "🧥", "Put on your warm jacket today.", "សូមពាក់អាវរងារបស់អ្នកនៅថ្ងៃនេះ។"),
            item(L, "Unit 4: Jj - Ll", "K k /k/", "kangaroo", "/ˌkæŋ.ɡəˈruː/", "ខែង-ហ្គា-រូ (k=ខ + a=អ៊ែ + ng=ង)", "k|an|ga|roo", "សត្វកង់ហ្គូរូ", "🦘", "A kangaroo can jump very high.", "សត្វកង់ហ្គូរូអាចលោតបានខ្ពស់ណាស់។"),
            item(L, "Unit 4: Jj - Ll", "K k /k/", "key", "/kiː/", "ឃី (k=ឃ + ey=អ៊ី)", "k|ey", "កូនសោ", "🔑", "Use this key to open the classroom door.", "ប្រើកូនសោនេះដើម្បីបើកទ្វារថ្នាក់រៀន។"),
            item(L, "Unit 4: Jj - Ll", "L l /l/", "lion", "/ˈlaɪ.ən/", "ឡាយ-អិន (l=ឡ + i=អាយ + on=អិន)", "l|i|on", "សត្វតោ", "🦁", "The lion is the king of the jungle.", "សត្វតោគឺជាស្តេចនៃព្រៃភ្នំ។"),
            item(L, "Unit 4: Jj - Ll", "L l /l/", "lemon", "/ˈlem.ən/", "ឡេ-មិន (l=ឡ + e=អ៊េ + mon=មិន)", "l|e|m|on", "ផ្លែក្រូចឆ្មារលឿង", "🍋", "Lemon juice tastes very sour.", "ទឹកក្រូចឆ្មារមានរសជាតិជូរខ្លាំង។"),

            // Unit 5: Mm, Nn, Oo
            item(L, "Unit 5: Mm - Oo", "M m /m/", "monkey", "/ˈmʌŋ.ki/", "ម៉ាំង-ឃី (m=ម + o=អាំ + nk=ងឃ + ey=អ៊ី)", "m|o|nk|ey", "សត្វស្វា", "🐵", "The funny monkey eats a yellow banana.", "សត្វស្វាដ៏ក្រមិចក្រមើមស៊ីផ្លែចេកពណ៌លឿង។"),
            item(L, "Unit 5: Mm - Oo", "M m /m/", "milk", "/mɪlk/", "មីលខ៏ (m=ម + i=អ៊ី + l=ល + k=ខ)", "m|i|l|k", "ទឹកដោះគោ", "🥛", "Drinking warm milk is good for health.", "ការផឹកទឹកដោះគោក្តៅឧណ្ហៗល្អសម្រាប់សុខភាព។"),
            item(L, "Unit 5: Mm - Oo", "N n /n/", "nest", "/nest/", "ណេសថ៏ (n=ណ + e=អ៊េ + s=ស + t=ថ)", "n|e|s|t", "សំបុកចាប", "🪺", "There are three eggs in the bird nest.", "មានស៊ុតបីគ្រាប់នៅក្នុងសំបុកបក្សី។"),
            item(L, "Unit 5: Mm - Oo", "N n /n/", "nut", "/nʌt/", "ណាត់ថ៏ (n=ណ + u=អាត់ + t=ថ)", "n|u|t", "គ្រាប់ធញ្ញជាតិ", "🥜", "The squirrel holds a small brown nut.", "សត្វកំប្រុកកាន់គ្រាប់ធញ្ញជាតិពណ៌ត្នោតតូចមួយ។"),
            item(L, "Unit 5: Mm - Oo", "O o /ɒ/", "octopus", "/ˈɒk.tə.pəs/", "អក់-ថឹ-ផឹស (o=អក់ + c=ខ + to=ថឹ + pus=ផឹស)", "o|c|to|pus", "សត្វមឹកយក្ស", "🐙", "An octopus has eight long arms.", "សត្វមឹកយក្សមានដៃវែងៗចំនួនប្រាំបី។"),
            item(L, "Unit 5: Mm - Oo", "O o /ɒ/", "ox", "/ɒks/", "អក់ស៏ (o=អក់ + x=កស៏)", "o|x", "សត្វគោឈ្មោល", "🐂", "The strong ox helps the farmer on the farm.", "គោឈ្មោលដ៏រឹងមាំជួយកសិករនៅក្នុងកសិដ្ឋាន។"),

            // Unit 6: Pp, Qq, Rr
            item(L, "Unit 6: Pp - Rr", "P p /p/", "panda", "/ˈpæn.də/", "ផែន-ដា (p=ផ + an=អ៊ែន + da=ដា)", "p|a|n|da", "សត្វខ្លាឃ្មុំផេនដា", "🐼", "The cute panda eats green bamboo.", "ខ្លាឃ្មុំផេនដាដ៏គួរឱ្យស្រឡាញ់ស៊ីឫស្សីបៃតង។"),
            item(L, "Unit 6: Pp - Rr", "P p /p/", "pen", "/pen/", "ផេន (p=ផ + e=អ៊េ + n=ន)", "p|e|n", "ប៊ិចសរសេរ", "🖊️", "Please write your name with a blue pen.", "សូមសរសេរឈ្មោះរបស់អ្នកដោយប្រើប៊ិចពណ៌ខៀវ។"),
            item(L, "Unit 6: Pp - Rr", "Q q /kw/", "queen", "/kwiːn/", "ខ្វីន (qu=ខ្វ + ee=អ៊ី + n=ន)", "qu|ee|n", "ព្រះមហាក្សត្រី", "👸", "The kind queen wears a golden crown.", "ព្រះមហាក្សត្រីដ៏សប្បុរសពាក់មកុដមាស។"),
            item(L, "Unit 6: Pp - Rr", "Q q /kw/", "quiz", "/kwɪz/", "ខ្វីហ្ស (qu=ខ្វ + i=អ៊ី + z=ហ្ស)", "qu|i|z", "ការប្រឡងសាកល្បងខ្លី", "📝", "We have an English phonics quiz today.", "យើងមានការប្រឡងប្រកបភាសាអង់គ្លេសខ្លីនៅថ្ងៃនេះ។"),
            item(L, "Unit 6: Pp - Rr", "R r /r/", "rabbit", "/ˈræb.ɪt/", "រ៉ែប-ប៊ីថ៏ (r=រ + a=អ៊ែ + bb=ប + it=អ៊ីថ)", "r|a|bb|it", "សត្វទន្សាយ", "🐰", "The white rabbit loves eating fresh carrots.", "ទន្សាយពណ៌សចូលចិត្តស៊ីការ៉ុតស្រស់ៗ។"),
            item(L, "Unit 6: Pp - Rr", "R r /r/", "rose", "/rəʊz/", "រ៉ូហ្ស (r=រ + o_e=អូ + s=ហ្ស)", "r|o|se", "ផ្កាកុលាប", "🌹", "She gave her teacher a red rose.", "នាងបានជូនផ្កាកុលាបពណ៌ក្រហមមួយទងដល់អ្នកគ្រូ។"),

            // Unit 7: Ss, Tt, Uu, Vv
            item(L, "Unit 7: Ss - Vv", "S s /s/", "sun", "/sʌn/", "សាន់ (s=ស + u=អាត់ + n=ន)", "s|u|n", "ព្រះអាទិត្យ", "☀️", "The bright sun shines in the morning sky.", "ព្រះអាទិត្យដ៏ភ្លឺចែងចាំងរះនៅលើមេឃពេលព្រឹក។"),
            item(L, "Unit 7: Ss - Vv", "T t /t/", "tiger", "/ˈtaɪ.ɡər/", "ថាយ-ហ្គឺរ (t=ថ + i=អាយ + ger=ហ្គឺរ)", "t|i|g|er", "សត្វខ្លាធំ", "🐯", "The tiger has orange and black stripes.", "សត្វខ្លាធំមានឆ្នូតពណ៌ទឹកក្រូចនិងពណ៌ខ្មៅ។"),
            item(L, "Unit 7: Ss - Vv", "U u /ʌ/", "umbrella", "/ʌmˈbrel.ə/", "អាំ-ប្រែល-ឡា (u=អាត់ + m=ម + bre=ប្រែ + lla=ឡា)", "u|m|br|e|lla", "ឆត្រ", "☂️", "Take an umbrella when it rains outside.", "សូមយកឆត្រទៅតាមខ្លួននៅពេលមេឃភ្លៀងខាងក្រៅ។"),
            item(L, "Unit 7: Ss - Vv", "V v /v/", "van", "/væn/", "វែន (v=វ + a=អ៊ែ + n=ន)", "v|a|n", "រថយន្តវែន / ឡានទូ", "🚐", "The school van brings students every morning.", "ឡានសាលាដឹកសិស្សានុសិស្សរៀងរាល់ព្រឹក។"),
            item(L, "Unit 7: Ss - Vv", "V v /v/", "violin", "/ˌvaɪəˈlɪn/", "វ៉ាយ-អឺ-លីន (v=វ + i=អាយ + o=អឺ + lin=លីន)", "v|i|o|lin", "ឧបករណ៍វីយូឡុង", "🎻", "He plays beautiful music on his violin.", "គាត់លេងភ្លេងដ៏ពិរោះដោយប្រើវីយូឡុងរបស់គាត់។"),

            // Unit 8: Ww, Xx, Yy, Zz
            item(L, "Unit 8: Ww - Zz", "W w /w/", "watch", "/wɒtʃ/", "វ៉ចឆ៏ (w=វ + a=អក់ + tch=ចឆ៏)", "w|a|tch", "នាឡិកាដៃ", "⌚", "My father bought me a new watch.", "ឪពុករបស់ខ្ញុំបានទិញនាឡិកាដៃថ្មីមួយឱ្យខ្ញុំ។"),
            item(L, "Unit 8: Ww - Zz", "W w /w/", "wolf", "/wʊlf/", "វូលហ្វ៏ (w=វ + o=អ៊ូ + l=ល + f=ហ្វ)", "w|o|l|f", "សត្វឆ្កែចចក", "🐺", "A gray wolf lives in the deep forest.", "ឆ្កែចចកពណ៌ប្រផេះរស់នៅក្នុងព្រៃជ្រៅ។"),
            item(L, "Unit 8: Ww - Zz", "X x /ks/", "fox", "/fɒks/", "ហ្វក់ស៏ (f=ហ្វ + o=អក់ + x=កស៏)", "f|o|x", "សត្វកញ្ជ្រោង", "🦊", "The clever red fox hides behind a bush.", "កញ្ជ្រោងក្រហមដ៏ឆ្លាតលាក់ខ្លួនក្រោយគុម្ពោតព្រៃ។"),
            item(L, "Unit 8: Ww - Zz", "X x /ks/", "box", "/bɒks/", "បក់ស៏ (b=ប + o=អក់ + x=កស៏)", "b|o|x", "ប្រអប់ / កេស", "📦", "Put your books inside the big box.", "សូមដាក់សៀវភៅរបស់អ្នកចូលក្នុងប្រអប់ធំ។"),
            item(L, "Unit 8: Ww - Zz", "Y y /j/", "yak", "/jæk/", "យ៉ែកខ៏ (y=យ + a=អ៊ែ + k=ខ)", "y|a|k", "សត្វគោព្រៃរោមវែង", "🦬", "A yak has long hair to stay warm.", "សត្វគោយ៉ែកមានរោមវែងដើម្បីរក្សាភាពកក់ក្តៅ។"),
            item(L, "Unit 8: Ww - Zz", "Z z /z/", "zebra", "/ˈzeb.rə/", "ហ្សេ-ប្រា (z=ហ្ស + e=អ៊េ + br=ប្រ + a=អា)", "z|e|br|a", "សត្វសេះបង្កង់", "🦓", "A zebra has black and white stripes.", "សេះបង្កង់មានឆ្នូតពណ៌ខ្មៅនិងពណ៌ស។"),
            item(L, "Unit 8: Ww - Zz", "Z z /z/", "zoo", "/zuː/", "ហ្ស៊ូ (z=ហ្ស + oo=អ៊ូ)", "z|oo", "សួនសត្វ", "🦁", "Our class visited the city zoo on Sunday.", "ថ្នាក់របស់យើងបានទៅទស្សនាសួនសត្វទីក្រុងកាលពីថ្ងៃអាទិត្យ។")
        );
        saveLevelItems(L, list);
    }

    // =========================================================================
    // LEVEL 2: SHORT VOWELS (ស្រៈសំឡេងខ្លី & ការប្រកបពាក្យ CVC) - 8 Units
    // =========================================================================
    @Transactional
    public void seedLevel2ShortVowels() {
        int L = 2;
        List<PhonicsItem> list = List.of(
            // Unit 1: Short a (-at, -an, -am)
            item(L, "Unit 1: Short a (-at, -an, -am)", "-at /æt/", "cat", "/kæt/", "ខ + អ៊ែ + ថ = ខែថ៏", "c|a|t", "សត្វឆ្មា", "🐱", "The cat sat on the mat.", "សត្វឆ្មាបានអង្គុយលើកន្ទេល។"),
            item(L, "Unit 1: Short a (-at, -an, -am)", "-at /æt/", "mat", "/mæt/", "ម + អ៊ែ + ថ = ម៉ែថ៏", "m|a|t", "កន្ទេល / កម្រាលជើង", "🧶", "Please wipe your feet on the mat.", "សូមជូតជើងរបស់អ្នកនៅលើកម្រាលជើង។"),
            item(L, "Unit 1: Short a (-at, -an, -am)", "-at /æt/", "bat", "/bæt/", "ប + អ៊ែ + ថ = បែថ៏", "b|a|t", "សត្វប្រចៀវ / ដំបងវាយកូនបាល់", "🦇", "A bat flies at night.", "សត្វប្រចៀវហើរនៅពេលយប់។"),
            item(L, "Unit 1: Short a (-at, -an, -am)", "-an /æn/", "man", "/mæn/", "ម + អ៊ែ + ន = ម៉ែន", "m|a|n", "មនុស្សប្រុស / បុរស", "👨", "The kind man helped the old lady.", "បុរសចិត្តល្អបានជួយលោកយាយចំណាស់។"),
            item(L, "Unit 1: Short a (-at, -an, -am)", "-an /æn/", "pan", "/pæn/", "ផ + អ៊ែ + ន = ផែន", "p|a|n", "ខ្ទះឆា", "🍳", "Mom cooks eggs in a hot pan.", "ម្តាយចម្អិនស៊ុតនៅក្នុងខ្ទះក្តៅ។"),
            item(L, "Unit 1: Short a (-at, -an, -am)", "-am /æm/", "ram", "/ræm/", "រ + អ៊ែ + ម = រ៉ែម", "r|a|m", "សត្វចៀមឈ្មោល", "🐏", "The ram has two big curved horns.", "សត្វចៀមឈ្មោលមានស្នែងកោងធំពីរ។"),

            // Unit 2: Short a (-ap, -ad, -ag)
            item(L, "Unit 2: Short a (-ap, -ad, -ag)", "-ap /æp/", "cap", "/kæp/", "ខ + អ៊ែ + ផ = ខែផ៏", "c|a|p", "មួកកាតឹប", "🧢", "He wears a blue cap to play sports.", "គាត់ពាក់មួកកាតឹបពណ៌ខៀវទៅលេងកីឡា។"),
            item(L, "Unit 2: Short a (-ap, -ad, -ag)", "-ap /æp/", "map", "/mæp/", "ម + អ៊ែ + ផ = ម៉ែផ៏", "m|a|p", "ផែនទី", "🗺️", "Look at the map of Cambodia.", "សូមមើលផែនទីនៃប្រទេសកម្ពុជា។"),
            item(L, "Unit 2: Short a (-ap, -ad, -ag)", "-ad /æd/", "dad", "/dæd/", "ដ + អ៊ែ + ដ = ដែដ៏", "d|a|d", "ឪពុក / ប៉ា", "👨‍👧", "My dad drives me to school every day.", "ប៉ារបស់ខ្ញុំជូនខ្ញុំទៅសាលារៀនរាល់ថ្ងៃ។"),
            item(L, "Unit 2: Short a (-ap, -ad, -ag)", "-ad /æd/", "sad", "/sæd/", "ស + អ៊ែ + ដ = សែដ៏", "s|a|d", "ក្រៀមក្រំ / ពិបាកចិត្ត", "😢", "Do not feel sad when you make a mistake.", "កុំពិបាកចិត្តនៅពេលអ្នកធ្វើខុស។"),
            item(L, "Unit 2: Short a (-ap, -ad, -ag)", "-ag /æɡ/", "bag", "/bæɡ/", "ប + អ៊ែ + ហ្គ = បែហ្គ", "b|a|g", "កាតាប / ថង់", "🎒", "She puts her English books in her school bag.", "នាងដាក់សៀវភៅអង់គ្លេសក្នុងកាតាបសិក្សារបស់នាង។"),

            // Unit 3: Short e (-et, -en, -ed)
            item(L, "Unit 3: Short e (-et, -en, -ed)", "-et /et/", "jet", "/dʒet/", "ជ + អ៊េ + ថ = ជេថ៏", "j|e|t", "យន្តហោះប្រតិកម្ម", "✈️", "A fast jet flies high above the clouds.", "យន្តហោះប្រតិកម្មដ៏លឿនហោះខ្ពស់លើពពក។"),
            item(L, "Unit 3: Short e (-et, -en, -ed)", "-et /et/", "net", "/net/", "ណ + អ៊េ + ថ = ណេថ៏", "n|e|t", "សំណាញ់ / មុង", "🥅", "Kick the ball into the goal net.", "ទាត់បាល់ចូលទៅក្នុងសំណាញ់ទី។"),
            item(L, "Unit 3: Short e (-et, -en, -ed)", "-en /en/", "hen", "/hen/", "ហ + អ៊េ + ន = ហេន", "h|e|n", "មេមាន់", "🐔", "The brown hen laid a fresh egg.", "មេមាន់ពណ៌ត្នោតបានពងស៊ុតស្រស់មួយគ្រាប់។"),
            item(L, "Unit 3: Short e (-et, -en, -ed)", "-en /en/", "ten", "/ten/", "ថ + អ៊េ + ន = ថេន", "t|e|n", "លេខដប់ (១០)", "🔟", "I can count from one to ten in English.", "ខ្ញុំអាចរាប់ពីមួយដល់ដប់ជាភាសាអង់គ្លេសបាន។"),
            item(L, "Unit 3: Short e (-et, -en, -ed)", "-ed /ed/", "bed", "/bed/", "ប + អ៊េ + ដ = បេដ៏", "b|e|d", "គ្រែដេក", "🛏️", "I make my bed every morning.", "ខ្ញុំរៀបចំគ្រែដេករបស់ខ្ញុំរៀងរាល់ព្រឹក។"),
            item(L, "Unit 3: Short e (-et, -en, -ed)", "-ed /ed/", "red", "/red/", "រ + អ៊េ + ដ = រ៉េដ៏", "r|e|d", "ពណ៌ក្រហម", "🔴", "She loves her bright red dress.", "នាងស្រឡាញ់រ៉ូបពណ៌ក្រហមភ្លឺរបស់នាង។"),

            // Unit 4: Short i (-it, -in, -ig)
            item(L, "Unit 4: Short i (-it, -in, -ig)", "-it /ɪt/", "sit", "/sɪt/", "ស + អ៊ី + ថ = ស៊ីថ៏", "s|i|t", "អង្គុយចុះ", "🪑", "Please sit down quietly in class.", "សូមអង្គុយចុះដោយស្ងៀមស្ងាត់ក្នុងថ្នាក់រៀន។"),
            item(L, "Unit 4: Short i (-it, -in, -ig)", "-in /ɪn/", "pin", "/pɪn/", "ផ + អ៊ី + ន = ភីន", "p|i|n", "ម្ជុលខ្ទាស់", "🧷", "Be careful with the sharp metal pin.", "សូមប្រយ័ត្នជាមួយម្ជុលដែកដ៏មុតស្រួច។"),
            item(L, "Unit 4: Short i (-it, -in, -ig)", "-in /ɪn/", "win", "/wɪn/", "វ + អ៊ី + ន = វីន", "w|i|n", "ឈ្នះ", "🏆", "Work hard so our team can win the game.", "ខិតខំប្រឹងប្រែងដើម្បីឱ្យក្រុមយើងអាចឈ្នះការប្រកួត។"),
            item(L, "Unit 4: Short i (-it, -in, -ig)", "-ig /ɪɡ/", "pig", "/pɪɡ/", "ផ + អ៊ី + ហ្គ = ភីហ្គ", "p|i|g", "សត្វជ្រូក", "🐷", "The pink pig rolls in the mud.", "សត្វជ្រូកពណ៌ផ្កាឈូកននៀលក្នុងភក់។"),
            item(L, "Unit 4: Short i (-it, -in, -ig)", "-ig /ɪɡ/", "big", "/bɪɡ/", "ប + អ៊ី + ហ្គ = ប៊ីហ្គ", "b|i|g", "ធំ", "🐘", "Our school has a very big library.", "សាលារៀនរបស់យើងមានបណ្ណាល័យដ៏ធំមួយ។"),

            // Unit 5: Short i (-ip, -id, -ix)
            item(L, "Unit 5: Short i (-ip, -id, -ix)", "-ip /ɪp/", "lip", "/lɪp/", "ល + អ៊ី + ផ = លីផ៏", "l|i|p", "បបូរមាត់", "👄", "Touch your upper lip when saying this sound.", "ប៉ះបបូរមាត់លើរបស់អ្នកនៅពេលបញ្ចេញសំឡេងនេះ។"),
            item(L, "Unit 5: Short i (-ip, -id, -ix)", "-ip /ɪp/", "zip", "/zɪp/", "ហ្ស + អ៊ី + ផ = ហ្ស៊ីផ៏", "z|i|p", "រូតកាតាប / រូតអាវ", "🤐", "Please zip up your school bag.", "សូមរូតបិទកាតាបសិក្សារបស់អ្នក។"),
            item(L, "Unit 5: Short i (-ip, -id, -ix)", "-id /ɪd/", "kid", "/kɪd/", "ខ + អ៊ី + ដ = ឃីដ៏", "k|i|d", "ក្មេងតូច / កូនពពែ", "🧒", "Every kid in the class loves learning phonics.", "ក្មេងៗគ្រប់គ្នាក្នុងថ្នាក់ចូលចិត្តរៀនប្រកបអង់គ្លេស។"),
            item(L, "Unit 5: Short i (-ip, -id, -ix)", "-id /ɪd/", "lid", "/lɪd/", "ល + អ៊ី + ដ = លីដ៏", "l|i|d", "គម្របឆ្នាំង / គម្របប្រអប់", "🍲", "Put the lid on the cooking pot.", "សូមគ្របគម្របលើឆ្នាំងសម្ល។"),
            item(L, "Unit 5: Short i (-ip, -id, -ix)", "-ix /ɪks/", "six", "/sɪks/", "ស + អ៊ី + កស៏ = ស៊ីកស៏", "s|i|x", "លេខប្រាំមួយ (៦)", "6️⃣", "There are six chairs around the table.", "មានកៅអីប្រាំមួយនៅជុំវិញតុ។"),

            // Unit 6: Short o (-ot, -op, -og)
            item(L, "Unit 6: Short o (-ot, -op, -og)", "-ot /ɒt/", "pot", "/pɒt/", "ផ + អក់ + ថ = ផត់ថ៏", "p|o|t", "ឆ្នាំង", "🍲", "The soup in the pot smells delicious.", "ស៊ុបនៅក្នុងឆ្នាំងមានក្លិនឈ្ងុយឆ្ងាញ់ណាស់។"),
            item(L, "Unit 6: Short o (-ot, -op, -og)", "-ot /ɒt/", "hot", "/hɒt/", "ហ + អក់ + ថ = ហត់ថ៏", "h|o|t", "ក្តៅ", "🔥", "Be careful, the water is very hot.", "សូមប្រយ័ត្ន ទឹកនេះក្តៅខ្លាំងណាស់។"),
            item(L, "Unit 6: Short o (-ot, -op, -og)", "-op /ɒp/", "mop", "/mɒp/", "ម + អក់ + ផ = ម៉ប់ផ៏", "m|o|p", "ប្រដាប់ជូតឥដ្ឋ", "🧹", "Use a wet mop to clean the classroom floor.", "ប្រើប្រដាប់ជូតឥដ្ឋសើមដើម្បីសម្អាតការ៉ូថ្នាក់រៀន។"),
            item(L, "Unit 6: Short o (-ot, -op, -og)", "-op /ɒp/", "hop", "/hɒp/", "ហ + អក់ + ផ = ហប់ផ៏", "h|o|p", "លោតជើងមួយ / លោតតិចៗ", "🐇", "Rabbits hop across the green field.", "សត្វទន្សាយលោតកាត់វាលស្មៅពណ៌បៃតង។"),
            item(L, "Unit 6: Short o (-ot, -op, -og)", "-og /ɒɡ/", "log", "/lɒɡ/", "ល + អក់ + ហ្គ = ឡក់ហ្គ", "l|o|g", "គល់ឈើ / កំណាត់ឈើ", "🪵", "A green frog sits on a brown log.", "កង្កែបពណ៌បៃតងអង្គុយលើកំណាត់ឈើពណ៌ត្នោត។"),

            // Unit 7: Short u (-ug, -un, -ut)
            item(L, "Unit 7: Short u (-ug, -un, -ut)", "-ug /ʌɡ/", "bug", "/bʌɡ/", "ប + អាត់ + ហ្គ = បាក់ហ្គ", "b|u|g", "សត្វល្អិត", "🐞", "A small red bug walked on the flower.", "សត្វល្អិតពណ៌ក្រហមតូចមួយវារលើផ្កា។"),
            item(L, "Unit 7: Short u (-ug, -un, -ut)", "-ug /ʌɡ/", "mug", "/mʌɡ/", "ម + អាត់ + ហ្គ = ម៉ាក់ហ្គ", "m|u|g", "កែវធំមានដៃកាន់", "🍺", "He poured warm milk into his mug.", "គាត់បានចាក់ទឹកដោះគោក្តៅឧណ្ហៗចូលក្នុងកែវធំរបស់គាត់។"),
            item(L, "Unit 7: Short u (-ug, -un, -ut)", "-un /ʌn/", "run", "/rʌn/", "រ + អាត់ + ន = រ៉ាន់", "r|u|n", "រត់", "🏃", "Students run happily in the schoolyard.", "សិស្សានុសិស្សរត់លេងយ៉ាងសប្បាយក្នុងទីធ្លាសាលា។"),
            item(L, "Unit 7: Short u (-ug, -un, -ut)", "-un /ʌn/", "fun", "/fʌn/", "ហ្វ + អាត់ + ន = ហ្វាន់", "f|u|n", "ភាពសប្បាយរីករាយ", "🎉", "Learning English phonics is so much fun!", "ការរៀនប្រកបភាសាអង់គ្លេសគឺពិតជាសប្បាយណាស់!"),
            item(L, "Unit 7: Short u (-ug, -un, -ut)", "-ut /ʌt/", "cut", "/kʌt/", "ខ + អាត់ + ថ = ខាត់ថ៏", "c|u|t", "កាត់ / ហាន់", "✂️", "Use scissors to cut the paper carefully.", "ប្រើកន្ត្រៃដើម្បីកាត់ក្រដាសដោយប្រុងប្រយ័ត្ន។"),

            // Unit 8: Short u (-ub, -up, -um)
            item(L, "Unit 8: Short u (-ub, -up, -um)", "-ub /ʌb/", "tub", "/tʌb/", "ថ + អាត់ + ប = ថាប់ប៏", "t|u|b", "អាងងូតទឹក", "🛁", "The baby takes a bath in a warm tub.", "ទារកងូតទឹកនៅក្នុងអាងទឹកក្តៅឧណ្ហៗ។"),
            item(L, "Unit 8: Short u (-ub, -up, -um)", "-ub /ʌb/", "cub", "/kʌb/", "ខ + អាត់ + ប = ខាប់ប៏", "c|u|b", "កូនខ្លា / កូនខ្លាឃ្មុំ", "🦁", "The lion cub plays near its mother.", "កូនតោលេងនៅជិតមេរបស់វា។"),
            item(L, "Unit 8: Short u (-ub, -up, -um)", "-up /ʌp/", "pup", "/pʌp/", "ផ + អាត់ + ផ = ផាប់ផ៏", "p|u|p", "កូនឆ្កែតូច", "🐕", "The little pup wags its tail happily.", "កូនឆ្កែតូចបក់កន្ទុយយ៉ាងសប្បាយរីករាយ។"),
            item(L, "Unit 8: Short u (-ub, -up, -um)", "-um /ʌm/", "gum", "/ɡʌm/", "ហ្គ + អាត់ + ម = ហ្គាំ", "g|u|m", "ស្ករកៅស៊ូ / អញ្ចាញធ្មេញ", "🍬", "Do not chew gum during the lesson.", "កុំទំពាស្ករកៅស៊ូក្នុងពេលកំពុងរៀន។")
        );
        saveLevelItems(L, list);
    }

    // =========================================================================
    // LEVEL 3: LONG VOWELS (ស្រៈសំឡេងវែង Magic E & Vowel Teams) - 8 Units
    // =========================================================================
    @Transactional
    public void seedLevel3LongVowels() {
        int L = 3;
        List<PhonicsItem> list = List.of(
            // Unit 1: Long a (a_e: -ake, -ame, -ate, -ave)
            item(L, "Unit 1: Long a (Magic e: a_e)", "a_e /eɪ/", "cake", "/keɪk/", "ខ + អេ + ខ = ខេកខ៏ (e ចុងពាក្យមិនបញ្ចេញសំឡេង)", "c|a|k|e", "នំខេក", "🎂", "We baked a chocolate cake for her birthday.", "យើងបានដុតនំខេកសូកូឡាសម្រាប់ថ្ងៃខួបកំណើតរបស់នាង។"),
            item(L, "Unit 1: Long a (Magic e: a_e)", "a_e /eɪ/", "lake", "/leɪk/", "ល + អេ + ខ = ឡេកខ៏", "l|a|k|e", "បឹង", "🏞️", "The water in the lake is very calm and clear.", "ទឹកនៅក្នុងបឹងមានភាពស្ងប់និងថ្លាឈ្វេង។"),
            item(L, "Unit 1: Long a (Magic e: a_e)", "a_e /eɪ/", "name", "/neɪm/", "ណ + អេ + ម = ណេម", "n|a|m|e", "ឈ្មោះ", "📛", "Please write your full name on the paper.", "សូមសរសេរឈ្មោះពេញរបស់អ្នកនៅលើក្រដាស។"),
            item(L, "Unit 1: Long a (Magic e: a_e)", "a_e /eɪ/", "game", "/ɡeɪm/", "ហ្គ + អេ + ម = ហ្គេម", "g|a|m|e", "ល្បែងសិក្សា / ការប្រកួត", "🎮", "Let us play a word spelling game together.", "តោះយើងលេងល្បែងប្រកបពាក្យទាំងអស់គ្នា។"),
            item(L, "Unit 1: Long a (Magic e: a_e)", "a_e /eɪ/", "gate", "/ɡeɪt/", "ហ្គ + អេ + ថ = ហ្គេថ៏", "g|a|t|e", "ទ្វាររបងសាលា", "⛩️", "Meet me at the school gate at seven o'clock.", "ជួបខ្ញុំនៅមាត់ទ្វាររបងសាលានៅម៉ោងប្រាំពីរ។"),

            // Unit 2: Long i (i_e: -ime, -ine, -ike, -ive)
            item(L, "Unit 2: Long i (Magic e: i_e)", "i_e /aɪ/", "bike", "/baɪk/", "ប + អាយ + ខ = បាយខ៏", "b|i|k|e", "កង់ជិះ", "🚲", "I ride my bike to school every morning.", "ខ្ញុំជិះកង់របស់ខ្ញុំទៅសាលារៀនរៀងរាល់ព្រឹក។"),
            item(L, "Unit 2: Long i (Magic e: i_e)", "i_e /aɪ/", "kite", "/kaɪt/", "ខ + អាយ + ថ = ខាយថ៏", "k|i|t|e", "ខ្លែងបង្ហោះ", "🪁", "The colorful kite flies high in the wind.", "ខ្លែងចម្រុះពណ៌ហោះខ្ពស់នៅក្នុងខ្យល់។"),
            item(L, "Unit 2: Long i (Magic e: i_e)", "i_e /aɪ/", "time", "/taɪm/", "ថ + អាយ + ម = ថាយម៏", "t|i|m|e", "ពេលវេលា / ម៉ោង", "⏰", "It is time to start our English lesson.", "ដល់ម៉ោងចាប់ផ្តើមមេរៀនភាសាអង់គ្លេសរបស់យើងហើយ។"),
            item(L, "Unit 2: Long i (Magic e: i_e)", "i_e /aɪ/", "nine", "/naɪn/", "ណ + អាយ + ន = ណាយន៏", "n|i|n|e", "លេខប្រាំបួន (៩)", "9️⃣", "There are nine planets in the story book.", "មានភពចំនួនប្រាំបួននៅក្នុងសៀវភៅរឿង។"),
            item(L, "Unit 2: Long i (Magic e: i_e)", "i_e /aɪ/", "five", "/faɪv/", "ហ្វ + អាយ + វ = ហ្វាយវ៏", "f|i|v|e", "លេខប្រាំ (៥)", "5️⃣", "We have five fingers on each hand.", "យើងមានម្រាមដៃប្រាំនៅលើដៃនីមួយៗ។"),

            // Unit 3: Long o & u (o_e, u_e)
            item(L, "Unit 3: Long o & u (o_e, u_e)", "o_e /əʊ/", "home", "/həʊm/", "ហ + អូ + ម = ហូម", "h|o|m|e", "ផ្ទះ / គេហដ្ឋាន", "🏠", "I practice reading English at home.", "ខ្ញុំហាត់អានភាសាអង់គ្លេសនៅផ្ទះ។"),
            item(L, "Unit 3: Long o & u (o_e, u_e)", "o_e /əʊ/", "bone", "/bəʊn/", "ប + អូ + ន = បូន", "b|o|n|e", "ឆ្អឹង", "🦴", "The puppy buried a bone in the garden.", "កូនឆ្កែបានកប់ឆ្អឹងមួយនៅក្នុងសួនច្បារ។"),
            item(L, "Unit 3: Long o & u (o_e, u_e)", "o_e /əʊ/", "rope", "/rəʊp/", "រ + អូ + ផ = រ៉ូផ៏", "r|o|p|e", "ខ្សែពួរ", "🪢", "The girls love jumping rope at break time.", "ក្មេងស្រីៗចូលចិត្តលោតខ្សែពួរនៅម៉ោងចេញលេង។"),
            item(L, "Unit 3: Long o & u (o_e, u_e)", "u_e /juː/", "cube", "/kjuːb/", "ខ + យូ + ប = ឃ្យូប", "c|u|b|e", "ដុំគូប / ដុំទឹកកកជ្រុង", "🧊", "Put an ice cube in the glass of water.", "ដាក់ដុំទឹកកកមួយដុំចូលក្នុងកែវទឹក។"),
            item(L, "Unit 3: Long o & u (o_e, u_e)", "u_e /juː/", "tube", "/tjuːb/", "ថ + យូ + ប = ធ្យូប", "t|u|b|e", "បំពង់ / ទីប", "🧪", "Squeeze toothpaste from the tube.", "ច្របាច់ថ្នាំដុសធ្មេញចេញពីបំពង់។"),

            // Unit 4: Vowel Teams ai, ay (/eɪ/)
            item(L, "Unit 4: Vowel Teams (ai, ay)", "ai /eɪ/", "rain", "/reɪn/", "រ + អេ + ន = រ៉េន", "r|ai|n", "ទឹកភ្លៀង", "🌧️", "Heavy rain falls on the green rice fields.", "ភ្លៀងធ្លាក់ខ្លាំងមកលើវាលស្រែពណ៌បៃតង។"),
            item(L, "Unit 4: Vowel Teams (ai, ay)", "ai /eɪ/", "train", "/treɪn/", "ត្រ + អេ + ន = ត្រេន", "tr|ai|n", "រថភ្លើង", "🚆", "The long train travels across the country.", "រថភ្លើងដ៏វែងធ្វើដំណើរឆ្លងកាត់ប្រទេស។"),
            item(L, "Unit 4: Vowel Teams (ai, ay)", "ai /eɪ/", "snail", "/sneɪl/", "ស្ន + អេ + ល = ស្នេល", "sn|ai|l", "សត្វខ្យង", "🐌", "A small snail moves very slowly.", "សត្វខ្យងតូចមួយវារយឺតៗណាស់។"),
            item(L, "Unit 4: Vowel Teams (ai, ay)", "ay /eɪ/", "play", "/pleɪ/", "ផ្ល + អេ = ផ្លេ", "pl|ay", "លេង / កម្សាន្ត", "⚽", "We play football together after school.", "យើងលេងបាល់ទាត់ជាមួយគ្នាក្រោយម៉ោងរៀន។"),
            item(L, "Unit 4: Vowel Teams (ai, ay)", "ay /eɪ/", "day", "/deɪ/", "ដ + អេ = ដេ", "d|ay", "ថ្ងៃ", "🌤️", "Today is a wonderful sunny day.", "ថ្ងៃនេះគឺជាថ្ងៃដែលមានពន្លឺថ្ងៃដ៏ស្រស់បំព្រង។"),

            // Unit 5: Vowel Teams ee, ea (/iː/)
            item(L, "Unit 5: Vowel Teams (ee, ea)", "ee /iː/", "tree", "/triː/", "ត្រ + អ៊ី = ទ្រី", "tr|ee", "ដើមឈើ", "🌳", "Planting a tree helps protect our earth.", "ការដាំដើមឈើជួយការពារផែនដីរបស់យើង។"),
            item(L, "Unit 5: Vowel Teams (ee, ea)", "ee /iː/", "bee", "/biː/", "ប + អ៊ី = ប៊ី", "b|ee", "សត្វឃ្មុំ", "🐝", "A busy bee collects sweet nectar from flowers.", "សត្វឃ្មុំដ៏ឧស្សាហ៍ក្រេបទឹកដមផ្អែមពីផ្កា។"),
            item(L, "Unit 5: Vowel Teams (ee, ea)", "ee /iː/", "seed", "/siːd/", "ស + អ៊ី + ដ = ស៊ីដ៏", "s|ee|d", "គ្រាប់ពូជរុក្ខជាតិ", "🌱", "Water the seed and it will grow into a plant.", "ស្រោចទឹកគ្រាប់ពូជ នោះវានឹងដុះក្លាយជារុក្ខជាតិ។"),
            item(L, "Unit 5: Vowel Teams (ee, ea)", "ea /iː/", "leaf", "/liːf/", "ល + អ៊ី + ហ្វ = លីហ្វ៏", "l|ea|f", "ស្លឹកឈើ", "🍃", "A green leaf fell into the clear stream.", "ស្លឹកឈើពណ៌បៃតងមួយសន្លឹកបានធ្លាក់ចូលក្នុងកូនអូរថ្លា។"),
            item(L, "Unit 5: Vowel Teams (ee, ea)", "ea /iː/", "read", "/riːd/", "រ + អ៊ី + ដ = រ៉ីដ៏", "r|ea|d", "អានសៀវភៅ", "📖", "I read English stories every evening.", "ខ្ញុំអានរឿងភាសាអង់គ្លេសរៀងរាល់ល្ងាច។"),

            // Unit 6: Long i Teams (igh, ie, y)
            item(L, "Unit 6: Long i Teams (igh, ie, y)", "igh /aɪ/", "night", "/naɪt/", "ណ + អាយ + ថ = ណាយថ៏", "n|igh|t", "ពេលយប់", "🌙", "The stars shine brightly in the night sky.", "ផ្កាយរះភ្លឺចែងចាំងនៅលើមេឃពេលយប់។"),
            item(L, "Unit 6: Long i Teams (igh, ie, y)", "igh /aɪ/", "light", "/laɪt/", "ល + អាយ + ថ = ឡាយថ៏", "l|igh|t", "ពន្លឺ / អំពូលភ្លើង", "💡", "Please turn off the light before leaving.", "សូមបិទអំពូលភ្លើងមុនពេលចាកចេញ។"),
            item(L, "Unit 6: Long i Teams (igh, ie, y)", "ie /aɪ/", "pie", "/paɪ/", "ផ + អាយ = ផាយ", "p|ie", "នំផាយ", "🥧", "Grandma baked a sweet apple pie.", "លោកយាយបានដុតនំផាយផ្លែប៉ោមដ៏ផ្អែមឈ្ងុយ។"),
            item(L, "Unit 6: Long i Teams (igh, ie, y)", "-y /aɪ/", "sky", "/skaɪ/", "ស្ក + អាយ = ស្កាយ", "sk|y", "មេឃ", "🌌", "Birds fly high in the blue sky.", "បក្សីហើរខ្ពស់នៅលើមេឃពណ៌ខៀវ។"),
            item(L, "Unit 6: Long i Teams (igh, ie, y)", "-y /aɪ/", "fly", "/flaɪ/", "ហ្វ្ល + អាយ = ហ្វ្លាយ", "fl|y", "ហើរ / ហោះ", "🦅", "Eagles can fly above high mountains.", "សត្វឥន្ទ្រីអាចហើរពីលើភ្នំខ្ពស់ៗ។"),

            // Unit 7: Long o Teams (oa, ow)
            item(L, "Unit 7: Long o Teams (oa, ow)", "oa /əʊ/", "boat", "/bəʊt/", "ប + អូ + ថ = បូថ៏", "b|oa|t", "ទូក", "⛵", "A fishing boat sails on the Mekong River.", "ទូកនេសាទមួយគ្រឿងបើកបរលើទន្លេមេគង្គ។"),
            item(L, "Unit 7: Long o Teams (oa, ow)", "oa /əʊ/", "coat", "/kəʊt/", "ខ + អូ + ថ = ខូថ៏", "c|oa|t", "អាវធំ / អាវភ្លៀង", "🧥", "Wear a raincoat when you walk in the rain.", "សូមពាក់អាវភ្លៀងនៅពេលអ្នកដើរក្នុងភ្លៀង។"),
            item(L, "Unit 7: Long o Teams (oa, ow)", "oa /əʊ/", "road", "/rəʊd/", "រ + អូ + ដ = រ៉ូដ៏", "r|oa|d", "ផ្លូវថ្នល់", "🛣️", "Look both ways before crossing the road.", "សូមមើលឆ្វេងស្តាំមុនពេលឆ្លងផ្លូវថ្នល់។"),
            item(L, "Unit 7: Long o Teams (oa, ow)", "ow /əʊ/", "snow", "/snəʊ/", "ស្ន + អូ = ស្នូ", "sn|ow", "ព្រិល", "❄️", "White snow covers the mountain top.", "ព្រិលពណ៌សគ្របដណ្តប់លើកំពូលភ្នំ។"),
            item(L, "Unit 7: Long o Teams (oa, ow)", "ow /əʊ/", "window", "/ˈwɪn.dəʊ/", "វីន-ដូ (w+i+n = វីន, d+ow = ដូ)", "w|i|n|d|ow", "បង្អួច", "🪟", "Open the window to let fresh air in.", "បើកបង្អួចដើម្បីឱ្យខ្យល់បរិសុទ្ធចូលមកក្នុង។"),

            // Unit 8: Long u Teams (oo, ue, ui)
            item(L, "Unit 8: Long u Teams (oo, ue, ui)", "oo /uː/", "moon", "/muːn/", "ម + អ៊ូ + ន = មូន", "m|oo|n", "ព្រះចន្ទ", "🌕", "The full moon is bright tonight.", "ព្រះចន្ទពេញវង់ភ្លឺច្បាស់ណាស់នៅយប់នេះ។"),
            item(L, "Unit 8: Long u Teams (oo, ue, ui)", "oo /uː/", "school", "/skuːl/", "ស្គ + អ៊ូ + ល = ស្គូល", "sch|oo|l", "សាលារៀន", "🏫", "We arrive at school on time every day.", "យើងមកដល់សាលារៀនទាន់ពេលវេលារៀងរាល់ថ្ងៃ។"),
            item(L, "Unit 8: Long u Teams (oo, ue, ui)", "oo /uː/", "spoon", "/spuːn/", "ស្ព + អ៊ូ + ន = ស្ពូន", "sp|oo|n", "ស្លាបព្រា", "🥄", "Eat your warm soup with a clean spoon.", "ញ៉ាំស៊ុបក្តៅរបស់អ្នកជាមួយស្លាបព្រាស្អាត។"),
            item(L, "Unit 8: Long u Teams (oo, ue, ui)", "ue /uː/", "blue", "/bluː/", "ប្ល + អ៊ូ = ប៊្លូ", "bl|ue", "ពណ៌ខៀវ", "🔵", "Our school uniform has a blue shirt.", "ឯកសណ្ឋានសាលាយើងមានអាវពណ៌ខៀវ។"),
            item(L, "Unit 8: Long u Teams (oo, ue, ui)", "ui /uː/", "fruit", "/fruːt/", "ហ្វ្រ + អ៊ូ + ថ = ហ្វ្រូថ៏", "fr|ui|t", "ផ្លែឈើ", "🍉", "Eating fresh fruit keeps your body healthy.", "ការញ៉ាំផ្លែឈើស្រស់ធ្វើឱ្យរាងកាយអ្នកមានសុខភាពល្អ។")
        );
        saveLevelItems(L, list);
    }

    // =========================================================================
    // LEVEL 4: CONSONANT BLENDS & DIGRAPHS (ព្យញ្ជនៈផ្សំ និងសូរពិសេស) - 8 Units
    // =========================================================================
    @Transactional
    public void seedLevel4ConsonantBlends() {
        int L = 4;
        List<PhonicsItem> list = List.of(
            // Unit 1: L-Blends (bl, cl, fl, gl, pl, sl)
            item(L, "Unit 1: L-Blends (bl, cl, fl, gl, pl)", "bl- /bl/", "black", "/blæk/", "ប្ល + អ៊ែ + ខ = ប្លែកខ៏", "bl|a|ck", "ពណ៌ខ្មៅ", "⚫", "He writes on the whiteboard with a black marker.", "គាត់សរសេរលើក្តារខៀនដោយប្រើហ្វឺតពណ៌ខ្មៅ។"),
            item(L, "Unit 1: L-Blends (bl, cl, fl, gl, pl)", "cl- /kl/", "clock", "/klɒk/", "ខ្ល + អក់ + ខ = ខ្លក់ខ៏", "cl|o|ck", "នាឡិកាព្យួរជញ្ជាំង", "🕰️", "Look at the wall clock to check the time.", "មើលនាឡិកាជញ្ជាំងដើម្បីពិនិត្យមើលម៉ោង។"),
            item(L, "Unit 1: L-Blends (bl, cl, fl, gl, pl)", "fl- /fl/", "flag", "/flæɡ/", "ហ្វ្ល + អ៊ែ + ហ្គ = ហ្វ្លែហ្គ", "fl|a|g", "ទង់ជាតិ", "🇰🇭", "Students salute the national flag every morning.", "សិស្សានុសិស្សគោរពទង់ជាតិរៀងរាល់ព្រឹក។"),
            item(L, "Unit 1: L-Blends (bl, cl, fl, gl, pl)", "gl- /ɡl/", "globe", "/ɡləʊb/", "ហ្គ្ល + អូ + ប = ហ្គ្លូប", "gl|o|b|e", "ផែនដីមូល (សកលលោក)", "🌍", "The teacher showed Cambodia on the world globe.", "លោកគ្រូបានបង្ហាញប្រទេសកម្ពុជានៅលើភពផែនដីមូល។"),
            item(L, "Unit 1: L-Blends (bl, cl, fl, gl, pl)", "pl- /pl/", "plane", "/pleɪn/", "ផ្ល + អេ + ន = ផ្លេន", "pl|a|n|e", "យន្តហោះ", "🛩️", "The plane landed safely at the airport.", "យន្តហោះបានចុះចតដោយសុវត្ថិភាពនៅព្រលានយន្តហោះ។"),

            // Unit 2: R-Blends (br, cr, dr, fr, gr, tr)
            item(L, "Unit 2: R-Blends (br, cr, dr, fr, gr, tr)", "br- /br/", "bread", "/bred/", "ប្រ + អ៊េ + ដ = ប្រេដ៏", "br|ea|d", "នំប៉័ង", "🍞", "Freshly baked bread smells very good.", "នំប៉័ងទើបតែដុតថ្មីៗមានក្លិនឈ្ងុយណាស់។"),
            item(L, "Unit 2: R-Blends (br, cr, dr, fr, gr, tr)", "cr- /kr/", "crab", "/kræb/", "ក្រ + អ៊ែ + ប = ក្រែប", "cr|a|b", "សត្វក្តាម", "🦀", "A red crab walks sideways on the sand.", "ក្តាមពណ៌ក្រហមដើរទទឹងនៅលើឆ្នេរខ្សាច់។"),
            item(L, "Unit 2: R-Blends (br, cr, dr, fr, gr, tr)", "dr- /dr/", "drum", "/drʌm/", "ដ្រ + អាត់ + ម = ដ្រាំ", "dr|u|m", "ស្គរ", "🥁", "He beats the big drum in the school band.", "គាត់វាយស្គរធំនៅក្នុងក្រុមតន្ត្រីសាលា។"),
            item(L, "Unit 2: R-Blends (br, cr, dr, fr, gr, tr)", "fr- /fr/", "frog", "/frɒɡ/", "ហ្វ្រ + អក់ + ហ្គ = ហ្វ្រក់ហ្គ", "fr|o|g", "សត្វកង្កែប", "🐸", "The green frog jumped into the lotus pond.", "កង្កែបពណ៌បៃតងបានលោតចូលក្នុងស្រះឈូក។"),
            item(L, "Unit 2: R-Blends (br, cr, dr, fr, gr, tr)", "gr- /ɡr/", "grass", "/ɡrɑːs/", "ហ្គ្រ + អា + ស = ហ្គ្រាស", "gr|a|ss", "វាលស្មៅ", "🌿", "Cows eat fresh green grass in the field.", "សត្វគោស៊ីស្មៅបៃតងស្រស់នៅក្នុងវាល។"),

            // Unit 3: S-Blends (sm, sn, sp, st, sw)
            item(L, "Unit 3: S-Blends (sm, sn, sp, st, sw)", "sm- /sm/", "smile", "/smaɪl/", "ស្ម + អាយ + ល = ស្មាយល៏", "sm|i|l|e", "ញញឹម", "😊", "Our teacher greets us with a warm smile.", "អ្នកគ្រូស្វាគមន៍ពួកយើងដោយស្នាមញញឹមដ៏កក់ក្តៅ។"),
            item(L, "Unit 3: S-Blends (sm, sn, sp, st, sw)", "sn- /sn/", "snake", "/sneɪk/", "ស្ន + អេ + ខ = ស្នេកខ៏", "sn|a|k|e", "សត្វពស់", "🐍", "Stay away from the snake in the tall grass.", "សូមនៅឱ្យឆ្ងាយពីសត្វពស់នៅក្នុងស្មៅខ្ពស់ៗ។"),
            item(L, "Unit 3: S-Blends (sm, sn, sp, st, sw)", "sp- /sp/", "speak", "/spiːk/", "ស្ព + អ៊ី + ខ = ស្ពីកខ៏", "sp|ea|k", "និយាយ", "🗣️", "Practice every day to speak English fluently.", "ហាត់អនុវត្តរាល់ថ្ងៃដើម្បីនិយាយភាសាអង់គ្លេសឱ្យបានស្ទាត់ជំនាញ។"),
            item(L, "Unit 3: S-Blends (sm, sn, sp, st, sw)", "st- /st/", "star", "/stɑːr/", "ស្ត + អារ = ស្តារ", "st|ar", "ផ្កាយ", "⭐", "You earned a gold star for reading well!", "អ្នកទទួលបានផ្កាយមាសមួយសម្រាប់ការអានបានល្អ!"),
            item(L, "Unit 3: S-Blends (sm, sn, sp, st, sw)", "sw- /sw/", "swim", "/swɪm/", "ស្វ + អ៊ី + ម = ស្វីម", "sw|i|m", "ហែលទឹក", "🏊", "Ducks know how to swim very well.", "សត្វទាចេះហែលទឹកបានយ៉ាងស្ទាត់។"),

            // Unit 4: Digraphs sh, ch, tch
            item(L, "Unit 4: Digraphs (sh, ch, tch)", "sh /ʃ/", "ship", "/ʃɪp/", "ស៊ + អ៊ី + ផ = ស៊ីផ៏", "sh|i|p", "កប៉ាល់ធំ", "🚢", "The large ship sails across the blue ocean.", "កប៉ាល់ធំបើកបរឆ្លងកាត់មហាសមុទ្រពណ៌ខៀវ។"),
            item(L, "Unit 4: Digraphs (sh, ch, tch)", "sh /ʃ/", "shell", "/ʃel/", "ស៊ + អ៊េ + ល = ស៊ែល", "sh|e|ll", "សំបកខ្យងសមុទ្រ", "🐚", "She found a pretty pink shell on the beach.", "នាងបានរកឃើញសំបកខ្យងពណ៌ផ្កាឈូកដ៏ស្អាតនៅលើឆ្នេរ។"),
            item(L, "Unit 4: Digraphs (sh, ch, tch)", "ch /tʃ/", "chair", "/tʃeər/", "ឆ + អ៊ែរ = ឆែរ", "ch|air", "កៅអីអង្គុយ", "🪑", "Push your chair in neatly after class.", "សូមរុញកៅអីរបស់អ្នកចូលឱ្យមានរបៀបក្រោយម៉ោងរៀន។"),
            item(L, "Unit 4: Digraphs (sh, ch, tch)", "ch /tʃ/", "cheese", "/tʃiːz/", "ឆ + អ៊ី + ហ្ស = ឈីហ្ស", "ch|ee|se", "ឈីស (ផលិតផលទឹកដោះគោ)", "🧀", "The little mouse loves eating yellow cheese.", "កណ្តុរតូចចូលចិត្តស៊ីឈីសពណ៌លឿង។"),
            item(L, "Unit 4: Digraphs (sh, ch, tch)", "ch /tʃ/", "lunch", "/lʌntʃ/", "ល + អាត់ + ន + ឆ = ឡាន់ឆ៏", "l|u|n|ch", "អាហារថ្ងៃត្រង់", "🍱", "We eat healthy rice and soup for lunch.", "យើងញ៉ាំបាយនិងសម្លដើម្បីសុខភាពសម្រាប់អាហារថ្ងៃត្រង់។"),

            // Unit 5: Digraphs th, wh, ph
            item(L, "Unit 5: Digraphs (th, wh, ph)", "th /θ/", "three", "/θriː/", "ធ្រ + អ៊ី = ធ្រី (ដាក់ចុងអណ្តាតប៉ះធ្មេញលើ)", "th|r|ee", "លេខបី (៣)", "3️⃣", "There are three books on the teacher's desk.", "មានសៀវភៅបីក្បាលនៅលើតុរបស់គ្រូ។"),
            item(L, "Unit 5: Digraphs (th, wh, ph)", "th /θ/", "teeth", "/tiːθ/", "ថ + អ៊ី + ធ = ធីធ៏", "t|ee|th", "ធ្មេញ", "🦷", "Brush your teeth twice every single day.", "ដុសធ្មេញរបស់អ្នកពីរដងជារៀងរាល់ថ្ងៃ។"),
            item(L, "Unit 5: Digraphs (th, wh, ph)", "th /ð/", "mother", "/ˈmʌð.ər/", "ម៉ា-ឌឺរ (m+o = ម៉ា, th+er = ឌឺរ)", "m|o|th|er", "ម្តាយ / ម៉ាក់", "👩‍👧", "My mother helps me review my English lessons.", "ម្តាយរបស់ខ្ញុំជួយខ្ញុំរំលឹកមេរៀនភាសាអង់គ្លេស។"),
            item(L, "Unit 5: Digraphs (th, wh, ph)", "wh /w/", "whale", "/weɪl/", "វ + អេ + ល = វែល", "wh|a|l|e", "ត្រីបាឡែន", "🐋", "The blue whale is the largest animal on Earth.", "ត្រីបាឡែនពណ៌ខៀវគឺជាសត្វធំជាងគេបំផុតលើផែនដី។"),
            item(L, "Unit 5: Digraphs (th, wh, ph)", "ph /f/", "phone", "/fəʊn/", "ហ្វ + អូ + ន = ហ្វូន (ph បញ្ចេញសំឡេង ហ្វ)", "ph|o|n|e", "ទូរស័ព្ទ", "📱", "Use your phone to listen to English pronunciation.", "ប្រើទូរស័ព្ទរបស់អ្នកដើម្បីស្តាប់ការបញ្ចេញសំឡេងភាសាអង់គ្លេស។"),

            // Unit 6: Ending Blends (ck, ng, nk, nd, nt, mp)
            item(L, "Unit 6: Ending Blends (ng, nk, nd, mp)", "-ng /ŋ/", "king", "/kɪŋ/", "ខ + អ៊ី + ង = ឃីង", "k|i|ng", "ព្រះមហាក្សត្រ / ស្តេច", "🤴", "The wise king loved all the people in his land.", "ព្រះមហាក្សត្រប្រកបដោយគតិបណ្ឌិតស្រឡាញ់ប្រជារាស្ត្រទាំងអស់។"),
            item(L, "Unit 6: Ending Blends (ng, nk, nd, mp)", "-ng /ŋ/", "sing", "/sɪŋ/", "ស + អ៊ី + ង = ស៊ីង", "s|i|ng", "ច្រៀង", "🎤", "Let us sing the English alphabet song together.", "តោះយើងច្រៀងបទចម្រៀងអក្សរក្រមអង់គ្លេសទាំងអស់គ្នា។"),
            item(L, "Unit 6: Ending Blends (ng, nk, nd, mp)", "-nk /ŋk/", "drink", "/drɪŋk/", "ដ្រ + អ៊ី + ងខ៏ = ដ្រីងខ៏", "dr|i|nk", "ផឹក / ពិសាទឹក", "🥤", "Remember to drink plenty of clean water.", "សូមចងចាំថាត្រូវផឹកទឹកស្អាតឱ្យបានច្រើន។"),
            item(L, "Unit 6: Ending Blends (ng, nk, nd, mp)", "-nd /nd/", "hand", "/hænd/", "ហ + អ៊ែ + នដ៏ = ហែនដ៏", "h|a|nd", "ដៃ", "✋", "Raise your hand when you want to answer.", "សូមលើកដៃនៅពេលអ្នកចង់ឆ្លើយសំណួរ។"),
            item(L, "Unit 6: Ending Blends (ng, nk, nd, mp)", "-mp /mp/", "lamp", "/læmp/", "ល + អ៊ែ + មផ៏ = ឡែមផ៏", "l|a|mp", "ចង្កៀង / អំពូលតុរៀន", "🛋️", "Turn on the desk lamp while reading at night.", "បើកអំពូលតុរៀននៅពេលអានសៀវភៅពេលយប់។"),

            // Unit 7: Soft c (/s/) & Soft g (/dʒ/)
            item(L, "Unit 7: Soft c & Soft g (ce, ci, ge, gi)", "ce /s/", "rice", "/raɪs/", "រ + អាយ + ស = រ៉ាយស៏ (c បញ្ចេញសំឡេង ស)", "r|i|ce", "បាយ / អង្ករ", "🍚", "Cambodian jasmine rice is famous around the world.", "អង្ករផ្កាម្លិះកម្ពុជាមានឈ្មោះល្បីល្បាញទូទាំងពិភពលោក។"),
            item(L, "Unit 7: Soft c & Soft g (ce, ci, ge, gi)", "ci /s/", "city", "/ˈsɪt.i/", "ស៊ី-ធី (c+i = ស៊ី, t+y = ធី)", "c|i|t|y", "ទីក្រុង", "🏙️", "Battambang is a clean and peaceful city.", "បាត់ដំបងគឺជាទីក្រុងដ៏ស្អាតនិងស្ងប់ស្ងាត់។"),
            item(L, "Unit 7: Soft c & Soft g (ce, ci, ge, gi)", "ci /s/", "pencil", "/ˈpen.səl/", "ផេន-សល (p+e+n = ផេន, c+i+l = សល)", "p|e|n|c|il", "ខ្មៅដៃ", "✏️", "Sharpen your pencil before the writing test.", "សូមចិតខ្មៅដៃរបស់អ្នកមុនពេលប្រឡងសរសេរ។"),
            item(L, "Unit 7: Soft c & Soft g (ce, ci, ge, gi)", "ge /dʒ/", "orange", "/ˈɒr.ɪndʒ/", "អរ-រីនជ៏ (ge បញ្ចេញសំឡេង ជ)", "or|a|n|ge", "ផ្លែក្រូចពោធិ៍សាត់", "🍊", "Sweet orange juice has lots of vitamin C.", "ទឹកក្រូចពោធិ៍សាត់ផ្អែមមានវីតាមីន C ច្រើន។"),
            item(L, "Unit 7: Soft c & Soft g (ce, ci, ge, gi)", "gi /dʒ/", "giraffe", "/dʒɪˈrɑːf/", "ជី-រ៉ាហ្វ (gi=ជី + raffe=រ៉ាហ្វ)", "gi|r|a|ffe", "សត្វកវែង (ស៊ីរ៉ាហ្វ)", "🦒", "A tall giraffe can eat leaves from high trees.", "សត្វកវែងអាចស៊ីស្លឹកឈើពីដើមឈើខ្ពស់ៗបាន។"),

            // Unit 8: Triple Blends & Silent Letters (str, spr, kn, wr)
            item(L, "Unit 8: Triple Blends & Silent Letters (str, kn, wr)", "str- /str/", "street", "/striːt/", "ស្ត្រ + អ៊ី + ថ = ស្ត្រីថ៏", "str|ee|t", "ផ្លូវក្នុងក្រុង", "🛣️", "Keep our school street clean and green.", "រក្សាផ្លូវសាលារៀនរបស់យើងឱ្យស្អាតនិងបៃតង។"),
            item(L, "Unit 8: Triple Blends & Silent Letters (str, kn, wr)", "spr- /spr/", "spring", "/sprɪŋ/", "ស្ព្រ + អ៊ី + ង = ស្ព្រីង", "spr|i|ng", "រដូវផ្ការីក", "🌸", "Beautiful flowers bloom in the spring season.", "ផ្កាស្អាតៗរីកสะพรั่งនៅក្នុងរដូវផ្ការីក។"),
            item(L, "Unit 8: Triple Blends & Silent Letters (str, kn, wr)", "kn- /n/", "knee", "/niː/", "ន + អ៊ី = នី (អក្សរ k មិនបញ្ចេញសំឡេង)", "kn|ee", "ជង្គង់", "🦵", "He bent his knee to tie his shoelaces.", "គាត់បានបន្ទន់ជង្គង់ដើម្បីចងខ្សែស្បែកជើង។"),
            item(L, "Unit 8: Triple Blends & Silent Letters (str, kn, wr)", "wr- /r/", "write", "/raɪt/", "រ + អាយ + ថ = រ៉ាយថ៏ (អក្សរ w មិនបញ្ចេញសំឡេង)", "wr|i|t|e", "សរសេរ", "✍️", "Students learn to read and write English clearly.", "សិស្សរៀនអាននិងសរសេរភាសាអង់គ្លេសឱ្យបានច្បាស់លាស់។")
        );
        saveLevelItems(L, list);
    }

    // =========================================================================
    // LEVEL 5: LETTER COMBINATIONS & ADVANCED FLUENCY (កម្រិតខ្ពស់បំផុត) - 8 Units
    // =========================================================================
    @Transactional
    public void seedLevel5LetterCombinations() {
        int L = 5;
        List<PhonicsItem> list = List.of(
            // Unit 1: R-Controlled Vowels (ar, or)
            item(L, "Unit 1: R-Controlled Vowels (ar, or)", "ar /ɑːr/", "car", "/kɑːr/", "ខ + អារ = ខារ", "c|ar", "រថយន្ត / ឡាន", "🚗", "My uncle drives a silver car to work.", "ពូរបស់ខ្ញុំបើកឡានពណ៌ទឹកប្រាក់ទៅធ្វើការ។"),
            item(L, "Unit 1: R-Controlled Vowels (ar, or)", "ar /ɑːr/", "park", "/pɑːrk/", "ផ + អារ + ខ = ផាកខ៏", "p|ar|k", "សួនច្បារសាធារណៈ", "🏞️", "Children love reading books in the quiet park.", "ក្មេងៗចូលចិត្តអានសៀវភៅនៅក្នុងសួនច្បារដ៏ស្ងប់ស្ងាត់។"),
            item(L, "Unit 1: R-Controlled Vowels (ar, or)", "ar /ɑːr/", "smart", "/smɑːrt/", "ស្ម + អារ + ថ = ស្មាតថ៏", "sm|ar|t", "ឆ្លាតវៃ / ពូកែ", "🧠", "Consistent practice makes you a smart learner.", "ការហាត់អនុវត្តជាប្រចាំធ្វើឱ្យអ្នកក្លាយជាសិស្សឆ្លាតវៃ។"),
            item(L, "Unit 1: R-Controlled Vowels (ar, or)", "or /ɔːr/", "corn", "/kɔːrn/", "ខ + អរ + ន = ខន", "c|or|n", "ពោត", "🌽", "Farmers grow sweet yellow corn on the farm.", "កសិករដាំពោតពណ៌លឿងផ្អែមនៅក្នុងកសិដ្ឋាន។"),
            item(L, "Unit 1: R-Controlled Vowels (ar, or)", "or /ɔːr/", "sport", "/spɔːrt/", "ស្ព + អរ + ថ = ស្ពតថ៏", "sp|or|t", "កីឡា", "🏅", "Playing sport every week keeps us strong.", "ការលេងកីឡារៀងរាល់សប្តាហ៍ធ្វើឱ្យយើងរឹងមាំ។"),

            // Unit 2: R-Controlled Vowels (er, ir, ur)
            item(L, "Unit 2: R-Controlled Vowels (er, ir, ur)", "er /ər/", "teacher", "/ˈtiː.tʃər/", "ធី-ឆឺរ (t+ea = ធី, ch+er = ឆឺរ)", "t|ea|ch|er", "គ្រូបង្រៀន", "👩‍🏫", "Our English teacher explains phonics very clearly.", "គ្រូភាសាអង់គ្លេសរបស់យើងពន្យល់ការប្រកបបានច្បាស់ណាស់។"),
            item(L, "Unit 2: R-Controlled Vowels (er, ir, ur)", "er /ər/", "water", "/ˈwɔː.tər/", "វ៉-ធឺរ (wa=វ៉ + ter=ធឺរ)", "w|a|t|er", "ទឹក", "💧", "Clean water is essential for all living things.", "ទឹកស្អាតគឺចាំបាច់បំផុតសម្រាប់ភាវៈរស់ទាំងអស់។"),
            item(L, "Unit 2: R-Controlled Vowels (er, ir, ur)", "ir /ɜːr/", "shirt", "/ʃɜːrt/", "ស៊ + អឺ + ថ = ស៊ឺថ៏", "sh|ir|t", "អាវសឺមី", "👔", "Students wear a clean white shirt to school.", "សិស្សានុសិស្សពាក់អាវពណ៌សស្អាតទៅសាលារៀន។"),
            item(L, "Unit 2: R-Controlled Vowels (er, ir, ur)", "ur /ɜːr/", "nurse", "/nɜːrs/", "ណ + អឺ + ស = ណឺស", "n|ur|se", "គិលានុបដ្ឋាយិកា (គ្រូពេទ្យ)", "👩‍⚕️", "The kind nurse takes care of sick patients.", "គិលានុបដ្ឋាយិកាដ៏សប្បុរសមើលថែអ្នកជំងឺ។"),
            item(L, "Unit 2: R-Controlled Vowels (er, ir, ur)", "ur /ɜːr/", "turtle", "/ˈtɜːr.təl/", "ធឺ-ថល (t+ur = ធឺ, t+le = ថល)", "t|ur|t|le", "សត្វអណ្តើក", "🐢", "The slow turtle walked steadily and won the race.", "អណ្តើកដើរយឺតៗតែមិនឈប់ ហើយបានឈ្នះការប្រណាំង។"),

            // Unit 3: Diphthongs (ou, ow)
            item(L, "Unit 3: Diphthongs (ou, ow /aʊ/)", "ou /aʊ/", "house", "/haʊs/", "ហ + អៅ + ស = ហៅស៏", "h|ou|se", "ផ្ទះ", "🏡", "There is a mango tree in front of my house.", "មានដើមស្វាយមួយដើមនៅមុខផ្ទះរបស់ខ្ញុំ។"),
            item(L, "Unit 3: Diphthongs (ou, ow /aʊ/)", "ou /aʊ/", "cloud", "/klaʊd/", "ខ្ល + អៅ + ដ = ខ្លៅដ៏", "cl|ou|d", "ពពក", "☁️", "White clouds float across the blue sky.", "ពពកពណ៌សរសាត់កាត់ផ្ទៃមេឃពណ៌ខៀវ។"),
            item(L, "Unit 3: Diphthongs (ou, ow /aʊ/)", "ou /aʊ/", "sound", "/saʊnd/", "ស + អៅ + នដ៏ = សៅនដ៏", "s|ou|nd", "សំឡេង / សូរសព្ទ", "🔊", "Listen carefully to the beginning sound of each word.", "សូមស្តាប់ដោយយកចិត្តទុកដាក់នូវសូរសំឡេងដើមនៃពាក្យនីមួយៗ។"),
            item(L, "Unit 3: Diphthongs (ou, ow /aʊ/)", "ow /aʊ/", "cow", "/kaʊ/", "ខ + អៅ = ខៅ", "c|ow", "សត្វគោញី", "🐄", "The dairy cow gives us fresh milk every day.", "គោទឹកដោះផ្តល់ទឹកដោះគោស្រស់ឱ្យយើងរាល់ថ្ងៃ។"),
            item(L, "Unit 3: Diphthongs (ou, ow /aʊ/)", "ow /aʊ/", "flower", "/ˈflaʊ.ər/", "ហ្វ្លៅ-អឺរ (fl+ow = ហ្វ្លៅ, er = អឺរ)", "fl|ow|er", "ផ្កា", "🌺", "The lotus flower is a symbol of purity.", "ផ្កាឈូកគឺជានិមិត្តរូបនៃភាពបរិសុទ្ធ។"),

            // Unit 4: Diphthongs (oi, oy, au, aw)
            item(L, "Unit 4: Diphthongs (oi, oy, au, aw)", "oi /ɔɪ/", "coin", "/kɔɪn/", "ខ + អយ + ន = ខយន៏", "c|oi|n", "កាក់ប្រាក់", "🪙", "He saved every coin in his piggy bank.", "គាត់បានសន្សំកាក់ទាំងអស់ទុកក្នុងកូនជ្រូកសន្សំប្រាក់។"),
            item(L, "Unit 4: Diphthongs (oi, oy, au, aw)", "oi /ɔɪ/", "voice", "/vɔɪs/", "វ + អយ + ស = វយស៏", "v|oi|ce", "សំឡេងនិយាយ", "🎙️", "Read the English sentence in a clear voice.", "សូមអានប្រយោគភាសាអង់គ្លេសដោយសំឡេងច្បាស់ៗ។"),
            item(L, "Unit 4: Diphthongs (oi, oy, au, aw)", "oy /ɔɪ/", "boy", "/bɔɪ/", "ប + អយ = បយ", "b|oy", "ក្មេងប្រុស", "👦", "The polite boy always respects his teachers.", "ក្មេងប្រុសគួរសមតែងតែគោរពលោកគ្រូអ្នកគ្រូរបស់គាត់។"),
            item(L, "Unit 4: Diphthongs (oi, oy, au, aw)", "aw /ɔː/", "draw", "/drɔː/", "ដ្រ + អរ = ដ្ររ", "dr|aw", "គូររូប", "🎨", "She likes to draw pictures of nature.", "នាងចូលចិត្តគូររូបភាពធម្មជាតិ។"),
            item(L, "Unit 4: Diphthongs (oi, oy, au, aw)", "au /ɔː/", "author", "/ˈɔː.θər/", "អ-ធឺរ (au=អ + th=ធ + or=អឺរ)", "au|th|or", "អ្នកនិពន្ធសៀវភៅ", "✍️", "Who is the author of your favorite story book?", "តើនរណាជាអ្នកនិពន្ធសៀវភៅរឿងដែលអ្នកចូលចិត្តបំផុត?"),

            // Unit 5: Complex Vowels (oo, ear, air, are)
            item(L, "Unit 5: Complex Vowels (oo, ear, air)", "oo /ʊ/", "book", "/bʊk/", "ប + អ៊ុក + ខ = ប៊ុកខ៏", "b|oo|k", "សៀវភៅ", "📚", "Open your English textbook to page ten.", "សូមបើកសៀវភៅពុម្ពភាសាអង់គ្លេសរបស់អ្នកទៅទំព័រទីដប់។"),
            item(L, "Unit 5: Complex Vowels (oo, ear, air)", "oo /ʊ/", "good", "/ɡʊd/", "ហ្គ + អ៊ុក + ដ = ហ្គូដ៏", "g|oo|d", "ល្អប្រសើរ", "👍", "You did a very good job on your phonics test!", "អ្នកធ្វើបានល្អណាស់ក្នុងការប្រឡងប្រកបរបស់អ្នក!"),
            item(L, "Unit 5: Complex Vowels (oo, ear, air)", "ear /ɪər/", "hear", "/hɪər/", "ហ + អៀរ = ហៀរ", "h|ear", "ឮ / ស្តាប់ឮ", "👂", "Can you hear the pronunciation clearly?", "តើអ្នកអាចស្តាប់ឮការបញ្ចេញសំឡេងច្បាស់ទេ?"),
            item(L, "Unit 5: Complex Vowels (oo, ear, air)", "air /eər/", "hair", "/heər/", "ហ + អ៊ែរ = ហែរ", "h|air", "សក់", "💇", "She combs her neat black hair every morning.", "នាងសិតសក់ពណ៌ខ្មៅឱ្យមានរបៀបរៀងរាល់ព្រឹក។"),
            item(L, "Unit 5: Complex Vowels (oo, ear, air)", "are /eər/", "share", "/ʃeər/", "ស៊ + អ៊ែរ = ស៊ែរ", "sh|are", "ចែករំលែក", "🤝", "Good friends always share books and knowledge.", "មិត្តល្អតែងតែចែករំលែកសៀវភៅនិងចំណេះដឹង។"),

            // Unit 6: Word Endings (-le, -al, -or)
            item(L, "Unit 6: Word Endings (-le, -al, -or)", "-le /əl/", "table", "/ˈteɪ.bəl/", "ថេ-បល (t+a = ថេ, b+le = បល)", "t|a|b|le", "តុ", "🪑", "Please put your notebook on the table.", "សូមដាក់សៀវភៅសរសេររបស់អ្នកនៅលើតុ។"),
            item(L, "Unit 6: Word Endings (-le, -al, -or)", "-le /əl/", "puzzle", "/ˈpʌz.əl/", "ផាហ្ស-ហ្សល (p+u+zz = ផាហ្ស, le = ហ្សល)", "p|u|zz|le", "ល្បែងផ្គុំរូប / ចំណោទ", "🧩", "Solving word puzzles helps improve your brain.", "ការដោះស្រាយល្បែងផ្គុំពាក្យជួយអភិវឌ្ឍខួរក្បាលរបស់អ្នក។"),
            item(L, "Unit 6: Word Endings (-le, -al, -or)", "-al /əl/", "animal", "/ˈæn.ɪ.məl/", "អ៊ែ-នី-ម៉ល (an=អ៊ែន, i=អ៊ី, mal=ម៉ល)", "a|n|i|m|al", "សត្វ", "🐾", "What is your favorite wild animal?", "តើសត្វព្រៃណាមួយដែលអ្នកចូលចិត្តជាងគេ?"),
            item(L, "Unit 6: Word Endings (-le, -al, -or)", "-al /əl/", "hospital", "/ˈhɒs.pɪ.təl/", "ហស-ស្ពី-ថល (hos=ហស, pi=ស្ពី, tal=ថល)", "h|o|s|pi|tal", "មន្ទីរពេទ្យ", "🏥", "Doctors and nurses work at the provincial hospital.", "វេជ្ជបណ្ឌិតនិងគិលានុបដ្ឋាយិកាធ្វើការនៅមន្ទីរពេទ្យខេត្ត។"),
            item(L, "Unit 6: Word Endings (-le, -al, -or)", "-or /ər/", "doctor", "/ˈdɒk.tər/", "ដក់-ធឺរ (doc=ដក់ + tor=ធឺរ)", "d|o|c|t|or", "វេជ្ជបណ្ឌិត / គ្រូពេទ្យ", "👨‍⚕️", "She studies hard because she wants to be a doctor.", "នាងខំរៀនខ្លាំងព្រោះនាងចង់ក្លាយជាវេជ្ជបណ្ឌិត។"),

            // Unit 7: Suffixes (-tion, -ture, -ful, -ous)
            item(L, "Unit 7: Suffixes (-tion, -ture, -ful, -ous)", "-tion /ʃən/", "education", "/ˌed.jʊˈkeɪ.ʃən/", "អ៊េ-ជូ-ខេ-សិន (-tion អានថា សិន)", "ed|u|ca|tion", "ការអប់រំ", "🎓", "Good education is the key to a bright future.", "ការអប់រំល្អគឺជាកូនសោឆ្ពោះទៅរកអនាគតដ៏ភ្លឺស្វាង។"),
            item(L, "Unit 7: Suffixes (-tion, -ture, -ful, -ous)", "-tion /ʃən/", "question", "/ˈkwes.tʃən/", "ខ្វេស-ឆិន (ques=ខ្វេស + tion=ឆិន)", "qu|es|tion", "សំណួរ", "❓", "Do not be afraid to ask a question in class.", "កុំខ្លាចក្នុងការសួរសំណួរនៅក្នុងថ្នាក់រៀន។"),
            item(L, "Unit 7: Suffixes (-tion, -ture, -ful, -ous)", "-ture /tʃər/", "nature", "/ˈneɪ.tʃər/", "ណេ-ឆឺរ (-ture អានថា ឆឺរ)", "n|a|ture", "ធម្មជាតិ", "🏞️", "We must protect our beautiful nature and forests.", "យើងត្រូវតែការពារធម្មជាតិនិងព្រៃឈើដ៏ស្រស់ស្អាតរបស់យើង។"),
            item(L, "Unit 7: Suffixes (-tion, -ture, -ful, -ous)", "-ture /tʃər/", "future", "/ˈfjuː.tʃər/", "ហ្វ្យូ-ឆឺរ (fu=ហ្វ្យូ + ture=ឆឺរ)", "f|u|ture", "អនាគត", "🚀", "Reading every day builds a strong future.", "ការអានរៀងរាល់ថ្ងៃកសាងអនាគតដ៏រឹងមាំ។"),
            item(L, "Unit 7: Suffixes (-tion, -ture, -ful, -ous)", "-ful /fəl/", "helpful", "/ˈhelp.fəl/", "ហេលផ៏-ហ្វល (help=ហេលផ៏ + ful=ហ្វល)", "h|e|lp|ful", "ដែលចេះជួយយកអាសា", "🤝", "A helpful student always supports classmates.", "សិស្សដែលចេះជួយយកអាសាតែងតែគាំទ្រមិត្តរួមថ្នាក់។"),

            // Unit 8: Multi-Syllable Academic & Fluency Mastery
            item(L, "Unit 8: Multi-Syllable Mastery (4-Skills Fluency)", "com-pu-ter", "computer", "/kəmˈpjuː.tər/", "ខឹម-ព្យូ-ធឺរ (៣ ព្យាង្គ៖ com + pu + ter)", "com|pu|ter", "កុំព្យូទ័រ", "💻", "We use a computer to learn English online.", "យើងប្រើកុំព្យូទ័រដើម្បីរៀនភាសាអង់គ្លេសតាមអនឡាញ។"),
            item(L, "Unit 8: Multi-Syllable Mastery (4-Skills Fluency)", "dic-tion-a-ry", "dictionary", "/ˈdɪk.ʃən.ər.i/", "ឌីក-សិន-ណឺ-រី (៤ ព្យាង្គ៖ dic + tion + a + ry)", "dic|tion|a|ry", "វចនានុក្រម", "📕", "Look up new words in an English-Khmer dictionary.", "ឆែកមើលពាក្យថ្មីៗនៅក្នុងវចនានុក្រមអង់គ្លេស-ខ្មែរ។"),
            item(L, "Unit 8: Multi-Syllable Mastery (4-Skills Fluency)", "know-ledge", "knowledge", "/ˈnɒl.ɪdʒ/", "ណល-លីជ (know=ណល + ledge=លីជ)", "kn|ow|ledge", "ចំណេះដឹង", "🌟", "Knowledge grows when you listen, read, speak, and write.", "ចំណេះដឹងរីកចម្រើននៅពេលអ្នក ស្តាប់ អាន និយាយ និងសរសេរ។"),
            item(L, "Unit 8: Multi-Syllable Mastery (4-Skills Fluency)", "con-ver-sa-tion", "conversation", "/ˌkɒn.vəˈseɪ.ʃən/", "ខន់-វឺ-សេ-សិន (៤ ព្យាង្គ៖ con + ver + sa + tion)", "con|ver|sa|tion", "ការសន្ទនា", "💬", "Practice English conversation with your friends daily.", "ហាត់សន្ទនាភាសាអង់គ្លេសជាមួយមិត្តភក្តិរបស់អ្នកជារៀងរាល់ថ្ងៃ។"),
            item(L, "Unit 8: Multi-Syllable Mastery (4-Skills Fluency)", "in-ter-na-tion-al", "international", "/ˌɪn.təˈnæʃ.ən.əl/", "អ៊ីន-ធឺ-ណែ-សិន-ណល (៥ ព្យាង្គ៖ in+ter+na+tion+al)", "in|ter|na|tion|al", "អន្តរជាតិ", "🌐", "English is an important international language.", "ភាសាអង់គ្លេសគឺជាភាសាអន្តរជាតិដ៏សំខាន់មួយ។")
        );
        saveLevelItems(L, list);
    }
}
