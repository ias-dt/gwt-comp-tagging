package com.google.code.gwt.component.tag;

import java.io.Serializable;


/**
 * Abstract representation of tag. Tag consists from plain string that represents
 * visible tag in component <code>tag</code> and from value that identities tag
 * in backend system. There are implementations for most use cases {@link IntTag},
 * {@link StringTag}
 *
 * @author Palo Gressa <gressa@acemcee.com>
 */
public class Tag<T> implements Serializable{
    private static final long serialVersionUID = -5381676167463921282L;

    public T value;
    public String tag;

    public Tag() {
    }

    public Tag(T value, String tag) {
        this.value = value;
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public T getValue() {
        return value;
    }
    
    public boolean canBeSuggested(String input){
        return input.toLowerCase().startsWith(tag);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Tag){
           Tag t = (Tag)obj; 
           return t.tag.equalsIgnoreCase(tag);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.tag != null ? this.tag.hashCode() : 0);
        return hash;
    }
    
    
}
