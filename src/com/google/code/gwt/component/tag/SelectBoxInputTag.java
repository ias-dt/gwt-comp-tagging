/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.google.code.gwt.component.tag;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates lightweight component which simplifies using {@link InputTag} mode
 * SELECT_BOX.
 *
 * @author palo
 */
public class SelectBoxInputTag<T extends Tag> extends InputTag<T> {

    private List<T> selectBoxTags;

    public SelectBoxInputTag() {
        setMode(Mode.SELECT_BOX);
        setSuggestionDelegate(new SelectBoxSuggestionCallback());
    }

    public void setSelectBoxTags(List<T> selectBoxTags) {
        this.selectBoxTags = selectBoxTags;
    }

    private class SelectBoxSuggestionCallback implements SuggestionCallback {

        @Override
        public void findSuggestions(String text, Callback callback) {
            if (selectBoxTags != null && !selectBoxTags.isEmpty()) {
                List<T> suggestions = new ArrayList<T>();
                if (text == null || text.trim().length() == 0) {
                    suggestions.addAll(selectBoxTags);
                } else {
                    for (T t : selectBoxTags) {
                        if (t.canBeSuggested(text)) {
                            suggestions.add(t);
                        }
                    }
                }
                
                callback.found(suggestions);
            }

        }
    }
}
