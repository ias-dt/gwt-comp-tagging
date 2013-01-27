package com.google.code.gwt.component.tag;

import java.util.List;

/**
 * Interface for implementing suggestion callback that is called right after the
 * content of inputText is changed.
 * 
 * @author Pavol Gressa <gressa@acemcee.com>
 * @param <T> 
 */
public interface SuggestionCallback<T extends Tag> {

    
    
    /**
     * Method is called right after the content in inputText is called. 
     * @param text
     * @param callback 
     */
    public void findSuggestions(String text, Callback callback);
        
    /**
     * Callback interface for notify {@link InputTag} about new suggestions has
     * been found.
     */
    public interface Callback<T>{
        /**
         * ID for synchronization
         * @return 
         */
        public int getId();
        
        /**
         * Method by which are new suggestions inserted into suggestion list. Suggestions
         * appears only when {@link #getId()} is equal to actual synchronization id.
         * @param suggestions 
         * @return <code>true</code> when suggested items were used, otherwise synchronization id was not actual.
         */
        public boolean found(List<T> suggestions);
        
    }   
    
}



