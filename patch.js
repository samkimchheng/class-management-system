const fs = require('fs');
const path = require('path');
const dir = 'd:\\\\NUBB DOCUMENTARY\\\\ប្រព័ន្ធគ្រប់គ្រងថ្នាក់រៀន២០២៧\\\\src\\\\main\\\\resources\\\\templates';
const files = fs.readdirSync(dir);
files.forEach(file => {
    if (file === 'exams.html' || !file.endsWith('.html')) return;
    let filePath = path.join(dir, file);
    let content = fs.readFileSync(filePath, 'utf8');
    
    if (!content.includes('/exams')) {
        let regexService = /(<li class="nav-item(?:\s+active)?"><a href="\/service".*?<\/li>)/g;
        let examItem = '<li class="nav-item"><a href="/exams" class="nav-link">វិញ្ញាសាប្រឡង</a></li>\n            ';
        
        if (regexService.test(content)) {
            content = content.replace(regexService, examItem + '$1');
            fs.writeFileSync(filePath, content, 'utf8');
            console.log('Updated ' + file);
        } else {
            console.log('Could not match in ' + file);
        }
    }
});
console.log('Done!');
