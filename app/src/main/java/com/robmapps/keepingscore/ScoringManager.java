package com.robmapps.keepingscore;

import android.widget.TextView;

import java.util.List;

/**
 * Manages scoring functionality and player statistics
 */
public class ScoringManager {
    private final SharedViewModel viewModel;
    private String gsPlayer;
    private String gaPlayer;
    private String timeFormatted;
    
    public interface ScoringListener {
        void onScoreUpdated(boolean isTeam1, boolean isSuccessful);
    }
    
    private ScoringListener listener;
    
    public ScoringManager(SharedViewModel viewModel) {
        this.viewModel = viewModel;
    }
    
    public void setListener(ScoringListener listener) {
        this.listener = listener;
    }
    
    public void setPlayerNames(String gsPlayer, String gaPlayer) {
        this.gsPlayer = gsPlayer;
        this.gaPlayer = gaPlayer;
    }
    
    public void setTimeFormatted(String timeFormatted) {
        this.timeFormatted = timeFormatted;
    }
    
    /**
     * Records a scoring attempt and updates the score if successful
     * 
     * @param isTeam1 Whether this is for team 1 (true) or team 2 (false)
     * @param playerPosition The position of the player (GS1, GA1, etc.)
     * @param playerName The name of the player
     * @param isSuccessful Whether the scoring attempt was successful
     */
    public void recordScoringAttempt(boolean isTeam1, String playerPosition, String playerName, boolean isSuccessful) {
        if (isSuccessful) {
            if (isTeam1) {
                viewModel.updateTeam1Score(1);
            } else {
                viewModel.updateTeam2Score(1);
            }
            
            if (listener != null) {
                listener.onScoreUpdated(isTeam1, isSuccessful);
            }
        }

        playerName = resolvePlayerName(playerPosition, playerName);
        viewModel.recordAttempt(playerName, playerPosition, isSuccessful, timeFormatted);
    }
    
    /**
     * Resolves the player name based on position
     */
    private String resolvePlayerName(String playerPosition, String playerName) {
        if (playerPosition.equals("GS1")) {
            return gsPlayer;
        } else if (playerPosition.equals("GA1")) {
            return gaPlayer;
        } else {
            return "Other Team";
        }
    }
    
    /**
     * Undoes the last scoring action
     */
    public void undoLastAction() {
        List<ScoringAttempt> currentActions = viewModel.getAllActions().getValue();
        if (currentActions != null && !currentActions.isEmpty()) {
            ScoringAttempt lastAction = currentActions.remove(currentActions.size() - 1);
            viewModel.updateAllActions(currentActions);
            
            if (lastAction.isSuccessful()) {
                adjustScoreForUndo(lastAction);
            }
        }
    }
    
    /**
     * Adjusts the score when undoing a successful goal
     */
    private void adjustScoreForUndo(ScoringAttempt lastAction) {
        String position = lastAction.getPlayerPosition();
        boolean isTeam1 = position.startsWith("GS1") || position.startsWith("GA1");
        
        if (isTeam1) {
            viewModel.updateTeam1Score(-1);
        } else if (position.startsWith("GS2") || position.startsWith("GA2")) {
            viewModel.updateTeam2Score(-1);
        }
        
        if (listener != null) {
            listener.onScoreUpdated(isTeam1, false);
        }
    }
    
    /**
     * Resets all scores to zero
     */
    public void resetScores() {
        viewModel.resetGame();
    }
}