// Copyright 2007 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry.integration.app1.pages;

import org.apache.tapestry.integration.app1.data.ToDoItem;
import org.apache.tapestry.integration.app1.services.ToDoDatabase;
import org.apache.tapestry.ioc.annotations.Inject;

import java.util.List;

public class GridFormDemo
{
    @Inject
    private ToDoDatabase _database;

    private ToDoItem _item;

    private List<ToDoItem> _items;

    void onPrepare()
    {
        _items = _database.findAll();
    }

    void onSuccess()
    {
        // Here's the down side: we don't have a good way of identifying just what changed.
        // If we provided our own GridDataSource, we would be able to update just the items
        // currently visible. But as is, we have to update each one!

        for (ToDoItem item : _items)
            _database.update(item);
    }

    public List<ToDoItem> getItems()
    {
        return _items;
    }

    public ToDoItem getItem()
    {
        return _item;
    }

    public void setItem(ToDoItem item)
    {
        _item = item;
    }

    void onActionFromReset()
    {
        _database.clear();

        for (int i = 0; i < 20; i++)
        {
            ToDoItem item = new ToDoItem();
            item.setTitle("ToDo # " + (i + 1));
            item.setOrder(i);

            _database.add(item);
        }
    }

}