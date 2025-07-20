# Coding Standards for KeepingScore_P

## Environment Setup Requirements

- Java Version: Java 21
- Gradle Version: 8.14.3
- Android Studio Version: 2024.3.1 Patch 2
- Minimum SDK: 27 (Android 8.1)
- Target SDK: 35 (Android 15)
- Compile SDK: 35

## General Guidelines

All changes to be recorded in changelog.md

Code does not need to meet enterprise framework standards.
Focus should be on simplicity, reusability, supportability, performance and stability.

Areas for Improvement
Code Organization:

✅ Large methods in Frag_Gameplay have been refactored into four new component classes to handle specific responsibilities:
GameTimerManager: Handles timer functionality
GameStateManager: Manages game state persistence
ScoringManager: Handles scoring logic
UIHelper: Manages UI operations

TODO comments indicate incomplete features and potential technical debt

Error Handling:
✅ Error handling has been improved in Frag_Gameplay, especially for vibration, keyboard operations, and timer management

✅ Null checks have been added in critical areas

Documentation:

✅ Comprehensive documentation added for all methods in Frag_Gameplay

✅ Class-level documentation added to Frag_Gameplay

Testing:

No evidence of unit or UI tests in the codebase

## Lint Standards

- All high and medium priority lint warnings must be fixed
- Low priority lint warnings should be addressed when possible or suppressed with justification
- Use Locale.ROOT for internal string comparisons
- Always use ContextCompat.registerReceiver with appropriate flags
- Avoid unnecessary nested layouts
- Always provide accessibility attributes (contentDescription, autofillHints, etc.)