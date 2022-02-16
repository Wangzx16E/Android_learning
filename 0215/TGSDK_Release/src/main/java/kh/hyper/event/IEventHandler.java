package kh.hyper.event;

public interface IEventHandler<EventType> {
  Class<EventType> getEventType();

  HandleThreadType getHandleThreadType();

	boolean onEvent(EventType event);
}
