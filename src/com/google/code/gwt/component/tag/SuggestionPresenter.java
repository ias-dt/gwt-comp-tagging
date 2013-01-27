package com.google.code.gwt.component.tag;

import com.google.gwt.dom.client.Element;


/**
 * Interface class used for custom implementation of suggestion list items.
 * <br/>
 * By default, {@link DefaultSuggestionPresenter} is used. 
 * @author Palo Gressa <gressa@acemcee.com>
 */
public interface SuggestionPresenter<T extends Tag> {
    
    /**
     * Callback which is called for every suggested tag. Implementator should 
     * create suggestion element item content into list item element. 
     * @param e parent element of created content
     * @param tag suggested tag
     * @param text plain text from input text element by with tha suggestions are loaded
     */
    public void createSuggestion(Element e, T tag, String text);
}
