# Vent

Vent is a simple type-based event system for generic purposes.

## Installation with Gradle

Add the following to your repositories:

```
maven { url 'https://jitpack.io' }
```

And the following to your dependencies:

```
compile 'com.github.manabreak:vent:1.2'
```

## Usage

Either create a new event system:

```
EventSystem system = new EventSystem();
```

...or use the default singleton event system:

```
EventSystem system = DefaultEventSystem.getInstance();
```

Next, create your event class:

```
public class LevelCompletedEvent {
    public String nextLevel;
}
```

Register your subscriber for the event:

```
system.subscribe(LevelCompletedEvent.class, event -> {
    System.out.println("Level complete! Next level: " + event.nextLevel);
});
```

And finally, post your events:

```
LevelCompletedEvent event = new LevelCompletedEvent();
event.nextLevel = "level_two";

// Queues up the event, but won't notify the subscribers yet
system.post(event);

// Call process() to notify subscribers and dispose the event
system.process();

// Or alternatively, use postImmediate():
system.postImmediate(event);
```

## `before` and `after`

You can register subscribers for handling events before and after
the ordinary event handling:

```
// Called before other subscribers are notified
system.before(LevelCompletedEvent.class, event -> { });

// Called after the event is handled
system.after(LevelCompletedEvent.class, event -> { });
```

## Performance considerations

You shouldn't usually create new events using the `new` operator,
at least not in performance-critical programs such as games.
Instead, create new ones only when you need them, and re-use existing
events.


## Events posted during processing

If an event handler posts an event in response to some other event, this
newly posted event will be queued up for the next processing cycle. This
means that you can do stuff like this:

    EventSystem.subscribe(FooEvent.class, foo -> EventSystem.post(new BarEvent());
    EventSystem.post(new FooEvent());
    EventSystem.process(); // The FooEvent will be processed and BarEvent will be queued
    EventSystem.process(); // BarEvent will be processed
