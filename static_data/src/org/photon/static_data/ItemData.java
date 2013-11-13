package org.photon.static_data;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.photon.jackson.flatjson.ManyToOne;
import scala.Option;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = ItemData.class)
public class ItemData {

	private final long id;

	@ManyToOne
	private final Option<ItemSetData> itemSet;

	public ItemData(long id, Option<ItemSetData> itemSet) {
		this.id = id;
		this.itemSet = itemSet;
	}

	public long getId() {
		return id;
	}

	public Option<ItemSetData> getItemSet() {
		return itemSet;
	}
}
