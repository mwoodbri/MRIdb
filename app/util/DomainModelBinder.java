package util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import models.DomainModel;
import play.data.binding.TypeBinder;
import util.Clipboard.Item;

public class DomainModelBinder implements TypeBinder<DomainModel[]> {

	@Override
	public DomainModel[] bind(String name, Annotation[] annotations, String value, Class actualClass, Type genericType) throws Exception {
		String[] items = value.split(String.valueOf(Clipboard.SEPARATOR));
		DomainModel[] models = new DomainModel[items.length];
		for (int i = 0; i < items.length; i++) {
			Item item = new Item(items[i]);
			models[i] = (DomainModel) item.type.getMethod("findById", Object.class).invoke(null, item.pk);
		}
		return models;
	}

}