package com.google.code.gwt.component.client.example;

import com.google.code.gwt.component.tag.*;
import com.google.code.gwt.component.tag.SuggestionCallback.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Palo Gressa <gressa@acemcee.com>
 */
public class TaggingEntryPoint implements EntryPoint {

    @Override
    public void onModuleLoad() {




        List<StringTag> items = new ArrayList<StringTag>();
        items.add(new StringTag("Tag 1", "Tag 1"));
        items.add(new StringTag("Tag 2", "Tag 2"));
        items.add(new StringTag("Tag 3", "Tag 3"));

        InputTag<StringTag> ti1 = new InputTag<StringTag>(items);
        ti1.setAllowWhiteSpaceInTag(true);
        ti1.setWidth("400px");
        ti1.setSuggestionDelegate(new SuggestionDelegateMock());

        InputTag<StringTag> ti2 = new InputTag<StringTag>(items);
        ti2.setWidth("400px");
        ti2.setMode(InputTag.Mode.READ);

        InputTag<StringTag> ti3 = new InputTag<StringTag>(items);
        ti3.setWidth("400px");
        ti3.setSuggestionDelegate(new SuggestionDelegateMock());
        ti3.setMode(InputTag.Mode.SELECT_BOX);
        
        SelectBoxInputTag<StringTag> ti4 = new SelectBoxInputTag<StringTag>();
        ti4.setSelectBoxTags(items);
        ti4.setWidth("400px");
                 

        RootPanel.get("test").add(ti1);
        RootPanel.get("test").add(ti2);
        RootPanel.get("test").add(ti3);
        RootPanel.get("test").add(ti4);

    }

    protected class SuggestionDelegateMock implements SuggestionCallback<StringTag> {

        @Override
        public void findSuggestions(String text, Callback callback) {
            List<StringTag> suggestions = new ArrayList<StringTag>();
            List<String> plain = new ArrayList<String>();
            plain.add("johnny");
            plain.add("joging");
            plain.add("jasper");
            plain.add("java");
            plain.add("javascipt");
            plain.add("javamania");
            plain.add("jackson");
            plain.add("jarvana");
            plain.add("jabadabadoo");
            plain.add("joseph");
            plain.add("jerry");

            for (String string : plain) {
                if (string.startsWith(text)) {
                    suggestions.add(new StringTag(text, string));
                }
            }

            callback.found(suggestions);
        }
    }
}
