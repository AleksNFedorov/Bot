package com.bot.worker.common;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Aleks on 11/17/16.
 */
public class Annotations {

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @BindingAnnotation
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ThreadsCount {
    }

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @BindingAnnotation
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TaskConfigFile {
    }
}
