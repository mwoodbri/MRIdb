package util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import models.DomainModel;
import play.data.binding.TypeBinder;
import util.Clipboard.Item;

public class DomainModelBinder implements TypeBinder<List> {

	@Override
	public DomainModel bind(String name, Annotation[] annotations, String value, Class actualClass, Type genericType) throws Exception {
		Item item = new Item(value);
		return (DomainModel) item.type.getMethod("findById", Object.class).invoke(null, item.pk);
	}

}