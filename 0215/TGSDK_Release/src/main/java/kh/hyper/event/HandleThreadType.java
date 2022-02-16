package kh.hyper.event;

public enum HandleThreadType {
	// Another background thread
	ANOTHER,

	// Android main thread
	MAIN,

	// The thread which dispatch the event
	PROTO
}
