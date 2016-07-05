package naga.core.orm.entity.impl;

import naga.core.orm.entity.Entity;
import naga.core.orm.entity.EntityList;
import naga.core.orm.entity.EntityStore;
import naga.core.util.collection.Collections;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Bruno Salmon
 */
public class EntityListImpl implements EntityList {

    private final Object listId;
    private final EntityStore store;
    private ArrayList<Entity> list = new ArrayList<>();

    public EntityListImpl(Object listId, EntityStore store) {
        this.listId = listId;
        this.store = store;
    }

    @Override
    public Object getListId() {
        return listId;
    }

    @Override
    public EntityStore getStore() {
        return store;
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public Entity get(int index) {
        return list.get(index);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public void add(Entity entity) {
        list.add(entity);
    }

    @Override
    public Iterator<Entity> iterator() {
        return list.iterator();
    }

    @Override
    public String toString() {
        return Collections.toString(iterator());
    }
}
