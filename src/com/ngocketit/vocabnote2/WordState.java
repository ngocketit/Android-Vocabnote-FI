package com.ngocketit.vocabnote2;

public interface WordState
{
    // No specified state, or unlearnt state by default
    public static final int STATE_NONE		= -1;

    public static final int STATE_UNLEARNT      = 0;

    // Needs review for pronunciation
    public static final int STATE_REVIEW_PRONUN	= 1;

    // Needs grammar review
    public static final int STATE_REVIEW_GRAMMA	= 2;

    // Needs phrasal verb review
    public static final int STATE_REVIEW_PHRASA	= 4;

    // Needs collocation review
    public static final int STATE_REVIEW_COLLOC	= 8;
    
    // This word is learnt, e.g, no review needed
    public static final int STATE_LEARNT	= 16;

    // Match any states  
    public static final int STATE_ALL = 
            STATE_LEARNT | 
            STATE_REVIEW_COLLOC | 
            STATE_REVIEW_PRONUN | 
            STATE_REVIEW_GRAMMA |
            STATE_REVIEW_PHRASA;
}
