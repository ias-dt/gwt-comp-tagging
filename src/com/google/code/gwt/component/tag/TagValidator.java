package com.google.code.gwt.component.tag;

/**
 * Interface validator class for testing new tags that are inserted as plain tags from
 * input text. Only plain tags are validated, suggested tags are not validated.
 * <br/>
 * Validation works only when actual mode is set to {@link InputTag#mode} == WRITE.
 * @author Palo Gressa <gressa@acemcee.com>
 */
public interface TagValidator {
    
    public boolean isValid(String tag);
    
    
}
