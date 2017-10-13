package com.axel_stein.noteapp.utils;

import java.util.ArrayList;

public class SqlTableCreator {
    private ArrayList<Item> items;

    public SqlTableCreator() {
        items = new ArrayList<>();
    }

    public Text text(String title) {
        Text text = new Text(title);
        items.add(text);
        return text;
    }

    public Int integer(String title) {
        Int integer = new Int(title);
        items.add(integer);
        return integer;
    }

    public Bool bool(String title) {
        Bool bool = new Bool(title);
        items.add(bool);
        return bool;
    }

    public String create(String table) {
        if (items.size() == 0) {
            throw new RuntimeException("Items are empty");
        }

        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE ");
        builder.append(table);
        builder.append(" (");
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            builder.append(item.mTitle);

            if (item instanceof Text) {
                builder.append(" TEXT");
            } else if (item instanceof Int) {
                Int integer = (Int) item;
                builder.append(" INTEGER");
                if (integer.primaryKey) {
                    builder.append(" PRIMARY KEY");
                }
                if (integer.autoIncrement) {
                    builder.append(" AUTOINCREMENT");
                }
            } else if (item instanceof Bool) {
                builder.append(" INTEGER");
            }

            if (item.isDefaultSet()) {
                builder.append(" DEFAULT ");
                builder.append(item.getDefault());
            }
            if (item.isNotNull()) {
                builder.append(" NOT NULL");
            }
            if (item.isUnique()) {
                builder.append(" UNIQUE");
            }

            if (i != items.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append(");");
        return builder.toString();
    }

    private static class Item<T> {
        private String mTitle;
        private boolean mNotNull;
        private boolean mUnique;
        private boolean mDefaultSet;
        private T mDef;

        public Item(String title) {
            mTitle = title;
        }

        public String getTitle() {
            return mTitle;
        }

        public Item<T> notNull() {
            mNotNull = true;
            return this;
        }

        public Item defaultValue(T t) {
            mDefaultSet = true;
            mDef = t;
            return this;
        }

        public Item unique() {
            mUnique = true;
            return this;
        }

        public boolean isNotNull() {
            return mNotNull;
        }

        public boolean isUnique() {
            return mUnique;
        }

        public boolean isDefaultSet() {
            return mDefaultSet;
        }

        public String getDefault() {
            return mDef.toString();
        }
    }

    public static final class Text extends Item<String> {

        public Text(String title) {
            super(title);
        }
    }

    public static final class Int extends Item<Integer> {
        private boolean primaryKey;
        private boolean autoIncrement;

        public Int(String title) {
            super(title);
        }

        public Int primaryKey() {
            primaryKey = true;
            return this;
        }

        public Int autoIncrement() {
            autoIncrement = true;
            return this;
        }
    }

    public static final class Bool extends Item<Boolean> {
        private int def;

        public Bool(String title) {
            super(title);
            defaultValue(false);
        }

        @Override
        public Item defaultValue(Boolean bool) {
            def = bool ? 1 : 0;
            return super.defaultValue(bool);
        }

        @Override
        public String getDefault() {
            return String.valueOf(def);
        }
    }

}