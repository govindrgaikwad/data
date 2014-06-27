package com.tmg.generator.basic.service;

import java.io.IOException;
import java.lang.reflect.Method;
import java.security.SecureClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;

/**
 * @author Govind Gaikwad
 * 
 * @version 1.0 June 27, 2014.
 */
public class ClassFileManager extends
		ForwardingJavaFileManager<StandardJavaFileManager> {

	private Map<String, JavaClassObject> classDefinitions = new HashMap<String, JavaClassObject>();

	private ClassLoader parentClassLoader = null;

	private ClassLoader loader = null;

	private List<String> classLoadSequence;

	/**
	 * Will initialize the manager with the specified standard java file manager
	 * 
	 * @param StandardJavaFileManager
	 */
	public ClassFileManager(StandardJavaFileManager standardManager) {
		super(standardManager);
		this.parentClassLoader = getClass().getClassLoader();
	}

	/**
	 * Will be used by us to get the class loader for our compiled class. It
	 * creates an anonymous class extending the SecureClassLoader which uses the
	 * byte code created by the compiler and stored in the JavaClassObject, and
	 * returns the Class for it
	 * 
	 * @param Location
	 */
	@Override
	public ClassLoader getClassLoader(Location location) {
		loader = new SecureClassLoader() {
			@Override
			protected Class<?> findClass(String name)
					throws ClassNotFoundException {

				Class<?> result = null;
				try {
					result = parentClassLoader.loadClass(name);
					if (result != null) {
						return result;
					}
				} catch (ClassNotFoundException cnfe) {

				}

				for (String key : classLoadSequence) {
					JavaClassObject jclassObject = classDefinitions.get(key);
					byte[] b = jclassObject.getBytes();
					try {
						Method m = ClassLoader.class.getDeclaredMethod(
								"defineClass", new Class[] { String.class,
										byte[].class, int.class, int.class });
						m.setAccessible(true);
						m.invoke(parentClassLoader, key,
								jclassObject.getBytes(), 0, b.length);
					} catch (Exception e) {
						// e.printStackTrace();
					}
					try {
						result = parentClassLoader.loadClass(name);
						if (result != null) {
							return result;
						}
					} catch (ClassNotFoundException cnfe) {

					}
				}

				JavaClassObject jclassObject = classDefinitions.get(name);
				if (jclassObject != null) {
					byte[] b = jclassObject.getBytes();
					return super.defineClass(name, jclassObject.getBytes(), 0,
							b.length);

				}
				return parentClassLoader.loadClass(name);

			}
		};
		return loader;
	}

	/**
	 * Gives the compiler an instance of the JavaClassObject so that the
	 * compiler can write the byte code into it.
	 */
	@Override
	public JavaFileObject getJavaFileForOutput(Location location,
			String className, Kind kind, FileObject sibling) throws IOException {
		JavaClassObject jclassObject = new JavaClassObject(className, kind);
		this.classDefinitions.put(className, jclassObject);
		return jclassObject;
	}

	public List<String> getClassLoadSequence() {
		return classLoadSequence;
	}

	public void setClassLoadSequence(List<String> classLoadSequence) {
		this.classLoadSequence = classLoadSequence;
	}

}