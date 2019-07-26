package gov.nih.tbi.commons.ws.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonJaxrsProvider<T> implements MessageBodyReader<T>, MessageBodyWriter<T> {

	private final Gson gson;

	public GsonJaxrsProvider() {
		gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
	}

	@Override
	public long getSize(T object, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return 0;
	}

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return mediaType.equals(MediaType.APPLICATION_JSON) || mediaType.equals(MediaType.APPLICATION_JSON_TYPE);
	}

	@Override
	public void writeTo(T object, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) 
			throws IOException, WebApplicationException {
		PrintWriter printWriter = new PrintWriter(entityStream);
		try {
			String json = gson.toJson(object);
			printWriter.write(json);
			printWriter.flush();
		} finally {
			printWriter.close();
		}

	}

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return true;
	}

	@Override
	public T readFrom(Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) 
			throws IOException, WebApplicationException {
		InputStreamReader reader = new InputStreamReader(entityStream, "UTF-8");
		try {
			return gson.fromJson(reader, genericType);
		} finally {
			reader.close();
		}
	}
}
