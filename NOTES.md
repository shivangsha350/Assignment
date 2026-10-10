# Notes

## Summary of Changes
- **SQL Precedence Fix**: Wrapped `(LOWER(title) LIKE :term OR LOWER(description) LIKE :term)` in parentheses in `TaskRepository.java`, `search_tasks.sql`, and `task_search_package.sql`. This prevents archived tasks from leaking and ensures status filtering works correctly with and without search terms.
- **API Validation & Throughput**: Added HTTP 400 responses in `TaskController.java` for invalid status values, non-positive page numbers, and page sizes outside 1–100. Removed artificial `Thread.sleep` to eliminate thread blocking while retaining complexity logging.
- **Frontend Race Conditions & Debounce**: Added 300ms search debouncing and `AbortController` request cancellation in `useTasks.js` to ignore stale out-of-order responses. Ensured `loading` resets to `false` on fetch failures and `error` clears on retry.
- **Pagination Reset**: Updated `App.jsx` to reset `page` to 1 on search or filter changes, preventing stranded empty pages.
- **Regression Tests**: Added JUnit 5 test suite verifying query logic, archived task exclusion, and input validation.

## What I Chose Not to Change
- **In-Memory Pagination**: Kept controller-level `subList` pagination rather than refactoring to Spring Data `Pageable` to keep the patch focused and minimal within the timebox.
- **UI & Architecture**: Preserved existing component hierarchy, styling, and endpoints without adding third-party libraries.

## Biggest Remaining Risk
- **In-Memory Pagination at Scale**: Loading all matching records into JVM memory before slicing will cause high memory consumption and GC latency as the dataset grows. Production deployment requires database-level pagination (`LIMIT`/`OFFSET`).

## Tools / AI Used
- Used AI to analyze SQL operator precedence and draft initial test cases. Validated all behavior against live curl requests, automated tests, and Vite build outputs.
