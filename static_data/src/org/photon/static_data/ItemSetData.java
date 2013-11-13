package org.photon.static_data;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.photon.jackson.flatjson.OneToMany;
import scala.collection.Seq;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = ItemSetData.class)
public class ItemSetData {

	private final long id;

	@OneToMany
	private final Seq<ItemData> items;

	public ItemSetData(long id, Seq<ItemData> items) {
		this.id = id;
		this.items = items;
	}
}
