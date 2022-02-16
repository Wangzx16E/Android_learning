package kh.hyper.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodEventHandler extends EventHandler<Object> {
  private Object target;
  private Method method;

  @SuppressWarnings("unchecked")
  public MethodEventHandler(Class<?> eventType, Object target, Method method, HandleThreadType handleThreadType) {
    super((Class<Object>) eventType, handleThreadType);

    this.target = target;
    this.method = method;
  }

  @Override
  public boolean onEvent(Object event) {
    Object ret = null;
    try {
      method.setAccessible(true);
      ret = method.invoke(target, event);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    if (ret != null && ret instanceof Boolean) {
      return ((Boolean) ret).booleanValue();
    }
    return false;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((method == null) ? 0 : method.hashCode());
    result = prime * result + ((target == null) ? 0 : target.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    MethodEventHandler other = (MethodEventHandler) obj;
    if (method == null) {
      if (other.method != null)
        return false;
    } else if (!method.equals(other.method))
      return false;
    if (target == null) {
      if (other.target != null)
        return false;
    } else if (!target.equals(other.target))
      return false;
    return true;
  }
}
