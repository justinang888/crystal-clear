package com.spring.aurora.config.web;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.view.json.JacksonJsonView;

import jakarta.servlet.http.HttpServletRequest;
import tools.jackson.databind.json.JsonMapper;

/**
 * Renders a controller's model as JSON for the React app. The controllers still fill a Model and
 * return a view name as they did for the JSPs; this view sends that model instead of a page.
 *
 * The JSON holds every model attribute, plus:
 *   "view"   - the view name the controller chose (for example "new-customer" after a failed save),
 *   "errors" - field name to message for each validation error on a form object.
 */
public class ModelJsonView extends JacksonJsonView {

    private final String viewName;
    private final MessageSource messageSource;
    private final Locale locale;

    public ModelJsonView(String viewName, JsonMapper mapper, MessageSource messageSource, Locale locale) {
        super(mapper);
        this.viewName = viewName;
        this.messageSource = messageSource;
        this.locale = locale;
    }

    @Override
    protected Object filterModel(Map<String, Object> model, HttpServletRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, String> errors = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : model.entrySet()) {
            if (entry.getValue() instanceof BindingResult bindingResult) {
                for (FieldError error : bindingResult.getFieldErrors()) {
                    errors.putIfAbsent(error.getField(), message(error));
                }
                // reject("email", ...) is a global error whose code names the field.
                bindingResult.getGlobalErrors().forEach(error -> errors.putIfAbsent(error.getCode() != null
                        && error.getCode().startsWith("Pattern.custForm.email") ? "emailAddress" : "", message(error)));
            } else {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        result.put("view", this.viewName);
        result.put("errors", errors);
        return result;
    }

    /** The message from validation.properties, or a plain one for a code it does not list. */
    private String message(MessageSourceResolvable error) {
        return this.messageSource.getMessage(new DefaultMessageSourceResolvable(error.getCodes(), error.getArguments(),
                error.getDefaultMessage() != null ? error.getDefaultMessage() : "This value is not valid."), this.locale);
    }
}
