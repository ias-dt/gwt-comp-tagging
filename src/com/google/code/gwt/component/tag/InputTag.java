package com.google.code.gwt.component.tag;

import com.google.gwt.dom.client.*;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 *
 * @author Palo Gressa <gressa@acemcee.com>
 */
public class InputTag<T extends Tag> extends Widget {

    /*
     * ------------------------------
     * -- Component DOM Elements --
     * ------------------------------
     * Main component DIV element
     */
    private Element component;
    /**
     * List that holds items
     */
    private Element tagList;
    /**
     * Suggestions list
     */
    private Element suggestionList;
    /**
     * Currently active input text
     */
    private InputElement inputText;
    /**
     * Span element which is positioned outside of screen for computing width of
     * new tag
     */
    private SpanElement widthSpanTester;
    /*
     * ----------------------------------
     * -- Component Business Objects --
     * ----------------------------------
     */
    /**
     * Tags
     */
    private List<ItemTag<T>> tags;
    /**
     * Reference to previously suggested tags
     */
    private List<T> suggestedTags;
    /**
     * New tags validator
     */
    private TagValidator tagValidator;

    /*
     * --------------------------------
     * -- Component Worker Objects --
     * --------------------------------
     */
    private int suggestionSynchroId = 0;
    /**
     * Delegete for getting suggestions for given input
     */
    private SuggestionCallback<T> suggestionDelegate;
    /**
     * Presenter for suggestions for given input
     */
    private SuggestionPresenter<T> suggestionPresenter;
    /**
     * Caret last position used to compute whether the focus of active element
     * should be switched to next / previous sibling
     */
    private int caretLastPosition = 0;
    private boolean allowWhiteSpaceInTag = false;
    private boolean allowDuplicates = false;
    private Timer t;
    /**
     * Mode of tag input
     */
    private Mode mode;

    public InputTag(List<T> tags) {
        // init tags
        this.tags = new ArrayList<ItemTag<T>>();
        // init layout
        initLayout(tags);
        // set default mode
        setMode(Mode.WRITE);
        // sed default presenter
        suggestionPresenter = new DefaultSuggestionPresenter<T>();
        // initialize suggestions
        suggestedTags = new ArrayList<T>();
    }

    public InputTag() {
        this(null);
    }

    public void setWidth(int pixels) {
        this.getElement().getStyle().setWidth(pixels, Unit.PX);
        suggestionList.getParentElement().getStyle().setWidth(pixels, Unit.PX);

    }

    @Override
    public void setWidth(String value) {
        DOM.setStyleAttribute(this.getElement(), "width", value);
        DOM.setStyleAttribute(suggestionList.getParentElement().<com.google.gwt.user.client.Element>cast(), "width", value);
    }

    private Element createTagWrapper() {
        /**
         * UL list wrapper
         */
        Element tagListWrapeer = DOM.createDiv();
        tagListWrapeer.setClassName("input-tag-list");
        DOM.setEventListener(tagListWrapeer.<com.google.gwt.user.client.Element>cast(), new EventListener() {

            @Override
            public void onBrowserEvent(Event event) {
                
                
                inputText.focus();
                
            }
        });
        DOM.sinkEvents(tagListWrapeer.<com.google.gwt.user.client.Element>cast(), Event.ONCLICK);
        return tagListWrapeer;
    }

    private void handleNewTag(Tag tag) {
        appendTag(tag);
        resetInputText();        
        if (Mode.SELECT_BOX.equals(mode)) {
            if (t != null) {
                t.cancel();
            }
            inputTextChanged(true);
        } else {
            hideSuggestions();
        }
        inputText.focus();

    }

    private void initLayout(List<T> tags) {
        // init main wrapper area
        component = DOM.createDiv();        
        Element tagListWrapeer = createTagWrapper();

        // init list area
        tagList = Document.get().createULElement();
        tagList.setClassName("input-tag-list-tags");

        // init
        tagListWrapeer.appendChild(tagList);
        component.appendChild(tagListWrapeer);
        setElement(component);

        Element e = DOM.createInputText();
        DOM.setStyleAttribute(e.<com.google.gwt.user.client.Element>cast(), "display", "none");
        component.appendChild(e);

        // insert first input text for tagging.
        initializeInputText();

        if (tags != null) {
            for (Tag tagItem : tags) {
                appendTag(tagItem);
            }
        }


        // init suggestion list
        suggestionList = Document.get().createULElement();
        suggestionList.setClassName("tags-suggestion-list");
        suggestionList.setId("suggestion-list");

        // suggestion list wrapper
        Element suggestionListWraper = DOM.createDiv();
        suggestionListWraper.setClassName("tags-suggestion-wrapper");
        suggestionListWraper.appendChild(suggestionList);
        suggestionListWraper.setId("suggestion-wrapper");

        component.appendChild(suggestionListWraper);
    }

    protected LIElement createTagLIElement(final EventListener listener, int eventBits) {
        // create list item
        final LIElement listItem = Document.get().createLIElement();
        listItem.setClassName("input-tag-list-item");
        listItem.setTabIndex(0);
        // Set event listeners
        DOM.setEventListener(listItem.<com.google.gwt.user.client.Element>cast(), new EventListener() {

            @Override
            public void onBrowserEvent(Event event) {
                if (listener != null) {
                    listener.onBrowserEvent(event);
                }
                if (event.getTypeInt() == Event.ONMOUSEOUT || event.getTypeInt() == Event.ONBLUR) {
                    listItem.removeClassName("input-tag-list-item-hover");
                } else if (event.getTypeInt() == Event.ONMOUSEOVER || event.getTypeInt() == Event.ONFOCUS) {
                    listItem.addClassName("input-tag-list-item-hover");
                }
            }
        });
        DOM.sinkEvents(listItem.<com.google.gwt.user.client.Element>cast(), Event.FOCUSEVENTS | Event.ONMOUSEOUT | Event.ONMOUSEOVER | eventBits);
        return listItem;
    }

    /**
     * Appends tag to InputTag component. Tag is placed right after last tag.
     *
     * @param tag representation
     * @return list item
     */
    public void appendTag(Tag tag) {
        // item
        final Element item = createTagLIElement(new EventListener() {

            @Override
            public void onBrowserEvent(Event event) {

                if (event.getTypeInt() == Event.ONKEYDOWN && (event.getKeyCode() == KeyCodes.KEY_BACKSPACE || event.getKeyCode() == KeyCodes.KEY_DELETE)) {
                    removeTag(event.getEventTarget().<com.google.gwt.dom.client.Element>cast());
                } else if (event.getTypeInt() == Event.ONKEYDOWN && event.getKeyCode() == KeyCodes.KEY_LEFT) {
                    shiftFocusLeft(event.getEventTarget().<com.google.gwt.dom.client.Element>cast());
                } else if (event.getTypeInt() == Event.ONKEYDOWN && event.getKeyCode() == KeyCodes.KEY_RIGHT) {
                    shiftFocusRight(event.getEventTarget().<com.google.gwt.dom.client.Element>cast());
                }
            }
        }, Event.ONKEYDOWN);
        item.addClassName("input-tag-list-box");
        item.addClassName("input-tag-list-item-deletable");

        // create tag text
        SpanElement tagSpan = Document.get().createSpanElement();
        tagSpan.setInnerText(tag.getTag());
        item.appendChild(tagSpan);

        // delete tag
        Element deleteAnchor = Document.get().createAnchorElement();
        deleteAnchor.setClassName("input-tag-list-tag-delete");
        DOM.setEventListener(deleteAnchor.<com.google.gwt.user.client.Element>cast(), new EventListener() {

            @Override
            public void onBrowserEvent(Event event) {
                if (event.getTypeInt() == Event.ONCLICK) {
                    removeTag(item);
                }
            }
        });
        DOM.sinkEvents(deleteAnchor.<com.google.gwt.user.client.Element>cast(), Event.ONCLICK);
        item.appendChild(deleteAnchor);
        // insert into DOM
        tagList.insertBefore(item, inputText.getParentElement());
        // insert into inner list
        getInputTags().add(new ItemTag(tag, item));
    }

    private void removeTag(Element listItem) {
        // we can remove tag only when mode allows it
        if (mode != Mode.READ) {

            // remove tag from inner tag list
            ItemTag t = null;
            for (ItemTag itemTag : getInputTags()) {
                if (itemTag.getListItem().equals(listItem)) {
                    t = itemTag;
                    break;
                }
            }
            if (t == null) {
                throw new NullPointerException("List item element that has to be removed was not found!");
            }
            getInputTags().remove(t);

            // make next sibling active
            shiftFocusRight(listItem);

            // remove tag from DOM
            listItem.removeFromParent();
        }
    }

    private void handleNewTag() {
        String value = inputText.getValue();
        if (value.trim().length() > 0) {
            // find possilbe tag in suggested tags
            Tag tag = findInSuggestedTags(value);

            if (mode == Mode.SELECT_BOX && tag == null) {
                return;
            }

            // if we did not found tag from oracle then create new one
            if (tag == null) {
                if (tagValidator != null && !tagValidator.isValid(value)) {
                    return;
                }
                tag = new Tag(null, value);
            }
            handleNewTag(tag);
        }
    }

    /*
     * ----------------------------------------------------------------------------
     * ---- I N P U T T E X T ----
     * ----------------------------------------------------------------------------
     */
    private void inputTextChanged(boolean force) {

        final String text = inputText.getValue();

        // update input text width
        widthSpanTester.setInnerText(text);
        inputText.getStyle().setWidth(widthSpanTester.getOffsetWidth() + 20, Unit.PX);

        // try suggestion oraculum for tags
        if (getSuggestionDelegate() != null && (force ? true : text.length() > 0)) {
            final int newSynchroId = ++suggestionSynchroId;

            // hide and clear suggestions
            hideSuggestions();

            // clear suggestions
            suggestedTags.clear();
            // clear suggestions element
            while (suggestionList.hasChildNodes()) {
                suggestionList.removeChild(suggestionList.getFirstChild());
            }

            getSuggestionDelegate().findSuggestions(text, new SuggestionCallback.Callback<T>() {

                @Override
                public int getId() {
                    return newSynchroId;
                }

                @Override
                public boolean found(List<T> suggestions) {
                    if (newSynchroId != suggestionSynchroId) {
                        return false;
                    }


                    // we will filter out already suggested tags 
                    if (!isAllowDuplicates()) {
                        removeDuplicates(suggestions);
                    }
                    suggestedTags = suggestions;
                    if (suggestedTags.size() > 0) {
                        suggestionList.getStyle().setDisplay(Display.BLOCK);
                        // create suggestions list items
                        for (int i = 0; i < suggestedTags.size(); i++) {
                            final T tag = suggestedTags.get(i);
                            LIElement suggestionElement = createSuggestionElement(tag);
                            getSuggestionPresenter().createSuggestion(suggestionElement, tag, text);
                            suggestionList.appendChild(suggestionElement);
                            // if select mode, then we select the first
                            if (i == 0 && getMode().equals(Mode.SELECT_BOX)) {
                                suggestionElement.addClassName("tags-suggestion-list-suggestion-focus");
                            }
                        }
                    }
                    return true;
                }

                private void removeDuplicates(List<T> suggestions) {
                    for (T alreadyChosen : getTags()) {
                        T matched = null;
                        for (T t : suggestions) {
                            if (alreadyChosen.equals(t)) {
                                matched = t;
                                break;
                            }

                        }
                        if (matched != null) {
                            suggestions.remove(matched);
                        }
                    }
                }
            });


        }
    }

    /**
     * Initializes inputText element for inserting new tags by keyboard. There
     * is only one inputText in time.
     */
    protected void initializeInputText() {
        inputText = (DOM.createInputText()).cast();
        inputText.setClassName("input-tag-list-tag-input");
        inputText.setTabIndex(0);
        DOM.setEventListener(inputText.<com.google.gwt.user.client.Element>cast(), new EventListener() {

            @Override
            public void onBrowserEvent(Event event) {

                //
                // Proces when key is pressed. handles only confirmation keys
                //
                if (event.getTypeInt() == Event.ONKEYPRESS) {
                    // enter is working only when no suggestion is selected
                    if (event.getKeyCode() == KeyCodes.KEY_ENTER) {
                        Node node = null;
                        int i = 0;
                        for (; i < suggestionList.getChildCount(); i++) {
                            if (hasNodeStyleClass(suggestionList.getChild(i), "tags-suggestion-list-suggestion-focus")) {
                                node = suggestionList.getChild(i);
                                break;
                            }
                        }

                        if (node != null) {
                            handleNewTag(suggestedTags.get(i));
                        } else {
                            handleNewTag();
                        }
                        // proces new tags when spacebar is hit
                    } else if (event.getCharCode() == 32 && !isAllowWhiteSpaceInTag()) {
                        handleNewTag();
                    }

                    //
                    // Handles input changed only when input is alfanumeric. Recounts input width and updates input text
                    //                            
                } else if (event.getTypeInt() == Event.ONKEYUP && (isAlfaNumericKey(event.getKeyCode()) || event.getKeyCode() == KeyCodes.KEY_BACKSPACE)) {
                    inputTextChanged(false);

                    //
                    // Handles input focus 
                    //                                
                } else if (event.getTypeInt() == Event.ONFOCUS) {
                    inputText.getParentElement().addClassName("input-tag-list-tag-focus");
                    // we will show all suggestions
                    if (Mode.SELECT_BOX.equals(mode)) {
                        inputTextChanged(true);
                    }

                    //
                    // Handles input focus steal - only when source is not by click 
                    // and click has been made above suggestion list
                    //                                
                } else if (event.getTypeInt() == Event.ONBLUR) {
                    inputText.getParentElement().removeClassName("input-tag-list-tag-focus");
                    /**
                     * TODO(somebody): find better solution how to catch BLUR,
                     * and hideSuggestions when focus was stolen by clicking on
                     * suggestion. If we hide suggestion list too soon, then
                     * ONCLICK event will be not fired and suggestion won't be
                     * selected.
                     */
                    t = new Timer() {

                        @Override
                        public void run() {
                            hideSuggestions();
                            t = null;
                        }
                    };
                    t.schedule(100);

                    //
                    // Handles update of caret position (to decide whether to jump to previous tag
                    // OR handles navigation instide of suggestion list
                    //                                
                } else if (event.getTypeInt() == Event.ONKEYDOWN) {
                    if ((caretLastPosition == 1 || caretLastPosition == 0) && getCursorPos(inputText) == 0) {
                        if (event.getKeyCode() == KeyCodes.KEY_BACKSPACE || event.getKeyCode() == KeyCodes.KEY_LEFT) {
                            shiftFocusLeft(inputText.getParentNode().<com.google.gwt.user.client.Element>cast());
                        }
                        caretLastPosition = getCursorPos(inputText);
                    }

                    if (event.getKeyCode() == KeyCodes.KEY_DOWN || event.getKeyCode() == KeyCodes.KEY_UP) {
                        // we'll try to find if there is some suggestion 
                        if (suggestionList.getChildCount() > 0) {
                            boolean found = false;
                            for (int i = 0; i < suggestionList.getChildCount(); i++) {
                                Node node = suggestionList.getChild(i);
                                if (hasNodeStyleClass(node, "tags-suggestion-list-suggestion-focus")) {
                                    if (event.getKeyCode() == KeyCodes.KEY_DOWN) {
                                        if (node.getNextSibling() != null) {
                                            node.getNextSibling().<com.google.gwt.dom.client.Element>cast().addClassName("tags-suggestion-list-suggestion-focus");
                                            node.<com.google.gwt.dom.client.Element>cast().removeClassName("tags-suggestion-list-suggestion-focus");
                                        }
                                    } else {
                                        if (node.getPreviousSibling() != null) {
                                            node.getPreviousSibling().<com.google.gwt.dom.client.Element>cast().addClassName("tags-suggestion-list-suggestion-focus");
                                            node.<com.google.gwt.dom.client.Element>cast().removeClassName("tags-suggestion-list-suggestion-focus");
                                        }
                                    }
                                    found = true;
                                    break;
                                }
                            }
                            // we mark the first one
                            if (!found && event.getKeyCode() == KeyCodes.KEY_DOWN) {
                                suggestionList.getChild(0).<com.google.gwt.dom.client.Element>cast().addClassName("tags-suggestion-list-suggestion-focus");
                            }
                        }
                    }
                }
            }
        });
        DOM.sinkEvents(inputText.<com.google.gwt.user.client.Element>cast(), Event.ONKEYPRESS | Event.ONKEYDOWN | Event.ONKEYUP | Event.FOCUSEVENTS);
        widthSpanTester = Document.get().createSpanElement();
        widthSpanTester.setAttribute("style", "float: left; left: -1000px; position: absolute; display: inline-block;");

        // create list item element and append all items
        Element item = createTagLIElement(new EventListener() {

            @Override
            public void onBrowserEvent(Event event) {
                if (event.getTypeInt() == Event.ONFOCUS) {
                    inputText.focus();
                    if (Mode.SELECT_BOX.equals(mode)) {
                        inputTextChanged(true);
                    }
                }

            }
        }, Event.ONFOCUS);

        item.addClassName("input-tag-list-tag-editable");
        item.appendChild(inputText);

        item.appendChild(widthSpanTester);

        // append new list item into list
        tagList.appendChild(item);
    }

    private void resetInputText() {
        // reset input text
        inputText.setValue("");
        inputText.getStyle().setWidth(50, Unit.PX);
    }

    /*
     * ----------------------------------------------------------------------------     
     * ---- S U G G E S T I O N S ----     
     * ----------------------------------------------------------------------------
     */
    private static int l = 0;

    protected LIElement createSuggestionElement(final Tag tag) {
        // create list item
        final LIElement listItem = Document.get().createLIElement();
        listItem.setId("suggestion-" + (l++));
        listItem.setClassName("tags-suggestion-list-suggestion");
        listItem.setTabIndex(0);
        // Set event listeners
        DOM.setEventListener(listItem.<com.google.gwt.user.client.Element>cast(), new EventListener() {

            @Override
            public void onBrowserEvent(Event event) {
                //
                // There is no ONMOUSEOUT because every time when mouse over is
                // called , we will remove class about active tag.
                //
                if (event.getTypeInt() == Event.ONMOUSEOVER) {
                    for (int i = 0; i < suggestionList.getChildCount(); i++) {
                        suggestionList.getChild(i).<com.google.gwt.user.client.Element>cast().removeClassName("tags-suggestion-list-suggestion-focus");
                    }
                    listItem.addClassName("tags-suggestion-list-suggestion-focus");
                    // 
                    // Handles suggestion from suggestion list
                    // 
                } else if (event.getTypeInt() == Event.ONCLICK) {
                    handleNewTag(tag);

                }
            }
        });
        DOM.sinkEvents(listItem.<com.google.gwt.user.client.Element>cast(), Event.ONMOUSEOVER | Event.ONCLICK);
        return listItem;
    }

    private void hideSuggestions() {
        suggestionList.getStyle().setDisplay(Display.NONE);
    }

    private Tag findInSuggestedTags(String value) {
        if (suggestedTags != null) {
            for (Tag tag : suggestedTags) {
                if (tag.getTag().equalsIgnoreCase(value)) {
                    return tag;
                }
            }
        }
        return null;
    }

    /*
     * ----------------------------------------------------------------------------
     * ---- U T I L S ----
     * ----------------------------------------------------------------------------
     */
    private static void shiftFocusLeft(Element listItem) {
        Node sib = listItem.getPreviousSibling();
        if (sib != null) {
            sib.<com.google.gwt.user.client.Element>cast().focus();
        }
    }

    private static void shiftFocusRight(Element listItem) {
        Element sib = listItem.getNextSiblingElement();
        if (sib != null) {
            sib.focus();
        }
    }
    
    private static boolean hasNodeStyleClass(Node node, String className) {
        String c = DOM.getElementAttribute(node.<com.google.gwt.user.client.Element>cast(), "class");
        if (c != null && c.length() > 0) {
            String[] classes = c.split(" ");
            for (int i = 0; i < classes.length; i++) {
                if (classes[i].equalsIgnoreCase(className)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isAlfaNumericKey(int key) {
        return !isSystemKey(key);
    }

    /**
     * Returns
     * <code>true</code> if event.getKeyCode is system key like: Enter,
     * Backspace, Alt.. <br/> All non system keycodes has key 0.
     *
     * @param keyCode
     * @return
     */
    private static boolean isSystemKey(int keyCode) {
        return keyCode == KeyCodes.KEY_ALT
                || keyCode == KeyCodes.KEY_BACKSPACE
                || keyCode == KeyCodes.KEY_CTRL
                || keyCode == KeyCodes.KEY_DELETE
                || keyCode == KeyCodes.KEY_DOWN
                || keyCode == KeyCodes.KEY_END
                || keyCode == KeyCodes.KEY_ENTER
                || keyCode == KeyCodes.KEY_ESCAPE
                || keyCode == KeyCodes.KEY_HOME
                || keyCode == KeyCodes.KEY_LEFT
                || keyCode == KeyCodes.KEY_PAGEDOWN
                || keyCode == KeyCodes.KEY_PAGEUP
                || keyCode == KeyCodes.KEY_RIGHT
                || keyCode == KeyCodes.KEY_SHIFT
                || keyCode == KeyCodes.KEY_TAB
                || keyCode == KeyCodes.KEY_UP;
    }

    /**
     * Copy from {@link com.google.gwt.user.client.ui.ValueBoxBase}
     */
    public native int getCursorPos(Element elem) /*-{
    // Guard needed for FireFox.
    try{
    return elem.selectionStart;
    } catch (e) {
    return 0;
    }
    }-*/;

    /**
     * @return the mode
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * @param mode the mode to set
     */
    public final void setMode(Mode mode) {
        this.mode = mode;
        switch (mode) {
             case WRITE:
                setEditable(true);
                component.setClassName("input-tag input-tag-mode-write");
                break;
            case SELECT_BOX:
                setEditable(true);
                component.setClassName("input-tag input-tag-mode-select-box");
                break;
            case READ:
                setEditable(false);
                component.setClassName("input-tag input-tag-mode-read-only");
                break;                        
        }
    }

    
    /**
     * Removes all tags
     */
    public void clear(){        
        if(!getInputTags().isEmpty()){
            
            for(ItemTag tag : getInputTags()){
                tag.listItem.removeFromParent();
            }
            getInputTags().clear();            
        }
    }
    
    /**
     * Make component only read only. This method has same effect when setting
     * mode to {@link Mode#READ}.
     *
     * @param value boolean value, when
     * <code>true</code> then component is editable otherwise is not
     */
    public void setEditable(boolean value) {
        if (inputText.getParentElement().getParentElement() == null && value) {
            tagList.appendChild(inputText.getParentElement());
            for (ItemTag<T> itemTag : getInputTags()) {
                itemTag.listItem.getFirstChildElement().getNextSiblingElement().getStyle().setVisibility(Visibility.VISIBLE);
                itemTag.listItem.addClassName("input-tag-list-item-deletable");
            }
        } else if (inputText.getParentElement().getParentElement() != null && !value) {
            inputText.getParentElement().removeFromParent();
            for (ItemTag<T> itemTag : getInputTags()) {
                itemTag.listItem.getFirstChildElement().getNextSiblingElement().getStyle().setVisibility(Visibility.HIDDEN);
                itemTag.listItem.removeClassName("input-tag-list-item-deletable");
            }
        }
        
        if(value){
            component.removeClassName("input-tag-mode-read-only");
        }else{
            component.addClassName("input-tag-mode-read-only");
        }
        
    }

    /**
     * Delegete for getting suggestions for given input
     *
     * @return the suggestionDelegate
     */
    public SuggestionCallback<T> getSuggestionDelegate() {
        return suggestionDelegate;
    }

    /**
     * Delegete for getting suggestions for given input
     *
     * @param suggestionDelegate the suggestionDelegate to set
     */
    public void setSuggestionDelegate(SuggestionCallback<T> suggestionDelegate) {
        this.suggestionDelegate = suggestionDelegate;
    }

    /**
     * @return the suggestionPresenter
     */
    public SuggestionPresenter<T> getSuggestionPresenter() {
        return suggestionPresenter;
    }

    /**
     * @param suggestionPresenter the suggestionPresenter to set
     */
    public void setSuggestionPresenter(SuggestionPresenter<T> suggestionPresenter) {
        this.suggestionPresenter = suggestionPresenter;
    }

    /**
     * @return the tagValidator
     */
    public TagValidator getTagValidator() {
        return tagValidator;
    }

    /**
     * @param tagValidator the tagValidator to set
     */
    public void setTagValidator(TagValidator tagValidator) {
        this.tagValidator = tagValidator;
    }

    /**
     * @return the allowWhiteSpaceInTag
     */
    public boolean isAllowWhiteSpaceInTag() {
        return allowWhiteSpaceInTag;
    }

    /**
     * @param allowWhiteSpaceInTag the allowWhiteSpaceInTag to set
     */
    public void setAllowWhiteSpaceInTag(boolean allowWhiteSpaceInTag) {
        this.allowWhiteSpaceInTag = allowWhiteSpaceInTag;
    }

    /**
     * @return the tags
     */
    private List<ItemTag<T>> getInputTags() {
        return tags;
    }

    public List<T> getTags() {
        List<T> t = new ArrayList(tags.size());
        for (ItemTag<T> t1 : tags) {
            t.add(t1.getTag());
        }
        return t;
    }

    /**
     * @return the allowDuplicates
     */
    public boolean isAllowDuplicates() {
        return allowDuplicates;
    }

    /**
     * @param allowDuplicates the allowDuplicates to set
     */
    public void setAllowDuplicates(boolean allowDuplicates) {
        this.allowDuplicates = allowDuplicates;
    }

    /**
     * Inner private class that identifies existing tags with list elements.
     */
    private class ItemTag<T extends Tag> {

        private T tag;
        private Element listItem;

        public ItemTag(T tag, Element listItem) {
            this.tag = tag;
            this.listItem = listItem;
        }

        public Element getListItem() {
            return listItem;
        }

        public T getTag() {
            return tag;
        }
    }

    /**
     * Enum that defines modes component modes.
     */
    public enum Mode {

        /**
         * Read only mode, no other tags can be inserted
         */
        READ,
        /**
         * Tags can be only from given set of items provided by {@link ... }
         */
        SELECT_BOX,
        /**
         * Free tags
         */
        WRITE;
    }
}
