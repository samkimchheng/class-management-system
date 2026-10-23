# Statistics by Grade per Subject Complete

## Changes Made:
- **Backend WebController.java**: 
  - Injected ScoreRuleRepository.
  - Refactored /statistics GET endpoint to accept subjectName.
  - Created an inner SubjectStat DTO to aggregate scores, pass, fail, and A-F grades per subject.
  - Implemented dynamic grading logic where the total score is divided by the subject-specific max boundary (fetched via scoreRuleRepository) before deriving percentages (90% = A, etc.).
  - Added grouping algorithm to yield a list of SubjectStat rows for every single subject present in the class.
- **Frontend statistics.html**:
  - Inserted a subjectName filter dropdown matching the ones in scores.html to instantly recalculate pie charts for just that subject.
  - Injected a large summary table at the bottom of the page (<table th:each="stat : ">) which gives a rapid overview across all subjects simultaneously.
  - Updated Print preview header to include the selected subject.