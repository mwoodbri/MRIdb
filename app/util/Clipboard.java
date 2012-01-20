package util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import models.Series;
import models.Study;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import play.db.jpa.GenericModel;

public class Clipboard {
	private static final String SEPARATOR = ",";

	protected Set<Item> items = new HashSet<Item>();

	public Clipboard(String clipboard) {
		if (clipboard != null && !clipboard.isEmpty()) {
			for (String item : clipboard.split(SEPARATOR)) {
				items.add(new Item(item));
			}
		}
	}

	public Clipboard add(String type, long pk) {
		items.add(new Item(type, pk));
		return this;
	}

	public Clipboard remove(Item item) {
		items.remove(item);
		return this;
	}

	@Override
	public String toString() {
		return StringUtils.join(items, SEPARATOR);
	}

	private static Map<String, String> types = new HashMap<String, String>() {{
		put("Study", "S");
		put("Series", "s");
	}};
	public static class Item {
		public String type;
		public long pk;
		public Item(String item) {
			type = item.substring(0, 1);
			pk = Long.parseLong(item.substring(1));
		}
		public Item(String type, long pk) {
			this.type = types.get(type);
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
			return String.format("%s%s", type, pk);
		}
	}
}
