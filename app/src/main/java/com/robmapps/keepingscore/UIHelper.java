package com.robmapps.keepingscore;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

/**
 * Helper class for UI operations
 */
public class UIHelper {
    private final Context context;
    private ImageView centrePassCircle;
    private ObjectAnimator animator;
    private boolean movingToEndLocation = true;
    private float startX, startY, endX, endY;
    
    public UIHelper(Context context) {
        this.context = context;
    }
    
    /**
     * Sets up the UI by adding touch listeners to hide keyboard when touching outside EditText fields
     * Recursively applies to all views in the hierarchy
     * 
     * @param view The root view to set up
     */
    public void setupUI(View view) {
        setupTouchListenerForView(view);
        setupChildViews(view);
    }
    
    /**
     * Sets up touch listener for a view to hide keyboard when touched
     * 
     * @param view The view to set up
     */
    private void setupTouchListenerForView(View view) {
        // Only add touch listener to non-EditText views
        if (!(view instanceof EditText)) {
            view.setOnTouchListener((v, event) -> {
                hideSoftKeyboard();
                clearFocusFromEditTexts();
                return false; // Allow the touch event to continue to underlying views
            });
        }
    }
    
    /**
     * Recursively sets up child views in a ViewGroup
     * 
     * @param view The parent view
     */
    private void setupChildViews(View view) {
        // If the view is a container, recursively set up its children
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View innerView = viewGroup.getChildAt(i);
                setupUI(innerView);
            }
        }
    }
    
    /**
     * Hides the soft keyboard
     */
    public void hideSoftKeyboard() {
        try {
            if (context instanceof Activity) {
                Activity activity = (Activity) context;
                InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                
                // Find the currently focused view to get a window token
                View focusedView = activity.getCurrentFocus();
                
                // If no view has focus, create a temporary one
                if (focusedView == null) {
                    focusedView = new View(activity);
                }
                
                inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
            }
        } catch (Exception e) {
            Log.e("UIHelper", "Error hiding keyboard", e);
        }
    }
    
    /**
     * Clears focus from any focused EditText
     */
    public void clearFocusFromEditTexts() {
        try {
            if (context instanceof Activity) {
                Activity activity = (Activity) context;
                View focusedView = activity.getCurrentFocus();
                
                if (focusedView instanceof EditText) {
                    focusedView.clearFocus();
                }
            }
        } catch (Exception e) {
            Log.e("UIHelper", "Error clearing focus", e);
        }
    }
    
    /**
     * Sets up the centre pass animation
     */
    public void setupCentrePassAnimation(ImageView centrePassCircle, View score1View, View score2View) {
        this.centrePassCircle = centrePassCircle;
        
        int[] score1Coordinates = new int[2];
        int[] score2Coordinates = new int[2];
        score1View.getLocationOnScreen(score1Coordinates);
        score2View.getLocationOnScreen(score2Coordinates);
        
        // Adjust animation based on screen orientation
        boolean isLandscape = ScreenSizeHelper.isLandscape(context);
        
        if (isLandscape) {
            // In landscape mode, adjust animation path
            startX = score1Coordinates[0] + (score1View.getWidth() / 2f) - (centrePassCircle.getWidth() / 2f);
            startY = score1Coordinates[1] + (score1View.getHeight() / 2f) - (centrePassCircle.getHeight() / 2f);
            endX = score2Coordinates[0] + (score2View.getWidth() / 2f) - (centrePassCircle.getWidth() / 2f);
            endY = score2Coordinates[1] + (score2View.getHeight() / 2f) - (centrePassCircle.getHeight() / 2f);
        } else {
            // In portrait mode, use original animation path
            startX = score1Coordinates[0] + (score1View.getWidth() / 2f) - (centrePassCircle.getWidth() / 2f);
            startY = score1Coordinates[1] - (score1View.getHeight() / 2f) - (centrePassCircle.getHeight() / 3f);
            endX = score2Coordinates[0] + (score2View.getWidth() / 2f) - (centrePassCircle.getWidth() / 2f);
            endY = score2Coordinates[1] - (score2View.getHeight() / 2f) - (centrePassCircle.getHeight() / 3f);
        }
        
        centrePassCircle.setX(startX);
        centrePassCircle.setY(startY);
        centrePassCircle.setVisibility(View.VISIBLE);
    }
    
    /**
     * Animates the centre pass circle between teams
     */
    public void animateCentrePass() {
        // Validate that animation can be performed
        if (centrePassCircle == null || endX == 0) {
            return;
        }
        
        // Show the animation circle
        centrePassCircle.setVisibility(View.VISIBLE);
        
        // Check if we're in landscape mode
        boolean isLandscape = ScreenSizeHelper.isLandscape(context);
        
        // Determine target coordinates based on the current direction and orientation
        if (isLandscape) {
            // In landscape, animate horizontally (X axis)
            float targetX = movingToEndLocation ? endX : startX;
            animator = ObjectAnimator.ofFloat(centrePassCircle, "X", centrePassCircle.getX(), targetX);
        } else {
            // In portrait, animate vertically (Y axis)
            float targetY = movingToEndLocation ? endY : startY;
            animator = ObjectAnimator.ofFloat(centrePassCircle, "Y", centrePassCircle.getY(), targetY);
        }
        animator.setDuration(200); // Ensure smooth animation both ways
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        // Add animation listener
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                movingToEndLocation = !movingToEndLocation; // Flip direction for next movement
            }
            
            @Override
            public void onAnimationStart(Animator animation) {
                // Not used
            }
            
            @Override
            public void onAnimationCancel(Animator animation) {
                // Not used
            }
            
            @Override
            public void onAnimationRepeat(Animator animation) {
                // Not used
            }
        });
        
        // Start the animation
        animator.start();
    }
    
    /**
     * Triggers device vibration with the specified pattern
     * 
     * @param pattern The vibration pattern to use
     */
    public void vibrate(long[] pattern) {
        try {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(pattern, -1); // -1 means don't repeat
            }
        } catch (Exception e) {
            Log.e("UIHelper", "Error triggering vibration", e);
        }
    }
}