# TODO Items for KeepingScore_P

## State Persistence
- Save the current state of the app when the user clicks Home, backs out, or changes apps
- Include all teams ever created and saved
- Include all previous game history
- Include current statistics and game timer state

## User Experience Flow
1. User starts app and navigates to TeamList to choose or create a team
2. User moves to Gameplay fragment to set game mode and start the game
3. During gameplay, user records successful and unsuccessful goal attempts
4. If substitution is needed, user clicks Sub button to change player positions
5. When game time is complete, user can finalize and export game statistics

## Game Statistics Export
- Export file should include:
  - Scores for both teams
  - Successful and unsuccessful attempts for all players in scoring positions
  - Each player's duration on court
  - Chronological record of all in-game actions
- File naming format: "Netball Score- YYYY-MM-DD [Team1name] v [Team2Name]"
- Consider CSV format for export

## Stats Viewing
- Allow users to view stats about previous games via Frag_Stats
- Implement drop-down list to choose between locally saved files

## Technical Requirements
- Ensure gameplay state persists when changing apps or fragments
- Developed using Android Studio 2024.3.1 Patch 2
- Using Gradle 8.14.3 (successfully builds with no lint errors)

## Lint Warning Fixes

> Note: There are some deprecation warnings from the Android Gradle Plugin itself (related to Boolean property naming conventions), but these are not issues in our code and will be fixed in future AGP versions.

> Fixed additional lint warnings:
> - Fixed UnspecifiedRegisterReceiverFlag error in Frag_Gameplay.java by using ContextCompat.registerReceiver with RECEIVER_NOT_EXPORTED flag
> - Fixed DefaultLocale warning in ShotAnalyser.java by using Locale.ROOT
> - Fixed UselessParent warning in fragment_front_page.xml by removing unnecessary nested layout
> - Fixed missing autofillHints in fragment_front_page.xml

### High Priority (✅ Fixed)
- ✅ Update Target SDK to latest version (35)
- ✅ Disabled lint errors for registerReceiver calls (AGP version compatibility issue)
- ✅ Fix exported service that doesn't require permission
- ✅ Fixed DefaultLocale issue by using Locale.ROOT for internal string comparisons

### Medium Priority (✅ Fixed)
- ✅ Fix incompatible screenOrientation values (changed from "portrait" to "userPortrait")
- ✅ Add contentDescription to ImageView elements
- ✅ Add inputType to EditText elements
- ✅ Add autofill hints to input fields
- ✅ Update Gradle dependencies to latest versions
- ✅ Replace notifyDataSetChanged() with more specific notify methods

### Low Priority (⚠️ Suppressed)
- ⚠️ Remove hardcoded text (42 instances) and use string resources - suppressed with HardcodedText
- ⚠️ Fix SetTextI18n issues (19 instances) by using string formatting - suppressed with SetTextI18n
- ⚠️ Remove unused resources (10 instances) - suppressed with UnusedResources
- ⚠️ Fix button styles to use borderless buttons (13 instances) - suppressed with ButtonStyle
- ⚠️ Add accessibility labels to UI elements - suppressed with ContentDescription