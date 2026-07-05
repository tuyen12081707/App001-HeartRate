## 🚀 Summary of Changes
This Pull Request introduces the Health News API integration, enhances the Add Record UI/UX, and restructures the application's Navigation flow according to the latest requirements.

### ✨ Key Features
- **Health News Feed (Task 3):** 
  - Integrated `Ktor` Client, `Kotlinx Serialization`, and `Kamel` for networking and robust image loading.
  - Implemented the full Clean Architecture flow (Domain -> Data -> Presentation) for fetching news.
  - Users can click on news cards to read full articles directly via the system browser (`LocalUriHandler`).

- **UI & Validation Refinements (Task 1):**
  - Upgraded the manual Heart Rate `WheelNumberPicker` with a modern alpha-fade effect for unselected numbers.
  - Strictly enforced validation: the Save button is disabled until both BPM and Body State are explicitly chosen.
  - Safely handled Navigation transitions between Add Record and Result screens to prevent null exceptions.

- **Navigation & UX Redesign:**
  - Reordered the Bottom Navigation Bar: `Dashboard` is now the default Home screen, followed by `History`, `News`, and `Profile`.

- **Automated Workflow & Conventions:**
  - Rewrote `.github/workflows/ai-code-review.yml` to execute real AI code reviews via Gemini API.
  - Formalized commit conventions by creating `GIT_COMMIT_RULES.md` and linked it to the agent working agreements.

## 🛠 Checklists
- [x] Tested locally on Simulator/Device.
- [x] Clean Architecture enforced (no C-Interop leaks).
- [x] Adhered to strict Git commit format guidelines.
- [x] Cleaned up debugging logs and redundant code.
