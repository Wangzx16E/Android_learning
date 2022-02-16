package kh.hyper.event;

public abstract class EventHandler<EventType> implements IEventHandler<EventType> {
	private Class<EventType> eventType;
	private HandleThreadType handleThreadType;

	public EventHandler(Class<EventType> eventType, HandleThreadType handleThreadType) {
		this.eventType = eventType;
		this.handleThreadType = handleThreadType;
	}

	public EventHandler(Class<EventType> eventType) {
		this(eventType, HandleThreadType.PROTO);
	}

	@Override
	public Class<EventType> getEventType() {
		return eventType;
	}

	@Override
	public HandleThreadType getHandleThreadType() {
		return handleThreadType;
	}

}
