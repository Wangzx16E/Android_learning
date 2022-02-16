package kh.hyper.event;

import android.os.Handler;
import android.os.Looper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kh.hyper.utils.HL;

public enum EventManager {
  instance;

  private ThreadLocal<EventQueue> eventQueues;
  private Map<Class<?>, Collection<IEventHandler<?>>> handlerMap;
  private ExecutorService executorService;
  private Handler mainThreadHandler;

  private EventManager() {
    eventQueues = new ThreadLocal<EventQueue>() {
      @Override
      protected EventQueue initialValue() {
        return new EventQueue();
      }
    };
    executorService = Executors.newSingleThreadExecutor();
    mainThreadHandler = new Handler(Looper.getMainLooper());
    handlerMap = new ConcurrentHashMap<Class<?>, Collection<IEventHandler<?>>>();
  }

  public void clear() {
    handlerMap.clear();
  }

  public void registerEventHandler(IEventHandler<?> eventHandler) {
    Collection<IEventHandler<?>> handlerCollection = handlerMap.get(eventHandler.getEventType());
    if (handlerCollection == null) {
      handlerCollection = new CopyOnWriteArrayList<IEventHandler<?>>();
      handlerMap.put(eventHandler.getEventType(), handlerCollection);
    }

    if (!handlerCollection.contains(eventHandler)) {
      handlerCollection.add(eventHandler);
    }
  }

  public void unregisterEventHandler(IEventHandler<?> eventHandler) {
		Collection<IEventHandler<?>> handlerCollection = handlerMap.get(eventHandler.getEventType());
		if (handlerCollection != null && handlerCollection.contains(eventHandler)) {
      handlerCollection.remove(eventHandler);
  }
	}

  public void register(Object target) {
    List<IEventHandler<?>> handlerList = findMethodHandler(target);
		for (IEventHandler<?> handler : handlerList) {
      registerEventHandler(handler);
    }
	}

  public void unregister(Object target) {
    List<IEventHandler<?>> handlerList = findMethodHandler(target);
    for (IEventHandler<?> handler : handlerList) {
      unregisterEventHandler(handler);
    }
  }

  public void dispatch(Object event) {
    EventQueue queue = eventQueues.get();
    queue.add(event);
    if (!queue.isDispatching) {
      queue.isDispatching = true;
      dispatchInternal();
    }
  }

  private void dispatchInternal() {
    EventQueue queue = eventQueues.get();
    Object event = queue.poll();
    while (event != null) {
      Collection<IEventHandler<?>> handlerCollection = handlerMap.get(event.getClass());
      if (handlerCollection != null && !handlerCollection.isEmpty()) {
        for (IEventHandler<?> entity : handlerCollection) {
          if (entity.getEventType() == event.getClass()) {
            if (dispatchToHandler(event, entity)) {
              break;
            }
          }
        }
      }
      event = queue.poll();
    }
    queue.isDispatching = false;
  }

  private boolean dispatchToHandler(final Object event, final IEventHandler<?> handler) {
    switch (handler.getHandleThreadType()) {
      case ANOTHER:
        executorService.execute(() -> invokeHandlerMethod(event, handler));
        break;
      case MAIN:
        if (Looper.getMainLooper() == Looper.myLooper()) {
          invokeHandlerMethod(event, handler);
        } else {
          mainThreadHandler.post(() -> invokeHandlerMethod(event, handler));
        }
        break;
      case PROTO:
        return invokeHandlerMethod(event, handler);
      default:
        //impossible
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  private boolean invokeHandlerMethod(Object event, IEventHandler<?> handler) {
    return ((IEventHandler<Object>) handler).onEvent(event);
  }

  private synchronized List<IEventHandler<?>> findMethodHandler(Object eventHandler) {
    Class<?> handlerClass = eventHandler.getClass();
    List<IEventHandler<?>> entityList = new ArrayList<IEventHandler<?>>();

    Class<?> clazz = handlerClass;
    while (clazz != null) {
      for (Method method : clazz.getDeclaredMethods()) {
        Handle handleAnnotation = method.getAnnotation(Handle.class);
        if (handleAnnotation != null) {
          Class<?>[] parameterClasses = method.getParameterTypes();
          if (parameterClasses.length != 1) {
            HL.e("Register IEventHandler Failed : {}\nHandleMethod must have only 1 parameter", eventHandler.getClass().getName());
            continue;
          }
          entityList.add(new MethodEventHandler(parameterClasses[0], eventHandler, method, handleAnnotation.value()));
        }
      }

      clazz = clazz.getSuperclass();
    }
    return entityList;
  }

  private static class EventQueue extends ConcurrentLinkedQueue<Object> {
    private static final long serialVersionUID = -3677803901727752983L;

    private boolean isDispatching = false;
  }

}
