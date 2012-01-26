package util;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.Series;
import models.Study;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import play.db.jpa.GenericModel;

public class Clipboard {

	private static final char SEPARATOR = ',';

	private Set<Item> items = new HashSet<Item>();

	public Clipboard(String clipboard) {
		if (clipboard != null && !clipboard.isEmpty()) {
			for (String item : clipboard.split(String.valueOf(SEPARATOR))) {
				items.add(new Item(item));
			}
		}
	}

	public void add(String type, long pk) throws ClassNotFoundException {
		items.add(new Item(type, pk));
	}

	public void remove(String type, long pk) throws ClassNotFoundException {
		items.remove(new Item(type, pk));
	}

	public void clear() {
		items.clear();
	}

	public Set<GenericModel> getObjects() throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Set<GenericModel> objects = new HashSet<GenericModel>();
		for (Item item : items) {
			GenericModel object = (GenericModel) item.type.getMethod("findById", Object.class).invoke(null, item.pk);
			if (!(object instanceof Series) || !objects.contains(((Series) object).study)) {
				objects.add(object);
			}
		}
		return objects;
	}

	@Override
	public String toString() {
		return StringUtils.join(items, SEPARATOR);
	}

	public static class Item {

		private static final char DELIMITER = ':';
		private static final List types = Arrays.asList(Study.class, Series.class);

		public Class type;
		public long pk;

		public Item(String item) {
			type = (Class) types.get(Integer.parseInt(item.split(String.valueOf(DELIMITER))[0]));
			pk = Long.parseLong(item.split(String.valueOf(DELIMITER))[1]);
		}

		public Item(String type, long pk) throws ClassNotFoundException {
			this.type = Class.forName(String.format("models.%s", type));
			this.pk = pk;
		}

		@Override
		public int hashCode() {
			return HashCodeBuilder.reflectionHashCode(this);
		}

		@Override
		public boolean equals(Object obj) {
			return EqualsBuilder.reflectionEquals(this, obj);
		}

		@Override
		public String toString() {
			return String.format("%s%s%s", types.indexOf(type), DELIMITER, pk);
		}
	}
}
