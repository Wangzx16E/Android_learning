package kh.hyper.utils;

import java.lang.annotation.Annotation;

public class AnnotationUtil {
  public static <T extends Annotation> T getAnnotation(Class<?> src, Class<T> annotationType) {
    T annotation = src.getAnnotation(annotationType);
    if (annotation == null) {
      Class<?>[] itfs = src.getInterfaces();
      for (Class<?> itf : itfs) {
        T a = itf.getAnnotation(annotationType);
        if (a != null) {
          annotation = a;
        }
      }
    }
    return annotation;
  }
}
