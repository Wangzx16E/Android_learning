package kh.hyper.event;

public class RawEventHandler extends EventHandler<Object> {
	@SuppressWarnings("unchecked")
  public RawEventHandler(Class<?> eventType, HandleThreadType handleThreadType) {
    super((Class<Object>) eventType, handleThreadType);
  }

	@SuppressWarnings("unchecked")
  public RawEventHandler(Class<?> eventType) {
    super((Class<Object>) eventType);
  }

	@Override
  public boolean onEvent(Object event) {
    return false;
  }
}
