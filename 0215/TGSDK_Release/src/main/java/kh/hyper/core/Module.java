package kh.hyper.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import kh.hyper.event.EventManager;
import kh.hyper.utils.HL;

public abstract class Module {
	protected Context context;
	protected Module parentModule;
	protected Collection<Module> subModules;
	protected AtomicBoolean initialized = new AtomicBoolean(false);

	private static Map<Class<?>, Object> sInstanceCache = new HashMap<Class<?>, Object>();

	private static class KernelHolder {
		private static final Module kernel = new Module() {

			@Override
			protected void onLoad(Parameter parameter) {
				try {
					Class<?> load = Class.forName("__HExtend__");
					Field f = load.getField("LOAD_CLASS");
					Class<?>[] classes = (Class<?>[]) f.get(null);
					for (Class<?> clazz : classes) {
						Module.of(clazz);
					}
				} catch (ClassNotFoundException e) {
					HL.w("Compiler lib init failed.");
				} catch (NoSuchFieldException e) {
					HL.w("Compiler lib generate occur error.");
				} catch (IllegalAccessException e) {
					HL.w("Compiler lib generate occur error.");
				} catch (IllegalArgumentException e) {
					HL.w("Compiler lib generate occur error.");
				}
			}

			@Override
			protected void onRelease() {
				sInstanceCache.clear();
				EventManager.instance.clear();
			}
		};
	}

	public static Module kernel() {
		return KernelHolder.kernel;
	}

	public synchronized void initialize(Context context, Parameter parameter) {
		if (initialized.get()) {
			HL.w("Module {} already initialized.", this);
			return;
		}
		initialized.set(true);
    if (context==null) return;
		this.context = context.getApplicationContext();
		if (kernel() != this) {
			kernel().load(this, parameter);
		} else {
			onLoadInternal(null, parameter);
		}
	}

	public synchronized void initialize(Context context) {
		initialize(context, null);
	}

	@SuppressWarnings("unchecked")
	public static <T> T of(Class<T> sClass, Parameter parameter) {
		T instance = (T) sInstanceCache.get(sClass);
		if (instance == null) {
			synchronized (sClass) {
				Class<?> instanceClass = sClass;
				if (sClass.isAnnotationPresent(IM.class)) {
					instanceClass = sClass.getAnnotation(IM.class).value();
				}
				try {
					instance = (T) instanceClass.newInstance();
					sInstanceCache.put(sClass, instance);
					if (instance instanceof Module) {
						((Module) instance).initialize(kernel().context, parameter);
					}
				} catch (InstantiationException e) {
          HL.e(e);
				} catch (IllegalAccessException e) {
          HL.e(e);
				}
			}
		}
		return instance;
	}

	public static <T> T of(Class<T> sClass) {
		return of(sClass, null);
	}

	protected abstract void onLoad(Parameter parameter);

	protected abstract void onRelease();

	public Context getContext() {
		return context;
	}

	public void load(Module module) {
		load(module, null);
	}

	public void load(Module module, Parameter parameter) {
		if (subModules == null) {
			subModules = new ConcurrentLinkedQueue<Module>();
		}
		if (subModules.contains(module)) {
			throw new IllegalArgumentException("Extend a Module that has been already extended.");
		}
		subModules.add(module);
		module.onLoadInternal(this, parameter);
	}

	public void release() {
		onReleaseInternal();
	}

	protected void loadAnnotationModules() {
		for (Field field : getClass().getDeclaredFields()) {
			IM im = field.getAnnotation(IM.class);
			if (im != null) {
				Class<?> moduleClass = im.value();
				if (moduleClass == void.class) {
					moduleClass = field.getType();
				}
				if (!Module.class.isAssignableFrom(moduleClass)) {
					throw new RuntimeException(moduleClass.getName() + " is not Module Class.");
				}
				try {
					Module m = (Module) moduleClass.newInstance();
					if (Modifier.isStatic(field.getModifiers())) {
						field.set(null, m);
					} else {
						field.set(this, m);
					}
					load(m);
				} catch (InstantiationException e) {
					HL.w("Create Module Instance Failed : InstantiationException");
					HL.w(e.getMessage());
				} catch (IllegalAccessException e) {
					HL.w("Create Module Instance Failed : IllegalAccessException");
					HL.w(e.getMessage());
				}
			}
		}
	}

	protected void onLoadInternal(Module parentModule, Parameter parameter) {
		this.parentModule = parentModule;
		if (parentModule != null) {
			this.context = parentModule.context;
		}
		loadAnnotationModules();
		onLoad(parameter);
	}

	protected void onReleaseInternal() {
		if (subModules != null) {
			for (Module module : subModules) {
				module.onReleaseInternal();
			}
			subModules.clear();
			subModules = null;
		}
		onRelease();
		initialized.set(false);
		if (parentModule != null) {
			parentModule.subModules.remove(this);
			parentModule = null;
		}
		context = null;
	}
}
