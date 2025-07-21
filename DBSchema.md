# KeepingScore Database Schema

## Overview
The KeepingScore app uses a Room database with 4 main tables to store team information, game statistics, game actions, and opposition teams.

## Database Version
Current version: 6

## Tables

### 1. Team Table
Stores information about club teams.

```
Table: teams
```

| Column | Type | Description |
|--------|------|-------------|
| id | INTEGER | Primary key, auto-increment |
| team_name | TEXT | Name of the team |
| players | TEXT | JSON string containing player information |

### 2. Game Stats Table
Stores summary information about games.

```
Table: game_stats
```

| Column | Type | Description |
|--------|------|-------------|
| id | INTEGER | Primary key, auto-increment |
| game_date | TEXT | Date and time of the game |
| game_start_time | TEXT | Actual start time of the game |
| team1_name | TEXT | Name of the club team |
| team2_name | TEXT | Name of the opposition team |
| team1_score | INTEGER | Score of the club team |
| team2_score | INTEGER | Score of the opposition team |
| game_mode | TEXT | Game configuration (e.g., "15m,4Q" for 15-minute quarters) |
| period_duration | INTEGER | Duration of each period in minutes |

### 3. Game Action Table
Stores individual game actions (goals, misses, etc.).

```
Table: game_actions
```

| Column | Type | Description |
|--------|------|-------------|
| id | INTEGER | Primary key, auto-increment |
| game_id | INTEGER | Foreign key to game_stats.id |
| team_name | TEXT | Name of the team performing the action |
| player_position | TEXT | Position of the player (e.g., GS1, GA1) |
| action_type | TEXT | Type of action (Goal, Miss) |
| player_name | TEXT | Name of the player |
| timestamp | TEXT | Time when the action occurred |
| sequence | INTEGER | Order of actions within a game |

**Indices:**
- `index_game_actions_game_id` on `game_id` column

**Foreign Keys:**
- `game_id` references `game_stats(id)` with CASCADE delete

### 4. Opposition Team Table
Stores information about opposition teams.

```
Table: opposition_teams
```

| Column | Type | Description |
|--------|------|-------------|
| id | INTEGER | Primary key, auto-increment |
| team_name | TEXT | Name of the opposition team |
| last_played_date | TEXT | Date when the team was last played against |

**Indices:**
- `index_opposition_teams_team_name` on `team_name` column

## Relationships

- A Team can have multiple GameStats (one-to-many)
- A GameStats can have multiple GameActions (one-to-many)
- GameActions belong to a specific GameStats (many-to-one)

## Migration History

### Migration 2 to 3
- Added game_actions table

### Migration 3 to 4
- Added opposition_teams table

### Migration 4 to 5
- Fixed opposition_teams table schema to include the index on team_name

### Migration 5 to 6
- Dropped the legacy 'log' column from game_stats table
- Added new columns to game_stats table:
  - game_start_time: Actual start time of the game
  - game_mode: Game configuration (e.g., "15m,4Q")
  - period_duration: Duration of each period in minutes

## Entity Classes

- `Team.java`: Represents a club team with players
- `GameStats.java`: Represents game summary information
- `GameAction.java`: Represents individual game actions
- `OppositionTeam.java`: Represents opposition teams

## DAO Classes

- `TeamDAO.java`: Data access for Team entities
- `GameStatsDAO.java`: Data access for GameStats entities
- `GameActionDAO.java`: Data access for GameAction entities
- `OppositionTeamDAO.java`: Data access for OppositionTeam entities