package com.bot.worker.common;

import com.google.inject.BindingAnnotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Application wide binding annotations annotations
 *
 * @see com.bot.worker.cli.CliModule
 * @see com.bot.worker.taskmanager.TaskManager
 * @author Aleks
 */
public class Annotations {

  /**
   * Amount of threads in app to process tasks, keep this value appropriate to amount of tasks
   * @see com.bot.worker.cli.BootOptions
   */
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
