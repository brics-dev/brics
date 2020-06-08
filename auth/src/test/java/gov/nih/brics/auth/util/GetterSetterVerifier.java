package gov.nih.brics.auth.util;

import static org.assertj.core.api.Assertions.*;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import javax.validation.constraints.NotNull;

import org.assertj.core.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Automates JUnit testing of simple getter / setter methods which make up the
 * entirety of a "Plain Old Java Object" (or POJO). The {@link #verify()} method
 * in this class was modeled after the
 * <a href="http://www.jqno.nl/equalsverifier/">EqualsVerifier</a> pattern.
 * 
 * <p>
 * To create a JUnit test for testing all classes within a package:
 * </p>
 * 
 * <pre>
 * public class PackagePOJOTest {
 * 	&#64;Test
 * 	public void testAllGettersAndSettersInThisPackage() throws Exception {
 * 		GetterSetterVerifier.testGettersAndSettersInPackage(this.getClass().getPackageName(), this.getClass());
 * 	}
 * }
 * </pre>
 *
 * <p>
 * If you need more fine-grained control, you can also create a single POJO test
 * using the following:
 * </p>
 * 
 * <pre>
 * &#64;Test
 * public void testGettersAndSetters() {
 * 	GetterSetterVerifier.forClass(MyPojoClass.class).verify();
 * }
 * </pre>
 *
 * <p>
 * You can also specify which properties you do no want to test in the event
 * that the associated getters and setters are non-trivial. For example:
 * </p>
 *
 * <pre>
 * GetterSetterVerifier.forClass(MyClass.class).exclude("someComplexProperty").exclude("anotherComplexProperty")
 * 		.verify();
 * </pre>
 *
 * <p>
 * On the other hand, if you'd rather be more verbose about what properties are
 * tested, you can specify them using the include syntax. When using the include
 * approach, only the properties that you specified will be tested. For example:
 * </p>
 * 
 * <pre>
 * GetterSetterVerifier.forClass(MyClass.class).include("someSimpleProperty").include("anotherSimpleProperty").verify();
 * </pre>
 */
public class GetterSetterVerifier<T> {
	private static final Logger logger = LoggerFactory.getLogger(GetterSetterVerifier.class);

	private Class<T> type;
	private Set<String> excludes;
	private Set<String> includes;

	/**
	 * Creates a getter / setter verifier to test properties for a particular class.
	 *
	 * @param type The class that we are testing
	 */
	private GetterSetterVerifier(@NotNull final Class<T> type) {
		this.type = type;
	}

	/**
	 * Method used to identify the properties that we are going to test. If no
	 * includes are specified, then all the properties are considered for testing.
	 *
	 * @param include The name of the property that we are going to test.
	 * @return This object, for method chaining.
	 */
	public GetterSetterVerifier<T> include(@NotNull final String include) {
		if (includes == null) {
			includes = new HashSet<>();
		}

		includes.add(include);
		return this;
	}

	/**
	 * Method used to identify the properties that will be ignored during testing.
	 * If no excludes are specified, then no properties will be excluded.
	 *
	 * @param exclude The name of the property that we are going to ignore.
	 * @return This object, for method chaining.
	 */
	public GetterSetterVerifier<T> exclude(@NotNull final String exclude) {
		if (excludes == null) {
			excludes = new HashSet<>();
		}

		excludes.add(exclude);
		return this;
	}

	/**
	 * Verify the class's getters and setters
	 */
	public void verify() {
		try {
			final BeanInfo beanInfo = Introspector.getBeanInfo(type);
			final PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();

			for (final PropertyDescriptor property : properties) {
				if (shouldTestProperty(property)) {
					testProperty(property);
				}
			}
		} catch (final Exception e) {
			throw new AssertionError(e.getMessage());
		}
	}

	/**
	 * Determine if we need to test the property based on a few conditions. 1. The
	 * property has both a getter and a setter. 2. The property was not excluded. 3.
	 * The property was considered for testing.
	 *
	 * @param property The property that we are determining if we going to test.
	 * @return True if we should test the property. False if we shouldn't.
	 */
	private boolean shouldTestProperty(@NotNull final PropertyDescriptor property) {
		if (property.getWriteMethod() == null || property.getReadMethod() == null) {
			return false;
		} else if (excludes != null && excludes.contains(property.getDisplayName())) {
			return false;
		}

		return includes == null || includes.contains(property.getDisplayName());
	}

	/**
	 * Test an individual property by getting the read method and write method and
	 * passing the default value for the type to the setter and asserting that the
	 * same value was returned.
	 *
	 * @param property The property that we are testing.
	 *
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws InvocationTargetException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 */
	private void testProperty(@NotNull final PropertyDescriptor property)
			throws IllegalAccessException, InstantiationException, InvocationTargetException, IllegalArgumentException,
			NoSuchMethodException, SecurityException {
		final Object target = type.getConstructor().newInstance();
		final Object setValue = defaultValue(property.getPropertyType());

		final Method getter = property.getReadMethod();
		final Method setter = property.getWriteMethod();

		setter.invoke(target, setValue);
		final Object getValue = getter.invoke(target);

		assertThat(getValue).isEqualTo(setValue);
	}

	/**
	 * Factory method for easily creating a test for the getters and setters.
	 *
	 * @param type The class that we are testing the getters and setters for.
	 * @return An object that can be used for testing the getters and setters of a
	 *         class.
	 */
	public static <T> GetterSetterVerifier<T> forClass(@NotNull final Class<T> type) {
		return new GetterSetterVerifier<T>(type);
	}

	/**
	 * Returns the default value of {@code type} as defined by JLS --- {@code 0} for
	 * numbers, {@code
	 * false} for {@code boolean} and {@code '\0'} for {@code char}. For
	 * non-primitive types and {@code void}, {@code null} is returned.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T defaultValue(Class<T> type) {
		if (type == null) {
			throw new NullPointerException("'type' cannot be null");
		}

		if (type == boolean.class) {
			return (T) Boolean.FALSE;
		} else if (type == char.class) {
			return (T) Character.valueOf('\0');
		} else if (type == byte.class) {
			return (T) Byte.valueOf((byte) 0);
		} else if (type == short.class) {
			return (T) Short.valueOf((short) 0);
		} else if (type == int.class) {
			return (T) Integer.valueOf(0);
		} else if (type == long.class) {
			return (T) Long.valueOf(0L);
		} else if (type == float.class) {
			return (T) Float.valueOf(0f);
		} else if (type == double.class) {
			return (T) Double.valueOf(0d);
		} else {
			return null;
		}
	}

	/**
	 * Executes {@link #verify()} for every class in the provided package.
	 * 
	 * @param packageName the name of the package from
	 *                    {@code class.getPackageName()}
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void testGettersAndSettersInPackage(String packageName, Class... exclusion) throws Exception {
		List<Class> classesInPackage = getClassesInPackage(packageName);
		HashSet<Class> classesToExclude = new HashSet(Arrays.asList(exclusion));
		for (Class classInPackage : classesInPackage) {
			if (classesToExclude.contains(classInPackage)) {
				// skipping, because this is an exclusion
				continue;
			}
			logger.debug("verifying Getters/Setters for: " + classInPackage.getCanonicalName());
			GetterSetterVerifier.forClass(classInPackage).verify();
		}
	}

	/**
	 * @param packageName the name of the package from
	 *                    {@code class.getPackageName()}
	 * @return a list of all Class objects in this package.
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public static List<Class> getClassesInPackage(final String packageName) throws Exception {
		final StandardJavaFileManager fileManager = ToolProvider.getSystemJavaCompiler().getStandardFileManager(null,
				null, null);
		Iterable<JavaFileObject> javaFileObjectList = fileManager.list(StandardLocation.CLASS_PATH, packageName,
				Collections.singleton(JavaFileObject.Kind.CLASS), false);

		List<Class> classesInPackage = new ArrayList<>();
		for (JavaFileObject javaFileObject : javaFileObjectList) {
			try {
				final String[] split = javaFileObject.getName().replace(".class", "").replace(")", "")
						.split(Pattern.quote(File.separator));

				final String fullClassName = packageName + "." + split[split.length - 1];
				classesInPackage.add(Class.forName(fullClassName));
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		return classesInPackage;
	}
}
